import axios from 'axios';

const API_URL = '/api/classes';

// 获取所有班级
export const getAllClasses = async (keyword?: string) => {
  let url = API_URL;
  const params: Record<string, string> = {};
  
  if (keyword) {
    params.keyword = keyword;
  }
  
  const response = await axios.get(url, { params });
  return response.data;
};

// 获取班级详情
export const getClassById = async (id: number) => {
  const response = await axios.get(`${API_URL}/${id}`);
  return response.data;
};

// 创建班级
export const createClass = async (classData: any) => {
  const response = await axios.post(API_URL, classData);
  return response.data;
};

// 更新班级
export const updateClass = async (id: number, classData: any) => {
  const response = await axios.put(`${API_URL}/${id}`, classData);
  return response.data;
};

// 删除班级
export const deleteClass = async (id: number) => {
  return await axios.delete(`${API_URL}/${id}`);
};

// 获取班级的教师列表
export const getTeachersByClassId = async (classId: number) => {
  const response = await axios.get(`${API_URL}/${classId}/teachers`);
  return response.data;
};

// 获取教师的班级列表
export const getClassesByTeacherId = async (teacherId: number) => {
  const response = await axios.get(`${API_URL}/teacher/${teacherId}`);
  return response.data;
};

// 分配教师到班级
export const assignTeacherToClass = async (classId: number, teacherId: number) => {
  const response = await axios.post(`${API_URL}/${classId}/teachers/${teacherId}`);
  return response.data;
};

// 从班级移除教师
export const removeTeacherFromClass = async (classId: number, teacherId: number) => {
  return await axios.delete(`${API_URL}/${classId}/teachers/${teacherId}`);
}; 