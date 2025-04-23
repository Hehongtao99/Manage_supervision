<template>
  <div class="fee-standard-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>费用标准管理</span>
          <el-button type="primary" @click="handleAddFeeStandard">添加费用标准</el-button>
        </div>
      </template>
      
      <el-table :data="feeStandards" style="width: 100%" v-loading="loading">
        <el-table-column prop="feeName" label="费用名称" />
        <el-table-column prop="amount" label="金额">
          <template #default="scope">
            {{ scope.row.amount.toFixed(2) }} 元
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column prop="createTime" label="创建时间">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEditFeeStandard(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteFeeStandard(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑费用标准对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加费用标准' : '编辑费用标准'"
      width="500px"
    >
      <el-form :model="feeStandardForm" label-width="100px" :rules="rules" ref="feeStandardFormRef">
        <el-form-item label="费用名称" prop="feeName">
          <el-input v-model="feeStandardForm.feeName" placeholder="请输入费用名称" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="feeStandardForm.amount" :precision="2" :step="100" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input
            v-model="feeStandardForm.description"
            type="textarea"
            placeholder="请输入费用说明"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeeStandard" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { feeStandardApi, type FeeStandard } from '@/api/feeStandard';

// 费用标准列表
const feeStandards = ref<FeeStandard[]>([]);
const loading = ref(false);

// 对话框相关
const dialogVisible = ref(false);
const dialogType = ref<'add' | 'edit'>('add');
const submitting = ref(false);
const feeStandardFormRef = ref<FormInstance>();

// 表单数据
const feeStandardForm = reactive<FeeStandard>({
  feeName: '',
  amount: 0,
  description: ''
});

// 表单验证规则
const rules = reactive<FormRules>({
  feeName: [
    { required: true, message: '请输入费用名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '金额必须大于等于0', trigger: 'blur' }
  ]
});

// 格式化日期
const formatDate = (dateString: string | undefined) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 加载费用标准列表
const loadFeeStandards = async () => {
  loading.value = true;
  try {
    const response = await feeStandardApi.getAllFeeStandards();
    feeStandards.value = response.data;
  } catch (error) {
    console.error('加载费用标准失败:', error);
    ElMessage.error('加载费用标准失败');
  } finally {
    loading.value = false;
  }
};

// 处理添加费用标准
const handleAddFeeStandard = () => {
  dialogType.value = 'add';
  resetForm();
  dialogVisible.value = true;
};

// 处理编辑费用标准
const handleEditFeeStandard = (row: FeeStandard) => {
  dialogType.value = 'edit';
  Object.assign(feeStandardForm, row);
  dialogVisible.value = true;
};

// 处理删除费用标准
const handleDeleteFeeStandard = (row: FeeStandard) => {
  ElMessageBox.confirm('确定要删除该费用标准吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (row.id) {
        await feeStandardApi.deleteFeeStandard(row.id);
        ElMessage.success('删除成功');
        loadFeeStandards();
      }
    } catch (error) {
      console.error('删除费用标准失败:', error);
      ElMessage.error('删除费用标准失败');
    }
  }).catch(() => {});
};

// 提交费用标准表单
const submitFeeStandard = async () => {
  if (!feeStandardFormRef.value) return;
  
  await feeStandardFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (dialogType.value === 'add') {
          await feeStandardApi.createFeeStandard(feeStandardForm);
          ElMessage.success('添加成功');
        } else {
          if (feeStandardForm.id) {
            await feeStandardApi.updateFeeStandard(feeStandardForm.id, feeStandardForm);
            ElMessage.success('更新成功');
          }
        }
        dialogVisible.value = false;
        loadFeeStandards();
      } catch (error) {
        console.error('保存费用标准失败:', error);
        ElMessage.error('保存费用标准失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  if (feeStandardFormRef.value) {
    feeStandardFormRef.value.resetFields();
  }
  Object.assign(feeStandardForm, {
    id: undefined,
    feeName: '',
    amount: 0,
    description: ''
  });
};

// 组件挂载时加载数据
onMounted(() => {
  loadFeeStandards();
});
</script>

<style scoped>
.fee-standard-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 