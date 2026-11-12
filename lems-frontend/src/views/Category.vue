
<template>
  <div class="category-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>分类管理</h2>
        <p>统一维护实验室设备分类信息，方便设备归类和查询。</p>
      </div>

      <el-button
        v-if="userStore.isAdmin"
        type="primary"
        size="large"
        @click="openAddDialog"
      >
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="statistics-row">

      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div>
            <span>分类总数</span>
            <strong>{{ categoryList.length }}</strong>
            <small>系统已建立的设备分类</small>
          </div>
          <div class="stat-icon purple">
            <el-icon><Grid /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div>
            <span>已关联设备</span>
            <strong>{{ equipmentCount }}</strong>
            <small>已归入分类的设备数量</small>
          </div>
          <div class="stat-icon blue">
            <el-icon><Box /></el-icon>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div>
            <span>未关联分类</span>
            <strong>{{ uncategorizedCount }}</strong>
            <small>尚未归入现有分类的设备</small>
          </div>
          <div class="stat-icon orange">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
      </el-col>

    </el-row>

    <!-- 分类列表 -->
    <div class="content-panel">

      <div class="panel-header">
        <div>
          <h3>设备分类列表</h3>
          <span>查看、搜索和维护设备分类信息</span>
        </div>

        <el-button
          text
          :loading="loading"
          @click="loadData"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 搜索 -->
      <div class="search-area">
        <el-input
          v-model="keyword"
          placeholder="请输入分类名称或描述"
          clearable
          style="width: 280px"
          @keyup.enter="currentPage = 1"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <span class="result-count">
          共 {{ filteredCategories.length }} 个分类
        </span>
      </div>

      <!-- 表格 -->
      <el-table
        :data="paginatedCategories"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无分类数据"
      >

        <el-table-column
          prop="id"
          label="分类 ID"
          width="100"
          align="center"
        />

        <el-table-column
          label="分类名称"
          min-width="180"
        >
          <template #default="{ row }">
            <div class="category-name">
              <div class="category-icon">
                <el-icon><Folder /></el-icon>
              </div>
              <strong>{{ row.categoryName }}</strong>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          label="关联设备数量"
          width="150"
          align="center"
        >
          <template #default="{ row }">
            <el-tag type="primary" effect="light">
              {{ getEquipmentCount(row.id) }} 台
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="description"
          label="分类描述"
          min-width="220"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{ row.description || '暂无描述' }}
          </template>
        </el-table-column>

        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="175"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          :width="userStore.isAdmin ? 180 : 90"
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
        <span>共 {{ filteredCategories.length }} 条记录</span>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredCategories.length"
          layout="sizes, prev, pager, next, jumper"
          background
        />
      </div>

    </div>

    <!-- 新增和编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'add' ? '新增设备分类' : '编辑设备分类'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >

      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="formRules"
        label-width="95px"
      >

        <el-form-item label="分类名称" prop="categoryName">
          <el-input
            v-model="categoryForm.categoryName"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请描述该分类包含的设备类型"
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
          @click="submitCategory"
        >
          {{ dialogMode === 'add' ? '确认新增' : '保存修改' }}
        </el-button>
      </template>

    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="分类详细信息"
      width="500px"
    >
      <el-descriptions
        v-if="currentCategory"
        :column="1"
        border
      >
        <el-descriptions-item label="分类 ID">
          {{ currentCategory.id }}
        </el-descriptions-item>

        <el-descriptions-item label="分类名称">
          {{ currentCategory.categoryName }}
        </el-descriptions-item>

        <el-descriptions-item label="关联设备数量">
          {{ getEquipmentCount(currentCategory.id) }} 台
        </el-descriptions-item>

        <el-descriptions-item label="分类描述">
          {{ currentCategory.description || '暂无描述' }}
        </el-descriptions-item>

        <el-descriptions-item label="创建时间">
          {{ formatDate(currentCategory.createTime) }}
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
  Grid,
  Box,
  Warning,
  Folder
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'
import categoryApi from '../api/category'
import equipmentApi from '../api/equipment'

// 用户状态
const userStore = useUserStore()

// 数据
const categoryList = ref([])
const equipmentList = ref([])

const loading = ref(false)
const submitLoading = ref(false)

// 搜索
const keyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogMode = ref('add')

const formRef = ref(null)
const currentCategory = ref(null)

// 表单
const createEmptyForm = () => ({
  categoryName: '',
  description: ''
})

const categoryForm = reactive(createEmptyForm())

// 表单规则
const formRules = {
  categoryName: [
    {
      required: true,
      message: '请输入分类名称',
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: '分类名称长度为2到50个字符',
      trigger: 'blur'
    }
  ],
  description: [
    {
      max: 200,
      message: '分类描述不能超过200个字符',
      trigger: 'blur'
    }
  ]
}

