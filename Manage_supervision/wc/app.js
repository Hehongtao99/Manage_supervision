// app.js
App({
  globalData: {
    userInfo: null,
    token: '',
    apiBaseUrl: 'http://localhost:8081/api', // 修改为实际可访问的API地址
    hasLogin: false,
    isParent: false
  },
  
  onLaunch: function() {
    console.log('应用启动');
    
    // 从缓存中获取用户登录信息
    try {
      // 不再每次启动都清空缓存，而是检查缓存有效性
      const token = wx.getStorageSync('token');
      const userInfo = wx.getStorageSync('userInfo');
      const loginTime = wx.getStorageSync('loginTime');
      const now = new Date().getTime();
      
      // 检查缓存是否过期（超过12小时视为过期）
      const isCacheExpired = !loginTime || (now - loginTime > 12 * 60 * 60 * 1000);
      
      if (token && userInfo && !isCacheExpired) {
        console.log('从缓存中恢复登录状态');
        
        // 修复头像URL问题
        if (userInfo.avatar) {
          userInfo.avatar = this.fixAvatarUrl(userInfo.avatar);
          // 保存修复后的用户信息
          wx.setStorageSync('userInfo', userInfo);
        }
        
        this.globalData.token = token;
        this.globalData.userInfo = userInfo;
        this.globalData.hasLogin = true;
        
        // 判断用户是否有家长角色
        if (userInfo.roles && userInfo.roles.includes('PARENT')) {
          this.globalData.isParent = true;
        }
        
        // 校验token是否有效，使用延迟确保app初始化完成
        setTimeout(() => {
          this.checkToken();
        }, 300);
      } else {
        console.log('没有找到缓存的登录信息或缓存已过期，需要重新登录');
        // 清除缓存
        this.clearAppCache();
        // 确保全局状态是未登录
        this.globalData.token = '';
        this.globalData.userInfo = null;
        this.globalData.hasLogin = false;
        this.globalData.isParent = false;
      }
    } catch (e) {
      console.error('获取缓存的登录信息失败:', e);
      // 清除缓存
      this.clearAppCache();
    }
  },
  
  // 清除应用缓存
  clearAppCache: function() {
    try {
      // 清除具体的缓存项
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
      wx.removeStorageSync('relations');
      wx.removeStorageSync('lastRefreshTime');
      wx.removeStorageSync('unreadCount');
      wx.removeStorageSync('dashboardData');
      wx.removeStorageSync('childrenData');
      wx.removeStorageSync('notificationsData');
      wx.removeStorageSync('loginTime');
      
      console.log('缓存已清除');
    } catch (e) {
      console.error('清除缓存失败:', e);
    }
  },
  
  // 修复头像URL问题
  fixAvatarUrl: function(avatarUrl) {
    if (!avatarUrl) return '/assets/images/default-avatar.png';
    
    console.log('原始头像URL:', avatarUrl);
    
    // 如果已经是完整URL，直接返回
    if (avatarUrl.startsWith('http')) {
      return avatarUrl;
    }
    
    // 检查URL是否包含多个/api/前缀
    if (avatarUrl.includes('/api/api/')) {
      console.log('修复前的头像URL(多个api前缀):', avatarUrl);
      // 提取文件名
      const fileName = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
      // 创建正确格式的URL
      const fixedUrl = `/file/uploads/${fileName}`;
      console.log('修复后的头像URL:', fixedUrl);
      return fixedUrl;
    }
    
    // 如果URL以/api/开头，也需要修复
    if (avatarUrl.startsWith('/api/file/')) {
      console.log('修复前的头像URL(api/file前缀):', avatarUrl);
      // 移除/api/前缀
      const fixedUrl = avatarUrl.replace('/api/', '/');
      console.log('修复后的头像URL:', fixedUrl);
      return fixedUrl;
    }
    
    // 如果以/开头但不完整，拼接API基础URL
    if (avatarUrl.startsWith('/') && !avatarUrl.startsWith('/api/') && !avatarUrl.startsWith('/file/')) {
      const fixedUrl = this.globalData.apiBaseUrl + avatarUrl;
      console.log('修复前的头像URL(路径前缀):', avatarUrl);
      console.log('修复后的头像URL(完整URL):', fixedUrl);
      return fixedUrl;
    }
    
    return avatarUrl;
  },
  
  // 获取完整的资源URL
  getFullUrl: function(path) {
    if (!path) return '';
    
    // 如果已经是完整URL，直接返回
    if (path.startsWith('http')) {
      return path;
    }
    
    // 如果不是以/开头，添加/
    const normalizedPath = path.startsWith('/') ? path : '/' + path;
    
    // 返回完整URL
    return this.globalData.apiBaseUrl + normalizedPath;
  },
  
  // 检查token是否有效
  checkToken: function() {
    console.log('检查token有效性');
    
    if (!this.globalData.token) {
      console.error('没有token，无法验证');
      return;
    }
    
    wx.request({
      url: `${this.globalData.apiBaseUrl}/auth/info`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${this.globalData.token}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.user) {
          console.log('token有效，用户信息:', res.data.user);
          
          // 修复后端返回的用户信息中的头像URL
          if (res.data.user.avatar) {
            res.data.user.avatar = this.fixAvatarUrl(res.data.user.avatar);
            console.log('修复后的头像URL:', res.data.user.avatar);
          }
          
          // 更新用户信息
          this.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);
          
          // 判断用户是否有家长角色
          if (res.data.user.roles && res.data.user.roles.includes('PARENT')) {
            this.globalData.isParent = true;
          }
        } else {
          console.error('token无效，清除登录状态', res.statusCode);
          // token无效，清除登录状态
          this.logout(false); // 传入false参数，表示不自动跳转到登录页
        }
      },
      fail: (err) => {
        // 请求失败，可能是网络问题，不做处理
        console.error('验证token请求失败:', err);
      }
    });
  },
  
  // 登录成功后保存用户信息
  login: function(token, userInfo) {
    console.log('保存登录信息');
    
    // 确保之前的缓存已经清除干净
    this.clearAppCache();
    
    // 修复头像URL问题
    if (userInfo.avatar) {
      userInfo.avatar = this.fixAvatarUrl(userInfo.avatar);
      console.log('登录时修复头像URL:', userInfo.avatar);
    } else {
      // 如果没有头像，使用默认头像
      userInfo.avatar = '/assets/images/default-avatar.png';
    }
    
    // 确保用户有roles属性
    if (!userInfo.roles) {
      userInfo.roles = ['PARENT'];
    }
    
    // 先更新全局数据，确保后续请求能使用正确的token
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    this.globalData.hasLogin = true;
    
    // 判断用户是否有家长角色
    if (userInfo.roles && userInfo.roles.includes('PARENT')) {
      this.globalData.isParent = true;
    } else {
      // 如果没有家长角色，添加家长角色（临时修复）
      if (!userInfo.roles.includes('PARENT')) {
        userInfo.roles.push('PARENT');
        this.globalData.isParent = true;
      }
    }
    
    // 确保用户角色信息存在
    console.log('用户角色:', userInfo.roles);
    
    // 保存到本地存储
    try {
      wx.setStorageSync('token', token);
      wx.setStorageSync('userInfo', userInfo);
      
      // 添加登录时间戳，便于后续验证
      wx.setStorageSync('loginTime', new Date().getTime());
      
      console.log('登录信息已保存到本地存储');
    } catch (e) {
      console.error('保存登录信息到本地存储失败:', e);
    }
  },
  
  // 退出登录
  logout: function(redirect = true) {
    console.log('执行登出操作, 是否跳转:', redirect);
    
    // 备份当前页面路径，便于跳转后恢复
    let currentPages = getCurrentPages();
    let currentPage = currentPages[currentPages.length - 1];
    let currentPagePath = '';
    if (currentPage) {
      currentPagePath = '/' + currentPage.route;
      console.log('当前页面路径:', currentPagePath);
    }
    
    // 清除全局数据
    this.globalData.token = '';
    this.globalData.userInfo = null;
    this.globalData.hasLogin = false;
    this.globalData.isParent = false;
    
    // 清除本地存储
    this.clearAppCache();
    
    // 是否需要跳转到登录页
    if (redirect) {
      console.log('跳转到登录页');
      
      // 避免在登录页面上再次跳转到登录页
      if (currentPagePath.includes('/login/login')) {
        console.log('当前已在登录页，不再跳转');
        return;
      }
      
      // 如果在登录页执行logout，不进行跳转
      if (currentPagePath && currentPagePath.includes('/login')) {
        console.log('当前页面是登录相关页面，不进行跳转');
        return;
      }
      
      // 尝试使用不同的页面跳转方式，避免超时问题
      if (currentPagePath.includes('/home/home')) {
        // 如果当前在首页，使用redirectTo避免循环跳转
        wx.redirectTo({
          url: '/app/pages/login/login',
          fail: (err) => {
            console.error('redirectTo失败:', err);
            // 最后尝试使用reLaunch
            wx.reLaunch({
              url: '/app/pages/login/login'
            });
          }
        });
      } else {
        // 否则优先使用switchTab
        wx.switchTab({
          url: '/app/pages/login/login',
          fail: (err) => {
            console.error('switchTab失败:', err);
            // 如果失败，尝试使用redirectTo
            wx.redirectTo({
              url: '/app/pages/login/login',
              fail: (redirectErr) => {
                console.error('redirectTo也失败:', redirectErr);
                // 最后尝试使用reLaunch
                wx.reLaunch({
                  url: '/app/pages/login/login'
                });
              }
            });
          }
        });
      }
    }
  }
}); 