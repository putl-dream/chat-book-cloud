<template>
    <div class="default-sidebar">
        <div class="article-right-card">
            <AuthorCard :userId="userId" />
        </div>
        <div class="article-right-card">
            <HotCard />
        </div>
    </div>
</template>

<script setup>
import AuthorCard from "@/components/widget/AuthorCard.vue";
import HotCard from "@/components/widget/HotCard.vue";

defineProps({
    userId: {
        type: [Number, String],
        required: false
    }
});
</script>

<style scoped>
.default-sidebar {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.article-right-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    overflow: hidden;
}
</style>
