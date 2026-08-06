import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/meetingroom' },
  { path: '/meetingroom', name: 'MeetingRoom', component: () => import('../views/MeetingRoom.vue') },
  { path: '/sign', name: 'SignIn', component: () => import('../views/SignIn.vue') },
  { path: '/report', name: 'Report', component: () => import('../views/MeetingReport.vue') },
  { path: '/interaction', name: 'Interaction', component: () => import('../views/Interaction.vue') },
  { path: '/analytics', name: 'Analytics', component: () => import('../views/Analytics.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
