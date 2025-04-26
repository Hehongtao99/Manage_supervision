const app = getApp();
const parentService = require('../../services/parent');

Page({
  data: {
    userInfo: null,
    children: [],
    notifications: [],
    isLoading: true,
    app: getApp(),
    timestamp: new Date().getTime(), // 添加时间戳避免缓存
    loadRetries: 0, // 加载重试次数
    apiBaseUrl: '',  // 存储API基础URL
    hasError: false, // 添加错误标记
    initialized: false // 标记是否初始化完成
  },

  onLoad: function() {
    console.log('home页面加载');
    
    // 强制初始加载内容，避免白屏
    this.setData({
      apiBaseUrl: app.globalData.apiBaseUrl,
      isLoading: true,
      hasError: false
    });
    
    // 立即检查登录状态并初始化页面框架
    this.initPageFramework();
  },
  
  // 初始化页面框架，不加载具体数据
  initPageFramework: function() {
    console.log('初始化页面框架');
    
    // 检查是否已登录
    if (!app.globalData.hasLogin || !app.globalData.userInfo) {
      console.log('未登录或用户信息不存在，重定向到登录页');
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
    
    // 确保存在API基础URL
    const apiBaseUrl = app.globalData.apiBaseUrl || 'http://localhost:8081/api';
    
    // 只设置用户信息，先展示页面框架
    this.setData({
      userInfo: app.globalData.userInfo,
      timestamp: new Date().getTime(),
      apiBaseUrl: apiBaseUrl,
      isLoading: false, // 立即隐藏全局loading
      initialized: true
    });
    
    console.log('页面框架初始化完成，显示基础内容');
    
    // 然后在短暂延迟后开始加载数据
    setTimeout(() => {
      this.loadHomeData();
    }, 300);
  },
  
  // 检查登录状态并加载数据
  checkLoginAndLoadData: function() {
    console.log('检查登录状态', app.globalData.hasLogin, '用户信息:', app.globalData.userInfo);
    
    // 检查是否已登录
    if (!app.globalData.hasLogin || !app.globalData.userInfo) {
      console.log('未登录或用户信息不存在，重定向到登录页');
      
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
    
    // 确保存在API基础URL
    const apiBaseUrl = app.globalData.apiBaseUrl || 'http://localhost:8081/api';
    console.log('使用API基础URL:', apiBaseUrl);
    
    // 设置用户信息 - 即使没有其他数据，至少确保用户信息显示
    this.setData({
      userInfo: app.globalData.userInfo,
      timestamp: new Date().getTime(), // 更新时间戳
      apiBaseUrl: apiBaseUrl // 更新API基础URL
    });
    
    console.log('设置用户信息成功:', this.data.userInfo, '头像:', this.data.userInfo.avatar);
    
    // 加载首页数据
    this.loadHomeData();
  },
  
  onShow: function() {
    console.log('home页面显示');
    
    // 每次显示页面时，先检查登录状态
    if (!app.globalData.hasLogin || !app.globalData.userInfo) {
      console.log('未登录或用户信息不存在，重定向到登录页');
      
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
    
    // 确保存在API基础URL
    const apiBaseUrl = app.globalData.apiBaseUrl || 'http://localhost:8081/api';
    
    // 如果apiBaseUrl发生变化，更新它
    if (this.data.apiBaseUrl !== apiBaseUrl) {
      console.log('更新API基础URL:', apiBaseUrl);
      this.setData({
        apiBaseUrl: apiBaseUrl
      });
    }
    
    // 已登录，更新数据
    if (app.globalData.hasLogin) {
      // 更新时间戳，强制刷新图片
      this.setData({
        timestamp: new Date().getTime(),
        userInfo: app.globalData.userInfo
      });
      
      console.log('页面显示时更新用户信息:', this.data.userInfo, '头像:', this.data.userInfo.avatar);
      
      // 如果页面还没有初始化，执行初始化流程
      if (!this.data.initialized) {
        this.initPageFramework();
      } else {
        // 否则，刷新数据但不显示全屏loading
        this.refreshDataWithoutLoading();
      }
    }
  },
  
  // 刷新数据但不显示全屏loading
  refreshDataWithoutLoading: function() {
    console.log('静默刷新数据');
    
    // 获取子女列表
    parentService.getChildren()
      .then(res => {
        console.log('子女列表加载成功', res);
        let children = res.relations || [];
        
        // 过滤掉状态为pending的孩子
        children = children.filter(child => {
          return child.status !== 'pending';
        });
        
        // 修复子女头像URL
        if (Array.isArray(children)) {
          children.forEach(child => {
            if (child.childAvatar) {
              // 使用全局方法修复头像URL
              child.childAvatar = app.fixAvatarUrl(child.childAvatar);
            }
          });
        }
        
        this.setData({
          children: children
        });
      })
      .catch(err => {
        console.error('获取子女列表失败', err);
      });
    
    // 获取通知
    parentService.getNotifications()
      .then(res => {
        console.log('通知列表加载成功', res);
        // 只显示最近5条通知
        const allNotifications = res.data || [];
        const recentNotifications = allNotifications.slice(0, 5); // 限制为最新的5条
        
        this.setData({
          notifications: recentNotifications
        });
      })
      .catch(err => {
        console.error('获取通知失败', err);
      });
  },
  
  // 处理头像加载错误
  handleImageError: function(e) {
    console.error('头像加载失败:', e);
    
    if (!this.data.userInfo) {
      console.error('用户信息不存在，无法处理头像错误');
      return;
    }
    
    console.error('当前用户头像URL:', this.data.userInfo.avatar);
    
    // 尝试修复头像URL
    try {
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
      } else if (this.data.userInfo.avatar && !this.data.userInfo.avatar.startsWith('http')) {
        // 如果不是以http开头，可能需要添加baseUrl
        const fullUrl = this.data.apiBaseUrl + (this.data.userInfo.avatar.startsWith('/') ? '' : '/') + this.data.userInfo.avatar;
        console.log('尝试拼接完整的头像URL:', fullUrl);
        
        // 更新头像URL
        this.setData({
          'userInfo.avatar': fullUrl,
          timestamp: new Date().getTime() // 更新时间戳
        });
        
        // 更新全局数据
        app.globalData.userInfo.avatar = fullUrl;
        wx.setStorageSync('userInfo', app.globalData.userInfo);
      } else if (!this.data.userInfo.avatar) {
        // 如果头像为空，使用默认头像
        console.log('头像URL为空，使用默认头像');
        this.setData({
          'userInfo.avatar': '/assets/images/default-avatar.png',
          timestamp: new Date().getTime() // 更新时间戳
        });
      }
    } catch (err) {
      console.error('处理头像错误时发生异常:', err);
    }
  },
  
  // 处理子女头像加载错误
  handleChildImageError: function(e) {
    const index = e.currentTarget.dataset.index;
    
    if (!this.data.children || !this.data.children[index]) {
      console.error('子女数据不存在，索引:', index);
      return;
    }
    
    const child = this.data.children[index];
    
    if (child && child.childAvatar) {
      console.error('子女头像加载失败:', child.childAvatar);
      
      try {
        // 如果头像URL以/api/开头，尝试修复路径
        if (child.childAvatar.startsWith('/api/')) {
          const fixedUrl = app.fixAvatarUrl(child.childAvatar);
          console.log('尝试使用修复后的子女头像URL:', fixedUrl);
          
          // 更新子女头像URL
          const children = this.data.children;
          children[index].childAvatar = fixedUrl;
          
          this.setData({
            children: children,
            timestamp: new Date().getTime() // 更新时间戳
          });
        } else if (!child.childAvatar.startsWith('http')) {
          // 如果不是以http开头，可能需要添加baseUrl
          const fullUrl = this.data.apiBaseUrl + (child.childAvatar.startsWith('/') ? '' : '/') + child.childAvatar;
          console.log('尝试拼接完整的子女头像URL:', fullUrl);
          
          // 更新子女头像URL
          const children = this.data.children;
          children[index].childAvatar = fullUrl;
          
          this.setData({
            children: children,
            timestamp: new Date().getTime() // 更新时间戳
          });
        }
      } catch (err) {
        console.error('处理子女头像错误时发生异常:', err);
      }
    }
  },
  
  // 加载首页数据
  loadHomeData: function() {
    console.log('开始加载首页数据');
    
    // 局部loading而非全屏loading
    const sectionLoading = {
      childrenLoading: true,
      notificationsLoading: true
    };
    
    this.setData(sectionLoading);
    
    // 分别加载每个部分的数据，互不影响
    
    // 1. 获取子女列表
    parentService.getChildren()
      .then(res => {
        console.log('子女列表加载成功', res);
        let children = res.relations || [];
        
        // 过滤掉状态为pending的孩子
        children = children.filter(child => {
          return child.status !== 'pending';
        });
        
        // 修复子女头像URL
        if (Array.isArray(children)) {
          children.forEach(child => {
            if (child.childAvatar) {
              // 使用全局方法修复头像URL
              child.childAvatar = app.fixAvatarUrl(child.childAvatar);
            }
          });
        }
        
        this.setData({
          children: children,
          childrenLoading: false
        });
      })
      .catch(err => {
        console.error('获取子女列表失败', err);
        this.setData({
          childrenLoading: false
        });
      });
    
    // 2. 获取通知
    parentService.getNotifications()
      .then(res => {
        console.log('通知列表加载成功', res);
        // 只显示最近5条通知
        const allNotifications = res.data || [];
        const recentNotifications = allNotifications.slice(0, 5); // 限制为最新的5条
        
        this.setData({
          notifications: recentNotifications,
          notificationsLoading: false
        });
      })
      .catch(err => {
        console.error('获取通知失败', err);
        this.setData({
          notificationsLoading: false
        });
      });
      
    // 设置一个超时定时器，确保不会永远显示加载中
    setTimeout(() => {
      if (this.data.childrenLoading || this.data.notificationsLoading) {
        console.log('部分数据加载超时，强制完成加载');
        this.setData({ 
          childrenLoading: false,
          notificationsLoading: false,
          hasError: true
        });
      }
    }, 5000); // 5秒超时
  },
  
  // 跳转到子女详情页
  goToChildDetail: function(e) {
    const childId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/app/pages/children/detail?id=${childId}`
    });
  },
  
  // 跳转到子女列表页
  navigateToChildren: function() {
    wx.switchTab({
      url: '/app/pages/children/children'
    });
  },
  
  // 跳转到添加子女页
  navigateToAddChild: function() {
    wx.navigateTo({
      url: '/app/pages/children/relate'
    });
  },
  
  // 查看通知详情
  viewNotification: function(e) {
    const notificationId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `../notifications/detail?id=${notificationId}`
    });
  },
  
  // 跳转到通知列表页
  navigateToNotifications: function() {
    wx.switchTab({
      url: '../notifications/index'
    });
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadHomeData();
    wx.stopPullDownRefresh();
  }
}); 