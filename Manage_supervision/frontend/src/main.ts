import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import axios from './utils/axios'
import { useUserStore } from './stores/user'
import permissionDirective from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(permissionDirective)

// 设置全局axios实例
app.config.globalProperties.$axios = axios

// 初始化认证状态
const userStore = useUserStore()
await userStore.initializeAuth()

// 注意：WebSocket连接初始化已经在App.vue中处理
// 不需要在这里重复初始化

app.mount('#app')
