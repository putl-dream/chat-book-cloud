<template>
    <div class="markdown-body" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css'; // Or any other style you prefer

const props = defineProps({
    content: {
        type: String,
        default: ''
    }
});

const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight: function (str, lang) {
        if (lang && hljs.getLanguage(lang)) {
            try {
                return '<pre class="hljs"><code>' +
                    hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
                    '</code></pre>';
            } catch (__) { }
        }

        return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
    }
});

const renderedContent = computed(() => {
    return md.render(props.content || '');
});
</script>

<style scoped>
.markdown-body {
    color: var(--text-color-regular);
    line-height: 1.8;
    font-size: 1.0625rem;
}

/* Basic Markdown Styles */
:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
    color: var(--text-color-primary);
}

:deep(h1) { font-size: 2em; }
:deep(h2) { font-size: 1.5em; border-bottom: 1px solid var(--border-color-light); padding-bottom: .3em; }
:deep(h3) { font-size: 1.25em; }
:deep(h4) { font-size: 1em; }

:deep(p) {
    margin-bottom: 16px;
}

:deep(a) {
    color: var(--color-primary);
    text-decoration: none;
}
:deep(a:hover) {
    text-decoration: underline;
}

:deep(ul), :deep(ol) {
    padding-left: 2em;
    margin-bottom: 16px;
}

:deep(blockquote) {
    margin: 0 0 16px;
    padding: 0 1em;
    color: var(--text-color-secondary);
    border-left: 0.25em solid var(--border-color-base);
}

:deep(pre) {
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 6px;
    margin-bottom: 16px;
}

:deep(code) {
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 85%;
    background-color: rgba(175, 184, 193, 0.2);
    border-radius: 6px;
    font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
}

:deep(pre code) {
    padding: 0;
    margin: 0;
    background-color: transparent;
    border-radius: 0;
}

:deep(img) {
    max-width: 100%;
    box-sizing: content-box;
    background-color: #fff;
}

:deep(table) {
    border-spacing: 0;
    border-collapse: collapse;
    margin-bottom: 16px;
    width: 100%;
}

:deep(table th), :deep(table td) {
    padding: 6px 13px;
    border: 1px solid var(--border-color-light);
}

:deep(table tr) {
    background-color: #fff;
    border-top: 1px solid #c6cbd1;
}

:deep(table tr:nth-child(2n)) {
    background-color: #f6f8fa;
}
</style>
