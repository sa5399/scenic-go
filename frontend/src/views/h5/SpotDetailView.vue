<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Exhibition, ReviewItem, Spot } from '@/types/api'
import { getExhibitions, getSpot } from '@/api/spot'
import { createReview, getReviews, toggleLike } from '@/api/review'
import { fen2yuan, formatTime } from '@/utils/format'
import { showToast } from '@/utils/h5ui'
import { useAuthStore } from '@/stores/auth'
import CoverBlock from '@/components/CoverBlock.vue'

const route = useRoute()
const router = useRouter()

const spotId = computed(() => Number(route.params.id))
const spot = ref<Spot | null>(null)
const loadError = ref('')

const activeTab = ref('intro')
const navSolid = ref(false)

/** 可预约：园区票或收费项目；免费景点凭入园票自由游览，不单独预约 */
const bookable = computed(() => spot.value !== null && (spot.value.type === 2 || spot.value.avgPrice > 0))

/** 滚过约半张封面后，透明导航切换为白底（短页面滚动余量小，阈值不宜太高） */
function onPageScroll() {
  navSolid.value = window.scrollY > 120
}

onMounted(() => {
  window.addEventListener('scroll', onPageScroll, { passive: true })
  // 园区票走专属精简页
  if (spotId.value === 13) {
    router.replace('/park')
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onPageScroll)
})
const exhibitions = ref<Exhibition[]>([])

/** 评价分页：van-list 驱动，tab 懒渲染首次激活才加载 */
const reviews = ref<ReviewItem[]>([])
const reviewTotal = ref(0)
const reviewPage = ref(0)
const reviewLoading = ref(false)
const reviewFinished = ref(false)
const reviewError = ref(false)

const auth = useAuthStore()
const reviewVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = reactive({ score: 5, content: '' })
const scoreTexts = ['很差', '不满意', '一般', '满意', '非常满意']
const scoreText = computed(() => scoreTexts[reviewForm.score - 1] ?? '')

/** 评价列表整页重置重拉 */
function resetReviews() {
  reviewPage.value = 0
  reviews.value = []
  reviewTotal.value = 0
  reviewFinished.value = false
  reviewError.value = false
  loadReviews()
}

async function loadSpot() {
  loadError.value = ''
  try {
    spot.value = await getSpot(spotId.value)
    getExhibitions(spotId.value)
      .then((list) => {
        exhibitions.value = list
      })
      .catch(() => {
        // 展品加载失败只影响展品 tab，显示空态
      })
  } catch (e) {
    loadError.value = (e as Error).message
  }
}

async function loadReviews() {
  reviewLoading.value = true
  reviewError.value = false
  try {
    const page = await getReviews(spotId.value, { current: reviewPage.value + 1, size: 10 })
    reviews.value.push(...page.records)
    reviewTotal.value = page.total
    reviewPage.value = page.current
    reviewFinished.value = reviews.value.length >= page.total
  } catch {
    reviewError.value = true
  } finally {
    reviewLoading.value = false
  }
}

function openReview() {
  if (!auth.isLoggedIn) {
    showToast('请先登录后再评价')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  reviewForm.score = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    showToast('写点内容再发表吧')
    return
  }
  reviewSubmitting.value = true
  try {
    await createReview({
      spotId: spotId.value,
      score: reviewForm.score,
      content: reviewForm.content.trim(),
    })
    showToast('评价已发表')
    reviewVisible.value = false
    resetReviews()
  } catch (e) {
    showToast((e as Error).message)
  } finally {
    reviewSubmitting.value = false
  }
}

async function onLike(review: ReviewItem) {
  try {
    const result = await toggleLike(review.id)
    review.liked = result.liked
  } catch {
    // 未登录由全局 401 处理跳登录；其余失败静默（点赞非关键路径）
  }
}

/** 介绍内容按空行分段展示 */
const introParagraphs = computed(() =>
  (spot.value?.content || spot.value?.summary || '').split(/\n+/).filter(Boolean),
)

onMounted(loadSpot)
</script>

