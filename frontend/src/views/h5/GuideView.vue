<script setup lang="ts">
defineOptions({ name: 'GuideView' })
import { computed, onMounted, ref, watch } from 'vue'
import { useGeolocation } from '@vueuse/core'
import type { Crowd, Facility, FacilityType, RouteResult, RouteSpot, Spot } from '@/types/api'
import { recommendRoute } from '@/api/guide'
import { getNearby } from '@/api/facility'
import { getSpots } from '@/api/spot'
import { useTagStore } from '@/stores/tags'
import { facilityTypeText } from '@/utils/format'
import { showToast } from '@/utils/h5ui'

/**
 * 导览页两个 tab：
 * - 路线推荐：标签（OR 命中）+ 人群 + 时间预算 → POST /guide/route
 * - 附近设施：浏览器定位（Geolocation，非地图 SDK）→ GET /facilities/nearby
 */
const activeTab = ref('route')

/** —— 路线推荐 —— */
const tagStore = useTagStore()
const selectedTags = ref<string[]>([])
const crowd = ref<Crowd | ''>('')
const minutes = ref(180)
const submitting = ref(false)
const routeResult = ref<RouteResult | null>(null)
const routeError = ref('')

const crowdOptions: Array<{ label: string; value: Crowd | '' }> = [
  { label: '不限', value: '' },
  { label: '亲子', value: 'family' },
  { label: '情侣', value: 'couple' },
  { label: '学生党', value: 'student' },
]

const minutePresets = [120, 180, 240, 360]

function toggleTag(name: string) {
  selectedTags.value = selectedTags.value.includes(name)
    ? selectedTags.value.filter((t) => t !== name)
    : [...selectedTags.value, name]
}

async function onRecommend() {
  submitting.value = true
  routeError.value = ''
  try {
    routeResult.value = await recommendRoute({
      tags: selectedTags.value.length > 0 ? selectedTags.value : undefined,
      crowd: crowd.value || undefined,
      minutes: minutes.value,
    })
  } catch (e) {
    routeError.value = (e as Error).message
  } finally {
    submitting.value = false
  }
}

/** 分钟 → “约 2.5 小时” / “约 90 分钟” */
function durationText(total: number): string {
  if (total <= 0) {
    return ''
  }
  const hours = total / 60
  return Number.isInteger(hours) ? `约 ${hours} 小时` : `约 ${Math.round(total / 15) * 15} 分钟`
}

const routeSpots = computed(() => routeResult.value?.spots ?? [])

/** 从 reason 里摘出“上一站步行约 X 分钟”（首站没有） */
function walkFromPrev(spot: RouteSpot): number | null {
  const m = spot.reason.match(/上一站步行约 (\d+) 分钟/)
  return m ? Number(m[1]) : null
}

/** 去掉步行片段后的原因文本 */
function reasonText(spot: RouteSpot): string {
  return spot.reason.replace(/；?上一站步行约 \d+ 分钟/, '')
}

/** —— 附近设施 —— */
// 位置来源两种：手动选景点（默认，用景点自身坐标，演示零门槛）或浏览器定位。
// 注意：coords 初始值是 Infinity（vueuse 默认），"是否已定位"必须看 locatedAt。
const { coords, locatedAt, error: geoError, resume } = useGeolocation({ immediate: false })
const facilityType = ref<FacilityType | undefined>(undefined)
const facilities = ref<Facility[]>([])
const facLoading = ref(false)
const facError = ref('')
const spots = ref<Spot[]>([])
const selectedSpot = ref<Spot | null>(null)
const pickerVisible = ref(false)
const locSource = ref<'spot' | 'gps'>('spot')

const facTypeOptions: Array<{ label: string; value?: FacilityType }> = [
  { label: '全部', value: undefined },
  { label: facilityTypeText[1], value: 1 },
  { label: facilityTypeText[2], value: 2 },
  { label: facilityTypeText[3], value: 3 },
  { label: facilityTypeText[4], value: 4 },
  { label: facilityTypeText[5], value: 5 },
  { label: facilityTypeText[6], value: 6 },
]

