<template>
  <div class="category-management">
    <div class="header">
      <h2>课程类别管理</h2>
      <el-button type="primary" @click="showAddDialog" v-permission="'category:add'">添加类别</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索类别名称"
        clearable
        @clear="handleSearch"
        style="width: 300px"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <el-table
      :data="categories"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="类别名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="250" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(row)"
            v-permission="'category:edit'"
            text
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(row)"
            v-permission="'category:delete'"
            text
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程类别' : '添加课程类别'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="类别名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入类别名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入类别描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { hasPermission } from '../../utils/permission'
import {
  getCourseCategories,
  createCourseCategory,
  updateCourseCategory,
  deleteCourseCategory
} from '../../api/courseCategory'

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索关键字
const searchKeyword = ref('')

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 类别列表
const categories = ref<any[]>([])

// 对话框控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入类别名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应在2到50个字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ]
})

// 获取类别列表
const fetchCategories = async () => {
  loading.value = true
  console.log('开始获取课程类别数据...')
  console.log('请求参数:', { page: currentPage.value, size: pageSize.value, name: searchKeyword.value })
  
  try {
    const response = await getCourseCategories(
      currentPage.value,
      pageSize.value,
      searchKeyword.value
    )
    console.log('获取课程类别成功，响应数据:', response)
    categories.value = response.content || []
    total.value = response.total || 0
  } catch (error) {
    console.error('获取课程类别失败:', error)
    console.error('错误详情:', JSON.stringify(error, null, 2))
    ElMessage.error('获取课程类别失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchCategories()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchCategories()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  form.id = undefined
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

// 显示编辑对话框
const handleEdit = (row: any) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description
  dialogVisible.value = true
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    submitting.value = true
    try {
      if (isEdit.value && form.id) {
        // 编辑模式
        await updateCourseCategory(form.id, {
          name: form.name,
          description: form.description
        })
        ElMessage.success('类别更新成功')
      } else {
        // 添加模式
        await createCourseCategory({
          name: form.name,
          description: form.description
        })
        ElMessage.success('类别添加成功')
      }
      dialogVisible.value = false
      fetchCategories()
    } catch (error) {
      console.error('保存类别失败:', error)
      ElMessage.error('保存失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 删除类别
const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除此类别吗？已关联此类别的课程将受到影响。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      loading.value = true
      try {
        await deleteCourseCategory(row.id)
        ElMessage.success('类别已删除')
        fetchCategories()
      } catch (error) {
        console.error('删除类别失败:', error)
        ElMessage.error('删除失败，请稍后重试')
      } finally {
        loading.value = false
      }
    })
    .catch(() => {
      // 用户取消删除操作
    })
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 