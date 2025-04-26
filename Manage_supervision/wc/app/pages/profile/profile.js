const app = getApp();
const authService = require('../../services/auth');

Page({
  data: {
    userInfo: null,
    isLoading: true,
    version: '1.0.0',
    app: getApp(),
    timestamp: new Date().getTime(), // 添加时间戳避免缓存
    logoutModalVisible: false
  },

  onLoad: function() {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    // 获取用户数据
    this.setData({
      userInfo: app.globalData.userInfo,
      isLoading: false,
      timestamp: new Date().getTime() // 更新时间戳
    });
  },
  
  onShow: function() {
    if (app.globalData.hasLogin) {
      // 刷新时间戳，强制重新加载图片
      this.setData({
        timestamp: new Date().getTime(),
        userInfo: app.globalData.userInfo
      });
      
      // 每次显示页面时主动更新一次用户信息
      this.refreshUserInfo();
    }
  },
  
  // 处理头像加载错误
  handleImageError: function(e) {
    console.error('头像加载失败:', this.data.userInfo.avatar);
    
    // 如果头像URL以/api/开头，尝试修复路径
    if (this.data.userInfo.avatar && this.data.userInfo.avatar.startsWith('/api/')) {
      const fixedUrl = app.fixAvatarUrl(this.data.userInfo.avatar);
      console.log('尝试使用修复后的头像URL:', fixedUrl);
      
      // 更新头像URL
      this.setData({
        'userInfo.avatar': fixedUrl,
        timestamp: new Date().getTime() // 更新时间戳
      });
      
      // 更新全局数据
      app.globalData.userInfo.avatar = fixedUrl;
      wx.setStorageSync('userInfo', app.globalData.userInfo);
    }
  },
  
  // 刷新用户信息
  refreshUserInfo: function() {
    wx.showNavigationBarLoading();
    
    authService.getUserInfo()
      .then(res => {
        if (res.user) {
          app.globalData.userInfo = res.user;
          this.setData({
            userInfo: res.user
          });
        }
      })
      .catch(err => {
        console.error('获取用户信息失败', err);
      })
      .finally(() => {
        wx.hideNavigationBarLoading();
      });
  },
  
  // 修改密码
  changePassword: function() {
    wx.navigateTo({
      url: './change-password'
    });
  },
  
  // 编辑资料
  editProfile: function() {
    wx.navigateTo({
      url: './edit'
    });
  },
  
  // 显示退出登录模态框
  showLogoutModal: function() {
    this.setData({
      logoutModalVisible: true
    });
  },
  
  // 隐藏退出登录模态框
  hideLogoutModal: function() {
    this.setData({
      logoutModalVisible: false
    });
  },
  
  // 退出登录
  logout: function() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      confirmColor: '#FF4D4F',
      success: (res) => {
        if (res.confirm) {
          // 显示加载中提示
          wx.showLoading({
            title: '退出中...',
            mask: true
          });
          
          // 调用登出服务
          authService.logout(() => {
            wx.hideLoading();
            
            // 跳转到登录页
            wx.reLaunch({
              url: '/app/pages/login/login'
            });
          });
        }
      }
    });
  }
}); 