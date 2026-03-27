<template>
  <section :class="['state-panel', toneClass]">
    <div class="state-panel-icon">
      <span class="text-lg font-semibold">{{ toneSymbol }}</span>
    </div>
    <div class="space-y-2">
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <RouterLink v-if="actionLabel && actionHref" :to="actionHref" class="state-panel-link">
        {{ actionLabel }}
      </RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { RouterLink } from "vue-router";

const props = withDefaults(
  defineProps<{
    title: string;
    description: string;
    tone?: "neutral" | "danger" | "warning";
    actionLabel?: string;
    actionHref?: string;
  }>(),
  {
    tone: "neutral",
  }
);

const toneClass = computed(() => {
  if (props.tone === "warning") return "state-panel-warning";
  if (props.tone === "danger") return "state-panel-danger";
  return "state-panel-neutral";
});

const toneSymbol = computed(() => {
  if (props.tone === "warning") return "!";
  if (props.tone === "danger") return "x";
  return "i";
});
</script>
