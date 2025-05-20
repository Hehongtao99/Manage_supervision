<template>
  <div class="runner-management-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>跑步爱好者管理</span>
          <div class="search-container">
            <el-input
              v-model="searchQuery"
              placeholder="搜索跑步爱好者..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="filteredRunners"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="姓名" width="140">
          <template #default="{ row }">
            <div class="name-cell">
              <el-avatar :size="32" :src="row.avatar">
                {{ (row.realName || row.name || '').substring(0, 1) }}
              </el-avatar>
              <span class="user-name">{{ row.realName || row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userNumber" label="编号" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              @click="viewRunnerDetails(row)"
            >
              详情
            </el-button>
            <el-button 
              size="small"
              :type="row.status === 'active' ? 'warning' : 'success'"
              @click="toggleRunnerStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteRunner(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRunners"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 跑步爱好者详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="跑步爱好者详情"
      width="60%"
      destroy-on-close
      class="runner-detail-dialog"
    >
      <div v-if="currentRunner" class="runner-info-container">
        <div class="runner-header">
          <div class="avatar-container">
            <el-avatar :size="80" :src="currentRunner.avatar">
              {{ (currentRunner.realName || currentRunner.name || currentRunner.username || '').substring(0, 1) }}
            </el-avatar>
          </div>
          <div class="runner-main-info">
            <h2>{{ currentRunner.realName || currentRunner.name || currentRunner.username }}</h2>
            <div class="runner-id">编号: {{ currentRunner.userNumber }}</div>
          </div>
        </div>
        
        <!-- 跑步爱好者基本信息部分 - 所有权限用户都可查看 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentRunner.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentRunner.realName }}</el-descriptions-item>
          <el-descriptions-item label="编号">{{ currentRunner.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentRunner.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentRunner.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentRunner.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRunner.status === 'active' ? 'success' : 'warning'">
              {{ currentRunner.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRunner.createTime || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="2">
            <el-tag 
              v-for="(role, index) in currentRunner.roles" 
              :key="index" 
              type="info" 
              class="role-tag"
            >
              {{ role }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentRunner.bio || '暂无个人简介' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="toggleRunnerStatus(currentRunner)"
          >
            {{ currentRunner && currentRunner.status === 'active' ? '禁用此跑步爱好者' : '启用此跑步爱好者' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import axios from '@/utils/axios';
import { useRouter } from 'vue-router';

interface Runner {
  id: number;
  name?: string;
  realName?: string;
  userNumber?: string;
  username?: string;
  email?: string;
  phone?: string;
  status: string;
  roles?: string[];
  avatar?: string;
  nickname?: string;
  bio?: string;
  createTime?: string;
}

const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalRunners = ref<number>(0);
const runners = ref<Runner[]>([]);
const selectedRunners = ref<Runner[]>([]);
const currentRunner = ref<Runner | null>(null);
const detailsDialogVisible = ref<boolean>(false);
const loading = ref<boolean>(false);
const router = useRouter();

// 在组件挂载时加载跑步爱好者数据
onMounted(async () => {
  await fetchRunners();
});

// 获取跑步爱好者数据
async function fetchRunners() {
  loading.value = true;
  try {
    const response = await axios.get('/api/supervisor/runners/assigned');
    const data = response.data;
    
    console.log('获取到的数据类型:', typeof data);
    console.log('获取到的数据结构:', data && typeof data === 'object' ? Object.keys(data) : '非对象数据');
    
    // 处理不同的响应格式
    if (Array.isArray(data)) {
      console.log('第一条跑步爱好者数据:', data.length > 0 ? JSON.stringify(data[0]) : '无数据');
      runners.value = data;
      totalRunners.value = data.length;
      if (data.length > 0) {
        ElMessage.success('成功获取跑步爱好者数据');
      } else {
        ElMessage.info('没有分配的跑步爱好者');
      }
    } else if (data && typeof data === 'object' && 'content' in data) {
      console.log('分页跑步爱好者数据第一条:', data.content.length > 0 ? JSON.stringify(data.content[0]) : '无数据');
      // 处理分页响应格式
      runners.value = data.content;
      totalRunners.value = data.totalElements || data.content.length;
      if (data.content.length > 0) {
        ElMessage.success('成功获取跑步爱好者数据');
      } else {
        ElMessage.info('没有分配的跑步爱好者');
      }
    } else {
      console.error('返回的跑步爱好者数据格式不正确:', data);
      ElMessage.warning('获取的跑步爱好者数据格式不正确');
      runners.value = [];
      totalRunners.value = 0;
    }
  } catch (error) {
    console.error('获取跑步爱好者数据失败:', error);
    ElMessage.error('获取跑步爱好者数据失败，请稍后重试');
    runners.value = [];
    totalRunners.value = 0;
  } finally {
    loading.value = false;
  }
}

// 根据搜索过滤跑步爱好者
const filteredRunners = computed(() => {
  if (!Array.isArray(runners.value)) {
    console.warn('跑步爱好者数据不是数组:', runners.value);
    return [];
  }
  
  if (!searchQuery.value) return runners.value;
  
  const query = searchQuery.value.toLowerCase();
  return runners.value.filter(runner => 
    (runner.realName && runner.realName.toLowerCase().includes(query)) || 
    (runner.name && runner.name.toLowerCase().includes(query)) || 
    (runner.username && runner.username.toLowerCase().includes(query)) || 
    (runner.userNumber && runner.userNumber.toLowerCase().includes(query)) || 
    (runner.email && runner.email.toLowerCase().includes(query))
  );
});

// 处理选择变更
const handleSelectionChange = (selection: Runner[]) => {
  selectedRunners.value = selection;
};

// 分页大小变化处理
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchRunners();
};

// 当前页变化处理
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  fetchRunners();
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
};

// 查看跑步爱好者详情
const viewRunnerDetails = async (runner: Runner) => {
  try {
    console.log('查看跑步爱好者详情:', runner);
    
    // 尝试从后端获取更详细的跑步爱好者信息
    const response = await axios.get(`/api/supervisor/runners/${runner.id}`);
    const runnerDetail = response.data;
    
    // 合并本地跑步爱好者数据和API返回的详情数据
    const processedRunner = {
      ...runner,
      ...runnerDetail,
      // 确保有realName
      realName: runnerDetail.realName || runner.realName || runner.name || '',
      // 确保有角色信息
      roles: runnerDetail.roles || runner.roles || ['USER']
    };
    
    currentRunner.value = processedRunner;
    detailsDialogVisible.value = true;
  } catch (error) {
    // 如果getRunnerDetail失败，直接使用表格中的跑步爱好者数据显示详情
    console.log('使用当前跑步爱好者数据作为详情:', runner);
    currentRunner.value = {
      ...runner,
      realName: runner.realName || runner.name || '',
      roles: runner.roles || ['USER']
    };
    detailsDialogVisible.value = true;
  }
};

// 切换跑步爱好者状态（启用/禁用）
const toggleRunnerStatus = async (runner: Runner) => {
  if (!runner) return;
  
  const newStatus = runner.status === 'active' ? 'inactive' : 'active';
  const confirmMessage = `确定要${newStatus === 'active' ? '启用' : '禁用'}跑步爱好者 ${runner.realName || runner.name || runner.username} 吗？`;
  
  try {
    await ElMessageBox.confirm(confirmMessage, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await axios.put(`/api/supervisor/runners/${runner.id}/status`, { status: newStatus });
    
    if (response.data && response.data.status === newStatus) {
      ElMessage.success(`跑步爱好者${runner.name}状态已${newStatus === 'active' ? '启用' : '禁用'}`);
      // 更新本地数据
      const index = runners.value.findIndex(r => r.id === runner.id);
      if (index !== -1) {
        runners.value[index].status = newStatus;
      }
      
      // 如果当前显示的是这个跑步爱好者的详情，也更新详情中的状态
      if (currentRunner.value && currentRunner.value.id === runner.id) {
        currentRunner.value.status = newStatus;
      }
    } else {
      ElMessage.error('状态更新失败');
    }
  } catch (error: any) {
    if (error === 'cancel') return;
    console.error('更新跑步爱好者状态失败:', error);
    ElMessage.error('更新状态失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 删除跑步爱好者
const deleteRunner = async (runner: Runner) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除跑步爱好者 ${runner.realName || runner.name || runner.username} 吗？此操作不可逆。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }
    );
    
    const response = await axios.delete(`/api/supervisor/runners/${runner.id}`);
    
    if (response.data && response.data.message) {
      ElMessage.success(`跑步爱好者${runner.name}已删除`);
      // 从本地数据中移除
      runners.value = runners.value.filter(r => r.id !== runner.id);
      totalRunners.value = runners.value.length;
      
      // 如果当前正在查看的是这个跑步爱好者，关闭详情对话框
      if (detailsDialogVisible.value && currentRunner.value && currentRunner.value.id === runner.id) {
        detailsDialogVisible.value = false;
      }
    } else {
      ElMessage.error('删除跑步爱好者失败');
    }
  } catch (error: any) {
    if (error === 'cancel') return;
    console.error('删除跑步爱好者失败:', error);
    ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};
</script>

<style scoped>
.runner-management-container {
  padding: 20px;
}

.main-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  display: flex;
  align-items: center;
}

.search-input {
  width: 250px;
  margin-left: 10px;
}

.name-cell {
  display: flex;
  align-items: center;
}

.user-name {
  margin-left: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.runner-info-container {
  padding: 20px;
}

.runner-header {
  display: flex;
  margin-bottom: 20px;
}

.avatar-container {
  margin-right: 20px;
}

.runner-main-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.runner-main-info h2 {
  margin: 0 0 10px 0;
}

.runner-id {
  color: #909399;
}

.role-tag {
  margin-right: 5px;
}

.data-section {
  margin-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.data-table th {
  font-weight: bold;
  background-color: #f5f7fa;
}

.data-table tr:hover {
  background-color: #f5f7fa;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 