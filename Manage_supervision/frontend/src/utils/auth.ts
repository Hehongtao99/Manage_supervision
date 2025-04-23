/**
 * 认证相关工具函数
 */

/**
 * 获取当前用户ID
 * @returns 当前登录用户的ID，如果未登录则返回null
 */
export function getCurrentUserId(): number | null {
  try {
    // 尝试从localStorage获取用户信息
    const userStoreStr = localStorage.getItem('userStore');
    if (userStoreStr) {
      const userStore = JSON.parse(userStoreStr);
      if (userStore.user && userStore.user.id) {
        return userStore.user.id;
      }
    }
    
    // 尝试从localStorage直接获取userId
    const userId = localStorage.getItem('userId');
    if (userId) {
      return parseInt(userId, 10);
    }
    
    return null;
  } catch (error) {
    console.error('获取用户ID失败:', error);
    return null;
  }
}

/**
 * 检查用户是否已登录
 * @returns 是否已登录
 */
export function isLoggedIn(): boolean {
  const token = localStorage.getItem('token');
  return !!token;
}

/**
 * 获取当前用户的角色数组
 * @returns 角色数组
 */
export function getUserRoles(): string[] {
  try {
    const userStoreStr = localStorage.getItem('userStore');
    if (userStoreStr) {
      const userStore = JSON.parse(userStoreStr);
      if (userStore.user && Array.isArray(userStore.user.roles)) {
        return userStore.user.roles;
      }
    }
    
    const rolesStr = localStorage.getItem('userRoles');
    if (rolesStr) {
      return JSON.parse(rolesStr);
    }
    
    return [];
  } catch (error) {
    console.error('获取用户角色失败:', error);
    return [];
  }
}

/**
 * 检查当前用户是否具有特定角色
 * @param role 要检查的角色
 * @returns 是否具有该角色
 */
export function hasRole(role: string): boolean {
  const roles = getUserRoles();
  return roles.some(r => r.toUpperCase() === role.toUpperCase());
} 