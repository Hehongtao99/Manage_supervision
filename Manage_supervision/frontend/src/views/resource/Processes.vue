<template>
  <div class="resource-processes-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>进程管理</h2>
          <div class="header-controls">
            <el-radio-group v-model="displayMode" size="small" @change="handleDisplayModeChange">
              <el-radio-button label="top">Top进程</el-radio-button>
              <el-radio-button label="page">全部进程</el-radio-button>
            </el-radio-group>
            <el-button type="primary" :icon="Refresh" circle size="small" @click="refreshData" />
          </div>
        </div>
      </template>
      
      <div v-if="displayMode === 'top'" class="top-controls">
        <div class="control-row">
          <el-select v-model="topMode" placeholder="排序方式" size="small" @change="loadTopProcesses">
            <el-option label="按CPU使用率" value="cpu"></el-option>
            <el-option label="按内存使用率" value="memory"></el-option>
          </el-select>
          
          <el-select v-model="topLimit" placeholder="显示数量" size="small" @change="loadTopProcesses">
            <el-option label="Top 10" :value="10"></el-option>
            <el-option label="Top 20" :value="20"></el-option>
            <el-option label="Top 50" :value="50"></el-option>
          </el-select>
        </div>
      </div>
      
      <div v-if="displayMode === 'page'" class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索进程名称"
          prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      
      <div class="processes-content">
        <el-table
          v-loading="loading"
          :data="processList"
          stripe
          style="width: 100%"
          max-height="600"
          @row-click="handleRowClick"
        >
          <el-table-column prop="pid" label="PID" width="80" sortable />
          <el-table-column prop="name" label="进程名称" min-width="150" />
          <el-table-column prop="user" label="用户" width="100" />
          <el-table-column label="CPU占用" width="100" sortable>
            <template #default="scope">
              <div class="cpu-usage">
                <el-progress
                  :percentage="Math.min(scope.row.cpuUsage, 100)"
                  :color="getCpuUsageColor(scope.row.cpuUsage)"
                  :stroke-width="10"
                  :show-text="false"
                />
                <span>{{ scope.row.cpuUsage.toFixed(1) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="内存占用" width="120" sortable>
            <template #default="scope">
              {{ formatMemory(scope.row.memoryUsage) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="启动时间" width="170" v-if="displayMode === 'page'">
            <template #default="scope">
              {{ formatDateTime(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                text
                @click.stop="showProcessDetail(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页控件 -->
        <div class="pagination-container" v-if="displayMode === 'page' && !isSearchMode">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 进程详情对话框 -->
    <el-dialog
      v-model="processDetailVisible"
      title="进程详情"
      width="700px"
      destroy-on-close
    >
      <div v-if="selectedProcess" class="process-detail">
        <div class="detail-item">
          <span class="label">进程ID：</span>
          <span class="value">{{ selectedProcess.pid }}</span>
        </div>
        <div class="detail-item">
          <span class="label">进程名称：</span>
          <span class="value">{{ selectedProcess.name }}</span>
        </div>
        <div class="detail-item">
          <span class="label">用户：</span>
          <span class="value">{{ selectedProcess.user }}</span>
        </div>
        <div class="detail-item">
          <span class="label">CPU占用：</span>
          <span class="value">{{ selectedProcess.cpuUsage.toFixed(1) }}%</span>
        </div>
        <div class="detail-item">
          <span class="label">内存占用：</span>
          <span class="value">{{ formatMemory(selectedProcess.memoryUsage) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">状态：</span>
          <span class="value">
            <el-tag :type="getStatusType(selectedProcess.status)">
              {{ getStatusText(selectedProcess.status) }}
            </el-tag>
          </span>
        </div>
        <div class="detail-item">
          <span class="label">启动时间：</span>
          <span class="value">{{ formatDateTime(selectedProcess.startTime) }}</span>
        </div>
        <div class="detail-item command-line">
          <span class="label">命令行：</span>
          <el-input
            type="textarea"
            :rows="4"
            :modelValue="selectedProcess.command"
            readonly
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDetailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { getProcessList, getProcessListPaged, getTopProcesses, searchProcessByName } from '@/api/process';
import type { SystemProcess, PageResponse } from '@/api/process';
import dayjs from 'dayjs';

// 状态变量
const loading = ref(false);
const processList = ref<SystemProcess[]>([]);
const searchKeyword = ref('');
const processDetailVisible = ref(false);
const selectedProcess = ref<SystemProcess | null>(null);

// 分页相关
const currentPage = ref(0);
const pageSize = ref(20);
const totalItems = ref(0);
const totalPages = ref(0);

// 展示模式
const displayMode = ref<'top' | 'page'>('top'); // 默认显示Top进程
const isSearchMode = ref(false);

// Top进程相关
const topMode = ref<'cpu' | 'memory'>('cpu');
const topLimit = ref<number>(20);

// 切换显示模式
const handleDisplayModeChange = () => {
  if (displayMode.value === 'top') {
    loadTopProcesses();
  } else {
    isSearchMode.value = false;
    loadProcessesPage();
  }
};

// 加载分页进程数据
const loadProcessesPage = async () => {
  loading.value = true;
  try {
    const response: PageResponse = await getProcessListPaged(currentPage.value, pageSize.value);
    processList.value = response.processes;
    totalItems.value = response.totalItems;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('获取进程列表失败:', error);
    ElMessage.error('获取进程列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 加载Top进程数据
const loadTopProcesses = async () => {
  loading.value = true;
  try {
    processList.value = await getTopProcesses(topLimit.value, topMode.value);
    ElMessage.success(`已加载CPU使用率最高的${topLimit.value}个进程`);
  } catch (error) {
    console.error('获取Top进程列表失败:', error);
    ElMessage.error('获取Top进程列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page - 1; // Element Plus分页组件从1开始，而API从0开始
  loadProcessesPage();
};

// 处理每页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 0; // 重置到第一页
  loadProcessesPage();
};

// 刷新数据
const refreshData = async () => {
  if (displayMode.value === 'top') {
    await loadTopProcesses();
  } else if (isSearchMode.value && searchKeyword.value) {
    await handleSearch();
  } else {
    await loadProcessesPage();
  }
  ElMessage.success('进程数据刷新成功');
};

// 搜索进程
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    isSearchMode.value = false;
    await loadProcessesPage();
    return;
  }
  
  isSearchMode.value = true;
  loading.value = true;
  try {
    processList.value = await searchProcessByName(searchKeyword.value.trim());
  } catch (error) {
    console.error('搜索进程失败:', error);
    ElMessage.error('搜索进程失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 格式化内存显示
const formatMemory = (memoryKB: number): string => {
  if (memoryKB < 1024) {
    return `${memoryKB.toFixed(0)} KB`;
  } else if (memoryKB < 1024 * 1024) {
    return `${(memoryKB / 1024).toFixed(1)} MB`;
  } else {
    return `${(memoryKB / (1024 * 1024)).toFixed(1)} GB`;
  }
};

// 格式化日期时间
const formatDateTime = (dateTimeStr: string | null): string => {
  if (!dateTimeStr) return '未知';
  return dayjs(dateTimeStr).format('YYYY-MM-DD HH:mm:ss');
};

// 获取CPU使用率颜色
const getCpuUsageColor = (usage: number): string => {
  if (usage < 30) return '#67C23A';
  if (usage < 70) return '#E6A23C';
  return '#F56C6C';
};

// 获取状态类型
const getStatusType = (status: string): string => {
  switch (status.toLowerCase()) {
    case 'running':
      return 'success';
    case 'terminated':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status: string): string => {
  switch (status.toLowerCase()) {
    case 'running':
      return '运行中';
    case 'terminated':
      return '已终止';
    default:
      return status;
  }
};

// 显示进程详情
const showProcessDetail = (process: SystemProcess) => {
  selectedProcess.value = process;
  processDetailVisible.value = true;
};

// 点击行
const handleRowClick = (row: SystemProcess) => {
  showProcessDetail(row);
};

// 组件挂载后加载数据
onMounted(() => {
  // 默认加载Top进程，这个更快
  loadTopProcesses();
});
</script>

<style scoped>
.resource-processes-container {
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

.header-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
  max-width: 500px;
}

.top-controls {
  margin-bottom: 20px;
}

.control-row {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-bottom: 10px;
}

.processes-content {
  min-height: 400px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.cpu-usage {
  display: flex;
  align-items: center;
  gap: 5px;
}

.cpu-usage .el-progress {
  width: 60px;
  margin-right: 5px;
}

.process-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
}

.detail-item .label {
  font-weight: bold;
  width: 100px;
  flex-shrink: 0;
}

.detail-item .value {
  flex: 1;
}

.command-line {
  flex-direction: column;
  gap: 8px;
}
</style> 