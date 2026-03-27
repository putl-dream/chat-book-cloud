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
        <p class="metric-label">内容检索 API</p>
        <h2>待补</h2>
        <p class="metric-detail">管理员视角仍缺少全站分页和筛选接口</p>
      </article>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <p class="section-kicker">Content Table</p>
          <h3>内容资产视图</h3>
        </div>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>标题</th>
              <th>作者</th>
              <th>分类 / 类型</th>
              <th>状态</th>
              <th>互动数据</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="article in articles" :key="article.id">
              <td>
                <div class="title-cell">
                  <strong>{{ article.title }}</strong>
                  <span>{{ article.summary }}</span>
                </div>
              </td>
              <td>
                <div class="user-cell">
                  <strong>{{ article.userName }}</strong>
                  <span class="mono">UID {{ article.userId }}</span>
                </div>
              </td>
              <td>{{ articleCategoryMap[article.category] }} / {{ contentTypeMap[article.contentType] }}</td>
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
                  {{ articleStatusMap[article.status] }}
                </span>
              </td>
              <td class="mono">
                V {{ article.viewCount }} / C {{ article.commentCount }} / P {{ article.praiseCount }}
              </td>
              <td class="mono">{{ article.createdAt }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { articleCategoryMap, articleStatusMap, contentTypeMap } from "@/data/admin-config";
import { getContentArticles } from "@/services/admin-api";
import type { AdminArticle } from "@/types/admin";

const articles = ref<AdminArticle[]>([]);

const publishedCount = computed(() => articles.value.filter((article) => article.status === 2).length);
const draftCount = computed(() => articles.value.filter((article) => article.status === 0).length);

onMounted(async () => {
  articles.value = await getContentArticles();
});
</script>
