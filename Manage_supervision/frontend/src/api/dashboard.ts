import axios from '../utils/axios'
import { useUserStore } from '../stores/user'

export interface UserInfo {
  id: number
  username: string
  roles: string[]
  createTime: string
}

export interface SystemInfo {
  javaVersion: string
  osName: string
  osVersion: string
  totalMemory: number
  freeMemory: number
  availableProcessors: number
}

export interface DashboardStats {
  totalUsers: number
  roleDistribution: Record<string, number>
  systemInfo: SystemInfo
  userList: UserInfo[]
}

export interface UserCreationTrend {
  dates: string[]
  counts: number[]
}

// 教师收入趋势接口
export interface TeacherIncomeTrend {
  dates: string[]
  amounts: number[]
}

// 教师课程收入分布接口
export interface TeacherCourseIncome {
  courseName: string
  amount: number
}

// 教师收入汇总接口
export interface TeacherIncomeStats {
  totalIncome: number
  thisMonthIncome: number
  lastMonthIncome: number
  courseIncomeDistribution: TeacherCourseIncome[]
}

// 学生学习记录报表接口
export interface StudentLearningRecords {
  courseNames: string[]
  courseHours: number[]
  totalCourses: number
  totalHours: number
}

// 学生学习趋势接口
export interface StudentLearningTrend {
  months: string[]
  hours: number[]
}

export const getDashboardStats = async (): Promise<DashboardStats> => {
  const userStore = useUserStore()
  
  // 根据用户角色选择合适的API端点
  let endpoint = '/stats'  // 默认使用普通用户端点
  
  if (userStore.isAdmin) {
    endpoint = '/admin/stats'  // 管理员使用管理员端点
  }
  
  // 添加调试日志
  console.log('调用仪表盘API，用户角色:', userStore.user.roles, '使用端点:', endpoint)
  
  const response = await axios.get<DashboardStats>(`/api/dashboard${endpoint}`)
  return response.data
}

/**
 * 获取近7天用户创建趋势数据
 * @returns 用户创建趋势数据，包含日期和对应的创建用户数量
 */
export const getUserCreationTrend = async (): Promise<UserCreationTrend> => {
  const response = await axios.get<UserCreationTrend>('/api/dashboard/admin/user-creation-trend')
  return response.data
}

/**
 * 获取学生学习记录报表数据 - 按课程统计学习时长
 * @returns 学习记录报表数据，包含课程名称和对应学习时长
 */
export const getStudentLearningRecords = async (): Promise<StudentLearningRecords> => {
  try {
    const response = await axios.get<StudentLearningRecords>('/api/dashboard/student/learning-records')
    return response.data
  } catch (error) {
    console.error('获取学生学习记录报表数据失败:', error)
    // 如果API不存在，返回模拟数据用于开发测试
    return getMockStudentLearningRecords()
  }
}

/**
 * 获取学生学习记录趋势数据 - 按时间统计学习时长
 * @returns 学习记录趋势数据，包含日期和对应学习时长
 */
export const getStudentLearningTrend = async (): Promise<StudentLearningTrend> => {
  try {
    const response = await axios.get<StudentLearningTrend>('/api/dashboard/student/learning-trend')
    return response.data
  } catch (error) {
    console.error('获取学生学习记录趋势数据失败:', error)
    // 如果API不存在，返回模拟数据用于开发测试
    return getMockStudentLearningTrend()
  }
}

/**
 * 获取教师收入趋势数据
 * @param period 时间周期：week - 近7天，month - 近30天，year - 近12个月
 * @returns 教师收入趋势数据，包含日期和对应的收入金额
 */
export const getTeacherIncomeTrend = async (period: 'week' | 'month' | 'year' = 'month'): Promise<TeacherIncomeTrend> => {
  try {
    const response = await axios.get<TeacherIncomeTrend>(`/api/supervisor/income/trend?period=${period}`)
    return response.data
  } catch (error) {
    console.error('获取教师收入趋势数据失败:', error)
    // 如果API不存在，返回模拟数据用于开发测试
    return getMockTeacherIncomeTrend(period)
  }
}

