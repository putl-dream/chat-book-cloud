<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Tag Taxonomy</p>
      <h1>标签体系与内容组织</h1>
      <p class="hero-copy">
        当前项目将标签分为主题标签、技术栈和学习路径三类。新版 Vue 管理端保留了列表、筛选、创建、编辑和删除流程，
        并直接复用原有后台接口契约。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">主题标签</p>
        <h2>{{ topicCount }}</h2>
        <p class="metric-detail">用于承担文章主分类职责</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">技术栈标签</p>
        <h2>{{ techCount }}</h2>
        <p class="metric-detail">真实数量来自 /tag/list</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">学习路径标签</p>
        <h2>{{ pathCount }}</h2>
        <p class="metric-detail">支持按类型筛选和分页查询</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">当前分页</p>
        <h2>{{ tagPage?.pageNo ?? 1 }}/{{ tagPage?.totalPages ?? 1 }}</h2>
        <p class="metric-detail">总计 {{ tagPage?.total ?? 0 }} 个标签</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Operations</p>
          <h3>标签筛选与 CRUD</h3>
        </div>
        <div class="inline-actions">
          <button class="panel-action-button" type="button" @click="loadTags">刷新</button>
          <button class="panel-action-button primary" type="button" @click="openCreateDialog">新增标签</button>
        </div>
      </div>

      <div class="toolbar-grid">
        <label class="field">
          <span>标签类型</span>
          <select :value="currentType ? String(currentType) : 'all'" @change="handleTypeChange">
            <option value="all">全部类型</option>
            <option value="3">主题标签</option>
            <option value="1">技术栈</option>
            <option value="2">学习路径</option>
          </select>
        </label>
        <label class="field">
          <span>搜索预留</span>
          <input disabled placeholder="后续可接名称关键词查询" />
        </label>
        <label class="field">
          <span>提交反馈</span>
          <input disabled placeholder="操作成功后会自动刷新当前列表" />
        </label>
      </div>

      <p v-if="message" class="form-message success">{{ message }}</p>
      <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

      <div v-if="tagPage" class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>类型</th>
              <th>颜色</th>
              <th>排序权重</th>
              <th>ID</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="tagPage.list.length === 0">
              <td class="text-cb-text-secondary py-8 text-center" colspan="6">当前筛选条件下没有标签记录。</td>
            </tr>
            <tr v-for="tag in tagPage.list" :key="tag.id">
              <td>
                <div class="title-cell">
                  <strong>{{ tag.name }}</strong>
                  <span>颜色与排序可直接维护</span>
                </div>
              </td>
              <td>
                <span class="pill pill-neutral">{{ tagTypeMap[tag.type] || "未知类型" }}</span>
              </td>
              <td>
                <div class="color-chip">
                  <span class="tag-dot" :style="{ backgroundColor: tag.color }" />
                  <span class="mono">{{ tag.color }}</span>
                </div>
              </td>
              <td class="mono">{{ tag.sort }}</td>
              <td class="mono">#{{ tag.id }}</td>
              <td>
                <div class="inline-actions">
                  <button class="table-action-button" type="button" @click="openEditDialog(tag)">编辑</button>
                  <button class="table-action-button danger" type="button" :disabled="submitting" @click="handleDelete(tag)">
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationControls
        v-if="tagPage"
        :page="tagPage.pageNo"
        :total="tagPage.total"
        :total-pages="tagPage.totalPages"
        @change="handlePageChange"
      />
    </section>

    <div v-if="dialogOpen" class="dialog-backdrop" role="presentation" @click="dialogOpen = false">
      <section class="dialog-panel" role="dialog" aria-modal="true" @click.stop>
        <div class="panel-header">
          <div>
            <p class="section-kicker">{{ dialogMode === "create" ? "Create Tag" : "Edit Tag" }}</p>
            <h3>{{ dialogMode === "create" ? "新增标签" : "编辑标签" }}</h3>
          </div>
          <button class="header-icon-button" type="button" @click="dialogOpen = false">x</button>
        </div>

        <form class="dialog-form" @submit.prevent="handleSubmit">
          <label class="field">
            <span>标签名称</span>
            <input v-model="formValues.name" />
          </label>

          <label class="field">
            <span>标签类型</span>
            <select v-model.number="formValues.type">
              <option :value="3">主题标签</option>
              <option :value="1">技术栈</option>
              <option :value="2">学习路径</option>
            </select>
          </label>

          <div class="dialog-grid">
            <label class="field">
              <span>颜色</span>
              <input v-model="formValues.color" />
            </label>

            <label class="field">
              <span>排序权重</span>
              <input v-model.number="formValues.sort" min="0" type="number" />
            </label>
          </div>

          <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

          <div class="dialog-actions">
            <button class="panel-action-button" type="button" @click="dialogOpen = false">取消</button>
            <button class="panel-action-button primary" :disabled="submitting" type="submit">
              {{ submitting ? "提交中..." : dialogMode === "create" ? "创建标签" : "保存修改" }}
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
import { tagTypeMap } from "@/data/admin-config";
import {
  BrowserApiError,
  createTag,
  deleteTag,
  getTagList,
  getTagsPage,
  updateTag,
} from "@/services/admin-api";
import type { AdminTag, AdminTagFormValues, PaginatedResult } from "@/types/admin";

