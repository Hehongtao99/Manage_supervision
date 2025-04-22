// 该文件是为了兼容性而创建的，实际上重新导出student.ts中的内容

import { getStudents as _getStudents } from './student';

// 将getStudents作为getAllStudents导出，以便兼容旧代码
export const getAllStudents = _getStudents;

// 直接在此处定义Student类型，而不是导入StudentDTO
export interface Student {
  id: number;
  name: string;
  studentId: string;
  class: string; // 注意：这里用class而不是className，与StudentManagement.vue中使用一致
  email: string;
  phone: string;
  status: string;
  lastLogin?: string;
  progress?: number;
} 