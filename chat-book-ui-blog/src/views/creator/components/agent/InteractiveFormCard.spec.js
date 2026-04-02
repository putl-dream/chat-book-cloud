// @vitest-environment jsdom
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import InteractiveFormCard from './InteractiveFormCard.vue';

const formMessage = {
    id: 'assistant-form',
    role: 'assistant',
    messageType: 'interactive_form',
    content: '先确认两个方向',
    payload: {
        formId: 'brief_form',
        title: '补充基础信息',
        description: '回答后我会继续生成建议',
        questions: [
            {
                id: 'audience',
                label: '目标读者',
                type: 'single_choice',
                required: true,
                options: [
                    { label: 'Java 开发者', value: 'Java 开发者' },
                    { label: '技术负责人', value: '技术负责人' }
                ]
            },
            {
                id: 'format',
                label: '内容形式',
                type: 'single_choice',
                required: true,
                options: [
                    { label: '技术博客文章', value: '技术博客文章' },
                    { label: '播客脚本', value: '播客脚本' }
                ]
            }
        ]
    }
};

const global = {
    stubs: {
        ElInput: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
        }
    }
};

describe('InteractiveFormCard', () => {
    it('advances on single choice and emits completed answers on the last question', async () => {
        const wrapper = mount(InteractiveFormCard, {
            props: {
                message: formMessage
            },
            global
        });

        const firstRoundButtons = wrapper.findAll('.interactive-card__chip');
        await firstRoundButtons[0].trigger('click');
        expect(wrapper.text()).toContain('内容形式');

        const secondRoundButtons = wrapper.findAll('.interactive-card__chip');
        await secondRoundButtons[0].trigger('click');

        expect(wrapper.emitted('submit')).toEqual([
            [{
                audience: 'Java 开发者',
                format: '技术博客文章'
            }]
        ]);
    });

    it('renders summary mode and allows switching back to edit mode', async () => {
        const wrapper = mount(InteractiveFormCard, {
            props: {
                message: {
                    ...formMessage,
                    interactionResponse: {
                        formId: 'brief_form',
                        answers: [
                            { questionId: 'audience', questionLabel: '目标读者', questionType: 'single_choice', value: 'Java 开发者' },
                            { questionId: 'format', questionLabel: '内容形式', questionType: 'single_choice', value: '技术博客文章' }
                        ]
                    }
                }
            },
            global
        });

        expect(wrapper.text()).toContain('已完成');
        expect(wrapper.text()).toContain('Java 开发者');

        await wrapper.find('.interactive-card__ghost-btn').trigger('click');

        expect(wrapper.text()).toContain('结构化提问');
        expect(wrapper.text()).toContain('目标读者');
    });
});
