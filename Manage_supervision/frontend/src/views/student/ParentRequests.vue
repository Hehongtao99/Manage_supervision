<template>
  <div class="parent-requests-container">
    <div class="page-header">
      <h2>家长绑定请求</h2>
    </div>

    <el-card class="request-list">
      <template #header>
        <div class="card-header">
          <span>请求列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadParentRelations">刷新</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="pendingRelations.length === 0" description="暂无家长绑定请求" />

      <div v-else>
        <el-table :data="pendingRelations" style="width: 100%">
          <el-table-column prop="parentName" label="家长姓名" min-width="120" />
          
          <el-table-column prop="parentUsername" label="家长账号" min-width="150" />
          
          <el-table-column prop="relationType" label="关系类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">
                {{ getRelationLabel(row.relationType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="handleRelation(row.id, 'confirmed')">
                接受
              </el-button>
              <el-button type="danger" size="small" @click="handleRelation(row.id, 'rejected')">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card class="confirmed-list mt-4">
      <template #header>
        <div class="card-header">
          <span>已绑定家长</span>
        </div>
      </template>

      <el-empty v-if="confirmedRelations.length === 0" description="暂无已绑定家长" />

      <div v-else>
        <el-table :data="confirmedRelations" style="width: 100%">
          <el-table-column prop="parentName" label="家长姓名" min-width="120" />
          
          <el-table-column prop="parentUsername" label="家长账号" min-width="150" />
          
          <el-table-column prop="relationType" label="关系类型" width="100">
            <template #default="{ row }">
              <el-tag type="success" size="small">
                {{ getRelationLabel(row.relationType) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="createTime" label="绑定时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from '@/utils/axios';

interface ParentRelation {
  id: number;
  parentId: number;
  parentName: string;
  parentUsername: string;
  relationType: string;
  status: string;
  createTime: string;
}

// 数据列表
const parentRelations = ref<ParentRelation[]>([]);
const loading = ref(false);

// 计算属性：过滤出待处理的请求
const pendingRelations = computed(() => {
  return parentRelations.value.filter(relation => relation.status === 'pending');
});

// 计算属性：过滤出已确认的关系
const confirmedRelations = computed(() => {
  return parentRelations.value.filter(relation => relation.status === 'confirmed');
});

// 初始化数据
onMounted(async () => {
  await loadParentRelations();
});

// 加载家长关系列表
const loadParentRelations = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/student/parent-relations');
    parentRelations.value = response.data.relations || [];
  } catch (error) {
    console.error('获取家长绑定请求失败', error);
    ElMessage.error('获取家长绑定请求失败');
  } finally {
    loading.value = false;
  }
};

// 处理关系请求（接受/拒绝）
const handleRelation = async (relationId: number, status: 'confirmed' | 'rejected') => {
  const action = status === 'confirmed' ? '接受' : '拒绝';
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}该家长的绑定请求吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const response = await axios.put(`/api/student/parent-relations/${relationId}`, { status });
    ElMessage.success(response.data.message || `已${action}家长绑定请求`);
    await loadParentRelations();
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}家长绑定请求失败`, error);
      ElMessage.error(error.response?.data?.message || `${action}家长绑定请求失败`);
    }
  }
};

// 获取关系类型标签
const getRelationLabel = (relationType: string): string => {
  const typeMap: Record<string, string> = {
    'father': '父亲',
    'mother': '母亲',
    'guardian': '监护人'
  };
  return typeMap[relationType] || relationType;
};

// 格式化时间显示
const formatTime = (timestamp: string) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>

<style scoped>
.parent-requests-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
}

.mt-4 {
  margin-top: 20px;
}
</style> 