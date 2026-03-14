import { ref, reactive } from 'vue';
import { ElMessage } from "element-plus";
import { captcha, login, signUp, loginByEmailCode } from "@/api/user.js";

export function useLoginLogic() {
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

  const countdown = ref(0);
  const isCounting = ref(false);
  const isEmailSignIn = ref(false);

  const startCountdown = (time) => {
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
  };

  const handleSignUp = async () => {
    if (!signupForm.captcha) {
      ElMessage.warning('请填写验证码');
      return;
    }
    try {
      const params = await signUp(signupForm);
      if (params) {
        ElMessage.success('注册成功');
        return true;
      }
    } catch (e) {
      console.error(e);
    }
    return false;
  };

  const handleSignIn = async () => {
    try {
      const data = await login(signInForm);
      if (data) {
        ElMessage.success('登录成功');
        localStorage.setItem("token", data);
        window.location.href = '/';
        return true;
      }
    } catch (e) {
      console.error(e);
    }
    return false;
  };

  const handleEmailSignIn = async () => {
    if (!signInForm.captcha) {
      ElMessage.warning('请填写验证码');
      return;
    }
    try {
      const data = await loginByEmailCode({
        email: signInForm.email,
        verificationCode: signInForm.captcha
      });
      if (data) {
        ElMessage.success('登录成功');
        localStorage.setItem("token", data);
        window.location.href = '/';
        return true;
      }
    } catch (e) {
      console.error(e);
    }
    return false;
  };

  const getCode = async () => {
    if (signupForm.email === '') {
      ElMessage.warning('请填写邮箱');
      return;
    }
    try {
      const params = await captcha(signupForm.email);
      if (params) {
        ElMessage.success('验证码发送成功');
      }
      startCountdown(120);
    } catch (e) {
      console.error(e);
    }
  };

  const getEmailCode = async () => {
    if (signInForm.email === '') {
      ElMessage.warning('请填写邮箱');
      return;
    }
    try {
      const params = await captcha(signInForm.email);
      if (params) {
        ElMessage.success('验证码发送成功');
      }
      startCountdown(120);
    } catch (e) {
      console.error(e);
    }
  };

  const toggleEmailSignIn = (isEmail) => {
    isEmailSignIn.value = isEmail;
  };

  return {
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
  };
}
