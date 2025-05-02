import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission, hasAnyPermission, hasAllPermissions } from '../utils/permission'

/**
 * 权限指令
 * 使用方式:
 * 1. 单个权限: v-permission="'USER_VIEW'"
 * 2. 任意一个权限: v-permission="{ any: ['USER_VIEW', 'USER_EDIT'] }"
 * 3. 所有权限: v-permission="{ all: ['USER_VIEW', 'USER_EDIT'] }"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    
    let hasAuth = false
    
    if (typeof value === 'string') {
      // 单个权限
      hasAuth = hasPermission(value)
    } else if (value && typeof value === 'object') {
      if (value.any && Array.isArray(value.any)) {
        // 任意一个权限
        hasAuth = hasAnyPermission(value.any)
      } else if (value.all && Array.isArray(value.all)) {
        // 所有权限
        hasAuth = hasAllPermissions(value.all)
      }
    }
    
    if (!hasAuth) {
      // 没有权限则隐藏元素
      if (el.parentNode) {
        el.parentNode.removeChild(el)
      } else {
        el.style.display = 'none'
      }
    }
  }
}

// 注册指令
export default {
  install(app: any) {
    app.directive('permission', permission)
  }
} 