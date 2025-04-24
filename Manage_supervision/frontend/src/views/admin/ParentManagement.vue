<template>
  <div class="parent-management-container">
    <el-card class="page-header">
      <div class="page-title">
        <h1>家长管理</h1>
        <div class="actions">
          <el-button type="primary" @click="showCreateParentDialog">添加家长</el-button>
        </div>
      </div>
    </el-card>

    <!-- 家长列表 -->
    <el-card class="mt-4">
      <div class="filter-container mb-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索家长"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="parentList.length === 0" class="no-data">
        <el-empty description="暂无家长数据" />
      </div>
      <el-table :data="parentList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="userNumber" label="编号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '活动' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="showChildrenDialog(scope.row)"
            >
              管理子女
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="showAddChildDialog(scope.row)"
            >
              添加子女
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="toggleParentStatus(scope.row)"
            >
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          :layout="'total, sizes, prev, pager, next, jumper'"
          :total="totalParents"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-sizes="[10, 20, 50, 100]"
        />
      </div>
    </el-card>

    <!-- 创建家长对话框 -->
    <el-dialog v-model="createParentDialogVisible" title="创建家长" width="500px">
      <el-form
        :model="parentForm"
        :rules="parentRules"
        ref="parentFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="parentForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="parentForm.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="parentForm.realName" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="parentForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="parentForm.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="parentForm.phone" />
        </el-form-item>
        <el-form-item label="个人简介" prop="bio">
          <el-input v-model="parentForm.bio" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createParentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createParent" :loading="submitting">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 子女列表对话框 -->
    <el-dialog
      v-model="childrenDialogVisible"
      :title="`${currentParent?.realName || ''} 的子女列表`"
      width="800px"
    >
      <el-table :data="childrenList" v-loading="childrenLoading" style="width: 100%">
        <el-table-column prop="childName" label="姓名" />
        <el-table-column prop="childUsername" label="用户名" />
        <el-table-column prop="relationType" label="关系">
          <template #default="scope">
            {{ getRelationTypeLabel(scope.row.relationType) }}
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              link
              @click="removeChildRelation(scope.row)"
            >
              解除关系
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="childrenList.length === 0" class="no-data">
        <el-empty description="暂无子女关系" />
      </div>
    </el-dialog>

    <!-- 添加子女对话框 -->
    <el-dialog
      v-model="addChildDialogVisible"
      :title="`为 ${currentParent?.realName || ''} 添加子女`"
      width="500px"
    >
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="学生确认机制"
        description="添加子女后，系统会向学生发送绑定请求，学生需要确认后才能建立关联关系。"
        style="margin-bottom: 15px;"
      />
      <el-form
        :model="childRelationForm"
        :rules="childRelationRules"
        ref="childRelationFormRef"
        label-width="100px"
      >
        <el-form-item label="子女标识" prop="identifier">
          <el-input v-model="childRelationForm.identifier" placeholder="输入学生用户名或学号" />
        </el-form-item>
        <el-form-item label="关系类型" prop="relation">
          <el-select v-model="childRelationForm.relation" placeholder="选择关系类型" style="width: 100%">
            <el-option label="父亲" value="father" />
            <el-option label="母亲" value="mother" />
            <el-option label="监护人" value="guardian" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addChildDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="assignChildToParent"
            :loading="assigningChild"
          >
            确认添加
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import * as parentApi from '@/api/parent'
import * as adminApi from '@/api/user'
import type { UserDTO, ParentData, ParentChildRelationData } from '@/types/user'

// 家长列表数据
const loading = ref(false)
const parentList = ref<UserDTO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalParents = ref(0)
const searchKeyword = ref('')

// 子女列表数据
const childrenDialogVisible = ref(false)
const childrenList = ref<ParentChildRelationData[]>([])
const childrenLoading = ref(false)
const currentParent = ref<UserDTO | null>(null)

// 创建家长表单
const createParentDialogVisible = ref(false)
const parentFormRef = ref<FormInstance>()
const submitting = ref(false)
const parentForm = reactive<ParentData>({
  username: '',
  password: '',
  realName: '',
  nickname: '',
  email: '',
  phone: '',
  bio: ''
})

const parentRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

// 添加子女表单
const addChildDialogVisible = ref(false)
const childRelationFormRef = ref<FormInstance>()
const assigningChild = ref(false)
const childRelationForm = reactive({
  identifier: '',
  relation: ''
})

const childRelationRules = {
  identifier: [{ required: true, message: '请输入学生用户名或学号', trigger: 'blur' }],
  relation: [{ required: true, message: '请选择关系类型', trigger: 'change' }]
}

