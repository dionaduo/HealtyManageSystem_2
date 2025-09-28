<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">志愿者管理</h2>
      </div>
      <el-button type="primary" class="add-btn" icon="Plus" @click="showAddVolunteerModal = true">
        新增志愿者
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="姓名">
          <el-input
              v-model="searchForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
              v-model="searchForm.mobile"
              placeholder="请输入手机号"
              clearable
              style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
              v-model="searchForm.auditStatus"
              placeholder="请选择审核状态"
              clearable
              style="width: 120px"
          >
            <el-option label="未审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="未通过" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型">
          <el-select
              v-model="searchForm.serviceTypeId"
              placeholder="请选择服务类型"
              clearable
              style="width: 150px"
          >
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="持证上岗">
          <el-select
              v-model="searchForm.certificateFlag"
              placeholder="请选择"
              clearable
              style="width: 100px"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch" class="btn-hover-animate">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="resetSearch" class="btn-hover-animate">
            重置
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
              type="success"
              icon="Download"
              @click="handleExportAll"
              class="btn-hover-animate"
          >
            导出全部
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card card-hover-animate" shadow="never">
      <el-table
          :data="volunteers.records"
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
        <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="sex" label="性别" width="80">
          <template #default="scope">
            <span>{{ scope.row.sex === 1 ? '男' : '女' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="mobile" label="手机号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="serviceTypeName" label="服务类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="serviceContentName" label="服务内容" min-width="120" show-overflow-tooltip />
        <el-table-column prop="certificateFlag" label="持证上岗" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.certificateFlag === 1 ? 'success' : 'info'">
              {{ scope.row.certificateFlag === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weekendsTakeOrders" label="周末接单" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.weekendsTakeOrders === 1 ? 'success' : 'info'">
              {{ scope.row.weekendsTakeOrders === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="serviceStartHour" label="服务开始时间" width="120" />
        <el-table-column prop="serviceEndHour" label="服务结束时间" width="120" />
        <el-table-column prop="auditStatus" label="审核状态" width="100" align="center">
          <template #default="scope">
            <el-tag
                :type="scope.row.auditStatus === 1 ? 'success' : scope.row.auditStatus === 2 ? 'danger' : 'warning'"
            >
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  v-if="scope.row.auditStatus === 0"
                  size="small"
                  type="success"
                  link
                  icon="Check"
                  @click="handleAudit(scope.row.id, 1)"
                  class="action-buttons"
              >
                通过
              </el-button>
              <el-button
                  v-if="scope.row.auditStatus === 0"
                  size="small"
                  type="danger"
                  link
                  icon="Close"
                  @click="handleAudit(scope.row.id, 2)"
                  class="action-buttons"
              >
                拒绝
              </el-button>
              <el-button
                  size="small"
                  type="primary"
                  link
                  icon="Edit"
                  @click="editVolunteerHandle(scope.row)"
                  class="action-buttons"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="deleteVolunteerHandle(scope.row.id)"
                  class="action-buttons"
              >
                删除
              </el-button>
              <el-button
                  size="small"
                  type="info"
                  link
                  icon="View"
                  @click="viewVolunteerDetail(scope.row)"
                  class="action-buttons"
              >
                详情
              </el-button>
              <el-button
                  size="small"
                  type="warning"
                  link
                  icon="Download"
                  @click="exportSingleVolunteer(scope.row)"
                  class="action-buttons"
              >
                导出
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
          :total="volunteers.total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          background
      />
    </div>

    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="multipleSelection.length > 0">
      <el-button type="primary" icon="Check" @click="handleBatchAudit(1)" class="btn-hover-animate">
        批量通过 ({{ multipleSelection.length }})
      </el-button>
      <el-button type="warning" icon="Close" @click="handleBatchAudit(2)" class="btn-hover-animate">
        批量拒绝 ({{ multipleSelection.length }})
      </el-button>
      <el-button type="danger" icon="Delete" @click="handleBatchDelete" class="btn-hover-animate">
        批量删除 ({{ multipleSelection.length }})
      </el-button>
      <el-button
          type="info"
          icon="Download"
          @click="handleBatchExport"
          class="btn-hover-animate"
      >
        批量导出 ({{ multipleSelection.length }})
      </el-button>
    </div>

    <!-- 新增志愿者弹窗 -->
    <el-dialog
        v-model="showAddVolunteerModal"
        title="新增志愿者"
        width="900px"
        @closed="resetNewVolunteer"
        :close-on-click-modal="false"
        class="page-enter-active"
    >
      <VolunteerForm
          ref="addFormRef"
          :volunteer-data="newVolunteer"
          :loading="addLoading"
          :service-type-options="typeOptions"
          @submit="submitAddVolunteer"
          @cancel="showAddVolunteerModal = false"
      />
    </el-dialog>

    <!-- 编辑志愿者弹窗 -->
    <el-dialog
        v-model="showEditVolunteerModal"
        title="编辑志愿者"
        width="900px"
        @closed="resetEditVolunteer"
        :close-on-click-modal="false"
        class="page-enter-active"
    >
      <VolunteerForm
          ref="editFormRef"
          :volunteer-data="editVolunteerData"
          :loading="editLoading"
          :service-type-options="typeOptions"
          @submit="submitEditVolunteer"
          @cancel="showEditVolunteerModal = false"
      />
    </el-dialog>

    <!-- 志愿者详情弹窗 -->
    <el-dialog
        v-model="showDetailModal"
        title="志愿者详情"
        width="900px"
        :close-on-click-modal="false"
    >
      <VolunteerDetail
          :volunteer-data="currentVolunteerDetail"
          @close="showDetailModal = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import VolunteerForm from '../components/VolunteerForm.vue';
import VolunteerDetail from '../components/VolunteerDetail.vue';
import {
  listVolunteer,
  getVolunteerDetail,
  addVolunteer,
  editVolunteer,
  deleteVolunteer,
  auditVolunteer,
  batchDeleteVolunteer,
  typeService
} from "@/api/auth.js";

const router = useRouter();

// 响应式数据
const volunteers = reactive({
  records: [],
  total: 0,
  pages: 0,
  current: 1,
  size: 5
});

const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const addLoading = ref(false);
const editLoading = ref(false);
const showAddVolunteerModal = ref(false);
const showEditVolunteerModal = ref(false);
const showDetailModal = ref(false);
const typeOptions = ref([]);
const multipleSelection = ref([]);
const multipleTableRef = ref();
const addFormRef = ref();
const editFormRef = ref();
const currentVolunteerDetail = ref({});

const searchForm = reactive({
  name: '',
  mobile: '',
  auditStatus: null,
  serviceTypeId: null, // 改为null而不是空字符串
  certificateFlag: null
});

const newVolunteer = reactive({
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
  serviceTypeIds: [], // 改为数组以匹配新的表单结构
  serviceContents: [], // 添加serviceContents数组
  auditStatus: 0
});

const editVolunteerData = reactive({
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
  serviceTypeIds: [], // 改为数组
  serviceContents: [], // 添加serviceContents数组
  auditStatus: 0
});

// 计算属性
const emptyText = computed(() => {
  return searchForm.name || searchForm.mobile || searchForm.auditStatus !== null ||
  searchForm.serviceTypeId || searchForm.certificateFlag !== null
      ? '暂无符合条件的志愿者'
      : '暂无志愿者数据';
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

const goHome = () => {
  router.push('/home');
};

// 数据获取
const fetchVolunteers = async () => {
  loading.value = true;
  try {
    // 构建搜索参数
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value
    };

    // 只添加非空的搜索条件
    if (searchForm.name) {
      params.name = searchForm.name;
    }
    if (searchForm.mobile) {
      params.mobile = searchForm.mobile;
    }
    if (searchForm.auditStatus !== null && searchForm.auditStatus !== undefined) {
      params.auditStatus = searchForm.auditStatus;
    }
    if (searchForm.serviceTypeId !== null && searchForm.serviceTypeId !== undefined) {
      params.serviceTypeId = searchForm.serviceTypeId;
    }
    if (searchForm.certificateFlag !== null && searchForm.certificateFlag !== undefined) {
      params.certificateFlag = searchForm.certificateFlag;
    }

    const response = await listVolunteer(params);
    if (response.code === 200 && response.data) {
      const data = response.data;
      Object.assign(volunteers, {
        records: data.records || [],
        total: data.total || 0,
        pages: Math.ceil(data.total / pageSize.value) || 0,
        current: currentPage.value,
        size: pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取志愿者数据失败');
    }
  } catch (error) {
    console.error('获取志愿者数据失败:', error);
    ElMessage.error('获取志愿者数据失败，请稍后重试');
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
        value: item.id,
      }));
    } else {
      ElMessage.error(response.msg || '获取服务类型失败');
    }
  } catch (error) {
    console.error('获取服务类型失败:', error);
    ElMessage.error('获取服务类型失败');
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchVolunteers();
};

const resetSearch = () => {
  searchForm.name = '';
  searchForm.mobile = '';
  searchForm.auditStatus = null;
  searchForm.serviceTypeId = null;
  searchForm.certificateFlag = null;
  currentPage.value = 1;
  fetchVolunteers();
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 批量审核
const handleBatchAudit = async (status) => {
  if (!multipleSelection.value.length) {
    return ElMessage.warning('请先勾选选项');
  }

  try {
    const action = status === 1 ? '通过' : '拒绝';
    await ElMessageBox.confirm(
        `确定要${action}选中的${multipleSelection.value.length}个志愿者吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const ids = multipleSelection.value.map(item => item.id);
    const response = await auditVolunteer({ ids, auditStatus: status });

    if (response.code === 200) {
      ElMessage.success(`批量${action}成功`);
      await fetchVolunteers();
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
    } else {
      ElMessage.error(response.msg || `批量${action}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error);
      ElMessage.error('批量审核失败');
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  if (!multipleSelection.value.length) {
    return ElMessage.warning('请先勾选选项');
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除选中的${multipleSelection.value.length}个志愿者吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const ids = multipleSelection.value.map(item => item.id);
    const response = await batchDeleteVolunteer({ ids });

    if (response.code === 200) {
      ElMessage.success('批量删除成功');
      await fetchVolunteers();
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
    } else {
      ElMessage.error(response.msg || '批量删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error('批量删除失败');
    }
  }
};

// 重置新增表单
const resetNewVolunteer = () => {
  Object.assign(newVolunteer, {
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
};

// 重置编辑表单
const resetEditVolunteer = () => {
  Object.assign(editVolunteerData, {
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
};

// 查看志愿者详情
const viewVolunteerDetail = async (row) => {
  try {
    const response = await getVolunteerDetail(row.id);
    if (response.code === 200 && response.data) {
      currentVolunteerDetail.value = response.data;
      showDetailModal.value = true;
    } else {
      ElMessage.error(response.msg || '获取志愿者详情失败');
    }
  } catch (error) {
    console.error('获取志愿者详情失败:', error);
    ElMessage.error('获取志愿者详情失败');
  }
};

// 志愿者操作
const submitAddVolunteer = async (formData) => {
  try {
    addLoading.value = true;
    // 确保serviceContents正确传递
    const submitData = {
      ...formData,
      serviceContents: formData.serviceContents || []
    };

    const response = await addVolunteer(submitData);
    if (response.code === 200) {
      ElMessage.success('新增志愿者成功');
      showAddVolunteerModal.value = false;
      await fetchVolunteers();
    } else {
      ElMessage.error(response.msg || '新增志愿者失败');
    }
  } catch (error) {
    console.error('新增志愿者失败:', error);
    ElMessage.error('新增志愿者失败，请稍后重试');
  } finally {
    addLoading.value = false;
  }
};

const editVolunteerHandle = async (row) => {
  try {
    const response = await getVolunteerDetail(row.id);
    if (response.code === 200 && response.data) {
      Object.assign(editVolunteerData, response.data);
      showEditVolunteerModal.value = true;

      // 等待DOM更新后重置表单验证
      await nextTick(() => {
        if (editFormRef.value) {
          editFormRef.value.clearValidate();
        }
      });
    } else {
      ElMessage.error(response.msg || '获取志愿者详情失败');
      console.error('获取详情响应:', response);
    }
  } catch (error) {
    console.error('获取志愿者详情失败:', error);
    ElMessage.error('获取志愿者详情失败: ' + error.message);
  }
};

const submitEditVolunteer = async (formData) => {
  try {
    editLoading.value = true;
    // 确保serviceContents正确传递
    const submitData = {
      ...formData,
      serviceContents: formData.serviceContents || []
    };

    const response = await editVolunteer(submitData);
    if (response.code === 200) {
      ElMessage.success('编辑志愿者成功');
      showEditVolunteerModal.value = false;
      await fetchVolunteers();
    } else {
      ElMessage.error(response.msg || '编辑志愿者失败');
    }
  } catch (error) {
    console.error('编辑志愿者失败:', error);
    ElMessage.error('编辑志愿者失败，请稍后重试');
  } finally {
    editLoading.value = false;
  }
};

const deleteVolunteerHandle = async (id) => {
  try {
    await ElMessageBox.confirm(
        '确定要删除该志愿者吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const response = await deleteVolunteer(id);
    if (response.code === 200) {
      ElMessage.success('删除志愿者成功');
      await fetchVolunteers();
    } else {
      ElMessage.error(response.msg || '删除志愿者失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除志愿者失败:', error);
      ElMessage.error('删除志愿者失败');
    }
  }
};

const handleAudit = async (id, status) => {
  try {
    const action = status === 1 ? '通过' : '拒绝';
    await ElMessageBox.confirm(
        `确定要${action}该志愿者吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const response = await auditVolunteer({ ids: [id], auditStatus: status });
    if (response.code === 200) {
      ElMessage.success(`审核${action}成功`);
      await fetchVolunteers();
    } else {
      ElMessage.error(response.msg || `审核${action}失败`);
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核操作失败:', error);
      ElMessage.error('审核操作失败');
    }
  }
};

// 分页处理
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchVolunteers();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchVolunteers();
};

import { exportVolunteers } from "@/api/auth.js";

// 导出单个志愿者
const exportSingleVolunteer = async (row) => {
  try {
    const response = await exportVolunteers({
      ids: [row.id]
    });

    handleExportResponse(response, `志愿者_${row.name}`);
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  }
};

// 批量导出
const handleBatchExport = async () => {
  if (!multipleSelection.value.length) {
    return ElMessage.warning('请先勾选要导出的志愿者');
  }

  try {
    const ids = multipleSelection.value.map(item => item.id);
    const response = await exportVolunteers({
      ids: ids
    });

    handleExportResponse(response, `志愿者_批量导出_${new Date().getTime()}`);
  } catch (error) {
    console.error('批量导出失败:', error);
    ElMessage.error('批量导出失败');
  }
};

// 导出全部（根据搜索条件）
const handleExportAll = async () => {
  try {
    // 构建导出参数
    const exportParams = {};

    // 只添加非空的搜索条件
    if (searchForm.name) {
      exportParams.name = searchForm.name;
    }
    if (searchForm.mobile) {
      exportParams.mobile = searchForm.mobile;
    }
    if (searchForm.auditStatus !== null && searchForm.auditStatus !== undefined) {
      exportParams.auditStatus = searchForm.auditStatus;
    }
    if (searchForm.serviceTypeId !== null && searchForm.serviceTypeId !== undefined) {
      exportParams.serviceTypeId = searchForm.serviceTypeId;
    }
    if (searchForm.certificateFlag !== null && searchForm.certificateFlag !== undefined) {
      exportParams.certificateFlag = searchForm.certificateFlag;
    }

    const response = await exportVolunteers({
      queryDTO: exportParams
    });

    handleExportResponse(response, `志愿者_全部导出_${new Date().getTime()}`);
  } catch (error) {
    console.error('导出全部失败:', error);
    ElMessage.error('导出全部失败');
  }
};

// 处理导出响应
const handleExportResponse = (response, fileName) => {
  // 创建blob对象
  const blob = new Blob([response], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });

  // 创建下载链接
  const downloadElement = document.createElement('a');
  const href = window.URL.createObjectURL(blob);
  downloadElement.href = href;
  downloadElement.download = `${fileName}.xlsx`;
  document.body.appendChild(downloadElement);

  // 触发下载
  downloadElement.click();

  // 清理
  document.body.removeChild(downloadElement);
  window.URL.revokeObjectURL(href);

  ElMessage.success('导出成功');
};

// 生命周期
onMounted(() => {
  fetchVolunteers();
  fetchServiceTypes();
});
</script>

<style scoped>
@import "@/assets/StyleCss.css";

</style>