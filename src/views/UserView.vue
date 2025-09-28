<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Loading, Picture, Plus} from '@element-plus/icons-vue';
import {batchDeleteUser, deleteUser, listUser, updateUser} from "@/api/auth.js";

const router = useRouter();

// 响应式数据
const users = reactive({
  records: [],
  total: 0,
  pages: 0,
  current: 1,
  size: 5
});

const currentPage = ref(1);
const pageSize = ref(5);
const loading = ref(false);
const showEditUserModal = ref(false);
const multipleTableRef = ref();
const multipleSelection = ref([]);
const avatarUploading = ref(false);

const searchForm = reactive({
  username: '',
  name: '',
  email: ''
});

const editUserData = reactive({
  id: '',
  username: '',
  name: '',
  mobile: '',
  mail: '',
  avatar: '',
  roles: '',
  status: '',
  communityName: '',
  fontBigFlag: ''
});

// 计算属性
const emptyText = computed(() => {
  return searchForm.username || searchForm.name || searchForm.email
      ? '暂无符合条件的用户'
      : '暂无用户数据';
});

// 角色选项
const roleOptions = ref([
  {label: '管理员', value: 1},
  {label: '普通用户', value: 2}
]);

// 状态选项
const statusOptions = ref([
  {label: '启用', value: 1},
  {label: '停用', value: 0}
]);

// 字体选项
const fontOptions = ref([
  {label: '是', value: 1},
  {label: '否', value: 0}
]);

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

const getImageUrl = (url) => {
  if (!url) return '';

  // 如果已经是完整的data URL，直接返回
  if (url.startsWith('data:image')) {
    return url;
  }

  // 如果是纯Base64（没有data:image前缀），添加前缀
  if (url.startsWith('/9j/') || url.startsWith('iVBORw') ||
      (url.length > 100 && !url.includes(' '))) {
    // 尝试判断图片类型
    let mimeType = 'image/jpeg';
    if (url.startsWith('iVBORw')) {
      mimeType = 'image/png';
    } else if (url.startsWith('R0lGOD') || url.startsWith('/9j/')) {
      mimeType = 'image/jpeg';
    }
    return `data:${mimeType};base64,${url}`;
  }

  // 如果是网络URL
  return `${url}?t=${new Date().getTime()}`;
};

const goHome = () => {
  router.push('/home');
};

// 头像上传处理
const handleAvatarChange = (file) => {
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

  avatarUploading.value = true;

  const reader = new FileReader();
  reader.onload = (e) => {
    const base64Data = e.target.result;
    editUserData.avatar = base64Data.split(',')[1];
    avatarUploading.value = false;
    ElMessage.success('头像上传成功');
  };
  reader.onerror = () => {
    avatarUploading.value = false;
    ElMessage.error('头像上传失败');
  };
  reader.readAsDataURL(rawFile);
};

// 移除头像
const removeAvatar = () => {
  editUserData.avatar = '';
  ElMessage.info('已移除头像');
};

