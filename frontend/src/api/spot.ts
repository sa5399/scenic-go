import { get } from '@/utils/request'
import type { Exhibition, PageResult, Spot, SpotQuery } from '@/types/api'

/** GET /spots · 免登录 · 排序后端定死：soldCount desc, score desc */
export function getSpots(query: SpotQuery = {}): Promise<PageResult<Spot>> {
  return get<PageResult<Spot>>('/spots', query)
}

/** GET /spots/{id} · 免登录 · 详情含 content 与 tags（注意后端有约 30 分钟 Redis 缓存） */
export function getSpot(id: number | string): Promise<Spot> {
  return get<Spot>(`/spots/${id}`)
}

/** GET /spots/{id}/exhibitions · 免登录 · 按sort升序，无展品返回空数组 */
export function getExhibitions(spotId: number | string): Promise<Exhibition[]> {
  return get<Exhibition[]>(`/spots/${spotId}/exhibitions`)
}
