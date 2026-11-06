<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from '@/utils/message'
import type { AdminReserveItem, PageResult, ReserveStatus, Spot } from '@/types/api'
import { getAdminReserves, getAdminSpots } from '@/api/admin'
import { fen2yuan, formatTime, reserveStatusText } from '@/utils/format'

/** 订单查询：全部用户的预约单，可按景点与状态筛选（role=3） */
const page = ref<PageResult<AdminReserveItem>>({ records: [], total: 0, current: 1, size: 10 })
const spotId = ref<number>()
const status = ref<ReserveStatus | undefined>(undefined)
const spots = ref<Spot[]>([])
const range = ref<[string, string] | null>(null)
const loading = ref(false)

const statusOptions = [0, 1, 2, 3, 4].map((v) => ({ label: reserveStatusText[v as ReserveStatus], value: v }))

async function load(current = 1) {
  loading.value = true
  try {
    page.value = await getAdminReserves({
      spotId: spotId.value,
      status: status.value,
      startDate: range.value?.[0],
      endDate: range.value?.[1],
      current,
      size: page.value.size,
    })
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  load(1)
  try {
    const p = await getAdminSpots({ current: 1, size: 100 })
    // 只有园区票和收费项目会产生订单，免费景点（凭票游览）不进筛选
    spots.value = p.records.filter((s) => s.type === 2 || s.avgPrice > 0)
  } catch {
    // 筛选项加载失败不阻塞主表
  }
})
</script>

<template>
  <el-card>
    <el-form inline @submit.prevent="load(1)">
      <el-form-item label="景点">
        <el-select v-model="spotId" placeholder="全部" clearable style="width: 200px" @change="load(1)">
          <el-option v-for="s in spots" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="status" placeholder="全部" clearable style="width: 140px" @change="load(1)">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="page.records">
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="nickName" label="下单人" min-width="100">
        <template #default="{ row }">{{ row.nickName || `#${row.userId}` }}</template>
      </el-table-column>
      <el-table-column prop="spotName" label="景点" min-width="120" />
      <el-table-column label="日期 / 时段" min-width="170">
        <template #default="{ row }">{{ row.slotDate }} {{ row.slotStart }}-{{ row.slotEnd }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ fen2yuan(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ reserveStatusText[row.status as ReserveStatus] }}</template>
      </el-table-column>
      <el-table-column label="下单时间" min-width="130">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="prev, pager, next, total"
      :total="page.total"
      :page-size="page.size"
      :current-page="page.current"
      @current-change="load"
    />
  </el-card>
</template>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
