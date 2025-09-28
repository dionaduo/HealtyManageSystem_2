<template>
  <div class="volunteer-form">
    <el-form
        :model="formData"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        label-position="right"
        class="demo-form"
        status-icon
    >
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        <div class="form-row">
          <el-form-item label="姓名" prop="name">
            <el-input
                v-model="formData.name"
                placeholder="请输入姓名"
                clearable
                class="form-input"
            />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="formData.sex">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="0">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="民族" prop="nation">
            <el-input
                v-model="formData.nation"
                placeholder="请输入民族"
                clearable
                class="form-input"
            />
          </el-form-item>
          <el-form-item label="年龄" prop="age">
            <el-input-number
                v-model="formData.age"
                :min="18"
                :max="80"
                controls-position="right"
                class="form-input"
            />
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="身份证号" prop="idCard">
            <el-input
                v-model="formData.idCard"
                placeholder="请输入身份证号"
                clearable
                maxlength="18"
                show-word-limit
                class="form-input"
                @blur="validateIdCard"
            />
          </el-form-item>
          <el-form-item label="手机号" prop="mobile">
            <el-input
                v-model="formData.mobile"
                placeholder="请输入手机号"
                clearable
                maxlength="11"
                class="form-input"
                @blur="validateMobile"
            />
          </el-form-item>
        </div>

        <el-form-item label="联系地址" prop="address">
          <el-input
              v-model="formData.address"
              type="textarea"
              :rows="2"
              placeholder="请输入联系地址"
              clearable
              class="form-textarea"
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <h3 class="section-title">服务信息</h3>
        <div class="form-row">
          <el-form-item label="服务类型" prop="serviceTypeIds">
            <el-select
                v-model="formData.serviceTypeIds"
                placeholder="请选择服务类型"
                multiple
                clearable
                style="width: 100%"
                @change="handleServiceTypesChange"
            >
              <el-option
                  v-for="item in serviceTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 为每个选择的服务类型显示对应的服务内容选择 -->
        <div v-for="(serviceContent, index) in formData.serviceContents" :key="index" class="service-content-group">
          <div class="form-row">
            <el-form-item
              :label="getServiceTypeName(serviceContent.serviceTypeId)"
              :prop="`serviceContents.${index}.serviceContentId`"
              :rules="{
                required: true,
                message: '请选择服务内容',
                trigger: 'change'
              }"
            >
              <el-select
                  v-model="serviceContent.serviceContentId"
                  placeholder="请选择服务内容"
                  clearable
                  filterable
                  style="width: 100%"
              >
                <el-option
                    v-for="content in getFilteredServiceContents(serviceContent.serviceTypeId)"
                    :key="content.value"
                    :label="content.label"
                    :value="content.value"
                />
                <template #empty>
                  <div v-if="serviceContentLoading[serviceContent.serviceTypeId]">加载中...</div>
                  <div v-else-if="getFilteredServiceContents(serviceContent.serviceTypeId).length === 0 && serviceContent.serviceTypeId">
                    暂无服务内容
                  </div>
                  <div v-else>请输入关键词搜索</div>
                </template>
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="form-row">
          <el-form-item label="服务开始时间" prop="serviceStartHour">
            <el-time-select
                v-model="formData.serviceStartHour"
                placeholder="请选择开始时间"
                start="06:00"
                step="00:30"
                end="22:00"
                class="form-select"
            />
          </el-form-item>
          <el-form-item label="服务结束时间" prop="serviceEndHour">
            <el-time-select
                v-model="formData.serviceEndHour"
                placeholder="请选择结束时间"
                start="06:00"
                step="00:30"
                end="22:00"
                :min-time="formData.serviceStartHour"
                class="form-select"
            />
          </el-form-item>
        </div>

        <div class="form-row">
          <el-form-item label="持证上岗" prop="certificateFlag">
            <el-radio-group v-model="formData.certificateFlag">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="周末接单" prop="weekendsTakeOrders">
            <el-radio-group v-model="formData.weekendsTakeOrders">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">证件信息</h3>
        <div class="upload-row">
          <el-form-item label="身份证正面" prop="idCardFrontImage">
            <div class="image-upload-container">
              <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="handleBeforeUpload"
                  :on-change="handleIdCardFrontUpload"
                  :auto-upload="false"
              >
                <img v-if="formData.idCardFrontImage" :src="formData.idCardFrontImage" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">上传身份证正面照片</div>
            </div>
          </el-form-item>

          <el-form-item label="身份证反面" prop="idCardBackImage">
            <div class="image-upload-container">
              <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="handleBeforeUpload"
                  :on-change="handleIdCardBackUpload"
                  :auto-upload="false"
              >
                <img v-if="formData.idCardBackImage" :src="formData.idCardBackImage" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">上传身份证反面照片</div>
            </div>
          </el-form-item>
        </div>

        <div class="upload-row">
          <el-form-item label="手持身份证" prop="idCardHolderImage">
            <div class="image-upload-container">
              <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="handleBeforeUpload"
                  :on-change="handleIdCardHolderUpload"
                  :auto-upload="false"
              >
                <img v-if="formData.idCardHolderImage" :src="formData.idCardHolderImage" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">上传手持身份证照片</div>
            </div>
          </el-form-item>

          <el-form-item
              label="资质证书"
              prop="certificateImage"
              v-if="formData.certificateFlag === 1"
          >
            <div class="image-upload-container">
              <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="handleBeforeUpload"
                  :on-change="handleCertificateUpload"
                  :auto-upload="false"
              >
                <img v-if="formData.certificateImage" :src="formData.certificateImage" class="avatar">
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">上传相关资质证书照片</div>
            </div>
          </el-form-item>
        </div>
      </div>

      <div class="form-actions">
        <el-button @click="handleCancel" class="btn-hover-animate">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading" class="btn-hover-animate">
          提交
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { typeService, typeContent } from "@/api/auth.js";

