import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/Home.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
