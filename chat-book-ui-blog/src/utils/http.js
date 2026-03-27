import axios from 'axios';
import router from "@/router/index.js";
import { ElMessage, ElNotification } from "element-plus";
import { API_CONFIG } from "@/config/index.js";
import { HTTP_STATUS } from "@/constants/index.js";
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { getAccessToken, refreshAccessToken, clearTokens } from '@/utils/token.js';

/**
 * NProgress 配置
 */
NProgress.configure({
    showSpinner: false,
    speed: 500,
    minimum: 0.3
});

/**
 * 存储等待中的请求，用于防止重复请求
 */
const pendingMap = new Map();

/**
 * 仅对非 GET 请求做重复提交拦截，允许页面初始化阶段并发拉取相同数据。
 */
function shouldTrackRequest(config) {
    const method = String(config?.method || 'get').toLowerCase();
    return !['get', 'head', 'options'].includes(method);
}

/**
 * 生成唯一的请求key
 */
function getRequestKey(config) {
    const { method, url, params, data } = config;
    return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&');
}

/**
 * 移除重复请求
 */
function removePending(config) {
    if (!shouldTrackRequest(config)) {
        return;
    }

    const key = getRequestKey(config);
    if (pendingMap.has(key)) {
        const controller = pendingMap.get(key);
        controller.abort();
        pendingMap.delete(key);
    }
}

/**
 * 创建 Axios 实例
 */
const service = axios.create({
    baseURL: API_CONFIG.baseURL + '/api',
    timeout: API_CONFIG.timeout || 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

/**
 * 请求拦截器
 */
service.interceptors.request.use(
    (config) => {
        NProgress.start();

        if (shouldTrackRequest(config)) {
            removePending(config);
            const controller = new AbortController();
            config.signal = controller.signal;
            pendingMap.set(getRequestKey(config), controller);
        }

        const token = getAccessToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
            config.headers['token'] = token;
        }
        return config;
    },
    (error) => {
        console.error('[Request Error]:', error);
        return Promise.reject(error);
    }
);

/**
 * 统一错误提示处理
 */
function handleError(code, msg) {
    let displayMsg = msg || '系统操作异常';
    if (msg?.includes('timeout')) displayMsg = '请求超时，请稍后重试';
    if (msg?.includes('Network Error')) displayMsg = '网络连接异常';

    if (code === HTTP_STATUS.UNAUTHORIZED || code === 401) {
        if (!window.isUnauthorizedNotifying) {
            window.isUnauthorizedNotifying = true;
            ElNotification.error({
                title: '登录失效',
                message: '正在尝试刷新登录状态...',
                duration: 2000,
                onClose: () => { window.isUnauthorizedNotifying = false; }
            });
        }
        return;
    }

    ElMessage({
        message: displayMsg,
        type: 'error',
        duration: 3 * 1000
    });
}

/**
 * 响应拦截器
 */
service.interceptors.response.use(
    (response) => {
        NProgress.done();
        removePending(response.config);

        const res = response.data;

        if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
            return res;
        }

        if (res.code === 200 || res.code === 0 || !res.code) {
            return res.data !== undefined ? res.data : res;
        }

        handleError(res.code, res.msg);
        return Promise.reject(new Error(res.msg || 'Error'));
    },
    async (error) => {
        NProgress.done();

        if (axios.isCancel(error)) {
            console.log('[Request Cancelled]:', error.message);
            return new Promise(() => {});
        }

        if (error.config) {
            removePending(error.config);
        }

        const { response, message } = error;
        const status = response ? response.status : null;
        const errorMsg = response?.data?.msg || message;

        // 401: 尝试自动刷新 Token
        if (status === 401) {
            if (!window._isRefreshing) {
                window._isRefreshing = true;
                try {
                    const newToken = await refreshAccessToken();
                    window._isRefreshing = false;
                    if (newToken) {
                        // Token 已刷新，重新发起原请求
                        error.config.headers['Authorization'] = `Bearer ${newToken}`;
                        error.config.headers['token'] = newToken;
                        return service(error.config);
                    }
                } catch (e) {
                    window._isRefreshing = false;
                }
            }

            // 刷新失败，跳转登录页
            clearTokens();
            if (router.currentRoute.value.name !== 'Login') {
                router.push({ name: 'Login' });
            }
            return Promise.reject(error);
        }

        handleError(status, errorMsg);
        return Promise.reject(error);
    }
);

/**
 * 检查是否登录
 * @returns {boolean} true: 已登录 false: 未登录
 */
export function checkLogin() {
    const token = getAccessToken();
    if (!token) {
        ElMessage.warning('请先登录');
        router.push('/login');
        return false;
    }
    return true;
}

export default service;
