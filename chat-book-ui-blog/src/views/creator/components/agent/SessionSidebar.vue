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

        <div v-if="!isCollapsed" class="sidebar-body">
            <el-button
                class="new-session-btn"
                type="primary"
                round
                @click="handleNewSession"
            >
                <el-icon><Plus /></el-icon>
                <span>新灵感对话</span>
            </el-button>

            <div class="search-shell">
                <el-input
                    v-model="searchKeyword"
                    class="search-input"
                    clearable
                    placeholder="搜索历史会话标题"
                    @input="handleSearchInput"
                    @clear="handleSearchClear"
                >
                    <template #prefix>
                        <el-icon><Search /></el-icon>
                    </template>
                </el-input>
            </div>

            <div v-if="activeSessionRecord" class="session-section">
                <div class="section-label">当前会话</div>
                <button
                    type="button"
                    class="session-item is-active"
                    @click="handleSelectSession(activeSessionRecord.id)"
                >
                    <el-icon class="session-icon"><ChatDotRound /></el-icon>
                    <div class="session-info">
                        <div class="session-headline">
                            <span class="session-name" :title="activeSessionRecord.title">
                                {{ activeSessionRecord.title }}
                            </span>
                            <span class="session-badge">当前</span>
                        </div>
                        <span class="session-meta">
                            {{ resolveSceneLabel(activeSessionRecord.sceneType) }}
                            <span v-if="activeSessionRecord.targetDraftId" class="session-separator">·</span>
                            <span v-if="activeSessionRecord.targetDraftId">已生成草稿</span>
                        </span>
                    </div>
                </button>
            </div>

            <div class="session-section history-section">
                <div class="section-label">历史会话</div>

                <div v-if="store.sessionHistoryLoading" class="session-skeleton-list">
                    <div v-for="index in 5" :key="index" class="session-skeleton">
                        <span class="session-skeleton__title"></span>
                        <span class="session-skeleton__meta"></span>
                    </div>
                </div>

                <div v-else-if="historySessions.length" class="session-list custom-scrollbar">
                    <button
                        v-for="item in historySessions"
                        :key="item.id"
                        type="button"
                        class="session-item session-item--history"
                        @click="handleSelectSession(item.id)"
                    >
                        <el-icon class="session-icon"><ChatDotRound /></el-icon>
                        <div class="session-info">
                            <div class="session-headline">
                                <span class="session-name" :title="item.title">{{ item.title }}</span>
                                <span class="session-time">{{ formatSessionTime(item.updateTime) }}</span>
                            </div>
                            <span class="session-meta">
                                {{ resolveSceneLabel(item.sceneType) }}
                                <span class="session-separator">·</span>
                                <span>{{ item.targetDraftId ? '已生成草稿' : '继续对话' }}</span>
                            </span>
                        </div>
                    </button>
                </div>

                <div v-else class="empty-hint">
                    {{ searchKeyword.trim() ? '没有匹配的历史会话。' : '还没有历史会话，先开始一段新对话。' }}
                </div>

                <el-button
                    v-if="store.hasMoreSessionHistory && !store.sessionHistoryLoading"
                    class="load-more-btn"
                    text
                    :loading="store.sessionHistoryLoadingMore"
                    @click="store.loadMoreSessionHistory"
                >
                    加载更多
                </el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { Fold, Expand, Plus, ChatDotRound, Search } from '@element-plus/icons-vue';

const store = useAgentStudioStore();
const isCollapsed = ref(false);
const searchKeyword = ref('');
let searchTimer = null;

const sceneLabelMap = Object.freeze({
    DISCUSS: '讨论共创',
    LEARN: '学习整理',
    DRAFT: '首稿生成',
    EDIT: '智能编辑'
});

const activeSessionRecord = computed(() => {
    if (!store.sessionId) {
        return null;
    }
    return store.sessionHistory.find((item) => item.id === store.sessionId) ?? {
        id: store.sessionId,
        title: store.sessionTitle,
        sceneType: store.currentScene,
        targetDraftId: store.session?.targetDraftId ?? null,
        updateTime: store.session?.updateTime ?? ''
    };
});

const historySessions = computed(() => store.sessionHistory.filter((item) => item.id !== store.sessionId));

watch(() => store.sessionHistoryKeyword, (nextKeyword) => {
    searchKeyword.value = nextKeyword ?? '';
}, { immediate: true });

const toggleCollapse = () => {
    isCollapsed.value = !isCollapsed.value;
};

const resolveSceneLabel = (sceneType) => sceneLabelMap[String(sceneType || '').toUpperCase()] || sceneLabelMap.DISCUSS;

const formatSessionTime = (value) => {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return '';
    }

    const now = new Date();
    const sameDay = date.toDateString() === now.toDateString();
    const formatter = sameDay
        ? new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' })
        : new Intl.DateTimeFormat('zh-CN', now.getFullYear() === date.getFullYear()
            ? { month: 'numeric', day: 'numeric' }
            : { year: 'numeric', month: 'numeric', day: 'numeric' });

    return formatter.format(date);
};

