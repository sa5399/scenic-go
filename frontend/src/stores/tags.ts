import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { Tag } from '@/types/api'
import { getTags } from '@/api/tag'

/**
 * 标签字典：首页筛选与导览表单共用，取一次缓存。
 * 加载失败不缓存（loaded 保持 false），下次进入页面可重试。
 */
export const useTagStore = defineStore('tags', () => {
  const tags = ref<Tag[]>([])
  const loaded = ref(false)

  async function ensure(): Promise<void> {
    if (loaded.value) {
      return
    }
    tags.value = await getTags()
    loaded.value = true
  }

  return { tags, loaded, ensure }
})
