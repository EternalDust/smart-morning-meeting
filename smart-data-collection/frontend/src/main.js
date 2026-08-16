// 从统一 Shell 入口接收 token / userName（iframe 嵌入时 URL 注入登录态）
const urlParams = new URLSearchParams(window.location.search)
const urlToken = urlParams.get('token')
const urlUserName = urlParams.get('userName') || urlParams.get('name')
if (urlToken) localStorage.setItem('token', urlToken)
if (urlUserName) localStorage.setItem('userName', urlUserName)

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
