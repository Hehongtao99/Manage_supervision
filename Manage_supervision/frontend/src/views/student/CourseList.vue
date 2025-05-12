<template>
  <div class="course-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>课程浏览</span>
          <div class="filter-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程名称或教师"
              clearable
              @input="handleSearch"
              class="search-input"
              prefix-icon="Search"
            />
            <el-select v-model="subjectFilter" placeholder="课程科目" clearable @change="handleSearch">
              <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
            </el-select>
            <el-select v-model="priceFilter" placeholder="价格区间" clearable @change="handleSearch">
              <el-option label="全部价格" value="" />
              <el-option label="0-50元/小时" value="0-50" />
              <el-option label="50-100元/小时" value="50-100" />
              <el-option label="100-200元/小时" value="100-200" />
              <el-option label="200元以上/小时" value="200+" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="course-grid" v-loading="loading">
        <el-empty v-if="filteredCourses.length === 0" description="暂无可浏览的课程" />
        
        <div v-else class="courses-wrapper">
          <el-row :gutter="20">
            <el-col 
              v-for="course in filteredCourses" 
              :key="course.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6"
              :xl="6"
            >
              <el-card class="course-card" shadow="hover" @click="showCourseDetail(course)">
                <div class="course-image">
                  <el-image 
                    v-if="course.imagePaths && course.imagePaths.length > 0"
                    :src="course.imagePaths[0]" 
                    fit="cover"
                    @error="handleImageError(course)"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </div>
                <div class="course-info">
                  <h3 class="course-title">{{ course.title }}</h3>
                  <div class="course-teacher">
                    <el-icon><User /></el-icon>
                    <span>{{ course.teacherName }}</span>
                  </div>
                  <div class="course-subject">
                    <el-icon><Collection /></el-icon>
                    <span>{{ course.subject }}</span>
                  </div>
                  <div class="course-price">
                    <span class="price-label">¥{{ course.hourlyPrice }}/小时</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 添加分页控件 -->
          <div class="pagination-container">
            <el-pagination
              v-model:currentPage="pageParams.page"
              v-model:page-size="pageParams.size"
              :page-sizes="[12, 24, 36, 48]"
              :small="false"
              :background="true"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 课程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程详情"
      width="800px"
      top="5vh"
      destroy-on-close
      class="course-detail-dialog"
    >
      <div v-if="currentCourse" class="course-detail">
        <!-- 轮播图部分 -->
        <div class="course-carousel-container" v-if="currentCourse.imagePaths && currentCourse.imagePaths.length > 0">
          <el-carousel 
            :interval="3000" 
            :autoplay="true"
            height="300px" 
            indicator-position="outside" 
            arrow="always" 
            class="course-carousel"
          >
            <el-carousel-item v-for="(path, index) in currentCourse.imagePaths" :key="index">
              <div class="carousel-item-container">
                <el-image
                  :src="path"
                  fit="cover"
                  class="carousel-image"
                  @error="handleDetailImageError(index)"
                >
                  <template #error>
                    <div class="image-error-placeholder">
                      <el-icon><Picture /></el-icon>
                      <div>图片加载失败</div>
                    </div>
                  </template>
                </el-image>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="no-images-placeholder" v-else>
          <el-icon><Picture /></el-icon>
          <span>暂无课程图片</span>
        </div>

        <div class="course-info-container">
          <h2 class="course-detail-title">{{ currentCourse.title }}</h2>
          
          <div class="course-meta">
            <div class="meta-item">
              <el-icon><Collection /></el-icon>
              <span>科目：{{ currentCourse.subject }}</span>
            </div>
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>教师：{{ currentCourse.teacherName }}</span>
            </div>
            <div class="meta-item price-tag">
              <el-icon><Money /></el-icon>
              <span>每小时价格：</span>
              <span class="highlight-price">¥{{ currentCourse.hourlyPrice }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>工作时间：{{ currentCourse.workTimeStart }}:00 - {{ currentCourse.workTimeEnd }}:00</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>发布时间：{{ currentCourse.createTime }}</span>
            </div>
          </div>
          
          <div class="course-description">
            <h3>课程描述</h3>
            <p>{{ currentCourse.description }}</p>
          </div>

          <div class="actions">
            <el-button type="primary" size="large" @click="showOrderDialog">
              <el-icon><ShoppingCart /></el-icon>下单
            </el-button>
            <el-button type="default" size="large" @click="startChat(currentCourse.teacherId)">
              <el-icon><ChatDotRound /></el-icon>联系教师
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 课程下单对话框 -->
    <el-dialog
      v-model="orderDialogVisible"
      title="确认课程订单"
      width="500px"
      destroy-on-close
    >
      <div v-if="currentCourse" class="order-form">
        <el-form ref="orderForm" :model="orderForm" label-width="100px">
          <el-form-item label="课程名称">
            <span>{{ currentCourse.title }}</span>
          </el-form-item>
          <el-form-item label="教师">
            <span>{{ currentCourse.teacherName }}</span>
          </el-form-item>
          <el-form-item label="科目">
            <span>{{ currentCourse.subject }}</span>
          </el-form-item>
          <el-form-item label="课时单价">
            <span class="highlight-price">¥{{ currentCourse.hourlyPrice }}/小时</span>
          </el-form-item>
          <el-form-item label="购买小时数" prop="hours" required>
            <el-input-number 
              v-model="orderForm.hours" 
              :min="1" 
              :max="100"
              @change="calculateTotal"
              @input="val => calculateTotal(val)"
            ></el-input-number>
          </el-form-item>
          <el-form-item label="总价">
            <span class="highlight-price">¥{{ formattedTotal }}</span>
          </el-form-item>
          <el-form-item label="留言">
            <el-input 
              v-model="orderForm.message" 
              type="textarea" 
              rows="3" 
              placeholder="请输入留言（选填）"
            ></el-input>
          </el-form-item>
          <el-form-item label="支付方式" required>
            <div class="payment-method-buttons">
              <div 
                class="payment-method-btn" 
                :class="{ active: orderForm.paymentMethod === 'wechat' }"
                @click="selectPaymentMethod('wechat')"
              >
                <span>微信支付</span>
              </div>
              <div 
                class="payment-method-btn" 
                :class="{ active: orderForm.paymentMethod === 'alipay' }"
                @click="selectPaymentMethod('alipay')"
              >
                <span>支付宝</span>
              </div>
            </div>
            <div class="payment-method-tip" v-if="!orderForm.paymentMethod">
              <el-alert
                title="请选择一种支付方式"
                type="warning"
                :closable="false"
                show-icon
                size="small"
              />
            </div>
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="orderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder" :loading="submitLoading">确认下单</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 支付二维码对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      :title="getPaymentDialogTitle"
      width="400px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="handlePaymentDialogClosed"
    >
      <div class="payment-dialog-content">
        <!-- 添加支付方式选择区域 -->
        <div class="payment-methods">
          <h3>请选择支付方式</h3>
          <div class="payment-method-buttons">
            <div 
              class="payment-method-btn" 
              :class="{ active: currentPaymentMethodValue === 'wechat' }"
              @click="selectPaymentMethodForExistingOrder('wechat')"
            >
              <span>微信支付</span>
            </div>
            <div 
              class="payment-method-btn" 
              :class="{ active: currentPaymentMethodValue === 'alipay' }"
              @click="selectPaymentMethodForExistingOrder('alipay')"
            >
              <span>支付宝</span>
            </div>
          </div>
        </div>
        
        <div class="qr-code-container">
          <div v-if="currentPaymentMethodValue === 'wechat'" class="qr-code-image wechat-qr">
            <div class="qr-inner">
              <QRCode
                :value="getPaymentQrValue"
                :size="150"
                level="H"
                render-as="svg"
              />
              <span class="qr-logo-text">微信</span>
            </div>
          </div>
          <div v-else-if="currentPaymentMethodValue === 'alipay'" class="qr-code-image alipay-qr">
            <div class="qr-inner">
              <QRCode
                :value="getPaymentQrValue"
                :size="150"
                level="H"
                render-as="svg"
              />
              <span class="qr-logo-text">支付宝</span>
            </div>
          </div>
          <div v-else class="qr-code-placeholder">
            <el-icon><Picture /></el-icon>
            <span>请选择支付方式</span>
          </div>
        </div>
        
        <div class="payment-info">
          <p>订单金额: <span class="highlight-price">¥{{ currentOrderAmount }}</span></p>
          <div v-if="currentPaymentMethodValue">
            <p class="payment-tip">请使用{{ currentPaymentMethodValue === 'wechat' ? '微信' : '支付宝' }}扫码支付</p>
            <p class="payment-tip">支付后请点击"确认支付"按钮</p>
          </div>
          <p v-else class="payment-tip warning-tip">请先选择支付方式</p>
        </div>
        
        <div class="payment-actions">
          <el-button @click="handleCancelPayment" type="default">取消支付</el-button>
          <el-button @click="handleConfirmPayment" type="primary">确认支付</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 取消支付确认对话框 -->
    <el-dialog
      v-model="cancelPaymentDialogVisible"
      title="取消订单"
      width="400px"
      destroy-on-close
    >
      <div class="cancel-order-content">
        <p>确定要取消此订单吗？</p>
        <p class="warning-text">注意：取消订单后，相应的收入将被扣除。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPaymentDialogVisible = false">返回</el-button>
          <el-button type="danger" @click="confirmCancelPayment" :loading="cancelLoading">确认取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Picture, User, Collection, ChatDotRound, Money, Clock, Calendar, ShoppingCart } from '@element-plus/icons-vue'
