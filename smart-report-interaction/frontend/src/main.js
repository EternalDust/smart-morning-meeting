import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 统一 Shell 通过 ?token= / ?userName= 注入登录态（跨窗口跳转时接住）
const params = new URLSearchParams(location.search)
const token = params.get('token')
const userName = params.get('userName') || params.get('name')
if (token) localStorage.setItem('token', token)
if (userName) localStorage.setItem('userName', userName)

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
