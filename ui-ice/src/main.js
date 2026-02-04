import {createApp} from 'vue'
import '@/styles/variables.css'
import '@/assets/style.css'
import 'element-plus/dist/index.css'
// import store from './store'; // 引入 Vuex store
import ElementPlus from 'element-plus'
import Router from "@/router/index.js";
import App from './App.vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// app.use(store)
app.use(ElementPlus)
app.use(Router)
app.mount('#app')