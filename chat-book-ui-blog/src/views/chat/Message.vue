<template>
    <div class="message-page">
        <div class="message-container">
            <div class="message-header c-page-header c-page-header--accent">
                <h2 class="page-title c-page-header__title">
                    <span class="u-text-gradient">消息通知</span>
                </h2>
            </div>

            <div class="message-list-wrapper custom-scrollbar">
                <div v-if="loading" class="loading c-loading-state c-glass-panel">
                    <el-skeleton :rows="5" animated />
                </div>

                <template v-else-if="messages.length > 0">
                    <transition-group name="list-fade" tag="div" class="message-grid">
                        <div v-for="(message, index) in messages" :key="message.id || index"
                            class="message-card-wrapper" :style="{ '--delay': `${index * 0.1}s` }"
                            @click="openArticle(message.articleId)">
                            <div class="message-card c-glass-panel">
                                <div class="message-icon-box">
                                    <el-icon :size="24">
                                        <BellFilled />
                                    </el-icon>
                                </div>
                                <div class="message-content-area">
                                    <MessageCard :message="message" />
                                </div>
                                <div class="card-arrow">
                                    <el-icon>
                                        <ArrowRight />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </transition-group>
                </template>

                <div v-else class="empty-state c-empty-panel">
                    <el-empty description="暂无新消息" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import MessageCard from "@/views/chat/components/MessageCard.vue";
import { getNotifications } from "@/views/article/_domain/interaction.js";
import { onMounted, ref } from "vue";
import router from "@/router/index.js";
import { ElSkeleton, ElEmpty } from 'element-plus';

const messages = ref([]);
const loading = ref(false);

const messageRequest = async () => {
    loading.value = true;
    try {
        // P0 Fix: getMessage 接口返回数据错误，已更换为 getNotifications
        const res = await getNotifications();
        if (res) {
            messages.value = res;
        }
    } catch (error) {
        console.error("获取消息失败", error);
    } finally {
        loading.value = false;
    }
}

const openArticle = async (articleId) => {
    // P0 Fix: 使用 message.articleId 跳转（NotificationVO 的 id 是足迹记录ID，非文章ID）
    await router.push({ name: 'Article', params: { id: articleId } });
};

onMounted(() => {
    messageRequest();
})

</script>

<style scoped>
.message-page {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
    background-color: var(--bg-color-base);
    padding: 40px 24px 20px;
    background-image:
        radial-gradient(circle at 10% 20%, rgba(var(--color-primary-rgb), 0.05) 0%, transparent 20%),
        radial-gradient(circle at 90% 80%, rgba(var(--color-success-rgb), 0.05) 0%, transparent 20%);
}

.message-container {
    max-width: 900px;
    width: 100%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
}

.message-header {
    --page-header-margin: 0 0 32px;
    --page-header-accent-padding: 16px;
    flex-shrink: 0;
}

.page-title {
    --page-header-title-size: 28px;
    --page-header-title-margin: 0;
    line-height: 1.2;
}

.message-list-wrapper {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding-right: 8px;
    padding-bottom: 20px;
    --scrollbar-thumb: rgba(148, 163, 184, 0.3);
    --scrollbar-thumb-hover: rgba(148, 163, 184, 0.5);
}

.message-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.message-card-wrapper {
    /* For animation stagger */
    animation: slideUp 0.5s ease backwards;
    animation-delay: var(--delay);
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message-card {
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
    --surface-padding: 20px;
    --surface-radius: 16px;
    --surface-bg: rgba(255, 255, 255, 0.8);
    --surface-blur: 12px;
    --surface-border: rgba(255, 255, 255, 0.6);
    --surface-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
}

.message-card:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
    background: rgba(255, 255, 255, 0.95);
    border-color: var(--color-primary-light);
}

.message-icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #e0eaff 0%, #f0f7ff 100%);
    color: var(--color-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    flex-shrink: 0;
    transition: transform 0.3s ease;
}

.message-card:hover .message-icon-box {
    transform: rotate(10deg);
    background: var(--color-primary);
    color: #fff;
}

.message-content-area {
    flex: 1;
    min-width: 0;
    /* Fix flex child text overflow */
}

.card-arrow {
    margin-left: 16px;
    color: var(--text-color-placeholder);
    transition: transform 0.3s ease;
}

.message-card:hover .card-arrow {
    transform: translateX(4px);
    color: var(--color-primary);
}

.loading {
    --feedback-loading-padding: 24px;
    --surface-padding: 24px;
    --surface-bg: rgba(255, 255, 255, 0.5);
    --surface-radius: 16px;
    --surface-border: transparent;
    --surface-shadow: none;
    --surface-blur: 0px;
}

.empty-state {
    --empty-panel-radius: 16px;
    --empty-panel-bg: rgba(255, 255, 255, 0.5);
}

/* Transitions */
.list-fade-enter-active,
.list-fade-leave-active {
    transition: all 0.5s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
    opacity: 0;
    transform: translateX(-20px);
}
</style>
