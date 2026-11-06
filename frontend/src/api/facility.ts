import { get } from '@/utils/request'
import type { Facility, FacilityQuery } from '@/types/api'

/** GET /facilities/nearby · 免登录 · 按距离升序；拿不到定位时后端按 (0,0) 查基本返回空 */
export function getNearby(query: FacilityQuery = {}): Promise<Facility[]> {
  return get<Facility[]>('/facilities/nearby', query)
}
