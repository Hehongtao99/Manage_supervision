// app.js
App({
  globalData: {
    userInfo: null,
    token: '',
    apiBaseUrl: 'http://localhost:8080/api', // 根据实际情况修改API地址
    hasLogin: false,
    isParent: false
  },
  
  onLaunch: function() {
    // 从缓存中获取用户登录信息
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.hasLogin = true;
      
      // 判断用户是否有家长角色
      if (userInfo.roles && userInfo.roles.includes('PARENT')) {
        this.globalData.isParent = true;
      }
      
      // 校验token是否有效
      this.checkToken();
    }
  },
  
  // 检查token是否有效
  checkToken: function() {
    wx.request({
      url: `${this.globalData.apiBaseUrl}/auth/info`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${this.globalData.token}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.user) {
          // 更新用户信息
          this.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);
          
          // 判断用户是否有家长角色
          if (res.data.user.roles && res.data.user.roles.includes('PARENT')) {
            this.globalData.isParent = true;
          }
        } else {
          // token无效，清除登录状态
          this.logout();
        }
      },
      fail: () => {
        // 请求失败，可能是网络问题，不做处理
      }
    });
  },
  
  // 登录成功后保存用户信息
  login: function(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    this.globalData.hasLogin = true;
    
    // 判断用户是否有家长角色
    if (userInfo.roles && userInfo.roles.includes('PARENT')) {
      this.globalData.isParent = true;
    }
    
    // 保存到本地存储
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },
  
  // 退出登录
  logout: function() {
    this.globalData.token = '';
    this.globalData.userInfo = null;
    this.globalData.hasLogin = false;
    this.globalData.isParent = false;
    
    // 清除本地存储
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    
    // 跳转到登录页
    wx.reLaunch({
      url: '/pages/login/login'
    });
  }
}); 