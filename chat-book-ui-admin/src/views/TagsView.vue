<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">System Tag Governance</p>
      <h1>系统标签治理与推荐语义</h1>
      <p class="hero-copy">
        系统标签只面向后台治理、推荐和分发。作者标签前台展示，系统标签在这里按维度、编码和状态进行维护。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">系统标签总量</p>
        <h2>{{ systemTagPage?.total ?? 0 }}</h2>
        <p class="metric-detail">真实数据来自 /system-tag/page</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">活跃标签</p>
        <h2>{{ activeCount }}</h2>
        <p class="metric-detail">当前全量列表中的 ACTIVE 状态数量</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">标签维度</p>
        <h2>{{ dimensionCount }}</h2>
        <p class="metric-detail">topic / stack / scene / intent / audience</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">当前分页</p>
        <h2>{{ systemTagPage?.pageNo ?? 1 }}/{{ systemTagPage?.totalPages ?? 1 }}</h2>
        <p class="metric-detail">支持按维度与状态筛选</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Operations</p>
          <h3>系统标签筛选与 CRUD</h3>
        </div>
        <div class="inline-actions">
          <button class="panel-action-button" type="button" @click="loadSystemTags">刷新</button>
          <button class="panel-action-button primary" type="button" @click="openCreateDialog">新增系统标签</button>
        </div>
      </div>

      <div class="toolbar-grid">
        <label class="field">
          <span>标签维度</span>
          <select :value="currentDimension || 'all'" @change="handleDimensionChange">
            <option value="all">全部维度</option>
            <option v-for="dimension in dimensionOptions" :key="dimension" :value="dimension">{{ dimension }}</option>
          </select>
        </label>
        <label class="field">
          <span>状态</span>
          <select :value="currentStatus || 'all'" @change="handleStatusChange">
            <option value="all">全部状态</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="DISABLED">DISABLED</option>
          </select>
        </label>
        <label class="field">
          <span>关键词</span>
          <input :value="currentKeyword" placeholder="搜索名称 / code / 描述" @change="handleKeywordChange" />
        </label>
      </div>

      <p v-if="message" class="form-message success">{{ message }}</p>
      <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

      <div v-if="systemTagPage" class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>名称</th>
              <th>编码</th>
              <th>维度</th>
              <th>状态</th>
              <th>排序 / 权重</th>
              <th>文章数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="systemTagPage.list.length === 0">
              <td class="text-cb-text-secondary py-8 text-center" colspan="7">当前筛选条件下没有系统标签记录。</td>
            </tr>
            <tr v-for="tag in systemTagPage.list" :key="tag.id">
              <td>
                <div class="title-cell">
                  <strong>{{ tag.name }}</strong>
                  <span>{{ tag.description || "未填写描述" }}</span>
                </div>
              </td>
              <td class="mono">{{ tag.code }}</td>
              <td><span class="pill pill-neutral">{{ tag.dimension }}</span></td>
              <td>
                <span :class="['pill', tag.status === 'ACTIVE' ? 'pill-safe' : 'pill-danger']">
                  {{ tag.status }}
                </span>
              </td>
              <td class="mono">{{ tag.sort }} / {{ tag.recommendWeight }}</td>
              <td class="mono">{{ tag.articleCount ?? 0 }}</td>
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
        v-if="systemTagPage"
        :page="systemTagPage.pageNo"
        :total="systemTagPage.total"
        :total-pages="systemTagPage.totalPages"
        @change="handlePageChange"
      />
    </section>

    <div v-if="dialogOpen" class="dialog-backdrop" role="presentation" @click="dialogOpen = false">
      <section class="dialog-panel" role="dialog" aria-modal="true" @click.stop>
        <div class="panel-header">
          <div>
            <p class="section-kicker">{{ dialogMode === "create" ? "Create System Tag" : "Edit System Tag" }}</p>
            <h3>{{ dialogMode === "create" ? "新增系统标签" : "编辑系统标签" }}</h3>
          </div>
          <button class="header-icon-button" type="button" @click="dialogOpen = false">x</button>
        </div>

        <form class="dialog-form" @submit.prevent="handleSubmit">
          <label class="field">
            <span>标签名称</span>
            <input v-model="formValues.name" />
          </label>

          <div class="dialog-grid">
            <label class="field">
              <span>稳定编码</span>
              <input v-model="formValues.code" />
            </label>
            <label class="field">
              <span>维度</span>
              <select v-model="formValues.dimension">
                <option v-for="dimension in dimensionOptions" :key="dimension" :value="dimension">{{ dimension }}</option>
              </select>
            </label>
          </div>

          <label class="field">
            <span>描述</span>
            <textarea v-model="formValues.description" rows="3" />
          </label>

          <div class="dialog-grid">
            <label class="field">
              <span>状态</span>
              <select v-model="formValues.status">
                <option value="ACTIVE">ACTIVE</option>
                <option value="DISABLED">DISABLED</option>
              </select>
            </label>
            <label class="field">
              <span>排序</span>
              <input v-model.number="formValues.sort" min="0" type="number" />
            </label>
          </div>

          <label class="field">
            <span>推荐权重</span>
            <input v-model.number="formValues.recommendWeight" min="0" step="0.1" type="number" />
          </label>

          <p v-if="errorMessage" class="form-message error">{{ errorMessage }}</p>

          <div class="dialog-actions">
            <button class="panel-action-button" type="button" @click="dialogOpen = false">取消</button>
            <button class="panel-action-button primary" :disabled="submitting" type="submit">
              {{ submitting ? "提交中..." : dialogMode === "create" ? "创建系统标签" : "保存修改" }}
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
import { confirmAction } from "@/composables/useConfirmDialog";
import {
  BrowserApiError,
  createSystemTag,
  deleteSystemTag,
  getSystemTagList,
  getSystemTagsPage,
  updateSystemTag,
} from "@/services/admin-api";
import type { AdminSystemTag, AdminSystemTagFormValues, PaginatedResult } from "@/types/admin";