// 数据获取
const fetchUsers = async () => {
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

    const response = await listUser(params);
    if (response.code === 200 && response.data) {
      const data = response.data;
      Object.assign(users, {
        records: data.records || [],
        total: data.total || 0,
        pages: Math.ceil(data.total / pageSize.value) || 0,
        current: currentPage.value,
        size: pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取用户数据失败');
    }
  } catch (error) {
    console.error('获取用户数据失败:', error);
    ElMessage.error('获取用户数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchUsers();
};

const resetSearch = () => {
  searchForm.username = '';
  searchForm.name = '';
  searchForm.email = '';
  currentPage.value = 1;
  fetchUsers();
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 重置编辑表单
const resetEditUser = () => {
  Object.assign(editUserData, {
    id: '',
    username: '',
    name: '',
    mobile: '',
    mail: '',
    avatar: '',
    roles: '',
    status: '',
    communityName: '',
    fontBigFlag: ''
  });
  avatarUploading.value = false;
};

// 编辑用户
const editUserHandle = (user) => {
  Object.assign(editUserData, {
    id: user.id,
    username: user.username,
    name: user.name,
    mobile: user.mobile,
    mail: user.mail,
    avatar: user.avatar,
    roles: user.roles,
    status: user.status,
    communityName: user.communityName,
    fontBigFlag: user.fontBigFlag
  });
  showEditUserModal.value = true;
};

const submitEditUser = async () => {
  try {
    const response = await updateUser(editUserData);
    if (response.code === 200) {
      await fetchUsers();
      showEditUserModal.value = false;
      ElMessage.success('用户更新成功');
    } else {
      ElMessage.error(response.msg || '更新用户失败');
    }
  } catch (error) {
    console.error('更新用户失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data || '更新用户失败，请稍后重试';
    ElMessage.error(errorMsg);
  }
};

// 删除用户
const deleteUserHandle = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const response = await deleteUser(id);
    if (response.code === 200) {
      await fetchUsers();
      ElMessage.success('用户删除成功');
    } else {
      ElMessage.error(response.msg || '删除用户失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '删除用户失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 批量删除用户
const batchDeleteUsers = async () => {
  try {
    if (!multipleSelection.value.length) {
      return ElMessage.warning('请先勾选要删除的用户');
    }

    await ElMessageBox.confirm(`确定要批量删除选中的 ${multipleSelection.value.length} 个用户吗？此操作不可恢复。`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const ids = multipleSelection.value.map(user => user.id);
    const response = await batchDeleteUser(ids);
    if (response.code === 200) {
      await fetchUsers();
      ElMessage.success('批量删除用户成功');
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
    } else {
      ElMessage.error(response.msg || '批量删除用户失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除用户失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '批量删除用户失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 分页处理
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchUsers();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchUsers();
};

// 初始化
onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">用户管理</h2>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="账号">
          <el-input
              v-model="searchForm.username"
              placeholder="请输入账号"
              clearable
              style="width: 120px"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
              v-model="searchForm.name"
              placeholder="请输入姓名"
              clearable
              style="width: 120px"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
              v-model="searchForm.email"
              placeholder="请输入邮箱"
              clearable
              style="width: 150px"
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
          :data="users.records"
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
        <el-table-column prop="username" label="账号" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip/>
        <el-table-column prop="mobile" label="手机号" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="mail" label="邮箱" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="avatar" label="头像" min-width="100">
          <template #default="scope">
            <div class="image-container">
              <el-image
                  v-if="scope.row.avatar"
                  :src="getImageUrl(scope.row.avatar)"
                  class="table-image"
                  :preview-src-list="[getImageUrl(scope.row.avatar)]"
                  fit="cover"
                  preview-teleported
                  :zoom-rate="1.2"
                  :max-scale="7"
                  :min-scale="0.2"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon>
                      <Picture/>
                    </el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
                <template #placeholder>
                  <div class="image-loading">
                    <el-icon class="is-loading">
                      <Loading/>
                    </el-icon>
                    <span>加载中...</span>
                  </div>
                </template>
              </el-image>
              <div v-else class="no-image">
                <el-icon>
                  <Picture/>
                </el-icon>
                <span>无头像</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.roles === 1 ? 'danger' : 'primary'">
              {{ scope.row.roles === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="社区名称" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="fontBigFlag" label="长辈版字体" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.fontBigFlag === 1 ? 'warning' : 'info'">
              {{ scope.row.fontBigFlag === 1 ? '是' : '否' }}
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
                  @click="editUserHandle(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="deleteUserHandle(scope.row.id)"
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
          :total="users.total"
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
          title="确定要批量删除选中的用户吗？此操作不可恢复。"
          @confirm="batchDeleteUsers"
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

    <!-- 编辑用户弹窗 -->
    <el-dialog
        v-model="showEditUserModal"
        title="编辑用户"
        width="700px"
        @closed="resetEditUser"
        :close-on-click-modal="false"
    >
      <el-form
          :model="editUserData"
          label-width="100px"
          class="dialog-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="账号">
              <el-input v-model="editUserData.username" disabled/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="editUserData.name" placeholder="请输入姓名"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="editUserData.mobile" placeholder="请输入手机号"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="editUserData.mail" placeholder="请输入邮箱"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="editUserData.roles" placeholder="请选择角色" style="width: 100%">
                <el-option
                    v-for="item in roleOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="editUserData.status" placeholder="请选择状态" style="width: 100%">
                <el-option
                    v-for="item in statusOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="社区名称">
              <el-input v-model="editUserData.communityName" placeholder="请输入社区名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="长辈版字体">
              <el-select v-model="editUserData.fontBigFlag" placeholder="请选择" style="width: 100%">
                <el-option
                    v-for="item in fontOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="头像">
          <div class="avatar-upload-container">
            <el-upload
                class="avatar-uploader"
                accept="image/jpg,image/jpeg,image/png"
                action="#"
                :on-change="handleAvatarChange"
                :show-file-list="false"
                :auto-upload="false"
                :disabled="avatarUploading"
            >
              <div v-if="editUserData.avatar" class="avatar-preview">
                <el-image
                    :src="getImageUrl(editUserData.avatar)"
                    class="avatar"
                    fit="cover"
                />
                <div class="avatar-actions">
                  <el-button
                      size="small"
                      type="danger"
                      icon="Delete"
                      @click.stop="removeAvatar"
                      :disabled="avatarUploading"
                  >
                    移除
                  </el-button>
                </div>
              </div>
              <div v-else class="avatar-upload-default">
                <el-icon v-if="!avatarUploading"><Plus /></el-icon>
                <el-icon v-else class="is-loading"><Loading /></el-icon>
                <span>{{ avatarUploading ? '上传中...' : '上传头像' }}</span>
              </div>
            </el-upload>
            <div class="upload-tips">支持 JPG、PNG 格式，大小不超过 10MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditUserModal = false">取消</el-button>
          <el-button type="primary" @click="submitEditUser" :loading="avatarUploading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import url('@/assets/StyleCss.css');
</style>