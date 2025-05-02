<template>
  <div class="intrusion-detection-container">
    <el-card class="detection-card">
      <template #header>
        <div class="card-header">
          <h2>入侵检测</h2>
          <el-button type="primary" @click="startDetection" :loading="isDetecting">
            开始检测
          </el-button>
        </div>
      </template>
      
      <div class="detection-content">
        <el-form :model="detectionForm" label-width="120px">
          <el-form-item label="检测类型">
            <el-select v-model="detectionForm.detectionType" placeholder="请选择检测类型">
              <el-option label="全面检测" value="full" />
              <el-option label="CPU异常" value="cpu" />
              <el-option label="内存异常" value="memory" />
              <el-option label="网络异常" value="network" />
              <el-option label="磁盘异常" value="disk" />
              <el-option label="进程异常" value="process" />
              <el-option label="系统日志" value="logs" />
              <el-option label="端口扫描" value="ports" />
              <el-option label="文件完整性" value="files" />
              <el-option label="用户活动" value="users" />
              <el-option label="系统服务" value="services" />
              <el-option label="注册表检测" value="registry" />
              <el-option label="恶意软件" value="malware" />
            </el-select>
          </el-form-item>
        </el-form>
        
        <div class="detection-status" v-if="detectionStatus">
          <el-alert
            :title="detectionStatus.title"
            :type="detectionStatus.type"
            :description="detectionStatus.description"
            show-icon
          />
        </div>
        
        <div class="detection-result" v-if="detectionResults.length > 0">
          <h3>检测结果</h3>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="异常列表" name="list">
              <el-table :data="detectionResults" style="width: 100%" border stripe height="500px">
                <el-table-column prop="dataType" label="异常类型" width="140" />
                <el-table-column prop="value" label="实际值" width="100" />
                <el-table-column prop="expectedRange" label="预期范围" width="120" />
                <el-table-column prop="deviation" label="偏差率(%)" width="100">
                  <template #default="scope">
                    {{ Math.round(scope.row.deviation * 100) / 100 }}%
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="异常描述" />
                <el-table-column prop="recordTime" label="检测时间" width="180">
                  <template #default="scope">
                    {{ new Date(scope.row.recordTime).toLocaleString() }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button 
                      type="text" 
                      @click="markAsResolved(scope.row.id)"
                      :disabled="scope.row.resolved"
                    >
                      {{ scope.row.resolved ? '已处理' : '标记处理' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="图表分析" name="chart">
              <div class="chart-container">
                <div id="typeChart" class="chart" style="position: relative; visibility: visible;"></div>
                <div id="severityChart" class="chart" style="position: relative; visibility: visible;"></div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { detectIntrusion, addDetectionToMonitor, getRecentDetections } from '../api/intrusionDetection'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()

const detectionForm = reactive({
  detectionType: 'full'
})

const isDetecting = ref(false)
const detectionStatus = ref(null)
const detectionResults = ref([])
const activeTab = ref('list')

// 图表实例
let typeChart = null
let severityChart = null

// 开始检测
const startDetection = async () => {
  try {
    isDetecting.value = true
    detectionStatus.value = {
      title: '检测中',
      type: 'info',
      description: `正在执行${getDetectionTypeName(detectionForm.detectionType)}，请稍候...`
    }
    
    // 调用后端API
    const res = await detectIntrusion({
      type: detectionForm.detectionType
    })
    
    // 处理返回结果
    if (res.data && res.data.anomalies) {
      detectionResults.value = res.data.anomalies.map(item => ({
        ...item,
        expectedRange: `${item.expectedMin} - ${item.expectedMax}`,
      }))
      
      if (detectionResults.value.length > 0) {
        detectionStatus.value = {
          title: '检测完成',
          type: 'warning',
          description: `检测到${detectionResults.value.length}个异常项，请查看详情并处理`
        }
        // 等待DOM更新后绘制图表
        setTimeout(() => {
          renderCharts()
        }, 100)
      } else {
        detectionStatus.value = {
          title: '检测完成',
          type: 'success',
          description: '未检测到异常情况，系统运行正常'
        }
      }
    }
  } catch (error) {
    console.error('检测出错:', error)
    detectionStatus.value = {
      title: '检测失败',
      type: 'error',
      description: error.message || '检测过程中发生错误，请稍后重试'
    }
  } finally {
    isDetecting.value = false
  }
}

// 渲染图表
const renderCharts = () => {
  // 按类型分组
  const typeGroups = {}
  detectionResults.value.forEach(item => {
    if (!typeGroups[item.dataType]) {
      typeGroups[item.dataType] = 0
    }
    typeGroups[item.dataType]++
  })
  
  // 按严重程度分组
  const severityGroups = {
    '严重': 0,
    '中等': 0,
    '轻微': 0
  }
  
  detectionResults.value.forEach(item => {
    if (item.deviation > 80) {
      severityGroups['严重']++
    } else if (item.deviation > 40) {
      severityGroups['中等']++
    } else {
      severityGroups['轻微']++
    }
  })
  
  // 确保DOM已渲染并且有正确的尺寸
  nextTick(() => {
    // 销毁旧的图表实例（如果存在）
    if (typeChart) {
      typeChart.dispose()
    }
    if (severityChart) {
      severityChart.dispose()
    }
    
    // 重新初始化图表
    typeChart = echarts.init(document.getElementById('typeChart'))
    severityChart = echarts.init(document.getElementById('severityChart'))
    
    // 设置类型图表配置
    typeChart.setOption({
      title: {
        text: '异常类型分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: Object.keys(typeGroups)
      },
      series: [
        {
          name: '异常类型',
          type: 'pie',
          radius: '55%',
          center: ['50%', '60%'],
          data: Object.keys(typeGroups).map(key => ({
            name: key,
            value: typeGroups[key]
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
    
    // 设置严重程度图表配置
    severityChart.setOption({
      title: {
        text: '异常严重程度分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: Object.keys(severityGroups)
      },
      series: [
        {
          name: '严重程度',
          type: 'pie',
          radius: '55%',
          center: ['50%', '60%'],
          data: Object.keys(severityGroups).map(key => ({
            name: key,
            value: severityGroups[key]
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
    
    // 强制重新计算尺寸
    typeChart.resize()
    severityChart.resize()
  })
}

// 查看详情
const viewDetail = (row) => {
  ElMessageBox.alert(
    `<div>
      <p><strong>数据类型:</strong> ${row.dataType}</p>
      <p><strong>异常值:</strong> ${row.value}</p>
      <p><strong>预期范围:</strong> ${row.expectedRange}</p>
      <p><strong>偏差率:</strong> ${row.deviation}%</p>
      <p><strong>记录时间:</strong> ${row.recordTime}</p>
      <p><strong>描述:</strong> ${row.description || '无'}</p>
      <p><strong>来源:</strong> ${row.source || '系统检测'}</p>
    </div>`,
    '异常详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 添加到数据监控
const addToMonitor = async () => {
  try {
    await addDetectionToMonitor(detectionResults.value)
    ElMessage.success('已成功添加到数据监控')
    
    // 询问是否前往数据监控页面
    ElMessageBox.confirm(
      '异常数据已添加到数据监控分析，是否立即查看?',
      '操作成功',
      {
        confirmButtonText: '前往查看',
        cancelButtonText: '稍后查看',
        type: 'success'
      }
    )
    .then(() => {
      router.push('/data-monitor')
    })
    .catch(() => {
      // 用户选择稍后查看，不做任何操作
    })
  } catch (error) {
    console.error('添加到监控失败:', error)
    ElMessage.error('添加到数据监控失败')
  }
}

// 导出结果
const exportResults = () => {
  // 创建一个Blob对象
  const jsonStr = JSON.stringify(detectionResults.value, null, 2)
  const blob = new Blob([jsonStr], { type: 'application/json' })
  
  // 创建下载链接
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `intrusion_detection_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 获取检测类型名称
const getDetectionTypeName = (type) => {
  const typeMap = {
    'full': '全面检测',
    'cpu': 'CPU异常检测',
    'memory': '内存异常检测',
    'network': '网络异常检测',
    'disk': '磁盘异常检测',
    'process': '进程异常检测',
    'logs': '系统日志检测',
    'ports': '端口扫描检测',
    'files': '文件完整性检测',
    'users': '用户活动检测',
    'services': '系统服务检测',
    'registry': '注册表检测',
    'malware': '恶意软件检测'
  }
  return typeMap[type] || type
}

// 标记为已处理
const markAsResolved = async (id) => {
  // 这里应该调用后端API标记为已处理
  // 暂时只做前端状态更新示例
  const index = detectionResults.value.findIndex(item => item.id === id)
  if (index !== -1) {
    detectionResults.value[index].resolved = true
  }
}

// 监听标签页切换
watch(activeTab, (newVal) => {
  if (newVal === 'chart' && detectionResults.value.length > 0) {
    // 添加一个小延迟以确保DOM已完全渲染
    setTimeout(() => {
      renderCharts()
    }, 50)
  }
})

// 处理窗口大小变化
onMounted(() => {
  // 窗口大小变化时重新计算图表尺寸
  const resizeHandler = () => {
    if (typeChart) {
      typeChart.resize()
    }
    if (severityChart) {
      severityChart.resize()
    }
  }
  
  window.addEventListener('resize', resizeHandler)
  
  // 组件卸载时移除事件监听
  onUnmounted(() => {
    window.removeEventListener('resize', resizeHandler)
    // 销毁图表实例
    if (typeChart) {
      typeChart.dispose()
    }
    if (severityChart) {
      severityChart.dispose()
    }
  })

  // 初始化获取所有的检测记录
  getRecentDetections(0).then(res => {  // 传入0表示获取所有记录
    if (res.data && res.data.length > 0) {
      // 将检测结果加载到界面中
      detectionResults.value = res.data.map(item => ({
        ...item,
        expectedRange: `${item.expectedMin} - ${item.expectedMax}`,
      }));
      
      // 如果有异常数据，设置状态提示
      if (detectionResults.value.length > 0) {
        detectionStatus.value = {
          title: '检测完成',
          type: 'warning',
          description: `检测到${detectionResults.value.length}个异常项，请查看详情并处理`
        }
        
        // 主动切换到图表标签页以触发图表渲染
        activeTab.value = 'chart';
        
        // 等待DOM更新完成后渲染图表
        setTimeout(() => {
          renderCharts();
        }, 100);
      }
    }
  }).catch(error => {
    console.error('获取检测记录失败:', error)
  })
})
</script>

<style scoped>
.intrusion-detection-container {
  padding: 20px;
}

.detection-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detection-content {
  margin-top: 20px;
}

.detection-status {
  margin: 20px 0;
}

.detection-result {
  margin-top: 30px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.chart-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 20px;
  min-height: 400px;
  width: 100%;
}

.chart {
  width: 45%;
  height: 400px;
  margin: 20px 0;
  min-width: 300px;
}

@media (max-width: 768px) {
  .chart {
    width: 100%;
  }
}
</style> 