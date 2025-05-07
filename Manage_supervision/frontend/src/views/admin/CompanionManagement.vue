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
              @click="handleViewDetails(row)"
            >
              查看详情
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCompanions } from '../../api/companion'
import type { UserProfile } from '../../types/user'

// 状态
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const companionList = ref<UserProfile[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: ''
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

const handleViewDetails = (companion: UserProfile) => {
  // 查看陪玩详情的实现
  ElMessage.info(`查看陪玩详情: ${companion.realName || companion.username}`)
}

const handleToggleStatus = async (companion: UserProfile) => {
  try {
    await ElMessageBox.confirm(
      `确定要${companion.status === 'active' ? '禁用' : '启用'}陪玩 ${companion.realName || companion.username} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里需要实现状态切换的API调用
    ElMessage.success(`${companion.status === 'active' ? '禁用' : '启用'}陪玩成功`)
    
    // 刷新列表
    fetchCompanions()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
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
</style> 