/**
 * 获取教师课程收入分布数据
 * @param period 时间周期：month - 当月，year - 当年，all - 所有时间
 * @returns 教师课程收入分布数据，包含课程名称和对应的收入金额
 */
export const getTeacherCourseIncomeDistribution = async (period: 'month' | 'year' | 'all' = 'month'): Promise<TeacherCourseIncome[]> => {
  try {
    const response = await axios.get<TeacherCourseIncome[]>(`/api/supervisor/income/distribution?period=${period}`)
    return response.data
  } catch (error) {
    console.error('获取教师课程收入分布数据失败:', error)
    // 如果API不存在，返回模拟数据用于开发测试
    return getMockTeacherCourseIncomeDistribution()
  }
}

/**
 * 获取教师收入统计数据
 * @returns 教师收入统计数据，包含总收入、当月收入、上月收入等
 */
export const getTeacherIncomeStats = async (): Promise<TeacherIncomeStats> => {
  try {
    const response = await axios.get<TeacherIncomeStats>('/api/supervisor/income/stats')
    return response.data
  } catch (error) {
    console.error('获取教师收入统计数据失败:', error)
    // 如果API不存在，返回模拟数据用于开发测试
    return getMockTeacherIncomeStats()
  }
}

// 模拟数据生成函数 - 仅在API未就绪时使用
function getMockTeacherIncomeTrend(period: 'week' | 'month' | 'year'): TeacherIncomeTrend {
  let dates: string[] = []
  let amounts: number[] = []
  
  if (period === 'week') {
    // 生成近7天数据
    const now = new Date()
    for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(now.getDate() - i)
      dates.push(`${date.getMonth() + 1}-${date.getDate()}`)
      amounts.push(Math.floor(Math.random() * 500) + 200)
    }
  } else if (period === 'month') {
    // 生成近30天数据
    const now = new Date()
    for (let i = 29; i >= 0; i--) {
      const date = new Date()
      date.setDate(now.getDate() - i)
      dates.push(`${date.getMonth() + 1}-${date.getDate()}`)
      amounts.push(Math.floor(Math.random() * 500) + 200)
    }
  } else {
    // 生成近12个月数据
    const now = new Date()
    for (let i = 11; i >= 0; i--) {
      const date = new Date()
      date.setMonth(now.getMonth() - i)
      dates.push(`${date.getFullYear()}-${date.getMonth() + 1}`)
      amounts.push(Math.floor(Math.random() * 5000) + 2000)
    }
  }
  
  return { dates, amounts }
}

function getMockTeacherCourseIncomeDistribution(): TeacherCourseIncome[] {
  return [
    { courseName: '高等数学', amount: 2500 },
    { courseName: '线性代数', amount: 1800 },
    { courseName: '概率论', amount: 1200 },
    { courseName: '大学物理', amount: 2100 },
    { courseName: '程序设计基础', amount: 3000 }
  ]
}

function getMockTeacherIncomeStats(): TeacherIncomeStats {
  return {
    totalIncome: 15600,
    thisMonthIncome: 3200,
    lastMonthIncome: 2800,
    courseIncomeDistribution: getMockTeacherCourseIncomeDistribution()
  }
}

/**
 * 模拟学生学习记录报表数据
 */
function getMockStudentLearningRecords(): StudentLearningRecords {
  return {
    courseNames: ['高等数学', '线性代数', '概率论', '大学物理', '程序设计基础'],
    courseHours: [20, 15, 10, 18, 25],
    totalCourses: 5,
    totalHours: 88
  }
}

/**
 * 模拟学生学习记录趋势数据
 */
function getMockStudentLearningTrend(): StudentLearningTrend {
  const now = new Date()
  const months: string[] = []
  const hours: number[] = []
  
  for (let i = 5; i >= 0; i--) {
    const date = new Date()
    date.setMonth(now.getMonth() - i)
    months.push(`${date.getFullYear()}-${date.getMonth() + 1}`)
    hours.push(Math.floor(Math.random() * 30) + 5)
  }
  
  return { months, hours }
} 