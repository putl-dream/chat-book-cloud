<template>
  <div class="login-shell">
    <section class="login-showcase">
      <div class="login-showcase-badge">
        <span>后台访问控制</span>
      </div>
      <h1>Chat Book 后台管理台</h1>
      <p>
        已将原来的 Next.js 管理端重构为 Vite + Vue 3，保留原有主题变量、接口契约和后台信息架构，
        以获得更快的本地热更新和更轻的前端构建链。
      </p>

      <div class="login-showcase-grid">
        <article>
          <strong>迁移重点</strong>
          <span>移除 Next 服务端依赖，改为浏览器端 API 层、Vue Router 守卫和 Pinia 会话状态。</span>
        </article>
        <article>
          <strong>已保留</strong>
          <span>后台导航、主题系统、标签 CRUD、文章审核和页面布局风格。</span>
        </article>
        <article>
          <strong>性能收益</strong>
          <span>本地开发只需 Vite dev server，无需等待 Next App Router 的服务端重编译。</span>
        </article>
      </div>
    </section>

    <section class="login-panel">
      <div class="login-panel-head">
        <div class="login-panel-icon">
          <span class="text-base font-semibold">A</span>
        </div>
        <div>
          <p class="eyebrow">Admin Login</p>
          <h2>使用管理员账号进入后台</h2>
        </div>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <label class="field">
          <span>用户名</span>
          <input
            v-model="username"
            autocomplete="username"
            :disabled="submitting || checking"
            name="username"
            placeholder="请输入管理员用户名"
          />
        </label>

        <label class="field">
          <span>密码</span>
          <input
            v-model="password"
            autocomplete="current-password"
            :disabled="submitting || checking"
            name="password"
            placeholder="请输入密码"
            type="password"
          />
        </label>

        <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>
        <p v-else-if="checking" class="form-message">正在校验现有登录态...</p>

        <button
          class="login-submit"
          :disabled="submitting || checking || !username.trim() || !password"
          type="submit"
        >
          {{ submitting ? "登录中..." : "进入后台" }}
        </button>
      </form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { BrowserApiError } from "@/services/admin-api";
import { normalizeNextPath } from "@/services/auth";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const username = ref("");
const password = ref("");
const checking = ref(true);
const submitting = ref(false);
const errorMessage = ref("");

function getReasonMessage(reason: string | null) {
  if (reason === "session-expired") {
    return "登录态已失效，请重新登录。";
  }

  if (reason === "forbidden") {
    return "当前账号没有后台访问权限。";
  }

  return "";
}

const safeNextPath = normalizeNextPath(typeof route.query.next === "string" ? route.query.next : null);
errorMessage.value = getReasonMessage(typeof route.query.reason === "string" ? route.query.reason : null);

onMounted(async () => {
  if (!authStore.token) {
    checking.value = false;
    return;
  }

  try {
    await authStore.ensureSession();
    router.replace(safeNextPath);
  } catch {
    // ensureSession 已在失败分支中清理会话，这里避免重复触发 logout。
  } finally {
    checking.value = false;
  }
});

async function handleSubmit() {
  submitting.value = true;
  errorMessage.value = "";

  try {
    await authStore.login(username.value.trim(), password.value);
    router.replace(safeNextPath);
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "登录失败，请检查网关地址、账号或密码。";
  } finally {
    submitting.value = false;
  }
}
</script>
