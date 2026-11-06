import { post } from '@/utils/request'
import type { LoginForm, LoginResult, RegisterForm } from '@/types/api'

/** POST /auth/register · 免登录 */
export function register(form: RegisterForm): Promise<void> {
  return post<void>('/auth/register', form)
}

/** POST /auth/login · 免登录 */
export function login(form: LoginForm): Promise<LoginResult> {
  return post<LoginResult>('/auth/login', form)
}

/** POST /auth/logout · 需登录；返回 401 说明本地 token 已失效，同样按已登出处理 */
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}
