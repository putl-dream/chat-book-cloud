import {createApp} from 'vue'
import '@/styles/index.css'
import 'element-plus/dist/index.css'
// import store from './store'; // 引入 Vuex store
import ElementPlus from 'element-plus'
import Router from "@/router/index.js";
import App from './App.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { initSiteTheme } from '@/composables/useSiteTheme.js'

const APP_NAME = 'ChatBook'
const ROUTE_TITLES = {
    Home: '首页',
    Learn: '学习',
    Practice: '实战',
    Tags: '标签',
    TagArticles: '标签文章',
    Backend: '后端',
    Frontend: '前端',
    Mysql: 'MySQL',
    Algorithm: '算法',
    Article: '文章详情',
    Message: '消息中心',
    History: '浏览历史',
    Profile: '个人主页',
    ProfileEdit: '编辑资料',
    Chat: '聊天',
    List: '搜索结果',
    CreativeHome: '创作中心',
    Content: '内容管理',
    Drafts: '草稿箱',
    AgentStudio: 'AI 创作',
    Write: '写文章',
    Edit: '编辑文章',
    Login: '登录'
}

const syncDocumentTitle = (route) => {
    const pageTitle = route?.name ? ROUTE_TITLES[route.name] : ''
    document.title = pageTitle ? `${pageTitle} | ${APP_NAME}` : APP_NAME
}

initSiteTheme()

Router.afterEach((to) => {
    syncDocumentTitle(to)
})

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// app.use(store)
app.use(ElementPlus)
app.use(Router)
Router.isReady().then(() => {
    syncDocumentTitle(Router.currentRoute.value)
    app.mount('#app')
})
