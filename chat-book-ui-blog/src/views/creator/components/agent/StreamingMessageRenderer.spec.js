// @vitest-environment jsdom
import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import { afterEach, describe, expect, it, vi } from 'vitest';

import StreamingMessageRenderer from './StreamingMessageRenderer.vue';

afterEach(() => {
    vi.useRealTimers();
});

describe('StreamingMessageRenderer', () => {
    it('sanitizes raw html while rendering streaming markdown', () => {
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '第一行\n<script>alert(1)</script>\n第二行'
            }
        });

        expect(wrapper.text()).toContain('第一行');
        expect(wrapper.text()).toContain('第二行');
        expect(wrapper.find('script').exists()).toBe(false);
    });

    it('renders markdown directly during streaming', () => {
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '这是 **强调** 和 `inline` 示例\n\n- 第一项\n- 第二项\n\n```js\nconst a = 1;\n```',
                streaming: true
            }
        });

        expect(wrapper.find('strong').text()).toContain('强调');
        expect(wrapper.find('code').text()).toContain('inline');
        expect(wrapper.findAll('li')).toHaveLength(2);
        expect(wrapper.find('pre').text()).toContain('const a = 1;');
    });

    it('degrades unfinished code fences into stable plain code blocks', () => {
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '```js\nconst a = 1;',
                streaming: true
            }
        });

        expect(wrapper.find('pre').exists()).toBe(true);
        expect(wrapper.find('pre').classes()).not.toContain('hljs');
        expect(wrapper.text()).toContain('const a = 1;');
    });

    it('batches rapid content updates before rerendering markdown', async () => {
        vi.useFakeTimers();
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '第一段',
                streaming: true
            }
        });

        expect(wrapper.text()).toContain('第一段');

        await wrapper.setProps({
            content: '第一段\n\n第二段'
        });
        await nextTick();

        expect(wrapper.text()).not.toContain('第二段');

        vi.advanceTimersByTime(60);
        await nextTick();

        expect(wrapper.text()).toContain('第二段');
    });

    it('flushes immediately when streaming ends so the final renderer does not lag behind', async () => {
        vi.useFakeTimers();
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '第一段',
                streaming: true
            }
        });

        await wrapper.setProps({
            content: '第一段\n\n第二段',
            streaming: true
        });
        await nextTick();

        expect(wrapper.text()).not.toContain('第二段');

        await wrapper.setProps({
            content: '第一段\n\n第二段',
            streaming: false
        });
        await nextTick();

        expect(wrapper.text()).toContain('第二段');
    });
});