import { getAllApprovedCourses, CourseDTO, startChatWithTeacher, getApprovedCoursesPaged, PageParams, PageResult } from '../../api/courses'
import { createOrder, cancelOrder, directCancelOrder } from '../../api/order'
import { useChatStore } from '../../stores/chat'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode.vue'

// 工具函数：格式化价格，确保显示为有效数字
const formatPrice = (price: number | undefined | null): number => {
  if (price === undefined || price === null || isNaN(price)) {
    return 0;
  }
  return parseFloat(Number(price).toFixed(2));
}

// 状态变量
const loading = ref(false)
const courses = ref<CourseDTO[]>([])
const searchKeyword = ref('')
const subjectFilter = ref('')
const priceFilter = ref('')
const subjects = ref<string[]>([])
const detailDialogVisible = ref(false)
const currentCourse = ref<CourseDTO | null>(null)
const currentConversation = ref(null)
const chatStore = useChatStore()
const router = useRouter()

// 下单相关状态
const orderDialogVisible = ref(false)
const orderForm = ref({
  hours: 1,
  message: '',
  totalAmount: 0,
  paymentMethod: '' // 默认不选择支付方式，需要用户主动选择
})
const submitLoading = ref(false)

// 支付相关状态
const paymentDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)
const currentOrderAmount = ref<string>('0.00')
const cancelPaymentDialogVisible = ref(false)
const cancelLoading = ref(false)
const currentPaymentMethodValue = ref('') // 添加新的ref变量

