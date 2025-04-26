const app = getApp();
const parentService = require('../../services/parent');
const config = require('../../config/config');

Page({
  data: {
    keyword: '',
    searchResults: [],
    isLoading: false,
    selectedStudent: null,
    relationType: 'father', // 默认为父亲
    relationTypes: config.relationTypes || [
      { value: 'father', label: '父亲' },
      { value: 'mother', label: '母亲' },
      { value: 'guardian', label: '监护人' }
    ],
    errorMsg: ''
  },

  onLoad: function() {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
  },

  // 处理搜索关键字输入
  onKeywordInput: function(e) {
    this.setData({
      keyword: e.detail.value,
      errorMsg: ''
    });

    // 当输入至少2个字符时，自动开始搜索
    if (e.detail.value.length >= 2) {
      this.searchStudents();
    } else {
      this.setData({
        searchResults: []
      });
    }
  },

  // 搜索学生
  searchStudents: function() {
    const keyword = this.data.keyword.trim();
    if (keyword.length < 2) {
      this.setData({
        errorMsg: '请输入至少2个字符进行搜索',
        searchResults: []
      });
      return;
    }

    this.setData({ 
      isLoading: true,
      errorMsg: '' 
    });

    parentService.searchStudents(keyword)
      .then(res => {
        this.setData({
          searchResults: res.students || [],
          isLoading: false
        });
        
        // 如果没有搜索结果，显示提示
        if (res.students.length === 0) {
          this.setData({
            errorMsg: '未找到匹配的学生'
          });
        }
      })
      .catch(err => {
        console.error('搜索学生失败', err);
        this.setData({
          isLoading: false,
          errorMsg: err.message || '搜索失败，请重试'
        });
      });
  },

  // 选择学生
  selectStudent: function(e) {
    const studentId = e.currentTarget.dataset.id;
    const student = this.data.searchResults.find(s => s.id === studentId);
    
    this.setData({
      selectedStudent: student,
      errorMsg: ''
    });
  },

  // 选择关系类型
  onRelationTypeChange: function(e) {
    this.setData({
      relationType: e.detail.value
    });
  },

  // 确认关联
  confirmRelation: function() {
    if (!this.data.selectedStudent) {
      this.setData({
        errorMsg: '请先选择一个学生'
      });
      return;
    }

    this.setData({ 
      isLoading: true,
      errorMsg: '' 
    });

    const studentIdentifier = this.data.selectedStudent.userNumber || this.data.selectedStudent.username;
    console.log('关联学生:', studentIdentifier, '关系类型:', this.data.relationType);

    parentService.relateChild(studentIdentifier, this.data.relationType)
      .then(res => {
        console.log('关联结果:', res);
        this.setData({ isLoading: false });
        
        let toastTitle = '关联请求已发送';
        // 根据后端返回结果判断状态
        if (res.relation && res.relation.status) {
          console.log('关联状态:', res.relation.status);
          const status = res.relation.status;
          
          if (status === 'active' || status === 'confirmed') {
            toastTitle = '关联成功';
          } else if (status === 'pending') {
            toastTitle = '申请已发送，等待确认';
          }
        } else {
          console.log('关联响应没有状态信息');
        }
        
        wx.showToast({
          title: toastTitle,
          icon: 'success'
        });
        
        // 返回到子女列表页面
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      })
      .catch(err => {
        console.error('关联子女失败', err);
        this.setData({
          isLoading: false,
          errorMsg: err.message || '关联失败，请重试'
        });
      });
  },

  // 取消并返回
  cancelRelation: function() {
    wx.navigateBack();
  }
}); 