const app = getApp();
const parentService = require('../../services/parent');
const config = require('../../config/config');

Page({
  data: {
    children: [],
    isLoading: true,
    relationTypes: config.relationTypes,
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
    
    // 加载子女列表
    this.loadChildren();
  },
  
  onShow: function() {
    // 每次显示页面时重新加载数据
    if (app.globalData.hasLogin) {
      this.loadChildren();
    }
  },
  
  // 加载子女列表
  loadChildren: function() {
    this.setData({ isLoading: true });
    
    parentService.getChildren()
      .then(res => {
        console.log('获取到的子女列表数据:', res);
        
        // 确保每个孩子对象都有有效的status字段
        const relations = res.relations || [];
        relations.forEach(child => {
          // 如果status为null、undefined或空字符串，设置为'active'
          if (!child.status) {
            child.status = 'active';
          }
          // 记录状态信息以便调试
          console.log(`孩子 ${child.childName} 的状态:`, child.status);
        });
        
        this.setData({
          children: relations,
          isLoading: false
        });
      })
      .catch(err => {
        console.error('获取子女列表失败', err);
        this.setData({ isLoading: false });
        wx.showToast({
          title: '获取数据失败',
          icon: 'none'
        });
      });
  },
  
  // 跳转到子女详情页
  goToChildDetail: function(e) {
    const childId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/app/pages/children/detail?id=${childId}`
    });
  },
  
  // 关联子女账号
  relateChild: function() {
    wx.navigateTo({
      url: '/app/pages/children/relate'
    });
  },
  
  // 解除与子女的关联
  unrelateChild: function(e) {
    const relationId = e.currentTarget.dataset.id;
    const childName = e.currentTarget.dataset.name;
    const status = e.currentTarget.dataset.status || 'active';
    
    const isPending = status === 'pending';
    const title = isPending ? '取消申请' : '确认解除关联';
    const content = isPending 
      ? `确定要取消与 ${childName} 的关联申请吗？` 
      : `确定要解除与 ${childName} 的关联吗？`;
    
    wx.showModal({
      title: title,
      content: content,
      confirmColor: '#f44336',
      success: (res) => {
        if (res.confirm) {
          parentService.removeRelation(relationId)
            .then(() => {
              wx.showToast({
                title: isPending ? '已取消申请' : '解除关联成功',
                icon: 'success'
              });
              // 重新加载子女列表
              this.loadChildren();
            })
            .catch(err => {
              console.error('操作失败', err);
              wx.showToast({
                title: err.message || (isPending ? '取消申请失败' : '解除关联失败'),
                icon: 'none'
              });
            });
        }
      }
    });
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadChildren();
    wx.stopPullDownRefresh();
  }
}); 