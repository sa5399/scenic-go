<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import type { Slot } from '@/types/api'
import { getSlots } from '@/api/slot'
import { createReserve } from '@/api/reserve'
import { showToast } from '@/utils/h5ui'

/** 园区票预约：选日期 → 选时段（显示余量）→ 提交（免费，无票种） */
const route = useRoute()
const router = useRouter()
const spotId = computed(() => Number(route.params.id))

const dates = Array.from({ length: 7 }, (_, i) => dayjs().add(i, 'day'))
const dateIndex = ref(0)
const selectedDate = computed(
  () => dates[dateIndex.value]?.format('YYYY-MM-DD') ?? dayjs().format('YYYY-MM-DD'),
)

const slots = ref<Slot[]>([])
const loading = ref(false)
const loadError = ref(false)
const pickedSlot = ref<Slot | null>(null)
const submitting = ref(false)

/** 时段列表：每时段取成人票行代表（余量以它为准） */
const timeSlots = computed(() => slots.value.filter((s) => s.ticketType === 1))

async function load() {
  loading.value = true
  loadError.value = false
  pickedSlot.value = null
  try {
    slots.value = await getSlots(spotId.value, selectedDate.value)
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function pickDate(index: number) {
  if (dateIndex.value === index) {
    return
  }
  dateIndex.value = index
  load()
}

async function onSubmit() {
  if (!pickedSlot.value) {
    showToast('请先选择一个入园时段')
    return
  }
  submitting.value = true
  try {
    await createReserve({ slotId: pickedSlot.value.slotId, quantity: 1 })
    showToast('入园预约成功，凭核销码入园')
    router.push('/reserves')
  } catch (e) {
    showToast((e as Error).message)
  } finally {
    submitting.value = false
  }
}

function dateLabel(d: Dayjs): string {
  const iso = d.format('YYYY-MM-DD')
  if (iso === dayjs().format('YYYY-MM-DD')) {
    return '今天'
  }
  if (iso === dayjs().add(1, 'day').format('YYYY-MM-DD')) {
    return '明天'
  }
  return d.format('MM-DD')
}

onMounted(load)
</script>

<template>
  <div class="park-book">
    <van-nav-bar title="入园预约" left-arrow fixed placeholder @click-left="router.back()" />

    <p v-if="loading" class="hint">加载中…</p>
    <van-empty v-else-if="loadError" image="network" description="时段加载失败">
      <van-button round type="primary" size="small" @click="load">重试</van-button>
    </van-empty>

    <template v-else>
      <div class="card">
        <div class="section-title">选择日期</div>
        <div class="date-row">
          <button
            v-for="(d, index) in dates"
            :key="d.format('YYYY-MM-DD')"
            class="date-chip"
            :class="{ 'date-chip--on': dateIndex === index }"
            @click="pickDate(index)"
          >
            <span>{{ dateLabel(d) }}</span>
            <span class="date-week">{{ d.format('ddd') }}</span>
          </button>
        </div>
      </div>

      <div class="card">
        <div class="section-title">选择入园时段（{{ selectedDate }}）</div>
        <div
          v-for="s in timeSlots"
          :key="s.slotId"
          class="slot-row"
          :class="{ 'slot-row--on': pickedSlot?.slotId === s.slotId }"
          @click="pickedSlot = s"
        >
          <div class="slot-time-big">{{ s.slotStart }} - {{ s.slotEnd }}</div>
          <div class="slot-remain">余 {{ s.remaining }} 人</div>
        </div>
        <p class="tip">免费入园，分时段控制客流；凭核销码在预约时段核销入园。</p>
      </div>

      <div class="bar">
        <van-button type="primary" round block :loading="submitting" @click="onSubmit">
          提交预约
        </van-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.hint {
  text-align: center;
  color: var(--sg-text-secondary);
  padding: 40px 0;
}

.card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.date-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar {
    display: none;
  }
}

.date-chip {
  flex-shrink: 0;
  width: 56px;
  padding: 8px 0;
  border: none;
  border-radius: 10px;
  background: var(--sg-fill);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;

  &--on {
    background: var(--sg-primary-weak);

    span {
      color: var(--sg-primary);
      font-weight: 600;
    }
  }
}

.date-week {
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.slot-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: var(--sg-fill);
  margin-bottom: 8px;
  cursor: pointer;

  &--on {
    border-color: var(--sg-primary);
    background: var(--sg-primary-tint);
  }
}

.slot-time-big {
  font-size: 15px;
  font-weight: 600;
}

.slot-remain {
  font-size: 13px;
  color: var(--sg-text-secondary);
}

.tip {
  font-size: 12px;
  line-height: 1.6;
  color: var(--sg-text-secondary);
  margin: 4px 0 0;
}

.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow-bar);
}
</style>
