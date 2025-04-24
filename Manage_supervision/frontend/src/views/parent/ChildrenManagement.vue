<template>
  <div class="children-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>子女管理</h2>
          <el-button type="primary" @click="showAddChildDialog">关联子女账号</el-button>
        </div>
      </template>

      <div class="table-container">
        <el-empty v-if="!childRelations.length" description="暂无关联的子女账号"></el-empty>

        <el-table v-else :data="childRelations" style="width: 100%" v-loading="loading" border>
          <el-table-column prop="childName" label="姓名" width="120"></el-table-column>
          <el-table-column prop="childUsername" label="用户名" width="130"></el-table-column>
          <el-table-column prop="childUserNumber" label="学号" width="130"></el-table-column>
          <el-table-column prop="className" label="班级" width="150"></el-table-column>
          <el-table-column prop="relationType" label="关系" width="100">
            <template #default="scope">
              {{ getRelationTypeLabel(scope.row.relationType) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="关联状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">
                {{ getStatusLabel(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 'confirmed'" 
                type="primary" 
                link 
                @click="viewChildDetails(scope.row)"
              >
                查看详情
              </el-button>
              <el-button type="danger" link @click="removeRelation(scope.row)">
                {{ scope.row.status === 'pending' ? '取消申请' : '解除关联' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 添加子女对话框 -->
    <el-dialog v-model="dialogVisible" title="关联子女账号" width="30%">
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="学生确认机制"
        description="关联申请发送后，学生需要在系统中确认后才能建立关联关系。"
        style="margin-bottom: 15px;"
      />
      <el-form 
        :model="relationForm" 
        :rules="relationRules"
        ref="relationFormRef"
        label-width="120px"
      >
        <el-form-item label="查找学生" prop="searchKey">
          <div class="search-container">
            <el-input 
              v-model="searchKey" 
              placeholder="输入姓名、用户名或学号查找" 
              @input="searchStudents"
              clearable
            />
            <el-button type="primary" @click="searchStudents" :loading="searching">搜索</el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="选择学生" prop="identifier" v-if="searchResults.length > 0">
          <el-select 
            v-model="relationForm.identifier" 
            placeholder="请选择学生"
            filterable
            style="width: 100%"
          >
            <el-option 
              v-for="student in searchResults" 
              :key="student.id" 
              :label="`${student.realName || student.username} (${student.userNumber || '无学号'})`" 
              :value="student.username"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="手动输入" prop="identifier" v-if="searchResults.length === 0">
          <el-input v-model="relationForm.identifier" placeholder="请输入子女账号用户名或学号" />
        </el-form-item>
        
        <el-form-item label="与子女关系" prop="relation">
          <el-select v-model="relationForm.relation" placeholder="请选择" style="width: 100%">
            <el-option label="父亲" value="father"></el-option>
            <el-option label="母亲" value="mother"></el-option>
            <el-option label="监护人" value="guardian"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRelation" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import axios from '@/utils/axios'

interface ChildRelation {
  id: number;
  childId: number;
  childName: string;
  childUsername: string;
  childUserNumber: string;
  className: string;
  relationType: string;
  status: string;
  createTime: string;
}

interface Student {
  id: number;
  username: string;
  realName: string;
  userNumber: string;
}

// 子女列表
const childRelations = ref<ChildRelation[]>([])
const loading = ref(false)
const submitting = ref(false)
const searching = ref(false)
const searchKey = ref('')
const searchResults = ref<Student[]>([])

// 关联子女表单
const dialogVisible = ref(false)
const relationFormRef = ref<FormInstance>()
const relationForm = reactive({
  identifier: '',
  relation: '',
  searchKey: ''
})

const relationRules = {
  identifier: [
    { required: true, message: '请输入或选择子女账号', trigger: 'change' }
  ],
  relation: [
    { required: true, message: '请选择与子女的关系', trigger: 'change' }
  ]
}

// 获取关系类型标签
const getRelationTypeLabel = (type: string): string => {
  const types: Record<string, string> = {
    'father': '父亲',
    'mother': '母亲',
    'guardian': '监护人'
  }
  return types[type] || type
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
const getStatusTagType = (status: string): string => {
  const types: Record<string, string> = {
    'pending': 'warning',
    'confirmed': 'success',
    'rejected': 'danger'
  }
  return types[status] || 'info'
}

// 显示添加子女对话框
const showAddChildDialog = () => {
  relationForm.identifier = ''
  relationForm.relation = ''
  searchKey.value = ''
  searchResults.value = []
  if (relationFormRef.value) {
    relationFormRef.value.resetFields()
  }
  dialogVisible.value = true
}

// 搜索学生
const searchStudents = async () => {
  if (!searchKey.value || searchKey.value.trim().length < 2) {
    searchResults.value = []
    return
  }
  
  searching.value = true
  try {
    const response = await axios.get('/api/parent/search-students', {
      params: { keyword: searchKey.value }
    })
    
    searchResults.value = response.data.students || []
    
    if (searchResults.value.length === 0) {
      ElMessage.info('未找到匹配的学生')
    }
  } catch (error) {
    console.error('搜索学生失败:', error)
    ElMessage.error('搜索学生失败')
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 提交关联子女请求
const submitRelation = async () => {
  if (!relationFormRef.value) return

  await relationFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await axios.post('/api/parent/children/relate', {
          childIdentifier: relationForm.identifier,
          relationType: relationForm.relation
        })
        
        ElMessage.success('关联申请已发送，请等待子女确认')
        dialogVisible.value = false
        loadChildRelations()
      } catch (error: any) {
        console.error('关联子女失败:', error)
        ElMessage.error(error.response?.data?.message || '关联子女失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 加载子女关系列表
const loadChildRelations = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/parent/children')
    childRelations.value = response.data.relations || []
  } catch (error) {
    console.error('获取子女列表失败:', error)
    ElMessage.error('获取子女列表失败')
  } finally {
    loading.value = false
  }
}

// 查看子女详情
const viewChildDetails = (relation: ChildRelation) => {
  ElMessage.info('查看子女详情功能尚未实现')
}

// 解除关联
const removeRelation = async (relation: ChildRelation) => {
  const actionText = relation.status === 'pending' ? '取消申请' : '解除关联'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await axios.delete(`/api/parent/children/${relation.id}`)
    ElMessage.success(`${actionText}成功`)
    loadChildRelations()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${actionText}失败:`, error)
      ElMessage.error(error.response?.data?.message || `${actionText}失败`)
    }
  }
}

onMounted(() => {
  loadChildRelations()
})
</script>

<style scoped>
.children-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.table-container {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.search-container {
  display: flex;
  gap: 10px;
}
</style> 