<template>
  <div class="detail" :class="{ 'nav-solid': navSolid, bookable }">
    <van-nav-bar
      left-arrow
      fixed
      :class="{ 'nav-solid': navSolid }"
      @click-left="router.back()"
    >
      <template #title>
        <span class="nav-title">{{ spot?.name ?? '景点详情' }}</span>
      </template>
    </van-nav-bar>

    <van-empty v-if="loadError" image="network" :description="loadError">
      <van-button round type="primary" size="small" @click="loadSpot">重试</van-button>
    </van-empty>

    <div v-else-if="!spot" class="detail-skeleton">
      <div class="sk-cover" />
      <div class="card">
        <div class="sk-line w40" />
        <div class="sk-line w90" />
        <div class="sk-line w70" />
      </div>
      <div class="card">
        <div class="sk-line w90" />
        <div class="sk-line w90" />
        <div class="sk-line w60" />
      </div>
    </div>

    <template v-else>
      <CoverBlock class="detail-cover" :src="spot.coverImg" :label="spot.name" width="100%" height="220px" />

      <div class="card detail-head">
        <div class="detail-name">{{ spot.name }}</div>
        <div class="detail-meta">
          <span class="detail-score">★ {{ spot.score }}</span>
          <span>已售 {{ spot.soldCount }}</span>
          <span>开放 {{ spot.openTime }}</span>
        </div>
        <div class="detail-price">
          {{ spot.avgPrice > 0 ? `¥${fen2yuan(spot.avgPrice)}/人` : '免费参观' }}
        </div>
        <div v-if="spot.tags?.length" class="detail-tags">
          <span v-for="tag in spot.tags" :key="tag.id" class="detail-tag">{{ tag.name }}</span>
        </div>
      </div>

      <div v-if="!bookable" class="free-roam">
        <van-icon name="checked" />
        凭「云栖湖景区入园票」免费游览，无需单独预约
      </div>
      <van-tabs v-model:active="activeTab" sticky lazy-render class="detail-tabs">
        <van-tab title="介绍" name="intro">
          <div class="card detail-intro">
            <p v-for="(para, index) in introParagraphs" :key="index">{{ para }}</p>
            <p class="detail-walk">建议游览 {{ spot.walkMinutes }} 分钟</p>
          </div>
        </van-tab>

        <!-- 展品只属于展馆（type=0），普通景点不显示该 tab -->
        <van-tab
          v-if="spot.type === 0"
          :title="exhibitions.length ? `展品 (${exhibitions.length})` : '展品'"
          name="exhibitions"
        >
          <div v-if="exhibitions.length" class="exh-list">
            <div v-for="exh in exhibitions" :key="exh.id" class="card exh-card">
              <CoverBlock :src="exh.coverImg" :label="exh.name" width="88" height="72" radius="6" />
              <div class="exh-body">
                <div class="exh-name">
                  {{ exh.name }}<span class="exh-cat">{{ exh.category }}</span>
                </div>
                <div class="exh-summary">{{ exh.summary }}</div>
              </div>
            </div>
          </div>
          <van-empty v-else description="暂无展品信息" />
        </van-tab>

        <van-tab :title="reviewTotal ? `评价 (${reviewTotal})` : '评价'" name="reviews">
          <van-list
            v-model:loading="reviewLoading"
            :finished="reviewFinished"
            :error="reviewError"
            error-text="加载失败，点击重试"
            finished-text="没有更多了"
            @load="loadReviews"
          >
            <div v-for="review in reviews" :key="review.id" class="card review-card">
              <div class="review-head">
                <CoverBlock :src="review.icon" width="32" height="32" round />
                <div class="review-user">
                  <div class="review-nick">{{ review.nickName }}</div>
                  <div class="review-info">
                    <span class="review-stars">{{ '★'.repeat(review.score) }}</span>
                    <span>{{ formatTime(review.createTime) }}</span>
                  </div>
                </div>
                <button class="review-like" @click="onLike(review)">👍 {{ review.liked }}</button>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
            <van-empty v-if="reviewFinished && reviews.length === 0" description="还没有评价，预约后来写第一条吧" />
          </van-list>
        </van-tab>

        <van-popup v-model:show="reviewVisible" position="bottom" round>
          <div class="review-form">
            <div class="rf-head">
              <span class="rf-title">写评价</span>
              <van-icon name="cross" class="rf-close" @click="reviewVisible = false" />
            </div>
            <div class="rf-rate">
              <van-rate
                v-model="reviewForm.score"
                :size="30"
                color="#e0863c"
                void-color="#dcdedc"
              />
              <span class="rf-score-text">{{ scoreText }}</span>
            </div>
            <div class="rf-input">
              <van-field
                v-model="reviewForm.content"
                type="textarea"
                rows="4"
                maxlength="500"
                placeholder="分享你的游览体验…"
                class="rf-field"
              />
              <span class="rf-count">{{ reviewForm.content.length }}/500</span>
            </div>
            <van-button
              type="primary"
              round
              block
              :loading="reviewSubmitting"
              :disabled="!reviewForm.content.trim()"
              class="rf-submit"
              @click="submitReview"
            >
              发表评价
            </van-button>
          </div>
        </van-popup>

      </van-tabs>

      <div v-if="activeTab === 'reviews'" class="detail-bar">
        <van-button type="primary" round block icon="edit" @click="openReview">写评价</van-button>
      </div>
      <div v-else-if="bookable" class="detail-bar">
        <div v-if="spot.avgPrice > 0" class="bar-price">
          <span class="bar-price-num">¥{{ fen2yuan(spot.avgPrice) }}</span>
          <span class="bar-price-suffix">起/人</span>
        </div>
        <span v-else class="bar-free">免费预约</span>
        <van-button type="primary" round class="bar-btn" :to="`/spots/${spotId}/book`">立即预约</van-button>
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

