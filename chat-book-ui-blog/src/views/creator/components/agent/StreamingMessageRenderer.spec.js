// @vitest-environment jsdom
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';

import StreamingMessageRenderer from './StreamingMessageRenderer.vue';

describe('StreamingMessageRenderer', () => {
    it('renders streaming text as plain text instead of interpreting html tags', () => {
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '第一行\n<script>alert(1)</script>\n第二行'
            }
        });

        expect(wrapper.text()).toContain('第一行');
        expect(wrapper.text()).toContain('<script>alert(1)</script>');
        expect(wrapper.text()).toContain('第二行');
        expect(wrapper.find('script').exists()).toBe(false);
    });

    it('supports lightweight markdown affordances while streaming', () => {
        const wrapper = mount(StreamingMessageRenderer, {
            props: {
                content: '这是 `inline` 示例\n\n- 第一项\n- 第二项\n\n```js\nconst a = 1;\n```'
            }
        });

        expect(wrapper.find('code').text()).toContain('inline');
        expect(wrapper.findAll('li')).toHaveLength(2);
        expect(wrapper.find('pre').text()).toContain('const a = 1;');
    });
});
