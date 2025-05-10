<template>
  <div class="subject-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程科目管理</span>
          <el-button type="primary" @click="handleAddSubject">
            <el-icon><Plus /></el-icon>添加科目
          </el-button>
        </div>
      </template>
      
      <div class="subject-list" v-loading="loading">
        <el-empty v-if="subjects.length === 0" description="暂无科目数据">
          <el-button type="primary" @click="handleAddSubject">添加科目</el-button>
        </el-empty>
        
        <el-table 
          v-else
          :data="subjects" 
          border 
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="科目名称" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button
                type="primary"
                link
                @click="handleEditSubject(scope.row)"
              >
                修改
              </el-button>
              <el-button
                type="danger"
                link
                @click="handleDeleteSubject(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    
    <!-- 新增/修改科目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改科目' : '添加科目'"
      width="30%"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="科目名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入科目名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { 
  SubjectDTO, 
  getAllSubjects, 
  createSubject, 
  updateSubject, 
  deleteSubject 
} from '../../api/subject'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const subjects = ref<SubjectDTO[]>([])
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  id: 0,
  name: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入科目名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

// 初始化
onMounted(async () => {
  await fetchSubjects()
})

// 获取科目列表
const fetchSubjects = async () => {
  loading.value = true
  try {
    subjects.value = await getAllSubjects()
  } catch (error) {
    ElMessage.error('获取科目列表失败')
  } finally {
    loading.value = false
  }
}

// 添加科目
const handleAddSubject = () => {
  isEdit.value = false
  form.id = 0
  form.name = ''
  dialogVisible.value = true
}

// 编辑科目
const handleEditSubject = (subject: SubjectDTO) => {
  isEdit.value = true
  form.id = subject.id
  form.name = subject.name
  dialogVisible.value = true
}

// 删除科目
const handleDeleteSubject = (subject: SubjectDTO) => {
  ElMessageBox.confirm(
    `确定要删除科目"${subject.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const result = await deleteSubject(subject.id)
      if (result.success) {
        ElMessage.success('科目删除成功')
        await fetchSubjects()
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {
    // 取消删除操作
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        // 更新科目
        const result = await updateSubject(form.id, form.name)
        if (result.success) {
          ElMessage.success('科目更新成功')
          dialogVisible.value = false
          await fetchSubjects()
        } else {
          ElMessage.error(result.message || '更新失败')
        }
      } else {
        // 添加科目
        const result = await createSubject(form.name)
        if (result.success) {
          ElMessage.success('科目添加成功')
          dialogVisible.value = false
          await fetchSubjects()
        } else {
          ElMessage.error(result.message || '添加失败')
        }
      }
    } catch (error) {
      ElMessage.error('操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped>
.subject-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subject-list {
  margin-top: 20px;
}

.dialog-footer {
  margin-top: 20px;
}
</style> 