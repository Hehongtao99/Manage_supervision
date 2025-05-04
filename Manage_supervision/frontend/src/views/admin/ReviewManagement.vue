<template>
  <div class="review-management">
    <h1 class="title">评论审核管理</h1>
    
    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-header">
        <h3>当前显示: {{ queryParams.status ? statusLabel(queryParams.status) : '全部评价' }}</h3>
        <div class="filter-tip">提示: 审核后评论不会从列表中消失，只是状态会发生变化。您可以通过筛选查看不同状态的评论。</div>
      </div>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable @change="handleFilter">
            <el-option label="全部评价" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetQuery">显示全部</el-button>
          <el-button type="warning" @click="loadTestData">添加测试数据</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 评论列表 -->
    <el-card shadow="never" class="list-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <el-empty 
        v-else-if="reviewList.length === 0" 
        :description="queryParams.status ? 
          `暂无${statusLabel(queryParams.status)}评论` : 
          '暂无评论数据'"
        :image-size="120">
      </el-empty>
      
      <el-table v-else :data="reviewList" style="width: 100%" border v-loading="loading" element-loading-text="加载中...">
        <el-table-column prop="orderNumber" label="订单编号" width="150" />
        <el-table-column label="评价人" width="120">
          <template #default="scope">
            <span v-if="scope.row.anonymous">匿名用户</span>
            <span v-else>{{ scope.row.reviewerName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="被评价陪玩" width="120">
          <template #default="scope">
            {{ scope.row.companionName }}
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <div class="review-content-cell">
              <span :class="{'pending-review': scope.row.reviewStatus === 'pending',
                            'approved-review': scope.row.reviewStatus === 'approved',
                            'rejected-review': scope.row.reviewStatus === 'rejected'}">
                {{ scope.row.content || '无评论内容' }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="160" />
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.reviewStatus)">
              {{ statusLabel(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.reviewStatus === 'pending'"
              size="small"
              type="success"
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.reviewStatus === 'pending'"
              size="small"
              type="danger"
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 拒绝理由对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝评论" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝理由" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
    
    <!-- 评论详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评论详情" width="600px">
      <template v-if="currentReview">
        <div class="review-detail">
          <div class="header">
            <div class="order-info">
              <h3>订单信息</h3>
              <p>订单编号：{{ currentReview.orderNumber }}</p>
              <p>游戏类型：{{ currentReview.gameType || '未指定' }}</p>
              <p>服务名称：{{ currentReview.serviceTitle }}</p>
            </div>
            <div class="user-info">
              <h3>用户信息</h3>
              <p>评价人：{{ currentReview.anonymous ? '匿名用户' : currentReview.reviewerName }}</p>
              <p>陪玩：{{ currentReview.companionName }}</p>
              <p>评价时间：{{ currentReview.createTime }}</p>
            </div>
          </div>
          
          <div class="review-content">
            <h3>评价内容</h3>
            <div class="rating">
              <span>评分：</span>
              <el-rate v-model="currentReview.rating" disabled text-color="#ff9900" />
            </div>
            <div class="content">
              <p>{{ currentReview.content || '用户未填写评价内容' }}</p>
            </div>
          </div>
          
          <div v-if="currentReview.replied" class="reply-content">
            <h3>陪玩回复</h3>
            <p>{{ currentReview.reply }}</p>
            <p class="reply-time">回复时间：{{ currentReview.replyTime }}</p>
          </div>
          
          <div class="audit-info" v-if="currentReview.reviewStatus !== 'pending'">
            <h3>审核信息</h3>
            <p>审核状态：
              <el-tag :type="statusTagType(currentReview.reviewStatus)">
                {{ statusLabel(currentReview.reviewStatus) }}
              </el-tag>
            </p>
            <p v-if="currentReview.reviewTime">审核时间：{{ currentReview.reviewTime }}</p>
            <p v-if="currentReview.reviewComment">拒绝理由：{{ currentReview.reviewComment }}</p>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getReviewsByStatus, approveReview, rejectReview, getReviewDetail } from '../../api/admin'
import axios from '../../utils/axios'

// 数据定义
const reviewList = ref([])
const total = ref(0)
const queryParams = reactive({
  status: '',
  page: 1,
  size: 10
})
const loading = ref(false)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  reason: '',
  reviewId: null
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentReview = ref(null)

// 初始化加载
onMounted(() => {
  console.log('组件挂载，开始加载评论列表');
  // 初始化时尝试加载所有评论
  queryParams.status = '';
  loadReviewList();
})

// 加载评论列表
const loadReviewList = async () => {
  loading.value = true;
  reviewList.value = []; // 清空当前列表，避免显示旧数据
  
  try {
    console.log('加载评论列表，状态:', queryParams.status, '页码:', queryParams.page, '每页数量:', queryParams.size);
    
    const response = await getReviewsByStatus(
      queryParams.status,
      queryParams.page,
      queryParams.size
    );
    
    console.log('评论列表加载成功，数据:', JSON.stringify(response));
    
    if (response && response.records) {
      reviewList.value = response.records;
      total.value = response.total || 0;
      console.log('成功设置评论列表数据，条数:', reviewList.value.length);
    } else {
      console.warn('API返回成功但没有records字段:', response);
      reviewList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取评论列表失败，错误详情:', error);
    if (error.response) {
      console.error('错误响应:', error.response.data, '状态码:', error.response.status);
    }
    reviewList.value = [];
    total.value = 0;
    ElMessage.error('获取评论列表失败，请稍后重试');
  } finally {
    loading.value = false;
    console.log('列表加载完成，当前数据条数:', reviewList.value.length);
  }
}

// 状态标签类型
const statusTagType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

// 状态显示文本
const statusLabel = (status) => {
  const labels = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return labels[status] || '未知'
}

// 筛选处理
const handleFilter = () => {
  queryParams.page = 1
  loadReviewList()
}

// 重置查询
const resetQuery = () => {
  queryParams.status = ''
  queryParams.page = 1
  loadReviewList()
}

// 页码大小变化
const handleSizeChange = (size) => {
  queryParams.size = size
  loadReviewList()
}

// 页码变化
const handleCurrentChange = (page) => {
  queryParams.page = page
  loadReviewList()
}

// 审核通过
const handleApprove = (row) => {
  ElMessageBox.confirm('确认通过此评论？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在处理...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    try {
      console.log('开始审核通过评论，ID:', row.id);
      const result = await approveReview(row.id)
      console.log('审核通过响应:', result);
      ElMessage.success('审核通过成功')
      
      // 保持当前筛选状态，不做改变
      await loadReviewList()
    } catch (error) {
      console.error('审核操作失败：', error)
      ElMessage.error('审核操作失败，请稍后重试')
    } finally {
      loadingInstance.close()
    }
  }).catch(() => {})
}

// 审核拒绝
const handleReject = (row) => {
  rejectForm.reviewId = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入拒绝理由')
    return
  }
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在处理...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    console.log('开始拒绝评论，ID:', rejectForm.reviewId, '理由:', rejectForm.reason);
    const result = await rejectReview(rejectForm.reviewId, rejectForm.reason)
    console.log('拒绝评论响应:', result);
    ElMessage.success('拒绝评论成功')
    rejectDialogVisible.value = false
    
    // 保持当前筛选状态，不做改变
    await loadReviewList()
  } catch (error) {
    console.error('拒绝评论失败：', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loadingInstance.close()
  }
}

// 查看详情
const handleDetail = async (row) => {
  try {
    const reviewDetail = await getReviewDetail(row.id)
    currentReview.value = reviewDetail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取评论详情失败：', error)
    ElMessage.error('获取评论详情失败，请稍后重试')
  }
}

// 加载测试数据
const loadTestData = async () => {
  try {
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在添加测试数据...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    console.log('开始加载测试数据');
    const response = await axios.get('/api/admin/reviews/test-data');
    console.log('测试数据加载结果:', response);
    
    if (response.data.success) {
      ElMessage.success(response.data.message || '测试数据添加成功');
      loadReviewList(); // 重新加载列表
    } else {
      ElMessage.error(response.data.message || '测试数据添加失败');
    }
    
    loadingInstance.close();
  } catch (error) {
    console.error('添加测试数据失败:', error);
    ElMessage.error('添加测试数据失败，请检查控制台日志');
  }
}
</script>

<style scoped>
.review-management {
  padding: 20px;
}

.title {
  margin-bottom: 20px;
  font-weight: 500;
  color: #303133;
}

.filter-section {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.filter-header {
  margin-bottom: 15px;
}

.filter-header h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 500;
}

.filter-tip {
  color: #909399;
  font-size: 13px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.review-detail {
  padding: 10px;
}

.review-detail .header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.review-detail .header > div {
  flex: 1;
}

.review-detail h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.review-detail p {
  margin: 5px 0;
  color: #606266;
}

.review-content {
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.rating {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.rating span {
  margin-right: 10px;
}

.content {
  padding: 10px;
  background-color: #f7f8fa;
  border-radius: 4px;
  min-height: 60px;
}

.reply-content {
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.audit-info {
  padding-top: 10px;
}

.loading-container {
  padding: 20px;
}

.review-content-cell {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pending-review {
  font-weight: bold;
  color: #e6a23c;
}

.approved-review {
  color: #67c23a;
}

.rejected-review {
  color: #f56c6c;
  text-decoration: line-through;
}

.el-table :deep(td) {
  padding: 8px 0;
}

.table-expanded-row {
  background-color: #f5f7fa;
}

.table-expanded-row td {
  padding: 16px;
}
</style> 