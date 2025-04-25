/**
 * 督导员/教师控制台DTO接口
 */
export interface SupervisorDashboardDTO {
  // 统计信息
  studentCount?: number      // 学生总数
  activeToday?: number       // 今日活跃学生数
  pendingTasks?: number      // 待处理事项数
  weeklyEvents?: number      // 本周活动数
  
  // 学生课题进度列表
  studentProjectProgresses?: StudentProjectProgressDTO[]
  
  // 最近活动列表
  recentActivities?: ActivityItem[]
}

/**
 * 学生课题进度DTO接口
 */
export interface StudentProjectProgressDTO {
  // 学生信息
  studentId: number
  studentName: string
  studentNumber: string // 学号
  
  // 课题信息
  projectId: number
  projectTitle: string
  projectStatus: string
  
  // 进度信息
  totalTasks: number     // 总任务数
  completedTasks: number // 已完成任务数
  progressPercentage: number // 进度百分比
  
  // 最近活动
  lastActivity?: string
  lastActivityDescription?: string
  
  // 状态信息
  status: string // 活跃、非活跃
  
  // 期限信息
  deadline?: string
  isOverdue?: boolean
  isNearDeadline?: boolean
}

/**
 * 活动项接口
 */
export interface ActivityItem {
  title: string          // 活动标题
  description: string    // 活动描述
  time: string           // 活动时间
  type: string           // 活动类型：作业提交、请假、注册、系统通知等
} 