import { get, post } from '@/utils/request'
import type {
  CheckinForm,
  CheckinLog,
  CheckinLogQuery,
  CheckinResult,
  PageResult,
} from '@/types/api'

/** POST /checkin · 需登录且 role ∈ {2,3} · 后端不校验"是否预约当天" */
export function checkin(form: CheckinForm): Promise<CheckinResult> {
  return post<CheckinResult>('/checkin', form)
}

/** GET /checkin/logs · 需登录且 role = 3 · startDate/endDate 含当天，按核销时间倒序 */
export function getCheckinLogs(query: CheckinLogQuery = {}): Promise<PageResult<CheckinLog>> {
  return get<PageResult<CheckinLog>>('/checkin/logs', query)
}
