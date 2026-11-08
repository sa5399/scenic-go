<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from '@/utils/message'
import type { PageResult, Spot } from '@/types/api'
import { getAdminSpots, updateSpot, updateSpotStatus } from '@/api/admin'
import { fen2yuan } from '@/utils/format'

/** 景点管理：含下架景点的全量列表，支持上下架与基础信息编辑（名称/简介/价格） */
const page = ref<PageResult<Spot>>({ records: [], total: 0, current: 1, size: 10 })
const keyword = ref('')
const status = ref<0 | 1 | undefined>(undefined)
const loading = ref(false)
const switchingId = ref(0)

const statusOptions = [
  { label: '上架中', value: 1 as const },
  { label: '已下架', value: 0 as const },
]

// 编辑弹窗
const dialogVisible = ref(false)
const editing = ref<Spot | null>(null)
const submitting = ref(false)
const form = reactive({ name: '', summary: '', priceYuan: 0 })

async function load(current = 1) {
  loading.value = true
  try {
    const result = await getAdminSpots({
      current,
      size: page.value.size,
      keyword: keyword.value || undefined,
      status: status.value,
    })
    // 园区票（type=2）不是景点，不参与上下架与信息编辑
    const visible = result.records.filter((s) => s.type !== 2)
    page.value = { records: visible, total: result.total - (result.records.length - visible.length), current: result.current, size: result.size }
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}


async function onToggle(spot: Spot) {
  const next: 0 | 1 = spot.status === 1 ? 0 : 1
  switchingId.value = spot.id
  try {
    await updateSpotStatus(spot.id, next)
    ElMessage.success(next === 1 ? '已上架' : '已下架')
    await load(page.value.current)
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    switchingId.value = 0
  }
}

function openEdit(spot: Spot) {
  editing.value = spot
  form.name = spot.name
  form.summary = spot.summary
  form.priceYuan = spot.avgPrice / 100
  dialogVisible.value = true
}

async function onSave() {
  if (!editing.value) {
    return
  }
  if (!form.name.trim() || !form.summary.trim()) {
    ElMessage.warning('名称和简介不能为空')
    return
  }
  submitting.value = true
  try {
    await updateSpot(editing.value.id, {
      name: form.name.trim(),
      summary: form.summary.trim(),
      avgPrice: Math.round(form.priceYuan * 100),
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load(page.value.current)
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    submitting.value = false
  }
}

onMounted(() => load(1))
</script>

<template>
  <el-card>
    <el-form inline @submit.prevent="load(1)">
      <el-form-item label="关键字">
        <el-input v-model="keyword" placeholder="景点名称 / 简介" clearable style="width: 220px" @keyup.enter="load(1)" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="status" placeholder="全部" clearable style="width: 120px" @change="load(1)">
          <el-option v-for="o in statusOptions" :key="o.label" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load(1)">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="page.records">
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="summary" label="简介" min-width="220" show-overflow-tooltip />
      <el-table-column label="价格" width="100">
        <template #default="{ row }">{{ row.avgPrice > 0 ? `¥${fen2yuan(row.avgPrice)}` : '免费' }}</template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">{{ { 1: '景点', 0: '展馆', 2: '园区' }[row.type as 0 | 1 | 2] }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            inline-prompt
            active-text="上架"
            inactive-text="下架"
            :disabled="row.status === undefined"
            :loading="switchingId === row.id"
            @change="onToggle(row as Spot)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row as Spot)">编辑</el-button>
        </template>
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

    <el-dialog v-model="dialogVisible" title="编辑景点" width="520px">
      <el-form label-width="56px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.priceYuan" :min="0" :precision="2" :step="10" style="width: 180px" />
          <span class="unit">元（0 表示免费，库存单价的分值自动换算）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.unit {
  margin-left: 10px;
  font-size: 12px;
  color: var(--sg-text-secondary);
}
</style>
