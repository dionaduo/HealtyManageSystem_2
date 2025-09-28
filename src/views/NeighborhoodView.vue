<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">小区管理</h2>
      </div>
      <el-button class="add-btn" type="primary" icon="Plus" @click="addHandle">
        新增小区
      </el-button>
    </div>
    <div style="padding:0 0 20px 0">
      <el-button class="add-btn" type="primary" icon="Plus" @click="BatchAdd">
        批量新增小区
      </el-button>
      <el-button
          type="primary"
          @click="exportAll"
          class="btn"
      >
        导出全部
      </el-button>
    </div>
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="小区名称">
          <el-input
              v-model="searchForm.name"
              placeholder="请输入小区名称"
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
          :data="neighborhoods.records"
          class="data-table"
          style="width: 100%"
          v-loading="loading"
          :empty-text="emptyText"
          stripe
          @selection-change="handleSelectionChange"
          ref="multipleTableRef"
      >
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column label="序号" width="80" type="index"
                         :index="(index) => (currentPage - 1) * pageSize + index + 1"/>
        <el-table-column prop="name" label="小区名称" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="createUserId" label="创建人ID" width="120" align="center"/>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="修改时间" width="180">
          <template #default="scope">
            <span class="time-text">{{ formatDate(scope.row.updateTime) }}</span>
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
                  @click="editHandle(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="deleteHandle(scope.row.id)"
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
          :total="neighborhoods.total"
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
          title="确定要批量删除选中的小区吗？此操作不可恢复。"
          @confirm="batchDelete"
          confirm-button-text="确定"
          cancel-button-text="取消"
      >
        <template #reference>
          <el-button type="danger" icon="Delete">
            批量删除 ({{ multipleSelection.length }})
          </el-button>
        </template>
      </el-popconfirm>
      <!-- 添加导出选中按钮 -->
      <el-button
          type="success"
          icon="Download"
          @click="exportSelected"
          style="margin-left: 10px;"
      >
        导出选中 ({{ multipleSelection.length }})
      </el-button>
    </div>

    <!-- 编辑小区弹窗 -->
    <el-dialog
        v-model="showEditModal"
        title="编辑小区"
        width="500px"
        @closed="resetEditForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="editData"
          label-width="80px"
          class="dialog-form"
      >
        <el-form-item label="小区名称" prop="name">
          <el-input
              v-model="editData.name"
              placeholder="请输入小区名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                    <el-button @click="showEditModal = false">取消</el-button>
                    <el-button type="primary" @click="submitEdit">保存</el-button>
                </span>
      </template>
    </el-dialog>

    <!-- 新增小区弹窗 -->
    <el-dialog
        v-model="showAddModal"
        title="新增小区"
        width="500px"
        @closed="resetAddForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="addData"
          label-width="80px"
          class="dialog-form"
      >
        <el-form-item label="小区名称" prop="name">
          <el-input
              v-model="addData.name"
              placeholder="请输入小区名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                    <el-button @click="showAddModal = false">取消</el-button>
                    <el-button type="primary" @click="submitAdd">保存</el-button>
                </span>
      </template>
    </el-dialog>

    <!-- 批量新增小区弹窗-->
    <el-dialog
        v-model="showBatchAddModal"
        title="批量新增小区"
        width="600px"
        @closed="resetBatchAddForm"
        :close-on-click-modal="false"
    >
      <el-form label-width="120px" class="dialog-form">
        <el-form-item label="上传文件">
          <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :file-list="fileList"
              accept=".xlsx,.xls"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请严格按照模板文件上传
                <el-link type="primary" @click="downloadTemplate" style="margin-left: 10px;">
                  下载模板文件
                </el-link>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchAddModal = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitBatchAdd"
              :loading="batchLoading"
              :disabled="!uploadedFile"
          >
            新增
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import * as XLSX from 'xlsx';
import {
  listNeighborhood,
  addNeighborhood,
  editNeighborhood,
  deleteNeighborhood,
  batchDeleteNeighborhood,
  batchAddNeighborhood
} from "@/api/auth.js";

