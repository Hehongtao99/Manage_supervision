<template>
  <div class="course-student-management">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <h2>我的学生管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名/学号"
              clearable
              class="search-input"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="students.length === 0" class="empty-container">
        <el-empty description="暂无学生数据" />
      </div>

      <div v-else class="students-container">
        <el-collapse accordion>
          <el-collapse-item v-for="student in filteredStudents" :key="student.id">
            <template #title>
              <div class="student-item-header">
                <el-avatar :src="student.avatar || '/avatar-placeholder.png'" :size="40"></el-avatar>
                <div class="student-info">
                  <div class="student-name">{{ student.realName || student.username }}</div>
                  <div class="student-number">学号: {{ student.userNumber || '暂无' }}</div>
                </div>
                <el-tag 
                  :type="student.status === 'active' ? 'success' : 'info'"
                  class="status-tag"
                >
                  {{ student.status === 'active' ? '在读' : '休学' }}
                </el-tag>
              </div>
            </template>

            <!-- 学生详细信息 -->
            <div class="student-detail">
              <div class="student-profile">
                <div class="profile-item">
                  <span class="label">姓名：</span>
                  <span>{{ student.realName || '暂无' }}</span>
                </div>
                <div class="profile-item">
                  <span class="label">学号：</span>
                  <span>{{ student.userNumber || '暂无' }}</span>
                </div>
                <div class="profile-item">
                  <span class="label">邮箱：</span>
                  <span>{{ student.email || '暂无' }}</span>
                </div>
                <div class="profile-item">
                  <span class="label">电话：</span>
                  <span>{{ student.phone || '暂无' }}</span>
                </div>
              </div>

              <!-- 学生课程信息 -->
              <div class="course-section">
                <h3>课程信息</h3>
                <el-table :data="student.courses" style="width: 100%">
                  <el-table-column prop="courseTitle" label="课程名称" min-width="180">
                    <template #default="scope">
                      <el-tooltip
                        :content="scope.row.courseTitle || '未知课程'"
                        placement="top"
                        :show-after="1000"
                      >
                        <span class="course-title">{{ scope.row.courseTitle || '未知课程' }}</span>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                  <el-table-column prop="courseSubject" label="学科" width="100">
                    <template #default="scope">
                      {{ scope.row.courseSubject || '未知' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="hours" label="课时" width="80"></el-table-column>
                  <el-table-column prop="price" label="单价" width="100">
                    <template #default="scope">
                      ¥{{ scope.row.price }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="totalAmount" label="总价" width="100">
                    <template #default="scope">
                      ¥{{ scope.row.totalAmount }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)">
                        {{ getStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import axios from 'axios';
import { orderStatusMap } from '../../api/order';

export default {
  name: 'CourseStudentManagement',
  components: {
    Search
  },
  setup() {
    const students = ref([]);
    const loading = ref(true);
    const searchKeyword = ref('');

    // 获取学生数据
    const fetchStudents = async () => {
      try {
        loading.value = true;
        const token = localStorage.getItem('token');
        const response = await axios.get('/api/supervisor/students/with-courses', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        students.value = response.data;
      } catch (error) {
        console.error('获取学生数据失败:', error);
        ElMessage.error('获取学生数据失败，请稍后再试');
      } finally {
        loading.value = false;
      }
    };

    // 根据关键词过滤学生
    const filteredStudents = computed(() => {
      if (!searchKeyword.value) {
        return students.value;
      }
      const keyword = searchKeyword.value.toLowerCase();
      return students.value.filter(student => 
        (student.realName && student.realName.toLowerCase().includes(keyword)) ||
        (student.username && student.username.toLowerCase().includes(keyword)) ||
        (student.userNumber && student.userNumber.toLowerCase().includes(keyword))
      );
    });

    // 格式化订单状态文本
    const getStatusText = (status) => {
      return orderStatusMap[status] || status;
    };

    // 获取订单状态对应的标签类型
    const getStatusType = (status) => {
      const typeMap = {
        'PENDING': 'success',
        'ACCEPTED': 'success',
        'REJECTED': 'danger',
        'CANCELED': 'info',
        'COMPLETED': 'success',
        'REFUND_PENDING': 'warning',
        'REFUND_REJECTED': 'danger',
        'APPEALING': 'warning',
        'APPEAL_APPROVED': 'success',
        'APPEAL_REJECTED': 'danger'
      };
      return typeMap[status] || '';
    };

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    // 处理搜索
    const handleSearch = () => {
      // 实时过滤，不需要额外实现
    };

    onMounted(() => {
      fetchStudents();
    });

    return {
      students,
      loading,
      searchKeyword,
      filteredStudents,
      handleSearch,
      getStatusText,
      getStatusType,
      formatDate
    };
  }
};
</script>

<style scoped>
.course-student-management {
  padding: 20px;
}

.page-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.search-input {
  width: 250px;
}

.loading-container,
.empty-container {
  padding: 40px 0;
  text-align: center;
}

.student-item-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.student-info {
  flex: 1;
}

.student-name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}

.student-number {
  font-size: 13px;
  color: #606266;
}

.status-tag {
  margin-left: auto;
}

.student-detail {
  padding: 15px 10px;
}

.student-profile {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}

.profile-item {
  display: flex;
  align-items: center;
}

.label {
  color: #606266;
  margin-right: 8px;
  min-width: 60px;
}

.course-section h3 {
  margin: 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 500;
  border-left: 3px solid #409EFF;
  padding-left: 10px;
}

.course-title {
  display: inline-block;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>