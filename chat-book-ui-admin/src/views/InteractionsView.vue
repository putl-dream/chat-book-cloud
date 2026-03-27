<template>
  <section class="page-shell">
    <div class="page-hero compact">
      <p class="eyebrow">Interaction Governance</p>
      <h1>评论、通知与互动巡检</h1>
      <p class="hero-copy">
        interaction-service 已承载点赞、收藏、评论、浏览和通知，但后台仍缺少全局视角。
        当前页面先完成监控入口骨架，便于后续扩展评论治理、异常行为识别和告警归因。
      </p>
    </div>

    <div class="metric-grid compact-grid">
      <article class="metric-card">
        <p class="metric-label">互动事件样本</p>
        <h2>{{ events.length }}</h2>
        <p class="metric-detail">当前以 mock 数据承载后台聚合入口</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">评论治理</p>
        <h2>{{ commentCount }}</h2>
        <p class="metric-detail">后续需要管理员评论分页和处置接口</p>
      </article>
      <article class="metric-card">
        <p class="metric-label">全局通知</p>
        <h2>待补</h2>
        <p class="metric-detail">当前 /interaction/foot/getNotifications 只面向登录用户</p>
      </article>
    </div>

    <div class="content-grid two-column">
      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Event Feed</p>
            <h3>互动事件流</h3>
          </div>
        </div>
        <div class="timeline">
          <article v-for="event in events" :key="event.id" class="timeline-item">
            <div class="timeline-dot" />
            <div class="timeline-body">
              <div class="stack-title-row">
                <h4>{{ event.senderName }} {{ actionTypeMap[event.actionType] }}了《{{ event.articleTitle }}》</h4>
                <span class="pill pill-neutral">{{ event.scope }}</span>
              </div>
              <p>{{ event.summary }}</p>
              <span class="mono">{{ event.createdAt }}</span>
            </div>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <p class="section-kicker">Missing Admin APIs</p>
            <h3>后台互动治理待补接口</h3>
          </div>
        </div>
        <div class="stack-list">
          <article class="stack-item">
            <div class="stack-title-row">
              <h4>评论分页与处置</h4>
              <span class="pill pill-danger">高优先级</span>
            </div>
            <p>需要支持全站评论分页、删除、屏蔽、恢复和敏感词命中记录。</p>
          </article>
          <article class="stack-item">
            <div class="stack-title-row">
              <h4>异常行为聚合</h4>
              <span class="pill pill-danger">高优先级</span>
            </div>
            <p>需要识别刷赞、刷浏览、短时批量收藏等异常模式。</p>
          </article>
          <article class="stack-item">
            <div class="stack-title-row">
              <h4>运营告警中心</h4>
              <span class="pill pill-warn">中优先级</span>
            </div>
            <p>建议把互动、文章审核和用户风控事件聚合到统一告警中心。</p>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { actionTypeMap } from "@/data/admin-config";
import { getInteractionEvents } from "@/services/admin-api";
import type { InteractionEvent } from "@/types/admin";

const events = ref<InteractionEvent[]>([]);
const commentCount = computed(() => events.value.filter((event) => event.actionType === "COMMENT").length);

onMounted(async () => {
  events.value = await getInteractionEvents();
});
</script>
