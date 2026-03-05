<template>
    <div class="markdown-body" v-html="renderedContent" @click="handleCopy"></div>
</template>

<script setup>
import { computed } from 'vue';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css'; // Or any other style you prefer
import { ElMessage } from 'element-plus';

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
        let highlightedCode = '';
        if (lang && hljs.getLanguage(lang)) {
            try {
                highlightedCode = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value;
            } catch (__) {
                highlightedCode = md.utils.escapeHtml(str);
            }
        } else {
            highlightedCode = md.utils.escapeHtml(str);
        }

        const wrapper = `<div class="code-block-wrapper">
                    <div class="code-header">
                        <span class="code-lang">${lang || 'text'}</span>
                        <button class="copy-btn">复制</button>
                    </div>
                    <pre class="hljs"><code>${highlightedCode}</code></pre>
                </div>`;
        return wrapper;
    }
});

// Custom fence renderer to prevent double wrapping
const defaultFence = md.renderer.rules.fence;
md.renderer.rules.fence = function (tokens, idx, options, env, self) {
    const token = tokens[idx];
    const info = token.info ? md.utils.unescapeAll(token.info).trim() : '';
    const langName = info.split(/\s+/g)[0];

    // Call the highlight option which now returns the full HTML structure
    const highlighted = options.highlight(token.content, langName);

    return highlighted + '\n';
};

const renderedContent = computed(() => {
    return md.render(props.content || '');
});

const handleCopy = async (event) => {
    const target = event.target;
    if (target.classList.contains('copy-btn')) {
        const wrapper = target.closest('.code-block-wrapper');
        if (wrapper) {
            const codeBlock = wrapper.querySelector('pre code');
            if (codeBlock) {
                const codeText = codeBlock.innerText;
                try {
                    await navigator.clipboard.writeText(codeText);
                    const originalText = target.textContent;
                    target.textContent = '已复制';
                    target.classList.add('copied');

                    setTimeout(() => {
                        target.textContent = originalText === '已复制' ? '复制' : originalText;
                        target.classList.remove('copied');
                    }, 2000);

                    ElMessage.success('复制代码成功');
                } catch (err) {
                    ElMessage.error('复制失败');
                    console.error('Failed to copy: ', err);
                }
            }
        }
    }
};
</script>

<style scoped>
.markdown-body {
    color: var(--text-color-regular);
    line-height: 1.8;
    font-size: 1.0625rem;
}

/* Code Block Wrapper Styles */
:deep(.code-block-wrapper) {
    margin-bottom: 16px;
    background-color: #f6f8fa;
    border-radius: 6px;
    border: 1px solid var(--border-color-light);
    overflow: hidden;
    /* Ensure header corners are clipped */
}

:deep(.code-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 16px;
    background-color: #f1f3f5;
    border-bottom: 1px solid var(--border-color-light);
    font-size: 0.85em;
    color: var(--text-color-secondary);
    user-select: none;
}

:deep(.code-lang) {
    font-weight: 600;
    text-transform: uppercase;
    font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
}

:deep(.copy-btn) {
    background: none;
    border: none;
    cursor: pointer;
    color: var(--text-color-secondary);
    font-size: 0.9em;
    transition: all 0.2s;
    padding: 4px 8px;
    border-radius: 4px;
}

:deep(.copy-btn:hover) {
    color: var(--color-primary);
    background-color: rgba(0, 0, 0, 0.05);
}

:deep(.copy-btn.copied) {
    color: var(--color-success);
}

/* Basic Markdown Styles */
:deep(h1),
:deep(h2),
:deep(h3),
:deep(h4),
:deep(h5),
:deep(h6) {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
    color: var(--text-color-primary);
}

:deep(h1) {
    font-size: 2em;
}

:deep(h2) {
    font-size: 1.5em;
    border-bottom: 1px solid var(--border-color-light);
    padding-bottom: .3em;
}

:deep(h3) {
    font-size: 1.25em;
}

:deep(h4) {
    font-size: 1em;
}

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

:deep(ul),
:deep(ol) {
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
    /* Matches wrapper bg */
    border-radius: 0 0 6px 6px;
    margin-bottom: 0;
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

:deep(table th),
:deep(table td) {
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
