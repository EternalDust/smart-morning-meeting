import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../components/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import ProblemList from '../views/ProblemList.vue'
import ProgressTrack from '../views/ProgressTrack.vue'
import Statistics from '../views/Statistics.vue'

const routes = [
    { path: '/login', name: 'Login', component: Login },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            { path: '/dashboard', name: 'Dashboard', component: Dashboard },
            { path: '/problems', name: 'ProblemList', component: ProblemList },
            { path: '/progress', name: 'ProgressTrack', component: ProgressTrack },
            { path: '/statistics', name: 'Statistics', component: Statistics }
        ]
    }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path !== '/login' && !token) next('/login')
    else next()
})

export default router