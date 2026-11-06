import { get } from '@/utils/request'
import type { UserInfo } from '@/types/api'

/** GET /user/me · 需登录 */
export function me(): Promise<UserInfo> {
  return get<UserInfo>('/user/me')
}
