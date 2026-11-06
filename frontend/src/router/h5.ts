import type { RouteRecordRaw } from 'vue-router'
import H5Layout from '@/layouts/H5Layout.vue'

/**
 * 游客端路由树（前缀 /），全部页面共用 H5Layout（居中容器），
 * Tabbar 只在 meta.tabbar 的四个主页面显示。
 */
export default [
  {
    path: '/',
    component: H5Layout,
    children: [
      {
        // 首页用显式 /home：若挂在 path:''，to="/" 解析出的根记录是所有页面的
        // matched 祖先，Vant tabbar 会把它判成"永远 active"从而吞掉点击
        path: 'home',
        name: 'home',
        component: () => import('@/views/h5/HomeView.vue'),
        meta: { tabbar: true, title: '首页' },
      },
      {
        path: '',
        redirect: { name: 'home' },
      },
      {
        path: 'guide',
        name: 'guide',
        component: () => import('@/views/h5/GuideView.vue'),
        meta: { tabbar: true, title: '导览' },
      },
      {
        path: 'reserves',
        name: 'reserves',
        component: () => import('@/views/h5/ReservesView.vue'),
        meta: { tabbar: true, requiresAuth: true, title: '我的预约' },
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/views/h5/UserView.vue'),
        meta: { tabbar: true, title: '我的' },
      },
      {
        path: 'login',
        name: 'login',
        component: () => import('@/views/h5/LoginView.vue'),
        meta: { title: '登录' },
      },
      {
        path: 'register',
        name: 'register',
        component: () => import('@/views/h5/RegisterView.vue'),
        meta: { title: '注册' },
      },
      {
        path: 'my-reviews',
        name: 'my-reviews',
        component: () => import('@/views/h5/MyReviewsView.vue'),
        meta: { requiresAuth: true, title: '我的评价' },
      },
      {
        path: 'park',
        name: 'park-booking',
        component: () => import('@/views/h5/ParkDetailView.vue'),
        meta: { title: '入园预约' },
      },
      {
        path: 'spots/:id',
        name: 'spot-detail',
        component: () => import('@/views/h5/SpotDetailView.vue'),
        meta: { title: '景点详情' },
      },
      {
        path: 'spots/:id/book',
        name: 'book',
        component: () => import('@/views/h5/BookView.vue'),
        meta: { requiresAuth: true, title: '预约' },
      },
      {
        path: 'reserves/:orderNo/pay',
        name: 'reserve-pay',
        component: () => import('@/views/h5/PayView.vue'),
        meta: { requiresAuth: true, title: '确认支付' },
      },
      {
        path: 'reserves/:orderNo',
        name: 'reserve-detail',
        component: () => import('@/views/h5/ReserveDetailView.vue'),
        meta: { requiresAuth: true, title: '订单详情' },
      },
      {
        path: 'announcements',
        name: 'announcements',
        component: () => import('@/views/h5/AnnouncementsView.vue'),
        meta: { title: '景区公告' },
      },
      {
        path: 'forbidden',
        name: 'forbidden',
        component: () => import('@/views/common/ForbiddenView.vue'),
        meta: { title: '没有权限' },
      },
    ],
  },
] as RouteRecordRaw[]
