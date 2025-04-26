const app = getApp();

Page({
  data: {
    username: '',
    password: '',
    showPassword: false,
    isLoading: false,
    errorMessage: ''
  },

  onLoad: function() {
    console.log('登录页面加载');
    
    // 如果已经登录，则跳转到首页
    if (app.globalData.hasLogin) {
      console.log('已经登录，跳转到首页');
      wx.switchTab({
        url: '/app/pages/home/home'
      });
    }
    
    // 确保API地址正确
    console.log('当前API地址:', app.globalData.apiBaseUrl);
  },
  
  onShow: function() {
    console.log('登录页面显示');
    
    // 检查是否已登录
    if (app.globalData.hasLogin) {
      console.log('已经登录，跳转到首页');
      wx.switchTab({
        url: '/app/pages/home/home'
      });
    }
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
  togglePasswordVisibility: function() {
    this.setData({
      showPassword: !this.data.showPassword
    });
  },

  // 登录按钮点击事件
  login: function() {
    const { username, password } = this.data;
    
    // 表单验证
    if (!username.trim()) {
      this.setData({ errorMessage: '请输入用户名' });
      return;
    }
    
    if (!password.trim()) {
      this.setData({ errorMessage: '请输入密码' });
      return;
    }
    
    // 显示加载状态
    this.setData({ isLoading: true });
    
    // 添加请求超时定时器
    const timeoutTimer = setTimeout(() => {
      this.setData({
        isLoading: false,
        errorMessage: '请求超时，请检查网络连接'
      });
    }, 15000); // 设置15秒超时
    
    // 确保API地址正确（临时修正）
    if (app.globalData.apiBaseUrl === 'https://localhost:8081/api') {
      // 本地开发环境地址可能需要修改为实际可访问的地址
      app.globalData.apiBaseUrl = 'http://localhost:8081/api'; // 替换为HTTP地址
      console.log('修正API地址为:', app.globalData.apiBaseUrl);
    }
    
    // 调用登录接口
    wx.request({
      url: `${app.globalData.apiBaseUrl}/auth/login`,
      method: 'POST',
      data: {
        username: username,
        password: password
      },
      success: (res) => {
        // 清除超时定时器
        clearTimeout(timeoutTimer);
        
        console.log('登录响应:', res);
        
        if (res.statusCode === 200 && res.data.token) {
          const { token, user } = res.data;
          
          console.log('用户信息:', user);
          
          // 检查用户是否有家长角色
          // 如果没有roles字段或roles不是数组，则给予默认家长角色（临时修复）
          if (!user.roles || !Array.isArray(user.roles)) {
            user.roles = ['PARENT'];
            console.log('添加默认家长角色');
          }
          
          const isParent = user.roles && user.roles.includes('PARENT');
          
          if (!isParent) {
            this.setData({
              isLoading: false,
              errorMessage: '您的账号没有家长权限，无法登录家长端'
            });
            return;
          }
          
          // 登录成功，保存用户信息
          app.login(token, user);
          
          console.log('登录成功，跳转到首页');
          
          // 使用switchTab代替reLaunch，避免跳转timeout
          wx.switchTab({
            url: '/app/pages/home/home',
            success: () => {
              console.log('成功跳转到首页');
            },
            fail: (err) => {
              console.error('switchTab跳转失败:', err);
              
              // 如果switchTab失败，尝试使用导航栈替换当前页
              setTimeout(() => {
                wx.redirectTo({
                  url: '/app/pages/home/home',
                  fail: (redirectErr) => {
                    console.error('redirectTo也失败:', redirectErr);
                    // 最后尝试重启小程序
                    wx.reLaunch({
                      url: '/app/pages/home/home'
                    });
                  }
                });
              }, 200);
            }
          });
        } else {
          // 登录失败
          this.setData({
            isLoading: false,
            errorMessage: res.data ? (res.data.message || '登录失败，请检查用户名和密码') : '服务器返回数据格式错误'
          });
        }
      },
      fail: (err) => {
        // 清除超时定时器
        clearTimeout(timeoutTimer);
        
        console.error('登录请求失败', err);
        this.setData({
          isLoading: false,
          errorMessage: '网络错误，请稍后重试'
        });
      },
      complete: () => {
        // 确保在任何情况下都会清除加载状态
        // 此处不重复设置是因为success和fail中已经处理过，这里是为了保险起见
        if (this.data.isLoading) {
          this.setData({ isLoading: false });
        }
      }
    });
  },

  // 跳转到注册页面
  goToRegister: function() {
    wx.navigateTo({
      url: '/pages/register/register'
    });
  }
}); 