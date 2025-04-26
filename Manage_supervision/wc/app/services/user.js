const { post, get, put } = require('../utils/request');

/**
 * 更新用户个人资料
 * @param {Object} profileData 个人资料数据
 * @returns {Promise} 返回Promise对象
 */
const updateProfile = (profileData) => {
  return put('/user/profile', profileData);
};

/**
 * 获取用户详细信息
 * @returns {Promise} 返回Promise对象
 */
const getUserDetail = () => {
  return get('/user/detail');
};

/**
 * 上传用户头像
 * @param {String} filePath 文件路径
 * @returns {Promise} 返回Promise对象
 */
const uploadAvatar = (filePath) => {
  // 文件上传需要使用wx.uploadFile，这里返回一个Promise包装
  return new Promise((resolve, reject) => {
    const app = getApp();
    
    // 显示加载提示
    wx.showLoading({
      title: '上传中...',
    });
    
    wx.uploadFile({
      url: `${app.globalData.apiBaseUrl}/user/avatar`,
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${app.globalData.token}`
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          
          // 处理头像URL，确保格式正确
          if (data.url && data.url.startsWith('/api/')) {
            console.log('头像URL处理前:', data.url);
            
            // 修改头像URL格式，去掉/api/前缀，以便在小程序中正确显示
            // 注意：这个修改很重要！后端返回的URL是以/api/开头，
            // 但我们保存到小程序用户信息中的URL不应该包含/api/前缀，
            // 因为在模板中显示时，会根据是否以/api/开头来决定如何拼接完整URL
            const fileName = data.url.substring(data.url.lastIndexOf('/') + 1);
            data.url = `/file/uploads/${fileName}`; // 修改为不含/api/的格式
            console.log('头像URL处理后:', data.url);
          }
          
          resolve(data);
        } catch (e) {
          wx.hideLoading(); // 确保在发生错误时隐藏加载提示
          reject(new Error('解析服务器响应失败'));
        }
      },
      fail: (err) => {
        wx.hideLoading(); // 确保在请求失败时隐藏加载提示
        reject(err);
      },
      complete: () => {
        // 确保请求完成后隐藏加载提示
        wx.hideLoading();
      }
    });
  });
};

module.exports = {
  updateProfile,
  getUserDetail,
  uploadAvatar
}; 