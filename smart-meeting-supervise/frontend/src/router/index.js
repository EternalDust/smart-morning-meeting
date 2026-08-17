import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ProblemList from '../views/ProblemList.vue'
import ProblemDetail from '../views/ProblemDetail.vue'
import ProgressTrack from '../views/ProgressTrack.vue'

// 统一门户登录：从 URL 上取 token 存入本地
const urlToken = new URLSearchParams(window.location.search).get('token')
if (urlToken) {
    localStorage.setItem('token', urlToken)
}

const routes = [
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            { path: '/dashboard', name: 'Dashboard', component: Dashboard, meta: { requiresAuth: true } },
            { path: '/problems', name: 'ProblemList', component: ProblemList, meta: { requiresAuth: true } },
            { path: '/problems/:id', name: 'ProblemDetail', component: ProblemDetail, meta: { requiresAuth: true } },
            { path: '/progress', name: 'ProgressTrack', component: ProgressTrack, meta: { requiresAuth: true } }
        ]
    }
]

const router = createRouter({ history: createWebHistory(), routes })

// 未登录时跳转统一门户
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.meta.requiresAuth && !token) {
        window.location.href = 'http://localhost:5000/'
    } else {
        next()
    }
})

export default router
