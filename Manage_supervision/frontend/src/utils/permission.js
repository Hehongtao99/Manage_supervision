/**
 * 权限工具函数
 * 用于根据用户权限动态生成菜单
 */

/**
 * 根据权限过滤菜单
 * @param {Array} menus 原始菜单配置
 * @param {Array} permissions 用户权限列表
 * @returns {Array} 过滤后的菜单
 */
export function filterMenusByPermission(menus, permissions) {
  if (!menus || !permissions) {
    return [];
  }

  return menus.filter(menu => {
    // 如果菜单没有设置权限，则认为不需要权限控制
    if (!menu.permission) {
      return true;
    }

    // 检查用户是否有权限访问此菜单
    const hasPermission = permissions.includes(menu.permission);
    
    // 递归处理子菜单
    if (hasPermission && menu.children && menu.children.length > 0) {
      const filteredChildren = filterMenusByPermission(menu.children, permissions);
      menu.children = filteredChildren;
    }
    
    return hasPermission;
  });
}

/**
 * 检查用户是否有某个权限
 * @param {string|Array} permission 权限标识或权限标识数组
 * @param {Array} permissions 用户权限列表
 * @param {boolean} requireAll 是否需要满足所有权限，默认为false(满足任一权限即可)
 * @returns {boolean} 是否有权限
 */
export function hasPermission(permission, permissions, requireAll = false) {
  if (!permission || !permissions) {
    return false;
  }

  if (Array.isArray(permission)) {
    if (requireAll) {
      // 需要满足所有权限
      return permission.every(p => permissions.includes(p));
    } else {
      // 满足任一权限即可
      return permission.some(p => permissions.includes(p));
    }
  }

  return permissions.includes(permission);
}

/**
 * 获取菜单和按钮权限
 * @param {Array} permissions 用户权限列表
 * @returns {Object} 菜单权限和按钮权限
 */
export function splitPermissions(permissions) {
  if (!permissions) {
    return {
      menus: [],
      buttons: []
    };
  }

  // 菜单权限不包含":"，按钮权限包含":"
  const menus = permissions.filter(p => !p.includes(':'));
  const buttons = permissions.filter(p => p.includes(':'));

  return {
    menus,
    buttons
  };
}

/**
 * 获取第一个有效的菜单路由
 * @param {Array} menus 菜单列表
 * @returns {string} 第一个有效的菜单路由
 */
export function getFirstValidRoute(menus) {
  if (!menus || menus.length === 0) {
    return '/';
  }

  for (const menu of menus) {
    if (menu.path) {
      return menu.path;
    }
    if (menu.children && menu.children.length > 0) {
      const childRoute = getFirstValidRoute(menu.children);
      if (childRoute !== '/') {
        return childRoute;
      }
    }
  }

  return '/';
} 