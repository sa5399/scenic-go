<script setup lang="ts">
defineOptions({ name: 'HomeView' })
import { onMounted, ref, watch } from 'vue'
import type { Spot, SpotType } from '@/types/api'
import { getSpots } from '@/api/spot'
import { useTagStore } from '@/stores/tags'
import SpotCard from '@/components/SpotCard.vue'
import { getAnnouncements } from '@/api/announcement'
import type { Announcement } from '@/types/api'
import { showConfirmDialog } from '@/utils/h5ui'

/**
 * 首页：搜索 + 类型/标签筛选 + van-list 上拉分页。
 * 筛选语义与后端一致：tagIds 多选为「全部命中」（AND）。
 */
const SIZE = 10

const keyword = ref('')
const type = ref<SpotType | undefined>(undefined)
const selectedTagIds = ref<number[]>([])
const tagStore = useTagStore()

const spots = ref<Spot[]>([])
const notice = ref<Announcement | null>(null)
const total = ref(0)
const current = ref(0) // 已加载到的页码
const loading = ref(false)
const finished = ref(false)
const loadError = ref(false) // 翻页失败（van-list 点击重试）
const firstError = ref(false) // 首屏失败（整页错误态）

const typeOptions: Array<{ label: string; value?: SpotType }> = [
  { label: '全部', value: undefined },
  { label: '景点', value: 1 },
  { label: '展馆', value: 0 },
]

async function loadPage() {
  loading.value = true
  loadError.value = false
  try {
    const page = await getSpots({
      current: current.value + 1,
      size: SIZE,
      keyword: keyword.value || undefined,
      type: type.value,
      tagIds: selectedTagIds.value.length > 0 ? selectedTagIds.value : undefined,
    })
    // 园区票（type=2）有专属入口，不进景点列表；后端旧构建未过滤时这里兜底
    const visible = page.records.filter((s) => s.type !== 2)
    total.value = page.total - (page.records.length - visible.length)
    spots.value.push(...visible)
    current.value = page.current
    finished.value = spots.value.length >= total.value
  } catch {
    loadError.value = true
    if (current.value === 0) {
      firstError.value = true
    }
  } finally {
    loading.value = false
  }
}

/** 筛选条件变化：清空重来，van-list 发现内容不足一屏会自动触发下一次 load */
function reset() {
  spots.value = []
  total.value = 0
  current.value = 0
  finished.value = false
  firstError.value = false
  loadError.value = false
  loading.value = false
}

function pickType(value?: SpotType) {
  if (type.value === value) {
    return
  }
  type.value = value
  reset()
}

function toggleTag(id: number) {
  // 重新赋值而不是 splice：保证 watch 兜底能感知筛选变化
  selectedTagIds.value = selectedTagIds.value.includes(id)
    ? selectedTagIds.value.filter((t) => t !== id)
    : [...selectedTagIds.value, id]
  reset()
}

onMounted(() => {
  // 标签加载失败不阻塞列表，下次进入页面自动重试
  tagStore.ensure().catch(() => {})
  // 公告条：只展示最新一条，加载失败静默隐藏
  getAnnouncements()
    .then((list) => {
      notice.value = list[0] ?? null
    })
    .catch(() => {})
})

function showNotice() {
  if (!notice.value) {
    return
  }
  showConfirmDialog({
    title: notice.value.title,
    message: notice.value.content || notice.value.title,
    showCancelButton: false,
    confirmButtonText: '知道了',
  }).catch(() => {})
}

// 首屏交给 van-list 的自动 load，这里只处理极端情况：重试首屏
function retryFirst() {
  firstError.value = false
  loadPage()
}

// 类型切换后若列表已满一屏，van-list 不会自动再 load，这里兜底触发
watch([type, selectedTagIds], () => {
  if (spots.value.length === 0 && !loading.value && !finished.value && !firstError.value) {
    loadPage()
  }
})
</script>

