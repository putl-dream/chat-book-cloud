<template>
  <section class="page-shell">
    <template v-if="userPage">
      <div class="page-hero compact">
        <p class="eyebrow">User Management</p>
        <h1>用户与账号治理</h1>
        <p class="hero-copy">
          管理员可以按关键词、角色和账号状态筛选用户，并直接执行角色调整、禁用、恢复和审计追踪。
        </p>
      </div>

      <div class="metric-grid compact-grid">
        <article class="metric-card">
          <p class="metric-label">筛选结果</p>
          <h2>{{ userPage.total }}</h2>
          <p class="metric-detail">来自 /user/admin/user 的真实分页结果</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">当前页管理员</p>
          <h2>{{ adminCount }}</h2>
          <p class="metric-detail">角色映射来源于 user-service 的 role 字段</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">当前页禁用账号</p>
          <h2>{{ disabledCount }}</h2>
          <p class="metric-detail">账号状态来自 user-service 的 status 字段</p>
        </article>
      </div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Filters</p>
            <h3>搜索与筛选</h3>
          </div>
          <div class="inline-actions">
            <button class="panel-action-button" type="button" @click="goToAuditView">查看操作审计</button>
          </div>
        </div>
        <div class="toolbar-grid">
          <label class="field">
            <span>关键词</span>
            <input v-model="filter.keyword" placeholder="搜索用户名或邮箱" @keydown.enter="applyFilters" />
          </label>
          <label class="field">
            <span>角色</span>
            <select v-model="filter.role">
              <option :value="null">全部角色</option>
              <option value="USER">普通用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </label>
          <label class="field">
            <span>账号状态</span>
            <select v-model="filter.status">
              <option :value="null">全部状态</option>
              <option :value="0">正常</option>
              <option :value="1">已禁用</option>
            </select>
          </label>
        </div>
        <div class="inline-actions" style="margin-top: 1rem;">
          <button class="panel-action-button primary" type="button" @click="applyFilters">搜索</button>
          <button class="panel-action-button" type="button" @click="resetFilters">重置</button>
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
                    <span :class="`pill ${user.status === 1 ? 'pill-danger' : 'pill-safe'}`">
                      {{ user.status === 1 ? "已禁用" : "正常" }}
                    </span>
                  </td>
                  <td>{{ user.profile || "暂无简介" }}</td>
                  <td class="mono">UID {{ user.userId }}</td>
                  <td>
                    <div class="inline-actions">
                      <button
                        v-if="user.status !== 1"
                        class="table-action-button danger"
                        :disabled="submitting"
                        type="button"
                        @click="handleDisable(user)"
                      >禁用</button>
                      <button
                        v-if="user.status === 1"
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
            <button
              class="panel-action-button primary"
              :disabled="submitting || roleDialog.targetRole === (roleDialog.user.role === 'admin' ? 'ADMIN' : 'USER')"
              type="button"
              @click="handleRoleSubmit"
            >
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
import { confirmAction } from "@/composables/useConfirmDialog";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { getRoleLabel, getRoleTone } from "@/data/admin-config";
import { BrowserApiError, disableUser, enableUser, getUsersPage, updateUserRole } from "@/services/admin-api";
import type { AdminUser, PaginatedResult } from "@/types/admin";

type UserRoleFilter = "USER" | "ADMIN" | null;
type UserStatusFilter = 0 | 1 | null;

type RoleDialogState = {
  user: AdminUser;
  targetRole: "USER" | "ADMIN";
} | null;

const route = useRoute();
const router = useRouter();

const userPage = ref<PaginatedResult<AdminUser> | null>(null);
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");
const roleDialog = ref<RoleDialogState>(null);
const filter = ref({
  keyword: "",
  role: null as UserRoleFilter,
  status: null as UserStatusFilter,
});

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function parseRole(value: unknown): UserRoleFilter {
  return value === "USER" || value === "ADMIN" ? value : null;
}

function parseStatus(value: unknown): UserStatusFilter {
  const parsed = Number(value);
  return parsed === 0 || parsed === 1 ? parsed : null;
}

function syncFilterFromRoute() {
  filter.value = {
    keyword: typeof route.query.keyword === "string" ? route.query.keyword : "",
    role: parseRole(route.query.role),
    status: parseStatus(route.query.status),
  };
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const adminCount = computed(() => userPage.value?.list.filter((user) => user.role === "admin").length ?? 0);
const disabledCount = computed(() => userPage.value?.list.filter((user) => user.status === 1).length ?? 0);

function buildQuery(nextPage = page.value) {
  const keyword = filter.value.keyword.trim();

  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(keyword ? { keyword } : {}),
    ...(filter.value.role ? { role: filter.value.role } : {}),
    ...(filter.value.status != null ? { status: String(filter.value.status) } : {}),
  };
}

async function loadUsers() {
  try {
    errorMessage.value = "";
    userPage.value = await getUsersPage({
      page: page.value,
      size: 20,
      keyword: filter.value.keyword.trim() || undefined,
      role: filter.value.role,
      status: filter.value.status,
    });

    if (userPage.value.list.length === 0 && userPage.value.total > 0 && page.value > 1) {
      router.replace({ path: "/users", query: buildQuery(page.value - 1) });
    }
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "用户列表读取失败，请确认网关地址和管理员接口是否可访问。";
  }
}

function applyFilters() {
  router.push({ path: "/users", query: buildQuery(1) });
}

function resetFilters() {
  filter.value = { keyword: "", role: null, status: null };
  router.push({ path: "/users", query: {} });
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/users", query: buildQuery(nextPage) });
}

function goToAuditView() {
  router.push("/users/audit");
}

async function runUserAction(fn: (userId: number) => Promise<void>, user: AdminUser, label: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";
    await fn(user.userId);
    message.value = `用户 ${user.username}（UID ${user.userId}）${label}成功。`;
    await loadUsers();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : "操作失败，请稍后重试。";
  } finally {
    submitting.value = false;
    setTimeout(() => {
      message.value = "";
    }, 3000);
  }
}

async function handleDisable(user: AdminUser) {
  const confirmed = await confirmAction({
    title: `禁用用户 ${user.username}`,
    description: `账号 UID ${user.userId} 将被限制登录与访问后台外的业务能力。`,
    note: "请确认该账号已经完成人工核查，避免误伤正常用户。",
    confirmText: "确认禁用",
    badge: "Account Governance",
    tone: "danger",
  });

  if (!confirmed) return;
  await runUserAction(disableUser, user, "禁用");
}

async function handleEnable(user: AdminUser) {
  const confirmed = await confirmAction({
    title: `恢复用户 ${user.username}`,
    description: `账号 UID ${user.userId} 会重新获得正常登录与访问权限。`,
    confirmText: "确认恢复",
    badge: "Account Governance",
  });

  if (!confirmed) return;
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

  try {
    submitting.value = true;
    errorMessage.value = "";
    await updateUserRole(user.userId, targetRole);
    message.value = `用户 ${user.username} 的角色已变更为 ${targetRole === "ADMIN" ? "管理员" : "普通用户"}。`;
    roleDialog.value = null;
    await loadUsers();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : "角色调整失败，请稍后重试。";
  } finally {
    submitting.value = false;
    setTimeout(() => {
      message.value = "";
    }, 3000);
  }
}

watch(
  () => route.fullPath,
  () => {
    syncFilterFromRoute();
    void loadUsers();
  },
  { immediate: true }
);
</script>
