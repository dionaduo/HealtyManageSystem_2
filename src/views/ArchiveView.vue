<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as archiveApi from '@/api/auth.js'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const addLoading = ref(false)
const editLoading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)
const showImportModal = ref(false)
const multipleSelection = ref([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  name: '',
  mobile: '',
  allergicDrugs: ''
})

// 档案数据
const archiveList = ref([])
const currentArchive = ref({})
const editArchiveData = ref({})

// 表单引用
const addFormRef = ref()
const editFormRef = ref()

// 枚举选项
const sexOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const residentialStatusOptions = [
  { label: '独居', value: 1 },
  { label: '子女合住', value: 2 },
  { label: '配偶合住', value: 3 },
  { label: '其它', value: 4 }
]

const selfCareSituationOptions = [
  { label: '能自理', value: 1 },
  { label: '需看护', value: 2 }
]

const medicalInsuranceOptions = [
  { label: '有', value: 1 },
  { label: '无', value: 0 }
]

const maritalStatusOptions = [
  { label: '未婚', value: 1 },
  { label: '已婚', value: 2 },
  { label: '离异', value: 3 },
  { label: '丧偶', value: 4 }
]

// 计算属性
const emptyText = computed(() => {
  return searchForm.name || searchForm.mobile || searchForm.allergicDrugs
      ? '暂无符合条件的档案'
      : '暂无档案数据'
})

