import axios from '../utils/axios'

export interface Course {
  id?: number;
  courseName: string;
  description?: string;
  teacherId?: number;
  teacherName?: string;
  duration?: number;
  category?: string;
  coverImage?: string;
  status?: string;
  createTime?: string;
  updateTime?: string;
}

export interface CourseQuery {
  page: number;
  size: number;
  courseName?: string;
  teacherId?: number;
  category?: string;
  status?: string;
}

export interface PageResponse<T> {
  content: T[];
  total: number;
  page: number;
  size: number;
}

/**
 * 获取课程列表
 */
export const getCourses = async (query: CourseQuery): Promise<PageResponse<Course>> => {
  const response = await axios.get('/api/courses', { params: query });
  return response.data;
}

/**
 * 根据ID获取课程详情
 */
export const getCourseById = async (id: number): Promise<Course> => {
  const response = await axios.get(`/api/courses/${id}`);
  return response.data;
}

/**
 * 创建新课程
 */
export const createCourse = async (course: Course): Promise<Course> => {
  const response = await axios.post('/api/courses', course);
  return response.data;
}

/**
 * 更新课程信息
 */
export const updateCourse = async (id: number, course: Course): Promise<Course> => {
  const response = await axios.put(`/api/courses/${id}`, course);
  return response.data;
}

/**
 * 删除课程
 */
export const deleteCourse = async (id: number): Promise<void> => {
  await axios.delete(`/api/courses/${id}`);
}

/**
 * 根据教师ID获取课程列表
 */
export const getCoursesByTeacherId = async (teacherId: number): Promise<Course[]> => {
  const response = await axios.get(`/api/courses/teacher/${teacherId}`);
  return response.data;
}

/**
 * 根据类别获取课程列表
 */
export const getCoursesByCategory = async (category: string): Promise<Course[]> => {
  const response = await axios.get(`/api/courses/category/${category}`);
  return response.data;
}

/**
 * 获取课程总数
 */
export const getCourseCount = async (): Promise<number> => {
  const response = await axios.get('/api/courses/count');
  return response.data;
}

/**
 * 获取教师课程数量
 */
export const getCourseCountByTeacherId = async (teacherId: number): Promise<number> => {
  const response = await axios.get(`/api/courses/teacher/${teacherId}/count`);
  return response.data;
} 