import request from '@/utils/request';

export interface Bill {
  id?: number;
  studentId: number;
  studentName?: string;
  feeStandardId: number;
  feeName?: string;
  amount: number;
  status: boolean;
  paymentTime?: string;
  paymentMethod?: string; // 'WECHAT' | 'ALIPAY'
  paymentMessage?: string;
  dueDate: string;
  createTime?: string;
}

export interface BillGenerateParams {
  feeStandardId: number;
  studentIds?: number[];
  classId?: number;
  dueDate: string;
}

export interface BillPaymentParams {
  paymentMethod: string; // 'WECHAT' | 'ALIPAY'
  paymentMessage?: string;
}

export interface PaginationData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const billApi = {
  getAllBills(page: number = 0, size: number = 10) {
    return request({
      url: `/api/bills?page=${page}&size=${size}`,
      method: 'get'
    });
  },
  
  getBillById(id: number) {
    return request({
      url: `/api/bills/${id}`,
      method: 'get'
    });
  },
  
  getBillsByStudentId(studentId: number) {
    return request({
      url: `/api/bills/student/${studentId}`,
      method: 'get'
    });
  },
  
  getBillsByStudentIdPaged(studentId: number, page: number = 0, size: number = 10) {
    return request({
      url: `/api/bills/student/${studentId}/page?page=${page}&size=${size}`,
      method: 'get'
    });
  },
  
  getBillsByStatus(status: boolean, page: number = 0, size: number = 10) {
    return request({
      url: `/api/bills/status/${status}?page=${page}&size=${size}`,
      method: 'get'
    });
  },
  
  updateBillStatus(id: number, status: boolean) {
    return request({
      url: `/api/bills/${id}/status?status=${status}`,
      method: 'put'
    });
  },
  
  payBill(id: number, paymentParams: BillPaymentParams) {
    console.log(`发起支付请求：账单ID=${id}, 支付方式=${paymentParams.paymentMethod}`);
    return request({
      url: `/api/bills/${id}/pay`,
      method: 'put',
      data: paymentParams
    });
  },
  
  generateBills(params: BillGenerateParams) {
    return request({
      url: '/api/bills/generate',
      method: 'post',
      data: params
    });
  },
  
  deleteBill(id: number) {
    return request({
      url: `/api/bills/${id}`,
      method: 'delete'
    });
  },
  
  // 根据班级ID获取账单
  getBillsByClassId(classId: number, page: number = 0, size: number = 10) {
    return request({
      url: `/api/bills/class/${classId}?page=${page}&size=${size}`,
      method: 'get'
    });
  },
  
  // 根据教师ID获取其管理的所有班级的学生账单
  getBillsByTeacherId(teacherId: number, page: number = 0, size: number = 10) {
    return request({
      url: `/api/bills/teacher/${teacherId}?page=${page}&size=${size}`,
      method: 'get'
    });
  }
}; 