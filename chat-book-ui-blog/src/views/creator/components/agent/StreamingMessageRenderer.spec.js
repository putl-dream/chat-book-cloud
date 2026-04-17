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
        expect(wrapper.html()).not.toContain('<script>alert(1)</script></span>');
    });
});
