import request from '../utils/request';

// 获取所有API接口
export const getAllApiEndpoints = async () => {
  try {
    return await request({
      url: '/api/endpoints',
      method: 'get'
    });
  } catch (error) {
    console.error('获取API接口列表失败', error);
    throw error;
  }
};

// 按控制器分组获取API接口
export const getApiEndpointsByController = async () => {
  try {
    return await request({
      url: '/api/endpoints/by-controller',
      method: 'get'
    });
  } catch (error) {
    console.error('按控制器分组获取API接口失败', error);
    throw error;
  }
};

// 获取所有控制器名称
export const getAllControllerNames = async () => {
  try {
    return await request({
      url: '/api/endpoints/controllers',
      method: 'get'
    });
  } catch (error) {
    console.error('获取控制器名称列表失败', error);
    throw error;
  }
};

// 根据控制器名称获取API接口
export const getApiEndpointsByControllerName = async (controllerName: string) => {
  try {
    return await request({
      url: `/api/endpoints/by-controller/${controllerName}`,
      method: 'get'
    });
  } catch (error) {
    console.error('根据控制器名称获取API接口失败', error);
    throw error;
  }
};

// 获取API访问统计信息
export const getApiAccessStats = async () => {
  try {
    return await request({
      url: '/api/endpoints/stats',
      method: 'get'
    });
  } catch (error) {
    console.error('获取API访问统计信息失败', error);
    throw error;
  }
}; 