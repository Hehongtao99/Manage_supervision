<template>
  <div class="region-management">
    <div class="page-header">
      <h2>地区管理</h2>
      <el-button
        type="primary"
        @click="handleAddRootRegion"
        v-permission="'ADMIN'"
      >
        添加省份
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左侧树形结构 -->
      <el-col :span="8">
        <el-card class="region-tree-card">
          <template #header>
            <div class="card-header">
              <h3>地区结构</h3>
              <el-tooltip content="刷新" placement="top">
                <el-button
                  type="primary"
                  circle
                  :icon="Refresh"
                  @click="loadRegionTree"
                  size="small"
                />
              </el-tooltip>
            </div>
          </template>
          <div class="tree-container">
            <el-tree
              ref="regionTreeRef"
              :data="treeData"
              node-key="id"
              :expand-on-click-node="false"
              :props="{ label: 'name' }"
              highlight-current
              default-expand-all
              @node-click="handleNodeClick"
            >
              <template #default="{ node, data }">
                <div class="custom-tree-node">
                  <span class="node-label">
                    <span v-if="data.level === 1" class="level-tag level-province">省</span>
                    <span v-else-if="data.level === 2" class="level-tag level-city">市</span>
                    <span v-else-if="data.level === 3" class="level-tag level-district">区</span>
                    {{ data.name }}
                  </span>
                  <div class="node-actions">
                    <el-tooltip content="添加子地区" placement="top">
                      <el-button
                        v-if="data.level < 3"
                        type="primary"
                        circle
                        :icon="Plus"
                        size="small"
                        @click.stop="handleAddChildRegion(data)"
                      />
                    </el-tooltip>
                    <el-tooltip content="编辑" placement="top">
                      <el-button
                        type="primary"
                        circle
                        :icon="Edit"
                        size="small"
                        @click.stop="handleEditRegion(data)"
                      />
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                      <el-button
                        type="danger"
                        circle
                        :icon="Delete"
                        size="small"
                        @click.stop="handleDeleteRegion(data)"
                      />
                    </el-tooltip>
                  </div>
                </div>
              </template>
            </el-tree>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详情信息 -->
      <el-col :span="16">
        <el-card v-if="selectedRegion" class="region-detail-card">
          <template #header>
            <div class="card-header">
              <h3>地区详情</h3>
              <div>
                <el-button type="primary" @click="handleEditRegion(selectedRegion)">编辑地区</el-button>
                <el-button 
                  :type="selectedRegion.status === 'active' ? 'warning' : 'success'"
                  @click="handleToggleStatus(selectedRegion)"
                >
                  {{ selectedRegion.status === 'active' ? '停用' : '启用' }}
                </el-button>
              </div>
            </div>
          </template>
          
          <el-descriptions border :column="2" size="large">
            <el-descriptions-item label="地区名称">{{ selectedRegion.name }}</el-descriptions-item>
            <el-descriptions-item label="地区编码">{{ selectedRegion.code }}</el-descriptions-item>
            <el-descriptions-item label="地区级别">
              <el-tag :type="levelTypeMap[selectedRegion.level || 1]">
                {{ levelTextMap[selectedRegion.level || 1] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedRegion.status === 'active' ? 'success' : 'danger'">
                {{ selectedRegion.status === 'active' ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="排序值">{{ selectedRegion.sort }}</el-descriptions-item>
            <el-descriptions-item label="上级地区">{{ selectedRegion.parentName || '无' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(selectedRegion.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" :span="2">
              {{ formatDateTime(selectedRegion.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div v-if="selectedRegion.level !== 3" class="sub-region-list">
            <h4>{{ levelTextMap[(selectedRegion.level || 1) + 1] }}列表</h4>
            
            <el-table
              :data="childRegions"
              border
              stripe
            >
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="code" label="编码" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                    {{ row.status === 'active' ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button
                    type="primary"
                    link
                    @click="handleEditRegion(row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    @click="handleDeleteRegion(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="add-sub-region" v-if="selectedRegion.level < 3">
              <el-button
                type="primary"
                plain
                icon="Plus"
                @click="handleAddChildRegion(selectedRegion)"
              >
                添加{{ levelTextMap[(selectedRegion.level || 1) + 1] }}
              </el-button>
            </div>
          </div>
        </el-card>
        
        <el-empty v-else description="请选择左侧地区" />
      </el-col>
    </el-row>

    <!-- 地区编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地区' : '添加地区'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="地区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入地区名称" />
        </el-form-item>
        
        <el-form-item label="地区编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入地区编码" />
        </el-form-item>
        
        <el-form-item label="上级地区">
          <el-input 
            v-model="form.parentName" 
            placeholder="无" 
            disabled
          />
        </el-form-item>
        
        <el-form-item label="地区级别">
          <el-tag :type="levelTypeMap[form.level || 1]">
            {{ levelTextMap[form.level || 1] }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Refresh } from '@element-plus/icons-vue'
import type { RegionData } from '../../types/region'
import {
  getRegionTree,
  getChildRegions,
  getRegionDetail,
  addRegion,
  updateRegion,
  deleteRegion,
  toggleRegionStatus
} from '../../api/region'

// 状态管理
const treeData = ref<RegionData[]>([])
const selectedRegion = ref<RegionData | null>(null)
const childRegions = ref<RegionData[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const regionTreeRef = ref()

// 级别相关配置
const levelTextMap = {
  1: '省份',
  2: '城市',
  3: '区县'
}

const levelTypeMap = {
  1: 'danger',
  2: 'warning',
  3: 'success'
}

// 表单数据
const form = reactive<RegionData>({
  name: '',
  code: '',
  parentId: null,
  parentName: '',
  level: 1,
  sort: 0
})

// 表单校验规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入地区名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入地区编码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '编码必须为6位数字', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
})

// 加载地区树数据
const loadRegionTree = async () => {
  try {
    const data = await getRegionTree()
    treeData.value = data
  } catch (error) {
    console.error('获取地区树失败', error)
    ElMessage.error('获取地区树失败')
  }
}

// 处理节点点击
const handleNodeClick = async (data: RegionData) => {
  selectedRegion.value = data
  
  // 如果不是区县，加载子地区
  if (data.level !== 3) {
    try {
      childRegions.value = await getChildRegions(data.id!)
    } catch (error) {
      console.error('获取子地区失败', error)
      ElMessage.error('获取子地区失败')
    }
  } else {
    childRegions.value = []
  }
}

// 添加省份
const handleAddRootRegion = () => {
  isEdit.value = false
  resetForm()
  
  // 设置为省级
  form.level = 1
  form.parentId = null
  form.parentName = ''
  
  dialogVisible.value = true
}

// 添加子地区
const handleAddChildRegion = (parentRegion: RegionData) => {
  isEdit.value = false
  resetForm()
  
  // 设置父级信息
  form.parentId = parentRegion.id
  form.parentName = parentRegion.name
  form.level = (parentRegion.level || 1) + 1
  
  dialogVisible.value = true
}

// 编辑地区
const handleEditRegion = (region: RegionData) => {
  isEdit.value = true
  
  // 克隆对象，防止直接修改引用
  Object.assign(form, region)
  
  dialogVisible.value = true
}

// 删除地区
const handleDeleteRegion = (region: RegionData) => {
  ElMessageBox.confirm(
    `确定要删除地区"${region.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRegion(region.id!)
      ElMessage.success('删除成功')
      // 重新加载数据
      loadRegionTree()
      // 如果删除的是当前选中的，清空选中
      if (selectedRegion.value?.id === region.id) {
        selectedRegion.value = null
      }
    } catch (error: any) {
      console.error('删除失败', error)
      if (error.response?.data?.error) {
        ElMessage.error(`删除失败: ${error.response.data.error}`)
      } else {
        ElMessage.error('删除失败')
      }
    }
  }).catch(() => {
    // 取消删除
  })
}

// 切换地区状态
const handleToggleStatus = async (region: RegionData) => {
  try {
    await toggleRegionStatus(region.id!)
    
    // 更新状态
    const newStatus = region.status === 'active' ? 'inactive' : 'active'
    if (selectedRegion.value?.id === region.id) {
      selectedRegion.value.status = newStatus
    }
    
    // 刷新树
    loadRegionTree()
    
    ElMessage.success(`${newStatus === 'active' ? '启用' : '停用'}成功`)
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 更新
          await updateRegion(form.id!, form)
          ElMessage.success('更新成功')
        } else {
          // 新增
          await addRegion(form)
          ElMessage.success('添加成功')
        }
        
        // 关闭对话框
        dialogVisible.value = false
        
        // 重新加载数据
        loadRegionTree()
        
        // 如果有选中节点，刷新子节点数据
        if (selectedRegion.value && selectedRegion.value.id) {
          childRegions.value = await getChildRegions(selectedRegion.value.id)
        }
      } catch (error: any) {
        console.error('保存失败', error)
        if (error.response?.data?.error) {
          ElMessage.error(`保存失败: ${error.response.data.error}`)
        } else {
          ElMessage.error('保存失败')
        }
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    code: '',
    parentId: null,
    parentName: '',
    level: 1,
    sort: 0
  })
  
  formRef.value?.resetFields()
}

// 格式化日期时间
const formatDateTime = (dateTimeString?: string) => {
  if (!dateTimeString) return '-'
  
  try {
    const date = new Date(dateTimeString)
    return date.toLocaleString()
  } catch (e) {
    return dateTimeString
  }
}

onMounted(() => {
  loadRegionTree()
})
</script>

<style scoped>
.region-management {
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

.card-header h3 {
  margin: 0;
  font-size: 16px;
}

.region-tree-card,
.region-detail-card {
  height: calc(100vh - 180px);
  overflow: auto;
}

.tree-container {
  height: calc(100% - 20px);
  overflow: auto;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
}

.node-actions {
  display: none;
}

.custom-tree-node:hover .node-actions {
  display: flex;
  gap: 4px;
}

.level-tag {
  display: inline-block;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 6px;
  color: #fff;
}

.level-province {
  background-color: #f56c6c;
}

.level-city {
  background-color: #e6a23c;
}

.level-district {
  background-color: #67c23a;
}

.sub-region-list {
  margin-top: 20px;
}

.sub-region-list h4 {
  margin-bottom: 15px;
}

.add-sub-region {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}
</style> 