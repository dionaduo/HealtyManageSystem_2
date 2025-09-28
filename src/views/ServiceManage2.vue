<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">服务管理</h2>
      </div>
      <el-button type="primary" class="add-btn" icon="Plus" @click="showAddServiceModal = true">
        新增服务
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="服务类型">
          <el-select
              v-model="searchForm.serviceTypeId"
              placeholder="请选择服务类型"
              clearable
              style="width: 180px"
              @change="handleServiceTypeChange"
          >
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="服务内容">
          <el-select
              v-model="searchForm.serviceContentId"
              placeholder="请选择服务内容"
              clearable
              style="width: 180px"
              :disabled="!searchForm.serviceTypeId"
          >
            <el-option
                v-for="item in filteredContentOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
          >
            <el-option label="已上架" :value="1" />
            <el-option label="待上架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
              v-model="searchForm.keyword"
              placeholder="请输入关键词"
              clearable
              style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="resetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
          :data="services.records"
          class="data-table"
          style="width: 100%"
          v-loading="loading"
          :empty-text="emptyText"
          stripe
          @selection-change="handleSelectionChange"
          ref="multipleTableRef"
      >
        <!-- 表格列保持不变，与之前相同 -->
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="80" type="index" :index="(index) => (currentPage - 1) * pageSize + index + 1" />
        <el-table-column prop="serviceType" label="服务类型" min-width="100" show-overflow-tooltip />
        <el-table-column prop="serviceContent" label="服务内容" min-width="120" show-overflow-tooltip />
        <el-table-column prop="promotionalImage" label="宣传图片" min-width="150">
          <template #default="scope">
            <div class="image-container">
              <el-image
                  v-if="scope.row.promotionalImage"
                  :src="getImageUrl(scope.row.promotionalImage)"
                  class="table-image"
                  :preview-src-list="[getImageUrl(scope.row.promotionalImage)]"
                  fit="cover"
                  preview-teleported
                  :zoom-rate="1.2"
                  :max-scale="7"
                  :min-scale="0.2"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
                <template #placeholder>
                  <div class="image-loading">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>加载中...</span>
                  </div>
                </template>
              </el-image>
              <div v-else class="no-image">
                <el-icon><Picture /></el-icon>
                <span>无图片</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="introduction" label="服务简介" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="定价" min-width="100">
          <template #default="scope">
            <span>{{ scope.row.price }}元/次</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已上架' : '待上架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="primary"
                  link
                  icon="Edit"
                  @click="editServiceHandle(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  v-if="scope.row.status === 0"
                  size="small"
                  type="success"
                  link
                  icon="ArrowUp"
                  @click="handleListing(scope.row.id)"
              >
                上架
              </el-button>
              <el-button
                  v-if="scope.row.status === 1"
                  size="small"
                  type="warning"
                  link
                  icon="ArrowDown"
                  @click="handleDelist(scope.row.id)"
              >
                下架
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="deleteService(scope.row.id)"
                  :disabled="scope.row.status === 1"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="services.total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          background
      />
    </div>

    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="multipleSelection.length > 0">
      <el-button
          type="success"
          icon="ArrowUp"
          @click="handleBatchListing"
          :disabled="!hasSelectedOffShelf"
      >
        批量上架 ({{ selectedOffShelfCount }})
      </el-button>
      <el-button
          type="warning"
          icon="ArrowDown"
          @click="handleBatchDelist"
          :disabled="!hasSelectedOnShelf"
      >
        批量下架 ({{ selectedOnShelfCount }})
      </el-button>
    </div>

    <!-- 新增服务弹窗 -->
    <el-dialog
        v-model="showAddServiceModal"
        title="新增服务"
        width="800px"
        @closed="resetNewService"
        :close-on-click-modal="false"
    >
      <ServiceForm
          ref="addFormRef"
          :service-data="newService"
          :loading="addLoading"
          :type-options="typeOptions"
          :content-options="contentOptions"
          @submit="submitAddService"
          @cancel="showAddServiceModal = false"
      />
    </el-dialog>

    <!-- 编辑服务弹窗 -->
    <el-dialog
        v-model="showEditServiceModal"
        title="编辑服务"
        width="800px"
        @closed="resetEditService"
        :close-on-click-modal="false"
    >
      <ServiceForm
          ref="editFormRef"
          :service-data="editServiceData"
          :loading="editLoading"
          :type-options="typeOptions"
          :content-options="contentOptions"
          @submit="submitEditService"
          @cancel="showEditServiceModal = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture, Loading } from '@element-plus/icons-vue';
import ServiceForm from '../components/ServiceForm.vue';
import {
  listService,
  listing,
  delist,
  delService,
  typeService,
  addService,
  editService,
  typeContent // 添加获取服务内容的API
} from "@/api/auth.js";

const router = useRouter();

// 响应式数据
const services = reactive({
  records: [],
  total: 0,
  pages: 0,
  current: 1,
  size: 5
});

