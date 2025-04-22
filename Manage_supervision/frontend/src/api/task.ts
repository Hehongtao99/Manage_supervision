import axios from '../utils/axios'
import { useUserStore } from '../stores/user'

export interface TaskCourse {
  name: string;
  status: string;
  description: string;
  completion: number;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  status: string;
  priority: string;
  supervisorId: number;
  supervisorName: string;
  assigneeId: number;
  assigneeName: string;
  projectId?: number;
  parentTaskId?: number;
  completed?: boolean;
  createTime: string;
  updateTime: string;
  type?: string;       // 任务类型，如初稿、期中、终稿
  progress?: number;   // 任务进度百分比
  lastSubmissionTime?: string; // 最后提交时间
  submissionCount?: number; // 提交次数
}

export interface TaskRequest {
  titleTemplate: string;
  descriptionTemplate: string;
  priority: string;
  status: string;
  startTime: string;
  endTime: string;
  assignToAllStudents: boolean;
}

export interface TaskSubmission {
  id: number;
  taskId: number;
  taskTitle: string;
  filePath: string;
  originalFilename: string;
  fileType: string;
  fileSize: number;
  submitterId: number;
  submitterName: string;
  submissionTime: string;
  comment: string;
}

/**
 * 任务评价接口定义
 */
export interface TaskEvaluation {
  id: number;
  taskId: number;
  taskTitle: string;
  score: number;
  comment: string;
  evaluatedBy: number;
  evaluatorName: string;
  evaluationTime: string;
}

// 任务缓存，用于提高性能
const taskCache = new Map<string, {
  tasks: Task[],
  timestamp: number
}>();

// 缓存过期时间（毫秒）
const CACHE_EXPIRY = 5000; // 5秒，降低缓存时间以确保数据更新更及时

// ==================== 督导接口 ====================

// 创建任务（督导视角）
export const createTask = async (taskData: any): Promise<Task> => {
  console.log('创建任务API调用，数据:', taskData);
  const response = await axios.post<Task>('/api/supervisor/tasks', taskData);
  
  // 如果任务有关联项目，清除该项目的任务缓存
  if (taskData.projectId) {
    // 清除缓存
    const cacheKey = `project-tasks-${taskData.projectId}`;
    taskCache.delete(cacheKey);
  }
  
  return response.data;
}

// 更新任务（督导视角）
export const updateTask = async (id: number, taskData: any): Promise<Task> => {
  console.log('更新任务API调用，任务ID:', id, '数据:', taskData);
  const response = await axios.put<Task>(`/api/supervisor/tasks/${id}`, taskData);
  
  // 如果任务有关联项目，清除该项目的任务缓存
  if (taskData.projectId) {
    // 清除缓存
    const cacheKey = `project-tasks-${taskData.projectId}`;
    taskCache.delete(cacheKey);
  }
  
  return response.data;
}

// 获取任务详情（督导视角）
export const getTaskDetail = async (id: number): Promise<Task> => {
  const response = await axios.get<Task>(`/api/supervisor/tasks/${id}`);
  return response.data;
}

// 删除任务（督导视角）
export const deleteTask = async (id: number): Promise<boolean> => {
  // 先获取任务信息，以便得到projectId
  try {
    const task = await getTaskDetail(id);
    const response = await axios.delete(`/api/supervisor/tasks/${id}`);
    
    // 如果任务有关联项目，清除该项目的任务缓存
    if (task && task.projectId) {
      // 清除缓存
      const cacheKey = `project-tasks-${task.projectId}`;
      taskCache.delete(cacheKey);
    }
    
    return response.status === 200;
  } catch (error) {
    console.error('删除任务失败:', error);
    return false;
  }
}

// 获取督导创建的所有任务
export const getSupervisorTasks = async (supervisorId: number): Promise<Task[]> => {
  const response = await axios.get<Task[]>(`/api/supervisor/tasks?supervisorId=${supervisorId}`);
  return response.data;
}

