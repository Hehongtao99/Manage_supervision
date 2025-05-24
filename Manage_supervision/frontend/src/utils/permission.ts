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
    console.log('用户是管理员，自动拥有权限:', permission)
    return true
  }
  
  // 检查用户是否有该权限
  if (!userStore.permissions || userStore.permissions.length === 0) {
    console.log('用户没有任何权限')
    return false
  }
  
  // 直接检查权限编码
  if (userStore.permissions.includes(permission)) {
    return true
  }
  
  // 检查兼容格式
  let hasCompatiblePermission = false
  
  // 如果是新格式(user:view)，检查旧格式(USER_VIEW)
  if (permission.includes(':')) {
    const compatibleCode = permission.toUpperCase().replace(':', '_')
    if (userStore.permissions.includes(compatibleCode)) {
      hasCompatiblePermission = true
    }
  } 
  // 如果是旧格式(USER_VIEW)，检查新格式(user:view)
  else if (permission.includes('_')) {
    const compatibleCode = permission.toLowerCase().replace('_', ':')
    if (userStore.permissions.includes(compatibleCode)) {
      hasCompatiblePermission = true
    }
  }
  
  // 检查简单格式(如"system", "user"等)
  if (!hasCompatiblePermission && !permission.includes(':') && !permission.includes('_')) {
    for (const userCode of userStore.permissions) {
      if (userCode.startsWith(permission + ':') || 
          userCode.startsWith(permission.toUpperCase() + '_')) {
        hasCompatiblePermission = true
        break
      }
    }
  }
  
  console.log('检查权限:', permission, '结果:', hasCompatiblePermission)
  return hasCompatiblePermission
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
    console.log('用户是管理员，自动拥有任意权限')
    return true
  }
  
  // 检查用户是否有任一权限
  return permissions.some(permission => hasPermission(permission))
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
    console.log('用户是管理员，自动拥有所有权限')
    return true
  }
  
  // 检查用户是否有所有权限
  return permissions.every(permission => hasPermission(permission))
} 