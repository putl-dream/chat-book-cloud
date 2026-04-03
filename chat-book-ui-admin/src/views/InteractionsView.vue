<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Comment Governance</p>
      <h1>评论治理中心</h1>
      <p class="hero-copy">
        全站评论统一治理入口，支持分页检索、异常评论统计、屏蔽、删除与恢复操作。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">正常评论</p>
        <h2>{{ stats?.normalCount ?? 0 }}</h2>
        <p class="metric-detail">状态为正常的评论总量</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">已屏蔽</p>
        <h2>{{ stats?.hiddenCount ?? 0 }}</h2>
        <p class="metric-detail">被管理员屏蔽的评论总量</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">已删除</p>
        <h2>{{ stats?.deletedCount ?? 0 }}</h2>
        <p class="metric-detail">已删除的评论总量</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Filters</p>
          <h3>评论筛选与检索</h3>
        </div>
      </div>
      <div class="toolbar-grid">
        <label class="field">
          <span>关键词</span>
          <input v-model="filter.keyword" placeholder="搜索评论内容" @keydown.enter="applyFilters" />
        </label>
        <label class="field">
          <span>状态</span>
          <select v-model="filter.status">
            <option :value="null">全部状态</option>
            <option :value="0">正常</option>
            <option :value="1">已删除</option>
            <option :value="2">已屏蔽</option>
          </select>
        </label>
        <label class="field">
          <span>文章 ID</span>
          <input v-model="filter.articleIdInput" placeholder="输入文章 ID" type="number" @keydown.enter="applyFilters" />
        </label>
        <label class="field">
          <span>用户 ID</span>
          <input v-model="filter.userIdInput" placeholder="输入用户 ID" type="number" @keydown.enter="applyFilters" />
        </label>
      </div>
      <div class="inline-actions" style="margin-top: 1rem;">
        <button class="panel-action-button primary" type="button" @click="applyFilters">搜索</button>
        <button class="panel-action-button" type="button" @click="resetFilters">重置</button>
      </div>
    </section>

    <template v-if="reviewPage">
      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Review Table</p>
            <h3>全站评论列表</h3>
          </div>
        </div>

        <p v-if="message" class="form-message success">{{ message }}</p>
        <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

        <RequestStatePanel
          v-if="reviewPage.list.length === 0"
          title="没有找到符合条件的评论"
          description="尝试调整筛选条件后重新搜索。"
        />

        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>评论内容</th>
                <th>用户</th>
                <th>文章 ID</th>
                <th>状态</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="review in reviewPage.list" :key="review.id">
                <td class="mono">{{ review.id }}</td>
                <td>
                  <div class="title-cell">
                    <strong>{{ review.content.length > 80 ? review.content.slice(0, 80) + "…" : review.content }}</strong>
                    <span v-if="review.parentId" class="mono">回复 #{{ review.parentId }}</span>
                  </div>
                </td>
                <td>
                  <div class="user-cell">
                    <strong>{{ review.username || "未知用户" }}</strong>
                    <span class="mono">UID {{ review.userId }}</span>
                  </div>
                </td>
                <td class="mono">{{ review.articleId }}</td>
                <td>
                  <span
                    :class="[
                      'pill',
                      review.status === 0 ? 'pill-safe' : review.status === 1 ? 'pill-danger' : 'pill-warn',
                    ]"
                  >
                    {{ reviewStatusMap[review.status] ?? "未知" }}
                  </span>
                </td>
                <td class="mono">{{ review.createTime }}</td>
                <td>
                  <div class="inline-actions">
                    <button
                      v-if="review.status === 0"
                      class="table-action-button danger"
                      :disabled="submitting"
                      type="button"
                      @click="handleHide(review.id)"
                    >屏蔽</button>
                    <button
                      v-if="review.status === 2"
                      class="table-action-button primary"
                      :disabled="submitting"
                      type="button"
                      @click="handleRestore(review.id)"
                    >恢复</button>
                    <button
                      v-if="review.status !== 1"
                      class="table-action-button danger"
                      :disabled="submitting"
                      type="button"
                      @click="handleDelete(review.id)"
                    >删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationControls
          :page="reviewPage.pageNo"
          :total="reviewPage.total"
          :total-pages="reviewPage.totalPages"
          @change="handlePageChange"
        />
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage && !reviewPage"
      title="评论列表加载失败"
      :description="errorMessage"
      tone="warning"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaginationControls from "@/components/shared/PaginationControls.vue";
