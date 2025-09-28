<template>
  <el-dialog
      :model-value="modelValue"
      title="订单回访"
      width="600px"
      @close="handleClose"
  >
    <el-form
        ref="revisitFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="revisit-form"
    >
      <!-- 订单基本信息 -->
      <div class="order-base-info">
        <h4>订单基本信息</h4>
        <p>服务老人：{{ getOrderInfo(props.order)?.serviceObjectName || '—' }}</p>
        <p>服务类型：{{ getServiceTypeText(getOrderInfo(props.order)?.code) }}</p>
        <p>服务时间：{{ formatTime(getOrderInfo(props.order)?.serviceStartTime) }} - {{ formatTime(getOrderInfo(props.order)?.serviceEndTime) }}</p>
        <p>志愿者姓名：{{ getVolunteerName(getOrderInfo(props.order)?.volunteerUserId) || '—' }}</p>
      </div>

      <el-form-item label="回访记录" prop="revisitRecord">
        <el-input
            v-model="form.revisitRecord"
            type="textarea"
            :rows="6"
            placeholder="请输入回访记录（最多1000个字符）"
            maxlength="1000"
            show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { revisitOrder } from '@/api/auth.js';
import dayjs from 'dayjs';

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  order: {
    type: Object,
    default: () => ({})
  }
});

// Emits
const emit = defineEmits(['update:modelValue', 'success']);

// 响应式数据
const loading = ref(false);
const revisitFormRef = ref();

const form = reactive({
  revisitRecord: ''
});

const rules = reactive({
  revisitRecord: [
    { required: true, message: '请输入回访记录', trigger: 'blur' },
    { max: 1000, message: '回访记录不能超过1000个字符', trigger: 'blur' }
  ]
});

// Watchers
watch(() => props.modelValue, (val) => {
  if (val) {
    resetForm();
  }
}, { immediate: true });

// 方法
// 安全获取orderInfo
const getOrderInfo = (row) => {
  return row?.orderInfo || {};
};

// 重置表单
const resetForm = () => {
  form.revisitRecord = getOrderInfo(props.order)?.revisitRecord || '';
};

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false);
};

// 处理提交
const handleSubmit = async () => {
  try {
    await revisitFormRef.value.validate();

    loading.value = true;
    const orderId = getOrderInfo(props.order)?.id;
    if (!orderId) {
      ElMessage.error('订单ID无效');
      return;
    }

    await revisitOrder(orderId, form.revisitRecord);

    ElMessage.success('回访录入成功');
    emit('success');
    handleClose();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回访失败:', error);
      ElMessage.error('回访失败: ' + (error.response?.data?.message || error.message));
    }
  } finally {
    loading.value = false;
  }
};

// 获取服务类型文本
const getServiceTypeText = (code) => {
  if (!code) return '—';
  const typeCode = code.substring(0, 2);
  const typeMap = {
    'HC': '上门护理',
    'HE': '上门体检'
  };
  return typeMap[typeCode] || '—';
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '—';
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};

// 获取志愿者姓名
const getVolunteerName = (volunteerUserId) => {
  if (!volunteerUserId) return '—';
  return `志愿者${volunteerUserId}`;
};
</script>

<style scoped>
.order-base-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.order-base-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.order-base-info p {
  margin: 6px 0;
  color: #606266;
  font-size: 13px;
}

.revisit-form {
  padding: 0 20px;
}
</style>