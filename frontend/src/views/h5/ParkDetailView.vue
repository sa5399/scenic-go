<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Spot } from '@/types/api'
import { getSpot } from '@/api/spot'
import CoverBlock from '@/components/CoverBlock.vue'

/** 园区票专属页：简单描述 + 预约按钮（区别于景点详情页） */
const PARK_ID = 13
const router = useRouter()
const spot = ref<Spot | null>(null)
const loadError = ref('')

onMounted(async () => {
  try {
    spot.value = await getSpot(PARK_ID)
  } catch (e) {
    loadError.value = (e as Error).message
  }
})
</script>

<template>
  <div class="park">
    <van-nav-bar title="入园预约" left-arrow fixed placeholder @click-left="router.back()" />

    <van-empty v-if="loadError" image="network" :description="loadError">
      <van-button round type="primary" size="small" @click="router.go(0)">重试</van-button>
    </van-empty>

    <template v-else-if="spot">
      <CoverBlock :src="spot.coverImg" :label="spot.name" width="100%" height="200px" />

      <div class="card">
        <div class="park-name">{{ spot.name }}</div>
        <p class="park-desc">{{ spot.content }}</p>
      </div>

      <div class="card info">
        <div class="row"><span>费用</span><b class="free">免费</b></div>
        <div class="row"><span>入园时段</span><b>{{ spot.openTime }}</b></div>
        <div class="row"><span>每时段容量</span><b>2000 人</b></div>
        <div class="row"><span>核销方式</span><b>凭核销码入园</b></div>
      </div>

      <div class="note">湖心岛摇橹船、亲子乐园、美术馆特展等园内项目，请到对应景点单独预约。</div>

      <div class="park-bar">
        <van-button type="primary" round block :to="`/spots/${PARK_ID}/book`">立即预约入园</van-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 16px;
}

.park-name {
  font-family: var(--sg-font-display);
  font-size: 19px;
  font-weight: 700;
}

.park-desc {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--sg-text);
  white-space: pre-wrap;
}

.info .row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;

  span {
    color: var(--sg-text-secondary);
  }
}

.free {
  color: var(--sg-primary);
}

.note {
  margin: 0 12px;
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--sg-primary-weak);
  color: var(--sg-primary);
  font-size: 12px;
  line-height: 1.6;
}

.park {
  padding-bottom: 90px;
}

.park-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow-bar);
}
</style>
