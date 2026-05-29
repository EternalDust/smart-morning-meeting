import { createRouter, createWebHistory } from 'vue-router'
import MeetingList from '../views/MeetingList.vue'
import MeetingDetail from '../views/MeetingDetail.vue'
import AgendaEdit from '../views/AgendaEdit.vue'
import MeetingCreate from '../views/MeetingCreate.vue'
import ApproveRecord from '../views/ApproveRecord.vue'
import ArchiveList from '../views/ArchiveList.vue'
import AttendeeManage from '../views/AttendeeManage.vue'

const routes = [
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

export default router