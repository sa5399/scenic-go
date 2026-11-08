<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { ReserveItem } from '@/types/api'
import { cancelReserve, getReserve, payReserve } from '@/api/reserve'
import { fen2yuan, formatTime, reserveStatusText } from '@/utils/format'
import { showConfirmDialog, showToast } from '@/utils/h5ui'

/** 支付收银台：15 分钟倒计时 + 模拟支付 / 取消订单 */
const route = useRoute()
const router = useRouter()

const orderNo = computed(() => String(route.params.orderNo))
const item = ref<ReserveItem | null>(null)
const loadError = ref('')
const paying = ref(false)
const remainMs = ref(0)
let timer: ReturnType<typeof setInterval> | undefined

const expired = computed(() => remainMs.value <= 0)

function startCountdown() {
  if (!item.value || item.value.status !== 0) {
    return
  }
  const expireAt = new Date(item.value.createTime).getTime() + 15 * 60 * 1000
  const tick = () => {
    remainMs.value = expireAt - Date.now()
    if (remainMs.value <= 0 && timer) {
      clearInterval(timer)
      timer = undefined
    }
  }
  tick()
  timer = setInterval(tick, 500)
}

async function load() {
  loadError.value = ''
  try {
    item.value = await getReserve(orderNo.value)
    startCountdown()
  } catch (e) {
    loadError.value = (e as Error).message
  }
}

async function onPay() {
  paying.value = true
  try {
    await payReserve(orderNo.value)
    showToast('支付成功')
    router.replace(`/reserves/${orderNo.value}`)
  } catch (e) {
    showToast((e as Error).message)
    await load()
  } finally {
    paying.value = false
  }
}

async function onCancel() {
  const confirmed = await showConfirmDialog({
    title: '取消订单',
    message: '确定取消该预约单吗？占用的库存将回补。',
  }).then(
    () => true,
    () => false,
  )
  if (!confirmed) {
    return
  }
  try {
    await cancelReserve(orderNo.value)
    showToast('已取消')
    router.replace('/reserves')
  } catch (e) {
    showToast((e as Error).message)
  }
}

onMounted(load)
onBeforeUnmount(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<template>
  <div class="pay">
    <van-nav-bar title="确认支付" left-arrow fixed placeholder @click-left="router.back()" />

    <van-empty v-if="loadError" image="network" :description="loadError">
      <van-button round type="primary" size="small" @click="load">重试</van-button>
    </van-empty>

    <template v-else-if="item">
      <div v-if="item.status === 0 && !expired" class="count-down">
        <van-count-down :time="remainMs" format="mm 分 ss 秒" />
        <div class="count-label">支付超时订单将自动关闭，库存回补</div>
      </div>

      <div class="card">
        <div class="row"><span>订单号</span><b>{{ item.orderNo }}</b></div>
        <div class="row"><span>景点</span><b>{{ item.spotName }}</b></div>
        <div class="row"><span>日期时段</span><b>{{ item.slotDate }} {{ item.slotStart }}-{{ item.slotEnd }}</b></div>
        <div class="row"><span>数量</span><b>{{ item.quantity }} 张</b></div>
        <div class="row">
          <span>金额</span>
          <b class="amount">¥{{ fen2yuan(item.totalAmount) }}</b>
        </div>
      </div>

      <template v-if="item.status === 0">
        <div v-if="!expired" class="card actions">
          <van-button type="primary" round block :loading="paying" @click="onPay">
            模拟支付 ¥{{ fen2yuan(item.totalAmount) }}
          </van-button>
          <van-button type="default" round block plain class="cancel" @click="onCancel">
            取消订单
          </van-button>
        </div>
        <van-empty v-else image="error" description="订单已超时关闭，库存已回补">
          <van-button round type="primary" size="small" to="/reserves">查看我的预约</van-button>
        </van-empty>
      </template>
      <div v-else class="card state-note">
        订单当前状态：{{ reserveStatusText[item.status] }} ·
        支付时间 {{ formatTime(item.payTime) }}
        <van-button round type="primary" size="small" class="state-btn" :to="`/reserves/${orderNo}`">
          查看核销码
        </van-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.count-down {
  text-align: center;
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 16px;

  :deep(.van-count-down) {
    font-family: var(--sg-font-display);
    font-size: 26px;
    font-weight: 700;
    color: var(--sg-accent);
  }
}

.count-label {
  margin-top: 6px;
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  font-size: 14px;

  span {
    color: var(--sg-text-secondary);
  }
}

.amount {
  color: var(--sg-primary);
  font-size: 16px;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.state-note {
  font-size: 13px;
  color: var(--sg-text-secondary);
  text-align: center;
}

.state-btn {
  display: block;
  margin: 10px auto 0;
}
</style>
