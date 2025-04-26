const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    realName: '',
    showPassword: false,
    showConfirmPassword: false,
    isLoading: false,
    errorMessage: ''
  },

  // 输入框内容变化时触发
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [field]: e.detail.value,
      errorMessage: '' // 清除错误信息
    });
  },

  // 切换密码显示状态
  togglePasswordVisibility: function(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({
      [field]: !this.data[field]
    });
  },

  // 验证表单
  validateForm: function() {
    const { username, password, confirmPassword, phone, realName } = this.data;
    
    if (!username.trim()) {
      this.setData({ errorMessage: '请输入用户名' });
      return false;
    }
    
    if (username.length < 4) {
      this.setData({ errorMessage: '用户名至少需要4个字符' });
      return false;
    }
    
    if (!password.trim()) {
      this.setData({ errorMessage: '请输入密码' });
      return false;
    }
    
    if (password.length < 6) {
      this.setData({ errorMessage: '密码至少需要6个字符' });
      return false;
    }
    
    if (password !== confirmPassword) {
      this.setData({ errorMessage: '两次输入的密码不一致' });
      return false;
    }
    
    if (!phone.trim()) {
      this.setData({ errorMessage: '请输入手机号' });
      return false;
    }
    
    if (!/^1\d{10}$/.test(phone)) {
      this.setData({ errorMessage: '请输入有效的手机号码' });
      return false;
    }
    
    if (!realName.trim()) {
      this.setData({ errorMessage: '请输入真实姓名' });
      return false;
    }
    
    return true;
  },

  // 注册按钮点击事件
  register: function() {
    // 表单验证
    if (!this.validateForm()) {
      return;
    }
    
    // 显示加载状态
    this.setData({ isLoading: true });
    
    const { username, password, phone, realName } = this.data;
    
    // 调用注册接口
    wx.request({
      url: `${app.globalData.apiBaseUrl}/auth/register`,
      method: 'POST',
      data: {
        username: username,
        password: password,
        phone: phone,
        realName: realName,
        // 默认申请家长角色
        role: 'PARENT'
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.user) {
          // 注册成功
          wx.showToast({
            title: '注册成功',
            icon: 'success',
            duration: 2000
          });
          
          // 延迟跳转到登录页面
          setTimeout(() => {
            wx.navigateBack();
          }, 2000);
        } else {
          // 注册失败
          this.setData({
            isLoading: false,
            errorMessage: res.data.message || '注册失败，请稍后重试'
          });
        }
      },
      fail: (err) => {
        console.error('注册请求失败', err);
        this.setData({
          isLoading: false,
          errorMessage: '网络错误，请稍后重试'
        });
      }
    });
  },

  // 返回登录页面
  goToLogin: function() {
    wx.navigateBack();
  }
}); 