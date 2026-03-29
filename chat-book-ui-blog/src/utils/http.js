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

function isUnauthorized(code) {
    return code === HTTP_STATUS.UNAUTHORIZED || code === 401;
}

function isRefreshRequest(config) {
    const url = String(config?.url || '');
    return url.includes('/auth/account/refresh');
}

function redirectToLogin() {
    clearTokens();
    if (router.currentRoute.value.name !== 'Login') {
        router.push({ name: 'Login' });
    }
}

async function tryRefreshAndRetry(originalRequest) {
    if (!originalRequest || originalRequest._retry || isRefreshRequest(originalRequest)) {
        return null;
    }

    originalRequest._retry = true;
    const newToken = await refreshAccessToken();
    if (!newToken) {
        return null;
    }

    // 旧请求在 removePending 中可能已被 AbortController 标记为中止，重试前需要移除 signal。
    if (originalRequest.signal) {
        delete originalRequest.signal;
    }

    originalRequest.headers = originalRequest.headers || {};
    originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
    originalRequest.headers['token'] = newToken;
    return service(originalRequest);
}

/**
 * 响应拦截器
 */
service.interceptors.response.use(
    async (response) => {
        NProgress.done();
        removePending(response.config);

        const res = response.data;

        if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
            return res;
        }

        if (res.code === 200 || res.code === 0 || !res.code) {
            return res.data !== undefined ? res.data : res;
        }

        if (isUnauthorized(res.code)) {
            handleError(res.code, res.msg);
            const retryResponse = await tryRefreshAndRetry(response.config);
            if (retryResponse) {
                return retryResponse;
            }
            redirectToLogin();
            return Promise.reject(new Error(res.msg || 'Unauthorized'));
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
        const originalRequest = error.config;

        // HTTP 401: 尝试自动刷新 Token
        if (isUnauthorized(status)) {
            handleError(status, errorMsg);
            const retryResponse = await tryRefreshAndRetry(originalRequest);
            if (retryResponse) {
                return retryResponse;
            }
            redirectToLogin();
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