const props = defineProps({
  volunteerData: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  },
  serviceTypeOptions: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['submit', 'cancel']);

// 表单引用
const formRef = ref();

// 响应式数据
const formData = reactive({
  id: '',
  userId: '',
  name: '',
  sex: 1,
  nation: '',
  age: null,
  idCard: '',
  mobile: '',
  address: '',
  serviceStartHour: '',
  serviceEndHour: '',
  certificateFlag: 0,
  weekendsTakeOrders: 0,
  idCardFrontImage: '',
  idCardBackImage: '',
  idCardHolderImage: '',
  certificateImage: '',
  serviceTypeIds: [],
  serviceContents: [],
  auditStatus: 0
});

const serviceContentOptions = ref([]);
const serviceContentLoading = ref({}); // 用于跟踪每个服务类型的内容加载状态

// 表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  nation: [
    { required: true, message: '请输入民族', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 18, max: 80, message: '年龄必须在18到80岁之间', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入联系地址', trigger: 'blur' }
  ],
  serviceTypeIds: [
    { required: true, message: '请选择服务类型', trigger: 'change' }
  ],
  serviceStartHour: [
    { required: true, message: '请选择服务开始时间', trigger: 'change' }
  ],
  serviceEndHour: [
    { required: true, message: '请选择服务结束时间', trigger: 'change' }
  ],
  certificateFlag: [
    { required: true, message: '请选择是否持证上岗', trigger: 'change' }
  ],
  weekendsTakeOrders: [
    { required: true, message: '请选择是否周末接单', trigger: 'change' }
  ],
  idCardFrontImage: [
    { required: true, message: '请上传身份证正面照片', trigger: 'change' }
  ],
  idCardBackImage: [
    { required: true, message: '请上传身份证反面照片', trigger: 'change' }
  ],
  idCardHolderImage: [
    { required: true, message: '请上传手持身份证照片', trigger: 'change' }
  ]
});

// 获取所有服务内容
const fetchAllServiceContents = async () => {
  try {
    const response = await typeContent();
    if (response.code === 200 && response.data) {
      serviceContentOptions.value = response.data.map(item => ({
        label: item.serviceContentName,
        value: item.serviceContentId,
        serviceTypeId: item.serviceTypeId
      }));
    }
  } catch (error) {
    console.error('获取服务内容失败:', error);
    ElMessage.error('获取服务内容失败');
  }
};

// 根据服务类型ID获取对应的服务内容
const getFilteredServiceContents = (serviceTypeId) => {
  if (!serviceTypeId) return [];
  return serviceContentOptions.value.filter(
      content => content.serviceTypeId == serviceTypeId
  );
};

// 获取服务类型名称
const getServiceTypeName = (serviceTypeId) => {
  const type = props.serviceTypeOptions.find(t => t.value === serviceTypeId);
  return type ? type.label : '未知类型';
};

// 获取特定服务类型的服务内容
const fetchServiceContentsByType = async (typeId) => {
  if (!typeId) return;

  // 设置加载状态
  serviceContentLoading.value[typeId] = true;

  try {
    // 先检查是否已经有该类型的服务内容
    const existingContents = getFilteredServiceContents(typeId);
    if (existingContents.length > 0) {
      // 如果已有数据，直接使用
      serviceContentLoading.value[typeId] = false;
      return;
    }

    // 如果没有数据，尝试从后端获取
    const response = await typeContent({ serviceTypeId: typeId });
    if (response.code === 200 && response.data && response.data.length > 0) {
      // 更新服务内容选项
      const newContents = response.data.map(item => ({
        label: item.serviceContentName,
        value: item.serviceContentId,
        serviceTypeId: item.serviceTypeId || typeId
      }));

      // 合并到现有选项中，避免重复
      serviceContentOptions.value = [...serviceContentOptions.value, ...newContents];
    } else {
      // 如果特定类型获取不到，尝试从所有数据中过滤
      if (serviceContentOptions.value.length === 0) {
        await fetchAllServiceContents();
      }
    }
  } catch (error) {
    console.error('获取服务内容失败:', error);
    // 失败时尝试获取所有服务内容
    if (serviceContentOptions.value.length === 0) {
      await fetchAllServiceContents();
    }
  } finally {
    serviceContentLoading.value[typeId] = false;
  }
};

