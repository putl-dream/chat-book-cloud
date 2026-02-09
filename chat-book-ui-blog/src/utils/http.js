import axios from 'axios';
import router from "@/router/index.js";
import { ElMessage, ElNotification } from "element-plus";
import { API_CONFIG } from "@/config/index.js";
import { HTTP_STATUS } from "@/constants/index.js";

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
        const token = localStorage.getItem('token');
        if (token) {
            // 兼容多种 Token 传递方式
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
        const res = response.data;

        // 兼容不同的后端返回结构
        // 如果没有 code 或者 code 为 200/0，认为是成功的
        if (res.code === 200 || res.code === 0 || !res.code) {
            return res.data !== undefined ? res.data : res;
        }

        // 业务错误处理
        handleBusinessError(res);
        return Promise.reject(new Error(res.msg || 'Error'));
    },
    (error) => {
        handleHttpError(error);
        return Promise.reject(error);
    }
);

/**
 * 处理业务错误
 */
function handleBusinessError(res) {
    const { code, msg } = res;
    
    // 如果是登录失效
    if (code === HTTP_STATUS.UNAUTHORIZED || code === 401) {
        ElNotification.error({
            title: '登录失效',
            message: '您的身份信息已过期，请重新登录',
            duration: 3000
        });
        localStorage.removeItem('token');
        localStorage.removeItem('avatar');
        if (router.currentRoute.value.name !== 'Login') {
            router.push({ name: 'Login' });
        }
        return;
    }

    ElMessage({
        message: msg || '系统操作异常',
        type: 'error',
        duration: 3 * 1000
    });
}

/**
 * 处理 HTTP 网络错误
 */
function handleHttpError(error) {
    console.error('[HTTP Error]:', error);
    let message = '网络连接异常';
    
    if (error.response) {
        const status = error.response.status;
        switch (status) {
            case 401:
                message = '未授权，请登录';
                localStorage.removeItem('token');
                router.push({ name: 'Login' });
                break;
            case 403:
                message = '拒绝访问';
                break;
            case 404:
                message = `请求地址出错: ${error.response.config.url}`;
                break;
            case 500:
                message = '服务器内部错误';
                break;
            default:
                message = error.response.data?.msg || `连接错误 ${status}`;
        }
    } else if (error.message.includes('timeout')) {
        message = '请求超时，请稍后重试';
    }

    ElMessage({
        message: message,
        type: 'error',
        duration: 5 * 1000
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