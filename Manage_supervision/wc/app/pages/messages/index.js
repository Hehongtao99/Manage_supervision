const app = getApp();
const parentService = require('../../services/parent');
const messageService = require('../../services/message');
const moment = require('../../utils/moment.min.js');

Page({
  data: {
    messages: [],
    children: [],
    childrenNames: [],
    selectedChildIndex: 0,
    teachers: [],
    teacherNames: [],
    selectedTeacherIndex: 0,
    messageContent: '',
    showNewMessageDialog: false,
    isLoading: true,
    page: 0,
    size: 10,
    hasMore: true,
    errorMessage: ''
  },

  onLoad: function() {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/app/pages/login/login'
      });
      return;
    }
    
    // 加载留言列表和子女信息
    this.loadData();
  },
  
  onShow: function() {
    // 每次显示页面时重新加载数据
    if (app.globalData.hasLogin) {
      this.loadData();
    }
  },
  
  // 加载留言列表和子女信息
  loadData: function() {
    this.setData({ isLoading: true });
    
    // 加载留言列表
    this.loadMessages();
    
    // 加载子女列表
    this.loadChildren();
  },
  
  // 加载留言列表
  loadMessages: function() {
    const { page, size } = this.data;
    
    messageService.getParentMessages(page, size)
      .then(res => {
        console.log('留言列表响应数据:', res);
        
        let messages = [];
        if (res.content && Array.isArray(res.content)) {
          // 标准分页格式
          messages = res.content;
          this.setData({
            hasMore: page < res.totalPages - 1
          });
        } else if (Array.isArray(res)) {
          // 直接返回数组格式
          messages = res;
          this.setData({
            hasMore: res.length === size
          });
        } else if (res.data) {
          if (res.data.content && Array.isArray(res.data.content)) {
            // 嵌套在data中的分页格式
            messages = res.data.content;
            this.setData({
              hasMore: page < res.data.totalPages - 1
            });
          } else if (Array.isArray(res.data)) {
            // 嵌套在data中的数组格式
            messages = res.data;
            this.setData({
              hasMore: res.data.length === size
            });
          }
        }
        
        // 格式化时间并处理null值
        messages.forEach(message => {
          // 确保字段有值，防止null显示
          message.teacherName = message.teacherName || '老师';
          message.studentName = message.studentName || '我的孩子';
          message.content = message.content || '';
          
          if (message.createTime) {
            message.createTime = moment(message.createTime).format('YYYY-MM-DD HH:mm');
          }
        });
        
        this.setData({
          messages: page === 0 ? messages : [...this.data.messages, ...messages],
          isLoading: false
        });
        
        // 缓存留言数据用于详情页面
        if (page === 0) {
          wx.setStorageSync('cached_messages', messages);
        } else if (messages.length > 0) {
          const cachedMessages = wx.getStorageSync('cached_messages') || [];
          wx.setStorageSync('cached_messages', [...cachedMessages, ...messages]);
        }
        
        // 标记所有回复为已读
        this.markAllRepliesAsRead();
      })
      .catch(err => {
        console.error('获取留言列表失败', err);
        this.setData({ isLoading: false });
        wx.showToast({
          title: '获取留言列表失败',
          icon: 'none'
        });
      });
  },
  
  // 加载子女列表
  loadChildren: function() {
    parentService.getChildren()
      .then(res => {
        console.log('获取子女列表响应:', res);
        let children = res.relations || [];
        
        if (children.length > 0) {
          // 检查子女对象是否包含教师信息
          console.log('第一个子女对象:', children[0]);
          
          const childrenNames = children.map(child => child.childName);
          
          this.setData({
            children,
            childrenNames
          });
        }
      })
      .catch(err => {
        console.error('获取子女列表失败', err);
      });
  },
  
  // 查看留言详情
  viewMessageDetail: function(e) {
    const messageId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/app/pages/messages/detail?id=${messageId}`
    });
  },
  
  // 打开新留言对话框
  openNewMessageDialog: function() {
    if (this.data.children.length === 0) {
      wx.showToast({
        title: '请先添加关联子女',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      showNewMessageDialog: true,
      selectedChildIndex: 0,
      teachers: [],
      teacherNames: [],
      selectedTeacherIndex: 0,
      messageContent: '',
      errorMessage: ''
    });
    
    // 加载第一个孩子的教师列表
    this.loadTeachersForSelectedChild();
  },
  
  // 为选中的孩子加载教师列表
  loadTeachersForSelectedChild: function() {
    const selectedChild = this.data.children[this.data.selectedChildIndex];
    if (!selectedChild) {
      this.setData({ 
        teachers: [],
        teacherNames: [],
        errorMessage: '无法获取孩子信息'
      });
      return;
    }
    
    this.setData({ errorMessage: '' });
    
    wx.showLoading({
      title: '加载教师...',
      mask: true
    });
    
    // 检查子女信息中是否直接包含教师信息
    if (selectedChild.teacherId && selectedChild.teacherName) {
      wx.hideLoading();
      // 直接使用子女对象中的教师信息
      const teacher = {
        id: selectedChild.teacherId,
        name: selectedChild.teacherName
      };
      
      this.setData({
        teachers: [teacher],
        teacherNames: [teacher.name],
        selectedTeacherIndex: 0,
        errorMessage: ''
      });
      return;
    }
    
    // 如果子女对象中没有教师信息，尝试从API获取
    parentService.getChildTeachers(selectedChild.childId)
      .then(res => {
        wx.hideLoading();
        console.log('教师列表响应:', res);
        
        let teachers = [];
        if (res.teachers && Array.isArray(res.teachers)) {
          teachers = res.teachers;
        } else if (Array.isArray(res)) {
          teachers = res;
        } else if (res.data && Array.isArray(res.data)) {
          teachers = res.data;
        }
        
        // 处理教师数据格式
        teachers = teachers.map(teacher => ({
          id: teacher.id,
          name: teacher.realName || teacher.username || '未命名教师'
        }));
        
        if (teachers.length > 0) {
          const teacherNames = teachers.map(teacher => teacher.name);
          this.setData({
            teachers,
            teacherNames,
            selectedTeacherIndex: 0
          });
        } else if (selectedChild.teacherId) {
          // 如果API没有返回教师列表但子女对象中有teacherId，创建一个简单的教师对象
          const fallbackTeacher = {
            id: selectedChild.teacherId,
            name: selectedChild.teacherName || '班主任'
          };
          
          this.setData({
            teachers: [fallbackTeacher],
            teacherNames: [fallbackTeacher.name],
            selectedTeacherIndex: 0
          });
        } else {
          this.setData({
            teachers: [],
            teacherNames: [],
            errorMessage: '该孩子没有关联教师'
          });
        }
      })
      .catch(err => {
        wx.hideLoading();
        console.error('获取教师列表失败', err);
        
        // 尝试使用回退方案：直接从子女信息获取教师
        if (selectedChild.teacherId) {
          const fallbackTeacher = {
            id: selectedChild.teacherId,
            name: selectedChild.teacherName || '班主任'
          };
          
          this.setData({
            teachers: [fallbackTeacher],
            teacherNames: [fallbackTeacher.name],
            selectedTeacherIndex: 0,
            errorMessage: ''
          });
        } else {
          this.setData({
            teachers: [],
            teacherNames: [],
            errorMessage: '无法获取教师信息'
          });
        }
      });
  },
  
  // 关闭对话框
  cancelDialog: function() {
    this.setData({
      showNewMessageDialog: false
    });
  },
  
  // 选择子女变化
  onChildChange: function(e) {
    this.setData({
      selectedChildIndex: e.detail.value,
      teachers: [],
      teacherNames: [],
      selectedTeacherIndex: 0
    });
    
    // 加载选中孩子的教师列表
    this.loadTeachersForSelectedChild();
  },
  
  // 选择教师变化
  onTeacherChange: function(e) {
    this.setData({
      selectedTeacherIndex: e.detail.value
    });
  },
  
  // 留言内容输入
  onContentInput: function(e) {
    this.setData({
      messageContent: e.detail.value
    });
  },
  
  // 发送留言
  sendMessage: function() {
    const { children, selectedChildIndex, teachers, selectedTeacherIndex, messageContent } = this.data;
    
    if (!messageContent.trim()) {
      this.setData({
        errorMessage: '请输入留言内容'
      });
      return;
    }
    
    const selectedChild = children[selectedChildIndex];
    if (!selectedChild) {
      this.setData({
        errorMessage: '请选择子女'
      });
      return;
    }
    
    const studentId = selectedChild.childId;
    if (!studentId) {
      this.setData({
        errorMessage: '无法获取学生信息'
      });
      return;
    }
    
    // 获取教师ID - 首先尝试从选择的教师中获取，如果没有则尝试从子女对象获取
    let teacherId = null;
    
    if (teachers.length > 0 && teachers[selectedTeacherIndex]) {
      teacherId = teachers[selectedTeacherIndex].id;
    } 
    
    // 如果没有获取到教师ID，尝试从子女信息中获取
    if (!teacherId && selectedChild.teacherId) {
      teacherId = selectedChild.teacherId;
      
      // 如果之前没有教师列表，创建一个临时的教师信息
      if (teachers.length === 0) {
        const tempTeacher = {
          id: selectedChild.teacherId,
          name: selectedChild.teacherName || '班主任'
        };
        
        this.setData({
          teachers: [tempTeacher],
          teacherNames: [tempTeacher.name],
          selectedTeacherIndex: 0
        });
      }
    }
    
    if (!teacherId) {
      this.setData({
        errorMessage: '无法获取教师信息'
      });
      return;
    }
    
    // 清除错误信息
    this.setData({
      errorMessage: ''
    });
    
    // 显示发送中提示
    wx.showLoading({
      title: '发送中...',
      mask: true
    });
    
    console.log('准备发送留言:', {
      teacherId: teacherId,
      studentId: studentId,
      content: messageContent
    });
    
    // 发送留言
    messageService.sendMessage(teacherId, studentId, messageContent)
      .then(res => {
        wx.hideLoading();
        console.log('留言发送成功响应:', res);
        
        wx.showToast({
          title: '留言发送成功',
          icon: 'success'
        });
        
        // 关闭对话框
        this.setData({
          showNewMessageDialog: false
        });
        
        // 重新加载留言列表
        this.setData({ page: 0 }, () => {
          this.loadMessages();
        });
      })
      .catch(err => {
        wx.hideLoading();
        console.error('发送留言失败', err);
        
        this.setData({
          errorMessage: err.message || '发送留言失败'
        });
      });
  },
  
  // 标记所有回复为已读
  markAllRepliesAsRead: function() {
    // 检查是否有未读回复
    const hasUnreadReplies = this.data.messages.some(message => 
      message.replyContent && !message.replyRead
    );
    
    if (hasUnreadReplies) {
      messageService.markAllRepliesAsRead()
        .then(res => {
          console.log('标记所有回复为已读成功', res);
          
          // 更新本地数据状态
          const messages = this.data.messages.map(message => {
            if (message.replyContent && !message.replyRead) {
              return { ...message, replyRead: true };
            }
            return message;
          });
          
          this.setData({ messages });
        })
        .catch(err => {
          console.error('标记所有回复为已读失败', err);
        });
    }
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.setData({ page: 0 }, () => {
      this.loadData();
      wx.stopPullDownRefresh();
    });
  },
  
  // 上拉加载更多
  onReachBottom: function() {
    if (this.data.hasMore && !this.data.isLoading) {
      this.setData({
        page: this.data.page + 1,
        isLoading: true
      }, () => {
        this.loadMessages();
      });
    }
  }
}); 