<template>
  <div class="resource-hosts-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>主机管理</h2>
          <div class="header-operations">
            <el-input
              v-model="searchQuery"
              placeholder="搜索主机名/IP"
              class="search-input"
              clearable
              :prefix-icon="Search"
              @input="handleSearch"
            />
            <el-button type="primary" :icon="Refresh" @click="fetchHostsData">刷新</el-button>
            <el-button type="success" @click="fetchCurrentHostInfo">刷新本机信息</el-button>
          </div>
        </div>
      </template>
      
      <div class="hosts-content">
        <el-table
          v-loading="loading"
          :data="filteredHosts"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="hostname" label="主机名" min-width="150" />
          <el-table-column prop="ip" label="IP地址" min-width="150" />
          <el-table-column prop="os" label="操作系统" min-width="150" />
          <el-table-column prop="cpuModel" label="CPU型号" min-width="180" />
          <el-table-column prop="cpuCores" label="CPU核心数" width="120" />
          <el-table-column prop="memoryTotal" label="内存总量" min-width="120">
            <template #default="scope">
              {{ formatMemory(scope.row.memoryTotal) }}
            </template>
          </el-table-column>
          <el-table-column prop="diskTotal" label="磁盘总量" min-width="120">
            <template #default="scope">
              {{ formatDisk(scope.row.diskTotal) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row.status)"
                effect="dark"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="primary"
                @click="viewHostDetails(scope.row)"
                :icon="View"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalHosts"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 主机详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="主机详情"
      width="70%"
      destroy-on-close
    >
      <div v-if="currentHost" class="host-details">
        <el-descriptions border :column="2">
          <el-descriptions-item label="主机名">{{ currentHost.hostname }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ currentHost.ip }}</el-descriptions-item>
          <el-descriptions-item label="操作系统">{{ currentHost.os }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentHost.status)">
              {{ getStatusText(currentHost.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="CPU型号">{{ currentHost.cpuModel }}</el-descriptions-item>
          <el-descriptions-item label="CPU核心数">{{ currentHost.cpuCores }}</el-descriptions-item>
          <el-descriptions-item label="内存总量">{{ formatMemory(currentHost.memoryTotal) }}</el-descriptions-item>
          <el-descriptions-item label="磁盘总量">{{ formatDisk(currentHost.diskTotal) }}</el-descriptions-item>
          <el-descriptions-item label="最后更新时间" :span="2">
            {{ formatDate(currentHost.lastUpdateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2" v-if="currentHost.description">
            {{ currentHost.description }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- CPU使用率图表 -->
        <div class="chart-container">
          <h3>CPU使用率 (%)</h3>
          <div ref="cpuChartRef" class="chart"></div>
        </div>

        <!-- 内存使用率图表 -->
        <div class="chart-container">
          <h3>内存使用率 (%)</h3>
          <div ref="memoryChartRef" class="chart"></div>
        </div>

        <!-- 网络流量图表 -->
        <div class="chart-container">
          <h3>网络流量 (KB/s)</h3>
          <div ref="networkChartRef" class="chart"></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { Refresh, Search, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import hostService, { Host } from '../../api/host'
import { format } from 'date-fns'

// 状态
const loading = ref(true)
const hosts = ref<Host[]>([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalHosts = ref(0)
const dialogVisible = ref(false)
const currentHost = ref<Host | null>(null)
const cpuChartRef = ref<HTMLElement | null>(null)
const memoryChartRef = ref<HTMLElement | null>(null)
const networkChartRef = ref<HTMLElement | null>(null)
let cpuChart: echarts.ECharts | null = null
let memoryChart: echarts.ECharts | null = null
let networkChart: echarts.ECharts | null = null
let timer: number | null = null

// 根据状态获取类型颜色
const getStatusType = (status: string) => {
  switch (status) {
    case 'online':
      return 'success'
    case 'offline':
      return 'info'
    case 'warning':
      return 'warning'
    case 'error':
      return 'danger'
    default:
      return 'info'
  }
}

// 根据状态获取文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'online':
      return '在线'
    case 'offline':
      return '离线'
    case 'warning':
      return '警告'
    case 'error':
      return '错误'
    default:
      return '未知'
  }
}

// 格式化内存大小
const formatMemory = (memory: number) => {
  if (memory < 1024) {
    return `${memory} MB`
  } else {
    return `${(memory / 1024).toFixed(2)} GB`
  }
}

// 格式化磁盘大小
const formatDisk = (disk: number) => {
  if (disk < 1024) {
    return `${disk} MB`
  } else if (disk < 1024 * 1024) {
    return `${(disk / 1024).toFixed(2)} GB`
  } else {
    return `${(disk / (1024 * 1024)).toFixed(2)} TB`
  }
}

// 格式化日期时间
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return format(date, 'yyyy-MM-dd HH:mm:ss')
  } catch (error) {
    return dateString
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1 // 重置到第一页
}

// 过滤后的主机列表
const filteredHosts = computed(() => {
  if (!searchQuery.value) {
    return hosts.value
  }
  const query = searchQuery.value.toLowerCase()
  return hosts.value.filter(
    host => 
      host.hostname.toLowerCase().includes(query) || 
      host.ip.toLowerCase().includes(query)
  )
})

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchHostsData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchHostsData()
}

// 获取主机数据
const fetchHostsData = async () => {
  loading.value = true
  try {
    // 调用API获取主机列表
    const response = await hostService.getHosts({
      page: currentPage.value,
      pageSize: pageSize.value,
      query: searchQuery.value
    })
    
    hosts.value = response.data
    totalHosts.value = response.total
    
    // 获取当前主机信息
    await fetchCurrentHostInfo()
    
    ElMessage.success('主机数据刷新成功')
  } catch (error) {
    console.error('获取主机数据失败:', error)
    ElMessage.error('获取主机数据失败，请检查网络连接')
    
    // 如果后端接口调用失败，尝试只获取当前主机信息
    try {
      await fetchCurrentHostInfo()
    } catch (innerError) {
      console.error('获取当前主机信息也失败:', innerError)
      
      // 仅在开发环境使用模拟数据
      if (import.meta.env.DEV) {
        mockHostData()
      }
    }
  } finally {
    loading.value = false
  }
}

// 查看主机详情
const viewHostDetails = async (host: Host) => {
  // 前端增加ID校验
  if (host.id === null || host.id === undefined) {
    console.error('尝试查看详情的主机ID无效:', host);
    ElMessage.error('无效的主机ID，无法查看详情');
    return;
  }
  
  try {
    // 如果是临时主机（ID为-1），则直接显示，不从后端获取
    if (host.id === -1) {
      currentHost.value = host;
      dialogVisible.value = true;
      
      // 在下一个DOM更新周期初始化图表
      setTimeout(() => {
        initCharts();
      }, 100);
      return;
    }
    
    // 获取主机详情数据
    const hostData = await hostService.getHostById(host.id);
    
    currentHost.value = hostData;
    dialogVisible.value = true;
    
    // 在下一个DOM更新周期初始化图表
    setTimeout(() => {
      initCharts();
    }, 100);
  } catch (error) {
    console.error('获取主机详情失败:', error);
    ElMessage.error('获取主机详情失败，请检查网络连接');
    
    // 仅在开发环境使用模拟数据
    if (import.meta.env.DEV) {
      currentHost.value = host;
      dialogVisible.value = true;
      
      setTimeout(() => {
        initCharts();
      }, 100);
    }
  }
}

// 初始化图表
const initCharts = () => {
  if (!currentHost.value) return
  
  // 初始化CPU使用率图表
  if (cpuChartRef.value) {
    cpuChart = echarts.init(cpuChartRef.value)
    const cpuOption = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 10 }, (_, i) => `${i + 1}分钟前`).reverse()
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          data: currentHost.value.cpuUsage || [],
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#5470c6'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(84, 112, 198, 0.5)' },
              { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
            ])
          }
        }
      ]
    }
    cpuChart.setOption(cpuOption)
  }
  
  // 初始化内存使用率图表
  if (memoryChartRef.value) {
    memoryChart = echarts.init(memoryChartRef.value)
    const memoryOption = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 10 }, (_, i) => `${i + 1}分钟前`).reverse()
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [
        {
          data: currentHost.value.memoryUsage || [],
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#91cc75'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(145, 204, 117, 0.5)' },
              { offset: 1, color: 'rgba(145, 204, 117, 0.1)' }
            ])
          }
        }
      ]
    }
    memoryChart.setOption(memoryOption)
  }
  
  // 初始化网络流量图表
  if (networkChartRef.value && currentHost.value.networkTraffic) {
    networkChart = echarts.init(networkChartRef.value)
    const networkOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['入站流量', '出站流量']
      },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 10 }, (_, i) => `${i + 1}分钟前`).reverse()
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value} KB/s'
        }
      },
      series: [
        {
          name: '入站流量',
          data: currentHost.value.networkTraffic.input || [],
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#ee6666'
          }
        },
        {
          name: '出站流量',
          data: currentHost.value.networkTraffic.output || [],
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#73c0de'
          }
        }
      ]
    }
    networkChart.setOption(networkOption)
  }
}

