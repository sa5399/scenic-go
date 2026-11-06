import { del, get, post, put } from '@/utils/request'
import type {
  AdminReserveItem,
  AdminReserveQuery,
  Announcement,
  AnnouncementForm,
  PageResult,
  Spot,
  SpotUpdateForm,
} from '@/types/api'

/** PUT /admin/spots/{id}/status · 需管理员 · 1 上架 0 下架 */
export function updateSpotStatus(id: number, status: 0 | 1): Promise<void> {
  return put<void>(`/admin/spots/${id}/status?status=${status}`)
}

/** PUT /admin/slots/{id}/stock · 需管理员 · 直接设置时段总库存 */
export function adjustSlotStock(id: number, totalStock: number): Promise<void> {
  return put<void>(`/admin/slots/${id}/stock?totalStock=${totalStock}`)
}

/** POST /admin/announcements · 需管理员 · 返回新公告 id */
export function createAnnouncement(form: AnnouncementForm): Promise<number> {
  return post<number>('/admin/announcements', form)
}

/** PUT /admin/announcements/{id} · 需管理员 · 修改标题与正文 */
export function updateAnnouncement(id: number, form: AnnouncementForm): Promise<void> {
  return put<void>(`/admin/announcements/${id}`, form)
}

/** PUT /admin/announcements/{id}/status · 需管理员 · 1 发布 0 下架 */
export function updateAnnouncementStatus(id: number, status: 0 | 1): Promise<void> {
  return put<void>(`/admin/announcements/${id}/status?status=${status}`)
}

/** GET /admin/reserves · 需管理员 · 全部用户的订单，供运营核对 */
export function getAdminReserves(query: AdminReserveQuery = {}): Promise<PageResult<AdminReserveItem>> {
  return get<PageResult<AdminReserveItem>>('/admin/reserves', query)
}
/** DELETE /admin/announcements/{id} · 需管理员 */
export function deleteAnnouncement(id: number): Promise<void> {
  return del<void>(`/admin/announcements/${id}`)
}

/** GET /admin/spots · 需管理员 · 含下架景点的全量列表 */
export function getAdminSpots(
  query: { keyword?: string; type?: 0 | 1; status?: 0 | 1; current?: number; size?: number } = {},
): Promise<PageResult<Spot>> {
  return get<PageResult<Spot>>('/admin/spots', query)
}

/** GET /admin/announcements · 需管理员 · 含下架公告的分页列表 */
export function getAdminAnnouncements(
  query: { current?: number; size?: number; status?: 0 | 1 } = {},
): Promise<PageResult<Announcement>> {
  return get<PageResult<Announcement>>('/admin/announcements', query)
}

/** PUT /admin/spots/{id} · 需管理员 · 编辑名称/简介/价格，改库后删详情缓存 */
export function updateSpot(id: number, form: SpotUpdateForm): Promise<void> {
  return put<void>(`/admin/spots/${id}`, form)
}
