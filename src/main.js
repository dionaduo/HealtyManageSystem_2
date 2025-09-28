// main.js
import { createApp } from 'vue'
import App from './App.vue'
import { createPinia } from "pinia"
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { useAuthStore } from '@/stores/auth'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化认证状态
const authStore = useAuthStore()
authStore.initialize()

app.mount('#app')

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}