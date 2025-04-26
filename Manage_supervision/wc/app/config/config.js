// config/config.js

module.exports = {
  // API配置
  api: {
    baseUrl: 'http://localhost:8080/api', // 接口基础地址，根据实际环境修改
    timeout: 10000, // 请求超时时间
  },
  
  // 权限配置
  permissions: {
    parent: ['VIEW_CHILD_INFO', 'RELATE_CHILD', 'VIEW_CHILD_COURSE', 'VIEW_CHILD_TASK'],
  },
  
  // 关系类型
  relationTypes: [
    { value: 'father', label: '父亲' },
    { value: 'mother', label: '母亲' },
    { value: 'guardian', label: '监护人' }
  ],
  
  // 默认头像
  defaultAvatar: '/assets/images/default-avatar.png',
}; 