<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import type { Slot, Spot } from '@/types/api'
import { getSpot } from '@/api/spot'
import { getSlots } from '@/api/slot'
import { createReserve } from '@/api/reserve'
import { fen2yuan } from '@/utils/format'
import { showToast } from '@/utils/h5ui'
import CoverBlock from '@/components/CoverBlock.vue'
import ParkBookView from '@/views/h5/ParkBookView.vue'

/**
 * 园内项目预约下单（两步向导）：
 * ① 选日期与时段（显示余量，单选）→ 确定
 * ② 选票种（成人 / 儿童 / 学生，显示价格）→ 提交生成一张待支付单
 * 园区票（id=13）分流到 ParkBookView 一页式预约（免费，无票种）。
 */
const route = useRoute()
const router = useRouter()
const spotId = computed(() => Number(route.params.id))
const isPark = computed(() => route.params.id === '13')

const spot = ref<Spot | null>(null)

const dates = Array.from({ length: 7 }, (_, i) => dayjs().add(i, 'day'))
const dateIndex = ref(0)
const selectedDate = computed(
  () => dates[dateIndex.value]?.format('YYYY-MM-DD') ?? dayjs().format('YYYY-MM-DD'),
)

const slots = ref<Slot[]>([])
const slotsLoading = ref(false)
const slotsError = ref(false)
const step = ref<'slot' | 'ticket'>('slot')
const pickedSlot = ref<Slot | null>(null) // 时段代表行（该时段第一行）
const pickedTicket = ref<Slot | null>(null)
const submitting = ref(false)

/** 时段列表：每时段取一行代表（显示时段与余量） */
const timeSlots = computed(() => {
  const seen = new Set<string>()
  return slots.value.filter((s) => {
    const key = `${s.slotStart}-${s.slotEnd}`
    if (seen.has(key)) {
      return false
    }
    seen.add(key)
    return true
  })
})

/** 已选时段下的三种票 */
const ticketRows = computed(() =>
  pickedSlot.value
    ? slots.value.filter(
        (s) => s.slotStart === pickedSlot.value?.slotStart && s.slotEnd === pickedSlot.value?.slotEnd,
      )
    : [],
)

const pickedTime = computed(() => `${pickedSlot.value?.slotStart} - ${pickedSlot.value?.slotEnd}`)

async function loadSpot() {
  try {
    spot.value = await getSpot(spotId.value)
  } catch {
    // 详情失败不阻塞下单流程
  }
}

async function loadSlots() {
  slotsLoading.value = true
  slotsError.value = false
  pickedSlot.value = null
  pickedTicket.value = null
  step.value = 'slot'
  try {
    slots.value = await getSlots(spotId.value, selectedDate.value)
  } catch {
    slotsError.value = true
  } finally {
    slotsLoading.value = false
  }
}

function pickDate(index: number) {
  if (dateIndex.value === index) {
    return
  }
  dateIndex.value = index
  loadSlots()
}

function confirmSlot() {
  if (!pickedSlot.value) {
    showToast('请先选择一个时段')
    return
  }
  pickedTicket.value = null
  step.value = 'ticket'
}

function onClickLeft() {
  if (step.value === 'ticket') {
    step.value = 'slot'
    return
  }
  router.back()
}

async function onSubmit() {
  if (!pickedTicket.value) {
    showToast('请先选择票种')
    return
  }
  submitting.value = true
  try {
    const order = await createReserve({ slotId: pickedTicket.value.slotId, quantity: 1 })
    router.push(`/reserves/${order.orderNo}/pay`)
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

onMounted(() => {
  loadSpot()
  loadSlots()
})
</script>

<template>
  <ParkBookView v-if="isPark" />
  <div v-else class="book">
    <van-nav-bar
      :title="step === 'slot' ? '预约下单' : '选择票种'"
      left-arrow
      fixed
      placeholder
      @click-left="onClickLeft"
    />

    <p v-if="slotsLoading" class="book-hint">加载中…</p>
    <van-empty v-else-if="slotsError" image="network" description="时段加载失败">
      <van-button round type="primary" size="small" @click="loadSlots">重试</van-button>
    </van-empty>

    <!-- 第一步：选日期与时段 -->
    <template v-else-if="step === 'slot'">
      <div v-if="spot" class="card book-head">
        <CoverBlock :src="spot.coverImg" :label="spot.name" width="64" height="52" radius="6" />
        <div class="book-head-name">{{ spot.name }}</div>
      </div>

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
        <div class="section-title">选择时段（{{ selectedDate }}）</div>
        <div
          v-for="s in timeSlots"
          :key="`${s.slotStart}-${s.slotEnd}`"
          class="slot-row"
          :class="{ 'slot-row--on': pickedSlot?.slotStart === s.slotStart }"
          @click="pickedSlot = s"
        >
          <div class="slot-time-big">{{ s.slotStart }} - {{ s.slotEnd }}</div>
          <div class="slot-remain">余 {{ s.remaining }}</div>
        </div>
        <van-empty v-if="timeSlots.length === 0" description="这一天暂无可约时段" />
      </div>

      <div class="book-bar">
        <van-button
          type="primary"
          round
          block
          :disabled="!pickedSlot"
          @click="confirmSlot"
        >
          确定
        </van-button>
      </div>
    </template>

    <!-- 第二步：选票种 -->
    <template v-else>
      <div class="card">
        <div class="section-title">入园时段：{{ pickedTime }}</div>
        <div
          v-for="s in ticketRows"
          :key="s.slotId"
          class="ticket-row"
          :class="{ 'ticket-row--on': pickedTicket?.slotId === s.slotId }"
          @click="pickedTicket = s"
        >
          <span class="ticket-name">{{ s.ticketName }}</span>
          <span class="ticket-price">¥{{ fen2yuan(s.price) }}</span>
        </div>
      </div>

      <div class="note">提交后生成一张待支付预约单，在"我的预约"里完成支付。</div>

      <div class="book-bar">
        <van-button type="primary" round block :loading="submitting" @click="onSubmit">
          提交预约
        </van-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.book {
  padding-bottom: 88px;
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

.book-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-head-name {
  font-size: 16px;
  font-weight: 600;
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

.book-hint {
  text-align: center;
  color: var(--sg-text-secondary);
  padding: 8px 0;
  font-size: 12px;
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
}

.slot-row--on {
  border-color: var(--sg-primary);
  background: var(--sg-primary-tint);
}

.slot-time-big {
  font-size: 15px;
  font-weight: 600;
}

.slot-remain {
  font-size: 13px;
  color: var(--sg-text-secondary);
}

.ticket-row {
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

.ticket-name {
  font-size: 14px;
  font-weight: 600;
}

.ticket-price {
  font-size: 14px;
  font-weight: 600;
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

.book-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow-bar);
}
</style>
