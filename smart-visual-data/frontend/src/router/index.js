import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../components/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    meta: { requiresAuth: true },
    redirect: '/attendance',
    children: [
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('../views/Attendance.vue')
      },
      {
        path: 'problem-solving',
        name: 'ProblemSolving',
        component: () => import('../views/ProblemSolving.vue')
      },
      {
        path: 'risk-prediction',
        name: 'RiskPrediction',
        component: () => import('../views/RiskPrediction.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const urlToken = new URLSearchParams(window.location.search).get('token')
if (urlToken) localStorage.setItem('token', urlToken)

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
