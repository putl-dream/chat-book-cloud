<template>
  <RouterView />
  <ConfirmDialogHost />
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import ConfirmDialogHost from "@/components/shared/ConfirmDialogHost.vue";
</script>
