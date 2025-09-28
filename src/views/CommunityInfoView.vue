<template>
  <div class="content-area">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">社区管理</h2>
      </div>
      <el-button class="add-btn" type="primary" icon="Plus" @click="addHandle">
        新增社区
      </el-button>
    </div>
    <div style="padding:0 0 20px 0">
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
        <el-form-item label="社区名称">
          <el-input
              v-model="searchForm.communityName"
              placeholder="请输入社区名称"
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
          :data="communities.records"
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
        <el-table-column prop="communityName" label="社区名称" min-width="200" show-overflow-tooltip/>
        <el-table-column label="坐标" width="180">
          <template #default="scope">
            <span>{{ scope.row.longitude }}, {{ scope.row.latitude }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rangeRadius" label="范围半径(米)" width="120" align="center"/>
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
          :total="communities.total"
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
          title="确定要批量删除选中的社区吗？此操作不可恢复。"
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

    <!-- 编辑社区弹窗 -->
    <el-dialog
        v-model="showEditModal"
        :title="isEditing ? '编辑社区' : '新增社区'"
        width="1020px"
        @closed="resetEditForm"
        :close-on-click-modal="false"
    >
      <el-form
          :model="editData"
          :rules="rules"
          ref="editFormRef"
          label-width="100px"
          class="dialog-form"
      >
        <el-form-item label="社区名称" prop="communityName">
          <el-input
              v-model="editData.communityName"
              placeholder="请输入社区名称"
              maxlength="50"
              show-word-limit
          />
        </el-form-item>
        <el-form-item label="坐标位置" prop="location">
          <div class="map-selector">
            <el-input
                v-model="editData.address"
                placeholder="搜索地点或在地图上点击选择"
                style="margin-bottom: 10px;"
                @change="searchLocation"
            >
              <template #append>
                <el-button icon="Search" @click="searchLocation" />
              </template>
            </el-input>
            <div class="map-container" ref="mapContainer" style="height: 300px; width: 100%;"></div>
            <div class="coordinates-display" v-if="editData.longitude && editData.latitude">
              当前坐标: {{ editData.longitude.toFixed(6) }}, {{ editData.latitude.toFixed(6) }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="范围半径" prop="rangeRadius">
          <el-slider
              v-model="editData.rangeRadius"
              :min="100"
              :max="5000"
              :step="100"
              show-input
              show-input-controls="false"
              input-size="small"
              style="width: 100%"
              @change="onRadiusChange"
          />
          <span style="margin-left: 8px;">米</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditModal = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">{{ isEditing ? '保存' : '新增' }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, computed, nextTick, watch, onUnmounted} from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as XLSX from 'xlsx';
import {
  listCommunityInfo,
  addCommunityInfo,
  editCommunityInfo,
  deleteCommunityInfo,
  batchDeleteCommunityInfo
} from "@/api/auth.js";

const router = useRouter();

// 响应式数据
const communities = reactive({
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
const multipleTableRef = ref();
const multipleSelection = ref([]);
const searchForm = reactive({
  communityName: ''
});

// 使用更清晰的数据结构
const editData = reactive({
  id: '',
  communityName: '',
  longitude: null,
  latitude: null,
  rangeRadius: 1000,
  address: ''
});

const isEditing = ref(false);
const mapContainer = ref(null);
let map = null;
let marker = null;
let circle = null;
let circleEditor = null;
let geocoder = null; // 地理编码器实例缓存

// 添加防抖搜索
let searchTimeout = null;

// 表单验证规则
const rules = {
  communityName: [
    { required: true, message: '请输入社区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '社区名称长度在2到50个字符之间', trigger: 'blur' }
  ],
  location: [
    { validator: validateLocation, trigger: 'change' }
  ],
  rangeRadius: [
    { required: true, message: '请输入范围半径', trigger: 'blur' },
    { type: 'number', min: 100, max: 5000, message: '范围半径应在100-5000米之间', trigger: 'blur' }
  ]
};

// 自定义坐标验证器
function validateLocation(rule, value, callback) {
  if (!editData.longitude || !editData.latitude) {
    callback(new Error('请选择坐标位置'));
  } else {
    callback();
  }
}

// 计算属性
const emptyText = computed(() => {
  return searchForm.communityName
      ? '暂无符合条件的社区'
      : '暂无社区数据';
});

// 监听搜索条件变化，添加防抖
watch(() => searchForm.communityName, (newVal) => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    if (newVal !== undefined) {
      handleSearch();
    }
  }, 500);
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

// 高德地图API加载优化
const loadAMap = () => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve(window.AMap);
      return;
    }

    // 检查是否正在加载
    if (window.AMapLoading) {
      const checkInterval = setInterval(() => {
        if (window.AMap) {
          clearInterval(checkInterval);
          resolve(window.AMap);
        }
      }, 100);
      return;
    }

    window.AMapLoading = true;
    const script = document.createElement('script');
    script.type = 'text/javascript';
    window._AMapSecurityConfig = {securityJsCode: 'da9e1c3577372b9802105cdfb9541c4c'}
    script.src = `https://webapi.amap.com/maps?v=2.0&key=933f25592f79bf8be4ee7ef15cd8ca90&plugin=AMap.Geocoder,AMap.PlaceSearch,AMap.CircleEditor`;
    script.onload = () => {
      window.AMapLoading = false;
      setTimeout(() => {
        resolve(window.AMap);
      }, 500);
    };
    script.onerror = (e) => {
      window.AMapLoading = false;
      reject(e);
    };
    document.head.appendChild(script);
  });
};

