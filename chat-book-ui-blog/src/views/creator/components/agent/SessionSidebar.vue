<template>
    <div 
        class="session-sidebar"
        :class="{ 'is-collapsed': isCollapsed }"
    >
        <div class="sidebar-header">
            <h3 v-if="!isCollapsed" class="sidebar-title">创作历史</h3>
            <el-button 
                class="collapse-btn" 
                circle 
                text
                @click="toggleCollapse"
            >
                <el-icon><Fold v-if="!isCollapsed" /><Expand v-else /></el-icon>
            </el-button>
        </div>

        <div class="sidebar-body" v-if="!isCollapsed">
            <el-button 
                class="new-session-btn" 
                type="primary" 
                round 
                @click="handleNewSession"
            >
                <el-icon><Plus /></el-icon>
                <span>新灵感对话</span>
            </el-button>

            <!-- Mocked history list for now, we will map actual api data when available -->
            <div class="session-list custom-scrollbar">
                <!-- If there is an active session, display it here -->
                <div 
                    v-if="store.sessionId" 
                    class="session-item is-active"
                >
                    <el-icon class="session-icon"><ChatDotRound /></el-icon>
                    <div class="session-info">
                        <span class="session-name" :title="store.sessionTitle">{{ store.sessionTitle }}</span>
                        <span class="session-meta">{{ store.currentSceneLabel }}</span>
                    </div>
                </div>

                <!-- Empty space hint if there's no list from API -->
                <div class="empty-hint" v-if="!store.sessionId">
                    没有任何会话。
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { Fold, Expand, Plus, ChatDotRound } from '@element-plus/icons-vue';

const store = useAgentStudioStore();
const isCollapsed = ref(false);

const toggleCollapse = () => {
    isCollapsed.value = !isCollapsed.value;
};

const handleNewSession = async () => {
    await store.openFreshSession();
};
</script>

<style scoped>
.session-sidebar {
    width: 260px;
    height: 100%;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-right: 1px solid rgba(22, 50, 79, 0.08);
    display: flex;
    flex-direction: column;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    will-change: width;
    overflow: hidden;
    flex-shrink: 0;
}

.session-sidebar.is-collapsed {
    width: 64px;
}

.sidebar-header {
    height: 60px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid rgba(22, 50, 79, 0.05);
}

.sidebar-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #13273f;
    white-space: nowrap;
}

.collapse-btn {
    color: rgba(19, 39, 63, 0.56);
    margin-left: auto;
}

.collapse-btn:hover {
    color: var(--color-primary);
    background: rgba(22, 50, 79, 0.04);
}

.session-sidebar.is-collapsed .sidebar-header {
    justify-content: center;
    padding: 0;
}

.session-sidebar.is-collapsed .collapse-btn {
    margin: 0;
}

.sidebar-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16px;
    overflow-y: auto;
}

.new-session-btn {
    width: 100%;
    height: 40px;
    margin-bottom: 20px;
    font-weight: 600;
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    border: none;
    box-shadow: 0 8px 16px rgba(209, 96, 61, 0.15);
}

.new-session-btn:hover {
    background: linear-gradient(135deg, #d66a49, #b94725);
}

.session-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.session-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 14px;
    border-radius: 14px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid transparent;
}

.session-item:hover {
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(22, 50, 79, 0.06);
}

.session-item.is-active {
    background: rgba(255, 255, 255, 1);
    border-color: rgba(209, 96, 61, 0.15);
    box-shadow: 0 4px 12px rgba(21, 37, 64, 0.04);
}

.session-icon {
    font-size: 18px;
    color: rgba(19, 39, 63, 0.4);
}

.session-item.is-active .session-icon {
    color: #d1603d;
}

.session-info {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.session-name {
    font-size: 14px;
    font-weight: 500;
    color: #13273f;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.session-item.is-active .session-name {
    font-weight: 600;
}

.session-meta {
    font-size: 11px;
    color: rgba(19, 39, 63, 0.5);
    margin-top: 2px;
}

.empty-hint {
    padding: 20px 0;
    text-align: center;
    font-size: 12px;
    color: rgba(19, 39, 63, 0.4);
}
</style>
