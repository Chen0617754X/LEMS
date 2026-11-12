
<template>
  <div class="equipment-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>设备管理</h2>
        <p>统一管理实验室设备信息，支持设备查询、新增、编辑和删除。</p>
      </div>

      <el-button
        v-if="userStore.isAdmin"
        type="primary"
        size="large"
        @click="openAddDialog"
      >
        <el-icon><Plus /></el-icon>
        新增设备
      </el-button>
    </div>

    <!-- 数据统计 -->
    <el-row :gutter="16" class="statistics">

      <el-col :xs="12" :sm="6" v-for="item in statistics" :key="item.title">
        <div class="stat-card">
          <div>
            <span>{{ item.title }}</span>
            <strong>{{ item.value }}</strong>
          </div>

          <div class="stat-icon" :style="{
            background: item.background,
            color: item.color
          }">
            <el-icon :size="25">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </div>
      </el-col>

    </el-row>

    <!-- 搜索区域 -->
    <div class="search-panel">

      <div class="search-title">
        <div>
          <h3>设备信息</h3>
          <span>查询和管理系统中的设备数据</span>
        </div>

        <el-button
          text
          :loading="loading"
          @click="loadEquipment"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-form
        :model="searchForm"
        inline
        class="search-form"
        @submit.prevent="handleSearch"
      >
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="设备编号 / 设备名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="设备分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="全部分类"
            clearable
            style="width: 170px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id ?? item.categoryId"
              :label="item.categoryName"
              :value="item.id ?? item.categoryId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="设备状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
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

          <el-button @click="resetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 设备表格 -->
    <div class="table-panel">

      <el-table
        :data="paginatedEquipment"
        v-loading="loading"
        stripe
        border
        row-key="id"
        style="width: 100%"
        empty-text="暂无设备数据"
      >

        <el-table-column
          prop="equipmentNo"
          label="设备编号"
          min-width="120"
          show-overflow-tooltip
        />

        <el-table-column
          prop="equipmentName"
          label="设备名称"
          min-width="150"
          show-overflow-tooltip
        />

        <el-table-column
          label="设备分类"
          min-width="120"
        >
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="brand"
          label="品牌"
          min-width="100"
        />

        <el-table-column
          prop="model"
          label="型号"
          min-width="110"
        />

        <el-table-column
          prop="price"
          label="单价"
          min-width="100"
        >
          <template #default="{ row }">
            ¥{{ Number(row.price || 0).toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column
          prop="location"
          label="存放位置"
          min-width="120"
          show-overflow-tooltip
        />

        <el-table-column
          label="设备状态"
          width="105"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              effect="light"
            >
              {{ row.status || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="purchaseDate"
          label="购入日期"
          min-width="115"
        />

        <el-table-column
          label="操作"
          :width="userStore.isAdmin ? 150 : 85"
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
        <span>共 {{ filteredEquipment.length }} 条记录</span>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredEquipment.length"
          layout="sizes, prev, pager, next, jumper"
          background
        />
      </div>

    </div>

    <!-- 新增 / 编辑设备弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增设备' : '编辑设备'"
      width="650px"
      destroy-on-close
      @closed="resetDialog"
    >

      <el-form
        ref="equipmentFormRef"
        :model="equipmentForm"
        :rules="formRules"
        label-width="100px"
        class="equipment-form"
      >

        <el-row :gutter="16">

          <el-col :span="12">
            <el-form-item label="设备编号" prop="equipmentNo">
              <el-input
                v-model="equipmentForm.equipmentNo"
                placeholder="请输入设备编号"
                maxlength="50"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备名称" prop="equipmentName">
              <el-input
                v-model="equipmentForm.equipmentName"
                placeholder="请输入设备名称"
                maxlength="100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-select
                v-model="equipmentForm.categoryId"
                placeholder="请选择分类"
                style="width: 100%"
              >
                <el-option
                  v-for="item in categoryList"
                  :key="item.id ?? item.categoryId"
                  :label="item.categoryName"
                  :value="item.id ?? item.categoryId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备品牌">
              <el-input
                v-model="equipmentForm.brand"
                placeholder="请输入品牌"
                maxlength="100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备型号">
              <el-input
                v-model="equipmentForm.model"
                placeholder="请输入型号"
                maxlength="100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备单价" prop="price">
              <el-input-number
                v-model="equipmentForm.price"
                :min="0"
                :precision="2"
                :step="100"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备数量" prop="quantity">
              <el-input-number
                v-model="equipmentForm.quantity"
                :min="1"
                :precision="0"
                :step="1"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="设备状态" prop="status">
              <el-select
                v-model="equipmentForm.status"
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
          </el-col>

          <el-col :span="12">
            <el-form-item label="存放位置">
              <el-input
                v-model="equipmentForm.location"
                placeholder="例如：实验室A-101"
                maxlength="100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="购入日期">
              <el-date-picker
                v-model="equipmentForm.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择购入日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="设备描述">
              <el-input
                v-model="equipmentForm.description"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="请输入设备相关描述"
              />
            </el-form-item>
          </el-col>

        </el-row>

      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitEquipment"
        >
          {{ dialogMode === 'add' ? '确认新增' : '保存修改' }}
        </el-button>
      </template>

    </el-dialog>

    <!-- 设备详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="设备详细信息"
      width="600px"
    >
      <el-descriptions
        v-if="currentEquipment"
        :column="2"
        border
      >
        <el-descriptions-item label="设备编号">
          {{ currentEquipment.equipmentNo }}
        </el-descriptions-item>

        <el-descriptions-item label="设备名称">
          {{ currentEquipment.equipmentName }}
        </el-descriptions-item>

        <el-descriptions-item label="设备分类">
          {{ getCategoryName(currentEquipment.categoryId) }}
        </el-descriptions-item>

        <el-descriptions-item label="品牌">
          {{ currentEquipment.brand || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备型号">
          {{ currentEquipment.model || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备单价">
          ¥{{ Number(currentEquipment.price || 0).toFixed(2) }}
        </el-descriptions-item>

        <el-descriptions-item label="设备数量">
          {{ currentEquipment.quantity }}
        </el-descriptions-item>

        <el-descriptions-item label="设备状态">
          <el-tag :type="getStatusType(currentEquipment.status)">
            {{ currentEquipment.status }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="存放位置">
          {{ currentEquipment.location || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="购入日期">
          {{ currentEquipment.purchaseDate || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备描述" :span="2">
          {{ currentEquipment.description || '-' }}
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
  Box,
  CircleCheck,
  Tools,
  Grid
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'
import equipmentApi from '../api/equipment'
import request from '../api/request'

// 用户状态
const userStore = useUserStore()

// 页面数据
const equipmentList = ref([])
const categoryList = ref([])

const loading = ref(false)
const submitLoading = ref(false)

// 搜索条件
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: ''
})

// 状态选项
const statusOptions = [
  { label: '正常', value: '正常' },
  { label: '待维修', value: '待维修' },
  { label: '维修中', value: '维修中' },
  { label: '报废', value: '报废' }
]

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogMode = ref('add')

const equipmentFormRef = ref(null)
const currentEquipment = ref(null)

// 新增和编辑表单
const createEmptyForm = () => ({
  equipmentNo: '',
  equipmentName: '',
  categoryId: null,
  brand: '',
  model: '',
  price: 0,
  quantity: 1,
  location: '',
  status: '正常',
  purchaseDate: '',
  description: ''
})

const equipmentForm = reactive(createEmptyForm())

// 表单验证
const formRules = {
  equipmentNo: [
    {
      required: true,
      message: '请输入设备编号',
      trigger: 'blur'
    }
  ],
  equipmentName: [
    {
      required: true,
      message: '请输入设备名称',
      trigger: 'blur'
    }
  ],
  categoryId: [
    {
      required: true,
      message: '请选择设备分类',
      trigger: 'change'
    }
  ],
  price: [
    {
      required: true,
      message: '请输入设备单价',
      trigger: 'change'
    }
  ],
  quantity: [
    {
      required: true,
      message: '请输入设备数量',
      trigger: 'change'
    }
  ],
  status: [
    {
      required: true,
      message: '请选择设备状态',
      trigger: 'change'
    }
  ]
}

// 统一提取后端数据
const extractList = (result) => {
  if (result?.code !== 200) {
    throw new Error(result?.message || '获取数据失败')
  }

  if (Array.isArray(result.data)) {
    return result.data
  }

  return result.data?.records || result.data?.list || []
}

// 分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(
    item => String(item.id ?? item.categoryId) === String(categoryId)
  )

  return category?.categoryName || `分类 ${categoryId ?? '-'}`
}

// 统计数据
const statistics = computed(() => [
  {
    title: '设备总数',
    value: equipmentList.value.length,
    icon: Box,
    color: '#2878BD',
    background: '#E8F2FC'
  },
  {
    title: '正常设备',
    value: equipmentList.value.filter(
      item => item.status === '正常'
    ).length,
    icon: CircleCheck,
    color: '#19A879',
    background: '#E7F7F0'
  },
  {
    title: '维修设备',
    value: equipmentList.value.filter(
      item => ['待维修', '维修中'].includes(item.status)
    ).length,
    icon: Tools,
    color: '#E49A35',
    background: '#FFF4E4'
  },
  {
    title: '设备分类',
    value: categoryList.value.length,
    icon: Grid,
    color: '#8B70D6',
    background: '#F0EBFC'
  }
])

// 前端二次筛选，保证关键词、分类、状态可以同时生效
const filteredEquipment = computed(() => {
  const keyword = searchForm.keyword.trim().toLowerCase()

  return equipmentList.value.filter(item => {

    const matchKeyword = !keyword ||
      String(item.equipmentNo || '').toLowerCase().includes(keyword) ||
      String(item.equipmentName || '').toLowerCase().includes(keyword)

    const matchCategory = searchForm.categoryId == null ||
      String(item.categoryId) === String(searchForm.categoryId)

    const matchStatus = !searchForm.status ||
      item.status === searchForm.status

    return matchKeyword && matchCategory && matchStatus
  })
})

// 当前页数据
const paginatedEquipment = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value

  return filteredEquipment.value.slice(
    start,
    start + pageSize.value
  )
})

// 查询设备
const loadEquipment = async () => {
  loading.value = true

  try {
    const [equipmentResult, categoryResult] = await Promise.all([
      equipmentApi.getAll(),
      request.get('/api/category')
    ])

    equipmentList.value = extractList(equipmentResult)
    categoryList.value = extractList(categoryResult)

    currentPage.value = 1

  } catch (error) {
    console.error('设备数据加载失败：', error)
    ElMessage.error('设备数据加载失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = async () => {
  currentPage.value = 1

  // 使用后端条件查询接口
  loading.value = true

  try {
    const result = await equipmentApi.search({
      keyword: searchForm.keyword.trim() || undefined,
      categoryId: searchForm.categoryId ?? undefined,
      status: searchForm.status || undefined
    })

    equipmentList.value = extractList(result)

  } catch (error) {
    console.error('设备搜索失败：', error)
    ElMessage.error('设备查询失败')
  } finally {
    loading.value = false
  }
}

// 重置
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.status = ''

  loadEquipment()
}

// 设备状态颜色
const getStatusType = (status) => {
  const map = {
    正常: 'success',
    待维修: 'danger',
    维修中: 'warning',
    报废: 'info'
  }

  return map[status] || 'info'
}

// 打开新增弹窗
const openAddDialog = () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('只有管理员可以新增设备')
    return
  }

  dialogMode.value = 'add'
  Object.assign(equipmentForm, createEmptyForm())
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = async (row) => {
  if (!userStore.isAdmin) return

  try {
    const result = await equipmentApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '设备详情获取失败')
    }

    dialogMode.value = 'edit'

    Object.assign(
      equipmentForm,
      createEmptyForm(),
      result.data
    )

    dialogVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('无法获取设备详情')
  }
}

// 重置弹窗
const resetDialog = () => {
  equipmentFormRef.value?.clearValidate()
  Object.assign(equipmentForm, createEmptyForm())
}

// 提交设备
const submitEquipment = async () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  try {
    await equipmentFormRef.value.validate()

    submitLoading.value = true

    const data = {
      ...equipmentForm,
      equipmentNo: equipmentForm.equipmentNo.trim(),
      equipmentName: equipmentForm.equipmentName.trim(),
      brand: equipmentForm.brand?.trim() || '',
      model: equipmentForm.model?.trim() || '',
      location: equipmentForm.location?.trim() || '',
      description: equipmentForm.description?.trim() || '',
      price: Number(equipmentForm.price),
      quantity: Number(equipmentForm.quantity)
    }

    let result

    if (dialogMode.value === 'add') {
      result = await equipmentApi.add(data)
    } else {
      result = await equipmentApi.update(
        equipmentForm.id,
        data
      )
    }

    if (result.code !== 200 && result.code !== 201) {
      throw new Error(result.message || '保存失败')
    }

    ElMessage.success(
      dialogMode.value === 'add'
        ? '设备新增成功'
        : '设备修改成功'
    )

    dialogVisible.value = false

    await loadEquipment()

  } catch (error) {
    if (error?.response || error instanceof Error) {
      console.error('设备保存失败：', error)
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看设备详情
const viewDetail = (row) => {
  currentEquipment.value = row
  detailVisible.value = true
}

// 删除设备
const handleDelete = async (row) => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除设备「${row.equipmentName}」吗？删除后无法恢复。`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await equipmentApi.delete(row.id)

    if (result.code !== 200) {
      throw new Error(result.message || '删除失败')
    }

    ElMessage.success('设备删除成功')

    await loadEquipment()

  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('删除设备失败：', error)
    }
  }
}

// 初始化
onMounted(() => {
  loadEquipment()
})
</script>

<style scoped>
.equipment-page {
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

/* 统计卡片 */
.statistics {
  row-gap: 15px;
}

.stat-card {
  min-height: 112px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 11px;
}

.stat-card span {
  display: block;
  color: #8795a6;
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  color: #263f5b;
  font-size: 28px;
}

.stat-icon {
  width: 45px;
  height: 45px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 11px;
}

/* 搜索区域 */
.search-panel,
.table-panel {
  padding: 22px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 11px;
}

.search-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.search-title h3 {
  color: #344b63;
  font-size: 16px;
}

.search-title span {
  display: block;
  margin-top: 7px;
  color: #9aa8b7;
  font-size: 12px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 5px;
  margin-right: 18px;
}

/* 表格 */
.table-panel {
  min-width: 0;
}

.table-panel :deep(.el-table th.el-table__cell) {
  background: #f5f8fc;
  color: #64748b;
  font-weight: 600;
}

.table-panel :deep(.el-table td.el-table__cell) {
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

/* 表单 */
.equipment-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

@media (max-width: 900px) {
  .pagination {
    flex-direction: column;
    align-items: flex-start;
  }

  .page-header {
    align-items: flex-start;
  }
}
</style>
