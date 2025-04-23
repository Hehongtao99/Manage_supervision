import { defineStore } from 'pinia'
import axios from '../utils/axios'
import router from '../router'
import type { UpdateProfileRequest, UpdatePasswordRequest } from '../types/user'

export interface UserInfo {
  id: number;
  username: string;
  name: string;
  role: string;
  avatar?: string;
  email?: string;
}

interface UserState {
  user: {
    id: number | null
    name: string
    username?: string
    email: string
    roles: string[]
    realName?: string
    nickname?: string
    phone?: string
    bio?: string
    avatar?: string
    userNumber?: string
  }
  token: string | null
  initialized: boolean
  error: string | null
  userInfo: UserInfo | null
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token'),
    user: {
      id: null,
      name: '',
      username: '',
      email: '',
      roles: [],
      userNumber: ''
    },
    initialized: false,
    error: null,
    userInfo: null,
    permissions: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.user.id,
    isAdmin: (state) => {
      console.log('检查管理员权限，当前角色:', state.user.roles)
      if (state.user.roles.includes('ADMIN')) {
        return true
      }
      return false
    },
    isSupervisor: (state) => {
      console.log('检查教师权限，当前角色:', state.user.roles)
      return state.user.roles.some((role: string) => 
        role === 'SUPERVISOR' || role === 'supervisor' || 
        role === 'TEACHER' || role === 'teacher'
      )
    },
    isStudent: (state) => {
      console.log('检查学生权限，当前角色:', state.user.roles)
      return state.user.roles.some((role: string) => 
        role === 'USER' || role === 'user'
      ) && !state.user.roles.some((role: string) => 
        role === 'ADMIN' || role === 'admin' || role === 'SUPERVISOR' || role === 'supervisor'
      )
    },
    getUserRole: (state) => {
      const role: string = state.user.roles[0]
      if (role === 'ADMIN') {
        return 'ADMIN'
      } else if (role === 'STAFF') {
        return 'STAFF'
      } else {
        return 'STUDENT'
      }
    },
    userRole(): string | null {
      return this.userInfo?.role || null;
    }
  },

  actions: {
    setAxiosAuthHeader() {
      if (this.token) {
        console.log('设置全局Authorization头:', this.token.substring(0, 10) + '...')
        axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
      } else {
        console.log('清除全局Authorization头')
        delete axios.defaults.headers.common['Authorization']
      }
    },

    async initializeAuth(): Promise<boolean> {
      if (this.initialized) return true

      const token = localStorage.getItem('token')
      if (token) {
        console.log('初始化认证，发现token:', token.substring(0, 10) + '...')
        this.token = token
        this.setAxiosAuthHeader()
        
        try {
          const success = await this.fetchUserInfo()
          if (success) {
            console.log('获取用户信息成功:', this.user)
            this.initialized = true
            return true
          } else {
            this.handleAuthError()
            this.initialized = true
            return false
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          this.handleAuthError()
          this.initialized = true
          return false
        }
      } else {
        console.log('初始化认证，未找到token')
      }
      
      this.initialized = true
      return false
    },

    handleAuthError() {
      console.log('处理认证错误，清除用户状态')
      this.token = null
      this.user = {
        id: null,
        name: '',
        email: '',
        roles: [],
        userNumber: ''
      }
      localStorage.removeItem('token')
      localStorage.removeItem('userRoles')
      this.setAxiosAuthHeader()
      
      if (router.currentRoute.value.meta.requiresAuth) {
        router.push('/login')
      }
    },

    async login(username: string, password: string) {
      try {
        this.error = null
        console.log('开始登录请求，用户名:', username)
        
        const response = await axios.post('/api/auth/login', {
          username,
          password
        })
        
        console.log('登录响应数据:', JSON.stringify(response.data))
        
        // 添加数据验证
        if (!response.data) {
          throw new Error('服务器返回数据格式错误')
        }
        
        if (!response.data.token) {
          throw new Error('服务器返回数据缺少token')
        }
        
        this.token = response.data.token
        
        // 验证用户数据
        if (!response.data.user) {
          throw new Error('服务器返回数据缺少用户信息')
        }
        
        this.user = {
          id: response.data.user.id || null,
          name: response.data.user.realName || response.data.user.username || '',
          username: response.data.user.username || '',
          email: response.data.user.email || '',
          roles: Array.isArray(response.data.user.roles) ? response.data.user.roles : [],
          realName: response.data.user.realName,
          nickname: response.data.user.nickname,
          phone: response.data.user.phone,
          bio: response.data.user.bio,
          avatar: response.data.user.avatar || '',
          userNumber: response.data.user.userNumber || ''
        }
        
        // 同时更新userInfo
        this.userInfo = {
          id: response.data.user.id || 0,
          username: response.data.user.username || '',
          name: response.data.user.realName || response.data.user.username || '',
          role: Array.isArray(response.data.user.roles) && response.data.user.roles.length > 0 
            ? response.data.user.roles[0] 
            : 'USER',
          avatar: response.data.user.avatar || '',
          email: response.data.user.email || ''
        }
        
        localStorage.setItem('token', response.data.token)
        // 保存用户角色到localStorage
        localStorage.setItem('userRoles', JSON.stringify(this.user.roles))
        // 保存用户ID到localStorage，用于API请求
        localStorage.setItem('userId', this.user.id?.toString() || '')
        // 保存userStore到localStorage
        localStorage.setItem('userStore', JSON.stringify({
          user: this.user,
          userInfo: this.userInfo
        }))
        this.setAxiosAuthHeader()
        
        console.log('登录成功，用户角色:', this.user.roles)
        
        this.redirectBasedOnRole()

        return true
      } catch (error: any) {
        console.error('Login failed:', error)
        console.error('Error details:', error.stack)
        
        if (error.response) {
          console.error('Response status:', error.response.status)
          console.error('Response data:', JSON.stringify(error.response.data))
        }
        
        if (error.response?.data?.message) {
          this.error = error.response.data.message
        } else if (error.message) {
          this.error = `登录失败: ${error.message}`
        } else {
          this.error = '登录失败，请稍后重试'
        }
        
        return false
      }
    },

    redirectBasedOnRole() {
      console.log('执行角色重定向，用户角色:', this.user.roles)
      if (this.isAdmin) {
        console.log('用户是管理员，重定向到管理员仪表盘')
        router.push('/admin/dashboard')
      } else if (this.isSupervisor) {
        console.log('用户是教师，重定向到教师控制台')
        router.push('/supervisor/students')
      } else {
        console.log('用户是普通用户，重定向到普通仪表盘')
        router.push('/dashboard')
      }
    },

    async register(username: string, password: string) {
      try {
        this.error = null
        await axios.post('/api/auth/register', {
          username,
          password
        })
        return true
      } catch (error: any) {
        console.error('Registration failed:', error)
        
        if (error.response?.data?.message) {
          this.error = error.response.data.message
        } else if (error.message) {
          this.error = `注册失败: ${error.message}`
        } else {
          this.error = '注册失败，请稍后重试'
        }
        
        return false
      }
    },

    async fetchUserInfo(forceRefresh = false) {
      if (!this.token) {
        console.log('获取用户信息失败: 未找到token')
        return false
      }
      
      try {
        console.log('开始获取用户信息...')
        const timestamp = forceRefresh ? `?_t=${Date.now()}` : ''
        const response = await axios.get(`/api/auth/info${timestamp}`)
        
        if (response.data && response.data.user) {
          // 确保所有用户数据被正确设置
          this.user = {
            ...this.user,
            id: response.data.user.id || this.user.id,
            name: response.data.user.realName || response.data.user.username || this.user.name,
            username: response.data.user.username || this.user.username,
            realName: response.data.user.realName || this.user.realName,
            nickname: response.data.user.nickname || this.user.nickname,
            email: response.data.user.email || this.user.email,
            phone: response.data.user.phone || this.user.phone,
            bio: response.data.user.bio || this.user.bio,
            avatar: response.data.user.avatar || this.user.avatar,
            roles: response.data.user.roles || this.user.roles,
            userNumber: response.data.user.userNumber || this.user.userNumber
          }
          
          this.userInfo = {
            id: response.data.user.id || 0,
            username: response.data.user.username || '',
            name: response.data.user.realName || response.data.user.username || '',
            role: Array.isArray(response.data.user.roles) && response.data.user.roles.length > 0 
              ? response.data.user.roles[0] 
              : 'USER',
            avatar: response.data.user.avatar || '',
            email: response.data.user.email || ''
          }
          
          // 保存用户角色到localStorage
          localStorage.setItem('userRoles', JSON.stringify(this.user.roles))
          // 保存用户ID到localStorage，用于API请求
          localStorage.setItem('userId', this.user.id?.toString() || '')
          // 保存userStore到localStorage
          localStorage.setItem('userStore', JSON.stringify({
            user: this.user,
            userInfo: this.userInfo
          }))
          
          console.log('用户信息获取成功:', this.user)
          console.log('用户角色:', this.user.roles)
          return true
        } else {
          console.error('服务器返回的用户数据格式不正确:', response.data)
          return false
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        return false
      }
    },
    
    async updateProfile(profileData: UpdateProfileRequest) {
      try {
        this.error = null
        console.log('开始更新个人信息:', JSON.stringify(profileData))
        
        const response = await axios.put('/api/user/profile', profileData)
        
        console.log('个人信息更新响应数据:', JSON.stringify(response.data))
        
        // 添加数据验证
        if (!response.data) {
          throw new Error('服务器返回数据格式错误')
        }
        
        if (response.data && response.data.user) {
          // 创建新的用户对象，确保深度合并
          const updatedUser = response.data.user
          
          // 更新用户信息，保留未更新的字段
          this.user = { 
            ...this.user,
            id: updatedUser.id || this.user.id,
            name: updatedUser.realName || updatedUser.username || this.user.name,
            email: updatedUser.email || this.user.email,
            realName: updatedUser.realName || this.user.realName,
            nickname: updatedUser.nickname || this.user.nickname,
            phone: updatedUser.phone || this.user.phone,
            bio: updatedUser.bio || this.user.bio,
            avatar: updatedUser.avatar || this.user.avatar,
            // 确保roles数组被正确处理
            roles: Array.isArray(updatedUser.roles) ? updatedUser.roles : this.user.roles
          }
          
          console.log('个人信息更新成功，更新后的用户信息:', JSON.stringify(this.user))
        }
        
        return true
      } catch (error: any) {
        console.error('Failed to update profile:', error)
        
        if (error.response?.data?.message) {
          this.error = error.response.data.message
        } else if (error.message) {
          this.error = `个人信息更新失败: ${error.message}`
        } else {
          this.error = '个人信息更新失败，请稍后重试'
        }
        
        return false
      }
    },
    
    async updateAvatar(avatarUrl: string): Promise<boolean> {
      try {
        console.log('开始更新头像URL:', avatarUrl)
        this.error = null
        
        if (!avatarUrl || typeof avatarUrl !== 'string') {
          console.error('头像URL格式错误:', avatarUrl)
          this.error = '头像URL格式错误'
          return false
        }
        
        // 更新用户头像
        this.user.avatar = avatarUrl
        
        // 可以选择调用后端API保存头像URL
        // 但通常这已经在上传时由服务器处理了
        console.log('头像URL更新成功:', avatarUrl)
        return true
      } catch (error: any) {
        console.error('Failed to update avatar:', error)
        
        if (error.message) {
          this.error = `头像更新失败: ${error.message}`
        } else {
          this.error = '头像更新失败，请稍后重试'
        }
        
        return false
      }
    },

    async changePassword(data: UpdatePasswordRequest) {
      try {
        await axios.post('/api/auth/change-password', data)
        return true
      } catch (error) {
        console.error('Failed to change password:', error)
        throw error
      }
    },

    logout() {
      // 移除token和用户信息
      this.token = null
      this.user = {
        id: null,
        name: '',
        email: '',
        roles: [],
        userNumber: ''
      }
      localStorage.removeItem('token')
      localStorage.removeItem('userRoles')
      localStorage.removeItem('userId')
      localStorage.removeItem('userStore')
      
      // 清除全局axios头
      this.setAxiosAuthHeader()
      // 同时清除全局axios实例的Authorization头
      axios.defaults.headers.common['Authorization'] = null
      delete axios.defaults.headers.common['Authorization']
      
      router.push('/login')
    },

    setToken(token: string) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo;
    },
    
    setPermissions(permissions: string[]) {
      this.permissions = permissions;
    },
    
    clearUserInfo() {
      this.token = null
      this.user = {
        id: null,
        name: '',
        email: '',
        roles: [],
        userNumber: ''
      }
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userRoles')
      localStorage.removeItem('userId')
      localStorage.removeItem('userStore')
      this.setAxiosAuthHeader()
    },
    
    // 直接设置用户头像URL
    setUserAvatar(avatarUrl: string) {
      if (!avatarUrl || typeof avatarUrl !== 'string') {
        console.error('头像URL格式错误:', avatarUrl);
        return;
      }
      
      // 更新用户头像
      this.user.avatar = avatarUrl;
      
      // 如果存在userInfo，也更新它
      if (this.userInfo) {
        this.userInfo.avatar = avatarUrl;
      }
      
      console.log('头像URL更新成功:', avatarUrl);
    }
  }
})