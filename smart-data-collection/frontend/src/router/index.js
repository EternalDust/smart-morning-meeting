import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据治理工作台', icon: 'Odometer' }
      },
      {
        path: 'datasource',
        name: 'DataSourceManage',
        component: () => import('@/views/DataSourceManage.vue'),
        meta: { title: '数据源管理', icon: 'Connection' }
      },
      {
        path: 'quality',
        name: 'QualityMonitor',
        component: () => import('@/views/QualityMonitor.vue'),
        meta: { title: '质量监控', icon: 'Monitor' }
      },
      {
        path: 'lineage',
        name: 'DataLineage',
        component: () => import('@/views/DataLineage.vue'),
        meta: { title: '数据溯源', icon: 'Share' }
      },
      {
        path: 'label',
        name: 'LabelManage',
        component: () => import('@/views/LabelManage.vue'),
        meta: { title: '标签管理', icon: 'Collection' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