type DialogMode = "create" | "edit";

const route = useRoute();
const router = useRouter();

const dimensionOptions = ["topic", "stack", "scene", "intent", "audience"];

const initialFormState: AdminSystemTagFormValues = {
  name: "",
  code: "",
  dimension: "topic",
  description: "",
  status: "ACTIVE",
  sort: 100,
  recommendWeight: 1,
};

const systemTagPage = ref<PaginatedResult<AdminSystemTag> | null>(null);
const allSystemTags = ref<AdminSystemTag[]>([]);
const dialogOpen = ref(false);
const dialogMode = ref<DialogMode>("create");
const formValues = ref<AdminSystemTagFormValues>({ ...initialFormState });
const submitting = ref(false);
const message = ref("");
const errorMessage = ref("");

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

const page = computed(() => parsePositiveInt(route.query.page, 1));
const size = computed(() => parsePositiveInt(route.query.size, 12));
const currentDimension = computed(() => (typeof route.query.dimension === "string" ? route.query.dimension : undefined));
const currentStatus = computed(() => (typeof route.query.status === "string" ? route.query.status : undefined));
const currentKeyword = computed(() => (typeof route.query.keyword === "string" ? route.query.keyword : ""));
const activeCount = computed(() => allSystemTags.value.filter((tag) => tag.status === "ACTIVE").length);
const dimensionCount = computed(() => new Set(allSystemTags.value.map((tag) => tag.dimension)).size);

function buildQuery(nextPage = page.value, overrides?: Partial<Record<"dimension" | "status" | "keyword", string | undefined>>) {
  const dimension = overrides?.dimension ?? currentDimension.value;
  const status = overrides?.status ?? currentStatus.value;
  const keyword = overrides?.keyword ?? currentKeyword.value;
  return {
    ...(nextPage > 1 ? { page: String(nextPage) } : {}),
    ...(size.value !== 12 ? { size: String(size.value) } : {}),
    ...(dimension ? { dimension } : {}),
    ...(status ? { status } : {}),
    ...(keyword ? { keyword } : {}),
  };
}

