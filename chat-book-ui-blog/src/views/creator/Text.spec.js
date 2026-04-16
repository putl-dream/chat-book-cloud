// @vitest-environment jsdom

import { flushPromises, mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Text from './Text.vue';

const {
    mockRouter,
    mockLoadAgentDraftImport,
    mockClearAgentDraftImport,
    mockLoadAgentGenerationIntent,
    mockClearAgentGenerationIntent,
    mockSocketSend,
    mockSocketClose,
    mockSetContent,
    mockEditorRef
} = vi.hoisted(() => ({
    mockRouter: {
        push: vi.fn(),
        replace: vi.fn()
    },
    mockLoadAgentDraftImport: vi.fn(() => null),
    mockClearAgentDraftImport: vi.fn(),
    mockLoadAgentGenerationIntent: vi.fn(() => null),
    mockClearAgentGenerationIntent: vi.fn(),
    mockSocketSend: vi.fn(() => true),
    mockSocketClose: vi.fn(),
    mockSetContent: vi.fn(),
    mockEditorRef: {
        __v_isRef: true,
        value: {
            getHTML: () => '',
            destroy: vi.fn(),
            commands: { setContent: vi.fn() },
            storage: { characterCount: { characters: () => 0 } },
            setEditable: vi.fn()
        }
    }
}));

mockEditorRef.value.commands.setContent = mockSetContent;

// Mocks
vi.mock('vue-router', () => ({
    useRoute: () => ({ params: { id: null } }),
    useRouter: () => mockRouter,
    onBeforeRouteLeave: vi.fn(),
    onBeforeRouteUpdate: vi.fn(),
    createRouter: () => ({
        push: vi.fn(),
        replace: vi.fn(),
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        currentRoute: { value: { name: 'Write' } },
        isReady: () => Promise.resolve()
    }),
    createWebHistory: vi.fn()
}));

vi.mock('@/utils/websocket.js', () => ({
    default: class SocketService {
        onOpen() {}
        onClose() {}
        onError() {}
        on() {}
        connect() {}
        send(type, data) { return mockSocketSend(type, data); }
        sendWithAck() { return Promise.resolve({}); }
        isConnected() { return true; }
        close() { mockSocketClose(); }
    },
    formatWsUrl: () => 'ws://localhost'
}));

vi.mock('@/views/article/_domain/article.js', () => ({
    publishArticle: vi.fn(),
    saveDraftArticle: vi.fn(),
    uploadFile: vi.fn()
}));

vi.mock('@/views/creator/_domain/agent.js', () => ({
    buildStreamingDraftPreview: vi.fn(() => null),
    loadAgentDraftImport: mockLoadAgentDraftImport,
    clearAgentDraftImport: mockClearAgentDraftImport,
    loadAgentGenerationIntent: mockLoadAgentGenerationIntent,
    clearAgentGenerationIntent: mockClearAgentGenerationIntent,
    extractArticleSummary: vi.fn(() => Promise.resolve({ summary: 'AI 摘要' }))
}));

// Mock Element Plus and Tiptap dependencies to prevent mount errors
vi.mock('element-plus', () => {
    const ElMessage = vi.fn();
    ElMessage.success = vi.fn();
    ElMessage.warning = vi.fn();
    ElMessage.error = vi.fn();

    return {
    ElMessage,
    ElMessageBox: { confirm: vi.fn() },
    ElDialog: { template: '<div><slot></slot><slot name="footer"></slot></div>' },
    ElForm: { template: '<div><slot></slot></div>' },
    ElFormItem: { template: '<div><slot></slot></div>' },
    ElSelect: { template: '<select><slot></slot></select>' },
    ElOption: { template: '<option></option>' },
    ElInput: { template: '<input />' },
    ElCheckboxGroup: { template: '<div><slot></slot></div>' },
    ElCheckbox: { template: '<label><slot></slot></label>' },
    ElRadioGroup: { template: '<div><slot></slot></div>' },
    ElRadio: { template: '<label><slot></slot></label>' },
    ElTag: { template: '<span><slot></slot></span>' },
    ElUpload: { template: '<div><slot></slot></div>' },
    ElButton: { template: '<button><slot></slot></button>' },
    ElIcon: { template: '<i><slot></slot></i>' },
    ElText: { template: '<span><slot></slot></span>' }
    };
});

vi.mock('@element-plus/icons-vue', () => ({
    Plus: { template: '<svg></svg>' },
    Close: { template: '<svg></svg>' },
    Setting: { template: '<svg></svg>' },
    DocumentChecked: { template: '<svg></svg>' },
    Promotion: { template: '<svg></svg>' },
    MagicStick: { template: '<svg></svg>' }
}));

vi.mock('@tiptap/vue-3', () => ({
    useEditor: () => mockEditorRef,
    EditorContent: { template: '<div></div>' }
}));

// Mock custom components
vi.mock('@/views/creator/components/CreativeHeader.vue', () => ({ default: { template: '<header></header>' } }));
vi.mock('@/views/creator/components/TiptapToolbar.vue', () => ({ default: { template: '<div></div>' } }));
vi.mock('@/views/creator/components/EditorAiPanel.vue', () => ({ default: { template: '<div class="editor-ai-panel"></div>' } }));
vi.mock('@/views/creator/components/PublishDialog.vue', () => ({ default: { template: '<div class="publish-dialog"></div>' } }));
vi.mock('@/views/article/components/ArticleToc.vue', () => ({ default: { template: '<div></div>' } }));
vi.mock('@/components/common/rich-text/RichTextEditor.vue', () => ({ default: { template: '<div class="rich-text-editor"></div>' } }));
vi.mock('@/views/article/_domain/tag.js', () => ({
    getHotAuthorTags: vi.fn(() => Promise.resolve([])),
    searchAuthorTags: vi.fn(() => Promise.resolve([]))
}));

describe('Text.vue Three-Column Layout', () => {
    let wrapper;

    beforeEach(() => {
        vi.clearAllMocks();
        window.localStorage.clear();
        window.sessionStorage.clear();
        // Reset window innerWidth
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1200 });
        wrapper = mount(Text);
    });

    it('Status 1: Left open, Right closed -> L 20%, Content 80%', async () => {
        // Initial state is left open, right closed
        const left = wrapper.find('.layout-left');
        const content = wrapper.find('.layout-content');
        const right = wrapper.find('.layout-right');

        expect(left.isVisible()).toBe(true);
        expect(right.isVisible()).toBe(false);
        expect(left.element.style.width).toBe('20%');
        expect(content.element.style.width).toBe('80%');
    });

    it('Status 2: Left closed, Right closed -> Content 100%', async () => {
        // Close left
        await wrapper.vm.toggleLeft();
        
        const left = wrapper.find('.layout-left');
        const content = wrapper.find('.layout-content');
        const right = wrapper.find('.layout-right');

        expect(left.isVisible()).toBe(false);
        expect(right.isVisible()).toBe(false);
        expect(content.element.style.width).toBe('100%');
    });

    it('Status 3: Left closed, Right open -> R 20%, Content 80%', async () => {
        // Close left, open right
        await wrapper.vm.toggleLeft();
        await wrapper.vm.toggleRight();

        const left = wrapper.find('.layout-left');
        const content = wrapper.find('.layout-content');
        const right = wrapper.find('.layout-right');

        expect(left.isVisible()).toBe(false);
        expect(right.isVisible()).toBe(true);
        expect(right.element.style.width).toBe('20%');
        expect(content.element.style.width).toBe('80%');
    });

    it('Status 4: Left open, Right open -> L 20%, R 20%, Content 60%', async () => {
        // Open right (left is already open initially)
        await wrapper.vm.toggleRight();

        const left = wrapper.find('.layout-left');
        const content = wrapper.find('.layout-content');
        const right = wrapper.find('.layout-right');

        expect(left.isVisible()).toBe(true);
        expect(right.isVisible()).toBe(true);
        expect(left.element.style.width).toBe('20%');
        expect(right.element.style.width).toBe('20%');
        expect(content.element.style.width).toBe('60%');
    });

    it('Responsive: <= 768px forces right to be closed and disabled', async () => {
        // Simulate mobile
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 500 });
        window.dispatchEvent(new Event('resize'));
        
        await wrapper.vm.$nextTick();

        // Right should be closed
        expect(wrapper.vm.layoutState.rightOpen).toBe(false);
        expect(wrapper.vm.layoutState.isMobile).toBe(true);

        // Trying to open right should do nothing
        await wrapper.vm.toggleRight();
        expect(wrapper.vm.layoutState.rightOpen).toBe(false);
    });

    it('imports agent draft without requiring local userInfo', async () => {
        wrapper.unmount();
        mockLoadAgentDraftImport.mockReturnValue({
            title: 'Agent 首稿',
            content: '<p>导入内容</p>',
            abstractText: '摘要'
        });

        wrapper = mount(Text);
        await flushPromises();

        expect(mockLoadAgentDraftImport).toHaveBeenCalled();
        expect(mockClearAgentDraftImport).toHaveBeenCalledTimes(1);
        expect(mockSetContent).toHaveBeenCalledWith('<p>导入内容</p>', false);
    });

    it('shows generation banner when entering from agent discussion flow', async () => {
        wrapper.unmount();
        mockLoadAgentGenerationIntent.mockReturnValue({ sessionId: 12 });

        wrapper = mount(Text);
        await flushPromises();

        expect(mockClearAgentGenerationIntent).toHaveBeenCalled();
        expect(wrapper.find('.ai-generation-banner').exists()).toBe(true);
        expect(wrapper.text()).toContain('停止生成');
    });

    it('sends backend stop request when stopping agent generation', async () => {
        wrapper.unmount();
        mockLoadAgentGenerationIntent.mockReturnValue({ sessionId: 12 });

        wrapper = mount(Text);
        await flushPromises();

        await wrapper.find('.stop-generation-btn').trigger('click');

        expect(mockSocketSend).toHaveBeenCalledWith('AGENT_DRAFT_GENERATE_STOP', { sessionId: 12 });
        expect(wrapper.text()).toContain('已停止生成');
    });
});
