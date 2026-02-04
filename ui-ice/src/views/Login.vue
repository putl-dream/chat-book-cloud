<template>
    <div class="body">
        <div class="container" :class="{ 'right-panel-active': isSignUpPanelActive, 'email-sign-in-active': isEmailSignIn }" id="login-box">
            <div class="form-container sign-up-container">
                <form @submit.prevent="handleSignUp">
                    <h1>注册</h1>
                    <div class="txtb">
                        <input v-model="signupForm.username" placeholder="昵称/Username" type="text" required>
                    </div>
                    <div class="txtb">
                        <input v-model="signupForm.email" placeholder="邮箱/Email" type="email" required>
                    </div>
                    <div class="txtb">
                        <input v-model="signupForm.password" placeholder="密码/Password" type="password" required>
                    </div>
                    <div class="txtb" style="display:flex;">
                        <input v-model="signupForm.captcha" placeholder="输入验证码" type="text" required>
                        <el-button color="#626aef" style="padding: 0;margin: 0;width: 150px;" @click="getCode"
                                   :disabled="isCounting">
                            {{ isCounting ? `${countdown}秒后重新获取` : '获取验证码' }}
                        </el-button>
                    </div>
                    <button>注册</button>
                </form>
            </div>
            <div class="form-container sign-in-container" v-if="!isEmailSignIn">
                <form @submit.prevent="handleSignIn">
                    <h1>登录</h1>
                    <div class="txtb">
                        <input v-model="signInForm.email" placeholder="邮箱/Email" type="email" required>
                    </div>
                    <div class="txtb">
                        <input v-model="signInForm.password" placeholder="密码/Password" type="password" required>
                    </div>
<!--                    <span href="#" @click.prevent="toggleEmailSignIn(true)">验证码登录</span>-->
                    <button style="margin: 10px 0">登录</button>
                    <el-text href="#">忘记密码？</el-text>
                </form>
            </div>
            <div class="form-container sign-in-container" v-if="isEmailSignIn">
                <form @submit.prevent="handleEmailSignIn">
                    <h1>登录</h1>
                    <div class="txtb">
                        <input v-model="signInForm.email" placeholder="邮箱/Email" type="email" required>
                    </div>
                    <div class="txtb" style="display:flex;">
                        <input v-model="signInForm.captcha" placeholder="输入验证码" type="text" required>
                        <el-button color="#626aef" style="padding: 0;margin: 0;width: 150px;" @click="getEmailCode"
                                   :disabled="isCounting">
                            {{ isCounting ? `${countdown}秒后重新获取` : '获取验证码' }}
                        </el-button>
                    </div>
                    <span href="#" @click.prevent="toggleEmailSignIn(false)">邮箱密码登录</span>
                    <button style="margin: 10px 0">登录</button>
                    <el-text href="#">忘记密码？</el-text>
                </form>
            </div>
            <div class="overlay-container">
                <div class="overlay">
                    <div class="overlay-panel overlay-left">
                        <h1>已有账号？</h1>
                        <p>请使用您的账号进行登录</p>
                        <button class="ghost" @click="togglePanel(false)">登录</button>
                    </div>
                    <div class="overlay-panel overlay-right">
                        <h1>没有账号?</h1>
                        <p>立即注册加入我们，和我们一起开始旅程吧</p>
                        <button class="ghost" @click="togglePanel(true)">注册</button>
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

const isSignUpPanelActive = ref(false);
const signupForm = reactive({
    username: '',
    email: '',
    password: '',
    captcha: ''
});
const signInForm = reactive({
    email: '',
    password: ''
});

const countdown = ref(0); // 倒计时秒数
const isCounting = ref(false); // 是否正在倒计时
const emailCountdown = ref(0); // 邮箱验证码倒计时秒数
const isEmailCounting = ref(false); // 是否正在邮箱验证码倒计时
const isEmailSignIn = ref(false); // 是否显示邮箱验证码登录

function togglePanel(isSignUp) {
    isSignUpPanelActive.value = isSignUp;
}

// 注册逻辑
async function handleSignUp() {
    if (!signupForm.captcha) {
        ElMessage.error('请填写验证码');
        return;
    }
    let params = await signUp(signupForm);
    if (params.code === 200) {
        ElMessage.success('注册成功');
        togglePanel(false);
    }
}

