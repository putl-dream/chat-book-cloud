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
                    <transition-group name="message-list-fade" tag="div" class="message-grid">
                        <div v-for="(message, index) in messages" :key="message.id || index"
                            class="message-card-wrapper" :style="{ '--delay': `${index * 0.1}s` }"
                            @click="openArticle(message.articleId)">
                            <div class="notification-card c-glass-panel">
                                <div class="notification-icon-box">
                                    <el-icon :size="24">
                                        <BellFilled />
                                    </el-icon>
                                </div>
                                <div class="notification-content-area">
                                    <MessageCard :message="message" />
                                </div>
                                <div class="notification-card-arrow">
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
