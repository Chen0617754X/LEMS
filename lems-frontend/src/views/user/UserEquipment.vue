
<template>
  <div class="equipment-page">
    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h2>设备查询</h2>
        <p>浏览实验室设备信息，快速查找所需设备</p>
      </div>
      <el-tag type="success" effect="light">只读权限</el-tag>
    </div>

    <!-- 数据统计 -->
    <el-row :gutter="16" class="statistics">
      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div class="stat-label">设备总数</div>
          <div class="stat-value">{{ equipmentList.length }}</div>
          <div class="stat-desc">当前系统设备数量</div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div class="stat-label">设备分类</div>
          <div class="stat-value">{{ categoryList.length }}</div>
          <div class="stat-desc">已登记的设备分类</div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="8">
        <div class="stat-card">
          <div class="stat-label">搜索结果</div>
          <div class="stat-value">{{ filteredList.length }}</div>
          <div class="stat-desc">当前符合条件的设备</div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <div class="search-card">
      <div class="search-title">设备信息筛选</div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="设备名称、编号或品牌"
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
            style="width: 180px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="设备状态">
          <el-input
            v-model="searchForm.status"
            placeholder="输入设备状态"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="resetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 设备列表 -->
    <div class="table-card">
      <div class="table-heading">
        <div>
          <h3>设备列表</h3>
          <span>共 {{ filteredList.length }} 条记录</span>
        </div>
        <el-button @click="loadEquipment">
          刷新数据
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="filteredList"
        stripe
        style="width: 100%"
        empty-text="暂无设备数据"
      >
        <el-table-column prop="equipmentNo" label="设备编号" min-width="130" />

        <el-table-column prop="equipmentName" label="设备名称" min-width="140" />

        <el-table-column label="品牌型号" min-width="150">
          <template #default="{ row }">
            {{ row.brand || '-' }} {{ row.model || '' }}
          </template>
        </el-table-column>

        <el-table-column label="所属分类" min-width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>

        <el-table-column label="设备状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ row.status || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="location" label="存放位置" min-width="120" />

        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="showDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 设备详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="设备详细信息"
      width="620px"
      destroy-on-close
    >
      <el-descriptions
        v-if="currentEquipment"
        :column="2"
        border
      >
        <el-descriptions-item label="设备编号">
          {{ currentEquipment.equipmentNo || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备名称">
          {{ currentEquipment.equipmentName || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备分类">
          {{ getCategoryName(currentEquipment.categoryId) }}
        </el-descriptions-item>

        <el-descriptions-item label="品牌">
          {{ currentEquipment.brand || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="型号">
          {{ currentEquipment.model || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备价格">
          {{ currentEquipment.price ?? '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备状态">
          {{ currentEquipment.status || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="存放位置">
          {{ currentEquipment.location || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="购入日期">
          {{ currentEquipment.purchaseDate || '-' }}
        </el-descriptions-item>

        <el-descriptions-item label="设备描述" :span="2">
          {{ currentEquipment.description || '暂无描述' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import equipmentApi from '../../api/equipment'
import request from '../../api/request'

const loading = ref(false)

const equipmentList = ref([])
const categoryList = ref([])

const detailVisible = ref(false)
const currentEquipment = ref(null)

const searchForm = ref({
  keyword: '',
  categoryId: '',
  status: ''
})

// 兼容统一 ApiResponse 返回结构
function getResponseData(result) {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }
  return result.data
}

// 查询全部设备
async function loadEquipment() {
  loading.value = true

  try {
    const result = await equipmentApi.getAll()
    equipmentList.value = getResponseData(result) || []
  } catch (error) {
    console.error('设备查询失败：', error)
  } finally {
    loading.value = false
  }
}

// 查询设备分类
async function loadCategories() {
  try {
    const result = await request.get('/api/category')
    categoryList.value = getResponseData(result) || []
  } catch (error) {
    console.error('分类查询失败：', error)
  }
}

// 根据分类ID获取分类名称
function getCategoryName(categoryId) {
  const category = categoryList.value.find(
    item => String(item.id) === String(categoryId)
  )

  return category?.categoryName || '未分类'
}

// 搜索结果
const filteredList = computed(() => {
  return equipmentList.value
})

// 条件搜索
async function handleSearch() {
  const params = {}

  if (searchForm.value.keyword.trim()) {
    params.keyword = searchForm.value.keyword.trim()
  }

  if (searchForm.value.categoryId !== '') {
    params.categoryId = searchForm.value.categoryId
  }

  if (searchForm.value.status.trim()) {
    params.status = searchForm.value.status.trim()
  }

  loading.value = true

  try {
    const result = await equipmentApi.search(params)
    equipmentList.value = getResponseData(result) || []
  } catch (error) {
    console.error('设备搜索失败：', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
function resetSearch() {
  searchForm.value = {
    keyword: '',
    categoryId: '',
    status: ''
  }

  loadEquipment()
}

// 查看设备详情
async function showDetail(row) {
  try {
    const result = await equipmentApi.getById(row.id)
    currentEquipment.value = getResponseData(result)
    detailVisible.value = true
  } catch (error) {
    console.error('设备详情获取失败：', error)
  }
}

// 状态标签颜色
function getStatusType(status) {
  const value = String(status || '').toLowerCase()

  if (['正常', '在用', 'available', 'normal'].includes(value)) {
    return 'success'
  }

  if (['维修中', '维修', 'repairing'].includes(value)) {
    return 'warning'
  }

  if (['报废', '停用', 'scrapped', 'disabled'].includes(value)) {
    return 'danger'
  }

  return 'info'
}

onMounted(() => {
  loadEquipment()
  loadCategories()
})
</script>

<style scoped>
.equipment-page {
  max-width: 1500px;
  margin: 0 auto;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 22px;
}

.page-heading h2 {
  margin: 0 0 9px;
  font-size: 23px;
  color: #26364d;
}

.page-heading p {
  margin: 0;
  font-size: 13px;
  color: #98a3b3;
}

.statistics {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  padding: 19px 22px;
  margin-bottom: 15px;
}

.stat-label {
  color: #8491a5;
  font-size: 13px;
}

.stat-value {
  font-size: 29px;
  font-weight: 700;
  color: #3478f6;
  margin: 10px 0;
}

.stat-desc {
  color: #a5afbd;
  font-size: 12px;
}

.search-card,
.table-card {
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  padding: 22px;
  margin-bottom: 20px;
}

.search-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: -18px;
}

.table-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-heading h3 {
  font-size: 16px;
  margin: 0 0 8px;
}

.table-heading span {
  font-size: 12px;
  color: #98a3b3;
}

@media (max-width: 768px) {
  .search-card,
  .table-card {
    padding: 14px;
  }
}
</style>
