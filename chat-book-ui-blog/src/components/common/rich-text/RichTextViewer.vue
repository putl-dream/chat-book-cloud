<template>
    <component
        :is="as"
        class="rich-text-viewer content-theme"
        :data-content-variant="variant"
        :data-content-style="theme || null"
        :data-readonly="true">
        <div
            class="rich-text-viewer__body"
            v-html="renderedHtml"
            @click="handleCopy" />
    </component>
</template>

<script setup>
import { computed } from 'vue';
import 'highlight.js/styles/github.css';
import { ElMessage } from 'element-plus';
import '@/styles/themes/article/light.css';
import '@/styles/themes/article/reading.css';

const props = defineProps({
    html: {
        type: String,
        default: ''
    },
    content: {
        type: String,
        default: ''
    },
    variant: {
        type: String,
        default: 'article'
    },
    theme: {
        type: String,
        default: ''
    },
    as: {
        type: String,
        default: 'div'
    }
});

const renderedHtml = computed(() => props.html || props.content || '');

const handleCopy = async (event) => {
    const target = event.target;

    if (!(target instanceof HTMLElement) || !target.classList.contains('copy-btn')) {
        return;
    }

    const wrapper = target.closest('.code-block-wrapper');
    const codeBlock = wrapper?.querySelector('pre code');

    if (!(codeBlock instanceof HTMLElement)) {
        return;
    }

    if (!navigator?.clipboard?.writeText) {
        ElMessage.error('当前环境不支持复制');
        return;
    }

    try {
        await navigator.clipboard.writeText(codeBlock.innerText);
        target.textContent = '已复制';
        target.classList.add('copied');

        window.setTimeout(() => {
            target.textContent = '复制';
            target.classList.remove('copied');
        }, 1600);

        ElMessage.success('复制代码成功');
    } catch (error) {
        console.error('Failed to copy code block:', error);
        ElMessage.error('复制失败');
    }
};
</script>

<style scoped>
.rich-text-viewer {
    display: block;
    width: 100%;
}
</style>
