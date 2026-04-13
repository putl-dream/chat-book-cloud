<template>
    <div class="workspace-layout u-animate-fade-in">
        <CreativeHeader class="workspace-header">
            <template #actions>
                <div class="header-actions">
                    <span class="status-badge" v-if="store.sessionStatusLabel">
                        {{ store.sessionStatusLabel }}
                    </span>
                    <el-button class="header-btn primary" @click="store.openFreshSession" round>
                        <el-icon><EditPen /></el-icon> 新建对话
                    </el-button>
                </div>
            </template>
        </CreativeHeader>

        <div class="workspace-body">
            <SessionSidebar />
            <ChatController />
        </div>
    </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import CreativeHeader from '@/views/creator/components/CreativeHeader.vue';
import SessionSidebar from '@/views/creator/components/agent/SessionSidebar.vue';
import ChatController from '@/views/creator/components/agent/ChatController.vue';
import { EditPen } from '@element-plus/icons-vue';

const route = useRoute();
const store = useAgentStudioStore();

// --- Hydrate Logic ---
// Watch Route for session ID changes
watch(
    () => route.params.sessionId,
    async (nextSessionId) => {
        const parsedId = nextSessionId ? Number(nextSessionId) : null;
        if (!parsedId) {
            store.resetStudioState();
            return;
        }
        if (store.sessionId === parsedId) {
            return;
        }
        await store.hydrateSession(parsedId);
    },
    { immediate: true }
);

onMounted(() => {
    store.connectWebSocket();
});

onBeforeUnmount(() => {
    store.disconnectWebSocket();
});
</script>

<style scoped>
.workspace-layout {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: #fff;
    z-index: 100;
}

.workspace-header {
    flex-shrink: 0;
    position: relative !important;
    border-bottom: 1px solid rgba(22, 50, 79, 0.08);
}

.workspace-body {
    flex: 1;
    display: flex;
    flex-direction: row;
    min-height: 0;
    background: #F9FAFB;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.status-badge {
    padding: 6px 14px;
    background: rgba(22, 50, 79, 0.04);
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
    color: rgba(19, 39, 63, 0.6);
    letter-spacing: 0.02em;
}

.header-btn.primary {
    background: #13273f;
    border: none;
    color: #fff;
    padding: 0 20px;
}

.header-btn.primary:not(:disabled):hover {
    background: #1e3a5f;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(19, 39, 63, 0.15);
}

.header-btn.primary:disabled {
    background: rgba(22, 50, 79, 0.1);
    color: rgba(255, 255, 255, 0.8);
}

/* Splitter Styling */
@media (max-width: 900px) {
    .workspace-body {
        flex-direction: column;
        overflow-y: auto;
    }
}
</style>