// 加载家长列表
const loadParentList = async () => {
  loading.value = true
  try {
    console.log('开始请求家长列表，参数：', {
      page: currentPage.value,
      size: pageSize.value,
      role: 'PARENT', // 确认角色参数
      keyword: searchKeyword.value
    })
    
    const res = await parentApi.getParentList(currentPage.value, pageSize.value, searchKeyword.value)
    // 检查响应数据格式
    console.log('家长列表数据:', res)
    
    // 验证返回的用户是否都有PARENT角色
    let allAreParents = true
    if (res && Array.isArray(res.records)) {
      res.records.forEach((user, index) => {
        if (!user.roles.includes('PARENT')) {
          console.error(`用户 #${index} (${user.username}) 不是家长角色:`, user.roles)
          allAreParents = false
        }
      })
      if (!allAreParents) {
        console.warn('返回的列表中包含非家长角色的用户！')
      } else {
        console.log('所有返回的用户都有PARENT角色')
      }
      
      parentList.value = res.records
      totalParents.value = res.total || 0
    } else if (res && Array.isArray(res.content)) {
      // 兼容不同的返回格式
      parentList.value = res.content
      totalParents.value = res.totalElements || 0
    } else if (res && Array.isArray(res)) {
      // 直接返回数组的情况
      parentList.value = res
      totalParents.value = res.length
    } else {
      console.error('无法识别的家长列表数据格式:', res)
      parentList.value = []
      totalParents.value = 0
    }
  } catch (error) {
    console.error('加载家长列表失败:', error)
    ElMessage.error('加载家长列表失败')
    parentList.value = []
    totalParents.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadParentList()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadParentList()
}

// 分页事件处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadParentList()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadParentList()
}

// 显示创建家长对话框
const showCreateParentDialog = () => {
  createParentDialogVisible.value = true
  // 重置表单
  if (parentFormRef.value) {
    parentFormRef.value.resetFields()
  }
}

// 创建家长
const createParent = async () => {
  if (!parentFormRef.value) return

  await parentFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await parentApi.createParent(parentForm)
        ElMessage.success('创建家长成功')
        createParentDialogVisible.value = false
        loadParentList()
      } catch (error) {
        console.error('创建家长失败:', error)
        ElMessage.error('创建家长失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 切换家长状态
const toggleParentStatus = async (parent: UserDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要${parent.status === 'active' ? '禁用' : '启用'}该家长账户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await adminApi.toggleUserStatus(parent.id)
    ElMessage.success(`操作成功`)
    loadParentList()
  } catch (error) {
    console.error('操作失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 显示子女列表对话框
const showChildrenDialog = async (parent: UserDTO) => {
  currentParent.value = parent
  childrenDialogVisible.value = true
  await loadChildrenList(parent.id)
}

// 加载子女列表
const loadChildrenList = async (parentId: number) => {
  childrenLoading.value = true
  try {
    const res = await parentApi.getParentChildren(parentId)
    console.log('子女列表数据:', res)
    
    // 检查返回数据格式并适配
    if (res && Array.isArray(res.relations)) {
      childrenList.value = res.relations
    } else if (res && Array.isArray(res)) {
      childrenList.value = res
    } else {
      console.error('无法识别的子女列表数据格式:', res)
      childrenList.value = []
    }
  } catch (error) {
    console.error('加载子女列表失败:', error)
    ElMessage.error('加载子女列表失败')
    childrenList.value = []
  } finally {
    childrenLoading.value = false
  }
}

// 获取关系类型标签
const getRelationTypeLabel = (type: string) => {
  const types = {
    father: '父亲',
    mother: '母亲',
    guardian: '监护人'
  }
  return types[type as keyof typeof types] || type
}

// 获取状态标签
const getStatusLabel = (status: string): string => {
  const statuses: Record<string, string> = {
    'pending': '等待确认',
    'confirmed': '已确认',
    'rejected': '已拒绝'
  }
  return statuses[status] || status
}

// 获取状态标签类型
const getStatusType = (status: string): string => {
  const types: Record<string, string> = {
    'pending': 'warning',
    'confirmed': 'success',
    'rejected': 'danger'
  }
  return types[status] || 'info'
}

// 解除关系
const removeChildRelation = async (relation: ParentChildRelationData) => {
  try {
    await ElMessageBox.confirm('确定要解除该家长和子女的关系吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await parentApi.removeParentChildRelation(relation.id)
    ElMessage.success('解除关系成功')
    if (currentParent.value) {
      loadChildrenList(currentParent.value.id)
    }
  } catch (error) {
    console.error('解除关系失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('解除关系失败')
    }
  }
}

// 显示添加子女对话框
const showAddChildDialog = (parent: UserDTO) => {
  currentParent.value = parent
  addChildDialogVisible.value = true
  // 重置表单
  childRelationForm.identifier = ''
  childRelationForm.relation = ''
  if (childRelationFormRef.value) {
    childRelationFormRef.value.resetFields()
  }
}

// 分配子女给家长
const assignChildToParent = async () => {
  if (!childRelationFormRef.value || !currentParent.value) return

  await childRelationFormRef.value.validate(async (valid) => {
    if (valid) {
      assigningChild.value = true
      try {
        await parentApi.assignChildToParent(
          currentParent.value.id,
          childRelationForm.identifier,
          childRelationForm.relation
        )
        ElMessage.success('添加子女成功，请等待学生确认绑定请求')
        addChildDialogVisible.value = false
        // 如果子女列表对话框是打开的，刷新子女列表
        if (childrenDialogVisible.value && currentParent.value) {
          loadChildrenList(currentParent.value.id)
        }
      } catch (error: any) {
        console.error('添加子女失败:', error)
        ElMessage.error(error.response?.data?.message || '添加子女失败')
      } finally {
        assigningChild.value = false
      }
    }
  })
}

onMounted(() => {
  try {
    loadParentList()
  } catch (error) {
    console.error('组件加载失败:', error)
    ElMessage.error('家长管理页面加载失败，请刷新重试')
  }
})
</script>

<style scoped>
.parent-management-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title h1 {
  margin: 0;
  font-size: 24px;
}

.filter-container {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mt-4 {
  margin-top: 1rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.no-data {
  padding: 20px 0;
}

.loading-container {
  padding: 20px;
}
</style> 