<template>
    <component
        :is="as"
        class="rich-text-viewer content-theme"
        :data-content-variant="variant"
        :data-source-format="sourceFormat"
        :data-readonly="true">
        <div
            class="rich-text-viewer__body"
            v-html="renderedContent"
            @click="handleCopy" />
    </component>
</template>

<script setup>
import { computed } from 'vue';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import { ElMessage } from 'element-plus';

const props = defineProps({
    content: {
        type: String,
        default: ''
    },
    sourceFormat: {
        type: String,
        default: 'html'
    },
    variant: {
        type: String,
        default: 'article'
    },
    as: {
        type: String,
        default: 'div'
    }
});

const CODE_LANGUAGE_RE = /(?:language|lang)-([\w+-]+)/i;

const escapeHtml = (value = '') => value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');

const highlightCode = (code = '', lang = '') => {
    if (lang && hljs.getLanguage(lang)) {
        try {
            return hljs.highlight(code, { language: lang, ignoreIllegals: true }).value;
        } catch (error) {
            console.warn('代码高亮失败:', error);
        }
    }

    return escapeHtml(code);
};

const resolveCodeLanguage = (...elements) => {
    for (const element of elements) {
        if (!element) continue;

        const classNames = Array.from(element.classList || []);
        for (const className of classNames) {
            const match = className.match(CODE_LANGUAGE_RE);
            if (match?.[1]) {
                return match[1].toLowerCase();
            }
        }

        const dataLanguage = element.getAttribute?.('data-language') || element.getAttribute?.('language');
        if (dataLanguage) {
            return dataLanguage.toLowerCase();
        }
    }

    return '';
};

const wrapCodeBlocks = (html = '') => {
    if (!html || typeof DOMParser === 'undefined') {
        return html;
    }

    const parser = new DOMParser();
    const doc = parser.parseFromString(`<div data-rich-text-root>${html}</div>`, 'text/html');
    const root = doc.body.querySelector('[data-rich-text-root]');

    if (!root) {
        return html;
    }

    root.querySelectorAll('pre').forEach((pre) => {
        if (pre.parentElement?.classList.contains('code-block-wrapper')) {
            return;
        }

        const code = pre.querySelector('code');
        const language = resolveCodeLanguage(code, pre) || 'text';

        if (code && language !== 'text') {
            code.innerHTML = highlightCode(code.textContent || '', language);
            pre.classList.add('hljs');
        }

        const wrapper = doc.createElement('div');
        wrapper.className = 'code-block-wrapper';

        const header = doc.createElement('div');
        header.className = 'code-header';

        const langLabel = doc.createElement('span');
        langLabel.className = 'code-lang';
        langLabel.textContent = language;

        const copyButton = doc.createElement('button');
        copyButton.type = 'button';
        copyButton.className = 'copy-btn';
        copyButton.textContent = '复制';

        header.append(langLabel, copyButton);
        pre.parentNode?.insertBefore(wrapper, pre);
        wrapper.append(header, pre);
    });

    return root.innerHTML;
};

const md = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true,
    highlight(str, lang) {
        const highlightedCode = highlightCode(str, lang);

        return `
            <div class="code-block-wrapper">
                <div class="code-header">
                    <span class="code-lang">${lang || 'text'}</span>
                    <button type="button" class="copy-btn">复制</button>
                </div>
                <pre class="hljs"><code>${highlightedCode}</code></pre>
            </div>
        `;
    }
});

md.renderer.rules.fence = function (tokens, idx, options) {
    const token = tokens[idx];
    const info = token.info ? md.utils.unescapeAll(token.info).trim() : '';
    const langName = info.split(/\s+/g)[0];

    return `${options.highlight(token.content, langName)}\n`;
};

const renderedContent = computed(() => {
    if (!props.content) {
        return '';
    }

    if (props.sourceFormat === 'markdown') {
        return md.render(props.content);
    }

    return wrapCodeBlocks(props.content);
});

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
