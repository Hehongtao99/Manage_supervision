<!--
  省市区管理页面
-->
<template>
  <div class="region-management">
    <el-card class="region-card">
      <div class="header-content">
        <h3>省市区管理</h3>
        <el-button type="primary" @click="handleAddTopRegion">添加省级区域</el-button>
      </div>
      
      <div class="region-content">
        <div class="region-tree-container">
          <el-tree
            ref="regionTree"
            :data="regionTree"
            node-key="id"
            :props="defaultProps"
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span class="node-actions">
                  <el-button
                    type="primary"
                    size="small"
                    link
                    @click.stop="handleAddChild(data)"
                  >
                    添加子区域
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    link
                    @click.stop="handleEdit(data)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    link
                    @click.stop="handleDelete(data)"
                  >
                    删除
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
        </div>
        
        <div class="region-detail" v-if="selectedRegion">
          <el-descriptions title="区域详情" :column="1" border>
            <el-descriptions-item label="区域名称">{{ selectedRegion.name }}</el-descriptions-item>
            <el-descriptions-item label="区域编码">{{ selectedRegion.code }}</el-descriptions-item>
            <el-descriptions-item label="区域级别">{{ getLevelText(selectedRegion.level) }}</el-descriptions-item>
            <el-descriptions-item label="排序序号">{{ selectedRegion.sortOrder }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ selectedRegion.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ selectedRegion.updateTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>
    
    <!-- 添加/编辑区域对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑区域' : '添加区域'"
      width="500px"
    >
      <el-form
        ref="regionFormRef"
        :model="regionForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="regionForm.name" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域编码" prop="code">
          <el-input v-model="regionForm.code" placeholder="请输入区域编码" />
        </el-form-item>
        <el-form-item label="区域级别" prop="level">
          <el-select v-model="regionForm.level" placeholder="请选择区域级别" style="width: 100%">
            <el-option :label="getLevelText(1)" :value="1" />
            <el-option :label="getLevelText(2)" :value="2" />
            <el-option :label="getLevelText(3)" :value="3" />
            <el-option :label="getLevelText(4)" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="regionForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRegionForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import axios from '../../utils/axios'

const regionTree = ref([])
const selectedRegion = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const regionForm = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  level: 1,
  sortOrder: 0
})

const defaultProps = {
  children: 'children',
  label: 'label'
}

const rules = {
  name: [{ required: true, message: '请输入区域名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入区域编码', trigger: 'blur' }],
  level: [{ required: true, message: '请选择区域级别', trigger: 'change' }]
}

const regionFormRef = ref<FormInstance>()

// 获取区域树
const loadRegionTree = async () => {
  try {
    const response = await axios.get('/api/regions/tree')
    regionTree.value = response.data
  } catch (error) {
    console.error('获取区域树失败:', error)
    ElMessage.error('获取区域树失败')
  }
}

// 点击节点
const handleNodeClick = (data) => {
  selectedRegion.value = data
}

// 获取级别文本
const getLevelText = (level) => {
  switch (level) {
    case 1: return '省/直辖市'
    case 2: return '市'
    case 3: return '区/县'
    case 4: return '街道/乡镇'
    default: return '未知'
  }
}

// 添加顶级区域
const handleAddTopRegion = () => {
  isEdit.value = false
  regionForm.id = null
  regionForm.parentId = null
  regionForm.name = ''
  regionForm.code = ''
  regionForm.level = 1
  regionForm.sortOrder = 0
  dialogVisible.value = true
  
  // 下一帧重置表单验证
  setTimeout(() => {
    if (regionFormRef.value) {
      regionFormRef.value.clearValidate()
    }
  }, 0)
}

// 添加子区域
const handleAddChild = (data) => {
  isEdit.value = false
  regionForm.id = null
  regionForm.parentId = data.id
  regionForm.name = ''
  regionForm.code = ''
  regionForm.level = data.level + 1
  regionForm.sortOrder = 0
  dialogVisible.value = true
  
  // 下一帧重置表单验证
  setTimeout(() => {
    if (regionFormRef.value) {
      regionFormRef.value.clearValidate()
    }
  }, 0)
}

// 编辑区域
const handleEdit = (data) => {
  isEdit.value = true
  regionForm.id = data.id
  regionForm.parentId = data.parentId
  regionForm.name = data.name
  regionForm.code = data.code
  regionForm.level = data.level
  regionForm.sortOrder = data.sortOrder || 0
  dialogVisible.value = true
  
  // 下一帧重置表单验证
  setTimeout(() => {
    if (regionFormRef.value) {
      regionFormRef.value.clearValidate()
    }
  }, 0)
}

// 删除区域
const handleDelete = (data) => {
  ElMessageBox.confirm(`确定要删除区域"${data.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/api/regions/${data.id}`)
      ElMessage.success('删除成功')
      loadRegionTree()
    } catch (error) {
      console.error('删除区域失败:', error)
      const errorMessage = error.response?.data?.message || '删除区域失败'
      ElMessage.error(errorMessage)
    }
  }).catch(() => {})
}

// 提交表单
const submitRegionForm = async () => {
  if (!regionFormRef.value) return
  
  await regionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 编辑区域
          await axios.put(`/api/regions/${regionForm.id}`, regionForm)
          ElMessage.success('更新成功')
        } else {
          // 添加区域
          await axios.post('/api/regions', regionForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadRegionTree()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

onMounted(() => {
  loadRegionTree()
})
</script>

<style scoped>
.region-management {
  padding: 20px;
}

.region-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-content h3 {
  margin: 0;
}

.region-content {
  display: flex;
  flex-wrap: wrap;
}

.region-tree-container {
  flex: 1;
  min-width: 300px;
  max-width: 500px;
  margin-right: 20px;
  border-right: 1px solid #ebeef5;
  padding-right: 20px;
}

.region-detail {
  flex: 1;
  min-width: 300px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  font-size: 14px;
}

.node-actions {
  margin-left: 10px;
}
</style> 