import axios from '../utils/axios'
import type { HotelData, HotelQueryParams } from '../types/hotel'

/**
 * 获取酒店列表
 * @param params 查询参数
 */
export const getHotelList = async (params: HotelQueryParams = {}) => {
  const response = await axios.get('/api/admin/hotels', { params })
  return response.data
}

/**
 * 获取酒店详情
 * @param id 酒店ID
 */
export const getHotelDetail = async (id: number) => {
  const response = await axios.get(`/api/admin/hotels/${id}`)
  return response.data
}

/**
 * 添加酒店
 * @param data 酒店数据
 */
export const addHotel = async (data: HotelData) => {
  const response = await axios.post('/api/admin/hotels', data)
  return response.data
}

/**
 * 更新酒店
 * @param id 酒店ID
 * @param data 酒店数据
 */
export const updateHotel = async (id: number, data: HotelData) => {
  const response = await axios.put(`/api/admin/hotels/${id}`, data)
  return response.data
}

/**
 * 删除酒店
 * @param id 酒店ID
 */
export const deleteHotel = async (id: number) => {
  const response = await axios.delete(`/api/admin/hotels/${id}`)
  return response.data
}

/**
 * 切换酒店状态
 * @param id 酒店ID
 */
export const toggleHotelStatus = async (id: number) => {
  const response = await axios.post(`/api/admin/hotels/${id}/toggle-status`)
  return response.data
}

/**
 * 获取景区周边酒店
 * @param scenicSpotId 景区ID
 * @param limit 限制数量
 */
export const getHotelsByScenicSpot = async (scenicSpotId: number, limit: number = 5) => {
  const response = await axios.get(`/api/admin/hotels/scenic-spot/${scenicSpotId}`, {
    params: { limit }
  })
  return response.data
}

/**
 * 根据地区获取关联景区
 * @param provinceId 省份ID
 * @param cityId 城市ID
 * @param districtId 区县ID
 */
export const getScenicSpotsByRegion = async (provinceId?: number, cityId?: number, districtId?: number) => {
  const response = await axios.get('/api/admin/scenic-spots', {
    params: {
      provinceId,
      cityId,
      districtId,
      status: 'active',
      page: 1,
      size: 100 // 获取足够多的景区供选择
    }
  })
  return response.data
} 