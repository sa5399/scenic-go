import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserInfo } from '@/types/api'
import { clearAuth, getToken, getUser, setToken, setUser } from '@/utils/token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<UserInfo | null>(getUser())

  const isLoggedIn = computed(() => token.value !== null)

  function setLogin(newToken: string, newUser: UserInfo): void {
    token.value = newToken
    user.value = newUser
    setToken(newToken)
    setUser(newUser)
  }

  /** 只清本地态；服务端登出由 api/auth.logout 负责 */
  function logoutLocal(): void {
    token.value = null
    user.value = null
    clearAuth()
  }

  /** 角色是否满足要求（未登录视为不满足） */
  function hasRole(roles: number[]): boolean {
    return user.value !== null && roles.includes(user.value.role)
  }

  return { token, user, isLoggedIn, setLogin, logoutLocal, hasRole }
})
