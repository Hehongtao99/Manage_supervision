const app = getApp();
const authService = require('../../services/auth');

Page({
  data: {
    formData: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    },
    showCurrentPassword: false,
    showNewPassword: false,
    showConfirmPassword: false,
    isChanging: false,
    // 密码强度评估
    passwordStrength: 0,
    strengthText: ''
  },
  
  onLoad: function() {
    // 检查登录状态
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
    }
  },

  // 处理输入变化
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { formData } = this.data;
    formData[field] = e.detail.value;
    this.setData({ formData });
    
    // 如果是新密码输入，评估密码强度
    if (field === 'newPassword') {
      this.evaluatePasswordStrength(e.detail.value);
    }
  },
  
  // 评估密码强度
  evaluatePasswordStrength: function(password) {
    if (!password) {
      this.setData({ 
        passwordStrength: 0,
        strengthText: ''
      });
      return;
    }
    
    let strength = 0;
    let strengthText = '';
    
    // 长度检查
    if (password.length >= 6) strength += 1;
    if (password.length >= 10) strength += 1;
    
    // 包含数字
    if (/\d/.test(password)) strength += 1;
    
    // 包含小写字母
    if (/[a-z]/.test(password)) strength += 1;
    
    // 包含大写字母
    if (/[A-Z]/.test(password)) strength += 1;
    
    // 包含特殊字符
    if (/[^a-zA-Z0-9]/.test(password)) strength += 1;
    
    // 确定强度级别
    if (strength <= 2) {
      strengthText = '弱';
    } else if (strength <= 4) {
      strengthText = '中';
    } else {
      strengthText = '强';
    }
    
    this.setData({
      passwordStrength: Math.min(Math.floor(strength * 100 / 6), 100),
      strengthText
    });
  },

  // 切换当前密码显示状态
  toggleCurrentPassword: function() {
    this.setData({
      showCurrentPassword: !this.data.showCurrentPassword
    });
  },

  // 切换新密码显示状态
  toggleNewPassword: function() {
    this.setData({
      showNewPassword: !this.data.showNewPassword
    });
  },

  // 切换确认密码显示状态
  toggleConfirmPassword: function() {
    this.setData({
      showConfirmPassword: !this.data.showConfirmPassword
    });
  },

  // 修改密码
  changePassword: function() {
    const { currentPassword, newPassword, confirmPassword } = this.data.formData;
    
    // 表单验证
    if (!currentPassword) {
      wx.showToast({
        title: '请输入当前密码',
        icon: 'none'
      });
      return;
    }
    
    if (!newPassword) {
      wx.showToast({
        title: '请输入新密码',
        icon: 'none'
      });
      return;
    }
    
    if (newPassword.length < 6) {
      wx.showToast({
        title: '新密码不能少于6位',
        icon: 'none'
      });
      return;
    }
    
    if (newPassword === currentPassword) {
      wx.showToast({
        title: '新密码不能与当前密码相同',
        icon: 'none'
      });
      return;
    }
    
    if (newPassword !== confirmPassword) {
      wx.showToast({
        title: '两次输入的密码不一致',
        icon: 'none'
      });
      return;
    }
    
    // 显示修改中
    this.setData({ isChanging: true });
    
    // 调用后端接口修改密码
    authService.changePassword(currentPassword, newPassword)
      .then(res => {
        console.log('修改密码成功:', res);
        
        wx.showToast({
          title: '密码修改成功',
          icon: 'success',
          duration: 1500
        });
        
        // 跳转到个人页面
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch(err => {
        console.error('修改密码失败:', err);
        
        wx.showToast({
          title: err.message || '修改失败，请检查当前密码是否正确',
          icon: 'none',
          duration: 2000
        });
      })
      .finally(() => {
        this.setData({ isChanging: false });
      });
  }
}); 