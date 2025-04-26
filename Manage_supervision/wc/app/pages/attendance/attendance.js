const app = getApp();
const parentService = require('../../services/parent');
const util = require('../../utils/util');

// 定义本地formatNumber函数，以防util模块的函数无法使用
const formatNumber = (n) => {
  n = n.toString();
  return n[1] ? n : '0' + n;
};

Page({
  data: {
    childId: null,
    childName: '',
    attendanceList: [],
    isLoading: true,
    currentDate: null,
    currentMonth: '',
    dateRange: {
      startDate: '',
      endDate: ''
    },
    summary: {
      present: 0,
      absent: 0,
      total: 0,
      presentRate: '0%'
    }
  },

  onLoad: function(options) {
    // 检查是否已登录
    if (!app.globalData.hasLogin) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    if (options.id) {
      // 获取当前日期
      const now = new Date();
      // 使用本地定义的formatNumber函数
      const currentDate = now.getFullYear() + '-' + formatNumber(now.getMonth() + 1) + '-' + formatNumber(now.getDate());
      const currentMonth = now.getFullYear() + '年' + formatNumber(now.getMonth() + 1) + '月';
      
      // 计算当月第一天和最后一天
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
      const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      
      const startDate = firstDay.getFullYear() + '-' + formatNumber(firstDay.getMonth() + 1) + '-' + formatNumber(firstDay.getDate());
      const endDate = lastDay.getFullYear() + '-' + formatNumber(lastDay.getMonth() + 1) + '-' + formatNumber(lastDay.getDate());
      
      this.setData({
        childId: options.id,
        childName: options.childName || '我的孩子',
        currentDate: currentDate,
        currentMonth: currentMonth,
        dateRange: {
          startDate: startDate,
          endDate: endDate
        }
      });
      
      // 加载考勤数据
      this.loadAttendanceData();
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },
  
  // 加载考勤数据
  loadAttendanceData: function() {
    this.setData({ isLoading: true });
    
    parentService.getChildAttendance(
      this.data.childId,
      this.data.dateRange.startDate,
      this.data.dateRange.endDate
    ).then(res => {
      let attendanceData = [];
      
      console.log('后端返回的考勤原始数据:', res);
      
      // 处理可能的不同数据结构
      if (Array.isArray(res)) {
        // 如果直接返回数组
        attendanceData = res;
      } else if (res.data && Array.isArray(res.data)) {
        // 如果数据在data字段中
        attendanceData = res.data;
      } else if (res.records && Array.isArray(res.records)) {
        // 如果数据在records字段中
        attendanceData = res.records;
      } else if (res.success && res.records && Array.isArray(res.records)) {
        // AttendanceController格式
        attendanceData = res.records;
      }
      
      // 处理数据，转换为统一格式（只有打卡和未打卡两种状态）
      const processedList = attendanceData.map(item => {
        // 统一日期格式
        const date = item.date || 
                     (item.checkInTime ? item.checkInTime.split(' ')[0] : '') || 
                     (item.createTime ? item.createTime.split(' ')[0] : '');
        
        // 简化状态为打卡和未打卡
        let status = '';
        // 任何有记录的状态都视为已打卡
        if (item.status === '正常' || item.status === 'present' || item.status === 'normal' || 
            item.status === '迟到' || item.status === 'late' || 
            item.status === '早退' || item.status === 'early') {
          status = 'present';
        } else {
          status = 'absent'; // 未打卡
        }
        
        // 添加周几
        const dateObj = new Date(date);
        const weekDay = ['日', '一', '二', '三', '四', '五', '六'][dateObj.getDay()];
        
        // 确保时间显示到秒
        let timeStr = '';
        if (item.time) {
          // 如果已有时间，确保有秒
          if (item.time.split(':').length === 2) {
            timeStr = item.time + ':00'; // 添加秒
          } else {
            timeStr = item.time;
          }
        } else if (item.checkInTime) {
          // 从checkInTime中提取完整时间（含秒）
          const timeParts = item.checkInTime.split(' ');
          if (timeParts.length > 1) {
            timeStr = timeParts[1];
            // 确保有秒
            if (timeStr.split(':').length === 2) {
              timeStr += ':00';
            }
          }
        } else {
          // 默认时间带秒
          timeStr = '08:00:00';
        }
        
        return {
          id: item.id || '',
          date: date,
          weekDay: weekDay,
          status: status,
          time: timeStr,
          remark: status === 'present' ? '已打卡' : '未打卡'
        };
      });
      
      this.setData({
        attendanceList: processedList,
        isLoading: false
      });
      
      this.calculateSummary(processedList);
    }).catch(err => {
      console.error('获取考勤数据失败', err);
      this.setData({ isLoading: false });
      
      // 使用模拟数据
      const mockData = this.generateMockData();
      this.setData({
        attendanceList: mockData
      });
      
      this.calculateSummary(mockData);
      
      wx.showToast({
        title: '获取数据失败',
        icon: 'none'
      });
    });
  },
  
  // 生成模拟数据（当API调用失败时使用）
  generateMockData: function() {
    const mockData = [];
    const now = new Date();
    
    for (let i = 0; i < 10; i++) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      const dateStr = date.getFullYear() + '-' + 
                     formatNumber(date.getMonth() + 1) + '-' + 
                     formatNumber(date.getDate());
      
      const weekDay = ['日', '一', '二', '三', '四', '五', '六'][date.getDay()];
      
      // 随机状态（打卡/未打卡）
      const status = Math.random() > 0.2 ? 'present' : 'absent';
      
      // 生成随机时间（含秒）
      const hour = formatNumber(Math.floor(Math.random() * 2) + 8); // 8-9点
      const minute = formatNumber(Math.floor(Math.random() * 60));
      const second = formatNumber(Math.floor(Math.random() * 60));
      const timeStr = status === 'present' ? `${hour}:${minute}:${second}` : '';
      
      mockData.push({
        id: i.toString(),
        date: dateStr,
        weekDay: weekDay,
        status: status,
        time: timeStr,
        remark: status === 'present' ? '已打卡' : '未打卡'
      });
    }
    
    return mockData;
  },
  
  // 计算考勤汇总数据
  calculateSummary: function(attendanceList) {
    let present = 0;
    let absent = 0;
    
    attendanceList.forEach(item => {
      if (item.status === 'present') {
        present++;
      } else {
        absent++;
      }
    });
    
    const total = present + absent;
    const presentRate = total > 0 ? Math.round(present / total * 100) : 0;
    
    this.setData({
      summary: {
        present: present,
        absent: absent,
        total: total,
        presentRate: presentRate + '%'
      }
    });
  },
  
  // 选择日期范围
  bindDateRangeChange: function(e) {
    const selectedRange = e.detail.value;
    this.setData({
      dateRange: {
        startDate: selectedRange[0],
        endDate: selectedRange[1]
      }
    });
    
    // 重新加载数据
    this.loadAttendanceData();
  },
  
  // 切换月份
  changeMonth: function(e) {
    const direction = e.currentTarget.dataset.direction;
    const currentDate = new Date(this.data.dateRange.startDate);
    let newMonth = currentDate.getMonth();
    let newYear = currentDate.getFullYear();
    
    if (direction === 'prev') {
      newMonth -= 1;
      if (newMonth < 0) {
        newMonth = 11;
        newYear -= 1;
      }
    } else {
      newMonth += 1;
      if (newMonth > 11) {
        newMonth = 0;
        newYear += 1;
      }
    }
    
    const firstDay = new Date(newYear, newMonth, 1);
    const lastDay = new Date(newYear, newMonth + 1, 0);
    
    const startDate = firstDay.getFullYear() + '-' + formatNumber(firstDay.getMonth() + 1) + '-' + formatNumber(firstDay.getDate());
    const endDate = lastDay.getFullYear() + '-' + formatNumber(lastDay.getMonth() + 1) + '-' + formatNumber(lastDay.getDate());
    
    this.setData({
      currentMonth: newYear + '年' + formatNumber(newMonth + 1) + '月',
      dateRange: {
        startDate: startDate,
        endDate: endDate
      }
    });
    
    // 重新加载数据
    this.loadAttendanceData();
  },
  
  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadAttendanceData();
    wx.stopPullDownRefresh();
  }
});