type DialogMode = "create" | "edit";

const route = useRoute();
const router = useRouter();

const initialFormState: AdminTagFormValues = {
  name: "",
  type: 3,
  color: "#0f766e",
  sort: 100,
};

const tagPage = ref<PaginatedResult<AdminTag> | null>(null);
const allTags = ref<AdminTag[]>([]);
const dialogOpen = ref(false);
const dialogMode = ref<DialogMode>("create");
const formValues = ref<AdminTagFormValues>({ ...initialFormState });
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const size = computed(() => parsePositiveInt(route.query.size, 12));
const currentType = computed(() => {
  const type = parsePositiveInt(route.query.type, 0);
  return type === 1 || type === 2 || type === 3 ? type : undefined;
});
const topicCount = computed(() => allTags.value.filter((tag) => tag.type === 3).length);
const techCount = computed(() => allTags.value.filter((tag) => tag.type === 1).length);
const pathCount = computed(() => allTags.value.filter((tag) => tag.type === 2).length);

function buildQuery(nextPage = page.value, type = currentType.value) {
  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(size.value !== 12 ? { size: String(size.value) } : {}),
    ...(type ? { type: String(type) } : {}),
  };
}

async function loadTags() {
  try {
    errorMessage.value = "";
    const [pageResult, listResult] = await Promise.all([
      getTagsPage({ page: page.value, size: size.value, type: currentType.value }),
      getTagList(),
    ]);
    tagPage.value = pageResult;
    allTags.value = listResult;
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "标签数据读取失败，请稍后重试。";
  }
}

function openCreateDialog() {
  dialogMode.value = "create";
  formValues.value = { ...initialFormState };
  message.value = "";
  errorMessage.value = "";
  dialogOpen.value = true;
}

function openEditDialog(tag: AdminTag) {
  dialogMode.value = "edit";
  formValues.value = {
    id: tag.id,
    name: tag.name,
    type: tag.type === 2 ? 2 : tag.type === 1 ? 1 : 3,
    color: tag.color,
    sort: tag.sort,
  };
  message.value = "";
  errorMessage.value = "";
  dialogOpen.value = true;
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/tags", query: buildQuery(nextPage) });
}

function handleTypeChange(event: Event) {
  const target = event.target as HTMLSelectElement;
  const nextType = target.value === "all" ? undefined : Number(target.value);
  router.push({ path: "/tags", query: buildQuery(1, nextType) });
}

async function handleSubmit() {
  if (!formValues.value.name.trim()) {
    errorMessage.value = "标签名称不能为空。";
    return;
  }

  try {
    submitting.value = true;
    errorMessage.value = "";

    if (dialogMode.value === "create") {
      await createTag({
        ...formValues.value,
        name: formValues.value.name.trim(),
      });
      message.value = "标签已创建。";
    } else {
      await updateTag({
        ...formValues.value,
        name: formValues.value.name.trim(),
      });
      message.value = "标签已更新。";
    }

    dialogOpen.value = false;
    await loadTags();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "标签操作失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(tag: AdminTag) {
  if (!window.confirm(`确认删除标签「${tag.name}」吗？`)) {
    return;
  }

  try {
    submitting.value = true;
    errorMessage.value = "";
    await deleteTag(tag.id);
    message.value = "标签已删除。";

    if (tagPage.value && tagPage.value.list.length === 1 && page.value > 1) {
      router.push({ path: "/tags", query: buildQuery(page.value - 1) });
      return;
    }

    await loadTags();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "删除失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

watch([page, size, currentType], loadTags, { immediate: true });
</script>
