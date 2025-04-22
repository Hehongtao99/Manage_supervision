import axios from 'axios';
import type { TimetableDTO, TimetableItemDTO } from '../types/timetable';

const API_URL = '/api/timetables';

export const getTimetablesByClassId = async (classId: number): Promise<TimetableDTO[]> => {
  if (classId === undefined || classId === null || isNaN(classId)) {
    console.error('班级ID无效:', classId);
    return [];
  }
  
  try {
    console.log(`请求班级${classId}的所有课表`);
    const response = await axios.get(`${API_URL}/class/${classId}`);
    return response.data;
  } catch (error) {
    console.error(`获取班级${classId}课表失败:`, error);
    throw error;
  }
};

export const getTimetablesByClassIdAndWeekNumber = async (
  classId: number,
  weekNumber: number
): Promise<TimetableDTO[]> => {
  if (classId === undefined || classId === null || isNaN(classId)) {
    console.error('班级ID无效:', classId);
    return [];
  }
  
  try {
    console.log(`请求班级${classId}第${weekNumber}周的课表`);
    const response = await axios.get(`${API_URL}/class/${classId}/week/${weekNumber}`);
    return response.data;
  } catch (error) {
    console.error(`获取班级${classId}第${weekNumber}周课表失败:`, error);
    throw error;
  }
};

export const getTimetableById = async (id: number): Promise<TimetableDTO> => {
  const response = await axios.get(`${API_URL}/${id}`);
  return response.data;
};

export const createTimetable = async (timetable: TimetableDTO): Promise<TimetableDTO> => {
  try {
    console.log('发送创建课表请求，数据:', timetable);
    const response = await axios.post(API_URL, timetable);
    console.log('创建课表成功，返回数据:', response.data);
    return response.data;
  } catch (error: any) {
    console.error('创建课表失败:', error);
    console.error('错误详情:', error.response?.data || error.message);
    throw error;
  }
};

export const updateTimetable = async (id: number, timetable: TimetableDTO): Promise<TimetableDTO> => {
  const response = await axios.put(`${API_URL}/${id}`, timetable);
  return response.data;
};

export const deleteTimetable = async (id: number): Promise<void> => {
  await axios.delete(`${API_URL}/${id}`);
};

export const getTimetableItems = async (timetableId: number): Promise<TimetableItemDTO[]> => {
  const response = await axios.get(`${API_URL}/${timetableId}/items`);
  return response.data;
};

export const createTimetableItem = async (item: TimetableItemDTO): Promise<TimetableItemDTO> => {
  const response = await axios.post(`${API_URL}/items`, item);
  return response.data;
};

export const updateTimetableItem = async (
  id: number,
  item: TimetableItemDTO
): Promise<TimetableItemDTO> => {
  const response = await axios.put(`${API_URL}/items/${id}`, item);
  return response.data;
};

export const deleteTimetableItem = async (id: number): Promise<void> => {
  await axios.delete(`${API_URL}/items/${id}`);
};

export const calculateTimeSlot = async (
  periodType: string,
  periodNumber: number
): Promise<any> => {
  const response = await axios.get(`${API_URL}/calculate-time`, {
    params: { periodType, periodNumber }
  });
  return response.data;
}; 