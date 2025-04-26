// app/pages/profile/edit.js
const app = getApp();
const userService = require('../../services/user');

Page({
  data: {
    userInfo: null,
    formData: {},
    isSaving: false,
    app: getApp(),
    timestamp: new Date().getTime() // 添加时间戳避免缓存
  },

  onLoad: function() {
    // 获取用户数据
    const userInfo = app.globalData.userInfo || {};
    this.setData({
      userInfo: userInfo,
      formData: {
        realName: userInfo.realName || '',
        phone: userInfo.phone || '',
        email: userInfo.email || '',
        address: userInfo.address || ''
      },
      timestamp: new Date().getTime() // 更新时间戳
    });
  },
  
  onShow: function() {
    // 每次页面显示时更新时间戳
    this.setData({
      timestamp: new Date().getTime()
    });
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

  // 处理输入变化
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { formData } = this.data;
    formData[field] = e.detail.value;
    this.setData({ formData });
  },

  // 选择头像
  chooseAvatar: function() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        
        // 预览显示
        this.setData({
          'userInfo.avatar': tempFilePath,
          'formData.avatarFile': tempFilePath
        });
        
        // 上传头像
        this.uploadAvatar(tempFilePath);
      }
    });
  },
  
  // 上传头像
  uploadAvatar: function(filePath) {
    // 使用服务层的uploadAvatar方法，它包含了showLoading和hideLoading
    userService.uploadAvatar(filePath)
      .then(data => {
        console.log('头像上传返回数据:', data);
        if (data.url) {
          // 保存头像URL
          let avatarUrl = data.url;
          
          // 服务层已经处理了URL格式，这里直接使用
          console.log('使用处理后的头像URL:', avatarUrl);
          
          // 更新本地显示
          this.setData({
            'formData.avatar': avatarUrl,
            'userInfo.avatar': avatarUrl,
            timestamp: new Date().getTime() // 更新时间戳，强制刷新图片
          });
          
          // 更新全局数据
          app.globalData.userInfo.avatar = avatarUrl;
          
          // 保存到本地存储
          wx.setStorageSync('userInfo', app.globalData.userInfo);
          
          wx.showToast({
            title: '头像上传成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: '头像上传失败',
            icon: 'none'
          });
        }
      })
      .catch(err => {
        console.error('头像上传失败', err);
        wx.showToast({
          title: '头像上传失败',
          icon: 'none'
        });
      });
  },
  
  // 保存个人资料
  saveProfile: function() {
    const { formData } = this.data;
    
    // 表单验证
    if (!formData.realName.trim()) {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none'
      });
      return;
    }
    
    // 显示加载状态
    this.setData({ isSaving: true });
    
    // 准备要提交的数据 - 仍然保留原有地址数据
    const profileData = {
      realName: formData.realName,
      phone: formData.phone,
      email: formData.email,
      address: formData.address || this.data.userInfo.address || '' // 使用原有地址数据
    };
    
    // 调用更新接口
    userService.updateProfile(profileData)
      .then(response => {
        this.setData({ isSaving: false });
        
        // 更新全局数据
        Object.assign(app.globalData.userInfo, profileData);
        
        // 更新本地存储
        wx.setStorageSync('userInfo', app.globalData.userInfo);
        
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });
        
        // 返回上一页
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch(error => {
        console.error('保存个人资料失败', error);
        this.setData({ isSaving: false });
        
        wx.showToast({
          title: error.message || '保存失败',
          icon: 'none'
        });
      });
  }
}); 