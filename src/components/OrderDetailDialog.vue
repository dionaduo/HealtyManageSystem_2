<template>
  <el-dialog
      :model-value="modelValue"
      title="订单详情"
      width="80%"
      top="5vh"
      @close="handleClose"
  >
    <div v-loading="loading" class="order-detail">
      <!-- 订单信息 -->
      <el-card class="detail-section">
        <template #header>
          <div class="section-header">
            <span class="section-title">订单信息</span>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ getOrderInfo(detail)?.code || '—' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(getOrderInfo(detail)?.orderTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getOrderInfo(detail)?.paymentStatus === 1 ? 'success' : 'danger'">
              {{ getOrderInfo(detail)?.paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ getOrderStatusText(getOrderInfo(detail)?.status) }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ getOrderInfo(detail)?.contactName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ getOrderInfo(detail)?.contactMobile || '—' }}</el-descriptions-item>
          <el-descriptions-item label="特殊提示" :span="2">{{ getOrderInfo(detail)?.note || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 服务信息 -->
      <el-card class="detail-section">
        <template #header>
          <div class="section-header">
            <span class="section-title">服务信息</span>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="服务老人">{{ getOrderInfo(detail)?.serviceObjectName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ detail.orderDetails?.serviceType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="服务子类">{{ detail.orderDetails?.serviceContent || '—' }}</el-descriptions-item>
          <el-descriptions-item label="服务时间">
            {{ formatTime(getOrderInfo(detail)?.serviceStartTime) }} - {{ formatTime(getOrderInfo(detail)?.serviceEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务评价">
            {{ detail.evaluate ? getEvaluateText(detail.evaluate.level) : '暂无服务评价' }}
          </el-descriptions-item>
          <el-descriptions-item label="签到时间">{{ formatTime(getOrderInfo(detail)?.signInTime) }}</el-descriptions-item>
          <el-descriptions-item label="签退时间">{{ formatTime(getOrderInfo(detail)?.signOutTime) }}</el-descriptions-item>
          <el-descriptions-item label="服务照片" :span="2">
            <div v-if="detail.serviceImages && detail.serviceImages.length > 0" class="image-list">
              <el-image
                  v-for="(image, index) in detail.serviceImages"
                  :key="index"
                  :src="image.image"
                  :preview-src-list="detail.serviceImages.map(img => img.image)"
                  class="service-image"
              />
            </div>
            <span v-else>暂无服务照片</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 检查数据（仅上门体检显示） -->
      <el-card v-if="detail.orderDetails?.serviceType === 'HE'" class="detail-section">
        <template #header>
          <div class="section-header">
            <span class="section-title">检查数据</span>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="血压">{{ detail.serviceData?.bloodPressure || '—' }}</el-descriptions-item>
          <el-descriptions-item label="血糖">{{ detail.serviceData?.bloodGlucose || '—' }}</el-descriptions-item>
          <el-descriptions-item label="血脂">{{ detail.serviceData?.bloodFat || '—' }}</el-descriptions-item>
          <el-descriptions-item label="心率">{{ detail.serviceData?.heartRate || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 回访记录 -->
      <el-card v-if="getOrderInfo(detail)?.revisitStatus === 1" class="detail-section">
        <template #header>
          <div class="section-header">
            <span class="section-title">回访记录</span>
          </div>
        </template>

        <div class="revisit-content">
          {{ getOrderInfo(detail)?.revisitRecord || '—' }}
        </div>
      </el-card>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getOrderDetail } from '@/api/auth.js';
import dayjs from 'dayjs';

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  orderId: {
    type: String,
    default: null
  }
});

// Emits
const emit = defineEmits(['update:modelValue']);

// 响应式数据
const loading = ref(false);
const detail = reactive({});

// Watchers
watch(() => props.modelValue, (val) => {
  if (val && props.orderId) {
    loadDetail();
  }
}, { immediate: true });

// 方法
// 安全获取orderInfo
const getOrderInfo = (row) => {
  return row?.orderInfo || {};
};

// 加载订单详情
const loadDetail = async () => {
  loading.value = true;
  try {
    console.log('正在获取订单详情，订单ID:', props.orderId);
    const id = props.orderId;
    // 验证订单ID
    if (!props.orderId) {
      ElMessage.error('订单ID不能为空');
      loading.value = false;
      return;
    }

    const response = await getOrderDetail(id);
    console.log('订单详情响应:', response);

    if (response && response.code === 200) {
      Object.assign(detail, response.data || {});
      console.log('详情数据:', detail);
    } else {
      console.error('获取订单详情失败，响应码:', response?.code, '消息:', response?.msg);
      ElMessage.error(response?.msg || '获取订单详情失败');
      Object.assign(detail, {});
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败: ' + (error.message || '未知错误'));
    Object.assign(detail, {});
  } finally {
    loading.value = false;
  }
};

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false);
  Object.assign(detail, {});
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '—';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待接单',
    3: '待完成',
    4: '已完成',
    5: '已评价',
    6: '已关闭',
    7: '已取消'
  };
  return statusMap[status] || '—';
};

// 获取评价文本
const getEvaluateText = (level) => {
  const levels = {
    1: '很差',
    2: '较差',
    3: '一般',
    4: '较好',
    5: '很好'
  };
  return levels[level] || '—';
};
</script>

<style scoped>
.order-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.image-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.service-image {
  width: 100px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.revisit-content {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.6;
}
</style>