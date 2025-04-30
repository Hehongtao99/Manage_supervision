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
      let processedList = attendanceData.map(item => {
        // 统一日期格式
        const date = item.date || 
                     (item.checkInTime ? item.checkInTime.split(' ')[0] : '') || 
                     (item.createTime ? item.createTime.split(' ')[0] : '');
        
        // 简化状态为打卡和未打卡
        let status = '';
        
        // 输出原始状态值用于调试
        console.log('原始考勤状态:', item.status, '原始记录:', item);
        
        // 任何有记录的状态都视为已打卡
        if (item.status === '正常' || 
            item.status === 'present' || 
            item.status === 'normal' || 
            item.status === '迟到' || 
            item.status === 'late' || 
            item.status === '早退' || 
            item.status === 'early' ||
            item.faceRecognized === true ||
            (item.recognitionDetails && item.recognitionDetails.includes('正常出勤'))) {
          status = 'present';
        } else {
          status = 'absent'; // 未打卡
        }
        
        // 添加周几
        const dateObj = new Date(date);
        const weekDay = ['日', '一', '二', '三', '四', '五', '六'][dateObj.getDay()];
        
        // 优化时间显示格式 - 只显示时:分
        let timeStr = '';
        if (item.time) {
          // 如果已有时间，只保留时:分
          timeStr = item.time.split(':').slice(0, 2).join(':');
        } else if (item.checkInTime) {
          // 从checkInTime中提取时间
          const timeParts = item.checkInTime.split(' ');
          if (timeParts.length > 1) {
            timeStr = timeParts[1].split(':').slice(0, 2).join(':');
          }
        } else {
          // 默认时间
          timeStr = '08:00';
        }
        
        return {
          id: item.id || '',
          date: date,
          weekDay: weekDay,
          status: status,
          time: timeStr,
          remark: status === 'present' ? '已打卡' : '未打卡',
          // 添加年月日字段用于筛选
          year: dateObj.getFullYear(),
          month: dateObj.getMonth() + 1,
          day: dateObj.getDate()
        };
      });
      
      // 筛选当前选择月份的记录
      const selectedStartDate = new Date(this.data.dateRange.startDate);
      const selectedYear = selectedStartDate.getFullYear();
      const selectedMonth = selectedStartDate.getMonth() + 1;
      
      console.log('筛选条件 - 年:', selectedYear, '月:', selectedMonth);
      
      // 筛选出当前选定月份的记录
      processedList = processedList.filter(item => {
        console.log('记录日期 - 年:', item.year, '月:', item.month);
        return item.year === selectedYear && item.month === selectedMonth;
      });
      
      console.log('筛选后的记录数量:', processedList.length);
      
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
    
    // 获取当前选定月份
    const selectedStartDate = new Date(this.data.dateRange.startDate);
    const selectedYear = selectedStartDate.getFullYear();
    const selectedMonth = selectedStartDate.getMonth();
    
    // 生成当前选定月份的模拟数据
    const daysInMonth = new Date(selectedYear, selectedMonth + 1, 0).getDate();
    
    for (let i = 1; i <= Math.min(daysInMonth, 10); i++) {
      const date = new Date(selectedYear, selectedMonth, i);
      
      const dateStr = date.getFullYear() + '-' + 
                    formatNumber(date.getMonth() + 1) + '-' + 
                    formatNumber(date.getDate());
      
      const weekDay = ['日', '一', '二', '三', '四', '五', '六'][date.getDay()];
      
      // 随机状态（打卡/未打卡）
      const status = Math.random() > 0.2 ? 'present' : 'absent';
      
      // 生成随机时间（只有时:分）
      const hour = formatNumber(Math.floor(Math.random() * 2) + 8); // 8-9点
      const minute = formatNumber(Math.floor(Math.random() * 60));
      const timeStr = status === 'present' ? `${hour}:${minute}` : '';
      
      mockData.push({
        id: i.toString(),
        date: dateStr,
        weekDay: weekDay,
        status: status,
        time: timeStr,
        remark: status === 'present' ? '已打卡' : '未打卡',
        year: selectedYear,
        month: selectedMonth + 1,
        day: i
      });
    }
    
    return mockData;
  },
  
  // 计算考勤汇总数据
  calculateSummary: function(attendanceList) {
    let present = 0;
    let absent = 0;
    
    // 输出所有记录的状态，用于调试
    console.log('计算考勤汇总，记录数量:', attendanceList.length);
    attendanceList.forEach((item, index) => {
      console.log(`记录${index+1}状态:`, item.status);
    });
    
    attendanceList.forEach(item => {
      if (item.status === 'present') {
        present++;
      } else {
        absent++;
      }
    });
    
    const total = present + absent;
    const presentRate = total > 0 ? Math.round(present / total * 100) : 0;
    
    console.log('统计结果 - 已打卡:', present, '未打卡:', absent, '出勤率:', presentRate + '%');
    
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
    
    // 从选择的日期提取月份信息，更新currentMonth
    const selectedStartDate = new Date(selectedRange[0]);
    const selectedYear = selectedStartDate.getFullYear();
    const selectedMonth = selectedStartDate.getMonth() + 1;
    
    this.setData({
      currentMonth: selectedYear + '年' + formatNumber(selectedMonth) + '月'
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