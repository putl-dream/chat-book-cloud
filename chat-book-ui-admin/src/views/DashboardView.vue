<template>
  <section class="page-shell">
    <template v-if="snapshot">
      <div class="page-hero">
        <p class="eyebrow">Platform Overview</p>
        <h1>围绕当前业务能力搭建后台主控台</h1>
        <p class="hero-copy">
          当前项目覆盖博客内容、创作台、评论互动、社交关系、聊天与统一鉴权。后台的重点不是重复前台功能，
          而是为这些能力补上运营、审核、治理和系统接入的控制面。
        </p>
      </div>

      <div class="metric-grid">
        <article v-for="metric in snapshot.metrics" :key="metric.label" class="metric-card">
          <p class="metric-label">{{ metric.label }}</p>
          <h2>{{ metric.value }}</h2>
          <p class="metric-detail">{{ metric.detail }}</p>
          <span class="metric-trend">{{ metric.trend }}</span>
        </article>
      </div>

      <div class="content-grid two-column">
        <section class="panel">
          <div class="panel-header">
            <div>
              <p class="section-kicker">运营视角</p>
              <h3>当前后台建设判断</h3>
            </div>
          </div>
          <div class="stack-list">
            <article v-for="item in snapshot.highlights" :key="item.title" class="stack-item">
              <div class="stack-title-row">
                <h4>{{ item.title }}</h4>
                <span :class="`pill pill-${getStatusTone(item.status)}`">
                  {{ item.status === "stable" ? "已具备" : item.status === "partial" ? "部分具备" : "待补齐" }}
                </span>
              </div>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <div>
              <p class="section-kicker">模块地图</p>
              <h3>当前已经落地的后台栏目</h3>
            </div>
          </div>
          <div class="stack-list">
            <article
              v-for="item in adminNavigation.flatMap((group) => group.items)"
              :key="item.href"
              class="stack-item"
            >
              <div class="stack-title-row">
                <h4>{{ item.label }}</h4>
                <span class="pill pill-neutral">{{ item.href }}</span>
              </div>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </section>
      </div>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Service Matrix</p>
            <h3>微服务接入状态与后台职责</h3>
          </div>
        </div>
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>服务</th>
                <th>后台职责</th>
                <th>当前接口</th>
                <th>缺口</th>
                <th>优先级</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="service in snapshot.services" :key="service.service">
                <td class="mono">{{ service.service }}</td>
                <td>{{ service.responsibility }}</td>
                <td class="mono">{{ service.currentApi }}</td>
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
      title="平台概览暂时不可用"
      :description="errorMessage"
      tone="warning"
    />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { adminNavigation, getPriorityTone, getStatusTone } from "@/data/admin-config";
import { BrowserApiError, getDashboardSnapshot } from "@/services/admin-api";
import type { DashboardSnapshot } from "@/types/admin";
import RequestStatePanel from "@/components/shared/RequestStatePanel.vue";

const snapshot = ref<DashboardSnapshot | null>(null);
const errorMessage = ref("");

onMounted(async () => {
  try {
    snapshot.value = await getDashboardSnapshot();
  } catch (error) {
    errorMessage.value =
      error instanceof BrowserApiError
        ? error.message
        : "概览页读取失败，请确认网关地址和管理员接口是否可访问。";
  }
});
</script>
