import axios, { AxiosError, type AxiosResponse } from 'axios'
import type { ApiResult } from '@/types/api'
import { clearAuth, getToken } from '@/utils/token'

/**
 * 统一请求层，规则见《docs/前端接口文档.md》§0：
 * - 后端 HTTP 恒 200，成败只看 body.code，get/post 里拆包
 * - code 200 → 直接返回 data（调用方拿到的就是业务数据）
 * - code 401 → 清登录态，跳当前端登录页（登出接口的 401 同样处理，视为已登出）
 * - 其余 code → 以 ApiError reject，由页面自行提示 msg
 */

export class ApiError extends Error {
  code: number

  constructor(code: number, msg: string) {
    super(msg)
    this.name = 'ApiError'
    this.code = code
  }
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10_000,
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// HTTP 层失败（后端没起 / 网关错 / 超时）统一转成 ApiError
instance.interceptors.response.use(undefined, (error: AxiosError) => {
  const msg = error.code === 'ECONNABORTED' ? '请求超时' : '网络异常，请稍后再试'
  return Promise.reject(new ApiError(-1, msg))
})

function unwrap<T>(response: AxiosResponse<ApiResult<T>>): T {
  const body = response.data
  if (body.code === 200) {
    return body.data
  }
  if (body.code === 401) {
    void handleUnauthorized()
    throw new ApiError(401, body.msg || '未登录或登录已失效')
  }
  throw new ApiError(body.code, body.msg || '请求失败')
}

async function handleUnauthorized(): Promise<void> {
  clearAuth()
  const { default: router } = await import('@/router')
  const current = router.currentRoute.value
  if (current.name === 'login' || current.name === 'admin-login') {
    return
  }
  const loginPath = current.path.startsWith('/admin') ? '/admin/login' : '/login'
  await router.push({ path: loginPath, query: { redirect: current.fullPath } })
}

/** 业务数据直接返回（已拆包） */
export async function get<T>(url: string, params?: object): Promise<T> {
  const res = await instance.get<ApiResult<T>>(url, { params })
  return unwrap(res)
}

export async function post<T>(url: string, data?: object): Promise<T> {
  const res = await instance.post<ApiResult<T>>(url, data)
  return unwrap(res)
}

export async function put<T>(url: string, data?: object): Promise<T> {
  const res = await instance.put<ApiResult<T>>(url, data)
  return unwrap(res)
}

export async function del<T>(url: string): Promise<T> {
  const res = await instance.delete<ApiResult<T>>(url)
  return unwrap(res)
}
