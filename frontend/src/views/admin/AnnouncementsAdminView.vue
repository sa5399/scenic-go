<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from '@/utils/message'
import type { Announcement, PageResult } from '@/types/api'
import {
  createAnnouncement,
  deleteAnnouncement,
  getAdminAnnouncements,
  updateAnnouncement,
  updateAnnouncementStatus,
} from '@/api/admin'
import { formatTime } from '@/utils/format'

/** 公告维护：含下架公告的全量列表，支持新增 / 编辑 / 发布 / 下架 / 删除 */
const page = ref<PageResult<Announcement>>({ records: [], total: 0, current: 1, size: 10 })
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref<Announcement | null>(null) // null = 新增
const submitting = ref(false)
const form = reactive({ title: '', content: '' })

async function load(current = 1) {
  loading.value = true
  try {
    page.value = await getAdminAnnouncements({ current, size: page.value.size })
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
}

function openEdit(item: Announcement) {
  editing.value = item
  form.title = item.title
  form.content = item.content
  dialogVisible.value = true
}

async function onSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  submitting.value = true
  try {
    if (editing.value) {
      await updateAnnouncement(editing.value.id, { title: form.title.trim(), content: form.content })
      ElMessage.success('已保存')
    } else {
      await createAnnouncement({ title: form.title.trim(), content: form.content })
      ElMessage.success('已发布')
    }
    dialogVisible.value = false
    await load(page.value.current)
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    submitting.value = false
  }
}

async function onToggle(item: Announcement) {
  const next: 0 | 1 = item.status === 1 ? 0 : 1
  try {
    await updateAnnouncementStatus(item.id, next)
    ElMessage.success(next === 1 ? '已发布' : '已下架')
    await load(page.value.current)
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

async function onDelete(item: Announcement) {
  const confirmed = await ElMessageBox.confirm(
    `确定删除公告「${item.title}」吗？删除后不可恢复。`,
    '删除公告',
    { type: 'warning' },
  ).then(
    () => true,
    () => false,
  )
  if (!confirmed) {
    return
  }
  try {
    await deleteAnnouncement(item.id)
    ElMessage.success('已删除')
    await load(page.value.current)
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

onMounted(() => load(1))
</script>

<template>
  <el-card v-loading="loading">
    <div class="toolbar">
      <span class="note">列表含已下架公告，可重新发布或删除。</span>
      <el-button type="primary" @click="openCreate">新增公告</el-button>
    </div>

    <el-table :data="page.records">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '已下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="130">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row as Announcement)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            plain
            @click="onToggle(row as Announcement)"
          >
            {{ row.status === 1 ? '下架' : '发布' }}
          </el-button>
          <el-button size="small" type="danger" plain @click="onDelete(row as Announcement)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑公告' : '新增公告'" width="560px">
      <el-form label-width="56px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.note {
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
