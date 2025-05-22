<!--
  省市区街道管理页面 - 树状结构版本
-->
<template>
  <div class="region-tree-management">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h3>区域管理</h3>
          <div class="buttons">
            <el-button type="primary" @click="handleAddTopLevel">新增省份</el-button>
            <el-button v-if="currentParentId" type="default" @click="goBack">返回上级</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="breadcrumbs.length > 0" class="breadcrumb-container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="goToRoot">省市区列表</el-breadcrumb-item>
          <el-breadcrumb-item 
            v-for="(item, index) in breadcrumbs" 
            :key="index"
            @click="goToBreadcrumb(index)"
          >
            {{ item.name }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="main-content">
        <!-- 左侧树状列表 -->
        <div class="tree-container">
          <el-tree
            v-loading="loading"
            ref="treeRef"
            :data="treeData"
            node-key="id"
            :props="{ 
              label: 'name',
              children: 'children'
            }"
            :expand-on-click-node="false"
            highlight-current
            :default-expanded-keys="defaultExpandedKeys"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ data.name }} 
                  <el-tag size="small">{{ getLevelText(data.level) }}</el-tag>
                  <el-tag size="small" type="info">{{ data.code }}</el-tag>
                </span>
                <span class="node-actions">
                  <el-button
                    v-if="data.level < 4"
                    type="success"
                    link
                    @click="handleAddChild(data)"
                  >
                    新增
                  </el-button>
                  <el-button
                    type="primary"
                    link
                    @click="handleEdit(data)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="danger"
                    link
                    @click="handleDelete(data)"
                  >
                    删除
                  </el-button>
                  <el-button 
                    v-if="data.level === 4" 
                    type="info" 
                    link 
                    @click="showUploadDialog(data)"
                  >
                    上传图片
                  </el-button>
                </span>
              </span>
            </template>
          </el-tree>
          
          <!-- 当前无区域数据的提示 -->
          <div v-if="treeData.length === 0 && !loading" class="empty-tip">
            <el-empty description="暂无区域数据" />
          </div>
        </div>
        
        <!-- 右侧预览区 - 只有点击街道级别才会显示 -->
        <div v-if="selectedNode && selectedNode.level === 4" class="preview-container">
          <el-card class="preview-card">
            <template #header>
              <div class="preview-header">
                <span class="preview-title">{{ selectedNode.name }} 详情</span>
              </div>
            </template>
            
            <div class="preview-content">
              <div class="preview-item">
                <span class="preview-label">名称：</span>
                <span class="preview-value">{{ selectedNode.name }}</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">编码：</span>
                <span class="preview-value">{{ selectedNode.code }}</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">级别：</span>
                <span class="preview-value">街道</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">经度：</span>
                <span class="preview-value">{{ selectedNode.longitude || '暂无' }}</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">纬度：</span>
                <span class="preview-value">{{ selectedNode.latitude || '暂无' }}</span>
              </div>
              
              <!-- 图片预览区 -->
              <div class="image-preview" v-if="selectedNode.imageUrl">
                <h4>街道图片：</h4>
                <el-image 
                  :src="selectedNode.imageUrl"
                  fit="cover"
                  :preview-src-list="[selectedNode.imageUrl]"
                  preview-teleported
                  :initial-index="0"
                  referrer-policy="no-referrer"
                  crossorigin="anonymous"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><icon-picture /></el-icon>
                      <span>图片加载失败，请检查图片链接</span>
                      <el-button size="small" type="primary" @click="openImageInNewTab(selectedNode.imageUrl)">
                        在新窗口打开
                      </el-button>
                    </div>
                  </template>
                  <template #placeholder>
                    <div class="image-loading">
                      <el-icon class="is-loading"><loading /></el-icon>
                      <span>加载中...</span>
                    </div>
                  </template>
                </el-image>
              </div>
              
              <div class="no-image" v-else>
                <el-empty description="暂无图片" :image-size="100">
                  <template #description>
                    <p>暂无街道图片，请点击"上传图片"按钮上传</p>
                  </template>
                  <el-button type="primary" @click="showUploadDialog(selectedNode)">
                    上传图片
                  </el-button>
                </el-empty>
              </div>
              
              <!-- 经纬度地图标记点 -->
              <div class="map-link" v-if="selectedNode.longitude && selectedNode.latitude">
                <el-link 
                  :href="`https://maps.google.com/?q=${selectedNode.latitude},${selectedNode.longitude}`" 
                  target="_blank" 
                  type="primary"
                >
                  <el-icon><location /></el-icon>
                  在地图中查看位置
                </el-link>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑区域' : '新增区域'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="父级区域" v-if="form.parentName">
          <el-input v-model="form.parentName" disabled />
        </el-form-item>
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入区域编码" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择级别" disabled style="width: 100%">
            <el-option label="省" :value="1" />
            <el-option label="市" :value="2" />
            <el-option label="区/县" :value="3" />
            <el-option label="街道" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <!-- 只有街道级别才显示经纬度 -->
        <template v-if="form.level === 4">
          <el-form-item label="经度" prop="longitude">
            <el-input-number 
              v-model="form.longitude" 
              :precision="6" 
              :step="0.000001" 
              style="width: 100%" 
              placeholder="请输入经度"
            />
          </el-form-item>
          <el-form-item label="纬度" prop="latitude">
            <el-input-number 
              v-model="form.latitude" 
              :precision="6" 
              :step="0.000001" 
              style="width: 100%" 
              placeholder="请输入纬度"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 上传图片弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传街道图片"
      width="500px"
    >
      <div v-if="selectedRegion && selectedRegion.imageUrl" class="current-image">
        <h4>当前图片：</h4>
        <el-image 
          :src="selectedRegion.imageUrl"
          style="max-width: 100%; max-height: 200px"
        />
      </div>
      
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :file-list="fileList"
        :show-file-list="false"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传JPG/PNG格式图片，大小不超过10MB
          </div>
        </template>
      </el-upload>
      
      <!-- 添加图片预览区域 -->
      <div v-if="imagePreview" class="upload-preview">
        <h4>图片预览：</h4>
        <div class="preview-image-container">
          <el-image 
            :src="imagePreview" 
            style="max-width: 100%; max-height: 200px" 
            fit="contain"
          />
          <div class="preview-info">
            <span>{{ uploadFileName }}</span>
            <el-button type="danger" size="small" @click="cancelPreview" circle>
              <el-icon><delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelUpload">取消</el-button>
          <el-button type="primary" @click="submitUpload" :disabled="!uploadFile">上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { UploadFilled, Picture as IconPicture, Location, Delete, Loading } from '@element-plus/icons-vue'
