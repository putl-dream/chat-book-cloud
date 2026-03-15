import request from '@/utils/http.js'

/**
 * login
 * @param {object} params 登录
 * @param {string} params.email 邮箱
 * @param {string} params.password 密码
 * @param {string} params.loginMethod PASSWORD
 * @returns
 */
export function login(params) {
    return request.post(`/auth/account/login`, {
        ...params,
        loginMethod: 'PASSWORD'
    });
}

/**
 * 验证码登录
 * @param {object} params 登录
 * @param {string} params.email 邮箱
 * @param {string} params.verificationCode 验证码
 * @returns
 */
export function loginByEmailCode(params) {
    return request.post(`/auth/account/login`, {
        email: params.email,
        loginMethod: 'VERIFICATION_CODE',
        verificationCode: params.verificationCode
    });
}

/**
 * signIn
 * @param {object} params 注册
 * @param {string} params.username 用户名
 * @param {string} params.email 邮箱
 * @param {string} params.password 密码
 * @param {string} params.captcha 验证码
 * @returns
 */
export function signUp(params) {
    return request.post(`/auth/account/registered`, params);
}

/**
 * captcha
 * @param {string} email
 * @returns
 */
export function captcha(email) {
    return request.get(`/auth/account/captcha`, { params: { email } });
}

/**
 * 查询自己
 * @returns
 */
export function getUserBySelf() {
    return request.get(`/user/bySelf`);
}

/**
 * 根据id查询用户
 * @param {string} id
 * @returns
 */
export function getUserById(id) {
    return request.get(`/user/byId`, { params: { id } });
}

/**
 * 更新用户信息
 * @param {object} data
 * @returns
 */
export function updateUser(data) {
    return request.post(`/user/update`, data);
}

/**
 * 上传头像
 * @param {File} file
 * @returns
 */
export function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    const config = {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    };
    return request.post(`/user/file/avatar/upload`, formData, config);
}
