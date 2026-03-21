<template>
    <div class="body">
        <div class="background-shapes">
            <div class="shape shape-1"></div>
            <div class="shape shape-2"></div>
            <div class="shape shape-3"></div>
        </div>
        <div class="container glass-effect"
            :class="{ 'right-panel-active': isSignUpPanelActive, 'email-sign-in-active': isEmailSignIn }"
            id="login-box">
            <div class="form-container sign-up-container">
                <form @submit.prevent="handleSignUp().then(success => success && togglePanel(false))">
                    <h1>创建账户</h1>
                    <div class="social-container">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span>或使用邮箱注册</span>
                    <div class="input-group">
                        <input v-model="signupForm.username" type="text" placeholder="昵称" required />
                        <el-icon class="input-icon">
                            <User />
                        </el-icon>
                    </div>
                    <div class="input-group">
                        <input v-model="signupForm.email" type="email" placeholder="邮箱" required />
                        <el-icon class="input-icon">
                            <Message />
                        </el-icon>
                    </div>
                    <div class="input-group">
                        <input v-model="signupForm.password" type="password" placeholder="密码" required />
                        <el-icon class="input-icon">
                            <Lock />
                        </el-icon>
                    </div>
                    <div class="input-group captcha-group">
                        <input v-model="signupForm.captcha" type="text" placeholder="验证码" required />
                        <el-button class="code-btn" type="primary" link @click="getCode" :disabled="isCounting">
                            {{ isCounting ? `${countdown}s` : '获取验证码' }}
                        </el-button>
                    </div>
                    <button class="submit-btn">立即注册</button>
                </form>
            </div>

            <div class="form-container sign-in-container" v-if="!isEmailSignIn">
                <form @submit.prevent="handleSignIn">
                    <h1>欢迎回来</h1>
                    <div class="social-container">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span>使用您的账户登录</span>
                    <div class="input-group">
                        <input v-model="signInForm.email" type="email" placeholder="邮箱" required />
                        <el-icon class="input-icon">
                            <Message />
                        </el-icon>
                    </div>
                    <div class="input-group">
                        <input v-model="signInForm.password" type="password" placeholder="密码" required />
                        <el-icon class="input-icon">
                            <Lock />
                        </el-icon>
                    </div>
                    <div class="actions">
                        <span class="action-link" @click.prevent="toggleEmailSignIn(true)">验证码登录</span>
                        <span class="action-link">忘记密码？</span>
                    </div>
                    <button class="submit-btn">登录</button>
                </form>
            </div>

            <div class="form-container sign-in-container" v-if="isEmailSignIn">
                <form @submit.prevent="handleEmailSignIn">
                    <h1>验证码登录</h1>
                    <div class="social-container">
                        <el-tooltip content="GitHub 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGithubLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="Google 登录" placement="top">
                            <a href="#" class="social" @click.prevent="handleGoogleLogin">
                                <svg width="20" height="20" viewBox="0 0 24 24">
                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                        <el-tooltip content="微信登录" placement="top">
                            <a href="#" class="social">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 01-.023-.156.49.49 0 01.201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.406-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.542.434-.982.969-.982z"/>
                                </svg>
                            </a>
                        </el-tooltip>
                    </div>
                    <span>使用邮箱验证码登录</span>
                    <div class="input-group">
                        <input v-model="signInForm.email" type="email" placeholder="邮箱" required />
                        <el-icon class="input-icon">
                            <Message />
                        </el-icon>
                    </div>
                    <div class="input-group captcha-group">
                        <input v-model="signInForm.captcha" type="text" placeholder="验证码" required />
                        <el-button class="code-btn" type="primary" link @click="getEmailCode" :disabled="isCounting">
                            {{ isCounting ? `${countdown}s` : '获取验证码' }}
                        </el-button>
                    </div>
                    <div class="actions">
                        <span class="action-link" @click.prevent="toggleEmailSignIn(false)">密码登录</span>
                        <span class="action-link">忘记密码？</span>
                    </div>
                    <button class="submit-btn">登录</button>
                </form>
            </div>

            <div class="overlay-container">
                <div class="overlay">
                    <div class="overlay-panel overlay-left">
                        <h1>已有账号？</h1>
                        <p>请使用您的账号进行登录，畅享更多功能</p>
                        <button class="ghost" @click="togglePanel(false)">去登录</button>
                    </div>
                    <div class="overlay-panel overlay-right">
                        <h1>没有账号?</h1>
                        <p>立即注册加入我们，开启您的创作之旅</p>
                        <button class="ghost" @click="togglePanel(true)">去注册</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
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
onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    const error = urlParams.get('error');

    if (token) {
        // Store token and redirect to home
        localStorage.setItem('token', token);
        window.location.href = '/';
    } else if (error) {
        // Handle OAuth error
        console.error('OAuth login failed:', error);
        ElMessage.error('第三方登录失�? ' + error);
    }
});
</script>

<style scoped>
.body {
    font-family: 'Inter', system-ui, -apple-system, sans-serif;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f4f8;
    position: relative;
    overflow: hidden;
}

.background-shapes .shape {
    position: absolute;
    filter: blur(80px);
    z-index: 0;
    opacity: 0.6;
    animation: float 20s infinite;
}

.shape-1 {
    top: -10%;
    left: -10%;
    width: 500px;
    height: 500px;
    background: #c4b5fd;
    animation-delay: 0s;
}

