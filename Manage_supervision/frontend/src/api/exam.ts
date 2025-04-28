import axios from '../utils/axios';

/**
 * 获取教师创建的考试列表（带分页）
 */
export const getExams = async (params: {
  page?: number;
  size?: number;
  status?: string;
  keyword?: string;
}) => {
  return await axios.get('/api/supervisor/exams', { params });
};

/**
 * 创建考试
 */
export const createExam = async (examData: any) => {
  return await axios.post('/api/supervisor/exams', examData);
};

/**
 * 更新考试
 */
export const updateExam = async (examId: number, examData: any) => {
  return await axios.put(`/api/supervisor/exams/${examId}`, examData);
};

/**
 * 删除考试
 */
export const deleteExam = async (examId: number) => {
  return await axios.delete(`/api/supervisor/exams/${examId}`);
};

/**
 * 获取考试详情
 */
export const getExam = async (examId: number) => {
  return await axios.get(`/api/supervisor/exams/${examId}`);
};

/**
 * 发布考试
 */
export const publishExam = async (examId: number) => {
  return await axios.post(`/api/supervisor/exams/${examId}/publish`);
};

/**
 * 获取考试题目列表
 */
export const getExamQuestions = async (examId: number) => {
  return await axios.get(`/api/supervisor/exams/${examId}/questions`);
};

/**
 * 获取考试学生列表
 */
export const getExamStudents = async (examId: number) => {
  return await axios.get(`/api/supervisor/exams/${examId}/students`);
};

/**
 * 添加考试题目
 */
export const addExamQuestions = async (examId: number, questions: any[]) => {
  return await axios.post(`/api/supervisor/exams/${examId}/questions`, questions);
};

/**
 * 添加考试学生
 */
export const addExamStudents = async (examId: number, students: any[]) => {
  return await axios.post(`/api/supervisor/exams/${examId}/students`, students);
};

/**
 * 删除考试题目
 */
export const removeExamQuestion = async (examId: number, questionId: number) => {
  return await axios.delete(`/api/supervisor/exams/${examId}/questions/${questionId}`);
};

/**
 * 删除考试学生
 */
export const removeExamStudent = async (examId: number, studentId: number) => {
  return await axios.delete(`/api/supervisor/exams/${examId}/students/${studentId}`);
};

/**
 * 获取学生可参加的考试列表
 */
export const getStudentExams = async (params: {
  page?: number;
  size?: number;
  status?: string;
}) => {
  return await axios.get('/api/student/exams', { params });
};

/**
 * 获取学生考试详情
 */
export const getStudentExam = async (examId: number) => {
  return await axios.get(`/api/student/exams/${examId}`);
};

/**
 * 学生开始考试
 */
export const startExam = async (examId: number) => {
  return await axios.post(`/api/student/exams/${examId}/start`);
};

/**
 * 学生提交考试
 */
export const submitExam = async (examId: number, answers: Record<string, string>) => {
  return await axios.post(`/api/student/exams/${examId}/submit`, answers);
};

/**
 * 调试API：创建测试考试数据
 */
export const createTestExamData = async () => {
  return await axios.post('/api/student/exams/test-data');
};

/**
 * 调试API：直接返回原始响应，用于分析数据结构
 */
export const getStudentExamsRaw = async (params: {
  page?: number;
  size?: number;
  status?: string;
}) => {
  const response = await axios.get('/api/student/exams', { params });
  console.log('原始响应对象:', response);
  console.log('响应数据类型:', typeof response.data);
  console.log('响应数据结构:', JSON.stringify(response.data, null, 2));
  return response;
};

/**
 * 获取学生考试答案详情（教师批阅用）
 */
export const getStudentExamAnswers = async (examId: number, studentId: number) => {
  return await axios.get(`/api/supervisor/exams/${examId}/students/${studentId}/answers`);
};

/**
 * 批阅学生考试
 */
export const gradeStudentExam = async (examId: number, studentId: number, gradeData: any) => {
  return await axios.post(`/api/supervisor/exams/${examId}/students/${studentId}/grade`, gradeData);
}; 