async function loadSystemTags() {
  try {
    errorMessage.value = "";
    const [pageResult, listResult] = await Promise.all([
      getSystemTagsPage({
        page: page.value,
        size: size.value,
        keyword: currentKeyword.value || null,
        dimension: currentDimension.value || null,
        status: currentStatus.value || null,
      }),
      getSystemTagList(),
    ]);
    systemTagPage.value = pageResult;
    allSystemTags.value = listResult;
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "系统标签数据读取失败，请稍后重试。";
  }
}

function openCreateDialog() {
  dialogMode.value = "create";
  formValues.value = { ...initialFormState };
  message.value = "";
  errorMessage.value = "";
  dialogOpen.value = true;
}

function openEditDialog(tag: AdminSystemTag) {
  dialogMode.value = "edit";
  formValues.value = {
    id: tag.id,
    name: tag.name,
    code: tag.code,
    dimension: tag.dimension,
    description: tag.description || "",
    status: tag.status,
    sort: tag.sort,
    recommendWeight: tag.recommendWeight,
  };
  message.value = "";
  errorMessage.value = "";
  dialogOpen.value = true;
}

function handlePageChange(nextPage: number) {
  router.push({ path: "/tags", query: buildQuery(nextPage) });
}

function handleDimensionChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  router.push({ path: "/tags", query: buildQuery(1, { dimension: value === "all" ? undefined : value }) });
}

function handleStatusChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  router.push({ path: "/tags", query: buildQuery(1, { status: value === "all" ? undefined : value }) });
}

function handleKeywordChange(event: Event) {
  const value = (event.target as HTMLInputElement).value.trim();
  router.push({ path: "/tags", query: buildQuery(1, { keyword: value || undefined }) });
}

async function handleSubmit() {
  if (!formValues.value.name.trim()) {
    errorMessage.value = "系统标签名称不能为空。";
    return;
  }
  if (!formValues.value.code.trim()) {
    errorMessage.value = "系统标签编码不能为空。";
    return;
  }

  try {
    submitting.value = true;
    errorMessage.value = "";

    const payload = {
      ...formValues.value,
      name: formValues.value.name.trim(),
      code: formValues.value.code.trim(),
      dimension: formValues.value.dimension.trim(),
      description: formValues.value.description?.trim() || "",
    };

    if (dialogMode.value === "create") {
      await createSystemTag(payload);
      message.value = "系统标签已创建。";
    } else {
      await updateSystemTag(payload);
      message.value = "系统标签已更新。";
    }

    dialogOpen.value = false;
    await loadSystemTags();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "系统标签操作失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(tag: AdminSystemTag) {
  const confirmed = await confirmAction({
    title: `删除系统标签「${tag.name}」`,
    description: "删除后会同时影响映射关系、推荐语义和后台治理视图。",
    note: "建议先确认没有正在使用该系统标签的映射配置或人工修正记录。",
    confirmText: "确认删除",
    badge: "System Tag",
    tone: "danger",
  });

  if (!confirmed) {
    return;
  }

  try {
    submitting.value = true;
    errorMessage.value = "";
    await deleteSystemTag(tag.id);
    message.value = "系统标签已删除。";

    if (systemTagPage.value && systemTagPage.value.list.length === 1 && page.value > 1) {
      router.push({ path: "/tags", query: buildQuery(page.value - 1) });
      return;
    }

    await loadSystemTags();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError ? error.message : "删除失败，请稍后重试。";
  } finally {
    submitting.value = false;
  }
}

watch([page, size, currentDimension, currentStatus, currentKeyword], loadSystemTags, { immediate: true });
</script>
