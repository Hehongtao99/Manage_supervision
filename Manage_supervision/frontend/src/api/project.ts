import axios from '../utils/axios';
import type { TaskRequest, Task } from './task';

export interface ProjectEvaluation {
  id: number;
  projectId: number;
  projectTitle: string;
  score: number;
  comment: string;
  evaluatedBy: number;
  evaluatorName: string;
  evaluationTime: string;
}

export interface Project {
  id: number;
  title: string;
  description?: string;
  category?: string;
  requirements?: string;
  resources?: string;
  status: string;
  startTime?: string;
  endTime?: string;
  supervisorId: number;
  assigneeId?: number;
  assigneeName?: string;
  createTime?: string;
  updateTime?: string;
  taskRequest?: TaskRequest;
  tasks?: Task[];
}

// 获取督导的所有项目
export const getSupervisorProjects = async (supervisorId: number) => {
  const response = await axios.get(`/api/supervisor/projects?supervisorId=${supervisorId}`);
  return response.data;
};

// 获取督导的特定状态项目
export const getSupervisorProjectsByStatus = async (supervisorId: number, status: string) => {
  const response = await axios.get(`/api/supervisor/projects/status/${status}?supervisorId=${supervisorId}`);
  return response.data;
};

// 获取项目详情
export const getProjectDetail = async (projectId: number) => {
  const response = await axios.get(`/api/supervisor/projects/${projectId}`);
  return response.data;
};

// 创建新项目
export const createProject = async (projectData: Partial<Project>) => {
  const response = await axios.post('/api/supervisor/projects', projectData);
  return response.data;
};

// 更新项目
export const updateProject = async (projectId: number, projectData: Partial<Project>) => {
  const response = await axios.put(`/api/supervisor/projects/${projectId}`, projectData);
  return response.data;
};

// 删除项目
export const deleteProject = async (projectId: number) => {
  const response = await axios.delete(`/api/supervisor/projects/${projectId}`);
  return response.status === 200;
};

// 分配项目给学生
export const assignProjectToStudent = async (projectId: number, studentId: number) => {
  const response = await axios.put(`/api/supervisor/projects/${projectId}/assign/${studentId}`);
  return response.data;
};

// 更新项目状态
export const updateProjectStatus = async (projectId: number, status: string) => {
  const response = await axios.put(`/api/supervisor/projects/${projectId}/status`, { status });
  return response.data;
};

// 按类别获取项目
export const getProjectsByCategory = async (category: string) => {
  const response = await axios.get(`/api/supervisor/projects/category/${category}`);
  return response.data;
};

// 获取学生的项目
export const getStudentProjects = async () => {
  const response = await axios.get(`/api/user/projects`);
  return response.data;
};

// 按优先级获取项目
export const getProjectsByPriority = async (priority: string) => {
  const response = await axios.get(`/api/supervisor/projects/priority/${priority}`);
  return response.data;
};

// 获取所有项目类别
export const getAllProjectCategories = async () => {
  const response = await axios.get('/api/supervisor/projects/categories');
  return response.data;
};

// 学生申请课题
export const applyForProject = async (projectData: Partial<Project>) => {
  // 确保数据格式正确
  const data = {
    ...projectData,
    // 如果有任务，确保格式正确
    tasks: projectData.tasks ? projectData.tasks.map(task => ({
      ...task,
      // 确保必要的字段存在
      supervisorId: projectData.supervisorId,
      // 如果startTime未设置，使用当前时间
      startTime: task.startTime || new Date().toISOString(),
      // 确保状态正确设置
      status: task.status || '未开始'
    })) : []
  };
  
  const response = await axios.post('/api/user/projects/apply', data);
  return response.data;
};

// 获取督导员待审核的课题申请
export const getPendingReviewProjects = async (supervisorId: number) => {
  const response = await axios.get(`/api/supervisor/projects/pending-review?supervisorId=${supervisorId}`);
  return response.data;
};

// 督导员审核课题申请
export const reviewProjectApplication = async (projectId: number, approved: boolean) => {
  const response = await axios.put(`/api/supervisor/projects/${projectId}/review`, { 
    approved
  });
  return response.data;
};

// ==================== 课题评价接口 ====================

/**
 * 创建或更新课题评价（督导视角）
 */
export const evaluateProject = async (projectId: number, score: number, comment: string): Promise<ProjectEvaluation> => {
  const response = await axios.post<ProjectEvaluation>(`/api/supervisor/projects/${projectId}/evaluate`, { score, comment });
  return response.data;
};

/**
 * 获取课题评价（督导视角）
 */
export const getProjectEvaluation = async (projectId: number): Promise<ProjectEvaluation> => {
  const response = await axios.get<ProjectEvaluation>(`/api/supervisor/projects/${projectId}/evaluation`);
  return response.data;
};

/**
 * 删除课题评价（督导视角）
 */
export const deleteProjectEvaluation = async (projectId: number): Promise<boolean> => {
  const response = await axios.delete(`/api/supervisor/projects/${projectId}/evaluation`);
  return response.status === 200;
};

/**
 * 获取课题评价（学生视角）
 */
export const getMyProjectEvaluation = async (projectId: number): Promise<ProjectEvaluation> => {
  const response = await axios.get<ProjectEvaluation>(`/api/user/projects/${projectId}/evaluation`);
  return response.data;
}; 