<template>
  <section class="page-shell">
    <template v-if="userPage">
      <div class="page-hero compact">
        <p class="eyebrow">User Management</p>
        <h1>用户与账号治理</h1>
        <p class="hero-copy">
          后端已提供管理员分页查询、角色调整和账号禁用/恢复能力。
          在此基础上补齐筛选栏和操作列，实现完整的用户治理流程。
        </p>
      </div>

      <div class="metric-grid compact-grid">
        <article class="metric-card">
          <p class="metric-label">用户总数</p>
          <h2>{{ userPage.total }}</h2>
          <p class="metric-detail">来自 /user/admin/user 的真实用户统计</p>
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
            <h3>搜索与筛选</h3>
          </div>
          <span class="pill pill-warn">后端仅支持分页，筛选参数待补齐</span>
        </div>
        <div class="toolbar-grid">
          <label class="field">
            <span>关键词</span>
            <input v-model="_filter.keyword" disabled placeholder="后端 /user/admin/user 尚未支持关键词筛选" />
          </label>
          <label class="field">
            <span>角色</span>
            <select v-model="_filter.role" disabled>
              <option :value="null">全部角色</option>
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </label>
          <label class="field">
            <span>账号状态</span>
            <select v-model="_filter.status" disabled>
              <option :value="null">全部状态</option>
              <option value="active">正常</option>
              <option value="disabled">已禁用</option>
            </select>
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

        <p v-if="message" class="form-message success">{{ message }}</p>
        <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

        <RequestStatePanel
          v-if="userPage.list.length === 0"
          title="当前没有符合条件的用户"
          description="尝试调整筛选条件后重新搜索。"
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
                  <th>操作</th>
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
                    <span :class="`pill ${user.status === 'disabled' ? 'pill-danger' : 'pill-safe'}`">
                      {{ user.status === "disabled" ? "已禁用" : "正常" }}
                    </span>
                  </td>
                  <td>{{ user.profile || "暂无简介" }}</td>
                  <td class="mono">UID {{ user.userId }}</td>
                  <td>
                    <div class="inline-actions">
                      <button
                        v-if="user.status !== 'disabled'"
                        class="table-action-button danger"
                        :disabled="submitting"
                        type="button"
                        @click="handleDisable(user)"
                      >禁用</button>
                      <button
                        v-if="user.status === 'disabled'"
                        class="table-action-button primary"
                        :disabled="submitting"
                        type="button"
                        @click="handleEnable(user)"
                      >恢复</button>
                      <button
                        class="table-action-button"
                        :disabled="submitting"
                        type="button"
                        @click="openRoleDialog(user)"
                      >{{ user.role === "admin" ? "降为用户" : "升为管理员" }}</button>
                    </div>
                  </td>
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

    <!-- Role adjustment dialog -->
    <div v-if="roleDialog" class="dialog-backdrop" role="presentation">
      <section class="dialog-panel" role="dialog" aria-modal="true">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Adjust Role</p>
            <h3>调整用户角色</h3>
          </div>
          <span class="pill pill-neutral">UID {{ roleDialog.user.userId }}</span>
        </div>
        <div class="dialog-form">
          <p style="margin-bottom: 1rem;">
            将用户 <strong>{{ roleDialog.user.username }}</strong> 的角色从
            <strong>{{ roleDialog.user.role === "admin" ? "管理员" : "普通用户" }}</strong>
            变更为：
          </p>
          <label class="field">
            <span>目标角色</span>
            <select v-model="roleDialog.targetRole">
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </label>
          <div class="dialog-actions">
            <button class="panel-action-button" :disabled="submitting" type="button" @click="closeRoleDialog">取消</button>
            <button class="panel-action-button primary" :disabled="submitting || roleDialog.targetRole === (roleDialog.user.role === 'admin' ? 'ADMIN' : 'USER')" type="button" @click="handleRoleSubmit">
              {{ submitting ? "提交中..." : "确认调整" }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaginationControls from "@/components/shared/PaginationControls.vue";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { getRoleLabel, getRoleTone } from "@/data/admin-config";
import { BrowserApiError, disableUser, enableUser, getUsersPage, updateUserRole } from "@/services/admin-api";
import type { AdminUser, PaginatedResult } from "@/types/admin";

const route = useRoute();
const router = useRouter();

const userPage = ref<PaginatedResult<AdminUser> | null>(null);
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");

type RoleDialogState = {
  user: AdminUser;
  targetRole: "USER" | "ADMIN";
} | null;

const roleDialog = ref<RoleDialogState>(null);

// 筛选状态已预留，后端支持筛选参数后可启用
const _filter = ref({
  keyword: "",
  role: null as string | null,
  status: null as string | null,
});

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const adminCount = computed(() => userPage.value?.list.filter((u) => u.role === "admin").length ?? 0);

function buildQuery(nextPage: number) {
  return nextPage > 1 ? { page: String(nextPage) } : {};
}

async function loadUsers() {
  try {
    errorMessage.value = "";
    userPage.value = await getUsersPage({ page: page.value, size: 20 });
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

// 后端支持筛选后启用以下函数
function applyFilters() {
  // router.push({ path: "/users", query: { ...buildQuery(1), ..._filter.value } });
}
function resetFilters() {
  // _filter.value = { keyword: "", role: null, status: null };
  // router.push({ path: "/users", query: buildQuery(1) });
}

async function runUserAction(fn: (userId: number) => Promise<void>, user: AdminUser, label: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";
    await fn(user.userId);
    message.value = `用户 ${user.username}（UID ${user.userId}）${label}成功。`;
    await loadUsers();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : `操作失败，请稍后重试。`;
  } finally {
    submitting.value = false;
    setTimeout(() => { message.value = ""; }, 3000);
  }
}

async function handleDisable(user: AdminUser) {
  if (!window.confirm(`确认禁用用户 ${user.username}（UID ${user.userId}）吗？`)) return;
  await runUserAction(disableUser, user, "禁用");
}

async function handleEnable(user: AdminUser) {
  if (!window.confirm(`确认恢复用户 ${user.username}（UID ${user.userId}）吗？`)) return;
  await runUserAction(enableUser, user, "恢复");
}

function openRoleDialog(user: AdminUser) {
  errorMessage.value = "";
  roleDialog.value = {
    user,
    targetRole: user.role === "admin" ? "USER" : "ADMIN",
  };
}

function closeRoleDialog() {
  roleDialog.value = null;
}

async function handleRoleSubmit() {
  if (!roleDialog.value) return;
  const { user, targetRole } = roleDialog.value;
  if (!window.confirm(`确认将用户 ${user.username} 的角色变更为 ${targetRole === "ADMIN" ? "管理员" : "普通用户"} 吗？`)) return;
  try {
    submitting.value = true;
    errorMessage.value = "";
    await updateUserRole(user.userId, targetRole);
    message.value = `用户 ${user.username} 的角色已变更为 ${targetRole === "ADMIN" ? "管理员" : "普通用户"}。`;
    roleDialog.value = null;
    await loadUsers();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : `角色调整失败，请稍后重试。`;
  } finally {
    submitting.value = false;
    setTimeout(() => { message.value = ""; }, 3000);
  }
}

watch([page], loadUsers, { immediate: true });
</script>
