const app = getApp();
const parentService = require('../../services/parent');

Page({
  data: {
    notification: null,
    isLoading: true
  },

  onLoad: function(options) {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }

    // 获取通知ID
    const notificationId = options.id;
    if (notificationId) {
      this.loadNotificationDetail(notificationId);
    } else {
      wx.showToast({
        title: '无效的通知ID',
        icon: 'none'
      });
      this.setData({ isLoading: false });
    }
  },

  // 加载通知详情
  loadNotificationDetail: function(notificationId) {
    this.setData({ isLoading: true });
    
    parentService.getNotificationDetail(notificationId)
      .then(res => {
        this.setData({
          notification: res.data || {},
          isLoading: false
        });
        
        // 标记通知为已读
        if (res.data && !res.data.read) {
          this.markAsRead(notificationId);
        }
      })
      .catch(err => {
        console.error('获取通知详情失败', err);
        this.setData({ isLoading: false });
        wx.showToast({
          title: '获取通知详情失败',
          icon: 'none'
        });
      });
  },
  
  // 标记通知为已读
  markAsRead: function(notificationId) {
    parentService.markNotificationAsRead(notificationId)
      .then(() => {
        console.log('通知已标记为已读');
      })
      .catch(err => {
        console.error('标记通知已读失败', err);
      });
  },
  
  // 返回上一页
  navigateBack: function() {
    wx.navigateBack();
  }
}); 