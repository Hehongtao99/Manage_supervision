const app = getApp();

/**
 * 封装微信请求
 * @param {Object} options 请求配置
 * @returns {Promise} 返回Promise对象
 */
const request = (options) => {
  // 获取全局配置
  const baseUrl = app.globalData.apiBaseUrl;
  const token = app.globalData.token;
  
  // 设置请求重试次数
  const maxRetries = options.maxRetries || 1;
  let currentRetry = 0;

  // 确保已经登录，除非请求明确不需要授权
  if (options.requiresAuth !== false && !token) {
    console.error('请求需要授权但未登录');
    return Promise.reject({ message: '未登录，请先登录', code: 401 });
  }

  // 创建实际的请求函数
  const executeRequest = () => {
    try {
      // 重新获取token，避免使用过期的token
      const currentToken = app.globalData.token;
      
      // 合并请求选项
      const requestOptions = {
        url: options.url.startsWith('http') ? options.url : `${baseUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data,
        header: options.header || {},
        timeout: 10000 // 设置10秒超时时间
      };

      // 如果是获取头像的请求，添加随机参数，避免缓存
      if (requestOptions.url.includes('/file/uploads/')) {
        if (requestOptions.url.includes('?')) {
          requestOptions.url += `&t=${new Date().getTime()}`;
        } else {
          requestOptions.url += `?t=${new Date().getTime()}`;
        }
      }

      // 如果存在token，添加到请求头
      if (currentToken) {
        requestOptions.header['Authorization'] = `Bearer ${currentToken}`;
      }
      
      // 添加默认的Content-Type
      if (requestOptions.method === 'POST' || requestOptions.method === 'PUT') {
        if (!requestOptions.header['Content-Type']) {
          requestOptions.header['Content-Type'] = 'application/json';
        }
      }

      console.log(`发送${requestOptions.method}请求:`, requestOptions.url);

      // 返回Promise
      return new Promise((resolve, reject) => {
        wx.request({
          ...requestOptions,
          success: (res) => {
            try {
              // 处理请求成功的情况
              if (res.statusCode >= 200 && res.statusCode < 300) {
                console.log(`请求成功:`, requestOptions.url);
                
                // 数据为空时，返回一个空对象而不是null
                if (!res.data) {
                  console.warn('响应数据为空，返回空对象');
                  resolve({});
                  return;
                }
                
                // 处理用户信息和头像URL
                if (res.data && res.data.user && res.data.user.avatar) {
                  res.data.user.avatar = app.fixAvatarUrl(res.data.user.avatar);
                }
                
                // 处理子女列表头像URL
                if (res.data && res.data.relations && Array.isArray(res.data.relations)) {
                  res.data.relations.forEach(item => {
                    if (item.childAvatar) {
                      item.childAvatar = app.fixAvatarUrl(item.childAvatar);
                    }
                  });
                }
                
                // 如果响应中包含token，更新全局token
                if (res.data && res.data.token) {
                  app.globalData.token = res.data.token;
                  wx.setStorageSync('token', res.data.token);
                }
                
                resolve(res.data);
              } else if (res.statusCode === 401) {
                // 未授权：token无效或已过期
                console.error('请求未授权 (401):', requestOptions.url);
                
                // 避免在登录页面调用logout造成循环
                if (!requestOptions.url.includes('/auth/login')) {
                  console.log('登录已过期，执行登出操作');
                  
                  // 延迟执行登出，避免在当前请求处理过程中再触发其他请求
                  setTimeout(() => {
                    app.logout(true);  // 确保跳转到登录页
                  }, 300);
                }
                
                reject({ message: '登录已过期，请重新登录', code: 401 });
              } else {
                // 其他错误
                console.error(`请求失败 (${res.statusCode}):`, requestOptions.url, res.data);
                
                // 处理特定的错误码
                if (res.statusCode === 404) {
                  reject({ message: '请求的资源不存在', code: 404 });
                } else if (res.statusCode >= 500) {
                  // 服务器错误可能需要重试
                  if (currentRetry < maxRetries) {
                    currentRetry++;
                    console.log(`服务器错误，第${currentRetry}次重试请求`);
                    
                    setTimeout(() => {
                      executeRequest().then(resolve).catch(reject);
                    }, 1000 * (currentRetry)); // 随着重试次数增加等待时间
                    return;
                  }
                  
                  reject({ message: '服务器内部错误', code: 500 });
                } else {
                  // 其他错误
                  reject(res.data || { message: `请求失败，状态码：${res.statusCode}`, code: res.statusCode });
                }
              }
            } catch (error) {
              console.error('处理响应数据时发生错误:', error);
              reject({ message: '处理响应数据时发生错误', code: -2 });
            }
          },
          fail: (err) => {
            // 网络错误等
            console.error('请求失败:', requestOptions.url, err);
            
            // 对网络错误进行重试
            if (currentRetry < maxRetries) {
              currentRetry++;
              console.log(`网络错误，第${currentRetry}次重试请求`);
              
              setTimeout(() => {
                executeRequest().then(resolve).catch(reject);
              }, 1000 * (currentRetry)); // 随着重试次数增加等待时间
              return;
            }
            
            reject({ message: '网络请求失败，请检查网络连接', code: -1 });
          }
        });
      });
    } catch (error) {
      console.error('请求发生异常:', error);
      return Promise.reject({ message: '请求发生异常', code: -3 });
    }
  };

  // 执行请求
  return executeRequest();
};

/**
 * GET请求
 * @param {string} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} header 请求头
 * @param {number} maxRetries 最大重试次数
 * @returns {Promise} 返回Promise对象
 */
const get = (url, data = {}, header = {}, maxRetries = 1) => {
  return request({
    url,
    data,
    header,
    method: 'GET',
    maxRetries
  });
};

/**
 * POST请求
 * @param {string} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} header 请求头
 * @param {number} maxRetries 最大重试次数
 * @returns {Promise} 返回Promise对象
 */
const post = (url, data = {}, header = {}, maxRetries = 1) => {
  return request({
    url,
    data,
    header,
    method: 'POST',
    maxRetries
  });
};

/**
 * PUT请求
 * @param {string} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} header 请求头
 * @param {number} maxRetries 最大重试次数
 * @returns {Promise} 返回Promise对象
 */
const put = (url, data = {}, header = {}, maxRetries = 1) => {
  return request({
    url,
    data,
    header,
    method: 'PUT',
    maxRetries
  });
};

/**
 * DELETE请求
 * @param {string} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} header 请求头
 * @param {number} maxRetries 最大重试次数
 * @returns {Promise} 返回Promise对象
 */
const del = (url, data = {}, header = {}, maxRetries = 1) => {
  return request({
    url,
    data,
    header,
    method: 'DELETE',
    maxRetries
  });
};

module.exports = {
  get,
  post,
  put,
  delete: del
}; 