/** 把浏览器定位失败翻译成可操作的原因（1 拒绝 / 2 不可用 / 3 超时） */
const geoErrorText = computed(() => {
  const code = (geoError.value as { code?: number } | null)?.code
  if (code === 1) {
    return '定位权限被拒绝：点击地址栏左侧图标，把"位置信息"改为允许后重试'
  }
  if (code === 2) {
    return '获取位置失败：请确认系统定位服务已开启'
  }
  if (code === 3) {
    return '定位超时，请再点一次"使用当前位置"'
  }
  return '定位失败：浏览器定位仅在 localhost 或 HTTPS 页面可用（手机访问请改用电脑演示）'
})

function hasFix(): boolean {
  return locatedAt.value !== null && Number.isFinite(coords.value.longitude) && Number.isFinite(coords.value.latitude)
}

/** 当前生效坐标：景点模式用景点坐标，定位模式用 GPS 坐标 */
function effectiveCoords(): { longitude: number; latitude: number } | null {
  if (locSource.value === 'spot') {
    return selectedSpot.value
      ? { longitude: selectedSpot.value.longitude, latitude: selectedSpot.value.latitude }
      : null
  }
  return hasFix()
    ? { longitude: coords.value.longitude, latitude: coords.value.latitude }
    : null
}

const locLabel = computed(() => {
  if (locSource.value === 'gps' && hasFix()) {
    return '按浏览器定位'
  }
  return selectedSpot.value ? `按景点：${selectedSpot.value.name}` : '选择所在景点'
})

const spotColumns = computed(() => spots.value.map((s) => ({ text: s.name, value: s.id })))

function onSpotConfirm({ selectedOptions }: { selectedOptions: Array<{ value: number }> }) {
  pickerVisible.value = false
  const id = selectedOptions[0]?.value
  const spot = spots.value.find((s) => s.id === id)
  if (!spot) {
    return
  }
  selectedSpot.value = spot
  locSource.value = 'spot'
  loadFacilities()
}

async function loadSpots() {
  try {
    const page = await getSpots({ current: 1, size: 100 })
    spots.value = page.records
    // 默认选中第一个景点并立即查询，页面进来就有数据
    if (spots.value.length > 0 && !selectedSpot.value) {
      selectedSpot.value = spots.value[0] ?? null
      loadFacilities()
    }
  } catch {
    // 景点列表加载失败不致命，用户仍可用浏览器定位
  }
}

async function loadFacilities() {
  const c = effectiveCoords()
  if (!c) {
    showToast('请先选择所在景点，或使用浏览器定位')
    return
  }
  facLoading.value = true
  facError.value = ''
  try {
    facilities.value = await getNearby({
      longitude: c.longitude,
      latitude: c.latitude,
      radius: 800,
      type: facilityType.value,
    })
  } catch (e) {
    facError.value = (e as Error).message
  } finally {
    facLoading.value = false
  }
}

function pickFacType(value?: FacilityType) {
  if (facilityType.value === value) {
    return
  }
  facilityType.value = value
  if (effectiveCoords()) {
    loadFacilities()
  }
}

// 浏览器定位成功后切换到定位模式并自动查询
watch(locatedAt, () => {
  locSource.value = 'gps'
  loadFacilities()
})

function onLocate() {
  resume()
}

function distanceText(meters: number): string {
  return meters >= 1000 ? `${(meters / 1000).toFixed(1)} km` : `${Math.round(meters)} m`
}

onMounted(() => {
  tagStore.ensure().catch(() => {})
  loadSpots()
})
</script>

