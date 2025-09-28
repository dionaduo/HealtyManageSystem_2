<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">订单管理</h2>
      </div>
      <el-button type="primary" class="export-btn" icon="Download" @click="handleExportAll">
        导出全部
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="订单编号">
          <el-input
              v-model="searchForm.code"
              placeholder="请输入订单编号"
              clearable
              style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="志愿者姓名">
          <el-input
              v-model="searchForm.volunteerName"
              placeholder="请输入志愿者姓名"
              clearable
              style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="志愿者手机号">
          <el-input
              v-model="searchForm.volunteerMobile"
              placeholder="请输入志愿者手机号"
              clearable
              style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="联系人姓名">
          <el-input
              v-model="searchForm.contactName"
              placeholder="请输入联系人姓名"
              clearable
              style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="联系电话">
          <el-input
              v-model="searchForm.contactMobile"
              placeholder="请输入联系电话"
              clearable
              style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="订单状态">
          <el-select
              v-model="searchForm.status"
              placeholder="请选择订单状态"
              clearable
              style="width: 120px"
          >
            <el-option label="待付款" :value="1" />
            <el-option label="待接单" :value="2" />
            <el-option label="待完成" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已评价" :value="5" />
            <el-option label="已关闭" :value="6" />
            <el-option label="已取消" :value="7" />
          </el-select>
        </el-form-item>

        <el-form-item label="支付状态">
          <el-select
              v-model="searchForm.paymentStatus"
              placeholder="请选择支付状态"
              clearable
              style="width: 100px"
          >
            <el-option label="已支付" :value="1" />
            <el-option label="未支付" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="服务类型">
          <el-select
              v-model="searchForm.serviceType"
              placeholder="请选择服务类型"
              clearable
              style="width: 120px"
          >
            <el-option label="上门护理" value="HC" />
            <el-option label="上门体检" value="HE" />
          </el-select>
        </el-form-item>

        <el-form-item label="下单时间">
          <el-date-picker
              v-model="searchForm.orderTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 360px"
          />
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

    <!-- 表格区域 -->
    <el-card class="table-card card-hover-animate" shadow="never">
      <el-table
          v-loading="loading"
          :data="orders.records"
          class="data-table"
          stripe
          @selection-change="handleSelectionChange"
          :empty-text="emptyText"
          ref="multipleTableRef"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" width="80" type="index" :index="(index) => (currentPage - 1) * pageSize + index + 1" align="center" />

        <el-table-column label="订单编号" prop="code" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span class="order-code">{{ getOrderInfo(scope.row).code || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="支付状态" prop="paymentStatus" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getOrderInfo(scope.row).paymentStatus === 1 ? 'success' : 'danger'" size="small">
              {{ getOrderInfo(scope.row).paymentStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="订单状态" prop="status" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getOrderStatusTagType(getOrderInfo(scope.row).status)" size="small">
              {{ getOrderStatusText(getOrderInfo(scope.row).status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="服务老人" prop="serviceObjectName" width="120" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ getOrderInfo(scope.row).serviceObjectName || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="志愿者" width="150" show-overflow-tooltip>
          <template #default="scope">
            <div>
              <div>{{ getVolunteerName(getOrderInfo(scope.row).volunteerUserId) || '—' }}</div>
              <div class="volunteer-mobile" v-if="getOrderInfo(scope.row).volunteerMobile">
                {{ getOrderInfo(scope.row).volunteerMobile }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系人" width="150" show-overflow-tooltip>
          <template #default="scope">
            <div>
              <div>{{ getOrderInfo(scope.row).contactName || '—' }}</div>
              <div class="contact-mobile" v-if="getOrderInfo(scope.row).contactMobile">
                {{ getOrderInfo(scope.row).contactMobile }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="服务类型" prop="serviceType" width="100" align="center">
          <template #default="scope">
            <span>{{ getServiceTypeText(getOrderInfo(scope.row).code) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单金额" prop="amount" width="100" align="center">
          <template #default="scope">
            <span v-if="getOrderInfo(scope.row).amount">¥{{ getOrderInfo(scope.row).amount }}</span>
            <span v-else>—</span>
          </template>
        </el-table-column>

        <el-table-column label="服务评价" width="120" align="center">
          <template #default="scope">
            <el-tag
                v-if="scope.row.evaluate && scope.row.evaluate.level"
                :type="getEvaluateTagType(scope.row.evaluate.level)"
                size="small"
            >
              {{ getEvaluateText(scope.row.evaluate.level) }}
            </el-tag>
            <span v-else class="text-muted">暂无评价</span>
          </template>
        </el-table-column>

        <el-table-column label="投诉" prop="complaintFlag" width="80" align="center">
          <template #default="scope">
            <el-tag
                :type="getOrderInfo(scope.row).complaintFlag === 1 ? 'danger' : 'info'"
                size="small"
            >
              {{ getOrderInfo(scope.row).complaintFlag === 1 ? '有' : '无' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="回访状态" prop="revisitStatus" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getOrderInfo(scope.row).revisitStatus === 1 ? 'success' : 'info'" size="small">
              {{ getOrderInfo(scope.row).revisitStatus === 1 ? '已回访' : '未回访' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下单时间" prop="orderTime" width="160">
          <template #default="scope">
            <span class="time-text">{{ formatDate(getOrderInfo(scope.row).orderTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="primary"
                  link
                  @click="handleDetail(scope.row)"
                  icon="View"
                  class="btn-hover-animate"
              >详情</el-button>

              <el-button
                  size="small"
                  type="success"
                  link
                  @click="handleRevisit(scope.row)"
                  :disabled="!canRevisit(scope.row)"
                  :icon="canRevisit(scope.row) ? 'Edit' : 'Warning'"
                  class="btn-hover-animate"
              >回访</el-button>

              <el-button
                  size="small"
                  type="danger"
                  link
                  @click="handleDelete(scope.row)"
                  icon="Delete"
                  class="btn-hover-animate"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="orders.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
        />
      </div>
    </el-card>

    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="multipleSelection.length > 0">
      <el-button
          type="danger"
          @click="handleBatchDelete"
          icon="Delete"
          class="btn-hover-animate"
      >
        批量删除 ({{ multipleSelection.length }})
      </el-button>
      <el-button
          type="info"
          @click="handleBatchExport"
          icon="Download"
          class="btn-hover-animate"
      >
        批量导出 ({{ multipleSelection.length }})
      </el-button>
    </div>

    <!-- 详情对话框 -->
    <OrderDetailDialog
        v-model="detailVisible"
        :order-id="currentOrderId"
        @close="handleDetailClose"
    />

    <!-- 回访对话框 -->
    <OrderRevisitDialog
        v-model="revisitVisible"
        :order="currentOrder"
        @success="handleRevisitSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import OrderDetailDialog from '../components/OrderDetailDialog.vue';
import OrderRevisitDialog from '../components/OrderRevisitDialog.vue';
import {
  listOrder,
  batchDeleteOrder,
  deleteOrder,
  exportOrders
} from "@/api/auth.js";

const router = useRouter();

// 响应式数据
const orders = reactive({
  records: [],
  total: 0,
  pages: 0,
  current: 1,
  size: 10
});

const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const detailVisible = ref(false);
const currentOrderId = ref(null);
const revisitVisible = ref(false);
const currentOrder = ref(null);
const multipleSelection = ref([]);
const multipleTableRef = ref();

// 搜索表单
const searchForm = reactive({
  code: '',
  volunteerName: '',
  volunteerMobile: '',
  contactName: '',
  contactMobile: '',
  status: null,
  paymentStatus: null,
  serviceType: null,
  orderTimeRange: []
});

// 计算属性
const emptyText = computed(() => {
  return searchForm.code || searchForm.volunteerName || searchForm.volunteerMobile ||
  searchForm.contactName || searchForm.contactMobile || searchForm.status !== null ||
  searchForm.paymentStatus !== null || searchForm.serviceType !== null ||
  (searchForm.orderTimeRange && searchForm.orderTimeRange.length > 0)
      ? '暂无符合条件的订单'
      : '暂无订单数据';
});

// 工具函数 - 安全获取orderInfo
const getOrderInfo = (row) => {
  return row.orderInfo || {};
};

const formatDate = (dateString) => {
  if (!dateString) return '—';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const goHome = () => {
  router.push('/home');
};

// 数据获取
const fetchOrders = async () => {
  loading.value = true;
  try {
    // 构建搜索参数
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value
    };

    // 只添加非空的搜索条件
    if (searchForm.code) {
      params.code = searchForm.code;
    }
    if (searchForm.volunteerName) {
      params.volunteerName = searchForm.volunteerName;
    }
    if (searchForm.volunteerMobile) {
      params.volunteerMobile = searchForm.volunteerMobile;
    }
    if (searchForm.contactName) {
      params.contactName = searchForm.contactName;
    }
    if (searchForm.contactMobile) {
      params.contactMobile = searchForm.contactMobile;
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      params.status = searchForm.status;
    }
    if (searchForm.paymentStatus !== null && searchForm.paymentStatus !== undefined) {
      params.paymentStatus = searchForm.paymentStatus;
    }
    if (searchForm.serviceType !== null && searchForm.serviceType !== undefined) {
      params.serviceType = searchForm.serviceType;
    }
    if (searchForm.orderTimeRange && searchForm.orderTimeRange.length === 2) {
      params.startTime = searchForm.orderTimeRange[0];
      params.endTime = searchForm.orderTimeRange[1];
    }

    const response = await listOrder(params);
    console.log('API返回数据:', response);
    if (response.code === 200 && response.data) {
      const data = response.data;
      console.log('处理后的数据:', data);

      // 处理数据，确保orderInfo不为null
      const processedRecords = (data.records || []).map(record => {
        if (!record.orderInfo) {
          record.orderInfo = {};
        }
        return record;
      });

      Object.assign(orders, {
        records: processedRecords,
        total: data.total || 0,
        pages: Math.ceil(data.total / pageSize.value) || 0,
        current: currentPage.value,
        size: pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取订单数据失败');
    }
  } catch (error) {
    console.error('获取订单数据失败:', error);
    ElMessage.error('获取订单数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchOrders();
};

const resetSearch = () => {
  Object.assign(searchForm, {
    code: '',
    volunteerName: '',
    volunteerMobile: '',
    contactName: '',
    contactMobile: '',
    status: null,
    paymentStatus: null,
    serviceType: null,
    orderTimeRange: []
  });
  currentPage.value = 1;
  fetchOrders();
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 批量删除
const handleBatchDelete = async () => {
  if (!multipleSelection.value.length) {
    return ElMessage.warning('请先勾选要删除的订单');
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除选中的${multipleSelection.value.length}个订单吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const ids = multipleSelection.value.map(item => getOrderInfo(item).id).filter(id => id);
    if (ids.length === 0) {
      return ElMessage.warning('选中的订单没有有效的ID');
    }

    const response = await batchDeleteOrder({ ids });

    if (response.code === 200) {
      ElMessage.success('批量删除成功');
      await fetchOrders();
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

// 导出功能
const handleExportAll = async () => {
  try {
    // 构建导出参数
    const exportParams = {};

    // 只添加非空的搜索条件
    if (searchForm.code) {
      exportParams.code = searchForm.code;
    }
    if (searchForm.volunteerName) {
      exportParams.volunteerName = searchForm.volunteerName;
    }
    if (searchForm.volunteerMobile) {
      exportParams.volunteerMobile = searchForm.volunteerMobile;
    }
    if (searchForm.contactName) {
      exportParams.contactName = searchForm.contactName;
    }
    if (searchForm.contactMobile) {
      exportParams.contactMobile = searchForm.contactMobile;
    }
    if (searchForm.status !== null && searchForm.status !== undefined) {
      exportParams.status = searchForm.status;
    }
    if (searchForm.paymentStatus !== null && searchForm.paymentStatus !== undefined) {
      exportParams.paymentStatus = searchForm.paymentStatus;
    }
    if (searchForm.serviceType !== null && searchForm.serviceType !== undefined) {
      exportParams.serviceType = searchForm.serviceType;
    }
    if (searchForm.orderTimeRange && searchForm.orderTimeRange.length === 2) {
      exportParams.startTime = searchForm.orderTimeRange[0];
      exportParams.endTime = searchForm.orderTimeRange[1];
    }

    const response = await exportOrders({
      queryDTO: exportParams
    });

    handleExportResponse(response, `订单_全部导出_${new Date().getTime()}`);
  } catch (error) {
    console.error('导出全部失败:', error);
    ElMessage.error('导出全部失败');
  }
};

const handleBatchExport = async () => {
  if (!multipleSelection.value.length) {
    return ElMessage.warning('请先勾选要导出的订单');
  }

  try {
    const ids = multipleSelection.value.map(item => getOrderInfo(item).id).filter(id => id);
    if (ids.length === 0) {
      return ElMessage.warning('选中的订单没有有效的ID');
    }

    const response = await exportOrders({
      ids: ids
    });

    handleExportResponse(response, `订单_批量导出_${new Date().getTime()}`);
  } catch (error) {
    console.error('批量导出失败:', error);
    ElMessage.error('批量导出失败');
  }
};

const handleExportResponse = (response, fileName) => {
  const blob = new Blob([response], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });

  const downloadElement = document.createElement('a');
  const href = window.URL.createObjectURL(blob);
  downloadElement.href = href;
  downloadElement.download = `${fileName}.xlsx`;
  document.body.appendChild(downloadElement);
  downloadElement.click();
  document.body.removeChild(downloadElement);
  window.URL.revokeObjectURL(href);

  ElMessage.success('导出成功');
};

// 订单操作
const handleDetail = (row) => {
  const orderId = getOrderInfo(row).id;
  console.log('订单ID:', orderId)
  console.log('行详细信息:' , row)
  if (!orderId) {
    ElMessage.warning('该订单没有有效的ID');
    return;
  }
  currentOrderId.value = orderId;
  detailVisible.value = true;
};

const handleDetailClose = () => {
  currentOrderId.value = null;
};

const canRevisit = (row) => {
  const status = getOrderInfo(row).status;
  console.log('订单状态:', status, '可回访:', status === 4 || status === 5);
  return status === 4 || status === 5; // 已完成或已评价状态
};

const handleRevisit = (row) => {
  console.log('点击回访:', row);

  if (!canRevisit(row)) {
    ElMessage.warning('该订单状态不可回访，仅支持已完成或已评价的订单');
    return;
  }

  const orderId = getOrderInfo(row).id;
  if (!orderId) {
    ElMessage.warning('该订单没有有效的ID');
    return;
  }

  currentOrder.value = row;
  revisitVisible.value = true;
  console.log('回访弹窗已打开');
};

const handleRevisitSuccess = () => {
  fetchOrders();
  revisitVisible.value = false;
  currentOrder.value = null;
};

const handleDelete = async (row) => {
  const orderId = getOrderInfo(row).id;
  if (!orderId) {
    ElMessage.warning('该订单没有有效的ID');
    return;
  }

  try {
    await ElMessageBox.confirm(
        '确定要删除该订单吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
    );

    const response = await deleteOrder(orderId);
    if (response.code === 200) {
      ElMessage.success('删除订单成功');
      await fetchOrders();
    } else {
      ElMessage.error(response.msg || '删除订单失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error);
      ElMessage.error('删除订单失败');
    }
  }
};

// 分页处理
const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchOrders();
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchOrders();
};

// 状态映射函数
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

const getOrderStatusTagType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'info',
    3: 'primary',
    4: 'success',
    5: 'success',
    6: 'danger',
    7: 'danger'
  };
  return typeMap[status] || 'info';
};

const getVolunteerName = (volunteerUserId) => {
  if (!volunteerUserId) return '—';
  return `志愿者${volunteerUserId}`;
};

const getServiceTypeText = (code) => {
  if (!code) return '—';
  const typeCode = code.substring(0, 2);
  const typeMap = {
    'HC': '上门护理',
    'HE': '上门体检'
  };
  return typeMap[typeCode] || '—';
};

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

const getEvaluateTagType = (level) => {
  const typeMap = {
    1: 'danger',
    2: 'warning',
    3: 'info',
    4: 'success',
    5: 'success'
  };
  return typeMap[level] || 'info';
};

// 生命周期
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
@import "@/assets/StyleCss.css";

</style>