<template>
  <div class="player-management">
    <div class="page-header">
      <h2>玩家管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入玩家姓名/用户名/ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 玩家列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="playerList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="玩家ID" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'active' ? 'success' : 'danger'"
            >
              {{ row.status === 'active' ? '活跃' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="陪玩分配" width="120">
          <template #default="{ row }">
            <span v-if="row.companionName">{{ row.companionName }}</span>
            <span v-else class="not-assigned">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleAssignToCompanion(row)"
              :disabled="Boolean(row.companionName)"
            >
              分配陪玩
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 分配陪玩对话框 -->
    <el-dialog
      title="分配陪玩"
      v-model="assignDialogVisible"
      width="600px"
    >
      <div class="assign-companion">
        <el-form :inline="true" class="search-form">
          <el-form-item label="搜索">
            <el-input
              v-model="companionSearchKeyword"
              placeholder="请输入陪玩姓名/ID"
              clearable
              @input="filterCompanions"
            />
          </el-form-item>
        </el-form>
        
        <el-table
          v-loading="companionsLoading"
          :data="filteredCompanions"
          border
          style="width: 100%"
          @row-click="handleSelectCompanion"
          highlight-current-row
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userNumber" label="陪玩ID" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
        </el-table>
        
        <div v-if="companionList.length === 0" class="empty-data">
          没有可用的陪玩
        </div>
        
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="confirmAssignCompanion"
            :disabled="!selectedCompanion"
            :loading="assignLoading"
          >
            确认分配
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import {
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlayers, updatePlayerStatus } from '../../api/player'
import { getCompanions, assignPlayersToCompanion } from '../../api/companion'
import type { UserProfile } from '../../types/user'

// 状态
const loading = ref(false)
const companionsLoading = ref(false)
const assignLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const playerList = ref<UserProfile[]>([])
const assignDialogVisible = ref(false)
const selectedPlayer = ref<UserProfile | null>(null)
const companionList = ref<UserProfile[]>([])
const selectedCompanion = ref<UserProfile | null>(null)
const companionSearchKeyword = ref('')

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 计算属性
const filteredCompanions = computed(() => {
  if (!companionSearchKeyword.value) {
    return companionList.value
  }
  
  const keyword = companionSearchKeyword.value.toLowerCase()
  return companionList.value.filter(companion => 
    companion.username.toLowerCase().includes(keyword) || 
    companion.realName?.toLowerCase().includes(keyword) || 
    companion.userNumber?.toLowerCase().includes(keyword)
  )
})

// 方法
const fetchPlayers = async () => {
  loading.value = true
  try {
    const response = await getPlayers(currentPage.value, pageSize.value, searchForm.keyword)
    playerList.value = response.content
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取玩家列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCompanions = async () => {
  companionsLoading.value = true
  try {
    const response = await getCompanions(1, 100) // 获取较多陪玩以便选择
    companionList.value = response.content
  } catch (error) {
    ElMessage.error('获取陪玩列表失败')
  } finally {
    companionsLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPlayers()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchPlayers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchPlayers()
}

const handleToggleStatus = async (player: UserProfile) => {
  try {
    await ElMessageBox.confirm(
      `确定要${player.status === 'active' ? '禁用' : '启用'}玩家 ${player.realName || player.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updatePlayerStatus(player.id, player.status === 'active' ? 'inactive' : 'active')
    ElMessage.success(`${player.status === 'active' ? '禁用' : '启用'}成功`)
    
    // 更新列表中的状态
    const index = playerList.value.findIndex(p => p.id === player.id)
    if (index !== -1) {
      playerList.value[index].status = player.status === 'active' ? 'inactive' : 'active'
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleAssignToCompanion = async (player: UserProfile) => {
  selectedPlayer.value = player
  assignDialogVisible.value = true
  selectedCompanion.value = null
  await fetchCompanions()
}

const handleSelectCompanion = (companion: UserProfile) => {
  selectedCompanion.value = companion
}

const filterCompanions = () => {
  // 过滤由computed属性处理
}

const confirmAssignCompanion = async () => {
  if (!selectedPlayer.value || !selectedCompanion.value) return
  
  assignLoading.value = true
  try {
    const result = await assignPlayersToCompanion(selectedCompanion.value.id, [selectedPlayer.value.id])
    
    if (result.success) {
      ElMessage.success('分配陪玩成功')
      assignDialogVisible.value = false
      
      // 更新列表中的陪玩信息
      const index = playerList.value.findIndex(p => p.id === selectedPlayer.value?.id)
      if (index !== -1) {
        playerList.value[index].companionName = selectedCompanion.value.realName || selectedCompanion.value.username
        playerList.value[index].companionId = selectedCompanion.value.id
      }
    } else {
      ElMessage.error(result.message || '分配陪玩失败')
    }
  } catch (error) {
    ElMessage.error('分配陪玩失败')
  } finally {
    assignLoading.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchPlayers()
})
</script>

<style scoped>
.player-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.not-assigned {
  color: #999;
  font-style: italic;
}

.empty-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 