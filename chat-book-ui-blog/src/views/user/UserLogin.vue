<template>
    <div class="c-auth-page">
        <div class="c-auth-page__background">
            <div class="c-auth-page__shape c-auth-page__shape--violet"></div>
            <div class="c-auth-page__shape c-auth-page__shape--cyan"></div>
            <div class="c-auth-page__shape c-auth-page__shape--pink"></div>
        </div>
        <div class="c-auth-layout" :class="{ 'right-panel-active': isSignUpPanelActive }">
            <div class="c-auth-layout__form c-auth-layout__form--sign-up">
                <form class="c-auth-layout__form-inner" @submit.prevent="handleSignUp().then(success => success && togglePanel(false))">
                    <h1 class="c-auth-layout__title">创建账户</h1>
                    <div class="c-auth-socials">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="c-auth-social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span class="c-auth-caption">或使用邮箱注册</span>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <User />
                        </el-icon>
                        <input v-model="signupForm.username" class="c-auth-field__input" type="text" placeholder="昵称" required />
                    </div>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <Message />
                        </el-icon>
                        <input v-model="signupForm.email" class="c-auth-field__input" type="email" placeholder="邮箱" required />
                    </div>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <Lock />
                        </el-icon>
                        <input v-model="signupForm.password" class="c-auth-field__input" type="password" placeholder="密码" required />
                    </div>
                    <div class="c-auth-field c-auth-field--inline">
                        <input v-model="signupForm.captcha" class="c-auth-field__input" type="text" placeholder="验证码" required />
                        <el-button class="c-auth-code-button" type="primary" link @click="getCode" :disabled="isCounting">
                            {{ isCounting ? `${countdown}s` : '获取验证码' }}
                        </el-button>
                    </div>
                    <button class="c-auth-submit c-auth-submit--spaced">立即注册</button>
                </form>
            </div>

            <div class="c-auth-layout__form c-auth-layout__form--sign-in" v-if="!isEmailSignIn">
                <form class="c-auth-layout__form-inner" @submit.prevent="handleSignIn">
                    <h1 class="c-auth-layout__title">欢迎回来</h1>
                    <div class="c-auth-socials">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="c-auth-social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span class="c-auth-caption">使用您的账户登录</span>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <Message />
                        </el-icon>
                        <input v-model="signInForm.email" class="c-auth-field__input" type="email" placeholder="邮箱" required />
                    </div>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <Lock />
                        </el-icon>
                        <input v-model="signInForm.password" class="c-auth-field__input" type="password" placeholder="密码" required />
                    </div>
                    <div class="c-auth-actions">
                        <span class="c-auth-action-link" @click.prevent="toggleEmailSignIn(true)">验证码登录</span>
                        <span class="c-auth-action-link">忘记密码？</span>
                    </div>
                    <button class="c-auth-submit c-auth-submit--spaced">登录</button>
                </form>
            </div>

            <div class="c-auth-layout__form c-auth-layout__form--sign-in" v-if="isEmailSignIn">
                <form class="c-auth-layout__form-inner" @submit.prevent="handleEmailSignIn">
                    <h1 class="c-auth-layout__title">验证码登录</h1>
                    <div class="c-auth-socials">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="c-auth-social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="c-auth-social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span class="c-auth-caption">使用邮箱验证码登录</span>
                    <div class="c-auth-field">
                        <el-icon class="c-auth-field__icon">
                            <Message />
                        </el-icon>
                        <input v-model="signInForm.email" class="c-auth-field__input" type="email" placeholder="邮箱" required />
                    </div>
                    <div class="c-auth-field c-auth-field--inline">
                        <input v-model="signInForm.captcha" class="c-auth-field__input" type="text" placeholder="验证码" required />
                        <el-button class="c-auth-code-button" type="primary" link @click="getEmailCode" :disabled="isCounting">
                            {{ isCounting ? `${countdown}s` : '获取验证码' }}
                        </el-button>
                    </div>
                    <div class="c-auth-actions">
                        <span class="c-auth-action-link" @click.prevent="toggleEmailSignIn(false)">密码登录</span>
                        <span class="c-auth-action-link">忘记密码？</span>
                    </div>
                    <button class="c-auth-submit c-auth-submit--spaced">登录</button>
                </form>
            </div>

            <div class="c-auth-layout__overlay-shell">
                <div class="c-auth-layout__overlay">
                    <div class="c-auth-layout__overlay-panel c-auth-layout__overlay-panel--left">
                        <h1 class="c-auth-layout__overlay-title">已有账号？</h1>
                        <p class="c-auth-layout__overlay-copy">请使用您的账号进行登录，畅享更多功能</p>
                        <button class="c-auth-ghost" @click="togglePanel(false)">去登录</button>
                    </div>
                    <div class="c-auth-layout__overlay-panel c-auth-layout__overlay-panel--right">
                        <h1 class="c-auth-layout__overlay-title">没有账号?</h1>
                        <p class="c-auth-layout__overlay-copy">立即注册加入我们，开启您的创作之旅</p>
                        <button class="c-auth-ghost" @click="togglePanel(true)">去注册</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { User, Lock, Message } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { useLoginLogic } from "./Login/_hooks/useLoginLogic.js";
import { oauthLogin } from "@/views/user/_domain/user.js";

const router = useRouter();
const isSignUpPanelActive = ref(false);

const {
  signupForm,
  signInForm,
  countdown,
  isCounting,
  isEmailSignIn,
  handleSignUp,
  handleSignIn,
  handleEmailSignIn,
  getCode,
  getEmailCode,
  toggleEmailSignIn
} = useLoginLogic();

function togglePanel(isSignUp) {
    isSignUpPanelActive.value = isSignUp;
}

const handleGoogleLogin = () => oauthLogin('google');
const handleGithubLogin = () => oauthLogin('github');

// Handle OAuth callback - check for token in URL
import { setTokens } from '@/utils/token.js';
import {useRouter} from "vue-router";
onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('accessToken');
    const refreshToken = urlParams.get('refreshToken');
    const error = urlParams.get('error');

    if (accessToken && refreshToken) {
        setTokens({ accessToken, refreshToken });
        window.location.href = '/';
    } else if (error) {
        console.error('OAuth login failed:', error);
        ElMessage.error('第三方登录失败: ' + error);
    }
});
</script>
