import { createRouter, createWebHistory } from 'vue-router'
import MeetingList from '../views/MeetingList.vue'
import MeetingDetail from '../views/MeetingDetail.vue'
import AgendaEdit from '../views/AgendaEdit.vue'

const routes = [
    { path: '/', redirect: '/meetings' },
    { path: '/meetings', component: MeetingList },
    { path: '/meetings/:id', component: MeetingDetail },
    { path: '/meetings/:id/agenda', component: AgendaEdit }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router