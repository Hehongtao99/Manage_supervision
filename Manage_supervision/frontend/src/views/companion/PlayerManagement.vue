<template>
  <div class="player-management">
    <!-- 如果没有玩家管理权限，显示提示信息 -->
    <el-empty 
      v-if="!hasPermission('PLAYER_MANAGEMENT')"
      description="您没有玩家管理权限"
    >
      <template #image>
        <el-icon style="font-size: 48px;"><Lock /></el-icon>
      </template>
    </el-empty>

    <!-- 有权限才显示玩家管理内容 -->
    <el-card class="box-card" v-if="hasPermission('PLAYER_MANAGEMENT')">
      <template #header>
        <div class="card-header">
          <span>玩家管理</span>
          <div class="search-container">
            <el-input
              v-model="searchQuery"
              placeholder="搜索玩家..."
              class="search-input"
              clearable
              @input="handleSearch"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="filteredPlayers"
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="姓名" width="120">
          <template #default="scope">
            {{ scope.row.realName || scope.row.name || scope.row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="userNumber" label="玩家ID" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="viewPlayerDetails(scope.row)"
              v-permission="'USER_VIEW'"
            >
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button 
              type="warning" 
              size="small"
              @click="togglePlayerStatus(scope.row)"
              v-permission="'USER_EDIT'"
            >
              <el-icon><Lock /></el-icon>
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
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
          :total="totalPlayers"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 玩家详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="玩家详情"
      width="60%"
      destroy-on-close
      class="player-detail-dialog"
    >
      <div v-if="currentPlayer" class="player-info-container">
        <div class="player-header">
          <div class="avatar-container">
            <el-avatar :size="80" :src="currentPlayer.avatar">
              {{ (currentPlayer.realName || currentPlayer.name || currentPlayer.username || '').substring(0, 1) }}
            </el-avatar>
          </div>
          <div class="player-main-info">
            <h2>{{ currentPlayer.realName || currentPlayer.name || currentPlayer.username }}</h2>
            <div class="player-id">玩家ID: {{ currentPlayer.userNumber }}</div>
          </div>
        </div>

        <el-divider />
        
        <!-- 玩家基本信息部分 - 所有权限用户都可查看 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentPlayer.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ currentPlayer.realName }}</el-descriptions-item>
          <el-descriptions-item label="玩家ID">{{ currentPlayer.userNumber }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentPlayer.nickname || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ currentPlayer.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentPlayer.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentPlayer.status === 'active' ? 'success' : 'warning'">
              {{ currentPlayer.status === 'active' ? '活跃' : '非活跃' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentPlayer.createTime || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="角色" :span="2">
            <el-tag 
              v-for="(role, index) in currentPlayer.roles" 
              :key="index" 
              type="info" 
              class="mr-2"
            >
              {{ role }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentPlayer.bio || '暂无个人简介' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 玩家进度部分 - 需要权限才能显示 -->
        <div v-if="hasPermission('PLAYER_PROGRESS_VIEW')" class="player-progress mt-4">
          <h3>游戏进度</h3>
          <el-divider />
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>总体游戏进度</span>
                  </div>
                </template>
                <el-progress :percentage="75" :stroke-width="20" :format="percentFormat" :color="getProgressColor(75)"></el-progress>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>最近活跃度</span>
                  </div>
                </template>
                <el-progress :percentage="60" :stroke-width="20" :format="percentFormat" :color="getProgressColor(60)"></el-progress>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" class="mt-4">
            <el-col :span="24">
              <el-card class="box-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>近期活动</span>
                  </div>
                </template>
                <el-timeline>
                  <el-timeline-item
                    timestamp="2023-05-10 20:46"
                    type="success"
                  >
                    完成游戏任务：《探索森林》
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-08 09:30"
                    type="primary"
                  >
                    参加在线游戏会话
                  </el-timeline-item>
                  <el-timeline-item
                    timestamp="2023-05-05 18:12"
                    type="warning"
                  >
                    延迟完成游戏任务：《解密古堡》
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  View,
  Edit,
  Delete,
  Lock
} from '@element-plus/icons-vue';
import { getPlayers, getPlayerDetail, updatePlayerStatus, deletePlayer, getAssignedPlayers } from '../../api/player';
import type { Player } from '../../api/user';
import { useRouter } from 'vue-router';
import { hasPermission } from '../../utils/permission';

// 状态变量
const loading = ref<boolean>(false);
const searchQuery = ref<string>('');
const currentPage = ref<number>(1);
const pageSize = ref<number>(10);
const totalPlayers = ref<number>(0);
const players = ref<Player[]>([]);
const selectedPlayers = ref<Player[]>([]);
const currentPlayer = ref<Player | null>(null);
const detailsDialogVisible = ref<boolean>(false);
const router = useRouter();

// 初始化数据
onMounted(() => {
  fetchPlayers();
});

// 获取玩家数据
const fetchPlayers = async () => {
  loading.value = true;
  try {
    const data = await getAssignedPlayers();
    console.log('获取的玩家数据:', JSON.stringify(data));
    
    if (Array.isArray(data)) {
      console.log('第一条玩家数据:', data.length > 0 ? JSON.stringify(data[0]) : '无数据');
      players.value = data;
      totalPlayers.value = data.length;
      if (data.length > 0) {
        ElMessage.success('成功获取玩家数据');
      } else {
        ElMessage.info('暂无分配的玩家');
      }
    } else if (data && typeof data === 'object' && Array.isArray(data.content)) {
      console.log('分页玩家数据第一条:', data.content.length > 0 ? JSON.stringify(data.content[0]) : '无数据');
      // 处理分页响应格式
      players.value = data.content;
      totalPlayers.value = data.totalElements || data.content.length;
      if (data.content.length > 0) {
        ElMessage.success('成功获取玩家数据');
      } else {
        ElMessage.info('暂无分配的玩家');
      }
    } else {
      console.error('返回的玩家数据格式不正确:', data);
      ElMessage.warning('获取的玩家数据格式不正确');
      players.value = [];
      totalPlayers.value = 0;
    }
  } catch (error) {
    console.error('获取玩家数据失败:', error);
    ElMessage.error('获取玩家数据失败，请稍后重试');
    players.value = [];
    totalPlayers.value = 0;
  } finally {
    loading.value = false;
  }
};

// 根据搜索过滤玩家
const filteredPlayers = computed(() => {
  if (!Array.isArray(players.value)) {
    console.warn('玩家数据不是数组:', players.value);
    return [];
  }
  
  if (!searchQuery.value) return players.value;
  
  const query = searchQuery.value.toLowerCase();
  return players.value.filter(player => 
    (player.realName && player.realName.toLowerCase().includes(query)) || 
    (player.name && player.name.toLowerCase().includes(query)) || 
    (player.username && player.username.toLowerCase().includes(query)) || 
    (player.userNumber && player.userNumber.toLowerCase().includes(query)) || 
    (player.email && player.email.toLowerCase().includes(query))
  );
});

// 处理搜索
const handleSearch = () => {
  // 搜索是通过计算属性实现的，无需额外操作
};

const handleSelectionChange = (selection: Player[]) => {
  selectedPlayers.value = selection;
};

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  fetchPlayers();
};

const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage;
  fetchPlayers();
};

const viewPlayerDetails = async (player: Player) => {
  // 检查权限
  if (!hasPermission('USER_VIEW')) {
    ElMessage.error('您没有查看玩家详情的权限');
    return;
  }
  
  try {
    loading.value = true;
    console.log('查看玩家详情, ID:', player.id);
    
    try {
      // 获取玩家详细信息
      const detailedPlayer = await getPlayerDetail(player.id);
      console.log('获取到的详细玩家信息:', detailedPlayer);
      
      // 确保所有必要的字段都存在
      const processedPlayer = {
        ...player,
        ...detailedPlayer,
        realName: detailedPlayer.realName || player.realName || player.name || '',
        username: detailedPlayer.username || player.username || '',
        userNumber: detailedPlayer.userNumber || player.userNumber || '',
        email: detailedPlayer.email || player.email || '',
        phone: detailedPlayer.phone || player.phone || '',
        status: detailedPlayer.status || player.status || 'inactive',
        createTime: detailedPlayer.createTime || player.createTime || '',
        roles: Array.isArray(detailedPlayer.roles) ? detailedPlayer.roles : 
              Array.isArray(player.roles) ? player.roles : ['USER'],
        bio: detailedPlayer.bio || ''
      };
      
      currentPlayer.value = processedPlayer;
      detailsDialogVisible.value = true;
    } catch (error) {
      // 如果getPlayerDetail失败，直接使用表格中的玩家数据显示详情
      console.log('使用当前玩家数据作为详情:', player);
      currentPlayer.value = {
        ...player,
        realName: player.realName || player.name || '',
        roles: Array.isArray(player.roles) ? player.roles : ['USER'],
        bio: ''
      };
      detailsDialogVisible.value = true;
    }
  } catch (error) {
    console.error('获取玩家详情失败:', error);
    ElMessage.error('获取玩家详情失败');
  } finally {
    loading.value = false;
  }
};

const togglePlayerStatus = async (player: Player) => {
  // 检查权限
  if (!hasPermission('USER_EDIT')) {
    ElMessage.error('您没有修改玩家状态的权限');
    return;
  }
  
  try {
    const newStatus = player.status === 'active' ? 'inactive' : 'active';
    const success = await updatePlayerStatus(player.id, newStatus);
    
    if (success) {
      ElMessage.success(`玩家${player.name}状态已${newStatus === 'active' ? '启用' : '禁用'}`);
      // 更新本地数据
      const index = players.value.findIndex(s => s.id === player.id);
      if (index !== -1) {
        players.value[index].status = newStatus;
      }
    } else {
      ElMessage.error('更新玩家状态失败');
    }
  } catch (error) {
    console.error('更新玩家状态失败:', error);
    ElMessage.error('更新玩家状态失败');
  }
};

const deletePlayerUser = async (player: Player) => {
  // 检查权限
  if (!hasPermission('USER_DELETE')) {
    ElMessage.error('您没有删除玩家的权限');
    return;
  }
  
  try {
    const success = await deletePlayer(player.id);
    
    if (success) {
      ElMessage.success(`玩家${player.name}已删除`);
      // 从本地数据中移除
      players.value = players.value.filter(s => s.id !== player.id);
      totalPlayers.value = players.value.length;
    } else {
      ElMessage.error('删除玩家失败');
    }
  } catch (error) {
    console.error('删除玩家失败:', error);
    ElMessage.error('删除玩家失败');
  }
};

const percentFormat = (percentage: number) => {
  return percentage + '%';
};

const getProgressColor = (percentage: number) => {
  if (percentage < 30) {
    return '#f56c6c';
  } else if (percentage < 70) {
    return '#e6a23c';
  } else {
    return '#67c23a';
  }
};
</script>

<style scoped>
.player-management {
  padding: 20px;
}

.box-card {
  width: 100%;
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
  width: 300px;
  margin-right: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.player-detail-dialog {
  /* Add your styles here */
}

.player-info-container {
  padding: 0 20px;
}

.player-header {
  display: flex;
  margin-bottom: 20px;
}

.avatar-container {
  margin-right: 20px;
}

.player-main-info {
  flex: 1;
}

.player-main-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.player-id {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}

.player-roles {
  margin-top: 12px;
}

.mt-4 {
  margin-top: 1rem;
}

.mr-2 {
  margin-right: 0.5rem;
}
</style> 