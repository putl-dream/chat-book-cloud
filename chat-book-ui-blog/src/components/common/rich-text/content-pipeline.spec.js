/**
 * @vitest-environment jsdom
 */
import { describe, expect, it } from 'vitest';

import { buildRichTextHtml, createHeadingId } from './content-pipeline.js';

describe('content-pipeline', () => {
    it('builds markdown into shared viewer html', () => {
        const html = buildRichTextHtml([
            '# 标题',
            '',
            '```js',
            'const value = 1;',
            '```'
        ].join('\n'), 'markdown');

        expect(html).toContain('id="标题"');
        expect(html).toContain('class="code-block-wrapper"');
        expect(html).toContain('class="code-lang">js</span>');
        expect(html).toContain('class="copy-btn"');
    });

    it('wraps html tables for horizontal scrolling', () => {
        const html = buildRichTextHtml('<h2>表格</h2><table><tbody><tr><td>数据</td></tr></tbody></table>', 'html');

        expect(html).toContain('id="表格"');
        expect(html).toContain('class="table-scroll"');
        expect(html).toContain('<table>');
    });

    it('creates stable ids for duplicate headings', () => {
        const occurrenceMap = new Map();

        expect(createHeadingId('重复标题', occurrenceMap)).toBe('重复标题');
        expect(createHeadingId('重复标题', occurrenceMap)).toBe('重复标题-2');
    });
});