<template>
  <div class="guide">
    <van-tabs v-model:active="activeTab" sticky>
      <!-- 路线推荐 -->
      <van-tab title="路线推荐" name="route">
        <div class="card">
          <div class="section-title">想看什么（可多选，命中任意一个即可）</div>
          <div v-if="tagStore.tags.length" class="tag-row">
            <button
              v-for="tag in tagStore.tags"
              :key="tag.id"
              class="chip"
              :class="{ 'chip--on': selectedTags.includes(tag.name) }"
              @click="toggleTag(tag.name)"
            >
              {{ tag.name }}
            </button>
          </div>
          <div v-else class="guide-hint">标签加载失败，可直接按时间预算推荐</div>

          <div class="section-title">同行人群</div>
          <div class="tag-row">
            <button
              v-for="opt in crowdOptions"
              :key="opt.label"
              class="chip"
              :class="{ 'chip--on': crowd === opt.value }"
              @click="crowd = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>

          <div class="section-title">时间预算</div>
          <div class="tag-row">
            <button
              v-for="preset in minutePresets"
              :key="preset"
              class="chip"
              :class="{ 'chip--on': minutes === preset }"
              @click="minutes = preset"
            >
              {{ durationText(preset) }}
            </button>
          </div>
          <div class="minutes-row">
            <span class="minutes-label">自定义（分钟）</span>
            <van-stepper v-model="minutes" :min="30" :max="480" :step="30" />
          </div>

          <van-button
            type="primary"
            round
            block
            :loading="submitting"
            class="guide-submit"
            @click="onRecommend"
          >
            生成推荐路线
          </van-button>
        </div>

        <van-empty v-if="routeError" image="network" :description="routeError" />

        <div v-if="routeResult" class="card">
          <div class="route-head">
            <span class="route-total">{{ durationText(routeResult.totalMinutes) }}</span>
            <span class="route-count">{{ routeSpots.length }} 个点位</span>
          </div>
          <template v-if="routeSpots.length">
            <div
                v-for="(spot, index) in routeSpots"
                :key="spot.id"
                class="route-stop"
                :class="{ 'is-first': index === 0 }"
              >
              <div
                v-if="index > 0 && walkFromPrev(spot)"
                class="route-walk-tag"
              >
                <van-icon name="clock-o" />
                步行 {{ walkFromPrev(spot) }} 分钟
              </div>
              <div class="route-index">{{ index + 1 }}</div>
              <div class="route-stop-body">
                <router-link :to="`/spots/${spot.id}`" class="route-name">
                  {{ spot.name }}
                </router-link>
                <div class="route-reason">{{ reasonText(spot) }}</div>
                <div class="route-walk">游览约 {{ spot.walkMinutes }} 分钟</div>
              </div>
            </div>
          </template>
          <van-empty v-else description="这个预算排不下任何点位，试着加长时间或减少标签" />
        </div>
      </van-tab>

      <!-- 附近设施 -->
      <van-tab title="附近设施" name="nearby">
        <div class="card">
          <div class="section-title">当前位置</div>
          <div class="loc-row" @click="pickerVisible = true">
            <van-icon name="location-o" class="loc-icon" />
            <span class="loc-text">{{ locLabel }}</span>
            <van-icon name="arrow" class="loc-arrow" />
          </div>
          <div class="loc-alt">
            或
            <van-button size="small" round plain type="primary" @click="onLocate">
              {{ locatedAt ? '刷新浏览器定位' : '使用浏览器定位' }}
            </van-button>
          </div>
          <div v-if="geoError && locSource === 'gps'" class="guide-hint guide-hint--warn">
            {{ geoErrorText }}
          </div>

          <div class="section-title">设施类型</div>
          <div class="tag-row">
            <button
              v-for="opt in facTypeOptions"
              :key="opt.label"
              class="chip"
              :class="{ 'chip--on': facilityType === opt.value }"
              @click="pickFacType(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <van-popup v-model:show="pickerVisible" position="bottom" round>
          <van-picker
            title="我当前所在景点"
            :columns="spotColumns"
            @confirm="onSpotConfirm"
            @cancel="pickerVisible = false"
          />
        </van-popup>

        <div v-if="facLoading" class="guide-hint">查询中…</div>
        <van-empty v-else-if="facError" image="network" :description="facError">
          <van-button round type="primary" size="small" @click="loadFacilities">重试</van-button>
        </van-empty>
        <template v-else>
          <div class="card" v-if="facilities.length">
            <div v-for="fac in facilities" :key="fac.id" class="fac-row">
              <span class="fac-type">{{ facilityTypeText[fac.type] }}</span>
              <span class="fac-name">{{ fac.name }}</span>
              <span class="fac-open">{{ fac.openTime }}</span>
              <span class="fac-distance">{{ distanceText(fac.distance) }}</span>
            </div>
          </div>
          <van-empty
            v-else
            :description="effectiveCoords() ? '附近 800 米内没有该类设施' : '先选择所在景点，或使用浏览器定位'"
          />
        </template>
      </van-tab>
    </van-tabs>
  </div>
