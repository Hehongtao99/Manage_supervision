import axios from 'axios';

const API_URL = '/api/questions';

class QuestionService {
  // 创建问题
  createQuestion(question) {
    return axios.post(API_URL, question);
  }

  // 更新问题
  updateQuestion(id, question) {
    return axios.put(`${API_URL}/${id}`, question);
  }

  // 删除问题
  deleteQuestion(id) {
    return axios.delete(`${API_URL}/${id}`);
  }

  // 获取问题详情
  getQuestion(id) {
    return axios.get(`${API_URL}/${id}`);
  }

  // 获取教师创建的所有问题
  getQuestionsByCreator(creatorId, page = 0, size = 10) {
    return axios.get(`${API_URL}/creator/${creatorId}`, {
      params: {
        page,
        size
      }
    });
  }

  // 按类型获取教师创建的问题
  getQuestionsByType(type, creatorId, page = 0, size = 10) {
    return axios.get(`${API_URL}/type/${type}/creator/${creatorId}`, {
      params: {
        page,
        size
      }
    });
  }

  // 获取题库中的所有问题
  getQuestionsByBank(bankId, page = 0, size = 10) {
    return axios.get(`${API_URL}/bank/${bankId}`, {
      params: {
        page,
        size
      }
    });
  }

  // 按类型获取题库中的问题
  getQuestionsByBankAndType(bankId, type, page = 0, size = 10) {
    return axios.get(`${API_URL}/bank/${bankId}/type/${type}`, {
      params: {
        page,
        size
      }
    });
  }

  // 搜索问题
  searchQuestions(keyword, creatorId, page = 0, size = 10) {
    return axios.get(`${API_URL}/search`, {
      params: {
        keyword,
        creatorId,
        page,
        size
      }
    });
  }

  // 将问题添加到题库
  addQuestionToBank(questionId, bankId) {
    return axios.post(`${API_URL}/${questionId}/bank/${bankId}`);
  }

  // 从题库中移除问题
  removeQuestionFromBank(questionId, bankId) {
    return axios.delete(`${API_URL}/${questionId}/bank/${bankId}`);
  }

  // 获取问题所在的所有题库ID
  getQuestionBankIds(questionId) {
    return axios.get(`${API_URL}/${questionId}/banks`);
  }
}

export default new QuestionService(); 