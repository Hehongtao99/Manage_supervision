<template>
  <div class="resource-overview-page">
    <h1 class="page-title">资源总览</h1>
    
    <!-- 统计卡片区域 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <!-- 主机状态统计 -->
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>主机状态</span>
                <el-button type="text" @click="navigateTo('/resource/hosts')">
                  查看详情
                </el-button>
              </div>
            </template>
            <div class="stats-content">
              <el-row>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ hostStats.online || 0 }}</div>
                  <div class="stats-label">在线</div>
                </el-col>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ hostStats.offline || 0 }}</div>
                  <div class="stats-label">离线</div>
                </el-col>
              </el-row>
              <div class="stats-chart">
                <el-progress 
                  :percentage="hostOnlinePercentage" 
                  :format="format" 
                  :stroke-width="10" 
                  status="success"
                />
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 进程统计 -->
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>进程状态</span>
                <el-button type="text" @click="navigateTo('/resource/processes')">
                  查看详情
                </el-button>
              </div>
            </template>
            <div class="stats-content">
              <el-row>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ processStats.running || 0 }}</div>
                  <div class="stats-label">运行中</div>
                </el-col>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ processStats.total || 0 }}</div>
                  <div class="stats-label">总进程数</div>
                </el-col>
              </el-row>
              <div class="stats-chart">
                <el-progress 
                  :percentage="cpuUsagePercentage" 
                  :stroke-width="10" 
                  :format="formatPercentage"
                />
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 系统状态 -->
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>系统状态</span>
                <el-button type="text" @click="navigateTo('/intrusion-detection')">
                  查看详情
                </el-button>
              </div>
            </template>
            <div class="stats-content">
              <el-row>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ systemInfo.memoryUsage || 0 }}%</div>
                  <div class="stats-label">内存使用率</div>
                </el-col>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ systemInfo.diskUsage || 0 }}%</div>
                  <div class="stats-label">磁盘使用率</div>
                </el-col>
              </el-row>
              <div class="stats-chart">
                <el-progress 
                  :percentage="systemInfo.memoryUsage" 
                  :stroke-width="10" 
                  :status="memoryStatus"
                  :format="formatPercentage"
                />
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 接口检测 -->
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stats-card">
            <template #header>
              <div class="card-header">
                <span>接口统计</span>
                <el-button type="text" @click="navigateTo('/api-endpoints')">
                  查看详情
                </el-button>
              </div>
            </template>
            <div class="stats-content">
              <el-row>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ apiStats.totalEndpoints || 0 }}</div>
                  <div class="stats-label">总接口数</div>
                </el-col>
                <el-col :span="12" class="stats-item">
                  <div class="stats-value">{{ apiStats.totalAccess || 0 }}</div>
                  <div class="stats-label">总访问数</div>
                </el-col>
              </el-row>
              <div v-if="apiStats.responseTime" class="stats-chart average-response">
                <div class="response-label">平均响应时间:</div>
                <div class="response-value">{{ apiStats.responseTime }}ms</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 详细数据区域 -->
    <el-row :gutter="20" class="detail-section">
      <!-- 左侧：主机和进程详情 -->
      <el-col :md="12">
        <!-- 主机状态列表 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>主机状态列表</span>
              <el-button type="text" @click="refreshData">刷新</el-button>
            </div>
          </template>
          <div class="host-list" v-loading="loading.hosts">
            <el-table :data="recentHosts" style="width: 100%" size="small">
              <el-table-column prop="hostname" label="主机名" />
              <el-table-column prop="ip" label="IP地址" width="120" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'" size="small">
                    {{ scope.row.status === 'online' ? '在线' : '离线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="cpuUsage" label="CPU使用率" width="120">
                <template #default="scope">
                  <el-progress 
                    :percentage="scope.row.cpuUsage ? scope.row.cpuUsage[scope.row.cpuUsage.length - 1] : 0" 
                    :stroke-width="5" 
                    :show-text="false"
                  />
                  <span>{{ scope.row.cpuUsage ? scope.row.cpuUsage[scope.row.cpuUsage.length - 1] : 0 }}%</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
        
        <!-- 进程Top5 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>CPU占用Top5进程</span>
              <el-button type="text" @click="refreshData">刷新</el-button>
            </div>
          </template>
          <div class="process-list" v-loading="loading.processes">
            <el-table :data="topProcesses" style="width: 100%" size="small">
              <el-table-column prop="pid" label="PID" width="80" />
              <el-table-column prop="name" label="进程名" min-width="120" show-overflow-tooltip />
              <el-table-column prop="cpuUsage" label="CPU占用" width="120">
                <template #default="scope">
                  <el-progress 
                    :percentage="scope.row.cpuUsage" 
                    :stroke-width="5" 
                    :show-text="false"
                  />
                  <span>{{ scope.row.cpuUsage }}%</span>
                </template>
              </el-table-column>
              <el-table-column prop="memoryUsage" label="内存占用" width="120">
                <template #default="scope">
                  <span>{{ formatMemory(scope.row.memoryUsage) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：接口和系统信息 -->
      <el-col :md="12">
        <!-- 最近访问的接口 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>最近访问的接口</span>
              <el-button type="text" @click="refreshData">刷新</el-button>
            </div>
          </template>
          <div class="api-list" v-loading="loading.apis">
            <el-table :data="recentApis" style="width: 100%" size="small">
              <el-table-column prop="method" label="方法" width="80">
                <template #default="scope">
                  <el-tag
                    :type="getMethodType(scope.row.method)"
                    size="small"
                    effect="plain"
                  >
                    {{ scope.row.method }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
              <el-table-column prop="accessCount" label="访问次数" width="80" />
              <el-table-column prop="lastAccessed" label="最后访问时间" width="180">
                <template #default="scope">
                  <span>{{ formatDateTime(scope.row.lastAccessed) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
        
        <!-- 系统信息图表 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span>系统资源使用趋势</span>
              <div>
                <el-radio-group v-model="chartTimeRange" size="small" @change="refreshData">
                  <el-radio-button label="hour">最近1小时</el-radio-button>
                  <el-radio-button label="day">最近24小时</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div class="chart-container" v-loading="loading.chart">
            <v-chart class="chart" :option="chartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components';
import VChart from 'vue-echarts';
import { getResourceOverview, getResourceUsageTrend } from '../../api/overview';
import { format as formatDate } from 'date-fns';
import type { Host } from '../../api/host';
import type { SystemProcess } from '../../api/process';

// 注册echarts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
]);

const router = useRouter();

// 数据加载状态
const loading = ref({
  hosts: false,
  processes: false,
  apis: false,
  chart: false
});

// 统计数据
const hostStats = ref({ online: 0, offline: 0, total: 0 });
const processStats = ref({ running: 0, total: 0 });
const systemInfo = ref({ cpuUsage: 0, memoryUsage: 0, diskUsage: 0 });
const apiStats = ref({ totalEndpoints: 0, totalAccess: 0, responseTime: 0 });

// 详细数据
const recentHosts = ref<Host[]>([]);
const topProcesses = ref<SystemProcess[]>([]);
const recentApis = ref<any[]>([]);
const usageTrend = ref({ 
  timestamps: [], 
  cpu: [], 
  memory: [], 
  network: { input: [], output: [] } 
});

// 图表时间范围
const chartTimeRange = ref('hour');

// 计算属性
const hostOnlinePercentage = computed(() => {
  const total = hostStats.value.total;
  if (total === 0) return 0;
  return Math.round((hostStats.value.online / total) * 100);
});

const cpuUsagePercentage = computed(() => {
  return systemInfo.value.cpuUsage || 0;
});

const memoryStatus = computed(() => {
  const usage = systemInfo.value.memoryUsage || 0;
  if (usage > 90) return 'exception';
  if (usage > 70) return 'warning';
  return 'success';
});

// 格式化函数
const format = (percentage: number) => `在线率 ${percentage}%`;
const formatPercentage = (percentage: number) => `${percentage}%`;

const formatMemory = (kb: number) => {
  if (kb < 1024) return `${kb} KB`;
  const mb = kb / 1024;
  if (mb < 1024) return `${mb.toFixed(1)} MB`;
  const gb = mb / 1024;
  return `${gb.toFixed(1)} GB`;
};

const formatDateTime = (dateTime: string | null) => {
  if (!dateTime) return '从未访问';
  const date = new Date(dateTime);
  return formatDate(date, 'yyyy-MM-dd HH:mm:ss');
};

const getMethodType = (method: string) => {
  switch (method) {
    case 'GET': return 'success';
    case 'POST': return 'primary';
    case 'PUT': return 'warning';
    case 'DELETE': return 'danger';
    default: return 'info';
  }
};

// 图表选项
const chartOption = computed(() => {
  return {
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any[]) {
        let result = params[0].axisValue + '<br/>';
        params.forEach(param => {
          const value = param.seriesName.includes('网络') 
            ? `${(param.value / 1024).toFixed(2)} KB/s` 
            : `${param.value}%`;
          result += `${param.marker} ${param.seriesName}: ${value}<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: ['CPU使用率', '内存使用率', '网络流入', '网络流出']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: usageTrend.value.timestamps.map(timestamp => {
        const date = new Date(timestamp);
        return formatDate(date, 'HH:mm');
      })
    },
    yAxis: [
      {
        type: 'value',
        name: '使用率 (%)',
        min: 0,
        max: 100
      },
      {
        type: 'value',
        name: '网络 (KB/s)',
        min: 0,
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        data: usageTrend.value.cpu,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        }
      },
      {
        name: '内存使用率',
        type: 'line',
        data: usageTrend.value.memory,
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        }
      },
      {
        name: '网络流入',
        type: 'line',
        yAxisIndex: 1,
        data: usageTrend.value.network.input.map(v => v / 1024), // 转换为KB/s
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        }
      },
      {
        name: '网络流出',
        type: 'line',
        yAxisIndex: 1,
        data: usageTrend.value.network.output.map(v => v / 1024), // 转换为KB/s
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        }
      }
    ]
  };
});

// 加载总览数据
const loadOverviewData = async () => {
  loading.value.hosts = true;
  loading.value.processes = true;
  loading.value.apis = true;
  
  try {
    const data = await getResourceOverview();
    
    // 更新统计数据
    hostStats.value = data.hostStats;
    processStats.value = data.processStats;
    systemInfo.value = data.systemInfo;
    apiStats.value = data.apiStats;
    
    // 更新详细数据
    recentHosts.value = data.recentHosts;
    topProcesses.value = data.topProcesses;
    recentApis.value = data.recentApis;
  } catch (error) {
    console.error('加载数据总览失败', error);
  } finally {
    loading.value.hosts = false;
    loading.value.processes = false;
    loading.value.apis = false;
  }
};

// 加载趋势数据
const loadTrendData = async () => {
  loading.value.chart = true;
  
  try {
    const data = await getResourceUsageTrend(chartTimeRange.value);
    usageTrend.value = data;
  } catch (error) {
    console.error('加载趋势数据失败', error);
  } finally {
    loading.value.chart = false;
  }
};

// 刷新全部数据
const refreshData = () => {
  loadOverviewData();
  loadTrendData();
};

// 导航到其他页面
const navigateTo = (path: string) => {
  router.push(path);
};

// 自动刷新定时器
let refreshTimer: number | null = null;

// 生命周期钩子
onMounted(() => {
  refreshData();
  // 每60秒自动刷新一次数据
  refreshTimer = window.setInterval(() => {
    refreshData();
  }, 60000);
});

onBeforeUnmount(() => {
  if (refreshTimer !== null) {
    clearInterval(refreshTimer);
  }
});
</script>

<style scoped>
.resource-overview-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-content {
  padding: 5px 0;
}

.stats-item {
  text-align: center;
  padding: 5px 0;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.stats-chart {
  margin-top: 15px;
}

.average-response {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
}

.response-label {
  margin-right: 10px;
  color: #606266;
}

.response-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.detail-section {
  margin-top: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.host-list,
.process-list,
.api-list {
  height: 250px;
  overflow-y: auto;
}

.chart-container {
  height: 250px;
}

.chart {
  height: 100%;
  width: 100%;
}

:deep(.el-card__header) {
  padding: 10px 20px;
}

:deep(.el-progress--line) {
  margin-bottom: 0;
}
</style> 