// 解析后端列表
const extractList = (result) => {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }

  if (Array.isArray(result.data)) {
    return result.data
  }

  return result.data?.records || result.data?.list || []
}

// 关联设备总数
const equipmentCount = computed(() =>
  equipmentList.value.filter(item => item.categoryId != null).length
)

// 未关联分类的设备
const uncategorizedCount = computed(() =>
  equipmentList.value.filter(item => item.categoryId == null).length
)

// 根据分类 ID 获取设备数量
const getEquipmentCount = (categoryId) => {
  return equipmentList.value.filter(
    item => String(item.categoryId) === String(categoryId)
  ).length
}

// 搜索过滤
const filteredCategories = computed(() => {
  const value = keyword.value.trim().toLowerCase()

  if (!value) return categoryList.value

  return categoryList.value.filter(item =>
    String(item.categoryName || '').toLowerCase().includes(value) ||
    String(item.description || '').toLowerCase().includes(value)
  )
})

// 当前页数据
const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value

  return filteredCategories.value.slice(
    start,
    start + pageSize.value
  )
})

// 格式化时间
const formatDate = (value) => {
  if (!value) return '-'

  return String(value).replace('T', ' ').slice(0, 19)
}

// 加载数据
const loadData = async () => {
  loading.value = true

  try {
    const [categoryResult, equipmentResult] = await Promise.all([
      categoryApi.getAll(),
      equipmentApi.getAll()
    ])

    categoryList.value = extractList(categoryResult)
    equipmentList.value = extractList(equipmentResult)

    currentPage.value = 1

  } catch (error) {
    console.error('分类数据加载失败：', error)
    ElMessage.error('分类数据加载失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

// 新增分类
const openAddDialog = () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('只有管理员可以新增分类')
    return
  }

  dialogMode.value = 'add'
  Object.assign(categoryForm, createEmptyForm())
  dialogVisible.value = true
}

// 编辑分类
const openEditDialog = async (row) => {
  if (!userStore.isAdmin) return

  try {
    const result = await categoryApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '分类详情获取失败')
    }

    dialogMode.value = 'edit'

    Object.assign(
      categoryForm,
      createEmptyForm(),
      result.data
    )

    dialogVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('无法获取分类详情')
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.clearValidate()
  Object.assign(categoryForm, createEmptyForm())
}

// 提交分类
const submitCategory = async () => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  try {
    await formRef.value.validate()

    submitLoading.value = true

    const data = {
      categoryName: categoryForm.categoryName.trim(),
      description: categoryForm.description?.trim() || ''
    }

    let result

    if (dialogMode.value === 'add') {
      result = await categoryApi.add(data)
    } else {
      result = await categoryApi.update(
        categoryForm.id,
        data
      )
    }

    if (result.code !== 200 && result.code !== 201) {
      throw new Error(result.message || '保存分类失败')
    }

    ElMessage.success(
      dialogMode.value === 'add'
        ? '分类新增成功'
        : '分类修改成功'
    )

    dialogVisible.value = false

    await loadData()

  } catch (error) {
    console.error('分类保存失败：', error)
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
const viewDetail = async (row) => {
  try {
    const result = await categoryApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '获取详情失败')
    }

    currentCategory.value = result.data
    detailVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('分类详情获取失败')
  }
}

// 删除分类
const handleDelete = async (row) => {
  if (!userStore.isAdmin) {
    ElMessage.warning('没有管理员操作权限')
    return
  }

  // 如果分类下还有设备，先阻止删除，避免产生关联问题
  const count = getEquipmentCount(row.id)

  if (count > 0) {
    ElMessage.warning(
      `该分类下还有 ${count} 台设备，请先处理这些设备后再删除`
    )
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.categoryName}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await categoryApi.delete(row.id)

    if (result.code !== 200) {
      throw new Error(result.message || '删除失败')
    }

    ElMessage.success('分类删除成功')

    await loadData()

  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('分类删除失败：', error)
      ElMessage.error(
        error?.response?.data?.message || '删除失败，请检查分类关联情况'
      )
    }
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 页面标题 */
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
  font-size: 22px;
}

.purple {
  color: #8b70d6;
  background: #f0ebfc;
}

.blue {
  color: #2878bd;
  background: #e8f2fc;
}

.orange {
  color: #e49a35;
  background: #fff4e4;
}

/* 主内容 */
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
.search-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.result-count {
  color: #9aa8b7;
  font-size: 12px;
}

/* 分类名称 */
.category-name {
  display: flex;
  align-items: center;
  gap: 11px;
}

.category-name strong {
  color: #344b63;
  font-size: 13px;
}

.category-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #2878bd;
  background: #e8f2fc;
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

  .pagination {
    flex-direction: column;
    align-items: flex-start;
  }

  .content-panel {
    padding: 14px;
  }
}
</style>