// 服务类型变化处理
const handleServiceTypesChange = async (selectedTypeIds) => {
  // 清空现有的服务内容
  formData.serviceContents = [];

  // 如果服务内容选项为空，则尝试获取所有服务内容
  if(serviceContentOptions.value.length === 0){
    await fetchAllServiceContents();
  }

  // 为每个选中的服务类型创建一个服务内容对象
  for (const typeId of selectedTypeIds) {
    formData.serviceContents.push({
      serviceTypeId: typeId,
      serviceContentId: ''
    });

    // 获取该服务类型的服务内容
    await fetchServiceContentsByType(typeId);
  }
};

// 身份证号验证
const validateIdCard = () => {
  if (!formData.idCard) return;

  const idCardPattern = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
  if (!idCardPattern.test(formData.idCard)) {
    ElMessage.error('身份证号格式不正确');
  }
};

// 手机号验证
const validateMobile = () => {
  if (!formData.mobile) return;

  const mobilePattern = /^1[3-9]\d{9}$/;
  if (!mobilePattern.test(formData.mobile)) {
    ElMessage.error('手机号格式不正确');
  }
};

// 图片上传处理
const handleBeforeUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt10M = file.size / 1024 / 1024 < 10;

  if (!isJPGOrPNG) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!');
    return false;
  }
  if (!isLt10M) {
    ElMessage.error('上传图片大小不能超过 10MB!');
    return false;
  }
  return true;
};

// 处理图片转Base64
const convertToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
};

const handleIdCardFrontUpload = async (uploadFile) => {
  try {
    formData.idCardFrontImage = await convertToBase64(uploadFile.raw);
  } catch (error) {
    ElMessage.error('图片转换失败');
  }
};

const handleIdCardBackUpload = async (uploadFile) => {
  try {
    formData.idCardBackImage = await convertToBase64(uploadFile.raw);
  } catch (error) {
    ElMessage.error('图片转换失败');
  }
};

const handleIdCardHolderUpload = async (uploadFile) => {
  try {
    formData.idCardHolderImage = await convertToBase64(uploadFile.raw);
  } catch (error) {
    ElMessage.error('图片转换失败');
  }
};

const handleCertificateUpload = async (uploadFile) => {
  try {
    formData.certificateImage = await convertToBase64(uploadFile.raw);
  } catch (error) {
    ElMessage.error('图片转换失败');
  }
};

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    const valid = await formRef.value.validate();
    if (valid) {
      // 验证服务时间
      if (formData.serviceStartHour && formData.serviceEndHour) {
        const startTime = new Date(`2000-01-01T${formData.serviceStartHour}:00`);
        const endTime = new Date(`2000-01-01T${formData.serviceEndHour}:00`);

        if (endTime <= startTime) {
          ElMessage.error('服务结束时间必须晚于开始时间');
          return;
        }
      }

      // 准备提交数据
      const submitData = { ...formData };

      // 移除空的服务内容项
      submitData.serviceContents = formData.serviceContents.filter(sc => sc.serviceContentId);

      emit('submit', submitData);
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};

// 暴露方法
defineExpose({
  clearValidate: () => {
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  }
});

// 监听props变化
watch(() => props.volunteerData, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    Object.assign(formData, newVal);

    // 处理编辑时的服务类型和服务内容数据
    if (newVal.serviceContents && Array.isArray(newVal.serviceContents)) {
      formData.serviceTypeIds = newVal.serviceContents.map(sc => sc.serviceTypeId);
      formData.serviceContents = newVal.serviceContents;

      // 获取服务内容数据
      newVal.serviceContents.forEach(async sc => {
        if (sc.serviceTypeId) {
          await fetchServiceContentsByType(sc.serviceTypeId);
        }
      });
    }
  }
}, { immediate: true, deep: true });

// 监听持证上岗状态变化
watch(() => formData.certificateFlag, (newVal) => {
  if (newVal === 0) {
    formData.certificateImage = '';
  }
});

// 生命周期
onMounted(() => {
  // 获取所有服务内容
  fetchAllServiceContents().catch(error => {
    console.error('初始化服务内容失败:', error);
    ElMessage.error('初始化服务内容失败');
  });
});
</script>

<style scoped>
@import "@/assets/StyleCss.css";

.volunteer-form {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section-title {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-row .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.form-input, .form-select {
  width: 100%;
}

.form-textarea {
  width: 100%;
}

.upload-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.upload-row .el-form-item {
  flex: 1;
}

.image-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-actions {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e6e8eb;
}

.service-content-container {
  position: relative;
  width: 100%;
  display: flex;
}

.service-content-container .el-input,
.service-content-container .el-select {
  flex: 1;
}

.service-content-container {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
}

.service-content-group {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e6e8eb;
}

.service-content-container .el-input,
.service-content-container .el-select {
  flex: 1;
}
</style>