// 获取支付对话框标题
const getPaymentDialogTitle = computed(() => {
  if(currentPaymentMethodValue.value === 'wechat') {
    return '微信支付';
  } else if(currentPaymentMethodValue.value === 'alipay') {
    return '支付宝支付';
  } else {
    return '请选择支付方式';
  }
})

// 引入分页相关接口和方法
import { getApprovedCoursesPaged, PageParams, PageResult } from '../../api/courses'

// 修改课程获取方法
const pageParams = reactive<PageParams>({
  page: 1,
  size: 12 // 或者根据UI合理设置
});

const total = ref(0);

// 在计算属性中过滤课程
const filteredCourses = computed(() => {
  let result = [...courses.value]
  
  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(course => 
      course.title.toLowerCase().includes(keyword) ||
      course.teacherName.toLowerCase().includes(keyword) ||
      course.description.toLowerCase().includes(keyword)
    )
  }
  
  // 科目过滤
  if (subjectFilter.value) {
    result = result.filter(course => course.subject === subjectFilter.value)
  }
  
  // 价格过滤
  if (priceFilter.value) {
    const priceRange = priceFilter.value.split('-')
    if (priceRange.length === 2) {
      const minPrice = Number(priceRange[0])
      const maxPrice = Number(priceRange[1])
      result = result.filter(course => 
        course.hourlyPrice >= minPrice && course.hourlyPrice <= maxPrice
      )
    } else if (priceFilter.value.endsWith('+')) {
      const minPrice = Number(priceFilter.value.replace('+', ''))
      result = result.filter(course => course.hourlyPrice >= minPrice)
    }
  }
  
  return result
});

// 计算属性：总价格
const calculatedTotal = computed(() => {
  if (!currentCourse.value || !orderForm.value?.hours) return 0;
  const price = currentCourse.value.hourlyPrice || 0;
  const hours = orderForm.value?.hours || 0;
  return formatPrice(price * hours);
});

// 格式化显示的总价
const formattedTotal = computed(() => {
  const total = calculatedTotal.value;
  return total.toFixed(2);
});

// 监听分页参数变化重新加载数据
watch(pageParams, async () => {
  await fetchCourses();
});