// 初始化地图 - 优化性能
const initMap = async () => {
  try {
    await loadAMap();

    if (!window.AMap) {
      ElMessage.error('地图加载失败，请刷新页面重试');
      return;
    }

    // 销毁旧地图实例
    if (map) {
      map.destroy();
      map = null;
    }

    // 创建新地图实例
    const center = editData.longitude && editData.latitude ?
        [editData.longitude, editData.latitude] :
        [116.397428, 39.90923]; // 默认中心点（北京）

    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: center,
      resizeEnable: true,
      viewMode: '2D', // 强制使用2D模式，性能更好
      zoomEnable: true,
      dragEnable: true
    });

    // 初始化地理编码器
    geocoder = new AMap.Geocoder();

    // 添加地图点击事件
    map.on('click', handleMapClick);

    // 如果已有坐标，则显示标记和范围
    if (editData.longitude && editData.latitude) {
      addMarker([editData.longitude, editData.latitude]);
      updateCircle([editData.longitude, editData.latitude], editData.rangeRadius);
    }
  } catch (error) {
    console.error('地图初始化失败:', error);
    ElMessage.error('地图初始化失败: ' + error.message);
  }
};

// 地图点击处理
const handleMapClick = (e) => {
  const { lng, lat } = e.lnglat;
  editData.longitude = lng;
  editData.latitude = lat;

  // 更新标记位置
  addMarker([lng, lat]);

  // 更新圆形范围
  updateCircle([lng, lat], editData.rangeRadius);

  // 逆地理编码获取地址
  getAddressFromCoordinates(lng, lat);
};

// 添加标记点
const addMarker = (position) => {
  if (marker) {
    marker.setPosition(position);
  } else {
    marker = new AMap.Marker({
      position: position,
      map: map
    });
  }
};

// 更新范围圆 - 优化性能
const updateCircle = (center, radius) => {
  // 清除旧的圆形范围
  if (circle) {
    if (circleEditor) {
      circleEditor.close();
    }
    circle.setMap(null);
    circle = null;
    circleEditor = null;
  }

  // 创建新的圆形范围
  circle = new AMap.Circle({
    center: center,
    radius: radius,
    fillOpacity: 0.3,
    fillColor: '#1890FF',
    strokeColor: '#1890FF',
    strokeWeight: 1,
    strokeOpacity: 0.8,
    map: map
  });

  // 创建圆形编辑器
  circleEditor = new AMap.CircleEditor(map, circle);

  // 监听圆形编辑事件
  circleEditor.on('move', (e) => {
    const center = circle.getCenter();
    editData.longitude = center.lng;
    editData.latitude = center.lat;
    addMarker(center);
  });

  circleEditor.on('adjust', (e) => {
    const radius = circle.getRadius();
    editData.rangeRadius = Math.round(radius);
  });

  // 开启编辑
  circleEditor.open();
};

// 半径变化处理
const onRadiusChange = (val) => {
  if (circle && editData.longitude && editData.latitude) {
    circle.setRadius(val);
  }
};

// 根据坐标获取地址
const getAddressFromCoordinates = (lng, lat) => {
  if (!geocoder) return;

  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.regeocode) {
      editData.address = result.regeocode.formattedAddress;
    } else {
      editData.address = '未知地址';
    }
  });
};

// 搜索地点 - 添加防抖和取消功能
let placeSearch = null;
let currentSearchRequest = null;