import axios from '../../utils/axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// 树形数据
const treeData = ref([])
const loading = ref(false)
const currentParentId = ref(null)
const breadcrumbs = ref([])
const treeRef = ref(null)
const defaultExpandedKeys = ref([]) // 默认展开的节点

// 选中的节点（用于右侧预览）
const selectedNode = ref(null)

// 是否显示经纬度字段（只对街道级别显示）
const showGeoFields = computed(() => {
  if (!treeData.value || treeData.value.length === 0) return false
  return treeData.value.some(item => item.level === 4)
})

// 选中的区域（用于上传图片等操作）
const selectedRegion = ref(null)

// 新增/编辑弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  id: null,
  parentId: null,
  parentName: '',
  name: '',
  code: '',
  level: 1,
  sortOrder: 0,
  longitude: null,
  latitude: null,
  imageUrl: '',
})

// 重置表单
const resetForm = () => {
  form.id = null
  form.parentId = null
  form.parentName = ''
  form.name = ''
  form.code = ''
  form.level = 1
  form.sortOrder = 0
  form.longitude = null
  form.latitude = null
  form.imageUrl = ''
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入区域编码', trigger: 'blur' },
    { pattern: /^\d{2,20}$/, message: '编码必须为2-20位数字', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ],
}

// 上传图片相关
const uploadDialogVisible = ref(false)
const fileList = ref([])
const uploadFile = ref(null)
const imagePreview = ref('') // 图片预览URL
const uploadFileName = ref('') // 上传文件名

// 初始化
onMounted(() => {
  loadRegionTree()
})

