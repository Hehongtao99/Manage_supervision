import request from '../utils/request';

/**
 * 获取用户个人资料
 */
export function getUserProfile() {
  return request({
    url: '/api/user/profile',
    method: 'get'
  });
}

/**
 * 更新用户个人资料
 * @param data 个人资料数据
 */
export function updateUserProfile(data: any) {
  return request({
    url: '/api/user/profile',
    method: 'post',
    data
  });
}

/**
 * 更新用户密码
 * @param data 密码数据
 */
export function updatePassword(data: any) {
  return request({
    url: '/api/user/password',
    method: 'post',
    data
  });
}

/**
 * 获取用户跑步偏好
 */
export function getRunningPreference() {
  return request({
    url: '/api/profile/running/preference',
    method: 'get'
  });
}

/**
 * 保存用户跑步偏好
 * @param data 跑步偏好数据
 */
export function saveRunningPreference(data: any) {
  return request({
    url: '/api/profile/running/preference',
    method: 'post',
    data
  });
}

/**
 * 保存个人宣言
 * @param motto 个人宣言
 */
export function saveMotto(motto: string) {
  return request({
    url: '/api/profile/running/motto',
    method: 'post',
    data: { motto }
  });
} 