import { useUserStore } from '../stores/user'

/**
 * 检查当前用户是否有指定权限
 * @param permission 权限标识
 * @returns 是否有权限
 */
export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  
  // 如果用户没有登录，返回false
  if (!userStore.isLoggedIn) {
    return false
  }
  
  // 如果是管理员，直接返回true
  if (userStore.isAdmin) {
    return true
  }
  
  // 检查用户是否有该权限
  return userStore.permissions.includes(permission)
}

/**
 * 检查当前用户是否有指定的任意一个权限
 * @param permissions 权限标识数组
 * @returns 是否有权限
 */
export function hasAnyPermission(permissions: string[]): boolean {
  const userStore = useUserStore()
  
  // 如果用户没有登录，返回false
  if (!userStore.isLoggedIn) {
    return false
  }
  
  // 如果是管理员，直接返回true
  if (userStore.isAdmin) {
    return true
  }
  
  // 检查用户是否有任一权限
  return permissions.some(permission => userStore.permissions.includes(permission))
}

/**
 * 检查当前用户是否有指定的所有权限
 * @param permissions 权限标识数组
 * @returns 是否有权限
 */
export function hasAllPermissions(permissions: string[]): boolean {
  const userStore = useUserStore()
  
  // 如果用户没有登录，返回false
  if (!userStore.isLoggedIn) {
    return false
  }
  
  // 如果是管理员，直接返回true
  if (userStore.isAdmin) {
    return true
  }
  
  // 检查用户是否有所有权限
  return permissions.every(permission => userStore.permissions.includes(permission))
} 