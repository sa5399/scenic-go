<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from '@/utils/message'
import type { PageResult, ReviewItem } from '@/types/api'
import { getMyReviews } from '@/api/review'

/** 我的评价：GET /reviews（只含当前用户，按时间倒序） */
const page = ref<PageResult<ReviewItem>>({ records: [], total: 0, current: 1, size: 10 })
const loading = ref(false)
const firstError = ref(false)

async function load(current = 1) {
  loading.value = true
  firstError.value = false
  try {
    page.value = await getMyReviews({ current, size: page.value.size })
  } catch (e) {
    firstError.value = true
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

load(1)
</script>

<template>
  <div class="my-reviews">
    <van-nav-bar title="我的评价" left-arrow fixed placeholder @click-left="$router.back()" />

    <p v-if="loading && page.records.length === 0" class="hint">加载中…</p>
    <van-empty v-else-if="firstError" image="network" description="加载失败">
      <van-button round type="primary" size="small" @click="load(1)">重试</van-button>
    </van-empty>
    <van-empty v-else-if="page.records.length === 0" description="还没有发表过评价" />

    <van-list
      v-else
      v-model:loading="loading"
      :finished="page.records.length >= page.total"
      finished-text="没有更多了"
      @load="load(page.current + 1)"
    >
      <div v-for="r in page.records" :key="r.id" class="review-card">
        <div class="review-head">
          <router-link v-if="r.spotId" :to="`/spots/${r.spotId}`" class="review-spot">
            {{ r.spotName || '景点' }}
          </router-link>
          <span class="review-stars">{{ '★'.repeat(r.score) }}</span>
        </div>
        <p class="review-content">{{ r.content || '（无文字）' }}</p>
        <div class="review-foot">
          <span>{{ r.createTime.slice(0, 16).replace('T', ' ') }}</span>
          <span>👍 {{ r.liked }}</span>
        </div>
      </div>
    </van-list>
  </div>
</template>

<style scoped lang="scss">
.my-reviews {
  padding-bottom: 16px;
}

.hint {
  text-align: center;
  color: var(--sg-text-secondary);
  padding: 40px 0;
}

.review-card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.review-spot {
  font-size: 14px;
  font-weight: 600;
  color: var(--sg-primary);
  text-decoration: none;
}

.review-stars {
  color: var(--sg-accent);
  font-size: 12px;
}

.review-content {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.6;
}

.review-foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--sg-text-secondary);
}
</style>
