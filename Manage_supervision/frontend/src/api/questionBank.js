import axios from 'axios';

const API_URL = '/api/question-banks';

class QuestionBankService {
  // 创建题库
  createQuestionBank(questionBank) {
    return axios.post(API_URL, questionBank);
  }

  // 更新题库
  updateQuestionBank(id, questionBank) {
    return axios.put(`${API_URL}/${id}`, questionBank);
  }

  // 删除题库
  deleteQuestionBank(id) {
    return axios.delete(`${API_URL}/${id}`);
  }

  // 获取题库详情
  getQuestionBank(id) {
    return axios.get(`${API_URL}/${id}`);
  }

  // 获取教师创建的所有题库
  getQuestionBanksByCreator(creatorId, status = 'ACTIVE', page = 0, size = 10) {
    return axios.get(`${API_URL}/creator/${creatorId}`, {
      params: {
        status,
        page,
        size
      }
    });
  }

  // 搜索题库
  searchQuestionBanks(keyword, creatorId, status = 'ACTIVE', page = 0, size = 10) {
    return axios.get(`${API_URL}/search`, {
      params: {
        keyword,
        creatorId,
        status,
        page,
        size
      }
    });
  }

  // 归档题库
  archiveQuestionBank(id) {
    return axios.put(`${API_URL}/${id}/archive`);
  }

  // 激活题库
  activateQuestionBank(id) {
    return axios.put(`${API_URL}/${id}/activate`);
  }

  // 获取题库中的题目数量
  getQuestionCountInBank(id) {
    return axios.get(`${API_URL}/${id}/question-count`);
  }
}

export default new QuestionBankService(); 