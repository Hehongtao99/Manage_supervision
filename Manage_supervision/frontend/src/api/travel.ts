import axios from '../utils/axios'
import type { ScenicSpotData } from '../types/scenicSpot'
import type { HotelData } from '../types/hotel'

/**
 * 根据地区获取景区列表
 * @param provinceId 省份ID
 * @param cityId 城市ID
 * @param districtId 区县ID
 * @param page 页码
 * @param size 每页数量
 */
export const getScenicSpotsByRegion = async (
  provinceId?: number, 
  cityId?: number, 
  districtId?: number,
  page: number = 1,
  size: number = 100
) => {
  const response = await axios.get('/api/travel/scenic-spots', {
    params: {
      provinceId,
      cityId,
      districtId,
      page,
      size
    }
  })
  return response.data
}

/**
 * 获取热门景区
 * @param limit 限制数量
 */
export const getHotScenicSpots = async (limit: number = 5) => {
  const response = await axios.get('/api/travel/scenic-spots/hot', { 
    params: { limit } 
  })
  return response.data
}

/**
 * 获取景区周边酒店
 * @param scenicSpotId 景区ID
 * @param limit 限制数量
 */
export const getHotelsByScenicSpot = async (scenicSpotId: number, limit: number = 5) => {
  const response = await axios.get(`/api/travel/hotels/scenic-spot/${scenicSpotId}`, {
    params: { limit }
  })
  return response.data
}

/**
 * 获取指定级别的地区数据
 * @param level 地区级别：1-省份，2-城市，3-区县
 */
export const getRegionsByLevel = async (level: number) => {
  const response = await axios.get(`/api/travel/regions/level/${level}`)
  return response.data
}

/**
 * 获取指定父级ID的子地区
 * @param parentId 父级ID
 */
export const getRegionsByParentId = async (parentId: number) => {
  const response = await axios.get(`/api/travel/regions/parent/${parentId}`)
  return response.data
} 