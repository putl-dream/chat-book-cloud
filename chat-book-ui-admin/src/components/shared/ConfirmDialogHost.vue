<template>
  <Teleport to="body">
    <div
      v-if="activeRequest"
      class="dialog-backdrop confirm-dialog-backdrop"
      role="presentation"
      @click="cancelConfirm"
    >
      <section
        class="dialog-panel confirm-dialog-panel"
        :data-tone="activeRequest.tone"
        role="alertdialog"
        aria-modal="true"
        :aria-labelledby="titleId"
        :aria-describedby="descriptionId"
        @click.stop
      >
        <div class="confirm-dialog-head">
          <div class="confirm-dialog-icon" :data-tone="activeRequest.tone" aria-hidden="true">
            {{ toneMark }}
          </div>
          <div class="confirm-dialog-copy">
            <p class="section-kicker">{{ activeRequest.badge }}</p>
            <h3 :id="titleId">{{ activeRequest.title }}</h3>
            <p v-if="activeRequest.description" :id="descriptionId" class="confirm-dialog-description">
              {{ activeRequest.description }}
            </p>
          </div>
        </div>

        <p v-if="activeRequest.note" class="confirm-dialog-note" :data-tone="activeRequest.tone">
          {{ activeRequest.note }}
        </p>

        <div class="dialog-actions confirm-dialog-actions">
          <button
            ref="cancelButtonRef"
            class="panel-action-button"
            type="button"
            @click="cancelConfirm"
          >
            {{ activeRequest.cancelText }}
          </button>
          <button :class="confirmButtonClass" type="button" @click="acceptConfirm">
            {{ activeRequest.confirmText }}
          </button>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";
import { useConfirmDialog } from "@/composables/useConfirmDialog";

const { activeRequest, acceptConfirm, cancelConfirm } = useConfirmDialog();
const cancelButtonRef = ref<HTMLButtonElement | null>(null);

let previousBodyOverflow = "";

const titleId = computed(() =>
  activeRequest.value ? `confirm-dialog-title-${activeRequest.value.id}` : undefined
);
const descriptionId = computed(() => {
  if (!activeRequest.value?.description) return undefined;
  return `confirm-dialog-description-${activeRequest.value.id}`;
});
const toneMark = computed(() => {
  if (!activeRequest.value) return ">";
  if (activeRequest.value.tone === "danger") return "!";
  if (activeRequest.value.tone === "warning") return "?";
  return ">";
});
const confirmButtonClass = computed(() => {
  if (!activeRequest.value) return ["panel-action-button", "primary"];

  return activeRequest.value.tone === "danger"
    ? ["panel-action-button", "danger", "is-solid"]
    : ["panel-action-button", "primary"];
});

function handleWindowKeydown(event: KeyboardEvent) {
  if (!activeRequest.value) return;

  if (event.key === "Escape") {
    event.preventDefault();
    cancelConfirm();
  }
}

watch(
  activeRequest,
  async (request, previousRequest) => {
    if (typeof window === "undefined" || typeof document === "undefined") return;

    if (request && !previousRequest) {
      previousBodyOverflow = document.body.style.overflow;
      document.body.style.overflow = "hidden";
      window.addEventListener("keydown", handleWindowKeydown);
    }

    if (!request && previousRequest) {
      document.body.style.overflow = previousBodyOverflow;
      window.removeEventListener("keydown", handleWindowKeydown);
      return;
    }

    if (request) {
      await nextTick();
      cancelButtonRef.value?.focus({ preventScroll: true });
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (typeof window !== "undefined") {
    window.removeEventListener("keydown", handleWindowKeydown);
  }

  if (typeof document !== "undefined") {
    document.body.style.overflow = previousBodyOverflow;
  }
});
</script>
