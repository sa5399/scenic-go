<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Spot } from '@/types/api'
import { fen2yuan } from '@/utils/format'

defineProps<{ spot: Spot }>()

const router = useRouter()
</script>

<template>
  <div class="spot-card" @click="router.push(`/spots/${spot.id}`)">
    <van-image width="112" height="88" radius="8" :src="spot.coverImg">
      <!-- public/img 图片未就位时的占位：取景点名首字 -->
      <template #error>
        <div class="spot-cover-fallback">{{ spot.name.slice(0, 1) }}</div>
      </template>
      <template #loading>
        <div class="spot-cover-fallback is-loading" />
      </template>
    </van-image>

    <div class="spot-body">
      <div class="spot-name">{{ spot.name }}</div>
      <div class="spot-summary">{{ spot.summary }}</div>
      <div class="spot-meta">
        <span class="spot-score">★ {{ spot.score }}</span>
        <span class="spot-sold">已售 {{ spot.soldCount }}</span>
        <span class="spot-price">
          {{ spot.avgPrice > 0 ? `¥${fen2yuan(spot.avgPrice)}/人` : '免费' }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.spot-card {
  display: flex;
  gap: 12px;
  margin: 0 12px 12px;
  padding: 12px;
  border-radius: var(--sg-radius);
  background: var(--sg-surface);
  cursor: pointer;
}

.spot-cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--sg-cover-gradient);
  color: var(--sg-primary);
  font-size: 26px;
  font-weight: 600;

  &.is-loading {
    background: var(--sg-fill);
  }
}

.spot-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.spot-name {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.spot-summary {
  flex: 1;
  margin-top: 4px;
  font-size: 12px;
  color: var(--sg-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.spot-meta {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 12px;
}

.spot-score {
  color: var(--sg-accent);
  font-weight: 600;
}

.spot-sold {
  color: var(--sg-text-secondary);
}

.spot-price {
  margin-left: auto;
  color: var(--sg-primary);
  font-weight: 600;
}
</style>
