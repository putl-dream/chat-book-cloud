// @vitest-environment jsdom

import { mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';

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
    interruptingChat: false,
    latestUserEditableMessage: '',
    visibleMessages: [],
    sendMessage: vi.fn(),
    interruptChat: vi.fn(),
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
    beforeEach(() => {
        mockStore.currentSceneLabel = '讨论共创';
        mockStore.nextSceneLabel = '首稿生成';
        mockStore.currentSceneSubtitle = '围绕主题逐步收敛';
        mockStore.switchReason = '';
        mockStore.generatingDraft = false;
        mockStore.hasMessages = true;
        mockStore.chatting = false;
        mockStore.loadingSession = false;
        mockStore.hasPendingInteractiveForm = false;
        mockStore.isDraftReady = true;
        mockStore.currentScene = 'DISCUSS';
        mockStore.generateButtonLabel = '进入首稿生成';
        mockStore.sceneFooterHint = '继续讨论';
        mockStore.creatingSession = false;
        mockStore.interruptingChat = false;
        mockStore.latestUserEditableMessage = '';
        mockStore.visibleMessages = [];
        vi.clearAllMocks();
    });

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

    it('keeps textarea editable while assistant is replying', () => {
        mockStore.chatting = true;
        mockStore.latestUserEditableMessage = '';
        mockStore.visibleMessages = [];

        const wrapper = mount(ChatController, {
            global: {
                stubs: {
                    'el-button': { template: '<button><slot /></button>' },
                    'el-input': {
                        props: ['modelValue', 'disabled'],
                        template: '<textarea class="chat-input" :disabled="disabled"></textarea>'
                    },
                    'el-icon': { template: '<i><slot /></i>' },
                    'el-skeleton': { template: '<div class="skeleton"></div>' }
                }
            }
        });

        expect(wrapper.find('.chat-input').attributes('disabled')).toBeUndefined();
    });

    it('restores the last user message before interrupting', async () => {
        mockStore.chatting = true;
        mockStore.latestUserEditableMessage = '这条消息还没写完';
        mockStore.visibleMessages = [];

        const wrapper = mount(ChatController, {
            global: {
                stubs: {
                    'el-button': {
                        props: ['text', 'loading', 'disabled'],
                        template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
                    },
                    'el-input': {
                        props: ['modelValue', 'disabled'],
                        emits: ['update:modelValue'],
                        template: '<textarea class="chat-input" :disabled="disabled" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
                    },
                    'el-icon': { template: '<i><slot /></i>' },
                    'el-skeleton': { template: '<div class="skeleton"></div>' }
                }
            }
        });

        await wrapper.findAll('button')[1].trigger('click');

        expect(wrapper.find('.chat-input').element.value).toBe('这条消息还没写完');
        expect(mockStore.interruptChat).toHaveBeenCalled();
    });
});
