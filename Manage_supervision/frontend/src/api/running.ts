import request from '../utils/request'

// 获取用户跑步偏好
export function getRunningPreference() {
  return request({
    url: '/api/profile/running/preference',
    method: 'get'
  })
}

// 保存用户跑步偏好
export function saveRunningPreference(data: any) {
  return request({
    url: '/api/profile/running/preference',
    method: 'post',
    data
  })
}

// 保存用户个人宣言
export function saveMotto(motto: string) {
  return request({
    url: '/api/profile/running/motto',
    method: 'post',
    data: { motto }
  })
}

// 添加跑步记录
export function addRunningRecord(data: {
  distance: number,
  duration: number,
  pace: string,
  recordDate?: string
}) {
  return request({
    url: '/api/student/running/record',
    method: 'post',
    data
  })
}

// 获取跑步记录列表
export function getRunningRecords() {
  return request({
    url: '/api/student/running/records',
    method: 'get'
  })
}

// 获取特定跑步记录
export function getRunningRecord(id: number) {
  return request({
    url: `/api/student/running/record/${id}`,
    method: 'get'
  })
}

// 获取跑步统计数据
export function getRunningStats() {
  return request({
    url: '/api/student/running/stats',
    method: 'get'
  })
}

// 获取用户成就信息
export function getUserAchievements() {
  return request({
    url: '/api/student/running/achievements',
    method: 'get'
  })
} 