// 方法
// 获取档案列表
const fetchArchives = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }

    const response = await archiveApi.getArchivePage(params)
    console.log('获取档案列表成功:', response)
    if (response.code === 200) {
      archiveList.value = response.data.records || []
      console.log('档案列表:', archiveList.value)
      total.value = response.data.total || 0
      console.log('档案总数:', total.value)
    } else {
      ElMessage.error(response.msg || '获取档案列表失败')
    }
  } catch (error) {
    console.error('获取档案列表失败:', error)
    ElMessage.error('获取档案列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchArchives()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.mobile = ''
  searchForm.allergicDrugs = ''
  currentPage.value = 1
  fetchArchives()
}

// 添加档案
const handleAdd = () => {
  currentArchive.value = {
    name: '',
    sex: 1,
    age: null,
    idCard: '',
    mobile: '',
    neighborhood: '',
    address: '',
    residentialStatus: null,
    selfCareSituation: null,
    allergicDrugs: '',
    pastMedicalHistory: '',
    medicalInsurance: null,
    maritalStatus: null
  }
  showAddModal.value = true
  nextTick(() => {
    addFormRef.value?.clearValidate()
  })
}

// 提交添加
const submitAdd = async () => {
  if (!addFormRef.value) return

  try {
    const valid = await addFormRef.value.validate()
    if (!valid) return

    addLoading.value = true
    const response = await archiveApi.addArchive(currentArchive.value)
    if (response.code === 200) {
      ElMessage.success('添加成功')
      showAddModal.value = false
      fetchArchives()
    } else {
      ElMessage.error(response.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加档案失败:', error)
    ElMessage.error('添加失败')
  } finally {
    addLoading.value = false
  }
}

// 编辑档案
const handleEdit = (row) => {
  editArchiveData.value = { ...row }
  showEditModal.value = true
  nextTick(() => {
    editFormRef.value?.clearValidate()
  })
}

// 提交编辑
const submitEdit = async () => {
  if (!editFormRef.value) return

  try {
    const valid = await editFormRef.value.validate()
    if (!valid) return

    editLoading.value = true
    const response = await archiveApi.updateArchive(editArchiveData.value)
    if (response.code === 200) {
      ElMessage.success('修改成功')
      showEditModal.value = false
      await fetchArchives()
    } else {
      ElMessage.error(response.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改档案失败:', error)
    ElMessage.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

// 删除档案
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
        '是否确定删除该档案信息？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const response = await archiveApi.deleteArchive(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchArchives()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除档案失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的档案')
    return
  }

  try {
    await ElMessageBox.confirm(
        `确定要删除选中的 ${multipleSelection.value.length} 条档案吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const ids = multipleSelection.value.map(item => item.id)
    // 这里需要后端支持批量删除接口，暂时使用循环删除
    for (const id of ids) {
      await archiveApi.deleteArchive(id)
    }

    ElMessage.success('批量删除成功')
    multipleSelection.value = []
    fetchArchives()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 导入档案
const handleImport = () => {
  showImportModal.value = true
}

// 下载导入模板
const downloadTemplate = async () => {
  try {
    const response = await archiveApi.downloadTemplate()

    // 创建blob对象并下载
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '档案导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

// 处理文件上传
const handleFileUpload = async (file) => {
  try {
    const formData = new FormData()
    formData.append('file', file)

    // 检查文件类型
    const allowedTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel']
    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('请上传Excel文件（.xlsx 或 .xls格式）')
      return false
    }

    // 检查文件大小（10MB限制）
    const maxSize = 10 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过10MB')
      return false
    }

    const response = await archiveApi.importArchive(formData)
    if (response.code === 200) {
      ElMessage.success('导入成功')
      showImportModal.value = false
      await fetchArchives()
    } else {
      ElMessage.error(response.msg || '导入失败')
    }

    return false // 阻止默认上传行为
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败：' + (error.message || '请检查文件格式是否正确'))
    return false
  }
}

// 导出档案
const handleExport = async () => {
  try {
    const response = await archiveApi.exportArchive(searchForm)

    // 创建blob对象并下载
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url

    // 生成文件名，包含当前日期
    const now = new Date()
    const dateStr = `${now.getFullYear()}${(now.getMonth() + 1).toString().padStart(2, '0')}${now.getDate().toString().padStart(2, '0')}`
    link.download = `档案数据导出_${dateStr}.xlsx`

    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 分页处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchArchives()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchArchives()
}

// 表格选择
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

// 关闭弹窗
const closeAddModal = () => {
  showAddModal.value = false
}

const closeEditModal = () => {
  showEditModal.value = false
}

// 身份证号验证规则
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

// 手机号验证规则
const validateMobile = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入联系电话'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 表单验证规则
const formRules = {
  name: [{required: true, message: '请输入姓名', trigger: 'blur'}],
  age: [{required: true, message: '请输入年龄', trigger: 'blur'}],
  idCard: [
    {required: true, message: '请输入身份证号', trigger: 'blur'},
    {validator: validateIdCard, trigger: 'blur'}
  ],
  mobile: [
    {required: true, message: '请输入联系电话', trigger: 'blur'},
    {validator: validateMobile, trigger: 'blur'}
  ],
  neighborhood: [{required: true, message: '请输入居住小区', trigger: 'blur'}],
  address: [{required: true, message: '请输入居住地址', trigger: 'blur'}],
  allergicDrugs: [{required: true, message: '请输入过敏药物', trigger: 'blur'}],
  pastMedicalHistory: [{required: true, message: '请输入既往病史', trigger: 'blur'}],
  sex: [{required: true, message: '请选择性别', trigger: 'change'}],
  residentialStatus: [{required: true, message: '请选择居住状态', trigger: 'change'}],
  medicalInsurance: [{required: true, message: '请选择医疗保险', trigger: 'change'}],
  maritalStatus: [{required: true, message: '请选择婚姻状况', trigger: 'change'}]
}
const goHome = () => {
  router.push('/home');
};

// 初始化
onMounted(() => {
  fetchArchives()
})
</script>

<template>
  <div class="content-area">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <el-button class="return-btn" icon="ArrowLeft" @click="goHome">
          返回首页
        </el-button>
        <h2 class="page-title">档案管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="add-btn" icon="Plus" @click="handleAdd">
          添加档案
        </el-button>
        <el-button type="success" icon="Upload" @click="handleImport">
          批量导入
        </el-button>
        <el-button type="warning" icon="Download" @click="handleExport">
          批量导出
        </el-button>
      </div>
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
        <el-form-item label="联系电话">
          <el-input
              v-model="searchForm.mobile"
              placeholder="请输入联系电话"
              clearable
              style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="过敏药物">
          <el-input
              v-model="searchForm.allergicDrugs"
              placeholder="请输入过敏药物"
              clearable
              style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch" class="btn-hover-animate">
            查询
          </el-button>
          <el-button icon="Refresh" @click="resetSearch" class="btn-hover-animate">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card card-hover-animate" shadow="never">
      <el-table
          :data="archiveList"
          class="data-table"
          style="width: 100%"
          v-loading="loading"
          :empty-text="emptyText"
          stripe
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center"/>
        <el-table-column
            label="序号"
            width="80"
            type="index"
            :index="(index) => (currentPage - 1) * pageSize + index + 1"
            align="center"
        />
        <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip/>
        <el-table-column prop="sexName" label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.sex === 1 ? 'primary' : 'danger'">
              {{ scope.row.sexName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center"/>
        <el-table-column prop="idCard" label="身份证号" min-width="180" show-overflow-tooltip/>
        <el-table-column prop="neighborhood" label="居住小区" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="address" label="居住地址" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="residentialStatusName" label="居住状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.residentialStatus ? 'primary' : 'info'">
              {{ scope.row.residentialStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="selfCareSituationName" label="自理情况" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.selfCareSituation === 1 ? 'success' : 'warning'">
              {{ scope.row.selfCareSituationName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="联系电话" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="allergicDrugs" label="过敏药物" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="pastMedicalHistory" label="既往病史" min-width="120" show-overflow-tooltip/>
        <el-table-column prop="medicalInsuranceName" label="医疗保险" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.medicalInsurance === 1 ? 'success' : 'info'">
              {{ scope.row.medicalInsuranceName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maritalStatusName" label="婚姻状况" width="100" align="center">
          <template #default="scope">
            <el-tag>
              {{ scope.row.maritalStatusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  size="small"
                  type="primary"
                  link
                  icon="Edit"
                  @click="handleEdit(scope.row)"
              >
                编辑
              </el-button>
              <el-button
                  size="small"
                  type="danger"
                  link
                  icon="Delete"
                  @click="handleDelete(scope.row.id)"
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
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
          background
      />
    </div>

    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="multipleSelection.length > 0">
      <el-button type="danger" icon="Delete" @click="handleBatchDelete" class="btn-hover-animate">
        批量删除 ({{ multipleSelection.length }})
      </el-button>
    </div>

    <!-- 添加档案弹窗 -->
    <el-dialog
        v-model="showAddModal"
        title="添加档案"
        width="800px"
        :close-on-click-modal="false"
        class="page-enter-active"
    >
      <el-form
          ref="addFormRef"
          :model="currentArchive"
          label-width="100px"
          :rules="formRules"
      >
        <div class="dialog-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="currentArchive.name" placeholder="请输入姓名"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="sex">
                <el-select v-model="currentArchive.sex" placeholder="请选择性别" style="width: 100%">
                  <el-option
                      v-for="item in sexOptions"
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
              <el-form-item label="年龄" prop="age">
                <el-input-number
                    v-model="currentArchive.age"
                    :min="0"
                    :max="150"
                    placeholder="请输入年龄"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="currentArchive.idCard" placeholder="请输入身份证号" maxlength="18"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="mobile">
                <el-input v-model="currentArchive.mobile" placeholder="请输入联系电话" maxlength="11"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="婚姻状况" prop="maritalStatus">
                <el-select v-model="currentArchive.maritalStatus" placeholder="请选择婚姻状况" style="width: 100%">
                  <el-option
                      v-for="item in maritalStatusOptions"
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
              <el-form-item label="居住小区" prop="neighborhood">
                <el-input v-model="currentArchive.neighborhood" placeholder="请输入居住小区"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="居住地址" prop="address">
                <el-input v-model="currentArchive.address" placeholder="请输入居住地址"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="居住状态" prop="residentialStatus">
                <el-select v-model="currentArchive.residentialStatus" placeholder="请选择居住状态" style="width: 100%">
                  <el-option
                      v-for="item in residentialStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="自理情况" prop="selfCareSituation">
                <el-select v-model="currentArchive.selfCareSituation" placeholder="请选择自理情况" style="width: 100%">
                  <el-option
                      v-for="item in selfCareSituationOptions"
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
              <el-form-item label="医疗保险" prop="medicalInsurance">
                <el-select v-model="currentArchive.medicalInsurance" placeholder="请选择医疗保险" style="width: 100%">
                  <el-option
                      v-for="item in medicalInsuranceOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="过敏药物" prop="allergicDrugs">
            <el-input
                v-model="currentArchive.allergicDrugs"
                type="textarea"
                :rows="2"
                placeholder="请输入过敏药物"
            />
          </el-form-item>

          <el-form-item label="既往病史" prop="pastMedicalHistory">
            <el-input
                v-model="currentArchive.pastMedicalHistory"
                type="textarea"
                :rows="3"
                placeholder="请输入既往病史"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeAddModal" :disabled="addLoading">取消</el-button>
          <el-button type="primary" @click="submitAdd" :loading="addLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑档案弹窗 -->
    <el-dialog
        v-model="showEditModal"
        title="编辑档案"
        width="800px"
        :close-on-click-modal="false"
        class="page-enter-active"
    >
      <el-form
          ref="editFormRef"
          :model="editArchiveData"
          label-width="100px"
          :rules="formRules"
      >
        <div class="dialog-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="editArchiveData.name" placeholder="请输入姓名"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="sex">
                <el-select v-model="editArchiveData.sex" placeholder="请选择性别" style="width: 100%">
                  <el-option
                      v-for="item in sexOptions"
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
              <el-form-item label="年龄" prop="age">
                <el-input-number
                    v-model="editArchiveData.age"
                    :min="0"
                    :max="150"
                    placeholder="请输入年龄"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="editArchiveData.idCard" placeholder="请输入身份证号" maxlength="18"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="联系电话" prop="mobile">
                <el-input v-model="editArchiveData.mobile" placeholder="请输入联系电话" maxlength="11"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="婚姻状况" prop="maritalStatus">
                <el-select v-model="editArchiveData.maritalStatus" placeholder="请选择婚姻状况" style="width: 100%">
                  <el-option
                      v-for="item in maritalStatusOptions"
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
              <el-form-item label="居住小区" prop="neighborhood">
                <el-input v-model="editArchiveData.neighborhood" placeholder="请输入居住小区"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="居住地址" prop="address">
                <el-input v-model="editArchiveData.address" placeholder="请输入居住地址"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="居住状态" prop="residentialStatus">
                <el-select v-model="editArchiveData.residentialStatus" placeholder="请选择居住状态" style="width: 100%">
                  <el-option
                      v-for="item in residentialStatusOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="自理情况" prop="selfCareSituation">
                <el-select v-model="editArchiveData.selfCareSituation" placeholder="请选择自理情况" style="width: 100%">
                  <el-option
                      v-for="item in selfCareSituationOptions"
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
              <el-form-item label="医疗保险" prop="medicalInsurance">
                <el-select v-model="editArchiveData.medicalInsurance" placeholder="请选择医疗保险" style="width: 100%">
                  <el-option
                      v-for="item in medicalInsuranceOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="过敏药物" prop="allergicDrugs">
            <el-input
                v-model="editArchiveData.allergicDrugs"
                type="textarea"
                :rows="2"
                placeholder="请输入过敏药物"
            />
          </el-form-item>

          <el-form-item label="既往病史" prop="pastMedicalHistory">
            <el-input
                v-model="editArchiveData.pastMedicalHistory"
                type="textarea"
                :rows="3"
                placeholder="请输入既往病史"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeEditModal" :disabled="editLoading">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入档案弹窗 -->
    <el-dialog
        v-model="showImportModal"
        title="批量导入档案"
        width="500px"
        :close-on-click-modal="false"
    >
      <div style="text-align: center; padding: 20px;">
        <el-alert
            title="请先下载模板并严格按照模板格式填写后再导入，否则可能会导入失败！"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px;"
        />

        <div style="display: flex; justify-content: center; gap: 20px; margin-bottom: 20px;">
          <el-button type="primary" @click="downloadTemplate" icon="Download">
            下载模板
          </el-button>
          <el-upload
              action=""
              :show-file-list="false"
              :before-upload="handleFileUpload"
              accept=".xlsx,.xls"
          >
            <el-button type="success" icon="Upload">
              选择文件导入
            </el-button>
          </el-upload>
        </div>

        <div
            style="color: #666; font-size: 12px; text-align: left; background: #f5f7fa; padding: 10px; border-radius: 4px;">
          <p><strong>导入说明：</strong></p>
          <p>• 支持格式：.xlsx, .xls</p>
          <p>• 文件大小限制：10MB</p>
          <p>• 请确保数据格式正确，必填项不能为空</p>
          <p>• 身份证号不能重复</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="showImportModal = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import "@/assets/StyleCss.css";
</style>