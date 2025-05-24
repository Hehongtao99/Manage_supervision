/**
 * 权限指令
 * 用法：
 * 1. 在main.ts中导入并注册：
 *    import permissionDirective from './directives/permission'
 *    app.use(permissionDirective)
 * 
 * 2. 在组件中使用：
 *    <button v-permission="'user:add'">添加用户</button>
 *    <button v-permission="['user:edit', 'user:delete']">编辑/删除用户</button>
 */

import { useUserStore } from '../stores/user'

function checkPermission(el, binding) {
  const { value } = binding
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  
  // 如果是管理员，直接显示
  if (userStore.isAdmin) {
    return
  }

  if (value && value.length > 0) {
    const hasPermission = Array.isArray(value)
      ? value.some(permission => permissions.includes(permission))
      : permissions.includes(value)

    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  } else {
    throw new Error('需要指定权限值')
  }
}

export default {
  install(app) {
    app.directive('permission', {
      mounted(el, binding) {
        checkPermission(el, binding)
      },
      updated(el, binding) {
        checkPermission(el, binding)
      }
    })
  }
} 