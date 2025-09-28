<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">广告管理</h2>
      </div>
      <el-button type="primary" class="add-btn" icon="Plus" @click="showAddAdvertisementModal = true">
        新增广告
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="广告标题">
          <el-input
              v-model="searchForm.title"
              placeholder="请输入广告标题"
              clearable
              suffix-icon="Search"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
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
          :data="advertisements.records"
          class="data-table"
          style="width: 100%"
          v-loading="loading"
          :empty-text="emptyText"
          stripe
          @selection-change="handleSelectionChange"
          ref="multipleTableRef"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="80" type="index" :index="(index) => (currentPage - 1) * pageSize + index + 1" />
        <el-table-column prop="title" label="广告标题" min-width="120" show-overflow-tooltip />
        <el-table-column prop="launchPage" label="投放页面" min-width="100">
          <template #default="scope">
            <el-tag type="info" size="small">
              {{ getLaunchPageText(scope.row.launchPage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="picture" label="广告图片" min-width="150">
          <template #default="scope">
            <div class="image-container">
              <el-image
                  v-if="scope.row.picture"
                  :src="getImageUrl(scope.row.picture)"
                  class="table-image"
                  :preview-src-list="[getImageUrl(scope.row.picture)]"
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
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="light" round>
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="primary"
                  link
                  icon="Edit"
                  @click="editAdvertisement(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="deleteAdvertisement(scope.row.id)"
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
          :total="advertisements.total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          background
      />
    </div>

    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="multipleSelection.length > 0">
      <el-popconfirm
          title="确定要批量删除选中的广告吗？此操作不可恢复。"
          @confirm="batchDeleteAdvertisements"
          confirm-button-text="确定"
          cancel-button-text="取消"
      >
        <template #reference>
          <el-button type="danger" icon="Delete">
            批量删除 ({{ multipleSelection.length }})
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 新增广告弹窗 -->
    <el-dialog
        v-model="showAddAdvertisementModal"
        title="新增广告"
        width="600px"
        @closed="resetNewAdvertisement"
        :close-on-click-modal="false"
    >
      <el-form
          :model="newAdvertisement"
          label-width="100px"
          :rules="rules"
          ref="addFormRef"
          class="dialog-form"
      >
        <el-form-item label="广告标题" prop="title">
          <el-input
              v-model="newAdvertisement.title"
              placeholder="请输入广告标题"
              clearable
          />
        </el-form-item>
        <el-form-item label="投放页面" prop="launchPage">
          <el-select
              v-model="newAdvertisement.launchPage"
              placeholder="请选择投放页面"
              style="width: 100%"
          >
            <el-option :value="1" label="banner" />
          </el-select>
        </el-form-item>
        <el-form-item label="广告图片" prop="picture">
          <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="(file) => handleFileUpload(file, 'new')"
              accept="image/*"
              class="image-uploader"
          >
            <el-button type="primary" icon="Upload">
              选择图片
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG 格式，大小不超过10MB
              </div>
            </template>
          </el-upload>
          <div v-if="newAdvertisement.imagePreview" class="image-preview">
            <el-image
                :src="newAdvertisement.imagePreview"
                class="preview-image"
                fit="cover"
            />
            <div class="preview-actions">
              <el-button
                  size="small"
                  type="danger"
                  icon="Delete"
                  @click="newAdvertisement.imagePreview = ''; newAdvertisement.imageFile = null;"
              >
                移除
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
              v-model="newAdvertisement.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddAdvertisementModal = false;">取消</el-button>
          <el-button type="primary" @click="submitAddAdvertisement" :loading="addLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑广告弹窗 -->
    <el-dialog
        v-model="showEditAdvertisementModal"
        title="编辑广告"
        width="600px"
        @closed="resetEditAdvertisement"
        :close-on-click-modal="false"
    >
      <el-form
          :model="editAdvertisementData"
          label-width="100px"
          :rules="rules"
          ref="editFormRef"
          class="dialog-form"
      >
        <el-form-item label="广告标题" prop="title">
          <el-input
              v-model="editAdvertisementData.title"
              placeholder="请输入广告标题"
              clearable
          />
        </el-form-item>
        <el-form-item label="投放页面" prop="launchPage">
          <el-select
              v-model="editAdvertisementData.launchPage"
              placeholder="请选择投放页面"
              style="width: 100%"
          >
            <el-option :value="1" label="banner" />
          </el-select>
        </el-form-item>
        <el-form-item label="广告图片">
          <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="(file) => handleFileUpload(file, 'edit')"
              accept="image/*"
              class="image-uploader"
          >
            <el-button type="primary" icon="Upload">
              选择图片
            </el-button>
          </el-upload>
          <div v-if="editAdvertisementData.imagePreview" class="image-preview">
            <el-image
                :src="editAdvertisementData.imagePreview"
                class="preview-image"
                fit="cover"
            />
            <div class="preview-actions">
              <el-button
                  size="small"
                  type="danger"
                  icon="Delete"
                  @click="editAdvertisementData.imagePreview = ''; editAdvertisementData.imageFile = null;"
              >
                移除
              </el-button>
            </div>
          </div>
          <el-image
              v-if="editAdvertisementData.picture && !editAdvertisementData.imagePreview"
              :src="getImageUrl(editAdvertisementData.picture)"
              class="preview-image"
              fit="cover"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
              v-model="editAdvertisementData.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditAdvertisementModal = false">取消</el-button>
          <el-button type="primary" @click="submitEditAdvertisement" :loading="editLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue';
import {useRouter} from 'vue-router';
import axios from "axios";
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  ArrowLeft,
  Plus,
  Search,
  Refresh,
  Edit,
  Delete,
  Upload,
  Picture,
  Loading
} from '@element-plus/icons-vue';

const router = useRouter();

// 响应式数据
const advertisements = reactive({
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
const showAddAdvertisementModal = ref(false);
const showEditAdvertisementModal = ref(false);
const addFormRef = ref();
const editFormRef = ref();
const multipleTableRef = ref();
const multipleSelection = ref([]);

const searchForm = reactive({
  title: '',
  status: null
});

const newAdvertisement = reactive({
  title: '',
  launchPage: 1,
  picture: '',
  status: 1,
  imageFile: null,
  imagePreview: ''
});

const editAdvertisementData = reactive({
  id: null,
  title: '',
  launchPage: 1,
  picture: '',
  status: 1,
  imageFile: null,
  imagePreview: ''
});

const rules = {
  title: [{required: true, message: '请输入广告标题', trigger: 'blur'}],
  launchPage: [{required: true, message: '请选择投放页面', trigger: 'change'}],
  status: [{required: true, message: '请选择状态', trigger: 'change'}]
};

// API基础URL
const API_BASE_URL = 'http://localhost:8080/adv';

// 计算属性
const emptyText = computed(() => {
  return searchForm.title || searchForm.status !== null
      ? '暂无符合条件的广告'
      : '暂无广告数据';
});

// 工具函数
const getLaunchPageText = (launchPage) => {
  const map = {1: 'banner'};
  return map[launchPage] || '未知';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
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

// 处理Base64图片URL
const getImageUrl = (base64String) => {
  if (!base64String) return '';
  // 检查是否已经是data URL格式
  if (base64String.startsWith('data:image')) {
    return base64String;
  }
  // 如果不是，添加默认的图片类型前缀
  return `data:image/jpeg;base64,${base64String}`;
};

const goHome = () => {
  router.push('/home');
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 批量删除广告
const batchDeleteAdvertisements = async () => {
  try {
    if (!multipleSelection.value.length) {
      return ElMessage.warning('请先勾选要删除的广告');
    }

    await ElMessageBox.confirm(`确定要批量删除选中的 ${multipleSelection.value.length} 个广告吗？此操作不可恢复。`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const ids = multipleSelection.value.map(adv => adv.id);
    for (const id of ids) {
      const stringId = id ? id.toString() : '';
      await axios.delete(`${API_BASE_URL}/${stringId}`);
    }

    await fetchAdvertisements();
    ElMessage.success('批量删除广告成功');
    multipleSelection.value = [];
    multipleTableRef.value.clearSelection();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除广告失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '批量删除广告失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 数据获取
const fetchAdvertisements = async () => {
  // 设置加载状态为true，显示加载动画
  loading.value = true;
  try {
    // 构造请求参数对象
    const params = {
      pageNo: currentPage.value,   // 当前页码
      pageSize: pageSize.value,    // 每页显示数量
      ...searchForm                // 展开搜索表单中的所有字段（如title和status）
    };

    // 移除空值参数，避免向后端发送无意义的空参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });

    // 发送GET请求获取广告数据
    const response = await axios.get(API_BASE_URL, {params});

    // 处理响应数据
    if (response.data && response.data.data) {
      const data = response.data.data;
      // 确保ID以字符串形式存储，避免JavaScript精度问题
      const records = data.records.map(record => ({
        ...record,
        id: record.id ? record.id.toString() : null // 将ID转换为字符串
      }));

      // 更新广告数据
      Object.assign(advertisements, {
        records: records || [],      // 广告记录列表
        total: data.total || 0,      // 总记录数
        pages: data.pages || 0,      // 总页数
        current: data.current || 1,  // 当前页码
        size: data.size || 5         // 每页大小
      });
    }
  } catch (error) {
    // 错误处理
    console.error('获取广告数据失败:', error);
    ElMessage.error('获取广告数据失败，请稍后重试');
  } finally {
    // 无论成功或失败，最后都将加载状态设为false
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchAdvertisements();
};

const resetSearch = () => {
  searchForm.title = '';
  searchForm.status = null;
  currentPage.value = 1;
  fetchAdvertisements();
};

// 文件上传处理 - 转换为Base64
const handleFileUpload = (file, type) => {
  if (!file) return;

  const rawFile = file.raw || file;

  // 检查文件类型
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件');
    return;
  }

  // 检查文件大小 (限制为10MB)
  if (rawFile.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB');
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    // 获取Base64字符串（去掉data URL前缀）
    const base64 = e.target.result.split(',')[1];

    if (type === 'new') {
      newAdvertisement.imageFile = base64;
      newAdvertisement.imagePreview = e.target.result; // 保持完整URL用于预览
    } else {
      editAdvertisementData.imageFile = base64;
      editAdvertisementData.imagePreview = e.target.result; // 保持完整URL用于预览
    }
  };
  reader.readAsDataURL(rawFile);
};

// 重置新增表单
const resetNewAdvertisement = () => {
  Object.assign(newAdvertisement, {
    title: '',
    launchPage: 1,
    picture: '',
    status: 1,
    imageFile: null,
    imagePreview: ''
  });
};

// 重置编辑表单
const resetEditAdvertisement = () => {
  Object.assign(editAdvertisementData, {
    id: null,
    title: '',
    launchPage: 1,
    picture: '',
    status: 1,
    imageFile: null,
    imagePreview: ''
  });
};

// 广告操作
const submitAddAdvertisement = async () => {
  if (!addFormRef.value) return;

  try {
    await addFormRef.value.validate();
    addLoading.value = true;

    const formData = new FormData();
    formData.append('title', newAdvertisement.title);
    formData.append('launchPage', newAdvertisement.launchPage.toString());
    formData.append('status', newAdvertisement.status.toString());

    if (newAdvertisement.imageFile) {
      // 对Base64进行URL编码，防止特殊字符问题
      formData.append('pictureBase64', encodeURIComponent(newAdvertisement.imageFile));
    }

    await axios.post(`${API_BASE_URL}/add`, formData, {
      headers: {'Content-Type': 'multipart/form-data'}
    });

    await fetchAdvertisements();
    showAddAdvertisementModal.value = false;
    ElMessage.success('广告添加成功');
  } catch (error) {
    console.error('添加广告失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data || '添加广告失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    addLoading.value = false;
  }
};

const editAdvertisement = (advertisement) => {
  Object.assign(editAdvertisementData, {
    id: advertisement.id ? advertisement.id.toString() : null, // 确保ID是字符串
    title: advertisement.title,
    launchPage: advertisement.launchPage,
    picture: advertisement.picture,
    status: advertisement.status,
    imageFile: null,
    imagePreview: ''
  });
  showEditAdvertisementModal.value = true;
};

const submitEditAdvertisement = async () => {
  if (!editFormRef.value) return;

  try {
    await editFormRef.value.validate();
    editLoading.value = true;

    const formData = new FormData();
    // 确保ID作为字符串传递
    formData.append('id', editAdvertisementData.id ? editAdvertisementData.id.toString() : '');
    formData.append('title', editAdvertisementData.title);
    formData.append('launchPage', editAdvertisementData.launchPage.toString());
    formData.append('status', editAdvertisementData.status.toString());

    if (editAdvertisementData.imageFile) {
      // 对Base64进行URL编码，防止特殊字符问题
      formData.append('pictureBase64', encodeURIComponent(editAdvertisementData.imageFile));
    }

    await axios.put(`${API_BASE_URL}/edit`, formData, {
      headers: {'Content-Type': 'multipart/form-data'}
    });

    await fetchAdvertisements();
    showEditAdvertisementModal.value = false;
    ElMessage.success('广告更新成功');
  } catch (error) {
    console.error('更新广告失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data || '更新广告失败，请稍后重试';
    ElMessage.error(errorMsg);
  } finally {
    editLoading.value = false;
  }
};

const deleteAdvertisement = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条广告吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    // 确保ID作为字符串传递
    const stringId = id ? id.toString() : '';
    await axios.delete(`${API_BASE_URL}/${stringId}`);
    await fetchAdvertisements();
    ElMessage.success('广告删除成功');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除广告失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '删除广告失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 分页处理
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchAdvertisements();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchAdvertisements();
};

// 初始化
onMounted(fetchAdvertisements);
</script>

<style scoped>
@import url('@/assets/StyleCss.css');
</style>