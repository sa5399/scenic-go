import { post } from '@/utils/request'
import type { RouteForm, RouteResult } from '@/types/api'

/** POST /guide/route · 免登录 · tags 是 OR 命中（与 /spots 的 AND 不同）；排不下任何点不报错 */
export function recommendRoute(form: RouteForm): Promise<RouteResult> {
  return post<RouteResult>('/guide/route', form)
}
