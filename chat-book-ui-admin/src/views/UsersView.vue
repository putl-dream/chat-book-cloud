<template>
  <section class="page-shell">
    <template v-if="userPage">
      <div class="page-hero compact">
        <p class="eyebrow">User Management</p>
        <h1>用户与账号治理</h1>
        <p class="hero-copy">
          当前后端已提供管理员分页查询用户能力，因此用户模块可以优先进入真实接入阶段。
          后续可继续补充角色调整、禁用账号、批量导出和登录审计等后台动作。
        </p>
      </div>

      <div class="metric-grid compact-grid">
        <article class="metric-card">
          <p class="metric-label">用户总数</p>
          <h2>{{ userPage.total }}</h2>
          <p class="metric-detail">真实数据来自 /user/admin/user?page&size</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">当前页管理员</p>
          <h2>{{ adminCount }}</h2>
          <p class="metric-detail">角色映射来源于 user-service 的 role 字段</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">分页状态</p>
          <h2>{{ userPage.pageNo }}/{{ userPage.totalPages }}</h2>
          <p class="metric-detail">单页 {{ userPage.pageSize }} 条，支持后台分页切换</p>
        </article>
      </div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Filters</p>
            <h3>搜索与筛选结构预留</h3>
          </div>
          <span class="pill pill-neutral">接口暂未开放搜索参数</span>
        </div>
        <div class="toolbar-grid">
          <label class="field">
            <span>用户名 / 邮箱</span>
            <input disabled placeholder="后续对接用户名、邮箱关键词搜索" />
          </label>
          <label class="field">
            <span>角色筛选</span>
            <select disabled>
              <option>全部角色</option>
            </select>
          </label>
          <label class="field">
            <span>账号状态</span>
            <input disabled placeholder="状态字段当前接口尚未返回" />
          </label>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">User Table</p>
            <h3>当前用户数据视图</h3>
          </div>
        </div>

        <RequestStatePanel
          v-if="userPage.list.length === 0"
          title="当前没有用户数据"
          description="接口已经接通，但本页暂时没有返回记录。后续可以继续补充搜索、筛选和批量操作。"
        />

        <template v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>用户</th>
                  <th>邮箱</th>
                  <th>角色</th>
                  <th>状态</th>
                  <th>简介</th>
                  <th>用户标识</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in userPage.list" :key="user.userId">
                  <td>
                    <div class="user-cell">
                      <strong>{{ user.username }}</strong>
                      <span>{{ user.photo ? "已配置头像" : "未配置头像" }}</span>
                    </div>
                  </td>
                  <td>{{ user.email }}</td>
                  <td>
                    <span :class="`pill pill-${getRoleTone(user.role)}`">{{ getRoleLabel(user.role) }}</span>
                  </td>
                  <td>
                    <span class="pill pill-neutral">{{ user.status ?? "接口未返回" }}</span>
                  </td>
                  <td>{{ user.profile || "暂无简介" }}</td>
                  <td class="mono">UID {{ user.userId }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <PaginationControls
            :page="userPage.pageNo"
            :total="userPage.total"
            :total-pages="userPage.totalPages"
            @change="handlePageChange"
          />
        </template>
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage"
      title="用户管理页暂时不可用"
      :description="errorMessage"
      tone="warning"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaginationControls from "@/components/shared/PaginationControls.vue";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { getRoleLabel, getRoleTone } from "@/data/admin-config";
import { BrowserApiError, getUsersPage } from "@/services/admin-api";
import type { AdminUser, PaginatedResult } from "@/types/admin";

const route = useRoute();
const router = useRouter();
const userPage = ref<PaginatedResult<AdminUser> | null>(null);
const errorMessage = ref("");

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const size = computed(() => parsePositiveInt(route.query.size, 20));
const adminCount = computed(() => userPage.value?.list.filter((user) => user.role === "admin").length ?? 0);

function buildQuery(nextPage: number) {
  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(size.value !== 20 ? { size: String(size.value) } : {}),
  };
}

async function loadUsers() {
  try {
    errorMessage.value = "";
    userPage.value = await getUsersPage({ page: page.value, size: size.value });
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "用户列表读取失败，请确认网关地址和管理员接口是否可访问。";
  }
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/users", query: buildQuery(nextPage) });
}

watch([page, size], loadUsers, { immediate: true });
</script>
