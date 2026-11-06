import type { CheckinChannel, FacilityType, ReserveStatus, Role, TagType } from '@/types/api'

/** 金额：分 → 元（6000 → "60"，1550 → "15.5"） */
export function fen2yuan(fen: number): string {
  return String(fen / 100)
}

export const roleText: Record<Role, string> = {
  0: '游客',
  1: '会员',
  2: '核销员',
  3: '管理员',
}

export const reserveStatusText: Record<ReserveStatus, string> = {
  0: '待支付',
  1: '已预约',
  2: '已核销',
  3: '已取消',
  4: '已过期',
}

export const tagTypeText: Record<TagType, string> = {
  1: '场景',
  2: '人群',
  3: '特色',
}

export const facilityTypeText: Record<FacilityType, string> = {
  1: '洗手间',
  2: '餐饮',
  3: '入口',
  4: '停车',
  5: '医疗',
  6: '商店',
}

export const checkinChannelText: Record<CheckinChannel, string> = {
  1: '闸机',
  2: '人工',
  3: '小程序自助',
}

/** "2026-10-06T11:02:06" → "10-06 11:02"；不合法原文返回 */
export function formatTime(iso: string | null): string {
  if (!iso) {
    return '—'
  }
  return iso.length >= 16 ? `${iso.slice(5, 10)} ${iso.slice(11, 16)}` : iso
}
