/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';

import RichTextViewer from './RichTextViewer.vue';

vi.mock('element-plus', () => ({
    ElMessage: {
        error: vi.fn(),
        success: vi.fn()
    }
}));

describe('RichTextViewer', () => {
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
});
