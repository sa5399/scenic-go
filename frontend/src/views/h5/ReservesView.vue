<script setup lang="ts">
defineOptions({ name: 'ReservesView' })
import { onActivated, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { ReserveItem, ReserveStatus } from '@/types/api'
import { cancelReserve, getReserves, payReserve } from '@/api/reserve'
import { fen2yuan, formatTime, reserveStatusText } from '@/utils/format'
import { showConfirmDialog, showToast } from '@/utils/h5ui'

/**
 * 我的预约：状态 tab 筛选 + van-list 分页 + 快捷操作（去支付 / 取消）。
 */
const SIZE = 10

const router = useRouter()

const statusTabs: Array<{ label: string; value?: ReserveStatus }> = [
  { label: '全部', value: undefined },
  { label: '待支付', value: 0 },
  { label: '已预约', value: 1 },
  { label: '已核销', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已过期', value: 4 },
]
const activeStatus = ref<ReserveStatus | undefined>(undefined)

const items = ref<ReserveItem[]>([])
const total = ref(0)
const current = ref(0)
const loading = ref(false)
const finished = ref(false)
const loadError = ref(false)
const firstError = ref(false)

async function loadPage() {
  loading.value = true
  loadError.value = false
  try {
    const page = await getReserves({
      status: activeStatus.value,
      current: current.value + 1,
      size: SIZE,
    })
    items.value.push(...page.records)
    total.value = page.total
    current.value = page.current
    finished.value = items.value.length >= page.total
  } catch {
    loadError.value = true
    if (current.value === 0) {
      firstError.value = true
    }
  } finally {
    loading.value = false
  }
}

function reset() {
  items.value = []
  total.value = 0
  current.value = 0
  finished.value = false
  firstError.value = false
  loadError.value = false
  loading.value = false
}

function pickTab(value?: ReserveStatus) {
  if (activeStatus.value === value) {
    return
  }
  activeStatus.value = value
  reset()
  if (items.value.length === 0 && !loading.value && !finished.value && !firstError.value) {
    loadPage()
  }
}

const statusClass: Record<ReserveStatus, string> = {
  0: 'is-wait',
  1: 'is-ok',
  2: 'is-done',
  3: 'is-muted',
  4: 'is-muted',
}

async function onPay(item: ReserveItem) {
  try {
    await payReserve(item.orderNo)
    showToast('支付成功')
    reset()
    loadPage()
  } catch (e) {
    showToast((e as Error).message)
  }
}

async function onCancel(item: ReserveItem) {
  const confirmed = await showConfirmDialog({
    title: '取消预约',
    message: `确定取消 ${item.spotName}（${item.slotDate}）的预约吗？`,
  }).then(
    () => true,
    () => false,
  )
  if (!confirmed) {
    return
  }
  try {
    await cancelReserve(item.orderNo)
    showToast('已取消')
    reset()
    loadPage()
  } catch (e) {
    showToast((e as Error).message)
  }
}
// keep-alive 缓存了本页：每次激活（含首次进入）都重新拉取并回到"全部"筛选，
// 避免停留在某个状态 tab 时看不到新出的单（如刚确认的已预约园区单）
onActivated(() => {
  activeStatus.value = undefined
  reset()
  loadPage()
})
</script>

<template>
  <div class="reserves">
    <van-nav-bar title="我的预约" left-arrow fixed placeholder @click-left="$router.back()" />
    <div class="reserves-tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.label"
        class="rtab"
        :class="{ 'rtab--on': activeStatus === tab.value }"
        @click="pickTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <van-empty v-if="firstError" image="network" description="加载失败">
      <van-button round type="primary" size="small" @click="loadPage">重试</van-button>
    </van-empty>

    <van-list
      v-else
      v-model:loading="loading"
      :finished="finished"
      :error="loadError"
      error-text="加载失败，点击重试"
      finished-text="没有更多了"
      @load="loadPage"
    >
      <div
        v-for="item in items"
        :key="item.orderNo"
        class="r-card"
        @click="router.push(`/reserves/${item.orderNo}`)"
      >
        <div class="r-head">
          <span class="r-spot">{{ item.spotName }}</span>
          <span class="r-status" :class="statusClass[item.status]">
            {{ reserveStatusText[item.status] }}
          </span>
        </div>
        <div class="r-info">
          <span>{{ item.slotDate }} {{ item.slotStart }}-{{ item.slotEnd }}</span>
          <span>{{ item.quantity }} 张</span>
          <span class="r-amount">¥{{ fen2yuan(item.totalAmount) }}</span>
        </div>
        <div class="r-foot">
          <span class="r-time">下单 {{ formatTime(item.createTime) }}</span>
          <span class="r-actions" @click.stop>
            <van-button
              v-if="item.status === 0"
              size="small"
              round
              type="primary"
              @click="onPay(item)"
            >
              {{ item.totalAmount > 0 ? '去支付' : '确认预约' }}
            </van-button>
            <van-button
              v-if="item.status === 0 || item.status === 1"
              size="small"
              round
              type="default"
              plain
              @click="onCancel(item)"
            >
              取消
            </van-button>
          </span>
        </div>
      </div>
      <van-empty v-if="finished && items.length === 0" description="还没有预约记录" />
    </van-list>
  </div>
</template>

<style scoped lang="scss">
.reserves {
  padding-bottom: 16px;
}

.reserves-tabs {
  display: flex;
  gap: 8px;
  padding: 12px;
  overflow-x: auto;
  background: var(--sg-surface);

  &::-webkit-scrollbar {
    display: none;
  }
}

.rtab {
  flex-shrink: 0;
  border: none;
  background: var(--sg-fill);
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;

  &--on {
    background: var(--sg-primary-weak);
    color: var(--sg-primary);
    font-weight: 600;
  }
}

.r-card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 0 12px 12px;
  padding: 14px;
  cursor: pointer;
}

.r-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.r-spot {
  font-size: 15px;
  font-weight: 600;
}

.r-status {
  font-size: 12px;
  font-weight: 600;

  &.is-wait {
    color: var(--sg-accent);
  }

  &.is-ok {
    color: var(--sg-primary);
  }

  &.is-done {
    color: #1989fa;
  }

  &.is-muted {
    color: var(--sg-text-secondary);
  }
}

.r-info {
  margin-top: 8px;
  display: flex;
  gap: 10px;
  font-size: 13px;
  color: var(--sg-text-secondary);
}

.r-amount {
  color: var(--sg-text);
  font-weight: 600;
}

.r-foot {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.r-time {
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.r-actions {
  display: flex;
  gap: 8px;
}
</style>
