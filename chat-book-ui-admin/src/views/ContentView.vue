<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Content Operations</p>
      <h1>内容管理与存量运营</h1>
      <p class="hero-copy">
        审核通过后的文章仍需要长期治理。后台内容管理模块负责全站内容检索、上下架、人工巡检、
        数据追踪和专题运营入口。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">已发布</p>
        <h2>{{ publishedCount }}</h2>
        <p class="metric-detail">用于首页推荐、热度监控和专题运营</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">草稿</p>
        <h2>{{ draftCount }}</h2>
        <p class="metric-detail">可作为创作者召回和创作漏斗分析依据</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">已删除</p>
        <h2>{{ deletedCount }}</h2>
        <p class="metric-detail">仅管理员可见，可执行恢复</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Filters</p>
          <h3>内容筛选与检索</h3>
        </div>
      </div>
      <div class="toolbar-grid">
        <label class="field">
          <span>关键词</span>
          <input
            v-model="filter.keyword"
            placeholder="搜索标题或摘要"
            @keydown.enter="applyFilters"
          />
        </label>
        <label class="field">
          <span>状态</span>
          <select v-model="filter.status">
            <option :value="null">全部状态</option>
            <option :value="0">草稿</option>
            <option :value="1">待审核</option>
            <option :value="2">已发布</option>
            <option :value="-1">已删除</option>
          </select>
        </label>
        <label class="field">
          <span>分类</span>
          <select v-model="filter.category">
            <option :value="null">全部分类</option>
            <option v-for="(label, key) in articleCategoryMap" :key="key" :value="Number(key)">
              {{ label }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>内容类型</span>
          <select v-model="filter.contentType">
            <option :value="null">全部类型</option>
            <option :value="0">学习 / 教程</option>
            <option :value="1">实战 / 项目</option>
          </select>
        </label>
      </div>
      <div class="inline-actions" style="margin-top: 1rem">
        <button class="panel-action-button primary" type="button" @click="applyFilters">
          搜索
        </button>
        <button class="panel-action-button" type="button" @click="resetFilters">重置</button>
      </div>
    </section>

    <template v-if="articlePage">
      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Content Table</p>
            <h3>内容资产视图</h3>
          </div>
        </div>

        <div class="review-bulk-bar">
          <label class="review-select-toggle">
            <input
              :checked="allSelected"
              :disabled="submitting || !articlePage.list.length"
              type="checkbox"
              @change="toggleAllSelection"
            />
            <span>当前页全选</span>
          </label>
          <div class="inline-actions">
            <span class="pill pill-neutral">已选 {{ selectedIds.length }} 篇</span>
            <button
              class="panel-action-button"
              :disabled="submitting || selectedIds.length === 0"
              type="button"
              @click="selectedIds = []"
            >
              清空选择
            </button>
            <button
              class="panel-action-button primary"
              :disabled="submitting || selectedIds.length === 0"
              type="button"
              @click="handleBatchPublish"
            >
              批量发布
            </button>
            <button
              class="panel-action-button"
              :disabled="submitting || selectedIds.length === 0"
              type="button"
              @click="handleBatchUnpublish"
            >
              批量下架
            </button>
            <button
              class="panel-action-button danger"
              :disabled="submitting || selectedIds.length === 0"
              type="button"
              @click="handleBatchDelete"
            >
              批量删除
            </button>
          </div>
        </div>

        <p v-if="message" class="form-message success">{{ message }}</p>
        <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

        <RequestStatePanel
          v-if="articlePage.list.length === 0"
          title="没有找到符合条件的文章"
          description="尝试调整筛选条件后重新搜索。"
        />

        <div v-else class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th style="width: 2rem"></th>
                <th>标题</th>
                <th>作者</th>
                <th>分类 / 类型</th>
                <th>状态</th>
                <th>互动数据</th>
                <th>时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="article in articlePage.list" :key="article.id">
                <td>
                  <input
                    :checked="selectedIds.includes(article.id)"
                    :disabled="submitting"
                    type="checkbox"
                    @change="toggleSelection(article.id)"
                  />
                </td>
                <td>
                  <div class="title-cell">
                    <strong>{{ article.title }}</strong>
                    <span>{{ article.abstractText || "无摘要" }}</span>
                  </div>
                </td>
                <td>
                  <div class="user-cell">
                    <strong>{{ article.userName }}</strong>
                    <span class="mono">UID {{ article.userId }}</span>
                  </div>
                </td>
                <td>
                  {{ articleCategoryMap[article.category] || "未分类" }} /
                  {{ contentTypeMap[article.contentType] || "未分类" }}
                </td>
                <td>
                  <span
                    :class="[
                      'pill',
                      article.status === 2
                        ? 'pill-safe'
                        : article.status === 1
                          ? 'pill-warn'
                          : article.status === 0
                            ? 'pill-neutral'
                            : 'pill-danger',
                    ]"
                  >
                    {{ articleStatusMap[article.status] ?? "未知" }}
                  </span>
                </td>
                <td class="mono">
                  V {{ article.viewCount }} / C {{ article.commentCount }} / P
                  {{ article.praiseCount }}
                </td>
                <td class="mono">{{ article.createTime }}</td>
                <td>
                  <div class="inline-actions">
                    <button
                      v-if="article.status !== 2"
                      class="table-action-button primary"
                      :disabled="submitting"
                      type="button"
                      @click="handlePublish(article)"
                    >
                      发布
                    </button>
                    <button
                      v-if="article.status === 2"
                      class="table-action-button"
                      :disabled="submitting"
                      type="button"
                      @click="handleUnpublish(article.id)"
                    >
                      下架
                    </button>
                    <button
                      v-if="article.status === -1"
                      class="table-action-button"
                      :disabled="submitting"
                      type="button"
                      @click="handleRestore(article.id)"
                    >
                      恢复
                    </button>
                    <button
                      v-if="article.status !== -1"
                      class="table-action-button danger"
                      :disabled="submitting"
                      type="button"
                      @click="handleDelete(article.id)"
                    >
                      删除
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationControls
          v-if="articlePage"
          :page="articlePage.pageNo"
          :total="articlePage.total"
          :total-pages="articlePage.totalPages"
          @change="handlePageChange"
        />
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage && !articlePage"
      title="内容列表加载失败"
      :description="errorMessage"
      tone="warning"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import PaginationControls from "@/components/shared/PaginationControls.vue";
