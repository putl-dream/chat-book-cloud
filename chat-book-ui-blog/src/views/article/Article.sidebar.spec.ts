/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Article from './Article.vue';
import { createRouter, createWebHistory } from 'vue-router';
import ElementPlus from 'element-plus';

// 模拟 window.innerWidth
Object.defineProperty(window, 'innerWidth', {
    writable: true,
    configurable: true,
    value: 1200
});

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/article/:id', component: Article }]
});

const queryArticleRequestMock = vi.fn();

// Mock hook
vi.mock('./Article/_hooks/useArticleLogic.js', () => ({
    useArticleLogic: () => ({
        article: { title: 'Test Article', content: 'Test Content', userName: 'Test User' },
        praiseStat: 0,
        collectStat: 0,
        activePanel: 'default',
        showRightPanel: { value: false },
        rightSidebarWidth: { value: 300 },
        startResize: vi.fn(),
        queryArticleRequest: queryArticleRequestMock,
        handleLike: vi.fn(),
        handleComment: vi.fn(),
        handleAiChat: vi.fn(),
        handleFavorite: vi.fn(),
        openAuthorPanel: vi.fn()
    })
}));

describe('Article.vue Sidebar Layout', () => {
    beforeEach(() => {
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1200 });
        vi.restoreAllMocks();
        queryArticleRequestMock.mockClear();
    });

    it('should have normal layout when viewport > 1024px', async () => {
        const wrapper = mount(Article, {
            global: {
                plugins: [router, ElementPlus],
                stubs: {
                    MarkdownRenderer: true,
                    Pointer: true,
                    Star: true,
                    ChatLineRound: true,
                    Service: true,
                    SidebarDefault: true,
                    SidebarComment: true,
                    SidebarAI: true
                }
            }
        });

        // 默认非折叠状态
        expect(wrapper.vm.isSidebarCollapsed).toBe(false);

        const page = wrapper.find('.article-page');
        expect(page.exists()).toBe(true);
        
        const sidebar = wrapper.find('.sidebar');
        expect(sidebar.classes()).not.toContain('is-collapsed');
    });

    it('should collapse sidebar when viewport <= 1024px', async () => {
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 800 });
        
        const wrapper = mount(Article, {
            global: {
                plugins: [router, ElementPlus],
                stubs: {
                    MarkdownRenderer: true,
                    Pointer: true,
                    Star: true,
                    ChatLineRound: true,
                    Service: true,
                    SidebarDefault: true,
                    SidebarComment: true,
                    SidebarAI: true
                }
            }
        });

        // 由于 onMounted 时检查了 viewport，需等待 DOM 更新
        await wrapper.vm.$nextTick();
        expect(wrapper.vm.isSidebarCollapsed).toBe(true);

        const sidebar = wrapper.find('.sidebar');
        expect(sidebar.classes()).toContain('is-collapsed');
    });

    it('should expand sidebar to overlay when hovered/clicked in collapsed mode', async () => {
        Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 800 });
        
        const wrapper = mount(Article, {
            global: {
                plugins: [router, ElementPlus],
                stubs: {
                    MarkdownRenderer: true,
                    Pointer: true,
                    Star: true,
                    ChatLineRound: true,
                    Service: true,
                    SidebarDefault: true,
                    SidebarComment: true,
                    SidebarAI: true
                }
            }
        });

        const sidebar = wrapper.find('.sidebar');
        
        // 触发 hover
        await sidebar.trigger('mouseenter');
        expect(wrapper.vm.isSidebarExpanded).toBe(true);
        expect(sidebar.classes()).toContain('is-expanded');

        // 触发 mouseleave
        await sidebar.trigger('mouseleave');
        expect(wrapper.vm.isSidebarExpanded).toBe(false);
        expect(sidebar.classes()).not.toContain('is-expanded');

        // 触发 click
        await sidebar.trigger('click');
        expect(wrapper.vm.isSidebarExpanded).toBe(true);
        expect(sidebar.classes()).toContain('is-expanded');
    });

    it('should query article once on initial mount and again when route id changes', async () => {
        await router.push('/article/1');
        await router.isReady();

        const wrapper = mount(Article, {
            global: {
                plugins: [router, ElementPlus],
                stubs: {
                    MarkdownRenderer: true,
                    Pointer: true,
                    Star: true,
                    ChatLineRound: true,
                    Service: true,
                    SidebarDefault: true,
                    SidebarComment: true,
                    SidebarAI: true
                }
            }
        });

        await wrapper.vm.$nextTick();
        expect(queryArticleRequestMock).toHaveBeenCalledTimes(1);

        await router.push('/article/2');
        await wrapper.vm.$nextTick();
        expect(queryArticleRequestMock).toHaveBeenCalledTimes(2);
    });
});
