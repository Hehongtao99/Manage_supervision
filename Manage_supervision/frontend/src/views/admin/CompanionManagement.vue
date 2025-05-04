<template>
  <div class="companion-management">
    <div class="page-header">
      <h2>陪玩管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入陪玩姓名/用户名"
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

    <!-- 陪玩列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="companionList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNumber" label="陪玩编号" width="120" />
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
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleViewPlayers(row)"
            >
              玩家管理
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

    <!-- 陪玩玩家管理对话框 -->
    <el-dialog
      title="陪玩玩家管理"
      v-model="playersDialogVisible"
      width="800px"
    >
      <div v-if="selectedCompanion" class="companion-info">
        <h3>{{ selectedCompanion.realName }} 的玩家列表</h3>
        
        <div class="action-bar">
          <el-button type="primary" @click="handleAssignPlayers">
            分配玩家
          </el-button>
        </div>
        
        <el-table
          v-loading="playersLoading"
          :data="companionPlayers"
          border
          style="width: 100%; margin-top: 15px;"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userNumber" label="玩家ID" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="danger"
                link
                @click="handleUnassignPlayer(row)"
              >
                取消分配
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-if="companionPlayers.length === 0" class="empty-data">
          该陪玩暂无分配的玩家
        </div>
      </div>
    </el-dialog>

    <!-- 分配玩家对话框 -->
    <el-dialog
      title="分配玩家"
      v-model="assignDialogVisible"
      width="800px"
    >
      <div class="assign-players">
        <el-form :inline="true" class="search-form">
          <el-form-item label="搜索">
            <el-input
              v-model="playerSearchKeyword"
              placeholder="请输入玩家姓名/ID"
              clearable
              @input="filterUnassignedPlayers"
            />
          </el-form-item>
        </el-form>
        
        <el-table
          v-loading="unassignedPlayersLoading"
          :data="filteredUnassignedPlayers"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userNumber" label="玩家ID" width="120" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="120" />
        </el-table>
        
        <div v-if="unassignedPlayers.length === 0" class="empty-data">
          没有未分配的玩家
        </div>
        
        <div class="dialog-footer">
          <el-button @click="assignDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="confirmAssignPlayers"
            :disabled="selectedPlayers.length === 0"
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
import { getCompanions, getCompanionPlayers, assignPlayersToCompanion, unassignPlayer } from '../../api/companion'
import { getUnassignedPlayers } from '../../api/player'
import type { Player } from '../../api/player'
import type { UserProfile } from '../../types/user'

// 状态
const loading = ref(false)
const playersLoading = ref(false)
const unassignedPlayersLoading = ref(false)
const assignLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const companionList = ref<UserProfile[]>([])
const playersDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const selectedCompanion = ref<UserProfile | null>(null)
const companionPlayers = ref<UserProfile[]>([])
const unassignedPlayers = ref<UserProfile[]>([])
const playerSearchKeyword = ref('')
const selectedPlayers = ref<UserProfile[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 计算属性
const filteredUnassignedPlayers = computed(() => {
  if (!playerSearchKeyword.value) {
    return unassignedPlayers.value
  }
  
  const keyword = playerSearchKeyword.value.toLowerCase()
  return unassignedPlayers.value.filter(player => 
    player.username.toLowerCase().includes(keyword) || 
    player.realName?.toLowerCase().includes(keyword) || 
    player.userNumber?.toLowerCase().includes(keyword)
  )
})

// 方法
const fetchCompanions = async () => {
  loading.value = true
  try {
    const response = await getCompanions(currentPage.value, pageSize.value, searchForm.keyword)
    companionList.value = response.content
    total.value = response.total
  } catch (error) {
    ElMessage.error('获取陪玩列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCompanionPlayers = async (companionId: number) => {
  playersLoading.value = true
  try {
    const players = await getCompanionPlayers(companionId)
    companionPlayers.value = players
  } catch (error) {
    ElMessage.error('获取陪玩玩家列表失败')
  } finally {
    playersLoading.value = false
  }
}

const fetchUnassignedPlayers = async () => {
  unassignedPlayersLoading.value = true
  try {
    const players = await getUnassignedPlayers()
    unassignedPlayers.value = players
  } catch (error) {
    ElMessage.error('获取未分配玩家列表失败')
  } finally {
    unassignedPlayersLoading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchCompanions()
}

const handleReset = () => {
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchCompanions()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchCompanions()
}

const handleViewPlayers = async (companion: UserProfile) => {
  selectedCompanion.value = companion
  playersDialogVisible.value = true
  await fetchCompanionPlayers(companion.id)
}

const handleAssignPlayers = async () => {
  if (!selectedCompanion.value) return
  
  assignDialogVisible.value = true
  selectedPlayers.value = []
  await fetchUnassignedPlayers()
}

const handleSelectionChange = (selection: UserProfile[]) => {
  selectedPlayers.value = selection
}

const filterUnassignedPlayers = () => {
  // 过滤由computed属性处理
}

const confirmAssignPlayers = async () => {
  if (!selectedCompanion.value || selectedPlayers.value.length === 0) return
  
  assignLoading.value = true
  try {
    const playerIds = selectedPlayers.value.map(player => player.id)
    const result = await assignPlayersToCompanion(selectedCompanion.value.id, playerIds)
    
    if (result.success) {
      ElMessage.success('分配玩家成功')
      assignDialogVisible.value = false
      await fetchCompanionPlayers(selectedCompanion.value.id)
    } else {
      ElMessage.error(result.message || '分配玩家失败')
    }
  } catch (error) {
    ElMessage.error('分配玩家失败')
  } finally {
    assignLoading.value = false
  }
}

const handleUnassignPlayer = async (player: UserProfile) => {
  if (!selectedCompanion.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要取消分配玩家 ${player.realName || player.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const result = await unassignPlayer(selectedCompanion.value.id, player.id)
    
    if (result.success) {
      ElMessage.success('取消分配成功')
      await fetchCompanionPlayers(selectedCompanion.value.id)
    } else {
      ElMessage.error(result.message || '取消分配失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('取消分配失败')
    }
  }
}

// 生命周期钩子
onMounted(() => {
  fetchCompanions()
})
</script>

<style scoped>
.companion-management {
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

.companion-info h3 {
  margin-top: 0;
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
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