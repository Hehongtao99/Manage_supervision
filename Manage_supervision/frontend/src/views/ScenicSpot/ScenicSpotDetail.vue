<template>
  <div class="scenic-spot-detail-container">
    <div class="scenic-spot-header">
      <div class="header-left">
        <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        <h2 class="scenic-spot-title">景区详情</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-container">
          <el-skeleton-item variant="image" style="width: 100%; height: 240px;" />
          <div style="padding: 20px;">
            <el-skeleton-item variant="h1" style="width: 50%;" />
            <div style="display: flex; margin-top: 20px;">
              <el-skeleton-item variant="text" style="margin-right: 16px; width: 30%;" />
              <el-skeleton-item variant="text" style="width: 30%;" />
            </div>
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%;" />
            <el-skeleton-item variant="text" style="margin-top: 16px; width: 100%;" />
          </div>
        </div>
      </template>

      <template #default>
        <el-card v-if="scenicSpot" class="scenic-detail-card">
          <div class="scenic-hero">
            <div class="scenic-image">
              <el-image
                v-if="scenicSpot.imageUrl"
                :src="scenicSpot.imageUrl"
                fit="cover"
                style="width: 100%; height: 100%;"
                :preview-src-list="[scenicSpot.imageUrl]"
              />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="scenic-info">
              <h1 class="scenic-name">{{ scenicSpot.name }}</h1>
              <div class="scenic-meta">
                <el-tag v-if="scenicSpot.level" class="meta-tag" :type="getLevelTagType(scenicSpot.level)">
                  {{ scenicSpot.level }}级景区
                </el-tag>
                <el-tag class="meta-tag" :type="scenicSpot.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ scenicSpot.status === 'ACTIVE' ? '正常运营' : '暂停开放' }}
                </el-tag>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="info-section">
            <h3 class="section-title">基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="景区名称">{{ scenicSpot.name }}</el-descriptions-item>
              <el-descriptions-item label="景区等级">{{ displayLevel(scenicSpot.level) }}</el-descriptions-item>
              <el-descriptions-item label="所在地区">{{ scenicSpot.locationPath }}</el-descriptions-item>
              <el-descriptions-item label="详细地址">{{ scenicSpot.address }}</el-descriptions-item>
              <el-descriptions-item label="营业时间">{{ scenicSpot.businessHours || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="门票价格">{{ scenicSpot.ticketPrice ? `¥${scenicSpot.ticketPrice}` : '免费' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ scenicSpot.contactPhone || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ scenicSpot.createTime }}</el-descriptions-item>
              <el-descriptions-item label="最后更新">{{ scenicSpot.updateTime }}</el-descriptions-item>
              <el-descriptions-item label="排序值">{{ scenicSpot.sort }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="info-section">
            <h3 class="section-title">景区描述</h3>
            <div class="description-content">
              {{ scenicSpot.description || '暂无描述信息' }}
            </div>
          </div>
        </el-card>

        <el-empty v-else description="未找到景区信息"></el-empty>
      </template>
    </el-skeleton>

    <!-- 编辑表单对话框 -->
    <el-dialog
      title="编辑景区"
      v-model="dialogVisible"
      width="60%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ScenicSpotForm
        v-if="dialogVisible"
        :scenic-spot="scenicSpot"
        :is-edit="true"
        @submit="handleFormSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getScenicSpotDetail, deleteScenicSpot } from '../../api/scenicSpot'
import type { ScenicSpotData } from '../../types/scenicSpot'
import ScenicSpotForm from './components/ScenicSpotForm.vue'

const route = useRoute()
const router = useRouter()
const scenicSpotId = ref<number>(Number(route.params.id))
const scenicSpot = ref<ScenicSpotData>()
const loading = ref(true)
const dialogVisible = ref(false)

// 获取景区详情
const fetchScenicSpotDetail = async () => {
  loading.value = true
  try {
    const res = await getScenicSpotDetail(scenicSpotId.value)
    scenicSpot.value = res.data
  } catch (error) {
    console.error('获取景区详情失败:', error)
    ElMessage.error('获取景区详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.push('/admin/scenic-spots')
}

// 编辑景区
const handleEdit = () => {
  dialogVisible.value = true
}

// 删除景区
const handleDelete = () => {
  if (!scenicSpot.value) return

  ElMessageBox.confirm(`确认删除景区"${scenicSpot.value.name}"吗？此操作不可恢复`, '警告', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteScenicSpot(scenicSpotId.value)
      ElMessage.success('删除成功')
      router.push('/admin/scenic-spots')
    } catch (error) {
      console.error('删除景区失败:', error)
      ElMessage.error('删除景区失败，请稍后重试')
    }
  }).catch(() => {})
}

// 表单提交
const handleFormSubmit = () => {
  dialogVisible.value = false
  fetchScenicSpotDetail()
}

// 获取景区等级对应的标签类型
const getLevelTagType = (level: string) => {
  switch(level) {
    case '5A': return 'success'
    case '4A': return 'primary'
    case '3A': return 'warning'
    case '2A': return 'info'
    case '1A': return 'info'
    default: return ''
  }
}

// 显示景区等级
const displayLevel = (level: string | undefined) => {
  if (!level) return '未评级'
  return level === 'UNRATED' ? '未评级' : level
}

// 页面加载时获取景区详情
onMounted(() => {
  if (scenicSpotId.value) {
    fetchScenicSpotDetail()
  } else {
    loading.value = false
    ElMessage.error('景区ID无效')
  }
})
</script>

<style scoped>
.scenic-spot-detail-container {
  padding: 20px;
}

.scenic-spot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.scenic-spot-title {
  margin: 0 0 0 10px;
  font-size: 20px;
  font-weight: 600;
}

.skeleton-container {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.scenic-detail-card {
  margin-bottom: 20px;
}

.scenic-hero {
  display: flex;
  flex-direction: column;
}

.scenic-image {
  height: 240px;
  overflow: hidden;
  margin-bottom: 20px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.no-image {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 16px;
}

.scenic-info {
  padding: 0 20px;
}

.scenic-name {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: bold;
}

.scenic-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.meta-tag {
  margin-right: 10px;
  margin-bottom: 5px;
}

.info-section {
  margin-top: 20px;
  padding: 0 20px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 15px;
  font-weight: 600;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

.description-content {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
  min-height: 100px;
  white-space: pre-line;
}

@media (min-width: 768px) {
  .scenic-hero {
    flex-direction: row;
  }

  .scenic-image {
    width: 40%;
    height: auto;
    margin-bottom: 0;
    margin-right: 20px;
  }

  .scenic-info {
    flex: 1;
    padding: 0;
  }
}
</style> 