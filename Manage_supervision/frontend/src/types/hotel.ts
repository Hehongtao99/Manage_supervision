export interface HotelData {
  id?: number;
  name: string;
  description?: string;
  
  // 地区信息
  provinceId: number | null;
  provinceName?: string;
  cityId?: number | null;
  cityName?: string;
  districtId?: number | null;
  districtName?: string;
  address?: string;
  locationPath?: string;
  
  // 酒店信息
  level?: string;
  imageUrl?: string;
  contactPhone?: string;
  startPrice?: number;
  
  // 景区关联信息
  scenicSpotId?: number | null;
  scenicSpotName?: string;
  distanceToSpot?: number;
  
  // 管理信息
  status?: string;
  sort?: number;
  createTime?: string;
  updateTime?: string;
}

export interface HotelQueryParams {
  page?: number;
  size?: number;
  name?: string;
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  level?: string;
  scenicSpotId?: number;
  status?: string;
} 