.detail-loading {
  text-align: center;
  padding: 80px 0;
  color: var(--sg-text-secondary);
}

.detail-head {
  margin-top: -20px; // 压住封面下缘
  position: relative;
}

.detail-name {
  font-family: var(--sg-font-display);
  font-size: 20px;
  font-weight: 700;
}

.detail-meta {
  margin-top: 6px;
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.detail-score {
  color: var(--sg-accent);
  font-weight: 600;
}

.detail-price {
  margin-top: 8px;
  color: var(--sg-primary);
  font-size: 16px;
  font-weight: 600;
}

.detail-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.detail-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--sg-primary-weak);
  color: var(--sg-primary);
}

.detail-tabs {
  padding-bottom: 76px; // 给底部预约栏让位
}

.detail:not(.bookable) .detail-tabs {
  padding-bottom: 24px;
}

.free-roam {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 12px 4px;
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--sg-primary-weak);
  color: var(--sg-primary);
  font-size: 13px;
}

.detail :deep(.van-nav-bar) {
  background: transparent;
  transition: background 0.2s;
}

.detail :deep(.van-nav-bar__title) {
  color: transparent;
  transition: color 0.2s;
}

.detail :deep(.van-nav-bar .van-icon) {
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
}

.detail.nav-solid :deep(.van-nav-bar) {
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow);
}

.detail.nav-solid :deep(.van-nav-bar__title) {
  color: var(--sg-text);
}

.detail.nav-solid :deep(.van-nav-bar .van-icon) {
  color: var(--sg-text);
  text-shadow: none;
}

.detail-skeleton {
  .sk-cover {
    height: 220px;
  }

  .sk-line {
    height: 14px;
    border-radius: 7px;
    margin-bottom: 12px;
  }

  .w40 { width: 40%; }
  .w60 { width: 60%; }
  .w70 { width: 70%; }
  .w90 { width: 90%; }

  .sk-cover,
  .sk-line {
    background: linear-gradient(90deg, #eef1ef 25%, #f7f8f8 45%, #eef1ef 65%);
    background-size: 200% 100%;
    animation: sg-shimmer 1.2s infinite;
  }
}

@keyframes sg-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.review-form {
  padding: 20px 16px calc(20px + env(safe-area-inset-bottom));
}

.rf-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.rf-title {
  font-family: var(--sg-font-display);
  font-size: 17px;
  font-weight: 700;
}

.rf-close {
  color: var(--sg-text-secondary);
  font-size: 18px;
  cursor: pointer;
}

.rf-rate {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 0 20px;
  background: var(--sg-fill);
  border-radius: 12px;
  margin-bottom: 14px;
}

.rf-score-text {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--sg-accent);
}

.rf-input {
  position: relative;
  background: var(--sg-fill);
  border-radius: 12px;
  overflow: hidden;
}

.rf-field {
  background: transparent;
}

.rf-count {
  position: absolute;
  right: 10px;
  bottom: 8px;
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.rf-submit {
  margin-top: 18px;
}



.detail-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bar-price-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--sg-primary);
}

.bar-price-suffix {
  font-size: 12px;
  color: var(--sg-text-secondary);
}

.bar-free {
  font-size: 16px;
  font-weight: 700;
  color: var(--sg-primary);
}

.bar-btn {
  padding: 0 30px;
}

.detail-intro {
  p {
    font-size: 14px;
    line-height: 1.7;
    margin: 0 0 10px;
    white-space: pre-wrap;
  }
}

.detail-walk {
  color: var(--sg-text-secondary);
  font-size: 12px;
}

.exh-card {
  display: flex;
  gap: 12px;
}

.exh-body {
  flex: 1;
  min-width: 0;
}

.exh-name {
  font-size: 14px;
  font-weight: 600;
}

.exh-cat {
  margin-left: 6px;
  font-size: 11px;
  font-weight: 400;
  color: var(--sg-text-secondary);
}

.exh-summary {
  margin-top: 4px;
  font-size: 12px;
  color: var(--sg-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-user {
  flex: 1;
  min-width: 0;
}

.review-nick {
  font-size: 13px;
  font-weight: 600;
}

.review-info {
  margin-top: 2px;
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--sg-text-secondary);
}

.review-stars {
  color: var(--sg-accent);
}

.review-like {
  border: none;
  background: var(--sg-fill);
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
  cursor: pointer;
}

.review-content {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.6;
}

.detail-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: var(--sg-surface);
  box-shadow: var(--sg-shadow-bar);
}
</style>
