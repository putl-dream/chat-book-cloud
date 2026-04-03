<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Admin Audit</p>
      <h1>管理员操作审计</h1>
      <p class="hero-copy">
        统一查看后台治理动作，追踪操作人、对象、时间和详情，支撑账号治理与内容治理的追溯。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">日志总数</p>
        <h2>{{ logPage?.total ?? 0 }}</h2>
        <p class="metric-detail">来自 /user/admin/operation-log/page 的真实审计记录</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">当前页操作人</p>
        <h2>{{ operatorCount }}</h2>
        <p class="metric-detail">用于追踪治理动作的执行主体</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">分页状态</p>
        <h2>{{ logPage?.pageNo ?? 1 }}/{{ logPage?.totalPages ?? 1 }}</h2>
        <p class="metric-detail">支持按动作、对象和时间窗口筛选</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Filters</p>
          <h3>审计检索条件</h3>
        </div>
      </div>
      <div class="toolbar-grid">
        <label class="field">
          <span>动作</span>
          <select v-model="filter.action">
            <option :value="null">全部动作</option>
            <option value="USER_ROLE_CHANGE">角色调整</option>
            <option value="USER_DISABLE">禁用账号</option>
            <option value="USER_ENABLE">恢复账号</option>
          </select>
        </label>
        <label class="field">
          <span>对象类型</span>
          <select v-model="filter.targetType">
            <option :value="null">全部对象</option>
            <option value="USER">用户</option>
            <option value="ARTICLE">文章</option>
            <option value="REVIEW">评论</option>
            <option value="TAG">标签</option>
          </select>
        </label>
        <label class="field">
          <span>对象 ID</span>
          <input v-model="filter.targetIdInput" placeholder="输入对象 ID" type="number" @keydown.enter="applyFilters" />
        </label>
        <label class="field">
          <span>操作人 ID</span>
          <input v-model="filter.operatorIdInput" placeholder="输入操作人 ID" type="number" @keydown.enter="applyFilters" />
        </label>
        <label class="field">
          <span>开始时间</span>
          <input v-model="filter.startTime" type="datetime-local" />
        </label>
        <label class="field">
          <span>结束时间</span>
          <input v-model="filter.endTime" type="datetime-local" />
        </label>
      </div>
      <div class="inline-actions" style="margin-top: 1rem;">
        <button class="panel-action-button primary" type="button" @click="applyFilters">搜索</button>
        <button class="panel-action-button" type="button" @click="resetFilters">重置</button>
      </div>
    </section>

    <template v-if="logPage">
      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Audit Table</p>
            <h3>管理员操作日志</h3>
          </div>
        </div>

        <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

        <RequestStatePanel
          v-if="logPage.list.length === 0"
          title="当前没有符合条件的审计记录"
          description="尝试调整筛选条件后重新搜索。"
        />

        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>时间</th>
                <th>操作人</th>
                <th>动作</th>
                <th>对象</th>
                <th>详情</th>
                <th>IP</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logPage.list" :key="log.id">
                <td class="mono">{{ log.createTime }}</td>
                <td>
                  <div class="user-cell">
                    <strong>{{ log.operatorName || "未知操作人" }}</strong>
                    <span class="mono">UID {{ log.operatorId ?? "--" }}</span>
                  </div>
                </td>
                <td>
                  <span class="pill pill-neutral">{{ actionLabelMap[log.action] || log.action }}</span>
                </td>
                <td>
                  <div class="title-cell">
                    <strong>{{ targetTypeMap[log.targetType] || log.targetType }}</strong>
                    <span class="mono">ID {{ log.targetId ?? "--" }}</span>
                  </div>
                </td>
                <td class="mono">{{ log.detail || "-" }}</td>
                <td class="mono">{{ log.ip || "-" }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationControls
          :page="logPage.pageNo"
          :total="logPage.total"
          :total-pages="logPage.totalPages"
          @change="handlePageChange"
        />
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage"
      title="审计日志页暂时不可用"
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
import { BrowserApiError, getAdminOperationLogsPage } from "@/services/admin-api";
import type { AdminOperationLog, PaginatedResult } from "@/types/admin";

const actionLabelMap: Record<string, string> = {
  USER_ROLE_CHANGE: "角色调整",
  USER_DISABLE: "禁用账号",
  USER_ENABLE: "恢复账号",
};

const targetTypeMap: Record<string, string> = {
  USER: "用户",
  ARTICLE: "文章",
  REVIEW: "评论",
  TAG: "标签",
};

type AuditActionFilter = "USER_ROLE_CHANGE" | "USER_DISABLE" | "USER_ENABLE" | null;
type AuditTargetTypeFilter = "USER" | "ARTICLE" | "REVIEW" | "TAG" | null;

const route = useRoute();
const router = useRouter();

const logPage = ref<PaginatedResult<AdminOperationLog> | null>(null);
const errorMessage = ref("");
const filter = ref({
  action: null as AuditActionFilter,
  targetType: null as AuditTargetTypeFilter,
  targetIdInput: "",
  operatorIdInput: "",
  startTime: "",
  endTime: "",
});

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function parseAction(value: unknown): AuditActionFilter {
  return value === "USER_ROLE_CHANGE" || value === "USER_DISABLE" || value === "USER_ENABLE" ? value : null;
}

function parseTargetType(value: unknown): AuditTargetTypeFilter {
  return value === "USER" || value === "ARTICLE" || value === "REVIEW" || value === "TAG" ? value : null;
}

function normalizeDateTime(value: unknown) {
  return typeof value === "string" ? value : "";
}

function toApiDateTime(value: string) {
  if (!value) return undefined;
  return value.length === 16 ? `${value.replace("T", " ")}:00` : value.replace("T", " ");
}

function syncFilterFromRoute() {
  filter.value = {
    action: parseAction(route.query.action),
    targetType: parseTargetType(route.query.targetType),
    targetIdInput: typeof route.query.targetId === "string" ? route.query.targetId : "",
    operatorIdInput: typeof route.query.operatorId === "string" ? route.query.operatorId : "",
    startTime: normalizeDateTime(route.query.startTime),
    endTime: normalizeDateTime(route.query.endTime),
  };
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const operatorCount = computed(() => new Set(logPage.value?.list.map((item) => item.operatorId).filter(Boolean) ?? []).size);

function buildQuery(nextPage = page.value) {
  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(filter.value.action ? { action: filter.value.action } : {}),
    ...(filter.value.targetType ? { targetType: filter.value.targetType } : {}),
    ...(filter.value.targetIdInput ? { targetId: filter.value.targetIdInput } : {}),
    ...(filter.value.operatorIdInput ? { operatorId: filter.value.operatorIdInput } : {}),
    ...(filter.value.startTime ? { startTime: filter.value.startTime } : {}),
    ...(filter.value.endTime ? { endTime: filter.value.endTime } : {}),
  };
}

async function loadLogs() {
  try {
    errorMessage.value = "";
    logPage.value = await getAdminOperationLogsPage({
      page: page.value,
      size: 20,
      action: filter.value.action,
      targetType: filter.value.targetType,
      targetId: filter.value.targetIdInput ? Number(filter.value.targetIdInput) : undefined,
      operatorId: filter.value.operatorIdInput ? Number(filter.value.operatorIdInput) : undefined,
      startTime: toApiDateTime(filter.value.startTime),
      endTime: toApiDateTime(filter.value.endTime),
    });

    if (logPage.value.list.length === 0 && logPage.value.total > 0 && page.value > 1) {
      router.replace({ path: "/users/audit", query: buildQuery(page.value - 1) });
    }
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "审计日志读取失败，请确认网关地址和管理员接口是否可访问。";
  }
}

function applyFilters() {
  router.push({ path: "/users/audit", query: buildQuery(1) });
}

function resetFilters() {
  filter.value = {
    action: null,
    targetType: null,
    targetIdInput: "",
    operatorIdInput: "",
    startTime: "",
    endTime: "",
  };
  router.push({ path: "/users/audit", query: {} });
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/users/audit", query: buildQuery(nextPage) });
}

watch(
  () => route.fullPath,
  () => {
    syncFilterFromRoute();
    void loadLogs();
  },
  { immediate: true }
);
</script>
