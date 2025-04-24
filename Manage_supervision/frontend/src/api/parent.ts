import http from '@/utils/axios'
import type { 
  ParentChildRelationData,
  ParentData,
  PagedResponse
} from '@/types/user'

/**
 * 创建家长账户
 * @param parent 家长信息
 * @returns 创建的家长信息
 */
export const createParent = async (parent: ParentData): Promise<any> => {
  return http.post('/api/admin/parents', parent)
}

/**
 * 获取家长列表（分页）
 * @param page 页码
 * @param size 每页大小
 * @param keyword 搜索关键词
 * @returns 家长列表
 */
export const getParentList = async (page: number, size: number, keyword?: string): Promise<PagedResponse> => {
  console.log(`正在获取家长列表，页码:${page}, 大小:${size}, 关键词:${keyword || '无'}`)
  try {
    const response = await http.get('/api/admin/users', {
      params: {
        page,
        size,
        role: 'PARENT',
        keyword
      }
    })
    console.log('获取家长列表成功，接收到数据:', response)
    return response.data as PagedResponse
  } catch (error) {
    console.error('获取家长列表失败:', error)
    throw error
  }
}

/**
 * 获取家长的子女关系列表
 * @param parentId 家长ID
 * @returns 子女关系列表
 */
export const getParentChildren = async (parentId: number): Promise<any> => {
  console.log(`正在获取家长(${parentId})的子女列表...`)
  try {
    const response = await http.get(`/api/admin/parents/${parentId}/children`)
    console.log('获取子女列表成功，接收到数据:', response)
    return response.data
  } catch (error) {
    console.error('获取子女列表失败:', error)
    throw error
  }
}

/**
 * 管理员为家长分配子女
 * @param parentId 家长ID
 * @param childIdentifier 子女标识符（用户名或学号）
 * @param relationType 关系类型
 * @returns 创建的关系
 */
export const assignChildToParent = async (
  parentId: number, 
  childIdentifier: string, 
  relationType: string
): Promise<any> => {
  return http.post(`/api/admin/parents/${parentId}/assign-child`, {
    childIdentifier,
    relationType
  })
}

/**
 * 移除家长与子女的关系
 * @param relationId 关系ID
 * @returns 操作结果
 */
export const removeParentChildRelation = async (relationId: number): Promise<any> => {
  return http.delete(`/api/admin/parent-child-relations/${relationId}`)
}

/**
 * 家长端 - 获取当前家长的子女关系列表
 * @returns 子女关系列表
 */
export const getMyChildren = async (): Promise<any> => {
  return http.get('/api/parent/children')
}

/**
 * 家长端 - 发送子女绑定请求
 * @param childIdentifier 子女标识符（用户名或学号）
 * @param relationType 关系类型
 * @returns 创建的关系
 */
export const requestChildBinding = async (
  childIdentifier: string,
  relationType: string
): Promise<any> => {
  return http.post('/api/parent/children/relate', {
    childIdentifier,
    relationType
  })
}

/**
 * 家长端 - 删除与子女的关系
 * @param relationId 关系ID
 * @returns 操作结果
 */
export const deleteChildRelation = async (relationId: number): Promise<any> => {
  return http.delete(`/api/parent/children/${relationId}`)
}

/**
 * 管理员获取学生的家长列表
 * @param studentId 学生ID
 * @returns 家长列表
 */
export const getStudentParents = async (studentId: number): Promise<any> => {
  console.log(`正在获取学生(${studentId})的家长列表...`)
  try {
    const response = await http.get(`/api/admin/students/${studentId}/parents`)
    console.log('获取学生家长列表成功，接收到数据:', response)
    return response.data
  } catch (error) {
    console.error('获取学生家长列表失败:', error)
    throw error
  }
} 