// 监听弹窗关闭，销毁图表
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    disposeCharts()
  }
})

// 销毁图表
const disposeCharts = () => {
  if (cpuChart) {
    cpuChart.dispose()
    cpuChart = null
  }
  if (memoryChart) {
    memoryChart.dispose()
    memoryChart = null
  }
  if (networkChart) {
    networkChart.dispose()
    networkChart = null
  }
}

// 开发环境使用模拟数据
const mockHostData = () => {
  const mockHosts: Host[] = [
    {
      id: 1,
      hostname: 'server-01',
      ip: '192.168.1.101',
      os: 'CentOS 7.9',
      cpuModel: 'Intel Xeon E5-2680 v3',
      cpuCores: 12,
      memoryTotal: 32768, // 32GB
      diskTotal: 1048576, // 1TB
      status: 'online',
      lastUpdateTime: new Date().toISOString(),
      cpuUsage: [23, 25, 28, 22, 26, 30, 35, 43, 36, 30],
      memoryUsage: [40, 42, 45, 48, 50, 52, 55, 58, 56, 54],
      networkTraffic: {
        input: [120, 132, 101, 134, 90, 230, 210, 120, 132, 101],
        output: [110, 122, 91, 124, 80, 210, 190, 110, 122, 91]
      }
    },
    {
      id: 2,
      hostname: 'server-02',
      ip: '192.168.1.102',
      os: 'Ubuntu 20.04 LTS',
      cpuModel: 'AMD EPYC 7302',
      cpuCores: 16,
      memoryTotal: 65536, // 64GB
      diskTotal: 2097152, // 2TB
      status: 'warning',
      lastUpdateTime: new Date().toISOString(),
      cpuUsage: [56, 60, 65, 70, 75, 80, 85, 82, 78, 75],
      memoryUsage: [60, 62, 65, 70, 75, 80, 85, 82, 80, 75],
      networkTraffic: {
        input: [320, 332, 301, 334, 390, 430, 410, 320, 332, 301],
        output: [280, 312, 291, 324, 350, 410, 390, 310, 322, 291]
      }
    },
    {
      id: 3,
      hostname: 'server-03',
      ip: '192.168.1.103',
      os: 'Debian 11',
      cpuModel: 'Intel Xeon Gold 6230',
      cpuCores: 20,
      memoryTotal: 131072, // 128GB
      diskTotal: 4194304, // 4TB
      status: 'offline',
      lastUpdateTime: new Date().toISOString(),
      cpuUsage: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      memoryUsage: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
      networkTraffic: {
        input: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        output: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }
    },
    {
      id: 4,
      hostname: 'server-04',
      ip: '192.168.1.104',
      os: 'CentOS 8',
      cpuModel: 'Intel Xeon E5-2690 v4',
      cpuCores: 14,
      memoryTotal: 49152, // 48GB
      diskTotal: 3145728, // 3TB
      status: 'online',
      lastUpdateTime: new Date().toISOString(),
      cpuUsage: [25, 28, 30, 32, 28, 25, 22, 20, 19, 20],
      memoryUsage: [45, 48, 50, 52, 55, 53, 51, 49, 47, 45],
      networkTraffic: {
        input: [220, 232, 201, 234, 190, 180, 170, 160, 150, 140],
        output: [130, 142, 121, 154, 110, 100, 90, 80, 70, 60]
      }
    },
    {
      id: 5,
      hostname: 'server-05',
      ip: '192.168.1.105',
      os: 'Ubuntu 18.04 LTS',
      cpuModel: 'AMD EPYC 7282',
      cpuCores: 16,
      memoryTotal: 32768, // 32GB
      diskTotal: 1048576, // 1TB
      status: 'error',
      lastUpdateTime: new Date().toISOString(),
      cpuUsage: [95, 96, 98, 99, 97, 96, 95, 96, 97, 98],
      memoryUsage: [92, 93, 95, 96, 98, 97, 96, 95, 96, 97],
      networkTraffic: {
        input: [50, 52, 51, 53, 52, 50, 49, 48, 47, 46],
        output: [30, 31, 32, 30, 29, 28, 27, 26, 25, 24]
      }
    }
  ]
  
  hosts.value = mockHosts
  totalHosts.value = mockHosts.length
}

