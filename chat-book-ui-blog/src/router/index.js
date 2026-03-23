import { createWebHistory, createRouter } from 'vue-router';

const routes = [
    {
        path: '/',
        name: 'app-root',
        component: () => import('@/layout/AppLayout.vue'),
        children: [
            // ----- Common Layout Routes -----
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true, sidebar: false }
            },
            {
                path: 'learn',
                name: 'Learn',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'practice',
                name: 'Practice',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'tags',
                name: 'Tags',
                component: () => import('@/views/home/Tags.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'tag/:tagId',
                name: 'TagArticles',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'backend',
                name: 'Backend',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            }, 
            {
                path: 'frontend',
                name: 'Frontend',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            }, 
            {
                path: 'mysql',
                name: 'Mysql',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'algorithm',
                name: 'Algorithm',
                component: () => import('@/views/home/Home.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'article/:id',
                name: 'Article',
                component: () => import('@/views/article/Article.vue'),
                meta: { headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'message',
                name: 'Message',
                component: () => import('@/views/chat/Message.vue'),
                meta: { requiresAuth: true, headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'history',
                name: 'History',
                component: () => import('@/views/user/UserHistory.vue'),
                meta: { requiresAuth: true, headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/user/UserProfile.vue'),
                meta: { requiresAuth: true, headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'profile/edit',
                name: 'ProfileEdit',
                component: () => import('@/views/user/UserProfileEdit.vue'),
                meta: { requiresAuth: true, headerType: 'common', showSearch: true, showFooter: true }
            },
            {
                path: 'chat',
                name: 'Chat',
                component: () => import('@/views/chat/Chat.vue'),
                meta: { requiresAuth: true, headerType: 'common', showSearch: true, showFooter: true }
            },
            // ----- Search Layout Routes -----
            {
                path: 'search/list/:keyValue',
                name: 'List',
                component: () => import('@/views/home/Search.vue'),
                meta: { headerType: 'common', showSearch: false, showFooter: true }
            },
            // ----- Creative Layout Routes -----
            {
                path: 'creative',
                name: 'CreativeHome',
                component: () => import('@/views/creator/Creative.vue'),
                meta: { requiresAuth: true, headerType: 'creative', sidebar: 'creative', showFooter: true }
            },
            {
                path: 'creative/content',
                name: 'Content',
                component: () => import('@/views/article/Content.vue'),
                meta: { requiresAuth: true, headerType: 'creative', sidebar: 'creative', showFooter: true }
            },
            {
                path: 'creative/drafts',
                name: 'Drafts',
                component: () => import('@/views/creator/DraftBox.vue'),
                meta: { requiresAuth: true, headerType: 'creative', sidebar: 'creative', showFooter: true }
            },
            // ----- Less Layout Routes -----
            {
                path: 'text',
                name: 'Write',
                component: () => import('@/views/creator/Text.vue'),
                meta: { requiresAuth: true, headerType: 'none', showFooter: false }
            },
            {
                path: 'text/:id',
                name: 'Edit',
                component: () => import('@/views/creator/Text.vue'),
                meta: { requiresAuth: true, headerType: 'none', showFooter: false }
            }
        ]
    },
    // Login remains standalone full page
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/user/UserLogin.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// Route Guard
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token');
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth && !token) {
        next({ name: 'Login' });
    } else {
        next();
    }
});

export default router;
