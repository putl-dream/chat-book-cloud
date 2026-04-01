import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Text from './Text.vue';

// Mocks
vi.mock('vue-router', () => ({
    useRoute: () => ({ params: { id: null } }),
    useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
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
        onOpen() {} onClose() {} onError() {} on() {} connect() {} send() {} isConnected() { return true; } close() {}
    },
    formatWsUrl: () => 'ws://localhost'
}));

vi.mock('@/views/article/_domain/article.js', () => ({
    publishArticle: vi.fn(),
    saveDraftArticle: vi.fn(),
    uploadFile: vi.fn()
}));

vi.mock('@/views/creator/_domain/agent.js', () => ({
    loadAgentDraftImport: vi.fn(() => null),
    clearAgentDraftImport: vi.fn()
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
    useEditor: () => ({
        getHTML: () => '',
        destroy: vi.fn(),
        commands: { setContent: vi.fn() },
        storage: { characterCount: { characters: () => 0 } }
    }),
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
    getTagsByType: vi.fn(() => Promise.resolve([]))
}));

describe('Text.vue Three-Column Layout', () => {
    let wrapper;

    beforeEach(() => {
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
});
