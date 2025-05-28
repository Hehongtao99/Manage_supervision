import request from '../utils/request'
import axios from 'axios'
import { getAuthHeader } from '../utils/auth'

// 类型定义
export interface RunningRecordRequest {
  distance: number;
  duration: number; // 跑步时长，单位：秒
  pace: string;
  recordDateTime?: string; // 记录时间，格式：YYYY-MM-DD HH:mm:ss
}

export interface RunningRecordResponse {
  id: number;
  userId: number;
  distance: number;
  duration: number; // 跑步时长，单位：秒
  pace: string;
  calories: number;
  createTime: string;
  recordDate: string;
}

export interface RunningStatsResponse {
  totalDistance: number;
  totalDuration: number;
  totalCalories: number;
  recordCount: number;
  averagePace: string;
  weeklyDistance: number;
  monthlyDistance: number;
}

export interface AchievementResponse {
  totalDistance: number;
  totalDuration: number;
  totalRecords: number;
  longestDistance: number;
  fastestPace: string;
  consecutiveDays: number;
  weeklyGoalStreak: number;
  monthlyGoalStreak: number;
}

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
export const addRunningRecord = async (record: RunningRecordRequest): Promise<RunningRecordResponse> => {
  const response = await axios({
    method: 'POST',
    url: '/api/runner/running/record',
    data: record,
    headers: getAuthHeader()
  })
  return response.data
}

// 获取用户跑步记录
export const getUserRunningRecords = async (): Promise<RunningRecordResponse[]> => {
  const response = await axios({
    method: 'GET',
    url: '/api/runner/running/records',
    headers: getAuthHeader()
  })
  return response.data
}

// 获取跑步记录详情
export const getRunningRecordById = async (id: number): Promise<RunningRecordResponse> => {
  const response = await axios({
    method: 'GET',
    url: `/api/runner/running/record/${id}`,
    headers: getAuthHeader()
  })
  return response.data
}

// 获取跑步统计数据
export const getRunningStats = async (): Promise<RunningStatsResponse> => {
  const response = await axios({
    method: 'GET',
    url: '/api/runner/running/stats',
    headers: getAuthHeader()
  })
  return response.data
}

// 获取用户成就信息
export const getUserAchievements = async (): Promise<AchievementResponse> => {
  const response = await axios({
    method: 'GET',
    url: '/api/runner/running/achievements',
    headers: getAuthHeader()
  })
  return response.data
} 