.shape-2 {
    bottom: -10%;
    right: -10%;
    width: 600px;
    height: 600px;
    background: #a5f3fc;
    animation-delay: -5s;
}

.shape-3 {
    top: 40%;
    left: 40%;
    width: 300px;
    height: 300px;
    background: #fbcfe8;
    animation-delay: -10s;
}

@keyframes float {

    0%,
    100% {
        transform: translate(0, 0) rotate(0deg);
    }

    33% {
        transform: translate(30px, -50px) rotate(10deg);
    }

    66% {
        transform: translate(-20px, 20px) rotate(-5deg);
    }
}

.container {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    border-radius: 24px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.05);
    position: relative;
    overflow: hidden;
    width: 900px;
    max-width: 95%;
    min-height: 600px;
    display: flex;
    z-index: 1;
}

.form-container {
    position: absolute;
    top: 0;
    height: 100%;
    transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    width: 50%;
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(10px);
}

.form-container form {
    display: flex;
    flex-direction: column;
    padding: 0 50px;
    height: 100%;
    justify-content: center;
    align-items: center;
    text-align: center;
}

h1 {
    font-weight: 800;
    margin: 0 0 20px;
    color: var(--text-color-primary);
    font-size: 28px;
}

.social-container {
    margin: 10px 0 20px;
    display: flex;
    gap: 16px;
}

.social {
    border: 1px solid #ddd;
    border-radius: 50%;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 40px;
    height: 40px;
    color: var(--text-color-secondary);
    transition: all 0.3s;
    background: white;
}

.social:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    transform: translateY(-2px);
}

span {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin-bottom: 20px;
}

.input-group {
    position: relative;
    width: 100%;
    margin: 8px 0;
}

.input-group input {
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid transparent;
    padding: 12px 15px 12px 45px;
    width: 100%;
    border-radius: 12px;
    font-size: 14px;
    outline: none;
    transition: all 0.3s;
    color: var(--text-color-primary);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.02);
}

.input-group input:focus {
    background: #fff;
    border-color: var(--color-primary);
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.1);
}

.input-icon {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: var(--text-color-placeholder);
    transition: color 0.3s;
}

.input-group input:focus+.input-icon {
    color: var(--color-primary);
}

.captcha-group {
    display: flex;
    gap: 10px;
}

.captcha-group input {
    padding-left: 15px;
    /* Adjust padding if no icon */
    flex: 1;
}

.code-btn {
    white-space: nowrap;
    padding: 0 15px;
}

.actions {
    width: 100%;
    display: flex;
    justify-content: space-between;
    margin: 15px 0 25px;
    font-size: 13px;
}

.action-link {
    color: var(--text-color-secondary);
    cursor: pointer;
    transition: color 0.3s;
}

.action-link:hover {
    color: var(--color-primary);
    text-decoration: underline;
}

.submit-btn {
    border-radius: 30px;
    border: none;
    background: linear-gradient(135deg, var(--color-primary), #6366f1);
    color: #fff;
    font-size: 14px;
    font-weight: 700;
    padding: 12px 45px;
    letter-spacing: 1px;
    text-transform: uppercase;
    transition: transform 80ms ease-in, box-shadow 0.3s;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(var(--color-primary-rgb), 0.3);
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
}

.submit-btn:active {
    transform: scale(0.95);
}

.submit-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.overlay-container {
    position: absolute;
    top: 0;
    left: 50%;
    width: 50%;
    height: 100%;
    overflow: hidden;
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 100;
}

.overlay {
    background: linear-gradient(135deg, var(--color-primary), #8b5cf6);
    background-repeat: no-repeat;
    background-size: cover;
    background-position: 0 0;
    color: #fff;
    position: relative;
    left: -100%;
    height: 100%;
    width: 200%;
    transform: translateX(0);
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.overlay-panel {
    position: absolute;
    top: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 0 40px;
    height: 100%;
    width: 50%;
    text-align: center;
    transform: translateX(0);
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.overlay-panel h1 {
    color: white;
    margin-bottom: 10px;
}

.overlay-panel p {
    font-size: 14px;
    font-weight: 300;
    line-height: 1.6;
    margin: 10px 0 30px;
    color: rgba(255, 255, 255, 0.9);
}

.ghost {
    background: transparent;
    border: 1px solid #fff;
    border-radius: 30px;
    color: #fff;
    font-size: 13px;
    font-weight: 700;
    padding: 10px 40px;
    letter-spacing: 1px;
    text-transform: uppercase;
    transition: all 0.3s;
    cursor: pointer;
}

.ghost:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.05);
}

.overlay-right {
    right: 0;
    transform: translateX(0);
}

.overlay-left {
    transform: translateX(-20%);
}

/* Animation States */
.container.right-panel-active .sign-in-container {
    transform: translateX(100%);
    opacity: 0;
}

.container.right-panel-active .overlay-container {
    transform: translateX(-100%);
}

.container.right-panel-active .sign-up-container {
    transform: translateX(100%);
    opacity: 1;
    z-index: 5;
}

.container.right-panel-active .overlay {
    transform: translateX(50%);
}

.container.right-panel-active .overlay-left {
    transform: translateX(0);
}

.container.right-panel-active .overlay-right {
    transform: translateX(20%);
}
</style>
