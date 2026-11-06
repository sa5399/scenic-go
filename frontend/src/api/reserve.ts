import { get, post } from '@/utils/request'
import type {
  PageResult,
  ReserveCreateResult,
  ReserveForm,
  ReserveItem,
  ReservePayResult,
  ReserveQuery,
} from '@/types/api'

/** POST /reserves · 需登录 · 待支付也占库存，15 分钟不付会被超时关单 */
export function createReserve(form: ReserveForm): Promise<ReserveCreateResult> {
  return post<ReserveCreateResult>('/reserves', form)
}

/** POST /reserves/{orderNo}/pay · 需登录 · 状态→1 并生成核销码 */
export function payReserve(orderNo: string): Promise<ReservePayResult> {
  return post<ReservePayResult>(`/reserves/${orderNo}/pay`)
}

/** POST /reserves/{orderNo}/cancel · 需登录 · 待支付/已预约可取消，库存回补 */
export function cancelReserve(orderNo: string): Promise<void> {
  return post<void>(`/reserves/${orderNo}/cancel`)
}

/** GET /reserves · 需登录 · 只含当前用户，按下单时间倒序 */
export function getReserves(query: ReserveQuery = {}): Promise<PageResult<ReserveItem>> {
  return get<PageResult<ReserveItem>>('/reserves', query)
}

/** GET /reserves/{orderNo} · 需登录 · 含核销码与各时间节点 */
export function getReserve(orderNo: string): Promise<ReserveItem> {
  return get<ReserveItem>(`/reserves/${orderNo}`)
}
