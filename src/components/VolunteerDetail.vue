<template>
  <div class="volunteer-detail">
    <div class="detail-section">
      <h3 class="section-title">基本信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ volunteerData.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ volunteerData.sex === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ volunteerData.nation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ volunteerData.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ volunteerData.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ volunteerData.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="2">{{ volunteerData.address || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="detail-section">
      <h3 class="section-title">服务信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="服务类型">
          <el-tag v-for="(typeName, index) in getServiceTypeNames()" :key="index" style="margin-right: 5px;">
            {{ typeName }}
          </el-tag>
          <span v-if="!volunteerData.serviceTypeName || getServiceTypeNames().length === 0">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="服务内容">
          <el-tag v-for="(contentName, index) in getServiceContentNames()" :key="index" type="success" style="margin-right: 5px;">
            {{ contentName }}
          </el-tag>
          <span v-if="!volunteerData.serviceContentName || getServiceContentNames().length === 0">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="服务开始时间">{{ volunteerData.serviceStartHour || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务结束时间">{{ volunteerData.serviceEndHour || '-' }}</el-descriptions-item>
        <el-descriptions-item label="持证上岗">{{ volunteerData.certificateFlag === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="周末接单">{{ volunteerData.weekendsTakeOrders === 1 ? '是' : '否' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="detail-section">
      <h3 class="section-title">证件信息</h3>
      <div class="image-grid">
        <div class="image-item">
          <div class="image-label">身份证正面</div>
          <el-image
              v-if="volunteerData.idCardFrontImage"
              :src="volunteerData.idCardFrontImage"
              :preview-src-list="[volunteerData.idCardFrontImage]"
              fit="cover"
              class="detail-image"
          />
          <div v-else class="image-placeholder">暂无图片</div>
        </div>

        <div class="image-item">
          <div class="image-label">身份证反面</div>
          <el-image
              v-if="volunteerData.idCardBackImage"
              :src="volunteerData.idCardBackImage"
              :preview-src-list="[volunteerData.idCardBackImage]"
              fit="cover"
              class="detail-image"
          />
          <div v-else class="image-placeholder">暂无图片</div>
        </div>

        <div class="image-item">
          <div class="image-label">手持身份证</div>
          <el-image
              v-if="volunteerData.idCardHolderImage"
              :src="volunteerData.idCardHolderImage"
              :preview-src-list="[volunteerData.idCardHolderImage]"
              fit="cover"
              class="detail-image"
          />
          <div v-else class="image-placeholder">暂无图片</div>
        </div>

        <div class="image-item" v-if="volunteerData.certificateFlag === 1">
          <div class="image-label">资质证书</div>
          <el-image
              v-if="volunteerData.certificateImage"
              :src="volunteerData.certificateImage"
              :preview-src-list="[volunteerData.certificateImage]"
              fit="cover"
              class="detail-image"
          />
          <div v-else class="image-placeholder">暂无图片</div>
        </div>
      </div>
    </div>

    <div class="detail-section">
      <h3 class="section-title">审核信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="审核状态">
          <el-tag
              :type="volunteerData.auditStatus === 1 ? 'success' : volunteerData.auditStatus === 2 ? 'danger' : 'warning'"
          >
            {{ getAuditStatusText(volunteerData.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(volunteerData.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="detail-actions">
      <el-button @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  volunteerData: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['close']);

// 工具函数
const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const getAuditStatusText = (status) => {
  switch (status) {
    case 0: return '未审核';
    case 1: return '已通过';
    case 2: return '未通过';
    default: return '未知';
  }
};

// 获取服务类型名称列表
const getServiceTypeNames = () => {
  if (!props.volunteerData.serviceTypeName) {
    return [];
  }

  // 如果包含逗号，说明是多个服务类型
  if (props.volunteerData.serviceTypeName.includes(',')) {
    return props.volunteerData.serviceTypeName.split(',');
  }

  // 单个服务类型
  return [props.volunteerData.serviceTypeName];
};

// 获取服务内容名称列表
const getServiceContentNames = () => {
  if (!props.volunteerData.serviceContentName) {
    return [];
  }

  // 如果包含逗号，说明是多个服务内容
  if (props.volunteerData.serviceContentName.includes(',')) {
    return props.volunteerData.serviceContentName.split(',');
  }

  // 单个服务内容
  return [props.volunteerData.serviceContentName];
};
</script>

<style scoped>
.volunteer-detail {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.image-item {
  text-align: center;
}

.image-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
}

.detail-image {
  width: 100%;
  height: 200px;
  border-radius: 4px;
  border: 1px solid #e6e8eb;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #909399;
  background: #f5f7fa;
}

.detail-actions {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e6e8eb;
}
</style>