// 获取督导创建的指定状态的任务
export const getSupervisorTasksByStatus = async (supervisorId: number, status: string): Promise<Task[]> => {
  const response = await axios.get<Task[]>(`/api/supervisor/tasks/status/${status}?supervisorId=${supervisorId}`);
  return response.data;
}

// 将任务分配给学生
export const assignTaskToStudent = async (taskId: number, studentId: number): Promise<Task> => {
  const response = await axios.put<Task>(`/api/supervisor/tasks/${taskId}/assign/${studentId}`);
  return response.data;
}

// 更新任务状态（督导视角）
export const updateTaskStatus = async (id: number, status: string): Promise<Task> => {
  const response = await axios.put<Task>(`/api/supervisor/tasks/${id}/status`, { status });
  return response.data;
}

// ==================== 学生接口 ====================

// 获取分配给学生的所有任务
export const getMyTasks = async (): Promise<Task[]> => {
  const response = await axios.get<Task[]>('/api/user/tasks');
  return response.data;
}

// 获取分配给学生的指定状态的任务
export const getMyTasksByStatus = async (status: string): Promise<Task[]> => {
  const response = await axios.get<Task[]>(`/api/user/tasks/status/${status}`);
  return response.data;
}

// 获取任务详情（学生视角）
export const getMyTaskDetail = async (id: number): Promise<Task> => {
  const response = await axios.get<Task>(`/api/user/tasks/${id}`);
  return response.data;
}

// 更新任务状态（学生视角）
export const updateMyTaskStatus = async (id: number, status: string): Promise<Task> => {
  const response = await axios.put<Task>(`/api/user/tasks/${id}/status`, { status });
  return response.data;
}

// 批量创建任务（督导视角）
export const createBatchTasks = async (projectId: number, taskRequest: TaskRequest): Promise<Task[]> => {
  const response = await axios.post<Task[]>(`/api/supervisor/projects/${projectId}/batch-tasks`, taskRequest);
  return response.data;
}

// 获取项目下的任务
export const getProjectTasks = async (projectId: number): Promise<Task[]> => {
  try {
    // 检查缓存
    const cacheKey = `project-tasks-${projectId}`;
    const cachedData = taskCache.get(cacheKey);
    const now = Date.now();

    if (cachedData && (now - cachedData.timestamp < CACHE_EXPIRY)) {
      console.log(`使用缓存的任务数据，项目ID: ${projectId}`);
      return cachedData.tasks;
    }

    // 获取当前用户信息
    const userStore = useUserStore();
    const isSupervisor = userStore.isSupervisor;
    
    // 根据角色选择不同的API端点
    let apiUrl = '';
    if (isSupervisor) {
      // 督导员角色直接使用督导专用API端点，无论课题来源
      apiUrl = `/api/supervisor/projects/${projectId}/batch-tasks`;
      console.log(`督导员使用API: ${apiUrl}`);
    } else {
      // 学生用户使用学生API端点
      apiUrl = `/api/user/projects/${projectId}/tasks`;
      console.log(`学生使用API: ${apiUrl}`);
    }
    
    // 设置请求超时
    const response = await axios.get<Task[]>(apiUrl, {
      timeout: 10000,  // 10秒超时
      headers: {
        'Cache-Control': 'no-cache'  // 确保获取最新数据
      }
    });
    
    // 更新缓存
    const tasks = response.data;
    taskCache.set(cacheKey, {
      tasks,
      timestamp: now
    });
    
    return tasks;
  } catch (error: any) {
    // 针对不同错误类型提供不同的错误信息
    if (error.response) {
      // 获取当前用户角色
      const userStore = useUserStore();
      const isSupervisor = userStore.isSupervisor;
      
      if (error.response.status === 403) {
        console.error(`权限错误：无权查看课题ID=${projectId}的任务`);
        if (isSupervisor) {
          // 督导遇到权限错误时使用不同的错误消息
          throw new Error(`无法获取课题下的任务，请确认课题ID是否正确或尝试刷新页面`);
        } else {
          // 学生遇到权限错误时的消息
          throw new Error(`无权查看该课题的任务，请确认该课题是否分配给了您`);
        }
      } else if (error.response.status === 404) {
        console.error(`未找到课题：课题ID=${projectId}不存在`);
        throw new Error(`未找到该课题，课题可能已被删除`);
      } else if (error.response.data?.message) {
        console.error(`获取课题任务错误：${error.response.data.message}`);
        throw new Error(error.response.data.message);
      }
    }
    // 默认错误处理
    console.error(`获取课题任务失败：`, error);
    throw error;
  }
}

