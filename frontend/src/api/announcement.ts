import { get } from '@/utils/request'
import type { Announcement } from '@/types/api'

/** GET /announcements · 免登录 · 只返回已发布公告，时间倒序 */
export function getAnnouncements(): Promise<Announcement[]> {
  return get<Announcement[]>('/announcements')
}
