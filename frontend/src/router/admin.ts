import type { RouteRecordRaw } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

/**
 * 管理端路由树（前缀 /admin，桌面）。
 * 整个 /admin（除登录页）要求 role ∈ {2 核销员, 3 管理员}；
 * 核销流水等页面单独再收紧到管理员。
 */
export default [
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '管理端登录' },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: [2, 3] },
    children: [
      {
        path: '',
        redirect: { name: 'admin-checkin' },
      },
      {
        path: 'checkin',
        name: 'admin-checkin',
        component: () => import('@/views/admin/CheckinView.vue'),
        meta: { title: '核销工作台' },
      },
      {
        path: 'checkin/logs',
        name: 'admin-checkin-logs',
        component: () => import('@/views/admin/CheckinLogsView.vue'),
        meta: { roles: [3], title: '核销流水' },
      },
      {
        path: 'spots',
        name: 'admin-spots',
        component: () => import('@/views/admin/SpotsAdminView.vue'),
        meta: { roles: [3], title: '景点管理' },
      },
      {
        path: 'slots',
        name: 'admin-slots',
        component: () => import('@/views/admin/SlotsAdminView.vue'),
        meta: { roles: [3], title: '时段与库存' },
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: () => import('@/views/admin/OrdersAdminView.vue'),
        meta: { roles: [3], title: '订单查询' },
      },
      {
        path: 'announcements',
        name: 'admin-announcements',
        component: () => import('@/views/admin/AnnouncementsAdminView.vue'),
        meta: { roles: [3], title: '公告维护' },
      },
    ],
  },
  {
    path: '/admin/forbidden',
    name: 'admin-forbidden',
    component: () => import('@/views/common/ForbiddenView.vue'),
    meta: { title: '没有权限' },
  },
] as RouteRecordRaw[]
