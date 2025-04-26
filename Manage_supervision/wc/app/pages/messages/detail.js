const app = getApp();
const messageService = require('../../services/message');
const moment = require('../../utils/moment.min.js');

Page({
  data: {
    messageId: null,
    message: null,
    isLoading: true
  },

  onLoad: function(options) {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
    
    // 获取留言ID
    if (options.id) {
      this.setData({
        messageId: options.id
      });
      
      // 加载留言详情
      this.loadMessageDetail();
    } else {
      wx.showToast({
        title: '无效的留言ID',
        icon: 'none'
      });
      
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },
  
  // 加载留言详情
  loadMessageDetail: function() {
    const { messageId } = this.data;
    
    this.setData({ isLoading: true });
    
    messageService.getMessageDetails(messageId)
      .then(res => {
        console.log('留言详情响应数据:', res);
        
        let message = res;
        if (res.data) {
          message = res.data;
        }
        
        // 确保字段存在，防止null显示
        message.teacherName = message.teacherName || '老师';
        message.studentName = message.studentName || '我的孩子';
        message.content = message.content || '';
        
        // 修复教师头像URL
        if (message.teacherAvatar) {
          message.teacherAvatar = app.fixAvatarUrl(message.teacherAvatar);
        }
        
        // 格式化时间
        if (message.createTime) {
          message.createTime = moment(message.createTime).format('YYYY-MM-DD HH:mm');
        }
        
        if (message.replyTime) {
          message.replyTime = moment(message.replyTime).format('YYYY-MM-DD HH:mm');
        }
        
        this.setData({
          message,
          isLoading: false
        });
      })
      .catch(err => {
        console.error('获取留言详情失败', err);
        this.setData({ isLoading: false });
        
        wx.showToast({
          title: '获取留言详情失败',
          icon: 'none'
        });
        
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      });
  },
  
  // 返回上一页
  navigateBack: function() {
    wx.navigateBack();
  }
}); 