import { confirmAction } from "@/composables/useConfirmDialog";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import {
  BrowserApiError,
  deleteReview,
  getInteractionReviewsPage,
  getInteractionReviewStats,
  hideReview,
  restoreReview,
} from "@/services/admin-api";
import type { InteractionReview, InteractionReviewStats, PaginatedResult } from "@/types/admin";

const reviewStatusMap: Record<number, string> = {
  0: "正常",
  1: "已删除",
  2: "已屏蔽",
};

const route = useRoute();
const router = useRouter();

const reviewPage = ref<PaginatedResult<InteractionReview> | null>(null);
const stats = ref<InteractionReviewStats | null>(null);
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");
const filter = ref({
  keyword: "",
  status: null as number | null,
  articleIdInput: "",
  userIdInput: "",
});

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function parseStatus(value: unknown) {
  const parsed = Number(value);
  return parsed === 0 || parsed === 1 || parsed === 2 ? parsed : null;
}

function syncFilterFromRoute() {
  filter.value = {
    keyword: typeof route.query.keyword === "string" ? route.query.keyword : "",
    status: parseStatus(route.query.status),
    articleIdInput: typeof route.query.articleId === "string" ? route.query.articleId : "",
    userIdInput: typeof route.query.userId === "string" ? route.query.userId : "",
  };
}

const page = ref(1);

watch(
  () => route.query.page,
  (value) => {
    page.value = parsePositiveInt(value, 1);
  },
  { immediate: true }
);

function buildQuery(nextPage = page.value) {
  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(filter.value.keyword ? { keyword: filter.value.keyword } : {}),
    ...(filter.value.status != null ? { status: String(filter.value.status) } : {}),
    ...(filter.value.articleIdInput ? { articleId: filter.value.articleIdInput } : {}),
    ...(filter.value.userIdInput ? { userId: filter.value.userIdInput } : {}),
  };
}

async function loadReviews() {
  try {
    errorMessage.value = "";
    const [pageResult, statsResult] = await Promise.all([
      getInteractionReviewsPage({
        page: page.value,
        size: 10,
        keyword: filter.value.keyword || undefined,
        status: filter.value.status,
        articleId: filter.value.articleIdInput ? Number(filter.value.articleIdInput) : undefined,
        userId: filter.value.userIdInput ? Number(filter.value.userIdInput) : undefined,
      }),
      getInteractionReviewStats(),
    ]);

    reviewPage.value = pageResult;
    stats.value = statsResult;

    if (reviewPage.value.list.length === 0 && reviewPage.value.total > 0 && page.value > 1) {
      router.replace({ path: "/interactions", query: buildQuery(page.value - 1) });
    }
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "评论列表读取失败，请确认网关地址和管理员接口是否可访问。";
  }
}

function applyFilters() {
  router.push({ path: "/interactions", query: buildQuery(1) });
}

function resetFilters() {
  filter.value = { keyword: "", status: null, articleIdInput: "", userIdInput: "" };
  router.push({ path: "/interactions", query: {} });
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/interactions", query: buildQuery(nextPage) });
}

async function runAction(fn: (id: number) => Promise<void>, id: number, label: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";
    await fn(id);
    message.value = `评论 #${id}${label}成功。`;
    await loadReviews();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : "评论治理操作失败，请稍后重试。";
  } finally {
    submitting.value = false;
    setTimeout(() => {
      message.value = "";
    }, 3000);
  }
}

async function handleHide(id: number) {
  const confirmed = await confirmAction({
    title: `屏蔽评论 #${id}`,
    description: "屏蔽后该评论将不再对普通用户展示，但仍会保留治理记录。",
    confirmText: "确认屏蔽",
    badge: "Comment Governance",
    tone: "warning",
  });

  if (!confirmed) return;
  await runAction(hideReview, id, "屏蔽");
}

async function handleRestore(id: number) {
  const confirmed = await confirmAction({
    title: `恢复评论 #${id}`,
    description: "恢复后该评论会重新回到正常可见状态。",
    confirmText: "确认恢复",
    badge: "Comment Governance",
  });

  if (!confirmed) return;
  await runAction(restoreReview, id, "恢复");
}

async function handleDelete(id: number) {
  const confirmed = await confirmAction({
    title: `删除评论 #${id}`,
    description: "删除后该评论将从内容视图中移除，且不建议作为常规治理手段频繁使用。",
    note: "高风险操作。请确认已经完成违规留痕或替代处置。",
    confirmText: "确认删除",
    badge: "Comment Governance",
    tone: "danger",
  });

  if (!confirmed) return;
  await runAction(deleteReview, id, "删除");
}

watch(
  () => route.fullPath,
  () => {
    syncFilterFromRoute();
    void loadReviews();
  },
  { immediate: true }
);
</script>
