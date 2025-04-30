<template>
  <div class="operation-logs">
    <div class="page-header">
      <h1 class="page-title">用户操作日志</h1>
      <div class="page-actions">
        <el-button 
          type="primary" 
          size="small" 
          @click="refreshData" 
          :disabled="loading"
        >
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
        <el-button 
          type="danger" 
          size="small" 
          @click="showDeleteConfirm" 
          :disabled="loading"
        >
          <el-icon><Delete /></el-icon> 清理历史日志
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="用户名">
          <el-input 
            v-model="filters.username" 
            placeholder="输入用户名" 
            clearable
            @clear="refreshData"
          />
        </el-form-item>
        <el-form-item label="操作">
          <el-input 
            v-model="filters.operation" 
            placeholder="操作描述" 
            clearable
            @clear="refreshData"
          />
        </el-form-item>
        <el-form-item label="模块">
          <el-select 
            v-model="filters.module" 
            placeholder="请选择模块" 
            clearable
            @clear="refreshData"
          >
            <el-option 
              v-for="item in moduleOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="操作日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="onDateRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="log-table-card">
      <div v-if="loading" class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <div v-else>
        <el-table
          :data="logs"
          style="width: 100%"
          border
          stripe
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="operation" label="操作" min-width="180" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="result" label="结果" width="100">
            <template #default="scope">
              <el-tag 
                :type="scope.row.result.includes('失败') ? 'danger' : 'success'"
                effect="plain"
                size="small"
              >
                {{ scope.row.result }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址" width="140" />
          <el-table-column prop="operationTime" label="操作时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.operationTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button 
                size="small" 
                type="primary" 
                link
                @click="showLogDetail(scope.row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="操作日志详情"
      width="700px"
    >
      <div v-if="currentLog" class="log-detail">
        <div class="detail-item">
          <span class="detail-label">ID:</span>
          <span class="detail-value">{{ currentLog.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">用户:</span>
          <span class="detail-value">{{ currentLog.username }} (ID: {{ currentLog.userId }})</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作:</span>
          <span class="detail-value">{{ currentLog.operation }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">模块:</span>
          <span class="detail-value">{{ currentLog.module }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">方法:</span>
          <span class="detail-value">{{ currentLog.method }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">参数:</span>
          <div class="detail-value code-block">
            <pre>{{ formatJson(currentLog.params) }}</pre>
          </div>
        </div>
        <div class="detail-item">
          <span class="detail-label">结果:</span>
          <span class="detail-value">{{ currentLog.result }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">IP地址:</span>
          <span class="detail-value">{{ currentLog.ip }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">浏览器信息:</span>
          <div class="detail-value">{{ currentLog.userAgent }}</div>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作时间:</span>
          <span class="detail-value">{{ formatDateTime(currentLog.operationTime) }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="清理历史日志"
      width="500px"
    >
      <div class="delete-dialog-content">
        <p>请选择要删除哪个日期之前的操作日志：</p>
        <el-date-picker
          v-model="deleteBeforeDate"
          type="date"
          placeholder="选择日期"
          style="width: 100%"
          value-format="YYYY-MM-DD"
        />
        <p class="warning-text">
          <el-icon><Warning /></el-icon>
          警告：删除操作不可恢复，请谨慎操作！
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            @click="deleteLogsBefore"
            :loading="deleteLoading"
          >
            确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Delete, Loading, Warning } from '@element-plus/icons-vue';
import type { OperationLogEntry } from '../../types/log';
import logApi from '../../api/log';

// 状态定义
const logs = ref<OperationLogEntry[]>([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const detailVisible = ref(false);
const currentLog = ref<OperationLogEntry | null>(null);
const deleteDialogVisible = ref(false);
const deleteBeforeDate = ref('');
const deleteLoading = ref(false);
const dateRange = ref<[string, string] | null>(null);

// 筛选条件
const filters = reactive({
  username: '',
  operation: '',
  startDate: '',
  endDate: '',
  module: ''
});

// 模块选项
const moduleOptions = [
  { value: 'Users', label: '用户管理' },
  { value: 'Roles', label: '角色管理' },
  { value: 'Auth', label: '认证授权' },
  { value: 'Projects', label: '课题管理' },
  { value: 'Tasks', label: '任务管理' },
  { value: 'Classes', label: '班级管理' },
  { value: 'Logs', label: '日志管理' }
];

// 生命周期钩子
onMounted(() => {
  loadData();
});

// 数据加载方法
const loadData = async () => {
  loading.value = true;
  try {
    const result = await logApi.getOperationLogs({
      username: filters.username || undefined,
      operation: filters.operation || undefined,
      startDate: filters.startDate || undefined,
      endDate: filters.endDate || undefined,
      module: filters.module || undefined,
      page: currentPage.value - 1, // 后端页码从0开始
      size: pageSize.value
    });
    
    logs.value = result.content;
    total.value = result.totalElements;
  } catch (error) {
    console.error('加载操作日志失败:', error);
    ElMessage.error('加载操作日志失败，请稍后重试');
    logs.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = () => {
  currentPage.value = 1;
  loadData();
};

// 搜索
const search = () => {
  currentPage.value = 1;
  loadData();
};

// 重置筛选条件
const resetFilters = () => {
  filters.username = '';
  filters.operation = '';
  filters.startDate = '';
  filters.endDate = '';
  filters.module = '';
  dateRange.value = null;
  refreshData();
};

// 日期范围变化
const onDateRangeChange = (val: [string, string] | null) => {
  if (val) {
    filters.startDate = val[0];
    filters.endDate = val[1];
  } else {
    filters.startDate = '';
    filters.endDate = '';
  }
};

// 每页条数变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  refreshData();
};

// 页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
  loadData();
};

// 显示日志详情
const showLogDetail = (log: OperationLogEntry) => {
  currentLog.value = log;
  detailVisible.value = true;
};

// 显示删除确认对话框
const showDeleteConfirm = () => {
  // 默认设置为一个月前
  const date = new Date();
  date.setMonth(date.getMonth() - 1);
  deleteBeforeDate.value = formatDate(date);
  deleteDialogVisible.value = true;
};

// 删除指定日期前的日志
const deleteLogsBefore = async () => {
  if (!deleteBeforeDate.value) {
    ElMessage.warning('请选择日期');
    return;
  }
  
  deleteLoading.value = true;
  try {
    const result = await logApi.deleteOperationLogsBefore(deleteBeforeDate.value);
    ElMessage.success(result.message);
    deleteDialogVisible.value = false;
    refreshData();
  } catch (error) {
    console.error('删除历史日志失败:', error);
    ElMessage.error('删除历史日志失败，请稍后重试');
  } finally {
    deleteLoading.value = false;
  }
};

// 格式化日期时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '';
  
  const date = new Date(dateTimeStr);
  if (isNaN(date.getTime())) return dateTimeStr;
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

// 格式化日期
const formatDate = (date: Date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 格式化JSON字符串
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '';
  
  try {
    const obj = JSON.parse(jsonStr);
    return JSON.stringify(obj, null, 2);
  } catch (e) {
    return jsonStr;
  }
};
</script>

<style scoped>
.operation-logs {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 20px;
}

.log-table-card {
  min-height: 500px;
  height: calc(100vh - 280px); /* 设置卡片固定高度，减去上方元素占用的空间 */
  display: flex;
  flex-direction: column;
}

.log-table-card > div {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止内容溢出 */
}

.log-table-card .el-table {
  flex: 1;
  overflow-y: auto; /* 表格添加垂直滚动条 */
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.loading-icon {
  font-size: 40px;
  color: #409eff;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.log-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
}

.detail-label {
  width: 100px;
  font-weight: bold;
  color: #606266;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.code-block {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow: auto;
  max-height: 200px;
}

.code-block pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.delete-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.warning-text {
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 10px;
}

/* 日志详情对话框的滚动样式 */
.el-dialog__body {
  max-height: 70vh; /* 设置最大高度为视口高度的70% */
  overflow-y: auto; /* 添加垂直滚动条 */
}
</style> 