const currentPage = ref(1);
const pageSize = ref(5);
const loading = ref(false);
const addLoading = ref(false);
const editLoading = ref(false);
const showAddServiceModal = ref(false);
const showEditServiceModal = ref(false);
const multipleTableRef = ref();
const multipleSelection = ref([]);
const typeOptions = ref([]);
const contentOptions = ref([]); // 所有服务内容选项
const addFormRef = ref();
const editFormRef = ref();

const searchForm = reactive({
  serviceTypeId: null,
  serviceContentId: null, // 添加服务内容搜索
  status: null,
  keyword: ''
});

const newService = reactive({
  serviceTypeId: null,
  serviceContent: '', // 服务内容名称
  serviceContentId: null, // 服务内容ID
  introduction: '',
  details: '',
  price: null,
  volunteerOpenFlag: 0,
  certificateFlag: 0,
  promotionalImage: '',
  promotionalLogo: ''
});

const editServiceData = reactive({
  id: null,
  serviceTypeId: null,
  serviceContent: '',
  serviceContentId: null,
  introduction: '',
  details: '',
  price: null,
  volunteerOpenFlag: 0,
  certificateFlag: 0,
  promotionalImage: '',
  promotionalLogo: '',
  status: 0
});

// 计算属性
const emptyText = computed(() => {
  return searchForm.serviceTypeId || searchForm.status !== null || searchForm.keyword
      ? '暂无符合条件的服务'
      : '暂无服务数据';
});

const selectedOnShelfCount = computed(() => {
  return multipleSelection.value.filter(item => item.status === 1).length;
});

const selectedOffShelfCount = computed(() => {
  return multipleSelection.value.filter(item => item.status === 0).length;
});

const hasSelectedOnShelf = computed(() => selectedOnShelfCount.value > 0);
const hasSelectedOffShelf = computed(() => selectedOffShelfCount.value > 0);

// 根据选中的服务类型过滤服务内容
const filteredContentOptions = computed(() => {
  if (!searchForm.serviceTypeId) {
    return [];
  }
  return contentOptions.value.filter(item => item.serviceTypeId === searchForm.serviceTypeId);
});

// 工具函数
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getImageUrl = (url) => {
  if (!url) return '';

  if (url.startsWith('data:image')) {
    return url;
  }

  if (url.startsWith('/9j/') || url.startsWith('iVBORw') || (url.length > 100 && !url.includes(' '))) {
    let mimeType = 'image/jpeg';
    if (url.startsWith('iVBORw')) {
      mimeType = 'image/png';
    } else if (url.startsWith('R0lGOD') || url.startsWith('/9j/')) {
      mimeType = 'image/jpeg';
    }
    return `data:${mimeType};base64,${url}`;
  }

  return `${url}?t=${new Date().getTime()}`;
};

const goHome = () => {
  router.push('/home');
};

