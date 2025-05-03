<template>
  <div class="scenic-spot-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      status-icon
    >
      <el-form-item label="景区名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入景区名称" />
      </el-form-item>

      <el-form-item label="景区图片" prop="imageUrl">
        <el-upload
          class="image-uploader"
          action="/api/upload/image"
          :show-file-list="false"
          :on-success="handleImageSuccess"
          :before-upload="beforeImageUpload"
        >
          <img v-if="form.imageUrl" :src="form.imageUrl" class="uploaded-image" />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="所在区域" class="region-selection-container">
        <div class="region-selection-wrapper">
          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item prop="provinceId" class="region-item">
                <div class="region-label">省份</div>
                <region-select
                  v-model="form.provinceId"
                  level="province"
                  placeholder="选择省份"
                  @change="handleProvinceChange"
                  class="enhanced-region-select"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="cityId" class="region-item">
                <div class="region-label">城市</div>
                <region-select
                  v-model="form.cityId"
                  level="city"
                  :parent-id="form.provinceId"
                  placeholder="选择城市"
                  :disabled="!form.provinceId"
                  @change="handleCityChange"
                  class="enhanced-region-select"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item prop="districtId" class="region-item">
                <div class="region-label">区县</div>
                <region-select
                  v-model="form.districtId"
                  level="district"
                  :parent-id="form.cityId"
                  placeholder="选择区县"
                  :disabled="!form.cityId"
                  class="enhanced-region-select"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <div v-if="hasRegionSelected" class="selected-region-info">
            <span class="info-label">已选区域：</span>
            <el-tag v-if="form.provinceName" class="region-tag">{{ form.provinceName }}</el-tag>
            <el-tag v-if="form.cityName" class="region-tag">{{ form.cityName }}</el-tag>
            <el-tag v-if="form.districtName" class="region-tag">{{ form.districtName }}</el-tag>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="详细地址" prop="address">
        <el-input v-model="form.address" placeholder="请输入详细地址" />
      </el-form-item>

      <el-form-item label="景区等级" prop="level">
        <el-select 
          v-model="form.level" 
          placeholder="请选择景区等级"
          class="larger-form-select"
          popper-class="larger-dropdown"
        >
          <el-option label="5A" value="5A" />
          <el-option label="4A" value="4A" />
          <el-option label="3A" value="3A" />
          <el-option label="2A" value="2A" />
          <el-option label="1A" value="1A" />
          <el-option label="未评级" value="UNRATED" />
        </el-select>
      </el-form-item>

      <el-form-item label="门票价格" prop="ticketPrice">
        <el-input-number
          v-model="form.ticketPrice"
          :min="0"
          :precision="2"
          :step="10"
          controls-position="right"
          placeholder="请输入门票价格"
          :formatter="(value) => `¥ ${value}`"
          :parser="(value) => value.replace('¥', '').trim()"
        />
        <span class="price-tip">填0表示免费</span>
      </el-form-item>

      <el-form-item label="营业时间" prop="businessHours">
        <el-input v-model="form.businessHours" placeholder="如: 09:00-17:00" />
      </el-form-item>

      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number
          v-model="form.sort"
          :min="0"
          :step="1"
          controls-position="right"
          placeholder="请输入排序值"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status" class="larger-radio-group">
          <el-radio label="active" class="larger-radio">启用</el-radio>
          <el-radio label="inactive" class="larger-radio">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="景区描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入景区描述"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ScenicSpotData } from '../../../types/scenicSpot'
import { addScenicSpot, updateScenicSpot } from '../../../api/scenicSpot'
import RegionSelect from '../../../components/RegionSelect.vue'
import axios from '../../../utils/axios'

const props = defineProps<{
  scenicSpot?: ScenicSpotData
  isEdit: boolean
}>()

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref<FormInstance>()
const form = reactive<ScenicSpotData>({
  id: undefined,
  name: '',
  description: '',
  provinceId: undefined,
  provinceName: '',
  cityId: undefined,
  cityName: '',
  districtId: undefined,
  districtName: '',
  address: '',
  level: undefined,
  businessHours: '',
  ticketPrice: undefined,
  contactPhone: '',
  imageUrl: '',
  status: 'active',
  sort: 0,
  createTime: '',
  updateTime: '',
  locationPath: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入景区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  provinceId: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  cityId: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  districtId: [
    { required: true, message: '请选择区县', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择景区等级', trigger: 'change' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-\d{7,8}$/, message: '请输入正确的联系电话', trigger: 'blur' }
  ],
  businessHours: [
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' }
  ]
})

// 初始化表单数据
onMounted(() => {
  if (props.isEdit && props.scenicSpot) {
    // 先将对象赋值过来
    Object.assign(form, props.scenicSpot)
    
    // 特殊处理：确保ticketPrice有正确的值，null或undefined时设为0
    if (form.ticketPrice === null || form.ticketPrice === undefined) {
      form.ticketPrice = 0
    }
  } else {
    // 新增时设置默认值
    form.ticketPrice = 0
  }
})

// 表单提交
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (props.isEdit) {
          await updateScenicSpot(form.id as number, form)
          ElMessage.success('更新景区成功')
        } else {
          await addScenicSpot(form)
          ElMessage.success('添加景区成功')
        }
        emit('submit')
      } catch (error) {
        console.error('保存景区失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败:', fields)
      ElMessage.error('请检查表单填写是否正确')
    }
  })
}

