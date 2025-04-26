// services/auth.js
const { post, get } = require('../utils/request');
const app = getApp();

/**
 * 用户登录
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise} 返回Promise对象
 */
const login = (username, password) => {
  return post('/auth/login', { username, password })
    .then(res => {
      if (res.token && res.user) {
        app.login(res.token, res.user);
        return {
          success: true,
          message: '登录成功',
          user: res.user
        };
      }
      return res;
    })
    .catch(err => {
      console.error('登录失败:', err);
      return {
        success: false,
        message: err.message || '登录失败，请检查网络连接'
      };
    });
};

/**
 * 用户注册
 * @param {Object} userData 用户数据
 * @returns {Promise} 返回Promise对象
 */
const register = (userData) => {
  return post('/auth/register', userData)
    .then(res => {
      return {
        success: true,
        message: '注册成功',
        user: res.user
      };
    })
    .catch(err => {
      console.error('注册失败:', err);
      return {
        success: false,
        message: err.message || '注册失败，请稍后再试'
      };
    });
};

/**
 * 获取当前用户信息
 * @returns {Promise} 返回Promise对象
 */
const getUserInfo = () => {
  return get('/auth/info')
    .then(res => {
      // 更新全局用户信息
      if (res.user) {
        app.globalData.userInfo = res.user;
        wx.setStorageSync('userInfo', res.user);
      }
      return res;
    });
};

/**
 * 修改密码
 * @param {string} currentPassword 当前密码
 * @param {string} newPassword 新密码
 * @returns {Promise} 返回Promise对象
 */
const changePassword = (currentPassword, newPassword) => {
  return post('/auth/change-password', {
    currentPassword,
    newPassword
  })
    .then(res => {
      return {
        success: true,
        message: res.message || '密码修改成功'
      };
    })
    .catch(err => {
      console.error('修改密码失败:', err);
      throw {
        success: false,
        message: err.message || '修改密码失败，请检查当前密码是否正确'
      };
    });
};

/**
 * 退出登录
 * @param {Function} callback 成功退出后的回调函数
 * @returns {void}
 */
const logout = (callback) => {
  // 向服务器发送登出请求（可选）
  // 注意：这里不需要等待服务器响应，因为JWT认证方式下，服务器端不需要维护会话状态
  
  // 清除本地存储
  app.logout();
  
  // 执行回调函数
  if (typeof callback === 'function') {
    callback();
  }
};

module.exports = {
  login,
  register,
  getUserInfo,
  changePassword,
  logout
}; 