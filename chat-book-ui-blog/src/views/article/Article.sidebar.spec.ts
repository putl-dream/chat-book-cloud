/**
 * @vitest-environment jsdom
 */
import { mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createRouter, createWebHistory } from 'vue-router';
import { ref } from 'vue';
import ElementPlus from 'element-plus';
import Article from './Article.vue';

Object.defineProperty(window, 'innerWidth', {
    writable: true,
    configurable: true,
    value: 1200
});

let articleLogicMock;

vi.mock('./_hooks/useArticleLogic.js', () => ({
    useArticleLogic: () => articleLogicMock
}));

vi.mock('@/components/common/rich-text/content-pipeline.js', () => ({
    buildRichTextHtml: vi.fn((html) => html)
}));

vi.mock('@/composables/useSiteTheme.js', () => ({
    useSiteTheme: () => ({
        articleTheme: ref('light')
    })
}));

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/article/:id', component: Article }]
});

const buildLogicMock = () => ({
    article: ref({
        title: 'Test Article',
        content: '<h2>Intro</h2>',
        userName: 'Test User',
        authorAvatar: '',
        createTime: '2026-03-21 10:00:00',
        viewCount: 128,
        userId: 2,
        tagIds: [1, 2]
    }),
    praiseStat: ref(0),
    collectStat: ref(0),
    activePanel: ref('default'),
    showRightPanel: ref(true),
    authorRelation: ref(-1),
    isSelfAuthor: ref(false),
    authorActionLoading: ref(false),
    rightSidebarWidth: ref(300),
    startResize: vi.fn(),
    queryArticleRequest: vi.fn(),
    handleLike: vi.fn(),
    handleComment: vi.fn(),
    handleAiChat: vi.fn(),
    handleFavorite: vi.fn(),
    handleFollow: vi.fn(),
    openDefaultPanel: vi.fn()
});

const mountArticle = () => mount(Article, {
    global: {
        plugins: [router, ElementPlus],
        stubs: {
            RichTextViewer: true,
            SidebarDefault: true,
            SidebarComment: true,
            SidebarAI: true
        }
    }
});

describe('Article.vue Sidebar Layout', () => {
    beforeEach(async () => {
        articleLogicMock = buildLogicMock();
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 1200
        });
        await router.push('/article/1');
        await router.isReady();
    });

    it('renders author strip and keeps AI in the left action rail', async () => {
        const wrapper = mountArticle();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.author-strip').text()).toContain('Test User');
        expect(wrapper.findAll('.action-label').map((item) => item.text())).toContain('AI');
        expect(wrapper.find('.fab-ai').exists()).toBe(false);
    });

    it('collapses sidebar when viewport <= 1024px', async () => {
        Object.defineProperty(window, 'innerWidth', {
            writable: true,
            configurable: true,
            value: 800
        });

        const wrapper = mountArticle();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.sidebar').classes()).toContain('is-collapsed');
    });

    it('queries article on mount and on route id change', async () => {
        const wrapper = mountArticle();
        await wrapper.vm.$nextTick();

        expect(articleLogicMock.queryArticleRequest).toHaveBeenCalledTimes(1);

        await router.push('/article/2');
        await wrapper.vm.$nextTick();

        expect(articleLogicMock.queryArticleRequest).toHaveBeenCalledTimes(2);
    });
});