// 获取课程列表（分页）
const fetchCourses = async () => {
  loading.value = true;
  try {
    const data = await getApprovedCoursesPaged(pageParams);
    courses.value = data.records;
    total.value = data.total;
    
    // 提取所有科目
    const subjectSet = new Set<string>();
    data.records.forEach((course: CourseDTO) => {
      if (course.subject) {
        subjectSet.add(course.subject);
      }
    });
    subjects.value = Array.from(subjectSet);
  } catch (error) {
    ElMessage.error('获取课程列表失败');
  } finally {
    loading.value = false;
  }
};

// 处理搜索
const handleSearch = () => {
  // 使用计算属性自动过滤
}

// 显示课程详情
const showCourseDetail = (course: CourseDTO) => {
  currentCourse.value = course
  detailDialogVisible.value = true
}

// 处理图片加载错误
const handleImageError = (course: CourseDTO) => {
  // 当图片加载失败时，设置为默认图片或从数组中移除
  if (course.imagePaths && course.imagePaths.length > 0) {
    // 简单处理：将第一张图片替换为默认图片
    course.imagePaths[0] = '/path/to/default/image.jpg'
  }
}

// 处理详情页图片加载错误
const handleDetailImageError = (index: number) => {
  if (currentCourse.value?.imagePaths) {
    // 可以选择移除图片或替换为默认图片
    currentCourse.value.imagePaths.splice(index, 1)
  }
}

// 开始与老师聊天
const startChat = async (teacherId: number) => {
  if (!teacherId) {
    ElMessage.error('教师ID无效，无法开始聊天')
    return
  }

  try {
    ElMessage({
      message: '正在连接聊天...',
      type: 'info',
      duration: 1500,
      showClose: false
    })

    console.log('开始与教师ID为', teacherId, '聊天')
    const response = await startChatWithTeacher(teacherId)
    console.log('聊天会话创建响应:', response)

    // 修复：response可能直接是会话对象，也可能在data字段中
    const conversationData = response.data || response
    
    if (!conversationData || !conversationData.id) {
      ElMessage.closeAll()
      ElMessage.error('创建聊天会话失败')
      console.error('创建聊天会话失败:', response)
      return
    }

    // 设置当前会话
    currentConversation.value = conversationData
    console.log('当前会话设置为:', currentConversation.value)

    // 检查会话是否已加载到聊天存储
    if (!chatStore.conversations.some(c => c.id === conversationData.id)) {
      // 需要添加会话到聊天存储
      chatStore.conversations.push(conversationData)
    }

    // 关闭加载消息
    ElMessage.closeAll()
    
    // 隐藏课程详情对话框
    detailDialogVisible.value = false
    
    // 重定向到聊天页面，而不是显示聊天对话框
    router.push({ 
      path: '/chat',
      query: { 
        conversationId: conversationData.id.toString()
      } 
    })
  } catch (error) {
    ElMessage.closeAll()
    console.error('聊天初始化错误:', error)
    ElMessage.error('连接聊天失败，请稍后再试')
    
    // 显示详细错误信息在控制台
    if (error.response) {
      console.error('错误响应数据:', error.response.data)
      console.error('错误状态码:', error.response.status)
    }
  }
}

// 显示下单对话框
const showOrderDialog = () => {
  if (!currentCourse.value) return
  
  // 确保课程价格是有效数字
  const hourlyPrice = currentCourse.value.hourlyPrice || 0
  
  // 计算默认总价
  const totalAmount = formatPrice(hourlyPrice * 1)
  
  // 重置订单表单
  orderForm.value = {
    hours: 1,
    message: '',
    totalAmount: totalAmount, // 格式化初始总价
    paymentMethod: '' // 默认不选择支付方式，需要用户主动选择
  }
  
  console.log('初始化订单:', {
    courseId: currentCourse.value.id,
    title: currentCourse.value.title,
    hourlyPrice: hourlyPrice,
    totalAmount: orderForm.value.totalAmount,
    paymentMethod: orderForm.value.paymentMethod
  })
  
  orderDialogVisible.value = true
}

// 计算总价
const calculateTotal = (val: number) => {
  if (!currentCourse.value) return
  
  // 确保val是有效数字
  const hours = (!isNaN(val) && val > 0) ? val : 1;
  
  // 获取课程单价，确保是有效数字
  const hourlyPrice = currentCourse.value.hourlyPrice || 0;
  
  // 计算并更新总价，使用格式化函数确保是有效数字
  const total = formatPrice(hourlyPrice * hours);
  console.log('计算总价: 小时数 =', hours, '单价 =', hourlyPrice, '总价 =', total);
  
  // 确保orderForm.value不为null
  if (!orderForm.value) return;
  
  // 确保更新是响应式的
  orderForm.value = {
    ...orderForm.value,
    totalAmount: total
  };
}

