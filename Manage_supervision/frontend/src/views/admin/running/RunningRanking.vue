<template>
  <div class="running-ranking">
    <div class="page-header">
      <h2>跑步排名</h2>
      <div class="date-filter">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :shortcuts="dateShortcuts"
          @change="handleDateChange"
        ></el-date-picker>
        <el-button type="primary" @click="fetchRankingData">查询</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabChange" class="ranking-tabs">
      <el-tab-pane label="距离排行" name="distance">
        <el-skeleton v-if="loading.distance" :rows="10" animated />

        <div v-else class="ranking-list">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>跑步距离排行榜</h3>
              </div>
            </template>
            <el-table :data="rankings.distance" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-cell">
                    <div v-if="scope.$index + 1 <= 3" :class="`rank-badge rank-top-${scope.$index + 1}`">
                      {{ scope.$index + 1 }}
                    </div>
                    <span v-else>{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="用户" min-width="200">
                <template #default="scope">
                  <div class="user-info">
                    <el-avatar :size="40" :src="scope.row.avatar">{{ scope.row.username?.charAt(0) }}</el-avatar>
                    <div class="user-detail">
                      <div class="username">{{ scope.row.realName || scope.row.username }}</div>
                      <div class="user-id">ID: {{ scope.row.userId }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="totalDistance" label="跑步距离(公里)" width="150" sortable />
              <el-table-column prop="runCount" label="跑步次数" width="150" sortable />
              <el-table-column label="平均距离(公里)" width="150">
                <template #default="scope">
                  {{ calculateAvgDistance(scope.row) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="次数排行" name="frequency">
        <el-skeleton v-if="loading.frequency" :rows="10" animated />

        <div v-else class="ranking-list">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>跑步次数排行榜</h3>
              </div>
            </template>
            <el-table :data="rankings.frequency" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-cell">
                    <div v-if="scope.$index + 1 <= 3" :class="`rank-badge rank-top-${scope.$index + 1}`">
                      {{ scope.$index + 1 }}
                    </div>
                    <span v-else>{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="用户" min-width="200">
                <template #default="scope">
                  <div class="user-info">
                    <el-avatar :size="40" :src="scope.row.avatar">{{ scope.row.username?.charAt(0) }}</el-avatar>
                    <div class="user-detail">
                      <div class="username">{{ scope.row.realName || scope.row.username }}</div>
                      <div class="user-id">ID: {{ scope.row.userId }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="runCount" label="跑步次数" width="150" sortable />
              <el-table-column prop="totalDistance" label="跑步距离(公里)" width="150" sortable />
              <el-table-column label="平均距离(公里)" width="150">
                <template #default="scope">
                  {{ calculateAvgDistance(scope.row) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="配速排行" name="pace">
        <el-skeleton v-if="loading.pace" :rows="10" animated />

        <div v-else class="ranking-list">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <h3>最佳配速排行榜</h3>
              </div>
            </template>
            <el-table :data="rankings.pace" style="width: 100%">
              <el-table-column label="排名" width="80">
                <template #default="scope">
                  <div class="rank-cell">
                    <div v-if="scope.$index + 1 <= 3" :class="`rank-badge rank-top-${scope.$index + 1}`">
                      {{ scope.$index + 1 }}
                    </div>
                    <span v-else>{{ scope.$index + 1 }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="用户" min-width="200">
                <template #default="scope">
                  <div class="user-info">
                    <el-avatar :size="40" :src="scope.row.avatar">{{ scope.row.username?.charAt(0) }}</el-avatar>
                    <div class="user-detail">
                      <div class="username">{{ scope.row.realName || scope.row.username }}</div>
                      <div class="user-id">ID: {{ scope.row.userId }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="bestPace" label="最佳配速" width="150" sortable />
              <el-table-column prop="runCount" label="跑步次数" width="150" sortable />
              <el-table-column prop="totalDistance" label="跑步距离(公里)" width="150" sortable />
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDistanceRanking, getFrequencyRanking, getBestPaceRanking } from '@/api/adminRunning'

const activeTab = ref('distance')
const dateRange = ref<[string, string]>(['', ''])
const rankLimit = ref(20)

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 加载状态
const loading = reactive({
  distance: true,
  frequency: true,
  pace: true
})

// 排名数据
const rankings = reactive({
  distance: [],
  frequency: [],
  pace: []
})

// 处理日期变化
const handleDateChange = (val: [string, string]) => {
  if (val) {
    dateRange.value = val
  }
}

// 处理标签页切换
const handleTabChange = (tab: any) => {
  const tabName = tab.props.name
  if (tabName && rankings[tabName].length === 0) {
    fetchTabData(tabName)
  }
}

// 计算平均距离
const calculateAvgDistance = (row: any) => {
  if (!row.runCount || row.runCount === 0) return '0.00'
  return (row.totalDistance / row.runCount).toFixed(2)
}

// 获取指定标签页的数据
const fetchTabData = async (tabName: string) => {
  loading[tabName] = true

  try {
    let startDate = dateRange.value[0]
    let endDate = dateRange.value[1]

    if (!startDate || !endDate) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      
      startDate = start.toISOString().split('T')[0]
      endDate = end.toISOString().split('T')[0]
      
      dateRange.value = [startDate, endDate]
    }

    let response
    
    switch (tabName) {
      case 'distance':
        response = await getDistanceRanking(startDate, endDate, rankLimit.value)
        break
      case 'frequency':
        response = await getFrequencyRanking(startDate, endDate, rankLimit.value)
        break
      case 'pace':
        response = await getBestPaceRanking(startDate, endDate, rankLimit.value)
        break
    }

    if (response && response.data && response.data.data) {
      rankings[tabName] = response.data.data
    }
  } catch (error) {
    console.error(`获取${tabName}排行榜数据失败:`, error)
    ElMessage.error(`获取排行榜数据失败`)
  } finally {
    loading[tabName] = false
  }
}

// 获取所有排行榜数据
const fetchRankingData = async () => {
  // 设置所有标签页为加载状态
  loading.distance = true
  loading.frequency = true
  loading.pace = true

  try {
    let startDate = dateRange.value[0]
    let endDate = dateRange.value[1]

    if (!startDate || !endDate) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      
      startDate = start.toISOString().split('T')[0]
      endDate = end.toISOString().split('T')[0]
      
      dateRange.value = [startDate, endDate]
    }

    // 使用Promise.all并行获取数据
    await Promise.all([
      getDistanceRanking(startDate, endDate, rankLimit.value)
        .then(res => {
          if (res.data && res.data.data) {
            rankings.distance = res.data.data
          }
          loading.distance = false
        }),
      getFrequencyRanking(startDate, endDate, rankLimit.value)
        .then(res => {
          if (res.data && res.data.data) {
            rankings.frequency = res.data.data
          }
          loading.frequency = false
        }),
      getBestPaceRanking(startDate, endDate, rankLimit.value)
        .then(res => {
          if (res.data && res.data.data) {
            rankings.pace = res.data.data
          }
          loading.pace = false
        })
    ])
  } catch (error) {
    console.error('获取排行榜数据失败:', error)
    ElMessage.error('获取排行榜数据失败')
    
    // 出错时也将加载状态设为false
    loading.distance = false
    loading.frequency = false
    loading.pace = false
  }
}

onMounted(() => {
  // 初始加载当前标签页的数据
  fetchTabData(activeTab.value)
})
</script>

<style scoped>
.running-ranking {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.date-filter {
  display: flex;
  gap: 10px;
}

.ranking-tabs {
  margin-top: 20px;
}

.ranking-list {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-detail {
  margin-left: 10px;
}

.username {
  font-weight: 500;
}

.user-id {
  font-size: 12px;
  color: #909399;
}

.rank-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.rank-badge {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-weight: bold;
}

.rank-top-1 {
  background-color: #f7c95c;
}

.rank-top-2 {
  background-color: #b4b8bc;
}

.rank-top-3 {
  background-color: #c18558;
}
</style> 