import {createWebHistory, createRouter} from 'vue-router';

const routes = [
    {
        path: '/',
        name: 'common',
        component: () => import('@/layout/CommonLayout.vue'),
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/views/Home.vue')
            },            {
                path: 'backend',
                name: 'Backend',
                component: () => import('@/views/Home.vue')
            },            {
                path: 'frontend',
                name: 'Frontend',
                component: () => import('@/views/Home.vue')
            },            {
                path: 'mysql',
                name: 'Mysql',
                component: () => import('@/views/Home.vue')
            },
            {
                path: 'algorithm',
                name: 'Algorithm',
                component: () => import('@/views/Home.vue')
            },
            {
                path: '/article/:id',
                name: 'Article',
                component: () => import('@/views/Article.vue')
            },
            {
                path: '/message',
                name: 'Message',
                component: () => import('@/views/Message.vue'),
                meta: {requiresAuth: true}
            },
            {
                path: '/history',
                name: 'History',
                component: () => import('@/views/History.vue'),
                meta: {requiresAuth: true}
            },
            {
                path: '/profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: {requiresAuth: true}
            },
            {
                path: '/chat',
                name: 'Chat',
                component: () => import('@/views/Chat.vue'),
                meta: {requiresAuth: true}
            }
        ]
    },
    {
        path: '/search',
        name: 'Search',
        component: () => import('@/layout/SearchLayout.vue'),
        children: [
            {
                path: 'list/:keyValue',
                name: 'List',
                component: () => import('@/views/Search.vue')
            }
        ]
    },
    {
        path: '/creative',
        name: 'Creative',
        component: () => import('@/layout/CreativeLayout.vue'),
        children: [
            {
                path: '',
                name: 'CreativeHome',
                component: () => import('@/views/Creative.vue'),
                meta: {requiresAuth: true}
            },
            {
                path: 'content',
                name: 'Content',
                component: () => import('@/views/Content.vue'),
                meta: {requiresAuth: true}
            }
        ]
    },
    {
        path: '/text',
        name: 'Text',
        component: () => import('@/layout/LessLayout.vue'),
        children: [
            {
                path: '',
                name: 'Write',
                component: () => import('@/views/Text.vue'),
                meta: {requiresAuth: true}
            },
            {
                path: ':id',
                name: 'Edit',
                component: () => import('@/views/Text.vue'),
                meta: {requiresAuth: true}
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token'); // 从 localStorage 获取 token
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth);

    if (requiresAuth && !token) {
        // 如果需要认证且没有 token，则重定向到登录页面
        next({ name: 'Login' });
    } else {
        next();
    }
});

export default router;