import { confirmAction } from "@/composables/useConfirmDialog";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { articleCategoryMap, articleStatusMap, contentTypeMap } from "@/data/admin-config";
import {
  batchDeleteArticles,
  batchPublish,
  batchUnpublish,
  BrowserApiError,
  deleteArticle,
  getContentArticlesPage,
  publishArticle,
  restoreArticle,
  unpublishArticle,
} from "@/services/admin-api";
import type { ContentArticle, ContentPageParams, PaginatedResult } from "@/types/admin";

const route = useRoute();
const router = useRouter();

const articlePage = ref<PaginatedResult<ContentArticle> | null>(null);
const selectedIds = ref<number[]>([]);
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");

const filter = ref<ContentPageParams>({
  keyword: (route.query.keyword as string) || "",
  status: route.query.status != null ? Number(route.query.status) : null,
  category: route.query.category != null ? Number(route.query.category) : null,
  contentType: route.query.contentType != null ? Number(route.query.contentType) : null,
});

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));

const publishedCount = computed(
  () => articlePage.value?.list.filter((a) => a.status === 2).length ?? 0
);
const draftCount = computed(
  () => articlePage.value?.list.filter((a) => a.status === 0).length ?? 0
);
const deletedCount = computed(
  () => articlePage.value?.list.filter((a) => a.status === -1).length ?? 0
);
const allSelected = computed(
  () =>
    !!articlePage.value?.list.length &&
    articlePage.value.list.every((a) => selectedIds.value.includes(a.id))
);

function buildQuery(nextPage: number) {
  const q: Record<string, string> = {};
  if (nextPage > 1) q.page = String(nextPage);
  if (filter.value.status != null) q.status = String(filter.value.status);
  if (filter.value.category != null) q.category = String(filter.value.category);
  if (filter.value.contentType != null) q.contentType = String(filter.value.contentType);
  if (filter.value.keyword) q.keyword = filter.value.keyword;
  return q;
}

async function loadContent() {
  try {
    errorMessage.value = "";
    articlePage.value = await getContentArticlesPage({
      ...filter.value,
      pageNo: page.value,
      pageSize: 10,
    });
    selectedIds.value = [];
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "内容列表读取失败，请确认网关地址和管理员接口是否可访问。";
  }
}

function applyFilters() {
  router.push({ path: "/articles/content", query: buildQuery(1) });
}

function resetFilters() {
  filter.value = { keyword: "", status: null, category: null, contentType: null };
  router.push({ path: "/articles/content", query: {} });
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/articles/content", query: buildQuery(nextPage) });
}