const searchLocation = () => {
  console.log('开始搜索位置:', editData.address);
  if (!editData.address || !map) {
    console.log('搜索条件不满足: address =', editData.address, 'map =', map);
    return;
  }

  if (!window.AMap) {
    console.log('地图API未加载完成');
    ElMessage.error('地图服务未加载完成，请稍后重试');
    return;
  }

  // 取消之前的搜索请求
  if (currentSearchRequest) {
    console.log('取消之前的搜索请求');
    currentSearchRequest.cancel && currentSearchRequest.cancel();
  }

  // 使用高德地点搜索服务
  if (!placeSearch) {
    console.log('初始化地点搜索服务');
    AMap.plugin('AMap.PlaceSearch', () => {
      placeSearch = new AMap.PlaceSearch({
        map: map,
        pageSize: 5 // 限制结果数量
      });
      console.log('地点搜索服务初始化完成:', placeSearch);
      doSearch();
    });
  } else {
    console.log('使用已有的地点搜索服务');
    doSearch();
  }
};

const doSearch = () => {
  console.log('执行搜索:', editData.address);
  placeSearch.search(editData.address, (status, result) => {
    console.log('搜索回调结果: status =', status, 'result =', result);
    if (status === 'complete' && result.poiList.pois.length > 0) {
      const poi = result.poiList.pois[0];
      const { lng, lat } = poi.location;
      console.log('搜索到地点:', poi.name, '坐标:', lng, lat);

      editData.longitude = lng;
      editData.latitude = lat;

      // 移动地图中心
      map.setCenter([lng, lat]);
      console.log('设置地图中心点:', [lng, lat]);

      // 更新标记
      addMarker([lng, lat]);
      console.log('添加标记点:', [lng, lat]);

      // 更新圆形范围
      updateCircle([lng, lat], editData.rangeRadius);
      console.log('更新圆形范围: center =', [lng, lat], 'radius =', editData.rangeRadius);

      // 逆地理编码获取地址
      getAddressFromCoordinates(lng, lat);
      console.log('执行逆地理编码获取地址');
    } else {
      console.log('未找到相关地点，status:', status, 'pois数量:', result.poiList ? result.poiList.pois.length : '无结果');
      ElMessage.warning('未找到相关地点');
    }
  });
};

