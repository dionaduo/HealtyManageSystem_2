<template>
  <el-form
      :model="serviceData"
      :rules="rules"
      label-width="120px"
      ref="formRef"
      class="dialog-form"
  >
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="服务类型" prop="serviceTypeId">
          <el-select
              v-model="serviceData.serviceTypeId"
              placeholder="请选择服务类型"
              clearable
              style="width: 100%"
              @change="handleServiceTypeChange"
          >
            <el-option
                v-for="item in allServiceTypes"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="服务内容" prop="serviceContent">
          <el-input
              v-model="serviceData.serviceContent"
              placeholder="请输入服务内容"
              clearable
              style="width: 100%"
              maxlength="20"
              show-word-limit
          />
          <div class="tips">不超过20个字符</div>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="志愿者开放" prop="volunteerOpenFlag">
          <el-select
              v-model="serviceData.volunteerOpenFlag"
              placeholder="请选择"
              style="width: 100%"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="持证接单" prop="certificateFlag">
          <el-select
              v-model="serviceData.certificateFlag"
              placeholder="请选择"
              style="width: 100%"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="宣传图片" prop="promotionalImage">
          <el-upload
              class="uploader-big"
              accept="image/jpg,image/jpeg,image/png"
              action="#"
              :on-change="(file) => handleChange(file, 'promotionalImage')"
              :show-file-list="false"
              :auto-upload="false"
          >
            <div v-if="serviceData.promotionalImage" class="service-image-preview">
              <el-image
                  :src="getImageUrl(serviceData.promotionalImage)"
                  class="avatar"
                  style="width: 100%; height: 120px;"
                  fit="cover"
              />
              <div class="service-preview-actions">
                <el-button
                    size="small"
                    type="danger"
                    icon="Delete"
                    @click.stop="serviceData.promotionalImage = ''"
                >
                  移除
                </el-button>
              </div>
            </div>
            <div v-else class="default-upload-big">
              <el-icon><Plus /></el-icon>
              <span>上传宣传图片</span>
            </div>
          </el-upload>
          <div class="tips">建议尺寸：800×400px，大小不超过10MB</div>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="缩略图片" prop="promotionalLogo">
          <el-upload
              accept="image/jpg,image/jpeg,image/png"
              action="#"
              :on-change="(file) => handleChange(file, 'promotionalLogo')"
              :show-file-list="false"
              :auto-upload="false"
          >
            <div v-if="serviceData.promotionalLogo" class="service-image-preview">
              <el-image
                  :src="getImageUrl(serviceData.promotionalLogo)"
                  class="avatar"
                  style="width: 120px; height: 120px;"
                  fit="cover"
              />
              <div class="service-preview-actions">
                <el-button
                    size="small"
                    type="danger"
                    icon="Delete"
                    @click.stop="serviceData.promotionalLogo = ''"
                >
                  移除
                </el-button>
              </div>
            </div>
            <div v-else class="default-upload-small">
              <el-icon><Plus /></el-icon>
              <span>上传缩略图</span>
            </div>
          </el-upload>
          <div class="tips">建议尺寸：200×200px，大小不超过10MB</div>
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="服务简介" prop="introduction">
      <el-input
          v-model="serviceData.introduction"
          placeholder="请输入服务简介"
          maxlength="50"
          show-word-limit
          type="textarea"
          :rows="3"
      />
      <div class="tips">不超过50个字符</div>
    </el-form-item>

    <el-form-item label="定价" prop="price">
      <el-input-number
          v-model="serviceData.price"
          placeholder="请输入定价"
          :controls="false"
          style="width: 200px"
          :min="1"
          :max="99999"
          :precision="0"
      />
      <span style="margin-left: 10px;">元/次</span>
    </el-form-item>

    <el-form-item label="服务详情" prop="details">
      <el-input
          v-model="serviceData.details"
          placeholder="请输入服务详情"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
      />
      <div class="tips">详细描述服务内容、流程等信息</div>
    </el-form-item>

    <el-form-item label="状态" prop="status" v-if="serviceData.id">
      <el-select
          v-model="serviceData.status"
          placeholder="请选择状态"
          style="width: 200px"
      >
        <el-option label="已上架" :value="1" />
        <el-option label="待上架" :value="0" />
      </el-select>
    </el-form-item>

    <el-form-item>
      <div style="text-align: right; width: 100%;">
        <el-button @click="$emit('cancel')">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading">
          保存
        </el-button>
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import {ElMessage} from 'element-plus';
import {Plus} from '@element-plus/icons-vue';
import { typeService } from "@/api/auth.js";

const props = defineProps({
  serviceData: {
    type: Object,
    default: () => ({})
  },
  loading: Boolean
});

