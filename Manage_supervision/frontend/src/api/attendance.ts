import request from '../utils/axios';

// 考勤数据类型
export interface AttendanceDTO {
  id?: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  createTime?: string;
  creatorId?: number;
  creatorName?: string;
  status?: string;
  totalUsers?: number;
  checkedInUsers?: number;
  records?: AttendanceRecordDTO[];
}

// 考勤记录数据类型
export interface AttendanceRecordDTO {
  id?: number;
  attendanceId?: number;
  userId?: number;
  username?: string;
  realName?: string;
  userNumber?: string;
  checkInTime?: string;
  status?: string;
  location?: string;
  notes?: string;
  faceVerified?: boolean;
}

// 将YYYY-MM-DD HH:mm:ss格式转换为ISO-8601格式（YYYY-MM-DDThh:mm:ss）
const formatToISODateTime = (dateTimeStr: string): string => {
  if (!dateTimeStr) return dateTimeStr;
  return dateTimeStr.replace(' ', 'T');
};

// 获取所有考勤列表（管理员）
export const getAttendanceList = async (page: number = 1, size: number = 10, status?: string) => {
  const params: any = { page, size };
  if (status) {
    params.status = status;
  }
  
  const response = await request.get('/api/attendance/admin/list', { params });
  return response.data;
};

// 获取考勤详情
export const getAttendanceById = async (id: number) => {
  const response = await request.get(`/api/attendance/${id}`);
  return response.data;
};

// 创建考勤
export const createAttendance = async (attendance: AttendanceDTO) => {
  // 转换日期时间格式
  const formattedAttendance = {
    ...attendance,
    startTime: formatToISODateTime(attendance.startTime),
    endTime: formatToISODateTime(attendance.endTime)
  };
  
  const response = await request.post('/api/attendance', formattedAttendance);
  return response.data;
};

// 更新考勤
export const updateAttendance = async (id: number, attendance: AttendanceDTO) => {
  // 转换日期时间格式
  const formattedAttendance = {
    ...attendance,
    startTime: formatToISODateTime(attendance.startTime),
    endTime: formatToISODateTime(attendance.endTime)
  };
  
  const response = await request.put(`/api/attendance/${id}`, formattedAttendance);
  return response.data;
};

// 删除考勤
export const deleteAttendance = async (id: number) => {
  const response = await request.delete(`/api/attendance/${id}`);
  return response.data;
};

// 获取考勤记录
export const getAttendanceRecords = async (id: number, page: number = 1, size: number = 10) => {
  const response = await request.get(`/api/attendance/${id}/records`, {
    params: { page, size }
  });
  return response.data;
};

// 获取未签到用户列表
export const getUncheckedUsers = async (id: number) => {
  const response = await request.get(`/api/attendance/${id}/unchecked`);
  return response.data;
};

// 获取考勤统计信息
export const getAttendanceStatistics = async (id: number) => {
  const response = await request.get(`/api/attendance/${id}/statistics`);
  return response.data;
};

// 导出考勤记录
export const exportAttendanceRecords = (id: number) => {
  return request.get(`/api/attendance/${id}/export`, {
    responseType: 'blob'
  });
};

// 获取考勤汇总
export const getAttendanceSummary = async (startDate?: string, endDate?: string) => {
  const params: Record<string, string> = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  
  const response = await request.get('/api/attendance/admin/summary', { params });
  return response.data;
};

// 考勤签到 (已弃用，强制使用人脸签到)
export const checkIn = async (attendanceId: number, record: AttendanceRecordDTO) => {
  throw new Error('普通签到已禁用，请使用人脸签到');
};

// 人脸考勤签到
export const faceCheckIn = async (attendanceId: number, record: AttendanceRecordDTO, faceData: string) => {
  // 确保提取base64数据部分（去掉前缀）
  const base64Data = faceData.includes(',') ? faceData.split(',')[1] : faceData;
  
  const data = {
    ...record,
    faceData: base64Data
  };
  
  console.log('发送人脸签到请求，数据长度:', base64Data.length);
  
  try {
    const response = await request.post(`/api/attendance/${attendanceId}/face-check-in`, data);
    // 无论成功还是包含错误信息，都返回响应数据
    return response;
  } catch (error: any) {
    // 如果是400错误，可能包含错误信息，应当正常返回
    if (error.response && error.response.status === 400 && error.response.data) {
      return error.response;
    }
    // 其他错误继续抛出
    throw error;
  }
};

// 获取当前用户的考勤列表
export const getCurrentUserAttendances = async () => {
  const response = await request.get('/api/attendance/user/list');
  return response.data;
};

// 获取当前用户的考勤记录
export const getCurrentUserAttendanceRecords = async () => {
  const response = await request.get('/api/attendance/user/records');
  return response.data;
}; 