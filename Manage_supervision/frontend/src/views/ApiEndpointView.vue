<template>
  <div class="api-endpoint-view">
    <h1>服务接口检测</h1>
    
    <!-- 统计信息卡片 -->
    <el-card class="stats-card" v-loading="statsLoading">
      <template #header>
        <div class="card-header">
          <span>接口统计</span>
        </div>
      </template>
      <div class="stats-content">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalEndpoints || 0 }}</div>
          <div class="stat-label">总接口数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalAccess || 0 }}</div>
          <div class="stat-label">总访问次数</div>
        </div>
      </div>
    </el-card>
    
    <!-- 控制器选择器 -->
    <div class="controller-selector">
      <el-select
        v-model="selectedController"
        placeholder="选择控制器"
        clearable
        @change="handleControllerChange"
      >
        <el-option
          v-for="controller in controllers"
          :key="controller"
          :label="controller"
          :value="controller"
        />
      </el-select>
      
      <el-select
        v-model="methodFilter"
        placeholder="HTTP方法"
        clearable
        @change="filterEndpoints"
      >
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
      </el-select>
      
      <el-input
        v-model="searchKeyword"
        placeholder="搜索接口路径"
        clearable
        @input="filterEndpoints"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <!-- 接口表格容器 -->
    <div class="table-container">
      <el-table
        :data="filteredEndpoints"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="method" label="方法" width="100">
          <template #default="scope">
            <el-tag
              :type="getMethodType(scope.row.method)"
              size="small"
              effect="dark"
            >
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="250" show-overflow-tooltip />
        <el-table-column prop="controllerName" label="控制器" width="180" show-overflow-tooltip />
        <el-table-column prop="methodName" label="方法名" width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requiredRoles" label="所需角色" width="120" show-overflow-tooltip />
        <el-table-column prop="accessCount" label="访问次数" width="100" sortable />
        <el-table-column prop="averageResponseTime" label="平均响应时间(ms)" width="150" sortable>
          <template #default="scope">
            <span>{{ scope.row.averageResponseTime || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastAccessed" label="最后访问时间" width="180">
          <template #default="scope">
            <span>{{ formatDateTime(scope.row.lastAccessed) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { 
  getAllApiEndpoints, 
  getApiEndpointsByControllerName, 
  getAllControllerNames,
  getApiAccessStats
} from '../api/apiEndpoint';

// 数据定义
interface ApiEndpoint {
  id: number;
  path: string;
  method: string;
  controllerName: string;
  methodName: string;
  requestParams: string;
  description: string;
  requiredRoles: string;
  lastAccessed: string;
  accessCount: number;
  averageResponseTime: number;
}

// 状态变量
const endpoints = ref<ApiEndpoint[]>([]);
const filteredEndpoints = ref<ApiEndpoint[]>([]);
const controllers = ref<string[]>([]);
const selectedController = ref('');
const methodFilter = ref('');
const searchKeyword = ref('');
const loading = ref(false);
const statsLoading = ref(false);
const stats = ref<Record<string, number>>({});

// 生命周期钩子
onMounted(async () => {
  loading.value = true;
  statsLoading.value = true;
  
  try {
    // 获取所有接口数据
    const data = await getAllApiEndpoints();
    endpoints.value = data;
    filteredEndpoints.value = data;
    
    // 获取所有控制器名称
    const controllerNames = await getAllControllerNames();
    controllers.value = controllerNames;
    
    // 获取统计信息
    const statsData = await getApiAccessStats();
    stats.value = statsData;
  } catch (error) {
    console.error('加载数据失败', error);
  } finally {
    loading.value = false;
    statsLoading.value = false;
  }
});

// 方法定义
const handleControllerChange = async () => {
  if (!selectedController.value) {
    // 如果没有选择控制器，则获取所有接口
    try {
      loading.value = true;
      const data = await getAllApiEndpoints();
      endpoints.value = data;
      filterEndpoints();
    } catch (error) {
      console.error('获取所有接口失败', error);
    } finally {
      loading.value = false;
    }
  } else {
    // 根据选择的控制器获取接口
    try {
      loading.value = true;
      const data = await getApiEndpointsByControllerName(selectedController.value);
      endpoints.value = data;
      filterEndpoints();
    } catch (error) {
      console.error('根据控制器获取接口失败', error);
    } finally {
      loading.value = false;
    }
  }
};

const filterEndpoints = () => {
  // 根据HTTP方法和搜索关键字过滤接口
  filteredEndpoints.value = endpoints.value.filter(endpoint => {
    const methodMatch = !methodFilter.value || endpoint.method === methodFilter.value;
    const keywordMatch = !searchKeyword.value || 
      endpoint.path.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      endpoint.description.toLowerCase().includes(searchKeyword.value.toLowerCase());
    
    return methodMatch && keywordMatch;
  });
};

// 获取HTTP方法对应的样式类型
const getMethodType = (method: string) => {
  switch (method) {
    case 'GET':
      return 'success';
    case 'POST':
      return 'primary';
    case 'PUT':
      return 'warning';
    case 'DELETE':
      return 'danger';
    default:
      return 'info';
  }
};

// 格式化日期时间
const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '从未访问';
  
  const date = new Date(dateTime);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};
</script>

<style scoped>
.api-endpoint-view {
  padding: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-content {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-item .stat-label {
  color: #606266;
  font-size: 14px;
}

.controller-selector {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.controller-selector .el-select,
.controller-selector .el-input {
  flex-grow: 1;
}

/* 新增：表格容器样式 */
.table-container {
  max-height: 600px;
  overflow-y: auto;
  margin-top: 20px;
}
</style> 