// 数据获取 - 添加缓存和错误处理
const fetchCommunities = async () => {
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

    const response = await listCommunityInfo(params);
    if (response.code === 200 && response.data) {
      const data = response.data;
      Object.assign(communities, {
        records: data.records || [],
        total: data.total || 0,
        pages: Math.ceil(data.total / pageSize.value) || 0,
        current: currentPage.value,
        size: pageSize.value
      });
    } else {
      ElMessage.error(response.msg || '获取社区数据失败');
    }
  } catch (error) {
    console.error('获取社区数据失败:', error);
    ElMessage.error('获取社区数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
  fetchCommunities();
};

const resetSearch = () => {
  searchForm.communityName = '';
  currentPage.value = 1;
  fetchCommunities();
};

// 复选框勾选
const handleSelectionChange = (val) => {
  multipleSelection.value = val;
};

// 重置编辑表单 - 优化资源清理
const resetEditForm = () => {
  Object.assign(editData, {
    id: '',
    communityName: '',
    longitude: null,
    latitude: null,
    rangeRadius: 1000,
    address: ''
  });

  // 清除地图资源
  cleanupMapResources();

  isEditing.value = false;
};

// 清理地图资源
const cleanupMapResources = () => {
  if (marker) {
    marker.setMap(null);
    marker = null;
  }

  if (circle) {
    if (circleEditor) {
      circleEditor.close();
    }
    circle.setMap(null);
    circle = null;
    circleEditor = null;
  }

  if (map) {
    map.off('click', handleMapClick);
    // 不销毁地图实例，以便复用
  }
};

// 编辑社区
const editHandle = (community) => {
  Object.assign(editData, {
    id: community.id,
    communityName: community.communityName,
    longitude: community.longitude,
    latitude: community.latitude,
    rangeRadius: community.rangeRadius,
    address: ''
  });

  isEditing.value = true;
  showEditModal.value = true;

  // 初始化地图并定位到已有坐标
  nextTick(() => {
    initMap();
  });
};

// 提交编辑 - 添加表单验证
const editFormRef = ref();
const submitEdit = async () => {
  try {
    // 表单验证
    if (!await validateForm()) return;

    let response;
    if (isEditing.value) {
      response = await editCommunityInfo(editData);
    } else {
      response = await addCommunityInfo(editData);
    }

    if (response.code === 200) {
      await fetchCommunities();
      showEditModal.value = false;
      ElMessage.success(isEditing.value ? '社区更新成功' : '社区新增成功');
    } else {
      ElMessage.error(response.msg || (isEditing.value ? '更新社区失败' : '新增社区失败'));
    }
  } catch (error) {
    console.error('操作社区失败:', error);
    const errorMsg = error.response?.data?.msg || error.response?.data ||
        (isEditing.value ? '更新社区失败，请稍后重试' : '新增社区失败，请稍后重试');
    ElMessage.error(errorMsg);
  }
};

// 表单验证
const validateForm = async () => {
  if (!editFormRef.value) return false;

  try {
    await editFormRef.value.validate();
    return true;
  } catch (error) {
    ElMessage.warning('请完善表单信息');
    return false;
  }
};

// 新增社区
const addHandle = () => {
  resetEditForm();
  showEditModal.value = true;

  // 初始化地图
  nextTick(() => {
    initMap();
  });
};

// 删除社区 - 添加确认和加载状态
const deleteHandle = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个社区吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const response = await deleteCommunityInfo(id);
    if (response.code === 200) {
      await fetchCommunities();
      ElMessage.success('社区删除成功');
    } else {
      ElMessage.error(response.msg || '删除社区失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除社区失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '删除社区失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 批量删除社区
const batchDelete = async () => {
  try {
    if (!multipleSelection.value.length) {
      return ElMessage.warning('请先勾选要删除的社区');
    }

    await ElMessageBox.confirm(`确定要批量删除选中的 ${multipleSelection.value.length} 个社区吗？此操作不可恢复。`, '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    });

    const ids = multipleSelection.value.map(item => item.id);
    const response = await batchDeleteCommunityInfo(ids);
    if (response.code === 200) {
      await fetchCommunities();
      ElMessage.success('批量删除社区成功');
      multipleSelection.value = [];
      multipleTableRef.value.clearSelection();
    } else {
      ElMessage.error(response.msg || '批量删除社区失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除社区失败:', error);
      const errorMsg = error.response?.data?.msg || error.response?.data || '批量删除社区失败，请稍后重试';
      ElMessage.error(errorMsg);
    }
  }
};

// 分页处理
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchCommunities();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  fetchCommunities();
};

// 导出功能优化
const exportToExcel = (data, filename) => {
  try {
    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, '社区数据');
    XLSX.writeFile(workbook, `${filename}.xlsx`);
  } catch (error) {
    console.error('Excel导出错误:', error);
    throw new Error('导出文件失败');
  }
};

// 格式化导出数据
const formatExportData = (communities) => {
  return communities.map(item => ({
    '社区名称': item.communityName,
    '经度': item.longitude,
    '纬度': item.latitude,
    '范围半径(米)': item.rangeRadius,
    '创建人ID': item.createUserId,
    '创建时间': formatDate(item.createTime),
    '修改时间': formatDate(item.updateTime)
  }));
};

// 导出选中数据
const exportSelected = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择要导出的社区');
    return;
  }

  try {
    const exportData = formatExportData(multipleSelection.value);
    exportToExcel(exportData, `选中社区数据_${new Date().getTime()}`);
    ElMessage.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};

// 导出全部数据 - 添加进度提示
const exportAll = async () => {
  const loading = ElMessage.loading('正在导出数据...', 0);

  try {
    const params = {
      pageNo: 1,
      pageSize: 10000,
      ...searchForm
    };

    // 清理空参数
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === undefined || params[key] === '') {
        delete params[key];
      }
    });

    const response = await listCommunityInfo(params);
    if (response.code === 200 && response.data) {
      const allData = response.data.records || [];
      if (allData.length === 0) {
        ElMessage.warning('没有数据可导出');
        return;
      }

      const exportData = formatExportData(allData);
      exportToExcel(exportData, `全部社区数据_${new Date().getTime()}`);
      ElMessage.success(`成功导出 ${allData.length} 条数据`);
    } else {
      ElMessage.error(response.msg || '获取数据失败');
    }
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  } finally {
    loading.close();
  }
};

// 组件卸载时清理资源
onUnmounted(() => {
  cleanupMapResources();
  if (searchTimeout) clearTimeout(searchTimeout);
});

// 初始化
onMounted(() => {
  fetchCommunities();
});
</script>

<style scoped>
@import url('@/assets/StyleCss.css');

</style>