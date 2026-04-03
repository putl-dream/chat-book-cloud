/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ElMessage } from 'element-plus';

import RichTextViewer from './RichTextViewer.vue';

vi.mock('element-plus', () => ({
    ElMessage: {
        error: vi.fn(),
        success: vi.fn()
    }
}));

const CODE_BLOCK_HTML = `
    <div class="code-block-wrapper">
        <div class="code-header">
            <span class="code-lang">js</span>
            <button type="button" class="copy-btn">复制</button>
        </div>
        <pre><code>const value = 1;</code></pre>
    </div>
`;

const originalClipboard = Object.getOwnPropertyDescriptor(window.navigator, 'clipboard');
const originalExecCommand = document.execCommand;

describe('RichTextViewer', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        Object.defineProperty(window.navigator, 'clipboard', {
            configurable: true,
            value: undefined
        });
        document.execCommand = undefined;
    });

    afterEach(() => {
        if (originalClipboard) {
            Object.defineProperty(window.navigator, 'clipboard', originalClipboard);
        } else {
            Object.defineProperty(window.navigator, 'clipboard', {
                configurable: true,
                value: undefined
            });
        }

        document.execCommand = originalExecCommand;
    });

    it('renders readonly html with the requested variant hook', () => {
        const wrapper = mount(RichTextViewer, {
            props: {
                as: 'article',
                variant: 'chat',
                theme: 'reading',
                html: '<p>AI 回复</p>'
            }
        });

        expect(wrapper.element.tagName).toBe('ARTICLE');
        expect(wrapper.attributes('data-content-variant')).toBe('chat');
        expect(wrapper.attributes('data-content-style')).toBe('reading');
        expect(wrapper.find('.rich-text-viewer__body').html()).toContain('<p>AI 回复</p>');
    });

    it('falls back to legacy content prop for compatibility callers', () => {
        const wrapper = mount(RichTextViewer, {
            props: {
                content: '<blockquote>兼容层内容</blockquote>'
            }
        });

        expect(wrapper.find('.rich-text-viewer__body').html()).toContain('<blockquote>兼容层内容</blockquote>');
    });

    it('copies code blocks with the Clipboard API when available', async () => {
        const writeText = vi.fn().mockResolvedValue(undefined);

        Object.defineProperty(window.navigator, 'clipboard', {
            configurable: true,
            value: { writeText }
        });

        const wrapper = mount(RichTextViewer, {
            props: {
                html: CODE_BLOCK_HTML
            }
        });

        const button = wrapper.get('.copy-btn');
        await button.trigger('click');

        expect(writeText).toHaveBeenCalledWith('const value = 1;');
        expect(button.text()).toBe('已复制');
        expect(button.classes()).toContain('copied');
        expect(ElMessage.success).toHaveBeenCalledWith('复制代码成功');
    });

    it('falls back to execCommand when the Clipboard API write fails', async () => {
        const writeText = vi.fn().mockRejectedValue(new Error('NotAllowedError'));
        const execCommand = vi.fn(() => true);

        Object.defineProperty(window.navigator, 'clipboard', {
            configurable: true,
            value: { writeText }
        });
        document.execCommand = execCommand;

        const wrapper = mount(RichTextViewer, {
            props: {
                html: CODE_BLOCK_HTML
            }
        });

        const button = wrapper.get('.copy-btn');
        await button.trigger('click');

        expect(writeText).toHaveBeenCalledWith('const value = 1;');
        expect(execCommand).toHaveBeenCalledWith('copy');
        expect(button.text()).toBe('已复制');
        expect(ElMessage.success).toHaveBeenCalledWith('复制代码成功');
    });
});