const router = useRouter();
// 响应式数据
const neighborhoods = reactive({
  records: [],
  total: 0,
  pages: 0,
  current: 1,
  size: 5
});
const currentPage = ref(1);
const pageSize = ref(5);
const loading = ref(false);
const showEditModal = ref(false);
const showAddModal = ref(false);
const multipleTableRef = ref();
const multipleSelection = ref([]);
const searchForm = reactive({
  name: ''
});
const editData = reactive({
  id: '',
  name: ''
});
const addData = reactive({
  name: ''
});
const showBatchAddModal = ref(false);
const uploadedFile = ref(null);
const fileList = ref([]);
const batchLoading = ref(false);
const uploadRef = ref();
// 计算属性
const emptyText = computed(() => {
  return searchForm.name
      ? '暂无符合条件的小区'
      : '暂无小区数据';
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
const goHome = () => {
  router.push('/home');
};
// 数据获取
const fetchNeighborhoods = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    };

    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });

    const response = await listNeighborhood(params);
    if (response.code === 200 && response.data) {
      const data = response.data;
      Object.assign(neighborhoods, {
        records: data.records || [],
        total: data.total || 0,
        pages: Math.ceil(data.total / pageSize.value) || 0,
        current: currentPage.value,
        size: pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取小区数据失败');
    }
  } catch (error) {
    console.error('获取小区数据失败:', error);
    ElMessage.error('获取小区数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchNeighborhoods();
};
const resetSearch = () => {
  searchForm.name = '';
  currentPage.value = 1;
  fetchNeighborhoods();
};
// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};
// 重置编辑表单
const resetEditForm = () => {
  Object.assign(editData, {
    id: '',
    name: ''
  });
};
// 重置新增表单
const resetAddForm = () => {
  Object.assign(addData, {
    name: ''
  });
};
// 编辑小区
const editHandle = (neighborhood) => {
  Object.assign(editData, {
    id: neighborhood.id,
    name: neighborhood.name
  });
  showEditModal.value = true;
};
const submitEdit = async () => {
  try {
    if (!editData.name.trim()) {
      ElMessage.warning('请输入小区名称');
      return;
    }

    const response = await editNeighborhood(editData);
    if (response.code === 200) {
      await fetchNeighborhoods();
      showEditModal.value = false;
      ElMessage.success('小区更新成功');
    } else {
      ElMessage.error(response.msg || '更新小区失败');
    }
  } catch (error) {
    console.error('更新小区失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data || '更新小区失败，请稍后重试';
    ElMessage.error(errorMsg);
  }
};
// 新增小区
const addHandle = () => {
  resetAddForm();
  showAddModal.value = true;
};
const submitAdd = async () => {
  try {
    if (!addData.name.trim()) {
      ElMessage.warning('请输入小区名称');
      return;
    }

    const response = await addNeighborhood(addData);
    if (response.code === 200) {
      await fetchNeighborhoods();
      showAddModal.value = false;
      ElMessage.success('小区新增成功');
    } else {
      ElMessage.error(response.msg || '新增小区失败');
    }
  } catch (error) {
    console.error('新增小区失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data || '新增小区失败，请稍后重试';
    ElMessage.error(errorMsg);
  }
};
// 删除小区
const deleteHandle = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个小区吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const response = await deleteNeighborhood(id);
    if (response.code === 200) {
      await fetchNeighborhoods();
      ElMessage.success('小区删除成功');
    } else {
      ElMessage.error(response.msg || '删除小区失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除小区失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '删除小区失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};
// 批量删除小区
const batchDelete = async () => {
  try {
    if (!multipleSelection.value.length) {
      return ElMessage.warning('请先勾选要删除的小区');
    }

    await ElMessageBox.confirm(`确定要批量删除选中的 ${multipleSelection.value.length} 个小区吗？此操作不可恢复。`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const ids = multipleSelection.value.map(item => item.id);
    const response = await batchDeleteNeighborhood(ids);
    if (response.code === 200) {
      await fetchNeighborhoods();
      ElMessage.success('批量删除小区成功');
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
    } else {
      ElMessage.error(response.msg || '批量删除小区失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除小区失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '批量删除小区失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};
// 分页处理
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchNeighborhoods();
};
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchNeighborhoods();
};
// 在现有的数据定义后添加批量新增相关函数
const BatchAdd = () => {
  showBatchAddModal.value = true;
};
// 下载模板文件
const downloadTemplate = () => {
  // 创建工作簿
  const wb = XLSX.utils.book_new();
  // 创建工作表数据
  const data = [['小区名称']];
  // 创建工作表
  const ws = XLSX.utils.aoa_to_sheet(data);
  // 将工作表添加到工作簿
  XLSX.utils.book_append_sheet(wb, ws, '小区模板');
  // 生成Excel文件并下载
  XLSX.writeFile(wb, '小区批量导入模板.xlsx');
};
// 处理文件选择
const handleFileChange = (file) => {
  const rawFile = file.raw;
  if (!rawFile) {
    ElMessage.warning('文件为空，请重新上传');
    return;
  }

  // 检查文件类型
  const isExcel = /\.(xlsx|xls)$/i.test(rawFile.name);
  if (!isExcel) {
    ElMessage.warning('请上传Excel文件');
    fileList.value = [];
    return;
  }

  uploadedFile.value = rawFile;
};
// 移除文件
const handleFileRemove = () => {
  uploadedFile.value = null;
};
// 重置批量新增表单
const resetBatchAddForm = () => {
  uploadedFile.value = null;
  fileList.value = [];
  batchLoading.value = false;
};
// 提交批量新增
const submitBatchAdd = async () => {
  if (!uploadedFile.value) {
    ElMessage.warning('请先上传文件');
    return;
  }

  batchLoading.value = true;

  try {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', uploadedFile.value);

    // 调用批量新增API
    const response = await batchAddNeighborhood(formData);

    if (response.code === 200) {
      ElMessage.success(response.data || `批量新增小区成功`);
      showBatchAddModal.value = false;
      await fetchNeighborhoods();
    } else {
      ElMessage.error(response.msg || '批量新增小区失败');
    }
  } catch (error) {
    console.error('批量新增失败:', error);
    ElMessage.error('批量新增失败，请检查文件格式');
  } finally {
    batchLoading.value = false;
  }
};
// 在现有的响应式数据中添加
const exportLoading = ref(false);
// 添加导出功能函数
const exportToExcel = (data, filename) => {
  const worksheet = XLSX.utils.json_to_sheet(data);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, '小区数据');
  XLSX.writeFile(workbook, `${filename}.xlsx`);
};
// 格式化导出数据
const formatExportData = (neighborhoods) => {
    return neighborhoods.map(item => ({
      '小区名称': item.name,
      '创建人ID': item.createUserId,
      '创建时间': formatDate(item.createTime),
      '修改时间': formatDate(item.updateTime)
    }));
};
// 导出选中数据
const exportSelected = async () => {
  if (multipleSelection.value.length === 0) {
  ElMessage.warning('请先选择要导出的小区');
  return;
}

  try {
    const exportData = formatExportData(multipleSelection.value);
    exportToExcel(exportData, `选中小区数据_${new Date().getTime()}`);
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};
// 导出全部数据
const exportAll = async () => {
  exportLoading.value = true;

  try {
    const params = {
      pageNo: 1,
      pageSize: 10000,
      ...searchForm
    };
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });
    const response = await listNeighborhood(params);
    if (response.code === 200 && response.data) {
      const allData = response.data.records || [];
      if (allData.length === 0) {
        ElMessage.warning('没有数据可导出');
        return;
      }
      const exportData = formatExportData(allData);
      exportToExcel(exportData, `全部小区数据_${new Date().getTime()}`);
      ElMessage.success(`成功导出 ${allData.length} 条数据`);
    } else {
      ElMessage.error(response.msg || '获取数据失败');
    }
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  } finally {
    exportLoading.value = false;
  }
};
// 初始化
onMounted(() => {
fetchNeighborhoods();
});
</script>

<style scoped>
@import url('@/assets/StyleCss.css');

</style>