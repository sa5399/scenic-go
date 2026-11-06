import { get, post } from '@/utils/request'
import type { PageResult, ReviewForm, ReviewItem, ReviewLikeResult } from '@/types/api'

/** GET /spots/{id}/reviews · 免登录 · 按评价时间倒序 */
export function getReviews(
  spotId: number | string,
  page: { current?: number; size?: number } = {},
): Promise<PageResult<ReviewItem>> {
  return get<PageResult<ReviewItem>>(`/spots/${spotId}/reviews`, page)
}

/** POST /reviews · 需登录 · score 超范围 400；同一人可发多条（后端不拦） */
export function createReview(form: ReviewForm): Promise<number> {
  return post<number>('/reviews', form)
}

/** GET /reviews · 需登录 · 我的评价（分页，按时间倒序，多返回 spotId/spotName） */
export function getMyReviews(page: { current?: number; size?: number } = {}): Promise<PageResult<ReviewItem>> {
  return get<PageResult<ReviewItem>>('/reviews', page)

}

/** POST /reviews/{id}/like · 需登录 · 同一接口切换点赞/取消 */
export function toggleLike(reviewId: number): Promise<ReviewLikeResult> {
  return post<ReviewLikeResult>(`/reviews/${reviewId}/like`)
}
