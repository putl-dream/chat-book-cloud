import { AUTH_CONFIG } from '@/config'
import request from '@/utils/http.js'

export function login(params) {
    return request.post('/auth/account/login', {
        ...params,
        loginMethod: 'PASSWORD'
    });
}

export function loginByEmailCode(params) {
    return request.post('/auth/account/login', {
        email: params.email,
        loginMethod: 'VERIFICATION_CODE',
        verificationCode: params.verificationCode
    });
}

export function signUp(params) {
    return request.post('/auth/account/registered', params);
}

export function captcha(email) {
    return request.get('/auth/account/captcha', { params: { email } });
}

export function getUserBySelf() {
    return request.get('/user/bySelf');
}

export function getUserById(id) {
    return request.get('/user/byId', { params: { id } });
}

export function updateUser(data) {
    return request.post('/user/update', data);
}

export function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/user/file/avatar/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export function oauthLogin(provider) {
    window.location.href = `${AUTH_CONFIG.baseURL}/oauth2/authorization/${provider}`;
}
