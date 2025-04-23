import request from '@/utils/request';

export interface FeeStandard {
  id?: number;
  feeName: string;
  amount: number;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export const feeStandardApi = {
  getAllFeeStandards() {
    return request({
      url: '/api/fee-standards',
      method: 'get'
    });
  },
  
  getFeeStandardById(id: number) {
    return request({
      url: `/api/fee-standards/${id}`,
      method: 'get'
    });
  },
  
  createFeeStandard(feeStandard: FeeStandard) {
    return request({
      url: '/api/fee-standards',
      method: 'post',
      data: feeStandard
    });
  },
  
  updateFeeStandard(id: number, feeStandard: FeeStandard) {
    return request({
      url: `/api/fee-standards/${id}`,
      method: 'put',
      data: feeStandard
    });
  },
  
  deleteFeeStandard(id: number) {
    return request({
      url: `/api/fee-standards/${id}`,
      method: 'delete'
    });
  }
}; 