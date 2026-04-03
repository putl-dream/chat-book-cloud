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

const legacyCopyText = (text) => {
    if (typeof document === 'undefined' || typeof document.execCommand !== 'function') {
        return false;
    }

    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'readonly');
    textarea.style.position = 'fixed';
    textarea.style.top = '0';
    textarea.style.left = '-9999px';
    textarea.style.opacity = '0';
    textarea.style.pointerEvents = 'none';

    document.body.appendChild(textarea);
    textarea.focus({ preventScroll: true });
    textarea.select();
    textarea.setSelectionRange(0, textarea.value.length);

    const copied = document.execCommand('copy');
    document.body.removeChild(textarea);

    return copied;
};

const copyCodeText = async (text) => {
    if (navigator?.clipboard?.writeText) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch {
            // Non-secure contexts or embedded webviews may reject Clipboard API writes.
        }
    }

    return legacyCopyText(text);
};

const handleCopy = async (event) => {
    const target = event.target instanceof HTMLElement
        ? event.target.closest('.copy-btn')
        : null;

    if (!(target instanceof HTMLElement)) {
        return;
    }

    const wrapper = target.closest('.code-block-wrapper');
    const codeBlock = wrapper?.querySelector('pre code');

    if (!(codeBlock instanceof HTMLElement)) {
        return;
    }

    try {
        const copied = await copyCodeText(codeBlock.textContent || '');

        if (!copied) {
            ElMessage.error('当前环境不支持复制');
            return;
        }

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
