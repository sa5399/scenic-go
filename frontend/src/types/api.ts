/**
 * 与《docs/前端接口文档.md》§1 一一对应。
 * 后端字段核对自控制器代码（commit 8c8a9f2），命名保持后端原样。
 */

/** —— 通用包装 —— */

export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** —— 用户与认证 —— */

/** 角色：0 游客 / 1 会员 / 2 核销员 / 3 管理员 */
export type Role = 0 | 1 | 2 | 3

/** 登录用户信息（登录返回 / GET /user/me） */
export interface UserInfo {
  id: number
  username: string
  nickName: string
  icon: string
  role: Role
}

export interface LoginResult {
  token: string
  user: UserInfo
}

export interface RegisterForm {
  username: string
  password: string
  nickName?: string
  phone?: string
}

export interface LoginForm {
  username: string
  password: string
}

/** —— 景点 / 展馆 —— */

/** 1 景点 / 0 展馆 / 2 园区票（入园预约，不进景点列表） */
export type SpotType = 0 | 1 | 2

/** 标签类型：1 场景 / 2 人群 / 3 特色 */
export type TagType = 1 | 2 | 3

export interface Tag {
  id: number
  name: string
  type: TagType
  sort: number
}

/** 列表项与详情同构；列表的 content / tags 为 null */
export interface Spot {
  id: number
  name: string
  summary: string
  content: string | null
  coverImg: string
  longitude: number
  latitude: number
  openTime: string
  avgPrice: number // 单位：分
  score: number
  soldCount: number
  recommendOrder: number
  walkMinutes: number
  type: SpotType
  status?: 0 | 1 // 管理端全量列表返回；游客端接口不带
  tags?: Tag[] | null // 仅详情返回
}

export interface SpotQuery {
  current?: number
  size?: number
  keyword?: string
  type?: SpotType
  tagIds?: number[] // 多个 = 全部命中（AND）
}

export interface Exhibition {
  id: number
  spotId: number
  name: string
  category: string
  summary: string
  coverImg: string
  sort: number
}

/** —— 导览（设施 / 路线） —— */

/** 1 洗手间 2 餐饮 3 入口 4 停车 5 医疗 6 商店 */
export type FacilityType = 1 | 2 | 3 | 4 | 5 | 6

export interface Facility {
  id: number
  name: string
  type: FacilityType
  longitude: number
  latitude: number
  spotId: number // 0 = 不挂在景点下
  openTime: string
  distance: number // 米，按距离升序
}

export interface FacilityQuery {
  longitude?: number
  latitude?: number
  radius?: number // 默认 500 米
  type?: FacilityType
}

/** 人群：family 亲子 / couple 情侣 / student 学生党 */
export type Crowd = 'family' | 'couple' | 'student'

export interface RouteForm {
  tags?: string[] // 标签名，命中任一即可（OR）
  crowd?: Crowd
  minutes: number // 时间预算，必须 > 0
  longitude?: number
  latitude?: number
}

export interface RouteSpot {
  id: number
  name: string
  walkMinutes: number
  reason: string
}

export interface RouteResult {
  totalMinutes: number
  spots: RouteSpot[]
}

/** —— 时段与票种 —— */

export interface Slot {
  slotId: number
  slotStart: string
  slotEnd: string
  ticketType: number // 票种编码，展示以 ticketName 为准
  ticketName: string
  price: number // 分
  remaining: number // 余量（以 Redis 为准）
  limitPerSlot: number // 单笔限购
}

/** —— 预约单 —— */

/** 0 待支付 1 已预约 2 已核销 3 已取消 4 已过期 */
export type ReserveStatus = 0 | 1 | 2 | 3 | 4

export interface ReserveForm {
  slotId: number
  quantity?: number // 默认 1，不得超过 limitPerSlot
}

export interface ReserveCreateResult {
  orderNo: string
  totalAmount: number // 分
  status: ReserveStatus
  expireMinutes: number // 待支付超时（15），前端做倒计时
}

export interface ReservePayResult {
  orderNo: string
  status: ReserveStatus
  checkinCode: string
  payTime: string
}

/** 列表与详情同构：列表里 *Code / *Time 为 null，详情有值 */
export interface ReserveItem {
  orderNo: string
  spotName: string
  slotDate: string
  slotStart: string
  slotEnd: string
  quantity: number
  totalAmount: number
  status: ReserveStatus
  createTime: string
  checkinCode: string | null
  payTime: string | null
  checkinTime: string | null
  cancelTime: string | null
}

export interface ReserveQuery {
  status?: ReserveStatus
  current?: number
  size?: number
}

/** —— 核销 —— */

/** 核销渠道：1 闸机 2 人工 3 小程序自助 */
export type CheckinChannel = 1 | 2 | 3

export interface CheckinForm {
  checkinCode: string
  channel?: CheckinChannel // 管理端工作台固定传 2（人工）
}

export interface CheckinResult {
  orderNo: string
  spotName: string
  quantity: number
  checkinTime: string
}

export interface CheckinLogQuery {
  spotId?: number
  startDate?: string // yyyy-MM-dd，含当天
  endDate?: string
  current?: number
  size?: number
}

export interface CheckinLog {
  id: number
  orderNo: string
  userId: number
  spotId: number
  spotName: string
  operatorId: number // 0 = 闸机
  channel: CheckinChannel
  checkinTime: string
}

/** —— 评价 —— */

export interface ReviewItem {
  id: number
  userId: number
  nickName: string
  icon: string
  content: string
  score: number // 1~5
  liked: number
  createTime: string
  spotId?: number // 仅"我的评价"返回
  spotName?: string
}

export interface ReviewForm {
  spotId: number
  reserveId?: number
  content?: string
  score?: number // 默认 5
}

export interface ReviewLikeResult {
  liked: number
  likedByMe: boolean
}

/** —— 公告 —— */

export interface Announcement {
  id: number
  title: string
  content: string
  status: 0 | 1 // 0 下架 1 发布
  createTime: string
}

export interface AnnouncementForm {
  title: string
  content: string
}

/** —— 管理端 —— */

/** 运营订单查询行：比用户侧 ReserveItem 多下单人信息，少核销码等敏感字段 */
export interface AdminReserveItem {
  orderNo: string
  userId: number
  nickName: string
  spotId: number
  spotName: string
  slotDate: string
  slotStart: string
  slotEnd: string
  quantity: number
  totalAmount: number
  status: ReserveStatus
  createTime: string
}

export interface AdminReserveQuery {
  spotId?: number
  status?: ReserveStatus
  startDate?: string // 下单日期范围起点，含当天
  endDate?: string // 下单日期范围终点，含当天
  current?: number
  size?: number
}

/** 管理端编辑景点基础信息（PUT /admin/spots/{id}） */
export interface SpotUpdateForm {
  name: string
  summary: string
  avgPrice: number // 单位：分，0 表示免费
}
