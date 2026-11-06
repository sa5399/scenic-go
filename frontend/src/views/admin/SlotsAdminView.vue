<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from '@/utils/message'
import type { Slot, Spot } from '@/types/api'
import { getSlots } from '@/api/slot'
import { adjustSlotStock, getAdminSpots } from '@/api/admin'
import { fen2yuan } from '@/utils/format'

/**
 * 时段与库存：选景点 + 日期列出时段票种，直接设置时段总库存。
 * 列表返回的 remaining 是余量（Redis 口径）；调整接口按传入值设定 totalStock。
 */
const spots = ref<Spot[]>([])
const spotId = ref<number>()
const date = ref(dayjs().format('YYYY-MM-DD'))
const slots = ref<Slot[]>([])
const loading = ref(false)
const stockInputs = ref<Record<number, number>>({})
const adjustingId = ref(0)

async function loadSpots() {
  try {
    const page = await getAdminSpots({ current: 1, size: 100 })
    // 只有园区票和收费项目有可管理的时段库存；免费景点凭票游览，无库存概念
    spots.value = page.records.filter((s) => s.type === 2 || s.avgPrice > 0)
    if (page.records.length > 0) {
      spotId.value = page.records[0]?.id
      loadSlots()
    }
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

async function loadSlots() {
  if (!spotId.value || !date.value) {
    return
  }
  loading.value = true
  try {
    slots.value = await getSlots(spotId.value, date.value)
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

async function onAdjust(slot: Slot) {
  const value = stockInputs.value[slot.slotId]
  if (value === undefined || !Number.isInteger(value) || value < 0) {
    ElMessage.warning('请输入不小于 0 的整数库存')
    return
  }
  adjustingId.value = slot.slotId
  try {
    await adjustSlotStock(slot.slotId, value)
    ElMessage.success(`时段 ${slot.slotStart}-${slot.slotEnd} ${slot.ticketName} 总库存已设为 ${value}`)
    await loadSlots()
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    adjustingId.value = 0
  }
}

onMounted(loadSpots)
</script>

<template>
  <el-card>
    <el-form inline>
      <el-form-item label="景点">
        <el-select v-model="spotId" style="width: 220px" @change="loadSlots">
          <el-option v-for="s in spots" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="date"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          style="width: 150px"
          @change="loadSlots"
        />
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="slots">
      <el-table-column prop="slotId" label="时段ID" width="90" />
      <el-table-column label="时段" min-width="140">
        <template #default="{ row }">{{ row.slotStart }} - {{ row.slotEnd }}</template>
      </el-table-column>
      <el-table-column prop="ticketName" label="票种" min-width="110" />
      <el-table-column label="票价" width="100">
        <template #default="{ row }">¥{{ fen2yuan(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="remaining" label="余量" width="90" />
      <el-table-column prop="limitPerSlot" label="单笔限购" width="100" />
      <el-table-column label="调整总库存" min-width="220">
        <template #default="{ row }">
          <el-input-number
            v-model="stockInputs[row.slotId]"
            :min="0"
            :step="10"
            size="small"
            style="width: 120px"
          />
          <el-button
            size="small"
            type="primary"
            :loading="adjustingId === row.slotId"
            style="margin-left: 8px"
            @click="onAdjust(row as Slot)"
          >
            保存
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <p class="note">
      说明：余量 = 总库存 − 已占用（待支付 + 已预约 + 已核销），以 Redis 为准；保存即把该时段总库存设为输入值。
    </p>
  </el-card>
</template>

<style scoped lang="scss">
.note {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--sg-text-secondary);
}
</style>
