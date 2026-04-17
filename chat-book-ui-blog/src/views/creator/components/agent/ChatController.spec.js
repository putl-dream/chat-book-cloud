// @vitest-environment jsdom

import { mount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';

import ChatController from './ChatController.vue';

const mockStore = vi.hoisted(() => ({
    currentSceneLabel: '讨论共创',
    nextSceneLabel: '首稿生成',
    currentSceneSubtitle: '围绕主题逐步收敛',
    switchReason: '',
    generatingDraft: false,
    hasMessages: true,
    chatting: false,
    loadingSession: false,
    hasPendingInteractiveForm: false,
    isDraftReady: true,
    currentScene: 'DISCUSS',
    generateButtonLabel: '进入首稿生成',
    sceneFooterHint: '继续讨论',
    creatingSession: false,
    visibleMessages: [],
    sendMessage: vi.fn(),
    createDraftFromSession: vi.fn(),
    submitInteractiveForm: vi.fn()
}));

vi.mock('@/store/agentStudio.js', () => ({
    useAgentStudioStore: () => mockStore
}));

vi.mock('@/components/common/rich-text/content-pipeline.js', () => ({
    buildRichTextHtml: (content) => content
}));

vi.mock('@/components/common/rich-text/RichTextViewer.vue', () => ({
    default: {
        props: ['html'],
        template: '<div class="rich-text-viewer">{{ html }}</div>'
    }
}));

vi.mock('@/views/creator/components/agent/StreamingMessageRenderer.vue', () => ({
    default: {
        props: ['content'],
        template: '<div class="streaming-renderer">{{ content }}</div>'
    }
}));

vi.mock('@/views/creator/components/agent/InteractiveFormCard.vue', () => ({
    default: {
        props: ['message'],
        template: '<div class="interactive-form-card">{{ message.content }}</div>'
    }
}));

describe('ChatController', () => {
    it('renders streaming assistant messages with streaming renderer only', () => {
        mockStore.visibleMessages = [{
            id: 'assistant-1',
            role: 'assistant',
            messageType: 'text',
            content: '正在逐字增长',
            previewText: '正在逐字增长',
            streaming: true
        }];

        const wrapper = mount(ChatController, {
            global: {
                stubs: {
                    'el-button': { template: '<button><slot /></button>' },
                    'el-input': { template: '<textarea />' },
                    'el-icon': { template: '<i><slot /></i>' },
                    'el-skeleton': { template: '<div class="skeleton"></div>' }
                }
            }
        });

        expect(wrapper.find('.streaming-renderer').text()).toContain('正在逐字增长');
        expect(wrapper.find('.rich-text-viewer').exists()).toBe(false);
    });

    it('keeps interactive form messages on the final card renderer', () => {
        mockStore.visibleMessages = [{
            id: 'assistant-form-1',
            role: 'assistant',
            messageType: 'interactive_form',
            content: '请补充信息',
            payload: {
                formId: 'form-1',
                questions: [{ id: 'q1', label: '目标读者', type: 'single_choice', options: [{ label: '开发者', value: 'dev' }] }]
            },
            streaming: false
        }];

        const wrapper = mount(ChatController, {
            global: {
                stubs: {
                    'el-button': { template: '<button><slot /></button>' },
                    'el-input': { template: '<textarea />' },
                    'el-icon': { template: '<i><slot /></i>' },
                    'el-skeleton': { template: '<div class="skeleton"></div>' }
                }
            }
        });

        expect(wrapper.find('.interactive-form-card').exists()).toBe(true);
        expect(wrapper.find('.streaming-renderer').exists()).toBe(false);
        expect(wrapper.find('.rich-text-viewer').exists()).toBe(false);
    });
});