<template>
  <div class="home">
    <van-notice-bar
      v-if="notice"
      left-icon="volume-o"
      :text="notice.title"
      class="home-notice"
      @click="showNotice"
    />
    <router-link to="/park" class="park-entry">
      <span class="park-text">
        <span class="park-name">云栖湖景区入园预约</span>
        <span class="park-sub">免费 · 分时入园 · 凭码核销</span>
      </span>
      <span class="park-arrow">›</span>
    </router-link>
    <van-search
      v-model="keyword"
      placeholder="搜索景点 / 展馆"
      shape="round"
      @search="reset"
      @clear="reset"
    />

    <div class="filters">
      <div class="type-row">
        <button
          v-for="opt in typeOptions"
          :key="opt.label"
          class="chip"
          :class="{ 'chip--on': type === opt.value }"
          @click="pickType(opt.value)"
        >
          {{ opt.label }}
        </button>
        <span class="filter-count">共 {{ total }} 个</span>
      </div>

      <div v-if="tagStore.tags.length" class="tag-row">
        <button
          v-for="tag in tagStore.tags"
          :key="tag.id"
          class="chip chip--tag"
          :class="{ 'chip--on': selectedTagIds.includes(tag.id) }"
          @click="toggleTag(tag.id)"
        >
          {{ tag.name }}
        </button>
      </div>
    </div>

    <van-empty v-if="firstError" image="network" description="加载失败，请确认后端已启动">
      <van-button round type="primary" size="small" @click="retryFirst">重试</van-button>
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
      <template v-if="spots.length === 0 && loading">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <div class="sk-img" />
          <div class="sk-body">
            <div class="sk-line w60" />
            <div class="sk-line w90" />
            <div class="sk-line w40" />
          </div>
        </div>
      </template>
      <SpotCard v-for="spot in spots" :key="spot.id" :spot="spot" />
      <van-empty v-if="finished && spots.length === 0" description="没有符合条件的景点 / 展馆" />
    </van-list>
  </div>
</template>

<style scoped lang="scss">
.home {
  padding-bottom: 16px;
}

.park-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px 12px 0;
  padding: 14px 16px;
  border-radius: var(--sg-radius);
  background: linear-gradient(135deg, #235e52 0%, #2e7d6d 60%, #3d8677 100%);
  color: #fff;
  text-decoration: none;
}

.park-text {
  flex: 1;
  min-width: 0;
}

.park-name {
  display: block;
  font-size: 15px;
  font-weight: 700;
}

.park-sub {
  display: block;
  margin-top: 3px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.78);
}

.park-arrow {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
}

.home-notice {
  --van-notice-bar-background: var(--sg-primary-weak);
  --van-notice-bar-text-color: var(--sg-primary);
}

.skeleton-card {
  display: flex;
  gap: 12px;
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 0 12px 12px;
  padding: 12px;
}

.sk-img {
  width: 112px;
  height: 88px;
  border-radius: 8px;
  flex-shrink: 0;
}

.sk-body {
  flex: 1;
  padding-top: 4px;
}

.sk-line {
  height: 13px;
  border-radius: 6px;
  margin-bottom: 10px;
}

.w40 { width: 40%; }
.w60 { width: 60%; }
.w70 { width: 70%; }
.w90 { width: 90%; }

.sk-img,
.sk-line {
  background: linear-gradient(90deg, #eef1ef 25%, #f7f8f8 45%, #eef1ef 65%);
  background-size: 200% 100%;
  animation: sg-shimmer 1.2s infinite;
}

@keyframes sg-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.filters {
  padding: 0 12px 12px;
}

.type-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-count {
  margin-left: auto;
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.tag-row {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar {
    display: none;
  }
}

.chip {
  flex-shrink: 0;
  border: none;
  background: var(--sg-fill);
  color: var(--sg-text);
  font-size: 13px;
  padding: 5px 14px;
  border-radius: 999px;
  cursor: pointer;

  &--on {
    background: var(--sg-primary-weak);
    color: var(--sg-primary);
    font-weight: 600;
  }

  &--tag {
    font-size: 12px;
    padding: 4px 12px;
  }
}
</style>
