<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from '@/utils/message'
import type { CheckinLog, PageResult } from '@/types/api'
import { getAdminSpots } from '@/api/admin'
import { getCheckinLogs } from '@/api/checkin'
import { checkinChannelText, formatTime } from '@/utils/format'

/** 核销流水（role=3）：骨架版——默认查询 + 表格 + 简单分页；景点/日期筛选下一迭代补 */
const page = ref<PageResult<CheckinLog>>({ records: [], total: 0, current: 1, size: 10 })
const loading = ref(false)
const spotId = ref<number>()
const range = ref<[string, string] | null>(null)
const spots = ref<{ id: number; name: string }[]>([])

async function load(current = 1) {
  loading.value = true
  try {
    page.value = await getCheckinLogs({
      current,
      size: page.value.size,
      spotId: spotId.value,
      startDate: range.value?.[0],
      endDate: range.value?.[1],
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
    <p v-if="loading" class="logs-loading">加载中…</p>
    <el-table v-else :data="page.records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="spotName" label="景点" min-width="120" />
      <el-table-column prop="userId" label="下单人" width="90" />
      <el-table-column prop="operatorId" label="核销员" width="90">
        <template #default="{ row }">{{ row.operatorId === 0 ? '闸机' : row.operatorId }}</template>
      </el-table-column>
      <el-table-column label="渠道" width="100">
        <template #default="{ row }">{{ checkinChannelText[row.channel as 1 | 2 | 3] }}</template>
      </el-table-column>
      <el-table-column label="核销时间" min-width="130">
        <template #default="{ row }">{{ formatTime(row.checkinTime) }}</template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="logs-pager"
      layout="prev, pager, next, total"
      :total="page.total"
      :page-size="page.size"
      :current-page="page.current"
      @current-change="load"
    />
  </el-card>
</template>

<style scoped lang="scss">
.logs-loading {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 16px 0;
}

.logs-pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