// 监听小时数变化
watch(() => orderForm.value?.hours, (newVal) => {
  if (newVal) {
    calculateTotal(newVal)
  }
})

// 提交订单
const submitOrder = async () => {
  if (!currentCourse.value || !orderForm.value) return
  
  // 验证支付方式是否已选择
  if (!orderForm.value.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  submitLoading.value = true
  try {
    // 确保courseId是有效的
    if (!currentCourse.value.id) {
      ElMessage.error('课程ID无效')
      submitLoading.value = false
      return
    }
    
    const courseId = Number(currentCourse.value.id)
    const hours = Number(orderForm.value.hours)
    const price = Number(currentCourse.value.hourlyPrice)
    
    console.log('提交订单参数:', {
      courseId: courseId,
      hours: hours,
      price: price,
      message: orderForm.value.message,
      paymentMethod: orderForm.value.paymentMethod
    })
    
    const result = await createOrder(
      courseId,
      hours,
      price,
      orderForm.value.message
    )
    
    if (result.success) {
      // 保存当前支付方式，用于二维码页面显示
      currentPaymentMethodValue.value = orderForm.value.paymentMethod
      
      // 保存当前订单金额
      currentOrderAmount.value = formattedTotal.value
      
      // 关闭订单对话框
      orderDialogVisible.value = false
      
      // 存储订单ID，用于支付成功后跳转或取消
      currentOrderId.value = result.orderId
      
      // 显示支付二维码对话框
      paymentDialogVisible.value = true
    } else {
      ElMessage.error(result.message || '订单创建失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('订单提交失败，请稍后再试')
  } finally {
    submitLoading.value = false
  }
}

// 处理取消支付
const handleCancelPayment = () => {
  cancelPaymentDialogVisible.value = true;
}

// 确认取消支付
const confirmCancelPayment = async () => {
  if (!currentOrderId.value) return;
  
  cancelLoading.value = true;
  try {
    const result = await directCancelOrder(currentOrderId.value);
    
    if (result.success) {
      ElMessage.success('订单已取消，相应收入已扣除');
      
      // 关闭所有相关对话框
      cancelPaymentDialogVisible.value = false;
      paymentDialogVisible.value = false;
      
      // 重置订单ID和相关状态
      currentOrderId.value = null;
      currentPaymentMethodValue.value = '';
      
      // 可选: 跳转到订单列表
      router.push('/orders');
    } else {
      ElMessage.error(result.message || '取消订单失败');
    }
  } catch (error) {
    console.error('取消订单失败:', error);
    ElMessage.error('取消订单失败，请稍后再试');
  } finally {
    cancelLoading.value = false;
  }
}

// 处理确认支付
const handleConfirmPayment = async () => {
  try {
    // 这里应该调用确认支付的API，但由于没有实际的支付功能，我们直接模拟成功
    ElMessage.success('支付成功')
    
    // 关闭支付对话框
    paymentDialogVisible.value = false
    
    // 重置支付状态
    currentOrderId.value = null
    currentPaymentMethodValue.value = '';
    
    // 跳转到订单列表页面
    router.push('/orders')
  } catch (error) {
    console.error('确认支付失败:', error)
    ElMessage.error('确认支付失败，请稍后再试')
  }
}

// 处理支付对话框关闭事件
const handlePaymentDialogClosed = () => {
  // 如果对话框是通过x按钮关闭的，而不是通过取消支付或确认支付，我们可能需要处理一些状态
  if (currentOrderId.value) {
    // 打开取消支付对话框，询问用户是否要取消
    handleCancelPayment();
  }
}

// 处理支付方式选择
const selectPaymentMethod = (method: string) => {
  if (orderForm.value) {
    orderForm.value.paymentMethod = method;
  }
}

// 为已创建的订单选择支付方式
const selectPaymentMethodForExistingOrder = (method: string) => {
  console.log(`选择支付方式: ${method}`);
  currentPaymentMethodValue.value = method;
}

// 获取支付二维码值
const getPaymentQrValue = computed(() => {
  if (!currentOrderId.value || !currentPaymentMethodValue.value) return '';
  
  return `order:${currentOrderId.value}:${currentPaymentMethodValue.value}`;
});

// 处理页码变化
const handlePageChange = (newPage: number) => {
  pageParams.page = newPage;
};

// 处理每页条数变化
const handleSizeChange = (newSize: number) => {
  pageParams.size = newSize;
  pageParams.page = 1; // 重置到第一页
};

// 在组件挂载时获取课程数据
onMounted(() => {
  fetchCourses();
});
</script>

<style scoped>
.course-list {
  margin: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.filter-container {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-container {
    width: 100%;
    margin-top: 15px;
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
  }
}

.search-input {
  width: 200px;
}

.courses-wrapper {
  margin-top: 20px;
}

.course-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.15);
}

.course-image {
  height: 180px;
  overflow: hidden;
}

.course-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}

.course-card:hover .course-image .el-image {
  transform: scale(1.05);
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  font-size: 40px;
  color: #c0c4cc;
}

.course-info {
  padding: 16px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.course-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  height: 45px;
}

.course-teacher, .course-subject {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.course-teacher .el-icon, .course-subject .el-icon {
  margin-right: 6px;
  font-size: 16px;
  color: #909399;
}

.course-price {
  margin-top: 10px;
  text-align: right;
}

.price-label {
  color: #e6a23c;
  font-weight: bold;
  font-size: 18px;
}

/* 课程详情页样式 */
.course-detail-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.course-detail {
  padding: 20px;
}

.course-carousel-container {
  margin-bottom: 30px;
}

.course-carousel {
  width: 100%;
}

.carousel-item-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 12px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.course-carousel:hover .carousel-image {
  transform: scale(1.02);
}

.image-error-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.image-error-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.no-images-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 8px;
  margin-bottom: 30px;
}

.no-images-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.course-info-container {
  padding: 0 10px;
}

.course-detail-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
  text-align: center;
  position: relative;
}