// 加载区域树
const loadRegionTree = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/regions/tree')
    treeData.value = (response.data || []).map(item => processTreeNode(item))
    
    // 设置默认展开的节点（只展开第一级节点）
    defaultExpandedKeys.value = treeData.value.map(node => node.id)
    
    currentParentId.value = null
    breadcrumbs.value = []
  } catch (error) {
    console.error('加载区域树失败', error)
    ElMessage.error('加载区域树失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 处理树节点数据
const processTreeNode = (node) => {
  // 处理数字和布尔值类型
  const processedNode = {
    ...node,
    level: Number(node.level || 1),
    sortOrder: Number(node.sortOrder || 0),
    hasChildren: Boolean(node.hasChildren),
    isStreet: Boolean(node.isStreet)
  }
  
  // 如果有子节点，递归处理
  if (node.children && node.children.length > 0) {
    processedNode.children = node.children.map(child => processTreeNode(child))
  }
  
  return processedNode
}

// 加载面包屑导航数据
const loadBreadcrumbs = async (id) => {
  try {
    const response = await axios.get(`/api/regions/${id}/path`)
    breadcrumbs.value = (response.data || []).map(item => ({
      id: item.id,
      name: item.name,
      level: Number(item.level || 1)
    }))
  } catch (error) {
    console.error('加载面包屑导航失败', error)
    breadcrumbs.value = []
  }
}

// 返回根级
const goToRoot = () => {
  loadRegionTree()
}

// 通过面包屑导航
const goToBreadcrumb = (index) => {
  if (index >= 0 && index < breadcrumbs.value.length) {
    const id = breadcrumbs.value[index].id
    loadTreeForParent(id)
  }
}

// 加载指定父节点的树
const loadTreeForParent = async (parentId) => {
  loading.value = true
  try {
    const response = await axios.get('/api/regions/tree', {
      params: { parentId }
    })
    treeData.value = (response.data || []).map(item => processTreeNode(item))
    
    // 设置默认展开的节点（只展开当前加载的节点）
    defaultExpandedKeys.value = treeData.value.map(node => node.id)
    
    currentParentId.value = parentId
    
    // 加载面包屑
    if (parentId) {
      await loadBreadcrumbs(parentId)
    } else {
      breadcrumbs.value = []
    }
  } catch (error) {
    console.error('加载区域树失败', error)
    ElMessage.error('加载区域树失败')
    treeData.value = []
  } finally {
    loading.value = false
  }
}

// 返回上级
const goBack = () => {
  if (breadcrumbs.value.length > 0) {
    // 如果只有一级面包屑，则返回根级
    if (breadcrumbs.value.length === 1) {
      goToRoot()
    } else {
      // 否则返回到上一级面包屑指向的区域
      const parentIndex = breadcrumbs.value.length - 2
      if (parentIndex >= 0) {
        loadTreeForParent(breadcrumbs.value[parentIndex].id)
      }
    }
  } else {
    goToRoot()
  }
}

// 新增顶级区域
const handleAddTopLevel = () => {
  isEdit.value = false
  resetForm()
  form.level = 1
  dialogVisible.value = true
  
  // 清除表单验证
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
}

// 新增子区域
const handleAddChild = (data) => {
  if (data.level >= 4) {
    ElMessage.warning('街道级别不能再添加子区域')
    return
  }
  
  isEdit.value = false
  resetForm()
  form.parentId = data.id
  form.parentName = data.name
  form.level = data.level + 1
  
  dialogVisible.value = true
  
  // 清除表单验证
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
}

// 编辑区域
const handleEdit = (data) => {
  isEdit.value = true
  resetForm()
  
  // 复制数据到表单
  form.id = data.id
  form.parentId = data.parentId
  form.name = data.name
  form.code = data.code
  form.level = Number(data.level || 1)
  form.sortOrder = Number(data.sortOrder || 0)
  
  if (data.level === 4) {
    form.longitude = data.longitude !== null && data.longitude !== undefined ? Number(data.longitude) : null
    form.latitude = data.latitude !== null && data.latitude !== undefined ? Number(data.latitude) : null
    form.imageUrl = data.imageUrl || ''
  }
  
  // 如果有父级，获取父级名称
  if (data.parentId) {
    axios.get(`/api/regions/${data.parentId}`)
      .then(res => {
        form.parentName = res.data.name
      })
      .catch(() => {
        form.parentName = '未知'
      })
  }
  
  dialogVisible.value = true
  
  // 清除表单验证
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
}

// 删除区域
const handleDelete = (data) => {
  ElMessageBox.confirm('确定要删除该区域吗？如果有子区域将无法删除。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.delete(`/api/regions/${data.id}`)
      ElMessage.success('删除成功')
      
      // 刷新树状数据
      if (currentParentId.value) {
        loadTreeForParent(currentParentId.value)
      } else {
        loadRegionTree()
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除失败，可能该区域下有子区域')
      }
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 处理表单数据
        const regionData = {
          id: form.id,
          parentId: form.parentId,
          name: form.name,
          code: form.code,
          level: Number(form.level),
          sortOrder: Number(form.sortOrder || 0),
          longitude: null,
          latitude: null,
        }
        
        // 只有街道级别才设置经纬度
        if (form.level === 4) {
          regionData.longitude = form.longitude !== null && form.longitude !== undefined 
            ? Number(form.longitude) : null
          regionData.latitude = form.latitude !== null && form.latitude !== undefined 
            ? Number(form.latitude) : null
        }
        
        // 保存区域
        await axios.post('/api/regions', regionData)
        
        ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
        dialogVisible.value = false
        
        // 刷新树状数据
        if (currentParentId.value) {
          loadTreeForParent(currentParentId.value)
        } else {
          loadRegionTree()
        }
      } catch (error) {
        console.error('保存失败', error)
        if (error.response && error.response.data && error.response.data.message) {
          ElMessage.error(error.response.data.message)
        } else {
          ElMessage.error('保存失败')
        }
      }
    }
  })
}

