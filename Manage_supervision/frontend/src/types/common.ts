/**
 * 基础响应接口
 */
export interface BaseResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
} 