const handleSearch = async () => {
    await store.refreshSessionHistory(searchKeyword.value);
};

const handleSearchInput = () => {
    window.clearTimeout(searchTimer);
    searchTimer = window.setTimeout(() => {
        handleSearch();
    }, 260);
};

const handleSearchClear = async () => {
    window.clearTimeout(searchTimer);
    await handleSearch();
};

const handleNewSession = async () => {
    await store.openFreshSession();
};

const handleSelectSession = async (targetSessionId) => {
    await store.openSessionHistory(targetSessionId);
};

onBeforeUnmount(() => {
    window.clearTimeout(searchTimer);
});
</script>

<style scoped>
.session-sidebar {
    width: 300px;
    height: 100%;
    background:
        radial-gradient(circle at top left, rgba(var(--color-primary-rgb), 0.12), transparent 28%),
        linear-gradient(180deg, var(--bg-color-glass), var(--bg-color-white));
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-right: 1px solid var(--border-color-light);
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
    border-bottom: 1px solid var(--border-color-light);
}

.sidebar-title {
    margin: 0;
    font-size: 15px;
    font-weight: 700;
    color: var(--text-color-primary);
    white-space: nowrap;
    letter-spacing: 0.02em;
}

.collapse-btn {
    color: var(--text-color-secondary);
    margin-left: auto;
}

.collapse-btn:hover {
    color: var(--color-primary);
    background: var(--bg-color-hover);
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
    height: 42px;
    margin-bottom: 14px;
    font-weight: 600;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
    border: none;
    box-shadow: 0 10px 20px rgba(var(--color-primary-rgb), 0.18);
}

.new-session-btn:hover {
    background: linear-gradient(135deg, var(--color-primary-hover), var(--color-primary-active));
}

.search-shell {
    margin-bottom: 18px;
}

.search-input :deep(.el-input__wrapper) {
    border-radius: 16px;
    background: var(--bg-color-white);
    box-shadow: 0 0 0 1px var(--border-color-base);
    padding: 0 12px;
}

.search-input :deep(.el-input__wrapper.is-focus) {
    box-shadow:
        0 0 0 1px var(--color-primary),
        0 10px 18px rgba(var(--color-primary-rgb), 0.08);
}

.session-section + .session-section {
    margin-top: 16px;
}

.history-section {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.section-label {
    margin-bottom: 10px;
    font-size: 11px;
    font-weight: 700;
    color: var(--text-color-placeholder);
    letter-spacing: 0.08em;
}

.session-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.session-item {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 13px 14px;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid transparent;
    text-align: left;
    background: var(--bg-color-glass);
}

.session-item:hover {
    background: var(--bg-color-hover);
    border-color: var(--border-color-base);
    transform: translateY(-1px);
}

.session-item.is-active {
    background: var(--bg-color-white);
    border-color: var(--color-primary);
    box-shadow: var(--box-shadow-base);
}

.session-item--history {
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.session-icon {
    font-size: 18px;
    color: var(--text-color-placeholder);
    flex-shrink: 0;
}

.session-item.is-active .session-icon {
    color: var(--color-primary);
}

.session-info {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
    flex: 1;
}

.session-headline {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.session-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.session-time,
.session-meta {
    font-size: 11px;
    color: var(--text-color-secondary);
}

.session-time {
    margin-left: auto;
    flex-shrink: 0;
}

.session-separator {
    margin: 0 4px;
}

.session-badge {
    padding: 2px 8px;
    border-radius: 999px;
    font-size: 10px;
    font-weight: 700;
    color: var(--color-primary);
    background: var(--color-primary-light);
    flex-shrink: 0;
}

.session-skeleton-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.session-skeleton {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 14px;
    border-radius: 16px;
    background: var(--bg-color-glass);
    border: 1px solid var(--border-color-light);
}

.session-skeleton__title,
.session-skeleton__meta {
    display: block;
    border-radius: 999px;
    background: linear-gradient(90deg, var(--border-color-light), var(--border-color-base), var(--border-color-light));
    background-size: 200% 100%;
    animation: skeleton-shimmer 1.3s linear infinite;
}

.session-skeleton__title {
    width: 72%;
    height: 12px;
}

.session-skeleton__meta {
    width: 46%;
    height: 10px;
}

.empty-hint {
    padding: 20px 8px;
    text-align: center;
    font-size: 12px;
    line-height: 1.6;
    color: var(--text-color-secondary);
    background: var(--bg-color-glass);
    border-radius: 16px;
    border: 1px dashed var(--border-color-base);
}

.load-more-btn {
    margin-top: 12px;
    align-self: center;
    color: var(--text-color-regular);
}

.load-more-btn:hover {
    color: var(--color-primary);
}

@keyframes skeleton-shimmer {
    from {
        background-position: 200% 0;
    }

    to {
        background-position: -200% 0;
    }
}
</style>