// 更新任务完成状态
export const updateTaskCompletionStatus = async (taskId: number, completed: boolean): Promise<Task> => {
  const response = await axios.put<Task>(`/api/tasks/${taskId}/completion`, { completed });
  return response.data;
};

// 提交任务文件
export const submitTaskFile = async (taskId: number, formData: FormData): Promise<TaskSubmission> => {
  const response = await axios.post<TaskSubmission>(`/api/user/tasks/${taskId}/submit`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  return response.data;
};

// 获取任务提交记录
export const getTaskSubmissions = async (taskId: number): Promise<TaskSubmission[]> => {
  const response = await axios.get<TaskSubmission[]>(`/api/tasks/${taskId}/submissions`);
  return response.data;
};

// 获取提交详情
export const getSubmissionDetail = async (submissionId: number): Promise<TaskSubmission> => {
  const response = await axios.get<TaskSubmission>(`/api/submissions/${submissionId}`);
  return response.data;
};

// 删除提交记录
export const deleteSubmission = async (submissionId: number): Promise<boolean> => {
  const response = await axios.delete(`/api/user/submissions/${submissionId}`);
  return response.status === 200;
};

// 更新提交评论
export const updateSubmissionComment = async (submissionId: number, comment: string): Promise<TaskSubmission> => {
  const response = await axios.put<TaskSubmission>(`/api/user/submissions/${submissionId}/comment`, { comment });
  return response.data;
};

// 获取用户的所有提交记录
export const getUserSubmissions = async (): Promise<TaskSubmission[]> => {
  const response = await axios.get<TaskSubmission[]>(`/api/user/submissions`);
  return response.data;
};

// 获取所有可用任务（未分配到项目的任务）
export const getAvailableTasks = async (supervisorId: number): Promise<Task[]> => {
  const response = await axios.get<Task[]>(`/api/supervisor/tasks/available?supervisorId=${supervisorId}`);
  return response.data;
};

// ==================== 任务评价接口 ====================

/**
 * 创建或更新任务评价（督导视角）
 */
export const evaluateTask = async (taskId: number, score: number, comment: string): Promise<TaskEvaluation> => {
  const response = await axios.post<TaskEvaluation>(`/api/supervisor/tasks/${taskId}/evaluate`, { score, comment });
  return response.data;
};

/**
 * 获取任务评价（督导视角）
 */
export const getTaskEvaluation = async (taskId: number): Promise<TaskEvaluation> => {
  const response = await axios.get<TaskEvaluation>(`/api/supervisor/tasks/${taskId}/evaluation`);
  return response.data;
};

/**
 * 删除任务评价（督导视角）
 */
export const deleteTaskEvaluation = async (taskId: number): Promise<boolean> => {
  const response = await axios.delete(`/api/supervisor/tasks/${taskId}/evaluation`);
  return response.status === 200;
};

/**
 * 获取任务评价（学生视角）
 */
export const getMyTaskEvaluation = async (taskId: number): Promise<TaskEvaluation> => {
  const response = await axios.get<TaskEvaluation>(`/api/user/tasks/${taskId}/evaluation`);
  return response.data;
};

// 清除特定项目的任务缓存
export const clearProjectTasksCache = (projectId: number) => {
  const cacheKey = `project-tasks-${projectId}`;
  if (taskCache.has(cacheKey)) {
    taskCache.delete(cacheKey);
    console.log(`已清除项目(ID:${projectId})的任务缓存`);
    return true;
  }
  return false;
};