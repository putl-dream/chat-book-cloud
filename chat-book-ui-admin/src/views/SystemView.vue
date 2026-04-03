<template>
  <section class="page-shell">
    <template v-if="snapshot">
      <div class="page-hero compact">
        <p class="eyebrow">System Readiness</p>
        <h1>后台接入状态与剩余改造清单</h1>
        <p class="hero-copy">
          新版管理端已经完成路由、会话、主题、数据适配层和页面职责拆分。当前重点转向剩余能力补齐与信息收口。
        </p>
      </div>

      <div class="content-grid two-column">
        <section class="panel">
          <div class="panel-header">
            <div>
              <p class="section-kicker">Launch Checklist</p>
              <h3>当前仍待推进的事项</h3>
            </div>
          </div>
          <div class="stack-list">
            <article v-for="item in launchChecklist" :key="item" class="stack-item">
              <div class="stack-title-row">
                <h4>{{ item }}</h4>
                <span class="pill pill-neutral">Todo</span>
              </div>
            </article>
          </div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <div>
              <p class="section-kicker">Architecture</p>
              <h3>当前前端分层</h3>
            </div>
          </div>
          <div class="stack-list">
            <article class="stack-item">
              <div class="stack-title-row">
                <h4>Vue Router 页面层</h4>
                <span class="pill pill-safe">已完成</span>
              </div>
              <p>按后台职责拆分为概览、用户、审核、内容、标签、互动、系统和主题页面。</p>
            </article>
            <article class="stack-item">
              <div class="stack-title-row">
                <h4>Pinia 会话层</h4>
                <span class="pill pill-safe">已完成</span>
              </div>
              <p>通过本地 token 和接口校验实现路由守卫，不再依赖 Next middleware。</p>
            </article>
            <article class="stack-item">
              <div class="stack-title-row">
                <h4>浏览器 API 层</h4>
                <span class="pill pill-safe">已完成</span>
              </div>
              <p>把原来服务端 admin-api 改成浏览器请求，实现更快的本地开发循环。</p>
            </article>
          </div>
        </section>
      </div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Service Priority</p>
            <h3>推荐的后端补口顺序</h3>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>服务</th>
                <th>应优先补齐的后台接口</th>
                <th>优先级</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="service in snapshot.services" :key="service.service">
                <td class="mono">{{ service.service }}</td>
                <td>{{ service.backendGap }}</td>
                <td>
                  <span :class="`pill pill-${getPriorityTone(service.priority)}`">
                    {{ service.priority === "high" ? "高" : service.priority === "medium" ? "中" : "低" }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>

    <RequestStatePanel
      v-else-if="errorMessage"
      title="系统接入页暂时不可用"
      :description="errorMessage"
      tone="warning"
    />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";
import { getPriorityTone } from "@/data/admin-config";
import { BrowserApiError, getDashboardSnapshot } from "@/services/admin-api";
import type { DashboardSnapshot } from "@/types/admin";

const launchChecklist = [
  "为文章审核补正文详情与审核历史查询能力",
  "把评论治理扩展到通知中心、异常行为监控和统一告警",
  "补齐标签名称搜索、关联文章统计和标签审计",
  "继续细化用户治理的批量能力与更丰富的审计筛选",
  "统一剩余页面的筛选、分页和空状态体验",
];

const snapshot = ref<DashboardSnapshot | null>(null);
const errorMessage = ref("");

onMounted(async () => {
  try {
    snapshot.value = await getDashboardSnapshot();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "系统接入页读取失败，请确认网关地址和管理员接口是否可访问。";
  }
});
</script>
