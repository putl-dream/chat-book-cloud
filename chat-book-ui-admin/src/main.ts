import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "@/App.vue";
import router from "@/router";
import { useAuthStore } from "@/stores/auth";
import { useThemeStore } from "@/stores/theme";
import "@/styles/index.css";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

const themeStore = useThemeStore();
themeStore.initialize();

const authStore = useAuthStore();
authStore.hydrateToken();

app.use(router);
app.mount("#app");
