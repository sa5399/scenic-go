import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { Role } from '@/types/api'
import { useAuthStore } from '@/stores/auth'
import h5Routes from './h5'
import adminRoutes from './admin'

declare module 'vue-router' {
  interface RouteMeta {
    /** 需要登录，未登录跳对应端登录页（带 redirect） */
    requiresAuth?: boolean
    /** 允许访问的角色，缺省不限制 */
    roles?: Role[]
    /** H5 端是否显示底部 Tabbar */
    tabbar?: boolean
    title?: string
  }
}

// 记住各页面离开时的滚动位置：返回（savedPosition）或再次进入 tab 页时恢复，
// 解决"进详情再退出来，列表回到顶部"的问题
const scrollPositions = new Map<string, number>()

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...h5Routes, ...adminRoutes] as RouteRecordRaw[],
  scrollBehavior(to, from, savedPosition) {
    scrollPositions.set(from.path, window.scrollY)
    if (savedPosition) {
      return savedPosition
    }
    const cached = scrollPositions.get(to.path)
    if (to.meta.tabbar && typeof cached === 'number') {
      return { top: cached }
    }
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    const loginPath = to.path.startsWith('/admin') ? '/admin/login' : '/login'
    return { path: loginPath, query: { redirect: to.fullPath } }
  }

  // 角色要求取 to.meta（vue-router 4 中子级 meta 覆盖父级）：布局级 [2,3] 管整体准入，
  // 子页可单独收紧到 [3]。不能取匹配链并集——父级角色会稀释子页收紧，导致越权放行
  const roles = to.meta.roles ?? []
  if (roles.length > 0 && !auth.hasRole(roles)) {
    return { name: to.path.startsWith('/admin') ? 'admin-forbidden' : 'forbidden' }
  }
  return true
})

export default router
