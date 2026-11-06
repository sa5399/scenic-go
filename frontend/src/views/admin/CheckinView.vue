<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from '@/utils/message'
import type { CheckinResult } from '@/types/api'
import { checkin } from '@/api/checkin'
import { formatTime } from '@/utils/format'

/** 核销工作台：输核销码 → POST /checkin（channel 固定 2 人工） */
const code = ref('')
const submitting = ref(false)
const result = ref<CheckinResult | null>(null)

async function onSubmit() {
  if (!code.value.trim()) {
    ElMessage.warning('请输入核销码')
    return
  }
  submitting.value = true
  result.value = null
  try {
    result.value = await checkin({ checkinCode: code.value.trim(), channel: 2 })
    ElMessage.success('核销成功')
    code.value = ''
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-card class="checkin-card">
    <el-form inline @submit.prevent="onSubmit">
      <el-form-item label="核销码">
        <el-input
          v-model="code"
          placeholder="CK 开头，支付成功后生成"
          style="width: 320px"
          clearable
          @keyup.enter="onSubmit"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="onSubmit">核销</el-button>
      </el-form-item>
    </el-form>

    <el-descriptions v-if="result" title="核销结果" :column="2" border class="checkin-result">
      <el-descriptions-item label="订单号">{{ result.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="景点">{{ result.spotName }}</el-descriptions-item>
      <el-descriptions-item label="数量">{{ result.quantity }} 张</el-descriptions-item>
      <el-descriptions-item label="核销时间">{{ formatTime(result.checkinTime) }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="输入核销码完成人工核销（后端不校验是否预约当天）" />
  </el-card>
</template>

<style scoped lang="scss">
.checkin-card {
  max-width: 760px;
}

.checkin-result {
  margin-top: 16px;
}
</style>
