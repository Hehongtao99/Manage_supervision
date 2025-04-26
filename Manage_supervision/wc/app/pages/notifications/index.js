const app = getApp();
const parentService = require('../../services/parent');

Page({
  data: {
    notifications: [],
    isLoading: true,
    app: getApp()
  },

  onLoad: function() {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }

    // 加载所有通知
    this.loadNotifications();
  },
  
  onShow: function() {
    // 每次显示页面时重新加载数据
    if (app.globalData.hasLogin) {
      this.loadNotifications();
    }
  },
  
  // 加载所有通知
  loadNotifications: function() {
    this.setData({ isLoading: true });
    
    parentService.getNotifications()
      .then(res => {
        this.setData({
          notifications: res.data || [],
          isLoading: false
        });
      })
      .catch(err => {
        console.error('获取通知失败', err);
        this.setData({ 
          isLoading: false 
        });
        wx.showToast({
          title: '获取通知失败',
          icon: 'none'
        });
      });
  },
  
  // 查看通知详情
  viewNotificationDetail: function(e) {
    const notificationId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `./detail?id=${notificationId}`
    });
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadNotifications();
    wx.stopPullDownRefresh();
  }
}); 