const fetchServices = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    };

    // 清理空参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });

    const response = await listService(params);

    if (response.code === 200 && response.data) {
      const data = response.data;

      // 添加服务类型和内容的映射处理
      const records = (data.records || []).map(record => {
        const serviceTypeIdStr = String(record.serviceTypeId);
        const serviceContentIdStr = String(record.serviceContentId);

        const serviceTypeObj = typeOptions.value.find(option =>
            String(option.value) === serviceTypeIdStr
        );

        const serviceContentObj = contentOptions.value.find(option =>
            String(option.value) === serviceContentIdStr
        );

        return {
          ...record,
          serviceType: serviceTypeObj ? serviceTypeObj.label : `未知类型(ID:${record.serviceTypeId})`,
          serviceContent: serviceContentObj ? serviceContentObj.label : `未知内容(ID:${record.serviceContentId})`,
        };
      });

      Object.assign(services, {
        records: records,
        total: data.total || 0,
        pages: data.pages || 0,
        current: data.current || currentPage.value,
        size: data.size || pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取服务数据失败');
    }
  } catch (error) {
    ElMessage.error('获取服务数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取服务类型
const fetchServiceTypes = async () => {
  try {
    const response = await typeService();
    if (response.code === 200 && response.data) {
      typeOptions.value = response.data.map(item => ({
        label: item.name,
        value: item.id
      }));
    } else {
      ElMessage.error(response.msg || '获取服务类型失败');
    }
  } catch (error) {
    ElMessage.error('获取服务类型失败');
  }
};

// 获取服务内容
const fetchServiceContents = async () => {
  try {
    const response = await typeContent();
    if (response.code === 200 && response.data) {
      // 根据后端ServiceContent结构处理数据
      contentOptions.value = response.data.map(item => ({
        label: item.serviceContentName,
        value: item.serviceContentId,
        serviceTypeId: item.serviceTypeId
      }));
    } else {
      ElMessage.error(response.msg || '获取服务内容失败');
    }
  } catch (error) {
    ElMessage.error('获取服务内容失败');
  }
};

// 服务类型改变时重置服务内容选择
const handleServiceTypeChange = (value) => {
  searchForm.serviceContentId = null;
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchServices();
};

const resetSearch = () => {
  searchForm.serviceTypeId = null;
  searchForm.serviceContentId = null;
  searchForm.status = null;
  searchForm.keyword = '';
  currentPage.value = 1;
  fetchServices();
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 重置表单函数
const resetNewService = () => {
  Object.assign(newService, {
    serviceTypeId: null,
    serviceContent: '',
    serviceContentId: null,
    introduction: '',
    details: '',
    price: null,
    volunteerOpenFlag: 0,
    certificateFlag: 0,
    promotionalImage: '',
    promotionalLogo: ''
  });
  if (addFormRef.value) {
    addFormRef.value.resetForm();
  }
};

const resetEditService = () => {
  Object.assign(editServiceData, {
    id: null,
    serviceTypeId: null,
    serviceContent: '',
    serviceContentId: null,
    introduction: '',
    details: '',
    price: null,
    volunteerOpenFlag: 0,
    certificateFlag: 0,
    promotionalImage: '',
    promotionalLogo: '',
    status: 0
  });
  if (editFormRef.value) {
    editFormRef.value.resetForm();
  }
};

// 服务操作
const submitAddService = async (formData) => {
  try {
    addLoading.value = true;
    const response = await addService(formData);
    if (response.code === 200) {
      await fetchServices();
      showAddServiceModal.value = false;
      ElMessage.success('服务添加成功');
    } else {
      ElMessage.error(response.msg || '添加服务失败');
    }
  } catch (error) {
    const errorMsg = error.response?.data?.msg || '添加服务失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    addLoading.value = false;
  }
};

const editServiceHandle = (service) => {
  Object.assign(editServiceData, {
    id: service.id,
    serviceTypeId: service.serviceTypeId,
    serviceContent: service.serviceContent,
    serviceContentId: service.serviceContentId,
    introduction: service.introduction,
    details: service.details,
    price: service.price,
    volunteerOpenFlag: service.volunteerOpenFlag,
    certificateFlag: service.certificateFlag,
    promotionalImage: service.promotionalImage,
    promotionalLogo: service.promotionalLogo,
    status: service.status
  });
  showEditServiceModal.value = true;
};

const submitEditService = async (formData) => {
  try {
    editLoading.value = true;
    const response = await editService(formData);
    if (response.code === 200) {
      await fetchServices();
      showEditServiceModal.value = false;
      ElMessage.success('服务更新成功');
    } else {
      ElMessage.error(response.msg || '更新服务失败');
    }
  } catch (error) {
    const errorMsg = error.response?.data?.msg || '更新服务失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    editLoading.value = false;
  }
};

// 单个操作
const handleListing = async (id) => {
  try {
    const response = await listing([id]);
    if (response.code === 200) {
      await fetchServices();
      ElMessage.success('上架成功');
    } else {
      ElMessage.error(response.msg || '上架失败');
    }
  } catch (error) {
    ElMessage.error('上架失败，请稍后重试');
  }
};

const handleDelist = async (id) => {
  try {
    const response = await delist([id]);
    if (response.code === 200) {
      await fetchServices();
      ElMessage.success('下架成功');
    } else {
      ElMessage.error(response.msg || '下架失败');
    }
  } catch (error) {
    ElMessage.error('下架失败，请稍后重试');
  }
};

// 批量操作
const handleBatchListing = async () => {
  try {
    const ids = multipleSelection.value
        .filter(item => item.status === 0)
        .map(item => item.id);

    if (ids.length === 0) {
      ElMessage.warning('请选择待上架的服务');
      return;
    }

    const response = await listing(ids);
    if (response.code === 200) {
      await fetchServices();
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
      ElMessage.success('批量上架成功');
    } else {
      ElMessage.error(response.msg || '批量上架失败');
    }
  } catch (error) {
    ElMessage.error('批量上架失败，请稍后重试');
  }
};

const handleBatchDelist = async () => {
  try {
    const ids = multipleSelection.value
        .filter(item => item.status === 1)
        .map(item => item.id);

    if (ids.length === 0) {
      ElMessage.warning('请选择已上架的服务');
      return;
    }

    const response = await delist(ids);
    if (response.code === 200) {
      await fetchServices();
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
      ElMessage.success('批量下架成功');
    } else {
      ElMessage.error(response.msg || '批量下架失败');
    }
  } catch (error) {
    ElMessage.error('批量下架失败，请稍后重试');
  }
};

const deleteService = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条服务吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const response = await delService(id);
    if (response.code === 200) {
      await fetchServices();
      ElMessage.success('服务删除成功');
    } else {
      ElMessage.error(response.msg || '删除服务失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.msg || '删除服务失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 分页处理
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchServices();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchServices();
};

// 初始化
onMounted(async () => {
  try {
    await Promise.all([fetchServiceTypes(), fetchServiceContents()]);
    await fetchServices();
  } catch (error) {
    ElMessage.error('初始化失败');
  }
});
</script>

<style scoped>
@import url('@/assets/StyleCss.css');

.batch-actions {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.time-text {
  font-size: 12px;
  color: #666;
}
</style>