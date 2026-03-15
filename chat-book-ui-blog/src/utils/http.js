import axios from 'axios';
import router from "@/router/index.js";
import { ElMessage, ElNotification } from "element-plus";
import { API_CONFIG } from "@/config/index.js";
import { HTTP_STATUS } from "@/constants/index.js";
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

/**
 * NProgress 配置
 */
NProgress.configure({ 
    showSpinner: false, // 是否显示加载进度环
    speed: 500,         // 动画速度
    minimum: 0.3        // 最小百分比
});

/**
 * 存储等待中的请求，用于防止重复请求
 */
const pendingMap = new Map();

/**
 * 生成唯一的请求 key
 */
function getRequestKey(config) {
    const { method, url, params, data } = config;
    return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&');
}

/**
 * 移除重复请求
 */
function removePending(config) {
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
    baseURL: API_CONFIG.baseURL,
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
        // 开启进度条
        NProgress.start();

        // 重复请求拦截：如果已经有相同的请求在进行，则取消之前的请求
        removePending(config);
        const controller = new AbortController();
        config.signal = controller.signal;
        pendingMap.set(getRequestKey(config), controller);

        const token = localStorage.getItem('token');
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
 * 响应拦截器
 */
service.interceptors.response.use(
    (response) => {
        // 关闭进度条
        NProgress.done();

        // 请求完成，从队列中移除
        removePending(response.config);

        const res = response.data;
        
        // 兼容处理：二进制数据直接返回
        if (response.request.responseType === 'blob' || response.request.responseType === 'arraybuffer') {
            return res;
        }

        // 统一成功逻辑判断 (code 为 200, 0 或不存在 code 时视为成功)
        if (res.code === 200 || res.code === 0 || !res.code) {
            return res.data !== undefined ? res.data : res;
        }

        // 业务逻辑错误
        handleError(res.code, res.msg);
        return Promise.reject(new Error(res.msg || 'Error'));
    },
    (error) => {
        // 关闭进度条
        NProgress.done();

        // 如果是手动取消的请求，不报错
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

        handleError(status, errorMsg);
        return Promise.reject(error);
    }
);

/**
 * 统一错误提示处理
 */
function handleError(code, msg) {
    // 1. 登录失效/未授权处理
    if (code === HTTP_STATUS.UNAUTHORIZED || code === 401) {
        // 避免多个并发请求导致弹出重复的登录失效通知
        if (!window.isUnauthorizedNotifying) {
            window.isUnauthorizedNotifying = true;
            ElNotification.error({
                title: '登录失效',
                message: '您的身份信息已过期，请重新登录',
                duration: 3000,
                onClose: () => { window.isUnauthorizedNotifying = false }
            });
            
            // 清除用户信息并跳转
            localStorage.removeItem('token');
            localStorage.removeItem('avatar');
            if (router.currentRoute.value.name !== 'Login') {
                router.push({ name: 'Login' });
            }
        }
        return;
    }

    // 2. 根据不同的 HTTP 状态码定制提示（如有需要可扩展）
    let displayMsg = msg || '系统操作异常';
    if (msg?.includes('timeout')) displayMsg = '请求超时，请稍后重试';
    if (msg?.includes('Network Error')) displayMsg = '网络连接异常';

    // 3. 弹出错误提示
    ElMessage({
        message: displayMsg,
        type: 'error',
        duration: 3 * 1000
    });
}

/**
 * 检查是否登录
 * @returns {boolean} true: 已登录, false: 未登录
 */
export function checkLogin() {
    const token = localStorage.getItem('token');
    if (!token) {
        ElMessage.warning('请先登录');
        router.push('/login');
        return false;
    }
    return true;
}

export default service;