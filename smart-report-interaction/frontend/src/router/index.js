import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/meetingroom' },
  { path: '/meetingroom', name: 'MeetingRoom', component: () => import('../views/MeetingRoom.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
