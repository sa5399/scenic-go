import { get } from '@/utils/request'
import type { Slot } from '@/types/api'

/** GET /spots/{id}/slots · 免登录 · 按开始时间升序；date 缺省为今天 */
export function getSlots(spotId: number | string, date?: string): Promise<Slot[]> {
  return get<Slot[]>(`/spots/${spotId}/slots`, { date })
}
