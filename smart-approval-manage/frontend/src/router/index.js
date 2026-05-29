import { createRouter, createWebHistory } from 'vue-router'
import MeetingList from '../views/MeetingList.vue'
import MeetingDetail from '../views/MeetingDetail.vue'
import AgendaEdit from '../views/AgendaEdit.vue'
import MeetingCreate from '../views/MeetingCreate.vue'
import LoginView from '../views/LoginView.vue'
import ApproveRecord from '../views/ApproveRecord.vue'
import ArchiveList from '../views/ArchiveList.vue'
import AttendeeManage from '../views/AttendeeManage.vue'

const routes = [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/', redirect: '/meetings' },
    { path: '/meetings', component: MeetingList },
    { path: '/meetings/create', component: MeetingCreate },
    { path: '/meetings/:id', component: MeetingDetail },
    { path: '/meetings/:id/agenda', component: AgendaEdit },
    { path: '/meetings/:id/records', component: ApproveRecord },
    { path: '/meetings/:id/attendees', component: AttendeeManage },
    { path: '/archives', component: ArchiveList }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

const urlToken = new URLSearchParams(window.location.search).get('token')
if (urlToken) localStorage.setItem('token', urlToken)

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (!to.meta.public && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router