</template>

<style scoped lang="scss">
.card {
  background: var(--sg-surface);
  border-radius: var(--sg-radius);
  margin: 12px;
  padding: 14px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--sg-text-secondary);
  margin: 12px 0 8px;

  &:first-child {
    margin-top: 0;
  }
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  border: none;
  background: var(--sg-fill);
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;

  &--on {
    background: var(--sg-primary-weak);
    color: var(--sg-primary);
    font-weight: 600;
  }
}

.minutes-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.minutes-label {
  font-size: 13px;
  color: var(--sg-text-secondary);
}

.guide-submit {
  margin-top: 16px;
}

.guide-hint {
  font-size: 12px;
  color: var(--sg-text-secondary);
  text-align: center;
  padding: 8px 0;

  &--warn {
    color: #ee0a24;
  }
}

.route-head {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.route-total {
  font-family: var(--sg-font-display);
  font-size: 20px;
  font-weight: 700;
  color: var(--sg-primary);
}

.route-count {
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.route-stop {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 8px 0 26px;
  margin-top: 18px; // 与上一站之间的连线区

  &.is-first {
    margin-top: 0;
  }

  // 连线：从本站圆点下方一直贯穿到下一站圆点上方
  &::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 36px;
    bottom: -34px;
    border-left: 1px dashed var(--sg-primary-border);
  }

  &:last-child {
    padding-bottom: 8px;

    &::before {
      display: none;
    }
  }
}

.route-walk-tag {
  position: absolute;
  left: 24px;
  top: -29px; // 垂直居中在两站之间的连线中点
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 9px;
  border: 1px solid var(--sg-primary-border);
  border-radius: 999px;
  background: var(--sg-surface);
  color: var(--sg-primary);
  font-size: 11px;
  z-index: 1;
}

.route-index {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--sg-primary);
  color: #fff;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.route-stop-body {
  flex: 1;
  min-width: 0;
}

.route-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--sg-text);
  text-decoration: none;
}

.route-reason {
  margin-top: 4px;
  font-size: 12px;
  color: var(--sg-text-secondary);
  line-height: 1.5;
}

.route-walk {
  margin-top: 4px;
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.fac-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--sg-fill);
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }
}

.fac-type {
  flex-shrink: 0;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--sg-primary-weak);
  color: var(--sg-primary);
}

.fac-name {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fac-open {
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.loc-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--sg-primary-tint);
  cursor: pointer;
}

.loc-icon {
  color: var(--sg-primary);
  font-size: 16px;
}

.loc-text {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: var(--sg-primary);
}

.loc-arrow {
  color: var(--sg-text-secondary);
}

.loc-alt {
  margin-top: 8px;
  font-size: 12px;
  color: var(--sg-text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.fac-distance {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--sg-primary);
}
</style>
