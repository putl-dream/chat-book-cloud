import {createApp} from 'vue'
import '@/styles/variables.css'
import '@/styles/content-theme.css'
import '@/styles/themes/site/light.css'
import '@/styles/themes/site/reading.css'
import '@/assets/style.css'
import 'element-plus/dist/index.css'
// import store from './store'; // 引入 Vuex store
import ElementPlus from 'element-plus'
import Router from "@/router/index.js";
import App from './App.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { initSiteTheme } from '@/composables/useSiteTheme.js'

initSiteTheme()

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// app.use(store)
app.use(ElementPlus)
app.use(Router)
app.mount('#app')
