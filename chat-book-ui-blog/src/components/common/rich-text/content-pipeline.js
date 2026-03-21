import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';

const CODE_LANGUAGE_RE = /(?:language|lang)-([\w+-]+)/i;
const ELEMENT_NODE = 1;
const TEXT_NODE = 3;
const COMMENT_NODE = 8;
const TASK_LIST_MARKER_RE = /^\s*\[([ xX])\]\s+/;
const TASK_ITEM_BLOCK_TAGS = new Set([
    'blockquote',
    'div',
    'figure',
    'h1',
    'h2',
    'h3',
    'h4',
    'h5',
    'h6',
    'hr',
    'ol',
    'p',
    'pre',
    'table',
    'ul'
]);
const ALLOWED_TAGS = new Set([
    'a',
    'audio',
    'blockquote',
    'br',
    'caption',
    'code',
    'del',
    'div',
    'em',
    'figcaption',
    'figure',
    'h1',
    'h2',
    'h3',
    'h4',
    'h5',
    'h6',
    'hr',
    'img',
    'input',
    'label',
    'li',
    'mark',
    'ol',
    'p',
    'pre',
    's',
    'source',
    'span',
    'strong',
    'sub',
    'sup',
    'table',
    'tbody',
    'td',
    'tfoot',
    'th',
    'thead',
    'tr',
    'u',
    'ul',
    'video'
]);
const DROP_CONTENT_TAGS = new Set([
    'base',
    'embed',
    'form',
    'iframe',
    'link',
    'meta',
    'object',
    'script',
    'style'
]);
const GLOBAL_ALLOWED_ATTRIBUTES = new Set(['class', 'style', 'title']);
const TAG_ALLOWED_ATTRIBUTES = {
    a: new Set(['href', 'rel', 'target']),
    audio: new Set(['controls', 'preload', 'src']),
    img: new Set(['alt', 'height', 'src', 'width']),
    input: new Set(['checked', 'disabled', 'type']),
    ol: new Set(['start']),
    source: new Set(['src', 'type']),
    td: new Set(['colspan', 'rowspan']),
    th: new Set(['colspan', 'rowspan', 'scope']),
    video: new Set(['controls', 'height', 'muted', 'playsinline', 'poster', 'preload', 'src', 'width'])
};
const SAFE_TEXT_ALIGN_VALUES = new Set(['left', 'right', 'center', 'justify', 'start', 'end']);
const SAFE_COLOR_VALUE_RE = /^(#[\da-f]{3,8}|rgba?\([^)]*\)|hsla?\([^)]*\)|var\(--[\w-]+\)|[a-z-]+)$/i;

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

const isRelativeUrl = (value = '') => {
    const normalized = String(value).trim();

    if (!normalized || normalized.startsWith('//')) {
        return false;
    }

    return !/^[a-z][\w+.-]*:/i.test(normalized);
};

const isSafeLinkUrl = (value = '') => {
    const normalized = String(value).trim().replace(/[\u0000-\u001F\u007F]+/g, '');

    if (!normalized) {
        return '';
    }

    if (isRelativeUrl(normalized)) {
        return normalized;
    }

    const lowerValue = normalized.toLowerCase();

    if (
        lowerValue.startsWith('http://')
        || lowerValue.startsWith('https://')
        || lowerValue.startsWith('mailto:')
        || lowerValue.startsWith('tel:')
    ) {
        return normalized;
    }

    return '';
};

const isSafeMediaUrl = (value = '') => {
    const normalized = String(value).trim().replace(/[\u0000-\u001F\u007F]+/g, '');

    if (!normalized) {
        return '';
    }

    if (isRelativeUrl(normalized) || normalized.startsWith('blob:')) {
        return normalized;
    }

    const lowerValue = normalized.toLowerCase();

    if (lowerValue.startsWith('http://') || lowerValue.startsWith('https://')) {
        return normalized;
    }

    if (/^data:image\/(?:png|gif|jpe?g|webp|bmp|avif);base64,[a-z0-9+/=]+$/i.test(normalized)) {
        return normalized;
    }

    return '';
};

const sanitizeStyle = (styleText = '') => {
    const sanitizedDeclarations = [];

    String(styleText)
        .split(';')
        .map((declaration) => declaration.trim())
        .filter(Boolean)
        .forEach((declaration) => {
            const separatorIndex = declaration.indexOf(':');

            if (separatorIndex === -1) {
                return;
            }

            const property = declaration.slice(0, separatorIndex).trim().toLowerCase();
            const value = declaration.slice(separatorIndex + 1).trim();

            if (!value) {
                return;
            }

            if (property === 'text-align' && SAFE_TEXT_ALIGN_VALUES.has(value.toLowerCase())) {
                sanitizedDeclarations.push(`${property}: ${value}`);
                return;
            }

            if (
                (property === 'color' || property === 'background-color')
                && SAFE_COLOR_VALUE_RE.test(value)
            ) {
                sanitizedDeclarations.push(`${property}: ${value}`);
            }
        });

    return sanitizedDeclarations.join('; ');
};

const unwrapNode = (node) => {
    const parentNode = node.parentNode;

    if (!parentNode) {
        return;
    }

    while (node.firstChild) {
        parentNode.insertBefore(node.firstChild, node);
    }

    parentNode.removeChild(node);
};

const isAllowedAttribute = (tagName, attributeName) => (
    GLOBAL_ALLOWED_ATTRIBUTES.has(attributeName)
    || TAG_ALLOWED_ATTRIBUTES[tagName]?.has(attributeName)
    || attributeName.startsWith('data-')
    || attributeName.startsWith('aria-')
);

const sanitizeAttributes = (element) => {
    const tagName = element.tagName.toLowerCase();

    Array.from(element.attributes).forEach((attribute) => {
        const attributeName = attribute.name.toLowerCase();

        if (attributeName.startsWith('on') || !isAllowedAttribute(tagName, attributeName)) {
            element.removeAttribute(attribute.name);
            return;
        }

        if (attributeName === 'style') {
            const sanitizedStyle = sanitizeStyle(attribute.value);

            if (sanitizedStyle) {
                element.setAttribute('style', sanitizedStyle);
            } else {
                element.removeAttribute(attribute.name);
            }

            return;
        }

        if (attributeName === 'href') {
            const safeUrl = isSafeLinkUrl(attribute.value);

            if (safeUrl) {
                element.setAttribute('href', safeUrl);
            } else {
                element.removeAttribute(attribute.name);
            }

            return;
        }

        if (attributeName === 'src') {
            const safeUrl = tagName === 'a'
                ? isSafeLinkUrl(attribute.value)
                : isSafeMediaUrl(attribute.value);

            if (safeUrl) {
                element.setAttribute('src', safeUrl);
            } else {
                element.removeAttribute(attribute.name);
            }

            return;
        }

        if (attributeName === 'target') {
            const safeTarget = attribute.value === '_blank' ? '_blank' : '';

            if (safeTarget) {
                element.setAttribute('target', safeTarget);
                element.setAttribute('rel', 'noopener noreferrer nofollow');
            } else {
                element.removeAttribute(attribute.name);
            }

            return;
        }

        if (attributeName === 'rel' && element.getAttribute('target') !== '_blank') {
            element.removeAttribute(attribute.name);
        }
    });

    if (tagName === 'input') {
        const inputType = (element.getAttribute('type') || '').toLowerCase();

        if (inputType !== 'checkbox') {
            element.remove();
            return false;
        }

        element.setAttribute('disabled', '');
    }

    if (tagName === 'a') {
        if (!element.getAttribute('href')) {
            element.removeAttribute('target');
            element.removeAttribute('rel');
            return true;
        }

        if (element.getAttribute('target') === '_blank') {
            element.setAttribute('rel', 'noopener noreferrer nofollow');
        }
    }

    return true;
};

const sanitizeNode = (node) => {
    if (node.nodeType === COMMENT_NODE) {
        node.remove();
        return;
    }

    if (node.nodeType !== ELEMENT_NODE) {
        return;
    }

    const tagName = node.tagName.toLowerCase();

    if (DROP_CONTENT_TAGS.has(tagName)) {
        node.remove();
        return;
    }

    if (!ALLOWED_TAGS.has(tagName)) {
        unwrapNode(node);
        return;
    }

    if (!sanitizeAttributes(node)) {
        return;
    }

    Array.from(node.childNodes).forEach(sanitizeNode);
};

export const sanitizeContentHtml = (html = '') => {
    if (!html || typeof DOMParser === 'undefined') {
        return html;
    }

    const parser = new DOMParser();
    const doc = parser.parseFromString(`<div data-rich-text-root>${html}</div>`, 'text/html');
    const root = doc.body.querySelector('[data-rich-text-root]');

    if (!root) {
        return html;
    }

    Array.from(root.childNodes).forEach(sanitizeNode);

    return root.innerHTML;
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

const findTaskMarkerTextNode = (node) => {
    for (const child of Array.from(node.childNodes || [])) {
        if (child.nodeType === TEXT_NODE) {
            if (child.textContent?.trim()) {
                return child;
            }

            continue;
        }

        if (child.nodeType !== ELEMENT_NODE) {
            continue;
        }

        const tagName = child.tagName.toLowerCase();

        if (tagName === 'ul' || tagName === 'ol') {
            return null;
        }

        const nestedTextNode = findTaskMarkerTextNode(child);

        if (nestedTextNode) {
            return nestedTextNode;
        }

        if (child.textContent?.trim()) {
            return null;
        }
    }

    return null;
};

const resolveTaskItemMarker = (listItem) => {
    const textNode = findTaskMarkerTextNode(listItem);

    if (!textNode) {
        return null;
    }

    const match = textNode.textContent?.match(TASK_LIST_MARKER_RE);

    if (!match) {
        return null;
    }

    return {
        checked: match[1].toLowerCase() === 'x',
        textNode
    };
};

const buildTaskItemCheckbox = (doc, checked) => {
    const label = doc.createElement('label');
    const checkbox = doc.createElement('input');
    const marker = doc.createElement('span');

    checkbox.type = 'checkbox';
    checkbox.setAttribute('disabled', '');

    if (checked) {
        checkbox.setAttribute('checked', 'checked');
    }

    label.append(checkbox, marker);

    return label;
};

const isTaskItemBlockNode = (node) => (
    node.nodeType === ELEMENT_NODE
    && TASK_ITEM_BLOCK_TAGS.has(node.tagName.toLowerCase())
);

const normalizeTaskItemContent = (doc, content) => {
    let paragraph = null;

    Array.from(content.childNodes).forEach((node) => {
        if (node.nodeType === TEXT_NODE && !node.textContent?.trim()) {
            node.remove();
            return;
        }

        if (isTaskItemBlockNode(node)) {
            paragraph = null;
            return;
        }

        if (!paragraph) {
            paragraph = doc.createElement('p');
            content.insertBefore(paragraph, node);
        }

        paragraph.appendChild(node);
    });

    const firstContentNode = Array.from(content.childNodes).find((node) => (
        node.nodeType !== TEXT_NODE || node.textContent?.trim()
    ));

    if (!firstContentNode || !isTaskItemBlockNode(firstContentNode) || firstContentNode.tagName.toLowerCase() !== 'p') {
        content.insertBefore(doc.createElement('p'), content.firstChild);
    }
};

const enhanceTaskLists = (doc, root) => {
    root.querySelectorAll('ul').forEach((list) => {
        if (list.getAttribute('data-type') === 'taskList') {
            return;
        }

        const listItems = Array.from(list.children).filter((child) => child.tagName === 'LI');

        if (!listItems.length || listItems.length !== list.children.length) {
            return;
        }

        const taskMarkers = listItems.map(resolveTaskItemMarker);

        if (taskMarkers.some((marker) => !marker)) {
            return;
        }

        list.setAttribute('data-type', 'taskList');

        listItems.forEach((listItem, index) => {
            const taskMarker = taskMarkers[index];

            taskMarker.textNode.textContent = taskMarker.textNode.textContent.replace(TASK_LIST_MARKER_RE, '');
            listItem.setAttribute('data-type', 'taskItem');
            listItem.setAttribute('data-checked', String(taskMarker.checked));

            const content = doc.createElement('div');

            while (listItem.firstChild) {
                content.appendChild(listItem.firstChild);
            }

            normalizeTaskItemContent(doc, content);
            listItem.append(buildTaskItemCheckbox(doc, taskMarker.checked), content);
        });
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

export const enhanceContentHtml = (html = '', options = {}) => {
    const { target = 'viewer' } = options;

    if (!html || typeof DOMParser === 'undefined') {
        return html;
    }

    const parser = new DOMParser();
    const doc = parser.parseFromString(`<div data-rich-text-root>${html}</div>`, 'text/html');
    const root = doc.body.querySelector('[data-rich-text-root]');

    if (!root) {
        return html;
    }

    enhanceTaskLists(doc, root);

    if (target !== 'viewer') {
        return root.innerHTML;
    }

    normalizeHeadingAnchors(root);
    enhanceCodeBlocks(doc, root);
    enhanceTables(doc, root);
    enhanceMedia(root);

    return root.innerHTML;
};

export const buildRichTextHtml = (content = '', sourceFormat = 'html', options = {}) => {
    if (!content) {
        return '';
    }

    const normalizedHtml = sourceFormat === 'markdown'
        ? parseMarkdownToHtml(content)
        : content;

    return enhanceContentHtml(sanitizeContentHtml(normalizedHtml), options);
};

export const buildRichTextEditorHtml = (content = '', sourceFormat = 'html') => buildRichTextHtml(content, sourceFormat, {
    target: 'editor'
});