const emit = defineEmits(['submit', 'cancel']);
const formRef = ref();
const allServiceTypes = ref([]);

const formData = reactive({
  serviceTypeId: '',
  serviceContent: '',
  introduction:'',
  details:'',
  price: '',
  volunteerOpenFlag: '',
  certificateFlag:'',
  promotionalImage: '',
  promotionalLogo: ''
});

// 表单验证规则
const rules = reactive({
  serviceTypeId: [
    {required: true, message: '请选择服务类型', trigger: 'change'}
  ],
  serviceContent: [
    {required: true, message: '请输入服务内容', trigger: 'blur'},
    {max: 20, message: '服务内容长度不能超过20个字符', trigger: 'blur'}
  ],
  volunteerOpenFlag: [
    {required: true, message: '请选择是否对志愿者开放', trigger: 'change'}
  ],
  certificateFlag: [
    {required: true, message: '请选择是否需要持证接单', trigger: 'change'}
  ],
  promotionalImage: [
    {required: true, message: '请上传宣传图片', trigger: 'change'}
  ],
  promotionalLogo: [
    {required: true, message: '请上传缩略图片', trigger: 'change'}
  ],
  introduction: [
    {required: true, message: '请输入服务简介', trigger: 'blur'},
    {max: 50, message: '服务简介长度不能超过50个字符', trigger: 'blur'}
  ],
  price: [
    {required: true, message: '请输入定价', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入定价'));
        } else if (value < 1 || value > 99999) {
          callback(new Error('定价必须在1-99999之间'));
        } else if (!Number.isInteger(value)) {
          callback(new Error('定价必须为整数'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  details: [
    {required: true, message: '请输入服务详情', trigger: 'blur'},
    {max: 500, message: '服务详情长度不能超过500个字符', trigger: 'blur'}
  ],
  status: [
    {required: true, message: '请选择状态', trigger: 'change'}
  ]
});

// 获取所有服务类型
const fetchAllServiceTypes = async () => {
  try {
    const response = await typeService();
    if (response.code === 200 && response.data) {
      allServiceTypes.value = response.data;
    }
  } catch (error) {
    console.error('获取服务类型失败:', error);
  }
};

// 图片处理
const getImageUrl = (url) => {
  if (!url) return '';

  if (url.startsWith('data:image')) {
    return url;
  }

  if (url.startsWith('/9j/') || url.startsWith('iVBORw') || (url.length > 100 && !url.includes(' '))) {
    let mimeType = 'image/jpeg';
    if (url.startsWith('iVBORw')) {
      mimeType = 'image/png';
    }
    return `data:${mimeType};base64,${url}`;
  }

  return url;
};

const handleChange = (file, field) => {
  const rawFile = file.raw || file;

  // 检查文件类型
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件');
    return false;
  }

  // 检查文件大小 (限制为10MB)
  if (rawFile.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB');
    return false;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    const base64Data = e.target.result;
    // 只存储base64数据部分，不包含data:image前缀
    props.serviceData[field] = base64Data.split(',')[1];

    // 清除该字段的验证错误
    formRef.value.clearValidate(field);
  };
  reader.readAsDataURL(rawFile);
};

const handleServiceTypeChange = (value) => {
  formRef.value.clearValidate('serviceTypeId');
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;


  try {
    const valid = await formRef.value.validate();
    if (valid) {
      // 准备提交数据 - 确保服务内容格式正确
      const submitData = {
        ...formData,
        // 确保服务内容数组格式正确
        serviceContents: formData.serviceContents
            .filter(sc => sc.serviceContentId && sc.serviceTypeId)
            .map(sc => ({
              serviceTypeId: sc.serviceTypeId,
              serviceContentId: sc.serviceContentId
            }))
      };

      emit('submit', submitData);
    }
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

// 初始化
onMounted(() => {
  fetchAllServiceTypes();
});

// 暴露方法给父组件
defineExpose({
  resetForm
});
</script>

<style scoped>
.tips {
  font-size: 12px;
  color: #999;
  line-height: 1.5;
  margin-top: 4px;
}

.uploader-big :deep(.el-upload) {
  width: 100%;
}

.default-upload-big, .default-upload-small {
  border-radius: 8px;
  background: #f5f7fa;
  box-sizing: border-box;
  border: 1px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #606266;
  cursor: pointer;
  transition: border-color 0.3s;
}

.default-upload-big:hover,
.default-upload-small:hover {
  border-color: #409eff;
}

.default-upload-big {
  width: 100%;
  height: 120px;
}

.default-upload-small {
  width: 120px;
  height: 120px;
}

.service-image-preview {
  position: relative;
  display: inline-block;
}

.service-preview-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  padding: 4px;
}
</style>