<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Announcement } from '@/types/api'
import { getAnnouncements } from '@/api/announcement'
import { formatTime } from '@/utils/format'

/** 景区公告：公开接口，只含已发布，时间倒序 */
const router = useRouter()
const list = ref<Announcement[]>([])
const loading = ref(true)
const loadError = ref(false)

onMounted(async () => {
  try {
    list.value = await getAnnouncements()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="announcements">
    <van-nav-bar title="景区公告" left-arrow fixed placeholder @click-left="router.back()" />

    <p v-if="loading" class="hint">加载中…</p>
    <van-empty v-else-if="loadError" image="network" description="加载失败">
      <van-button round type="primary" size="small" @click="router.go(0)">重试</van-button>
    </van-empty>
    <van-empty v-else-if="list.length === 0" description="暂无公告" />

    <div v-for="a in list" :key="a.id" class="ann-card">
      <div class="ann-title">{{ a.title }}</div>
      <p class="ann-content">{{ a.content }}</p>
      <div class="ann-time">{{ formatTime(a.createTime) }}</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.hint {
  text-align: center;
  color: var(--sg-text-secondary);
  padding: 40px 0;
}

.ann-card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.ann-title {
  font-size: 15px;
  font-weight: 600;
}

.ann-content {
  margin: 8px 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--sg-text);
  white-space: pre-wrap;
}

.ann-time {
  font-size: 11px;
  color: var(--sg-text-secondary);
}
</style>
