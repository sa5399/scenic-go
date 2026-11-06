import { get } from '@/utils/request'
import type { Tag, TagType } from '@/types/api'

/** GET /tags · 免登录 · 建议调用方缓存一份 */
export function getTags(type?: TagType): Promise<Tag[]> {
  return get<Tag[]>('/tags', { type })
}
