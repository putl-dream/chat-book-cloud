import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';

const CODE_LANGUAGE_RE = /(?:language|lang)-([\w+-]+)/i;

const markdownParser = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: true
});

const escapeHtml = (value = '') => value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');

export const slugifyHeadingText = (value = '', fallback = 'section') => {
    const normalized = String(value)
        .trim()
        .toLowerCase()
        .replace(/_/g, '-')
        .replace(/[^\w\u3400-\u9fff\s-]/g, '')
        .replace(/\s+/g, '-')
        .replace(/-+/g, '-')
        .replace(/^-|-$/g, '');

    return normalized || fallback;
};

export const createHeadingId = (value = '', occurrenceMap = new Map(), fallback = 'section') => {
    const base = slugifyHeadingText(value, fallback);
    const nextIndex = (occurrenceMap.get(base) || 0) + 1;

    occurrenceMap.set(base, nextIndex);

    return nextIndex === 1 ? base : `${base}-${nextIndex}`;
};

export const parseMarkdownToHtml = (markdown = '') => {
    if (!markdown) {
        return '';
    }

    return markdownParser.render(markdown);
};

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

const normalizeHeadingAnchors = (root) => {
    const headingOccurrences = new Map();

    root.querySelectorAll('h1, h2, h3, h4, h5, h6').forEach((heading, index) => {
        heading.id = createHeadingId(heading.textContent || '', headingOccurrences, `section-${index + 1}`);
    });
};

const enhanceCodeBlocks = (doc, root) => {
    root.querySelectorAll('pre').forEach((pre) => {
        if (pre.parentElement?.classList.contains('code-block-wrapper')) {
            return;
        }

        let code = pre.querySelector('code');
        if (!code) {
            code = doc.createElement('code');
            code.textContent = pre.textContent || '';
            pre.textContent = '';
            pre.appendChild(code);
        }

        const language = resolveCodeLanguage(code, pre) || 'text';

        if (language !== 'text') {
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
};

const enhanceTables = (doc, root) => {
    root.querySelectorAll('table').forEach((table) => {
        if (table.parentElement?.classList.contains('table-scroll')) {
            return;
        }

        const wrapper = doc.createElement('div');
        wrapper.className = 'table-scroll';
        table.parentNode?.insertBefore(wrapper, table);
        wrapper.appendChild(table);
    });
};

const enhanceMedia = (root) => {
    root.querySelectorAll('img').forEach((image) => {
        if (!image.hasAttribute('loading')) {
            image.setAttribute('loading', 'lazy');
        }

        if (!image.hasAttribute('decoding')) {
            image.setAttribute('decoding', 'async');
        }
    });
};

export const enhanceContentHtml = (html = '') => {
    if (!html || typeof DOMParser === 'undefined') {
        return html;
    }

    const parser = new DOMParser();
    const doc = parser.parseFromString(`<div data-rich-text-root>${html}</div>`, 'text/html');
    const root = doc.body.querySelector('[data-rich-text-root]');

    if (!root) {
        return html;
    }

    normalizeHeadingAnchors(root);
    enhanceCodeBlocks(doc, root);
    enhanceTables(doc, root);
    enhanceMedia(root);

    return root.innerHTML;
};

export const buildRichTextHtml = (content = '', sourceFormat = 'html') => {
    if (!content) {
        return '';
    }

    const normalizedHtml = sourceFormat === 'markdown'
        ? parseMarkdownToHtml(content)
        : content;

    return enhanceContentHtml(normalizedHtml);
};