.course-detail-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, #1989fa, #409EFF);
  border-radius: 3px;
}

.course-meta {
  margin-bottom: 25px;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.meta-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.meta-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #409EFF;
}

.price-tag .highlight-price {
  color: #ff6700;
  font-weight: bold;
  font-size: 18px;
  margin-left: 4px;
}

.course-description {
  margin-bottom: 30px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.course-description h3 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.course-description p {
  line-height: 1.8;
  color: #606266;
  white-space: pre-line;
}

.actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.actions .el-button {
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 25px;
  transition: all 0.3s;
}

.actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(64, 158, 255, 0.3);
}

/* 聊天窗口样式 */
.chat-dialog :deep(.el-dialog__body) {
  padding: 0;
  height: 500px;
}

.chat-error-container {
  padding: 20px;
  text-align: center;
  border-radius: 8px;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
  margin: 20px 0;
}

.chat-error-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.chat-loading {
  padding: 40px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 20px;
}

.chat-loading .is-loading {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 16px;
}

.chat-dialog {
  width: 700px;
  max-width: 90vw !important;
}

@media (max-width: 768px) {
  .chat-dialog {
    width: 90vw !important;
  }
}

.order-form {
  padding: 0 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.highlight-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 1.1em;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

/* 支付相关样式 */
.qr-code-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.qr-code-image {
  width: 200px;
  height: 200px;
  background-color: #fff;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.wechat-qr {
  background-color: #2aae67;
}

.alipay-qr {
  background-color: #00a0e9;
}

.qr-inner {
  width: 180px;
  height: 180px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  border-radius: 4px;
}

.qr-logo-text {
  margin-top: 10px;
  font-weight: bold;
  color: #303133;
}

.qr-code-placeholder {
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 8px;
}

.qr-code-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.payment-info {
  text-align: center;
  margin-bottom: 20px;
}

.payment-tip {
  color: #909399;
  font-size: 14px;
  margin: 8px 0;
}

.warning-tip {
  color: #E6A23C;
  font-weight: bold;
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.payment-method-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
}

.payment-method-btn {
  padding: 10px 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
}

.payment-method-btn:hover {
  background-color: #f5f7fa;
  border-color: #409EFF;
}

.payment-method-btn.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
  font-weight: bold;
}

.payment-method-tip {
  margin-top: 5px;
}

/* 添加分页容器样式 */
.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-container {
    width: 100%;
    margin-top: 15px;
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
  }
}

.payment-methods {
  margin-bottom: 20px;
  text-align: center;
}

.payment-methods h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
}

.cancel-order-content {
  padding: 20px;
}

.warning-text {
  color: #E6A23C;
  font-weight: bold;
  margin-top: 10px;
}
</style> 