// 取消
const cancel = () => {
  emit('cancel')
}

// 图片上传相关
const handleImageSuccess = (response: any) => {
  form.imageUrl = response.data.url
}

const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 检查是否有选择区域
const hasRegionSelected = computed(() => {
  return !!form.provinceName || !!form.cityName || !!form.districtName;
});

// 地区选择联动
const handleProvinceChange = async (provinceId: number) => {
  form.cityId = undefined;
  form.districtId = undefined;
  form.cityName = '';
  form.districtName = '';
  
  if (provinceId) {
    try {
      // 获取省份名称
      const response = await axios.get(`/api/admin/regions/${provinceId}`);
      if (response.data && response.data.name) {
        form.provinceName = response.data.name;
      }
    } catch (error) {
      console.error('获取省份名称失败:', error);
    }
  } else {
    form.provinceName = '';
  }
}

const handleCityChange = async (cityId: number) => {
  form.districtId = undefined;
  form.districtName = '';
  
  if (cityId) {
    try {
      // 获取城市名称
      const response = await axios.get(`/api/admin/regions/${cityId}`);
      if (response.data && response.data.name) {
        form.cityName = response.data.name;
      }
    } catch (error) {
      console.error('获取城市名称失败:', error);
    }
  } else {
    form.cityName = '';
  }
}

// 监听区域ID变化
watch(() => form.districtId, async (districtId) => {
  if (districtId) {
    try {
      // 获取区县名称
      const response = await axios.get(`/api/admin/regions/${districtId}`);
      if (response.data && response.data.name) {
        form.districtName = response.data.name;
      }
    } catch (error) {
      console.error('获取区县名称失败:', error);
    }
  } else {
    form.districtName = '';
  }
});
</script>

<style scoped>
.scenic-spot-form {
  padding: 10px;
}

.larger-form-select {
  width: 100%;
}

:deep(.larger-form-select .el-input__inner) {
  height: 44px !important;
  line-height: 44px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
}

:deep(.el-form-item__label) {
  font-size: 16px !important;
  font-weight: 500 !important;
}

:deep(.larger-radio-group) {
  font-size: 16px !important;
}

:deep(.larger-radio) {
  margin-right: 20px !important;
  padding: 10px 0 !important;
}

:deep(.larger-radio .el-radio__label) {
  font-size: 16px !important;
  font-weight: 500 !important;
}

:deep(.el-input__inner) {
  height: 44px !important;
  line-height: 44px !important;
  font-size: 16px !important;
}

:deep(.el-input-number .el-input__inner) {
  height: 44px !important;
  line-height: 44px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
}

:deep(.el-textarea__inner) {
  font-size: 16px !important;
  line-height: 1.6 !important;
  padding: 12px 15px !important;
  min-height: 120px !important;
}

:deep(.el-select-dropdown__item.selected) {
  font-weight: bold !important;
  color: var(--el-color-primary) !important;
  background-color: rgba(64, 158, 255, 0.15) !important;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-uploader:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.region-selection-container {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 15px;
  background-color: #f5f7fa;
  margin-bottom: 20px;
}

.region-selection-wrapper {
  width: 100%;
}

.region-item {
  margin-bottom: 0;
}

.region-label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.enhanced-region-select {
  width: 100%;
}

:deep(.enhanced-region-select .el-input__wrapper) {
  background-color: #fff;
  border: 1px solid #dcdfe6;
  box-shadow: 0 0 0 1px rgba(220, 223, 230, 0.8);
  padding: 0 15px;
  height: 50px;
}

:deep(.enhanced-region-select:hover .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-primary) !important;
}

:deep(.enhanced-region-select .el-input__inner) {
  height: 50px !important;
  font-size: 16px !important;
  font-weight: 500 !important;
}

:deep(.enhanced-region-select .el-select__caret) {
  font-size: 18px !important;
  margin-right: 6px;
}

:deep(.el-select-dropdown.larger-region-dropdown) {
  min-width: 280px !important;
}

:deep(.larger-region-dropdown .el-select-dropdown__item) {
  height: auto !important;
  line-height: 1.5 !important;
  padding: 12px 20px !important;
  font-size: 16px !important;
}

:deep(.el-form-item.region-item .el-form-item__error) {
  position: static;
  margin-top: 5px;
}

.selected-region-info {
  margin-top: 15px;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

.info-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.region-tag {
  margin-right: 8px;
  font-size: 14px;
  padding: 6px 12px;
}

.price-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}
</style> 