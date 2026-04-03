import { computed, readonly, ref } from "vue";

export type ConfirmDialogTone = "primary" | "warning" | "danger";

export type ConfirmDialogOptions = {
  title: string;
  description?: string;
  note?: string;
  confirmText?: string;
  cancelText?: string;
  badge?: string;
  tone?: ConfirmDialogTone;
};

type ConfirmDialogRequest = {
  id: number;
  title: string;
  description: string;
  note: string;
  confirmText: string;
  cancelText: string;
  badge: string;
  tone: ConfirmDialogTone;
  triggerElement: HTMLElement | null;
  resolve: (value: boolean) => void;
};

const activeRequest = ref<ConfirmDialogRequest | null>(null);
const pendingQueue: ConfirmDialogRequest[] = [];

let requestSeed = 0;

function getDefaultBadge(tone: ConfirmDialogTone) {
  if (tone === "danger") return "Sensitive Action";
  if (tone === "warning") return "Please Confirm";
  return "Confirm Action";
}

function getActiveElement() {
  if (typeof document === "undefined" || !(document.activeElement instanceof HTMLElement)) {
    return null;
  }

  return document.activeElement;
}

function normalizeRequest(
  options: ConfirmDialogOptions,
  resolve: (value: boolean) => void
): ConfirmDialogRequest {
  const tone = options.tone ?? "primary";

  return {
    id: ++requestSeed,
    title: options.title,
    description: options.description ?? "",
    note: options.note ?? "",
    confirmText: options.confirmText ?? "确认",
    cancelText: options.cancelText ?? "取消",
    badge: options.badge ?? getDefaultBadge(tone),
    tone,
    triggerElement: getActiveElement(),
    resolve,
  };
}

function showNextRequest() {
  if (activeRequest.value || pendingQueue.length === 0) return;
  activeRequest.value = pendingQueue.shift() ?? null;
}

function restoreFocus(element: HTMLElement | null) {
  if (!element || !element.isConnected) return;

  try {
    element.focus({ preventScroll: true });
  } catch {
    element.focus();
  }
}

function settleActiveRequest(value: boolean) {
  const currentRequest = activeRequest.value;
  if (!currentRequest) return;

  activeRequest.value = pendingQueue.shift() ?? null;
  currentRequest.resolve(value);
  restoreFocus(currentRequest.triggerElement);
}

export function confirmAction(options: ConfirmDialogOptions) {
  return new Promise<boolean>((resolve) => {
    pendingQueue.push(normalizeRequest(options, resolve));
    showNextRequest();
  });
}

export function useConfirmDialog() {
  return {
    activeRequest: readonly(activeRequest),
    isOpen: computed(() => activeRequest.value !== null),
    acceptConfirm: () => settleActiveRequest(true),
    cancelConfirm: () => settleActiveRequest(false),
  };
}
