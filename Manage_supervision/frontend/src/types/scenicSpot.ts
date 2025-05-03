export interface ScenicSpotData {
  id?: number;
  name: string;
  description?: string;
  provinceId: number | null;
  provinceName?: string;
  cityId?: number | null;
  cityName?: string;
  districtId?: number | null;
  districtName?: string;
  address?: string;
  level?: string;
  businessHours?: string;
  ticketPrice?: number;
  contactPhone?: string;
  imageUrl?: string;
  status?: string;
  sort?: number;
  createTime?: string;
  updateTime?: string;
  locationPath?: string;
}

export interface ScenicSpotQueryParams {
  page?: number;
  size?: number;
  name?: string;
  provinceId?: number;
  cityId?: number;
  districtId?: number;
  level?: string;
  status?: string;
} 