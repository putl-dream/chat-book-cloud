<template>
  <div class="centered-shell">
    <div class="panel max-w-2xl">
      <p class="eyebrow">403 Forbidden</p>
      <h1 class="text-[2rem] leading-tight">当前账号没有后台访问权限</h1>
      <p class="hero-copy">
        你已经成功登录，但当前账号角色不满足后台管理要求。请切换管理员账号后重新进入。
      </p>
      <div class="inline-actions">
        <RouterLink class="panel-action-button primary" to="/login">返回登录页</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from "vue-router";
</script>
