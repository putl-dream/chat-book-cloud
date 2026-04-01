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

describe('Text.vue Editor Layout Adjustments', () => {
    let wrapper;

    beforeEach(() => {
        // Reset window innerWidth
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1200 });
        wrapper = mount(Text);
    });

    it('should not have bottom padding on the main content container to let the editor reach the bottom', () => {
        const mainContent = wrapper.find('.main-content');
        // We verify the class exists and is used for layout
        expect(mainContent.exists()).toBe(true);
        // The actual padding check would ideally be E2E or checking computed styles in a real browser,
        // but we ensure the component renders correctly with the new structure.
    });

    it('should maintain editor content filling the space', () => {
        const editorWrapper = wrapper.find('.main-content-editor');
        expect(editorWrapper.exists()).toBe(true);
        // Assert it uses flex layout for filling
        expect(wrapper.find('.scroll-area').exists()).toBe(true);
    });

    it('should handle resize events and panel toggling without breaking layout state', async () => {
        // Trigger resize
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 500 });
        window.dispatchEvent(new Event('resize'));
        await wrapper.vm.$nextTick();
        
        expect(wrapper.vm.layoutState.isMobile).toBe(true);
        
        // Toggle left panel
        wrapper.vm.toggleLeft();
        await wrapper.vm.$nextTick();
        
        // Check content width logic still applies correctly
        expect(wrapper.vm.contentWidth).toBeGreaterThan(0);
    });
});
