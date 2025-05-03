<template>
  <div class="hotel-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="酒店名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入酒店名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="酒店等级" prop="level">
                <el-select v-model="form.level" placeholder="请选择酒店等级" style="width: 100%;">
                  <el-option label="五星级" value="五星级" />
                  <el-option label="四星级" value="四星级" />
                  <el-option label="三星级" value="三星级" />
                  <el-option label="经济型" value="经济型" />
                  <el-option label="未评级" value="未评级" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="起始价格" prop="startPrice">
                <el-input-number 
                  v-model="form.startPrice" 
                  :precision="2" 
                  :step="10" 
                  :min="0" 
                  style="width: 100%;"
                  placeholder="请输入起始价格"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="酒店描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入酒店描述"
            />
          </el-form-item>

          <el-form-item label="酒店图片" prop="imageUrl">
            <el-upload
              class="hotel-image-uploader"
              action="/api/admin/upload"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
            >
              <img v-if="form.imageUrl" :src="form.imageUrl" class="hotel-image" />
              <el-icon v-else class="hotel-image-uploader-icon"><plus /></el-icon>
            </el-upload>
            <div class="upload-tip">请上传酒店图片，建议尺寸16:9，大小不超过2MB</div>
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="地理位置" name="location">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="省份" prop="provinceId">
                <region-select 
                  v-model="form.provinceId" 
                  level="province" 
                  placeholder="请选择省份"
                  @change="handleProvinceChange"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" prop="cityId">
                <region-select 
                  v-model="form.cityId" 
                  level="city"
                  :parent-id="form.provinceId" 
                  placeholder="请选择城市"
                  :disabled="!form.provinceId"
                  @change="handleCityChange"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区县" prop="districtId">
                <region-select 
                  v-model="form.districtId" 
                  level="district"
                  :parent-id="form.cityId" 
                  placeholder="请选择区县"
                  :disabled="!form.cityId"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="详细地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入详细地址" />
          </el-form-item>

          <el-divider content-position="left">关联景区</el-divider>

          <el-form-item label="关联景区" prop="scenicSpotId">
            <scenic-spot-select 
              v-model="form.scenicSpotId" 
              :province-id="form.provinceId"
              :city-id="form.cityId"
              :district-id="form.districtId"
              :disabled="!form.provinceId"
              placeholder="请选择关联景区"
              @change="handleScenicSpotChange"
            />
          </el-form-item>

          <el-form-item label="距离景区" prop="distanceToSpot" v-if="form.scenicSpotId">
            <el-input-number 
              v-model="form.distanceToSpot" 
              :precision="1" 
              :step="0.5" 
              :min="0" 
              :max="100"
              style="width: 200px;"
              placeholder="请输入距离"
            >
              <template #suffix>公里</template>
            </el-input-number>
            <div class="input-tip">请输入酒店到景区的距离，单位为公里</div>
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="高级设置" name="advanced">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="排序值" prop="sort">
                <el-input-number
                  v-model="form.sort"
                  :min="0"
                  :max="9999"
                  placeholder="请输入排序值"
                  style="width: 100%;"
                />
                <div class="input-tip">数值越小，排序越靠前</div>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="isEdit">
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio label="active">启用</el-radio>
                  <el-radio label="inactive">禁用</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { HotelData } from '../../../types/hotel'
import { addHotel, updateHotel } from '../../../api/hotel'
import RegionSelect from '../../../components/RegionSelect.vue'
import ScenicSpotSelect from '../../../components/ScenicSpotSelect.vue'

const props = defineProps({
  hotel: {
    type: Object as () => HotelData,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref<FormInstance>()
const activeTab = ref('basic')
const submitting = ref(false)

// 表单数据
const form = reactive<HotelData>({
  id: props.hotel.id,
  name: props.hotel.name || '',
  description: props.hotel.description || '',
  provinceId: props.hotel.provinceId || null,
  cityId: props.hotel.cityId || undefined,
  districtId: props.hotel.districtId || undefined,
  address: props.hotel.address || '',
  level: props.hotel.level || undefined,
  imageUrl: props.hotel.imageUrl || '',
  contactPhone: props.hotel.contactPhone || '',
  scenicSpotId: props.hotel.scenicSpotId || undefined,
  distanceToSpot: props.hotel.distanceToSpot || undefined,
  startPrice: props.hotel.startPrice || undefined,
  status: props.hotel.status || 'active',
  sort: props.hotel.sort ?? 0
})

// 表单验证规则
const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入酒店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择酒店等级', trigger: 'change' }
  ],
  provinceId: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  cityId: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-\d{7,8}$/, message: '请输入正确的联系电话', trigger: 'blur' }
  ],
  distanceToSpot: [
    { type: 'number', message: '距离必须为数字', trigger: 'blur' }
  ]
})

// 图片上传前的校验
const beforeImageUpload = (file: File) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传成功回调
const handleImageSuccess = (response: any) => {
  form.imageUrl = response.data.url
}

// 省份变更处理
const handleProvinceChange = (value: number) => {
  form.cityId = undefined
  form.districtId = undefined
  form.scenicSpotId = undefined
}

// 城市变更处理
const handleCityChange = (value: number) => {
  form.districtId = undefined
  form.scenicSpotId = undefined
}

// 景区变更处理
const handleScenicSpotChange = (value: number) => {
  if (!value) {
    form.distanceToSpot = undefined
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (props.isEdit && form.id) {
          await updateHotel(form.id, form)
          ElMessage.success('更新酒店成功')
        } else {
          await addHotel(form)
          ElMessage.success('添加酒店成功')
        }
        emit('submit')
      } catch (error) {
        console.error('保存酒店失败:', error)
        ElMessage.error('保存失败，请检查表单并重试')
      } finally {
        submitting.value = false
      }
    } else {
      // 表单验证失败时，切换到第一个有错误的标签页
      if (formRef.value) {
        const firstErrorField = Object.keys(formRef.value.fields).find(field => {
          return formRef.value?.fields[field].validateState === 'error'
        })
        
        if (firstErrorField) {
          if (['name', 'level', 'contactPhone', 'startPrice', 'description', 'imageUrl'].includes(firstErrorField)) {
            activeTab.value = 'basic'
          } else if (['provinceId', 'cityId', 'districtId', 'address', 'scenicSpotId', 'distanceToSpot'].includes(firstErrorField)) {
            activeTab.value = 'location'
          } else {
            activeTab.value = 'advanced'
          }
        }
      }
      
      return false
    }
  })
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}

// 页面加载时初始化
onMounted(() => {
  // 如果是编辑模式，将对象中的属性赋值给表单
  if (props.isEdit && props.hotel) {
    Object.keys(props.hotel).forEach(key => {
      if (key in form) {
        // @ts-ignore
        form[key] = props.hotel[key]
      }
    })
  }
})
</script>

<style scoped>
.hotel-form {
  padding: 20px 0;
}

.form-actions {
  margin-top: 30px;
  text-align: right;
}

.hotel-image-uploader {
  width: 300px;
  height: 169px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.hotel-image-uploader:hover {
  border-color: #409eff;
}

.hotel-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 169px;
  line-height: 169px;
  text-align: center;
}

.hotel-image {
  width: 300px;
  height: 169px;
  display: block;
  object-fit: cover;
}

.upload-tip,
.input-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style> 