// 登录逻辑
async function handleSignIn() {
    let signIn = await login(signInForm);
    if (signIn.code === 200) {
        ElMessage.success('登录成功');
        localStorage.setItem("token", signIn.data.token);
        window.location.href = '/';
    }
}

// 邮箱验证码登录逻辑
async function handleEmailSignIn() {
    if (!signInForm.captcha) {
        ElMessage.error('请填写验证码');
        return;
    }
    // TODO: 实现邮箱验证码登录逻辑
    ElMessage.info('邮箱验证码登录功能待实现');
}

// 获取验证码逻辑
async function getCode() {
    if (signupForm.email === '') {
        ElMessage.error('请填写邮箱');
        return;
    }
    let params = await captcha(signupForm.email);
    if (params.code === 200) {
        ElMessage.success('验证码发送成功');
    }
    // 启动倒计时
    startCountdown(120);
}

function startCountdown(time) {
    countdown.value = time; // 设置倒计时为120秒
    isCounting.value = true; // 启动倒计时，禁用按钮
    const interval = setInterval(() => {
        if (countdown.value > 0) {
            countdown.value--;
        } else {
            clearInterval(interval);
            isCounting.value = false; // 倒计时结束，启用按钮
        }
    }, 1000);
}

// 获取邮箱验证码逻辑
async function getEmailCode() {
    if (signInForm.email === '') {
        ElMessage.error('请填写邮箱');
        return;
    }
    let params = await captcha(signInForm.email);
    if (params.code === 200) {
        ElMessage.success('验证码发送成功');
    }
    // 启动邮箱验证码倒计时
    startCountdown(120)
}



// 切换邮箱验证码登录
function toggleEmailSignIn(isEmail) {
    isEmailSignIn.value = isEmail;
}
</script>


<style scoped>
.body {
    font-family: 'Inter', system-ui, -apple-system, sans-serif;
    background: radial-gradient(circle at top left, #f8fafc, #eff6ff, #dbeafe);
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
}

h1 {
    font-weight: 800;
    margin: 0 0 24px;
    color: var(--text-color-primary);
    letter-spacing: -0.025em;
}

p {
    font-size: 0.9375rem;
    line-height: 1.6;
    color: var(--text-color-regular);
    margin: 16px 0 32px;
}

.container {
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-large);
    -webkit-backdrop-filter: var(--blur-large);
    border: 1px solid rgba(255, 255, 255, 0.4);
    border-radius: var(--border-radius-xl);
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    width: 800px;
    max-width: 90%;
    min-height: 520px;
    display: flex;
}

.form-container {
    position: absolute;
    top: 0;
    height: 100%;
    transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
    width: 50%;
    background: white;
}

.form-container form {
    background: transparent;
    display: flex;
    flex-direction: column;
    padding: 0 48px;
    height: 100%;
    justify-content: center;
    text-align: center;
}

.txtb {
    position: relative;
    margin: 12px 0;
}

.txtb input {
    font-size: 0.875rem;
    color: var(--text-color-primary);
    border: 1px solid var(--border-color-base);
    border-radius: var(--border-radius-large);
    width: 100%;
    outline: none;
    background: var(--bg-color-base);
    padding: 12px 16px;
    transition: var(--transition-base);
}

.txtb input:focus {
    border-color: var(--color-primary);
    background: white;
    box-shadow: 0 0 0 4px var(--color-primary-light);
}

button {
    border-radius: var(--border-radius-large);
    border: none;
    background: var(--color-primary);
    color: #fff;
    font-size: 0.875rem;
    font-weight: 600;
    padding: 12px 32px;
    transition: var(--transition-base);
    cursor: pointer;
    margin-top: 16px;
}

button:hover {
    background: var(--color-primary-hover);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

button:active {
    transform: translateY(0);
}

button.ghost {
    background: transparent;
    border: 1.5px solid #fff;
    margin-top: 0;
}

button.ghost:hover {
    background: rgba(255, 255, 255, 0.1);
    box-shadow: none;
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
    background: linear-gradient(135deg, var(--color-primary), #4f46e5);
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
    padding: 0 54px;
    height: 100%;
    width: 50%;
    text-align: center;
    transform: translateX(0);
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.overlay-panel h1 {
    color: white;
}

.overlay-panel p {
    color: rgba(255, 255, 255, 0.9);
}

.overlay-right {
    right: 0;
}

.overlay-left {
    transform: translateX(-20%);
}

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
