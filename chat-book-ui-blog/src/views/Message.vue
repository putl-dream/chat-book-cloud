<template>
    <div class="message-page">
        <div class="message-container">
            <div class="message-header">
                <h2 class="page-title">消息中心</h2>
            </div>

            <div class="message-list-wrapper">
                <div v-if="loading" class="loading">
                    <el-skeleton :rows="5" animated />
                </div>
                
                <template v-else-if="messages.length > 0">
                    <div v-for="(message, index) in messages" :key="index" class="message-item" @click="openArticle(message.id)">
                        <MessageCard :message="message"/>
                    </div>
                </template>
                
                <div v-else class="empty-state">
                    <el-empty description="暂无消息通知" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import MessageCard from "@/components/widget/MessageCard.vue";
import {getMessage} from "@/api/user.js";
import {onMounted, ref} from "vue";
import router from "@/router/index.js";
import { ElSkeleton, ElEmpty } from 'element-plus';

const messages = ref([]);
const loading = ref(false);

const messageRequest = async () => {
    loading.value = true;
    try {
        const res = await getMessage();
        if (res){
            messages.value = res;
        }
    } catch (error) {
        console.error("获取消息失败", error);
    } finally {
        loading.value = false;
    }
}

const openArticle = async (id) => {
    await router.push({name: 'Article', params: {id: id}});
};

onMounted(() => {
    messageRequest();
})

</script>

<style scoped>
.message-page {
    min-height: 100%;
    background-color: var(--bg-color-base);
    padding-top: 24px;
}

.message-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 20px;
}

.message-header {
    margin-bottom: 20px;
}

.page-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-color-primary);
    padding-left: 12px;
    border-left: 4px solid var(--color-primary);
}

.message-list-wrapper {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    box-shadow: var(--box-shadow-base);
    overflow: hidden;
    min-height: 400px;
}

.message-item {
    border-bottom: 1px solid var(--border-color-lighter);
    cursor: pointer;
}

.message-item:last-child {
    border-bottom: none;
}

.loading {
    padding: 24px;
}

.empty-state {
    padding: 40px 0;
}
</style>