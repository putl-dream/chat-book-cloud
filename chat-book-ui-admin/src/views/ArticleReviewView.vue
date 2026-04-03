<template>
  <section class="page-shell">
    <template v-if="reviewPage">
      <div class="page-hero compact">
        <p class="eyebrow">Editorial Review</p>
        <h1>文章审核工作台</h1>
        <p class="hero-copy">
          待审核列表、单条审核、批量审核和详情侧栏已经统一到浏览器端，当前可直接查看摘要、分类、内容类型与标签信息。
        </p>
      </div>

      <div class="metric-grid compact-grid">
        <article class="metric-card">
          <p class="metric-label">待审核文章</p>
          <h2>{{ reviewPage.total }}</h2>
          <p class="metric-detail">真实数据来自 /page/adminArticlePage</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">当前页作者</p>
          <h2>{{ authorCount }}</h2>
          <p class="metric-detail">可联动用户治理与风险巡检</p>
        </article>
        <article class="metric-card">
          <p class="metric-label">审核动作接口</p>
          <h2>3</h2>
          <p class="metric-detail">已接通通过、驳回和批量审核</p>
        </article>
      </div>

      <section v-if="focusedArticle" class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Detail Overview</p>
            <h3>文章详情概览</h3>
          </div>
          <span class="pill pill-neutral">Article #{{ focusedArticle.id }}</span>
        </div>
        <div class="stack-list">
          <article class="stack-item">
            <div class="stack-title-row">
              <h4>{{ focusedArticle.title }}</h4>
              <span class="pill pill-neutral">{{ contentTypeMap[focusedArticle.contentType ?? -1] || "未标记类型" }}</span>
            </div>
            <p>{{ focusedArticle.summary }}</p>
            <p class="meta-line">
              作者 {{ focusedArticle.userName }} / 分类 {{ articleCategoryMap[focusedArticle.category] || "未分类" }} /
              创建于 {{ focusedArticle.createdAt }}
            </p>
            <div class="chip-row">
              <span v-for="tagName in getTagNames(focusedArticle.tagIds)" :key="`${focusedArticle.id}-${tagName}`" class="chip">
                {{ tagName }}
              </span>
              <span v-if="getTagNames(focusedArticle.tagIds).length === 0" class="chip">未关联标签</span>
            </div>
          </article>
          <article class="stack-item">
            <div class="stack-title-row">
              <h4>互动数据</h4>
              <span class="pill pill-safe">已接通</span>
            </div>
            <p class="mono">
              浏览 {{ focusedArticle.viewCount }} / 评论 {{ focusedArticle.commentCount }} / 点赞 {{ focusedArticle.praiseCount }} / 收藏 {{ focusedArticle.collectCount }}
            </p>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Pending Queue</p>
            <h3>待审核内容队列</h3>
          </div>
        </div>

        <RequestStatePanel
          v-if="reviewPage.list.length === 0"
          title="当前没有待审核文章"
          description="审核队列为空，后续可继续增强正文详情与审核历史。"
        />

        <div v-else class="stack-list">
          <div class="review-bulk-bar">
            <label class="review-select-toggle">
              <input
                :checked="allSelected"
                :disabled="submitting || reviewPage.list.length === 0"
                type="checkbox"
                @change="toggleAllSelection"
              />
              <span>当前页全选</span>
            </label>

            <div class="inline-actions">
              <span class="pill pill-neutral">已选 {{ selectedIds.length }} 篇</span>
              <button class="panel-action-button" :disabled="submitting || selectedIds.length === 0" type="button" @click="selectedIds = []">
                清空选择
              </button>
              <button
                class="panel-action-button primary"
                :disabled="submitting || selectedIds.length === 0"
                type="button"
                @click="handleBatchApprove"
              >
                {{ submitting ? "处理中..." : "批量通过" }}
              </button>
              <button
                class="panel-action-button"
                :disabled="submitting || selectedIds.length === 0"
                type="button"
                @click="openBatchReject"
              >
                批量驳回
              </button>
            </div>
          </div>

          <p v-if="message" class="form-message success">{{ message }}</p>
          <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

          <article v-for="article in reviewPage.list" :key="article.id" :id="`review-${article.id}`" class="review-card">
            <div class="review-card-top">
              <div>
                <h4>{{ article.title }}</h4>
                <p class="meta-line">
                  作者 {{ article.userName }} / 分类 {{ articleCategoryMap[article.category] || "未分类" }} /
                  类型 {{ contentTypeMap[article.contentType ?? -1] || "未标记类型" }} /
                  互动 V {{ article.viewCount }} · C {{ article.commentCount }} · P {{ article.praiseCount }}
                </p>
              </div>
              <span class="pill pill-warn">待审核</span>
            </div>

            <p class="review-summary">{{ article.summary }}</p>

            <div class="chip-row">
              <span class="chip">{{ contentTypeMap[article.contentType ?? -1] || "未标记类型" }}</span>
              <span v-for="tagName in getTagNames(article.tagIds)" :key="`${article.id}-${tagName}`" class="chip">{{ tagName }}</span>
              <span v-if="getTagNames(article.tagIds).length === 0" class="chip">未关联标签</span>
              <span v-if="focusId === article.id" class="chip">当前聚焦文章</span>
            </div>

            <div class="review-card-actions">
              <label class="review-select-toggle">
                <input
                  :checked="selectedIds.includes(article.id)"
                  :disabled="submitting"
                  type="checkbox"
                  @change="toggleSelection(article.id)"
                />
                <span>纳入批量处理</span>
              </label>

              <div class="inline-actions">
                <button class="table-action-button" :disabled="submitting" type="button" @click="handleApprove(article.id)">
                  通过
                </button>
                <button class="table-action-button danger" :disabled="submitting" type="button" @click="openSingleReject(article)">
                  驳回
                </button>
                <button class="table-action-button" type="button" @click="focusArticle(article.id)">查看详情</button>
              </div>
            </div>

            <div class="review-footer">
              <span class="mono">Article #{{ article.id }}</span>
              <span class="mono">{{ article.createdAt }}</span>
            </div>
          </article>

          <PaginationControls
            :page="reviewPage.pageNo"
            :total="reviewPage.total"
            :total-pages="reviewPage.totalPages"
            @change="handlePageChange"
          />
        </div>
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage"
      title="文章审核页暂时不可用"
      :description="errorMessage"
      tone="warning"
    />

    <div v-if="rejectDialog" class="dialog-backdrop" role="presentation">
      <section class="dialog-panel" role="dialog" aria-modal="true">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Reject Review</p>
            <h3>{{ rejectDialog.title }}</h3>
          </div>
          <span class="pill pill-danger">需要记录驳回原因</span>
        </div>

        <form class="dialog-form" @submit.prevent="handleRejectSubmit">
          <label class="field">
            <span>驳回原因</span>
            <textarea
              v-model="rejectReason"
              :disabled="submitting"
              placeholder="请填写结构化驳回原因，例如：封面与内容不符、摘要过短、存在违规描述。"
              rows="5"
            />
          </label>

          <p class="form-message">驳回后文章会退回草稿态，审核日志会记录审核人、时间和原因。</p>

          <div class="dialog-actions">
            <button class="panel-action-button" :disabled="submitting" type="button" @click="closeRejectDialog">
              取消
            </button>
            <button class="panel-action-button primary" :disabled="submitting" type="submit">
              {{ submitting ? "提交中..." : "确认驳回" }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaginationControls from "@/components/shared/PaginationControls.vue";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { articleCategoryMap, contentTypeMap } from "@/data/admin-config";
import {
  approveReviewArticle,
  batchReviewArticles,
  BrowserApiError,
  getReviewArticlesPage,
  getTagList,
  rejectReviewArticle,
} from "@/services/admin-api";
import type { AdminTag, PaginatedResult, ReviewAction, ReviewArticle } from "@/types/admin";

type RejectDialogState =
  | {
      mode: "single" | "batch";
      articleIds: number[];
      title: string;
    }
  | null;

const route = useRoute();
const router = useRouter();

const reviewPage = ref<PaginatedResult<ReviewArticle> | null>(null);
const selectedIds = ref<number[]>([]);
const rejectDialog = ref<RejectDialogState>(null);
const rejectReason = ref("");
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");
const tagList = ref<AdminTag[]>([]);
const tagListLoaded = ref(false);

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const size = computed(() => parsePositiveInt(route.query.size, 8));
const focusId = computed(() => parsePositiveInt(route.query.focus, 0));
const focusedArticle = computed(() => reviewPage.value?.list.find((article) => article.id === focusId.value));
const authorCount = computed(() => new Set(reviewPage.value?.list.map((article) => article.userId) ?? []).size);
const allSelected = computed(
  () => !!reviewPage.value?.list.length && reviewPage.value.list.every((article) => selectedIds.value.includes(article.id))
);
const tagNameMap = computed(() => new Map(tagList.value.map((tag) => [tag.id, tag.name])));

function getTagNames(tagIds?: number[]) {
  return (tagIds ?? []).map((tagId) => tagNameMap.value.get(tagId) || `#${tagId}`);
}

function buildQuery(nextPage = page.value, nextFocus = focusId.value || undefined) {
  return {
    ...(nextFocus ? { focus: String(nextFocus) } : {}),
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(size.value !== 8 ? { size: String(size.value) } : {}),
  };
}

async function loadTagList() {
  if (tagListLoaded.value) return;

  try {
    tagList.value = await getTagList();
    tagListLoaded.value = true;
  } catch {
    tagListLoaded.value = false;
  }
}

async function loadReviewPage() {
  try {
    errorMessage.value = "";
    const [pageResult] = await Promise.all([
      getReviewArticlesPage({ page: page.value, size: size.value }),
      loadTagList(),
    ]);
    reviewPage.value = pageResult;
    selectedIds.value = [];

    if (reviewPage.value.list.length === 0 && reviewPage.value.total > 0 && page.value > 1) {
      router.replace({ path: "/articles/review", query: buildQuery(page.value - 1) });
    }
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "审核列表读取失败，请确认网关地址和文章后台接口是否可访问。";
  }
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/articles/review", query: buildQuery(nextPage) });
}

function focusArticle(articleId: number) {
  router.push({ path: "/articles/review", query: buildQuery(page.value, articleId), hash: `#review-${articleId}` });
}

function toggleSelection(articleId: number) {
  selectedIds.value = selectedIds.value.includes(articleId)
    ? selectedIds.value.filter((id) => id !== articleId)
    : [...selectedIds.value, articleId];
}

function toggleAllSelection() {
  if (!reviewPage.value) return;
  selectedIds.value = allSelected.value ? [] : reviewPage.value.list.map((article) => article.id);
}

async function runReviewAction(articleIds: number[], action: ReviewAction, reason?: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";

    if (articleIds.length === 1) {
      if (action === "APPROVE") {
        await approveReviewArticle(articleIds[0]);
        message.value = `文章 #${articleIds[0]} 已审核通过。`;
      } else {
        await rejectReviewArticle(articleIds[0], reason ?? "");
        message.value = `文章 #${articleIds[0]} 已驳回并退回草稿。`;
      }
    } else {
      await batchReviewArticles(articleIds, action, reason);
      message.value =
        action === "APPROVE"
          ? `已批量通过 ${articleIds.length} 篇文章。`
          : `已批量驳回 ${articleIds.length} 篇文章。`;
    }

    closeRejectDialog();
    await loadReviewPage();
  } catch (error) {
    errorMessage.value = error instanceof BrowserApiError ? error.message : "审核操作失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

async function handleApprove(articleId: number) {
  if (!window.confirm(`确认通过文章 #${articleId} 吗？`)) return;
  await runReviewAction([articleId], "APPROVE");
}

async function handleBatchApprove() {
  if (selectedIds.value.length === 0) {
    errorMessage.value = "请先选择要批量处理的文章。";
    return;
  }

  if (!window.confirm(`确认批量通过选中的 ${selectedIds.value.length} 篇文章吗？`)) return;
  await runReviewAction(selectedIds.value, "APPROVE");
}

function openSingleReject(article: ReviewArticle) {
  errorMessage.value = "";
  rejectReason.value = "";
  rejectDialog.value = {
    mode: "single",
    articleIds: [article.id],
    title: `驳回文章 #${article.id}`,
  };
}

function openBatchReject() {
  if (selectedIds.value.length === 0) {
    errorMessage.value = "请先选择要批量驳回的文章。";
    return;
  }

  errorMessage.value = "";
  rejectReason.value = "";
  rejectDialog.value = {
    mode: "batch",
    articleIds: selectedIds.value,
    title: `批量驳回 ${selectedIds.value.length} 篇文章`,
  };
}

function closeRejectDialog() {
  rejectDialog.value = null;
  rejectReason.value = "";
}

async function handleRejectSubmit() {
  if (!rejectDialog.value) return;

  if (!rejectReason.value.trim()) {
    errorMessage.value = "请输入驳回原因，便于后续追溯与作者修改。";
    return;
  }

  const confirmMessage =
    rejectDialog.value.mode === "single"
      ? `确认驳回文章 #${rejectDialog.value.articleIds[0]} 吗？`
      : `确认批量驳回选中的 ${rejectDialog.value.articleIds.length} 篇文章吗？`;

  if (!window.confirm(confirmMessage)) return;
  await runReviewAction(rejectDialog.value.articleIds, "REJECT", rejectReason.value.trim());
}

watch(
  () => route.fullPath,
  () => {
    void loadReviewPage();
  },
  { immediate: true }
);
</script>
