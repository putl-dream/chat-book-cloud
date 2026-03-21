/**
 * @vitest-environment jsdom
 */
import { describe, expect, it } from 'vitest';

import { buildRichTextEditorHtml, buildRichTextHtml, createHeadingId, sanitizeContentHtml } from './content-pipeline.js';

describe('content-pipeline', () => {
    it('builds markdown into shared viewer html for article content', () => {
        const html = buildRichTextHtml([
            '# 标题',
            '',
            '正文段落',
            '',
            '```js',
            'const value = 1;',
            '```'
        ].join('\n'), 'markdown');

        expect(html).toContain('id="标题"');
        expect(html).toContain('<p>正文段落</p>');
        expect(html).toContain('class="code-block-wrapper"');
        expect(html).toContain('class="code-lang">js</span>');
        expect(html).toContain('class="copy-btn"');
    });

    it('wraps tables and enhances images for reading mode', () => {
        const html = buildRichTextHtml(
            '<h2>表格</h2><table><tbody><tr><td>数据</td></tr></tbody></table><img src="https://cdn.example.com/demo.png" alt="示例">',
            'html'
        );

        expect(html).toContain('id="表格"');
        expect(html).toContain('class="table-scroll"');
        expect(html).toContain('<table>');
        expect(html).toContain('loading="lazy"');
        expect(html).toContain('decoding="async"');
    });

    it('converts markdown task lists into shared readonly task list html', () => {
        const html = buildRichTextHtml([
            '- [ ] 待办事项',
            '- [x] 已完成事项'
        ].join('\n'), 'markdown');

        expect(html).toContain('data-type="taskList"');
        expect(html).toContain('data-type="taskItem"');
        expect(html).toContain('data-checked="false"');
        expect(html).toContain('data-checked="true"');
        expect(html).toContain('<input type="checkbox" disabled="">');
        expect(html).toContain('<input type="checkbox" disabled="" checked="checked">');
        expect(html).toContain('<p>待办事项</p>');
        expect(html).toContain('<p>已完成事项</p>');
    });

    it('creates stable ids for duplicate headings', () => {
        const occurrenceMap = new Map();

        expect(createHeadingId('重复标题', occurrenceMap)).toBe('重复标题');
        expect(createHeadingId('重复标题', occurrenceMap)).toBe('重复标题-2');
    });

    it('sanitizes unsafe html while preserving safe formatting hooks', () => {
        const html = sanitizeContentHtml([
            '<p onclick="alert(1)" style="color: #ff0000; background-image: url(javascript:alert(1)); text-align: center">内容</p>',
            '<script>alert(1)</script>',
            '<a href="javascript:alert(1)" target="_blank">危险链接</a>',
            '<a href="https://safe.example.dev/post" target="_blank" onclick="alert(1)">安全链接</a>',
            '<img src="javascript:alert(1)" onerror="alert(1)" alt="xss">'
        ].join(''));

        expect(html).not.toContain('<script');
        expect(html).not.toContain('onclick=');
        expect(html).not.toContain('javascript:alert');
        expect(html).toContain('style="color: #ff0000; text-align: center"');
        expect(html).toContain('<a>危险链接</a>');
        expect(html).toContain('href="https://safe.example.dev/post"');
        expect(html).toContain('rel="noopener noreferrer nofollow"');
        expect(html).toContain('<img alt="xss">');
    });

    it('sanitizes markdown raw html before shared viewer enhancement', () => {
        const html = buildRichTextHtml('安全内容<script>alert(1)</script><img src="x" onerror="alert(1)">', 'markdown');

        expect(html).not.toContain('<script');
        expect(html).not.toContain('onerror=');
        expect(html).toContain('<p>安全内容<img src="x"');
        expect(html).toContain('loading="lazy"');
    });

    it('preserves task list semantics for readonly rendering', () => {
        const html = buildRichTextHtml([
            '<ul data-type="taskList">',
            '<li data-type="taskItem" data-checked="true">',
            '<label><input type="checkbox" checked><span></span></label>',
            '<div><p>待办事项</p></div>',
            '</li>',
            '</ul>'
        ].join(''), 'html');

        expect(html).toContain('data-type="taskList"');
        expect(html).toContain('data-type="taskItem"');
        expect(html).toContain('<input type="checkbox" checked="" disabled="">');
        expect(html).toContain('<p>待办事项</p>');
    });

    it('builds editor import html without readonly viewer wrappers', () => {
        const html = buildRichTextEditorHtml([
            '# 标题',
            '',
            '- [ ] 待办事项',
            '',
            '```js',
            'const value = 1;',
            '```'
        ].join('\n'), 'markdown');

        expect(html).toContain('<h1>标题</h1>');
        expect(html).not.toContain('id="标题"');
        expect(html).toContain('data-type="taskList"');
        expect(html).toContain('<pre><code class="language-js">const value = 1;');
        expect(html).not.toContain('code-block-wrapper');
        expect(html).not.toContain('copy-btn');
    });
});
