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
                <form @submit.prevent="handleSignUp">
                    <h1>创建账户</h1>
                    <div class="social-container">
                        <a href="#" class="social"><el-icon><i-ep-platform /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-google /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-chat-dot-round /></el-icon></a>
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
                        <a href="#" class="social"><el-icon><i-ep-platform /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-google /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-chat-dot-round /></el-icon></a>
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
                        <a href="#" class="social"><el-icon><i-ep-platform /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-google /></el-icon></a>
                        <a href="#" class="social"><el-icon><i-ep-chat-dot-round /></el-icon></a>
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
import { ref, reactive } from 'vue';
import { ElMessage } from "element-plus";
import { captcha, login, signUp } from "@/api/user.js";
import { User, Lock, Message } from '@element-plus/icons-vue';

const isSignUpPanelActive = ref(false);
const signupForm = reactive({
    username: '',
    email: '',
    password: '',
    captcha: ''
});
const signInForm = reactive({
    email: '',
    password: '',
    captcha: ''
});

const countdown = ref(0); // 倒计时秒数
const isCounting = ref(false); // 是否正在倒计时
const isEmailSignIn = ref(false); // 是否显示邮箱验证码登录

function togglePanel(isSignUp) {
    isSignUpPanelActive.value = isSignUp;
}

// 注册逻辑
async function handleSignUp() {
    if (!signupForm.captcha) {
        ElMessage.warning('请填写验证码');
        return;
    }
    try {
        let params = await signUp(signupForm);
        if (params) {
            ElMessage.success('注册成功');
            togglePanel(false);
        }
    } catch (e) {
        console.error(e);
    }
}

// 登录逻辑
async function handleSignIn() {
    try {
        let data = await login(signInForm);
        if (data) {
            ElMessage.success('登录成功');
            localStorage.setItem("token", data);
            window.location.href = '/';
        }
    } catch (e) {
        console.error(e);
    }
}

// 邮箱验证码登录逻辑
async function handleEmailSignIn() {
    if (!signInForm.captcha) {
        ElMessage.warning('请填写验证码');
        return;
    }
    ElMessage.info('邮箱验证码登录功能开发中');
}

// 获取验证码逻辑
async function getCode() {
    if (signupForm.email === '') {
        ElMessage.warning('请填写邮箱');
        return;
    }
    try {
        let params = await captcha(signupForm.email);
        if (params) {
            ElMessage.success('验证码发送成功');
        }
        startCountdown(120);
    } catch (e) {
        console.error(e);
    }
}

function startCountdown(time) {
    countdown.value = time;
    isCounting.value = true;
    const interval = setInterval(() => {
        if (countdown.value > 0) {
            countdown.value--;
        } else {
            clearInterval(interval);
            isCounting.value = false;
        }
    }, 1000);
}

// 获取邮箱验证码逻辑
async function getEmailCode() {
    if (signInForm.email === '') {
        ElMessage.warning('请填写邮箱');
        return;
    }
    try {
        let params = await captcha(signInForm.email);
        if (params) {
            ElMessage.success('验证码发送成功');
        }
        startCountdown(120);
    } catch (e) {
        console.error(e);
    }
}

// 切换邮箱验证码登录
function toggleEmailSignIn(isEmail) {
    isEmailSignIn.value = isEmail;
}
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