
<template>
  <div class="repair-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>维修管理</h2>
        <p>记录设备故障信息，跟踪维修进度和处理结果。</p>
      </div>

      <el-button
        v-if="userStore.isAdmin"
        type="primary"
        size="large"
        @click="openAddDialog"
      >
        <el-icon><Plus /></el-icon>
        新增报修
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="statistics-row">

      <el-col
        v-for="item in statistics"
        :key="item.title"
        :xs="12"
        :sm="6"
      >
        <div class="stat-card">
          <div>
            <span>{{ item.title }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.description }}</small>
          </div>

          <div
            class="stat-icon"
            :style="{
              background: item.background,
              color: item.color
            }"
          >
            <el-icon :size="24">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </div>
      </el-col>

    </el-row>

    <!-- 维修记录列表 -->
    <div class="content-panel">

      <div class="panel-header">
        <div>
          <h3>维修记录</h3>
          <span>查询和管理实验室设备维修记录</span>
        </div>

        <el-button text :loading="loading" @click="loadRepairs">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 搜索区域 -->
      <el-form
        :model="searchForm"
        inline
        class="search-form"
        @submit.prevent="handleSearch"
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="设备名称、编号或故障描述"
            clearable
            style="width: 230px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="维修状态">
          <el-select
            v-model="searchForm.repairStatus"
            placeholder="全部状态"
            clearable
            style="width: 145px"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table
        :data="paginatedRepairs"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无维修记录"
      >

        <el-table-column
          prop="id"
          label="记录 ID"
          width="85"
          align="center"
        />

        <el-table-column
          prop="equipmentNo"
          label="设备编号"
          min-width="120"
        />

        <el-table-column
          prop="equipmentName"
          label="设备名称"
          min-width="140"
          show-overflow-tooltip
        />

        <el-table-column
          prop="faultDescription"
          label="故障描述"
          min-width="190"
          show-overflow-tooltip
        />

        <el-table-column
          prop="repairPerson"
          label="维修人员"
          min-width="110"
        >
          <template #default="{ row }">
            {{ row.repairPerson || '待分配' }}
          </template>
        </el-table-column>

        <el-table-column
          label="维修状态"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.repairStatus)">
              {{ row.repairStatus || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="reportTime"
          label="报修时间"
          min-width="165"
        >
          <template #default="{ row }">
            {{ formatDate(row.reportTime) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          :width="userStore.isAdmin ? 170 : 80"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">

            <el-button
              type="primary"
              link
              @click="viewDetail(row)"
            >
              详情
            </el-button>

            <template v-if="userStore.isAdmin">
              <el-button
                type="primary"
                link
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>

              <el-button
                type="danger"
                link
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>

          </template>
        </el-table-column>

      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <span>共 {{ filteredRepairs.length }} 条记录</span>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredRepairs.length"
          layout="sizes, prev, pager, next, jumper"
          background
        />
      </div>

    </div>

    <!-- 新增 / 编辑维修记录 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增设备报修' : '编辑维修记录'"
      width="620px"
      destroy-on-close
      @closed="resetForm"
    >

      <el-form
        ref="formRef"
        :model="repairForm"
        :rules="formRules"
        label-width="100px"
      >

        <!-- 选择设备 -->
        <el-form-item label="关联设备" prop="equipmentId">
          <el-select
            v-model="repairForm.equipmentId"
            placeholder="请选择报修设备"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in equipmentList"
              :key="item.id"
              :label="`${item.equipmentNo} - ${item.equipmentName}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 选择具体实体设备 -->
        <el-form-item label="实体设备" prop="assetId">
          <el-select
            v-model="repairForm.assetId"
            placeholder="请先选择设备型号"
            filterable
            style="width: 100%"
            :disabled="!repairForm.equipmentId"
          >
            <el-option
              v-for="item in filteredAssetList"
              :key="item.id"
              :label="`${item.equipmentNo} - ${item.location || '未设置位置'} - ${item.status}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="故障描述" prop="faultDescription">
          <el-input
            v-model="repairForm.faultDescription"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请详细描述设备故障情况"
          />
        </el-form-item>

        <el-form-item label="维修人员">
          <el-input
            v-model="repairForm.repairPerson"
            placeholder="请输入维修人员姓名"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="维修状态" prop="repairStatus">
          <el-select
            v-model="repairForm.repairStatus"
            style="width: 100%"
          >
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="维修结果">
          <el-input
            v-model="repairForm.repairResult"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="维修完成后填写处理结果"
          />
        </el-form-item>

        <el-form-item label="报修时间" prop="reportTime">
          <el-date-picker
            v-model="repairForm.reportTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择报修时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item
          v-if="repairForm.repairStatus === '已完成'"
          label="完成时间"
          prop="finishTime"
        >
          <el-date-picker
            v-model="repairForm.finishTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择维修完成时间"
            style="width: 100%"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitRepair"
        >
          {{ dialogMode === 'add' ? '确认报修' : '保存修改' }}
        </el-button>
      </template>

    </el-dialog>

    <!-- 维修详情 -->
    <el-dialog
      v-model="detailVisible"
      title="维修记录详情"
      width="600px"
    >
      <el-descriptions
        v-if="currentRepair"
        :column="2"
        border
      >
        <el-descriptions-item label="记录 ID">
          {{ currentRepair.id }}
        </el-descriptions-item>

        <el-descriptions-item label="设备编号">
          {{ currentRepair.equipmentNo || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备名称">
          {{ currentRepair.equipmentName || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="维修人员">
          {{ currentRepair.repairPerson || '待分配' }}
        </el-descriptions-item>

        <el-descriptions-item label="维修状态">
          <el-tag :type="getStatusType(currentRepair.repairStatus)">
            {{ currentRepair.repairStatus }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="报修时间">
          {{ formatDate(currentRepair.reportTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="故障描述" :span="2">
          {{ currentRepair.faultDescription || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="维修结果" :span="2">
          {{ currentRepair.repairResult || '暂未填写' }}
        </el-descriptions-item>

        <el-descriptions-item label="完成时间" :span="2">
          {{ formatDate(currentRepair.finishTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted
} from 'vue'

import {
  ElMessage,
  ElMessageBox
} from 'element-plus'

import {
  Plus,
  Refresh,
  Search,
  Tickets,
  Clock,
  Tools,
  CircleCheck
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'
import repairApi from '../api/repair'
import equipmentApi from '../api/equipment'

// 用户状态
const userStore = useUserStore()

// 页面数据
const repairList = ref([])
const equipmentList = ref([])

const loading = ref(false)
const submitLoading = ref(false)

// 状态
const statusOptions = [
  { label: '待维修', value: '待维修' },
  { label: '维修中', value: '维修中' },
  { label: '已完成', value: '已完成' }
]

// 搜索条件
const searchForm = reactive({
  keyword: '',
  repairStatus: ''
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogMode = ref('add')

const formRef = ref(null)
const currentRepair = ref(null)

// 当前时间
const getCurrentDateTime = () => {
  const date = new Date()
  const pad = value => String(value).padStart(2, '0')

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 空表单
const createEmptyForm = () => ({
  id: null,
  equipmentId: null,
  faultDescription: '',
  repairPerson: '',
  repairStatus: '待维修',
  repairResult: '',
  reportTime: getCurrentDateTime(),
  finishTime: null
})

const repairForm = reactive(createEmptyForm())

// 表单规则
const formRules = {
  equipmentId: [
    {
      required: true,
      message: '请选择关联设备',
      trigger: 'change'
    }
  ],
  faultDescription: [
    {
      required: true,
      message: '请输入故障描述',
      trigger: 'blur'
    }
  ],
  repairStatus: [
    {
      required: true,
      message: '请选择维修状态',
      trigger: 'change'
    }
  ],
  reportTime: [
    {
      required: true,
      message: '请选择报修时间',
      trigger: 'change'
    }
  ]
}

// 统一解析接口响应
const extractList = result => {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }

  if (Array.isArray(result.data)) {
    return result.data
  }

  return result.data?.records || result.data?.list || []
}

// 统计卡片
const statistics = computed(() => [
  {
    title: '维修总数',
    value: repairList.value.length,
    description: '系统累计维修记录',
    icon: Tickets,
    color: '#2878BD',
    background: '#E8F2FC'
  },
  {
    title: '待维修',
    value: repairList.value.filter(
      item => item.repairStatus === '待维修'
    ).length,
    description: '等待处理的维修记录',
    icon: Clock,
    color: '#E49A35',
    background: '#FFF4E4'
  },
  {
    title: '维修中',
    value: repairList.value.filter(
      item => item.repairStatus === '维修中'
    ).length,
    description: '正在处理的维修记录',
    icon: Tools,
    color: '#8B70D6',
    background: '#F0EBFC'
  },
  {
    title: '已完成',
    value: repairList.value.filter(
      item => item.repairStatus === '已完成'
    ).length,
    description: '已经完成的维修记录',
    icon: CircleCheck,
    color: '#19A879',
    background: '#E7F7F0'
  }
])

// 搜索过滤
const filteredRepairs = computed(() => {
  const keyword = searchForm.keyword.trim().toLowerCase()

  return repairList.value.filter(item => {

    const matchKeyword = !keyword ||
      String(item.equipmentName || '').toLowerCase().includes(keyword) ||
      String(item.equipmentNo || '').toLowerCase().includes(keyword) ||
      String(item.faultDescription || '').toLowerCase().includes(keyword)

    const matchStatus = !searchForm.repairStatus ||
      item.repairStatus === searchForm.repairStatus

    return matchKeyword && matchStatus
  })
})

// 当前页
const paginatedRepairs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value

  return filteredRepairs.value.slice(
    start,
    start + pageSize.value
  )
})

// 日期格式
const formatDate = value => {
  if (!value) return '-'

  return String(value).replace('T', ' ').slice(0, 19)
}

// 状态颜色
const getStatusType = status => {
  const map = {
    待维修: 'danger',
    维修中: 'warning',
    已完成: 'success'
  }

  return map[status] || 'info'
}

// 加载数据
const loadRepairs = async () => {
  loading.value = true

  try {
    const [repairResult, equipmentResult] = await Promise.all([
      repairApi.getAll(),
      equipmentApi.getAll()
    ])

    repairList.value = extractList(repairResult)
    equipmentList.value = extractList(equipmentResult)

    currentPage.value = 1

  } catch (error) {
    console.error('维修数据加载失败：', error)
    ElMessage.error('维修数据加载失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

// 条件查询
const handleSearch = async () => {
  currentPage.value = 1
  loading.value = true

  try {
    const result = await repairApi.search({
      keyword: searchForm.keyword.trim() || undefined,
      repairStatus: searchForm.repairStatus || undefined
    })

    repairList.value = extractList(result)

  } catch (error) {
    console.error('维修查询失败：', error)
    ElMessage.error('维修记录查询失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.repairStatus = ''
  loadRepairs()
}

// 新增报修
const openAddDialog = () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('当前账号没有新增维修记录的权限')
    return
  }

  if (!equipmentList.value.length) {
    ElMessage.warning('请先添加设备，再进行报修')
    return
  }

  dialogMode.value = 'add'
  Object.assign(repairForm, createEmptyForm())
  dialogVisible.value = true
}

// 编辑维修记录
const openEditDialog = async row => {
  if (!userStore.isAdmin) return

  try {
    const result = await repairApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '维修详情获取失败')
    }

    dialogMode.value = 'edit'

    Object.assign(
      repairForm,
      createEmptyForm(),
      result.data
    )

    repairForm.equipmentId = Number(result.data.equipmentId)

    dialogVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('维修记录详情获取失败')
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(repairForm, createEmptyForm())
}

// 提交维修记录
const submitRepair = async () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  try {
    await formRef.value.validate()

    if (
      repairForm.repairStatus === '已完成' &&
      !repairForm.repairResult.trim()
    ) {
      ElMessage.warning('维修完成时，请填写维修结果')
      return
    }

    if (
      repairForm.repairStatus === '已完成' &&
      !repairForm.finishTime
    ) {
      repairForm.finishTime = getCurrentDateTime()
    }

    if (repairForm.repairStatus !== '已完成') {
      repairForm.finishTime = null
    }

    submitLoading.value = true

    const data = {
      ...repairForm,
      equipmentId: Number(repairForm.equipmentId),
      repairPerson: repairForm.repairPerson?.trim() || '',
      faultDescription: repairForm.faultDescription.trim(),
      repairResult: repairForm.repairResult?.trim() || ''
    }

    let result

    if (dialogMode.value === 'add') {
      result = await repairApi.add(data)
    } else {
      result = await repairApi.update(
        repairForm.id,
        data
      )
    }

    if (result.code !== 200 && result.code !== 201) {
      throw new Error(result.message || '保存维修记录失败')
    }

    ElMessage.success(
      dialogMode.value === 'add'
        ? '报修记录新增成功'
        : '维修记录修改成功'
    )

    dialogVisible.value = false

    await loadRepairs()

  } catch (error) {
    console.error('维修记录保存失败：', error)

    if (error?.response) {
      ElMessage.error(
        error.response.data?.message || '保存失败，请检查后端响应'
      )
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
const viewDetail = async row => {
  try {
    const result = await repairApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '维修详情获取失败')
    }

    currentRepair.value = result.data
    detailVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('无法获取维修详情')
  }
}

// 删除维修记录
const handleDelete = async row => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除设备「${row.equipmentName || row.equipmentNo}」的维修记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await repairApi.delete(row.id)

    if (result.code !== 200) {
      throw new Error(result.message || '删除失败')
    }

    ElMessage.success('维修记录删除成功')

    await loadRepairs()

  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除维修记录失败：', error)
      ElMessage.error(
        error?.response?.data?.message || '删除失败，请检查后端响应'
      )
    }
  }
}

// 初始化
onMounted(() => {
  loadRepairs()
})
</script>

<style scoped>
.repair-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.page-header h2 {
  color: #233d59;
  font-size: 23px;
  font-weight: 700;
}

.page-header p {
  margin-top: 9px;
  color: #8998aa;
  font-size: 13px;
}

/* 统计 */
.statistics-row {
  row-gap: 15px;
}

.stat-card {
  min-height: 125px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 11px;
}

.stat-card span,
.stat-card strong,
.stat-card small {
  display: block;
}

.stat-card span {
  color: #8795a6;
  font-size: 13px;
}

.stat-card strong {
  margin-top: 10px;
  color: #263f5b;
  font-size: 28px;
}

.stat-card small {
  margin-top: 7px;
  color: #a0adbb;
  font-size: 11px;
}

.stat-icon {
  width: 46px;
  height: 46px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 11px;
}

/* 内容区域 */
.content-panel {
  min-width: 0;
  padding: 22px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 11px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.panel-header h3 {
  color: #344b63;
  font-size: 16px;
}

.panel-header span {
  display: block;
  margin-top: 7px;
  color: #9aa8b7;
  font-size: 12px;
}

/* 搜索 */
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 18px;
}

/* 表格 */
.content-panel :deep(.el-table th.el-table__cell) {
  background: #f5f8fc;
  color: #64748b;
  font-weight: 600;
}

.content-panel :deep(.el-table td.el-table__cell) {
  color: #52667b;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  padding-top: 22px;
  color: #8998aa;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-header {
    align-items: flex-start;
  }

  .content-panel {
    padding: 14px;
  }

  .pagination {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
