<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { ReserveItem } from '@/types/api'
import { cancelReserve, getReserve, payReserve } from '@/api/reserve'
import { fen2yuan, formatTime, reserveStatusText } from '@/utils/format'
import { showConfirmDialog, showToast } from '@/utils/h5ui'

const route = useRoute()
const router = useRouter()

const orderNo = computed(() => String(route.params.orderNo))
const item = ref<ReserveItem | null>(null)
const loadError = ref('')
const paying = ref(false)

async function load() {
  loadError.value = ''
  try {
    item.value = await getReserve(orderNo.value)
  } catch (e) {
    loadError.value = (e as Error).message
  }
}

async function onPay() {
  paying.value = true
  try {
    await payReserve(orderNo.value)
    showToast('支付成功')
    await load()
  } catch (e) {
    showToast((e as Error).message)
  } finally {
    paying.value = false
  }
}

async function onCancel() {
  const confirmed = await showConfirmDialog({
    title: '取消预约',
    message: `确定取消 ${item.value?.spotName ?? ''} 的预约吗？占用的库存将回补。`,
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
    await load()
  } catch (e) {
    showToast((e as Error).message)
  }
}

/** 复制核销码：优先剪贴板 API，非安全上下文回退 execCommand（手机模拟/局域网 HTTP 也能用） */
async function copyCode() {
  const code = item.value?.checkinCode
  if (!code) {
    return
  }
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(code)
    } else {
      const ta = document.createElement('textarea')
      ta.value = code
      ta.style.position = 'fixed'
      ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
    }
    showToast('核销码已复制，去管理端粘贴核销')
  } catch {
    showToast('复制失败，请手动抄写核销码')
  }
}

onMounted(load)
</script>

<template>
  <div class="rd">
    <van-nav-bar title="订单详情" left-arrow fixed placeholder @click-left="router.back()" />

    <van-empty v-if="loadError" image="network" :description="loadError">
      <van-button round type="primary" size="small" @click="load">重试</van-button>
    </van-empty>

    <div v-else-if="!item" class="rd-loading">加载中…</div>

    <template v-else>
      <div class="card rd-status">
        <div class="rd-status-text">{{ reserveStatusText[item.status] }}</div>
        <div class="rd-status-sub">订单号 {{ item.orderNo }}</div>
      </div>

      <div v-if="item.checkinCode" class="card rd-code">
        <div class="rd-code-label">入园核销码（核销后失效）</div>
        <div class="rd-code-num" @click="copyCode">{{ item.checkinCode }}</div>
        <van-button size="small" round plain type="primary" class="rd-code-copy" @click="copyCode">
          复制核销码
        </van-button>
      </div>

      <div class="card">
        <div class="rd-row"><span>景点</span><b>{{ item.spotName }}</b></div>
        <div class="rd-row"><span>日期</span><b>{{ item.slotDate }}</b></div>
        <div class="rd-row"><span>时段</span><b>{{ item.slotStart }} - {{ item.slotEnd }}</b></div>
        <div class="rd-row"><span>数量</span><b>{{ item.quantity }} 张</b></div>
        <div class="rd-row">
          <span>金额</span>
          <b class="rd-amount">{{ item.totalAmount > 0 ? `¥${fen2yuan(item.totalAmount)}` : '免费' }}</b>
        </div>
      </div>

      <div class="card">
        <div class="rd-row"><span>下单时间</span><b>{{ formatTime(item.createTime) }}</b></div>
        <div class="rd-row"><span>支付时间</span><b>{{ formatTime(item.payTime) }}</b></div>
        <div class="rd-row"><span>核销时间</span><b>{{ formatTime(item.checkinTime) }}</b></div>
        <div class="rd-row"><span>取消时间</span><b>{{ formatTime(item.cancelTime) }}</b></div>
      </div>

      <div
        v-if="item.status === 0 || item.status === 1"
        class="rd-actions"
      >
        <van-button v-if="item.status === 0" type="primary" round :loading="paying" @click="onPay">
          {{ item.totalAmount > 0 ? `去支付 ¥${fen2yuan(item.totalAmount)}` : '确认预约' }}
        </van-button>
        <van-button type="default" round plain @click="onCancel">取消预约</van-button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.rd-loading {
  text-align: center;
  padding: 80px 0;
  color: var(--sg-text-secondary);
}

.rd-status {
  text-align: center;
}

.rd-status-text {
  font-family: var(--sg-font-display);
  font-size: 20px;
  font-weight: 700;
}

.rd-status-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.rd-code {
  text-align: center;
  border: 1px dashed var(--sg-primary-border);
}

.rd-code-label {
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.rd-code-copy {
  margin-top: 10px;
}

.rd-code-num {
  margin-top: 6px;
  user-select: all;
  cursor: pointer;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--sg-primary);
}

.rd-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;

  span {
    color: var(--sg-text-secondary);
  }
}

.rd-amount {
  color: var(--sg-primary);
}

.rd-actions {
  display: flex;
  gap: 12px;
  margin: 16px 12px;

  .van-button {
    flex: 1;
  }
}
</style>
