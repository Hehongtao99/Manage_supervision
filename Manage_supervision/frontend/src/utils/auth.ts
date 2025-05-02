/**
 * 从本地存储获取令牌
 */
export const getToken = (): string | null => {
  return localStorage.getItem('token')
}

/**
 * 设置令牌到本地存储
 * @param token JWT令牌
 */
export const setToken = (token: string): void => {
  localStorage.setItem('token', token)
}

/**
 * 移除令牌
 */
export const removeToken = (): void => {
  localStorage.removeItem('token')
}

/**
 * 获取认证头部
 */
export const getAuthHeader = (): Record<string, string> => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

/**
 * 判断用户是否已登录
 */
export const isLoggedIn = (): boolean => {
  return !!getToken()
}

/**
 * 解析JWT令牌
 * @param token JWT令牌
 */
export const parseJwt = (token: string): any => {
  try {
    return JSON.parse(atob(token.split('.')[1]))
  } catch (e) {
    return null
  }
}

/**
 * 获取用户ID
 */
export const getUserId = (): number | null => {
  const token = getToken()
  if (!token) return null
  
  const parsed = parseJwt(token)
  return parsed ? parsed.userId : null
}

/**
 * 检查令牌是否过期
 */
export const isTokenExpired = (): boolean => {
  const token = getToken()
  if (!token) return true
  
  const parsed = parseJwt(token)
  if (!parsed || !parsed.exp) return true
  
  // exp是以秒为单位的时间戳
  return parsed.exp * 1000 < Date.now()
} 