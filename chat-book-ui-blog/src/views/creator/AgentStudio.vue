<template>
    <div 
        class="workspace-layout u-animate-fade-in" 
        :class="{ 'is-dragging': isDragging }"
        @mousemove="onDrag"
        @mouseup="stopDrag"
        @mouseleave="stopDrag"
    >
        <CreativeHeader class="workspace-header">
            <template #actions>
                <div class="header-actions">
                    <span class="status-badge" v-if="store.sessionStatusLabel">
                        {{ store.sessionStatusLabel }}
                    </span>
                    <el-button 
                        class="header-btn primary" 
                        :disabled="!store.hasDraft" 
                        @click="store.importDraftToEditor"
                        round
                    >
                        <el-icon><DocumentChecked /></el-icon> 导入编辑器
                    </el-button>
                </div>
            </template>
        </CreativeHeader>

        <div class="workspace-body" ref="workspaceBodyRef">
            <SessionSidebar />
            <DraftCanvas />
            
            <div 
                v-show="!isMobile"
                class="layout-splitter" 
                :class="{ 'is-active': isDragging }" 
                @mousedown.prevent="startDrag"
            ></div>

            <ChatController :style="chatControllerStyle" />
        </div>
    </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, watch, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import CreativeHeader from '@/views/creator/components/CreativeHeader.vue';
import SessionSidebar from '@/views/creator/components/agent/SessionSidebar.vue';
import DraftCanvas from '@/views/creator/components/agent/DraftCanvas.vue';
import ChatController from '@/views/creator/components/agent/ChatController.vue';
import { DocumentChecked } from '@element-plus/icons-vue';

const route = useRoute();
const store = useAgentStudioStore();

// --- Dragging Logic for Splitter ---
const workspaceBodyRef = ref(null);
const isDragging = ref(false);
const chatWidth = ref(360);
const isMobile = ref(false);

const checkViewport = () => {
    isMobile.value = window.innerWidth <= 900;
};

const chatControllerStyle = computed(() => {
    // If mobile, allow responsive CSS to take over
    if (isMobile.value) {
        return {};
    }
    return { width: `${chatWidth.value}px` };
});

const startDrag = () => {
    isDragging.value = true;
    document.body.style.userSelect = 'none'; // Prevent text selection
};

const onDrag = (e) => {
    if (!isDragging.value || !workspaceBodyRef.value) return;
    
    const containerRect = workspaceBodyRef.value.getBoundingClientRect();
    // Calculate new width from the right edge
    let newWidth = containerRect.right - e.clientX;
    
    // Constraints: min 300px, max 50% of the workspace area
    const maxWidth = containerRect.width * 0.6;
    const minWidth = 300;
    
    if (newWidth < minWidth) newWidth = minWidth;
    if (newWidth > maxWidth) newWidth = maxWidth;
    
    chatWidth.value = newWidth;
};

const stopDrag = () => {
    if (isDragging.value) {
        isDragging.value = false;
        document.body.style.userSelect = '';
    }
};

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
    checkViewport();
    window.addEventListener('resize', checkViewport);
});

onBeforeUnmount(() => {
    store.disconnectWebSocket();
    window.removeEventListener('resize', checkViewport);
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
    min-height: 0; /* Important for scrollable children inside flex column */
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
.layout-splitter {
    width: 8px;
    margin-left: -4px;
    margin-right: -4px;
    background: transparent;
    cursor: col-resize;
    position: relative;
    z-index: 20;
    transition: background-color 0.2s;
    flex-shrink: 0;
}

.layout-splitter:hover,
.layout-splitter.is-active {
    background: rgba(209, 96, 61, 0.15);
}

.layout-splitter::after {
    content: '';
    position: absolute;
    left: 3px;
    top: 50%;
    transform: translateY(-50%);
    width: 2px;
    height: 24px;
    background: rgba(22, 50, 79, 0.2);
    border-radius: 2px;
}

.workspace-layout.is-dragging {
    cursor: col-resize;
}

.workspace-layout.is-dragging .layout-splitter {
    background: rgba(209, 96, 61, 0.25);
}

@media (max-width: 900px) {
    .workspace-body {
        flex-direction: column;
        overflow-y: auto;
    }
}
</style>
