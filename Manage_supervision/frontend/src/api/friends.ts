import axios from '../utils/axios';

/**
 * 获取好友列表
 */
export const getFriends = () => {
  return axios.get('/api/friends');
};

/**
 * 搜索用户
 */
export const searchUsers = (keyword: string) => {
  return axios.get('/api/friends/search', {
    params: {
      keyword
    }
  });
};

/**
 * 发送好友请求
 */
export const sendFriendRequest = (toUserId: number, message: string) => {
  return axios.post('/api/friends/requests', {
    toUserId,
    message
  });
};

/**
 * 处理好友请求
 */
export const processFriendRequest = (requestId: number, accept: boolean) => {
  return axios.put(`/api/friends/requests/${requestId}`, null, {
    params: {
      accept
    }
  });
};

/**
 * 获取收到的好友请求
 */
export const getReceivedFriendRequests = () => {
  return axios.get('/api/friends/requests/received');
};

/**
 * 获取发送的好友请求
 */
export const getSentFriendRequests = () => {
  return axios.get('/api/friends/requests/sent');
};

/**
 * 删除好友
 */
export const deleteFriend = (friendId: number) => {
  return axios.delete(`/api/friends/${friendId}`);
}; 