function toggleSelection(id: number) {
  selectedIds.value = selectedIds.value.includes(id)
    ? selectedIds.value.filter((i) => i !== id)
    : [...selectedIds.value, id];
}

function toggleAllSelection() {
  if (!articlePage.value) return;
  selectedIds.value = allSelected.value ? [] : articlePage.value.list.map((a) => a.id);
}

async function runAction(fn: (id: number) => Promise<void>, id: number, label: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";
    await fn(id);
    message.value = `${label}操作成功。`;
    await loadContent();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : `${label}操作失败，请稍后重试。`;
  } finally {
    submitting.value = false;
    setTimeout(() => {
      message.value = "";
    }, 3000);
  }
}

async function runBatchAction(fn: (ids: number[]) => Promise<void>, ids: number[], label: string) {
  try {
    submitting.value = true;
    errorMessage.value = "";
    await fn(ids);
    message.value = `批量${label}成功（${ids.length} 篇）。`;
    await loadContent();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : `批量${label}失败，请稍后重试。`;
  } finally {
    submitting.value = false;
    setTimeout(() => {
      message.value = "";
    }, 3000);
  }
}

async function handlePublish(article: ContentArticle) {
  const confirmed = await confirmAction({
    title: `发布文章 ${article.title}`,
    description: "发布后文章会进入线上可见状态，并参与前台分发与运营曝光。",
    confirmText: "确认发布",
    badge: "Content Operations",
  });

  if (!confirmed) return;
  await runAction(publishArticle, article.id, "发布");
}

async function handleUnpublish(id: number) {
  const confirmed = await confirmAction({
    title: `下架文章 #${id}`,
    description: "下架后文章将停止对外展示，但仍保留后台治理与恢复能力。",
    confirmText: "确认下架",
    badge: "Content Operations",
    tone: "warning",
  });

  if (!confirmed) return;
  await runAction(unpublishArticle, id, "下架");
}

async function handleDelete(id: number) {
  const confirmed = await confirmAction({
    title: `删除文章 #${id}`,
    description: "删除后该文章会从内容资产视图移除。",
    note: "此操作不可恢复，请确认该文章不再需要保留。",
    confirmText: "确认删除",
    badge: "Content Operations",
    tone: "danger",
  });

  if (!confirmed) return;
  await runAction(deleteArticle, id, "删除");
}

async function handleRestore(id: number) {
  const confirmed = await confirmAction({
    title: `恢复文章 #${id}`,
    description: "恢复后文章会回到可运营状态，并重新出现在内容管理列表中。",
    confirmText: "确认恢复",
    badge: "Content Operations",
  });

  if (!confirmed) return;
  await runAction(restoreArticle, id, "恢复");
}

async function handleBatchPublish() {
  if (selectedIds.value.length === 0) {
    errorMessage.value = "请先选择要批量发布的文章。";
    return;
  }
  const confirmed = await confirmAction({
    title: `批量发布 ${selectedIds.value.length} 篇文章`,
    description: "这些文章会同时进入线上分发流程，请确认内容已经满足运营要求。",
    confirmText: "确认批量发布",
    badge: "Content Operations",
  });

  if (!confirmed) return;
  await runBatchAction(batchPublish, [...selectedIds.value], "发布");
}

async function handleBatchUnpublish() {
  if (selectedIds.value.length === 0) {
    errorMessage.value = "请先选择要批量下架的文章。";
    return;
  }
  const confirmed = await confirmAction({
    title: `批量下架 ${selectedIds.value.length} 篇文章`,
    description: "下架后这些文章会同时停止对外展示。",
    confirmText: "确认批量下架",
    badge: "Content Operations",
    tone: "warning",
  });

  if (!confirmed) return;
  await runBatchAction(batchUnpublish, [...selectedIds.value], "下架");
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    errorMessage.value = "请先选择要批量删除的文章。";
    return;
  }
  const confirmed = await confirmAction({
    title: `批量删除 ${selectedIds.value.length} 篇文章`,
    description: "这些文章会从内容资产列表中统一移除。",
    note: "此操作不可恢复，请确认批量范围无误。",
    confirmText: "确认批量删除",
    badge: "Content Operations",
    tone: "danger",
  });

  if (!confirmed) return;
  await runBatchAction(batchDeleteArticles, [...selectedIds.value], "删除");
}

// Sync filter from URL query
watch(page, loadContent, { immediate: true });
</script>
