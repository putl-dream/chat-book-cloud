/**
 * @vitest-environment jsdom
 */
import { describe, expect, it } from 'vitest';

import {
    buildMarkdownPasteHtml,
    markdownTaskListInputRegex,
    shouldHandleMarkdownPaste
} from './markdown-shortcuts.js';

describe('markdown-shortcuts', () => {
    it('detects plain text markdown pastes and ignores html/file clipboard payloads', () => {
        expect(shouldHandleMarkdownPaste({
            text: '# 标题\n\n段落内容'
        })).toBe(true);

        expect(shouldHandleMarkdownPaste({
            text: '- [ ] 待办事项\n- [x] 已完成事项'
        })).toBe(true);

        expect(shouldHandleMarkdownPaste({
            text: '普通文本内容'
        })).toBe(false);

        expect(shouldHandleMarkdownPaste({
            text: '# 标题',
            html: '<h1>标题</h1>'
        })).toBe(false);

        expect(shouldHandleMarkdownPaste({
            text: '- [ ] 待办事项',
            hasFiles: true
        })).toBe(false);
    });

    it('builds editor-friendly html for markdown paste import', () => {
        const html = buildMarkdownPasteHtml([
            '# 标题',
            '',
            '- [ ] 待办事项',
            '',
            '```js',
            'const value = 1;',
            '```'
        ].join('\n'));

        expect(html).toContain('<h1>标题</h1>');
        expect(html).toContain('data-type="taskList"');
        expect(html).toContain('<pre><code class="language-js">const value = 1;');
        expect(html).not.toContain('code-block-wrapper');
        expect(html).not.toContain('copy-btn');
    });

    it('matches task list markdown shortcuts', () => {
        expect('[ ] ').toMatch(markdownTaskListInputRegex);
        expect('[x] ').toMatch(markdownTaskListInputRegex);
        expect('- [ ] ').toMatch(markdownTaskListInputRegex);
        expect('* [x] ').toMatch(markdownTaskListInputRegex);
        expect('- 普通列表 ').not.toMatch(markdownTaskListInputRegex);
    });
});