// 添加获取真实主机信息的方法
const fetchCurrentHostInfo = async () => {
  try {
    // 调用API获取当前主机信息
    const currentHost = await hostService.getCurrentHostInfo();
    
    // 判断是否已存在相同主机名的主机
    const existingHostIndex = hosts.value.findIndex(host => 
      host.hostname === currentHost.hostname && host.ip === currentHost.ip
    );
    
    if (existingHostIndex >= 0) {
      // 更新已存在的主机信息 - 保留原始ID
      const originalId = hosts.value[existingHostIndex].id;
      hosts.value[existingHostIndex] = { 
        ...currentHost, // Spread new info from API
        id: originalId   // Ensure original ID is kept
      };
      ElMessage.success('已更新本机主机信息');
    } else {
      // 添加到主机列表 - 不使用临时ID，而是通过API添加
      try {
        // 通过API正式添加主机
        const savedHost = await hostService.addHost(currentHost);
        // 使用后端返回的ID
        hosts.value.unshift(savedHost);
        totalHosts.value += 1;
        ElMessage.success('已添加本机主机信息');
      } catch (addError) {
        console.error('添加主机信息失败:', addError);
        ElMessage.warning('无法将主机信息保存到数据库，仅显示在当前页面');
        
        // 仅在前端显示，但不用于详情查看
        const tempHost = {...currentHost};
        // 使用特殊标记，表示这是一个临时主机
        tempHost.id = -1; 
        hosts.value.unshift(tempHost);
        totalHosts.value += 1;
      }
    }
  } catch (error) {
    console.error('获取当前主机信息失败:', error);
    ElMessage.error('获取当前主机信息失败，请检查网络连接和服务器状态');
  }
};

// 组件挂载
onMounted(() => {
  fetchHostsData()
  
  // 设置定时刷新
  timer = window.setInterval(() => {
    fetchHostsData()
  }, 60000) // 每分钟刷新一次
})

// 组件卸载
onUnmounted(() => {
  disposeCharts()
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style scoped>
.resource-hosts-container {
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

.header-operations {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.host-details {
  padding: 0 20px;
}

.chart-container {
  margin-top: 30px;
  margin-bottom: 20px;
}

.chart {
  height: 300px;
  width: 100%;
}

.el-descriptions {
  margin-bottom: 20px;
}
</style> 