// 显示上传对话框
const showUploadDialog = (row) => {
  if (!row || row.level !== 4) {
    ElMessage.warning('只有街道级别才能上传图片')
    return
  }
  
  selectedRegion.value = row
  fileList.value = []
  uploadFile.value = null
  uploadDialogVisible.value = true
}

// 处理文件变更
const handleFileChange = (file) => {
  uploadFile.value = file.raw
  uploadFileName.value = file.name
  
  // 验证文件类型
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    fileList.value = []
    uploadFile.value = null
    imagePreview.value = ''
    return
  }
  
  // 验证文件大小
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    fileList.value = []
    uploadFile.value = null
    imagePreview.value = ''
    return
  }
  
  // 生成预览URL
  imagePreview.value = URL.createObjectURL(file.raw)
}

// 取消预览
const cancelPreview = () => {
  if (imagePreview.value) {
    URL.revokeObjectURL(imagePreview.value)
  }
  imagePreview.value = ''
  uploadFile.value = null
  uploadFileName.value = ''
  fileList.value = []
}

// 取消上传
const cancelUpload = () => {
  uploadDialogVisible.value = false
  cancelPreview()
  selectedRegion.value = null
}

// 提交上传
const submitUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择要上传的图片')
    return
  }
  
  if (!selectedRegion.value || !selectedRegion.value.id) {
    ElMessage.warning('请先选择一个街道')
    uploadDialogVisible.value = false
    return
  }
  
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    
    const response = await axios.post(
      `/api/regions/${selectedRegion.value.id}/image`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
    
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    
    // 如果当前选中的预览节点就是上传的节点，同步更新图片URL
    if (selectedNode.value && selectedNode.value.id === selectedRegion.value.id) {
      selectedNode.value.imageUrl = response.data
    }
    
    // 刷新树状数据
    if (currentParentId.value) {
      loadTreeForParent(currentParentId.value)
    } else {
      loadRegionTree()
    }
  } catch (error) {
    console.error('上传失败', error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('上传失败')
    }
  }
}

// 获取级别文本
const getLevelText = (level) => {
  // 确保level是数字类型
  const numLevel = parseInt(level, 10)
  
  // 如果转换后不是有效数字，返回未知
  if (isNaN(numLevel)) {
    return '未知'
  }
  
  switch (numLevel) {
    case 1: return '省'
    case 2: return '市'
    case 3: return '区/县'
    case 4: return '街道'
    default: return '未知'
  }
}

// 处理节点点击事件
const handleNodeClick = (data) => {
  // 只有点击街道级别才设置选中的节点，用于右侧预览
  if (data.level === 4) {
    selectedNode.value = { ...data }
  } else {
    selectedNode.value = null
  }
}

const openImageInNewTab = (url) => {
  if (url) {
    window.open(url, '_blank');
  }
}
</script>

<style scoped>
.region-tree-management {
  height: 100%;
}

.main-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.breadcrumb-container {
  margin-top: 10px;
  margin-bottom: 10px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.tree-container {
  flex: 1;
  overflow: auto;
  border-right: 1px solid #ebeef5;
  padding-right: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-actions {
  margin-left: 20px;
}

.empty-tip {
  margin-top: 50px;
  text-align: center;
}

.el-breadcrumb-item {
  cursor: pointer;
}

.current-image {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  padding: 10px;
  border-radius: 4px;
}

.el-tag {
  margin-left: 5px;
}

.preview-container {
  width: 300px;
  overflow: auto;
  padding-left: 10px;
}

.preview-card {
  height: 100%;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-title {
  font-weight: bold;
  font-size: 16px;
}

.preview-content {
  padding: 10px 0;
}

.preview-item {
  margin-bottom: 10px;
  display: flex;
}

.preview-label {
  font-weight: bold;
  width: 60px;
  color: #606266;
}

.preview-value {
  flex: 1;
  word-break: break-all;
}

.image-preview {
  margin-top: 20px;
}

.image-preview h4 {
  margin-bottom: 10px;
  color: #606266;
}

.el-image {
  width: 100%;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 150px;
  color: #909399;
}

.no-image {
  margin-top: 20px;
}

.map-link {
  margin-top: 20px;
  text-align: center;
}

.upload-preview {
  margin-top: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.upload-preview h4 {
  margin-bottom: 10px;
  color: #606266;
}

.preview-image-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 10px;
  padding: 0 10px;
}

.image-info {
  display: none; /* 隐藏图片信息 */
}

.image-url {
  display: none; /* 隐藏图片URL */
}

.image-loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 150px;
  color: #909399;
}

.image-loading .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}
</style> 