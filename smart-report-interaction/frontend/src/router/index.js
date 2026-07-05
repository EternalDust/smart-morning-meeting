import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/meeting/1' },
  { path: '/meeting/:id', name: 'MeetingRoom', component: () => import('../views/MeetingRoom.vue') },
  { path: '/sign', redirect: '/meeting/1' },
  { path: '/report', redirect: '/meeting/1' },
  { path: '/interaction', redirect: '/meeting/1' },
  { path: '/analytics', name: 'Analytics', component: () => import('../views/Analytics.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
