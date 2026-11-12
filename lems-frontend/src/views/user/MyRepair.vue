
<template>
  <div class="my-repair-page">

    <!-- 标题 -->
    <div class="page-heading">
      <div>
        <h2>我的维修记录</h2>
        <p>查看自己提交的设备报修申请及维修处理进度。</p>
      </div>

      <el-button type="primary" @click="$router.push('/user/repair')">
        <el-icon><Plus /></el-icon>
        新建报修
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="statistics">
      <el-col :xs="12" :sm="6" v-for="item in statistics" :key="item.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: item.bg, color: item.color }">
            <el-icon :size="23"><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <div class="stat-number">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <div class="search-card">
      <el-input
        v-model="keyword"
        placeholder="搜索设备名称、设备编号或故障描述"
        clearable
        :prefix-icon="Search"
        style="max-width: 360px"
      />

      <el-select v-model="statusFilter" placeholder="全部状态" clearable>
        <el-option label="待维修" value="待维修" />
        <el-option label="维修中" value="维修中" />
        <el-option label="已完成" value="已完成" />
      </el-select>

      <el-button type="primary" @click="loadRepairs">
        刷新记录
      </el-button>
    </div>

    <!-- 维修记录表格 -->
    <div class="table-card">

      <div class="table-heading">
        <div>
          <h3>维修申请列表</h3>
          <span>共 {{ filteredRepairs.length }} 条记录</span>
        </div>
      </div>

      <el-table
        :data="filteredRepairs"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无维修记录，您可以提交新的报修申请"
      >
        <el-table-column prop="id" label="维修编号" width="100" />

        <el-table-column label="设备信息" min-width="170">
          <template #default="{ row }">
            <div class="equipment-name">
              <strong>{{ row.equipmentName || '未知设备' }}</strong>
              <span>{{ row.equipmentNo || '-' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="faultDescription"
          label="故障描述"
          min-width="190"
          show-overflow-tooltip
        />

        <el-table-column
          prop="repairPerson"
          label="维修人员"
          width="110"
        >
          <template #default="{ row }">
            {{ row.repairPerson || '待安排' }}
          </template>
        </el-table-column>

        <el-table-column label="报修时间" min-width="165">
          <template #default="{ row }">
            {{ formatTime(row.reportTime) }}
          </template>
        </el-table-column>

        <el-table-column label="维修状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.repairStatus)" effect="light">
              {{ row.repairStatus || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

    </div>

    <!-- 维修详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="维修记录详情"
      width="620px"
      destroy-on-close
    >
      <div v-if="currentRepair" class="detail-content">

        <div class="detail-status">
          <span>当前维修状态</span>
          <el-tag
            :type="getStatusType(currentRepair.repairStatus)"
            size="large"
          >
            {{ currentRepair.repairStatus }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="维修编号">
            {{ currentRepair.id }}
          </el-descriptions-item>

          <el-descriptions-item label="设备编号">
            {{ currentRepair.equipmentNo || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="设备名称">
            {{ currentRepair.equipmentName || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="维修人员">
            {{ currentRepair.repairPerson || '待安排' }}
          </el-descriptions-item>

          <el-descriptions-item label="报修时间">
            {{ formatTime(currentRepair.reportTime) }}
          </el-descriptions-item>

          <el-descriptions-item label="完成时间">
            {{ formatTime(currentRepair.finishTime) }}
          </el-descriptions-item>

          <el-descriptions-item label="故障描述" :span="2">
            {{ currentRepair.faultDescription || '-' }}
          </el-descriptions-item>

          <el-descriptions-item label="维修结果" :span="2">
            {{ currentRepair.repairResult || '暂未填写维修结果' }}
          </el-descriptions-item>
        </el-descriptions>

      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

import {
  Plus,
  Search,
  Clock,
  Tools,
  CircleCheck,
  Document
} from '@element-plus/icons-vue'

import repairApi from '../../api/repair'

const loading = ref(false)
const detailVisible = ref(false)

const repairs = ref([])
const currentRepair = ref(null)

const keyword = ref('')
const statusFilter = ref('')

// 统计数据
const statistics = computed(() => [
  {
    label: '全部申请',
    value: repairs.value.length,
    icon: Document,
    bg: '#edf4ff',
    color: '#3478e5'
  },
  {
    label: '待维修',
    value: repairs.value.filter(r => r.repairStatus === '待维修').length,
    icon: Clock,
    bg: '#fff5e8',
    color: '#e9a23b'
  },
  {
    label: '维修中',
    value: repairs.value.filter(r => r.repairStatus === '维修中').length,
    icon: Tools,
    bg: '#edf3ff',
    color: '#4778dc'
  },
  {
    label: '已完成',
    value: repairs.value.filter(r => r.repairStatus === '已完成').length,
    icon: CircleCheck,
    bg: '#eaf8f0',
    color: '#36a675'
  }
])

// 前端搜索和筛选
const filteredRepairs = computed(() => {
  return repairs.value.filter(item => {
    const text = keyword.value.trim().toLowerCase()

    const matchesKeyword = !text || [
      item.equipmentName,
      item.equipmentNo,
      item.faultDescription
    ].some(value => String(value || '').toLowerCase().includes(text))

    const matchesStatus = !statusFilter.value ||
      item.repairStatus === statusFilter.value

    return matchesKeyword && matchesStatus
  })
})

// 加载维修记录
async function loadRepairs() {
  loading.value = true

  try {
    const result = await repairApi.getMyRepairs()

    if (result?.code !== 200) {
      throw new Error(result?.message || '查询失败')
    }

    const data = result.data

    repairs.value = Array.isArray(data)
      ? data
      : data?.records || data?.list || []

  } catch (error) {
    console.error('维修记录加载失败：', error)
    ElMessage.error('维修记录加载失败，请检查服务器连接')
  } finally {
    loading.value = false
  }
}

// 查看详情
async function showDetail(row) {
  try {
    const result = await repairApi.getMyRepairById(row.id)

    if (result?.code !== 200) {
      throw new Error(result?.message || '详情查询失败')
    }

    currentRepair.value = result.data
    detailVisible.value = true

  } catch (error) {
    console.error('维修详情加载失败：', error)
    ElMessage.error('维修详情获取失败')
  }
}

// 状态颜色
function getStatusType(status) {
  const types = {
    '待维修': 'warning',
    '维修中': 'primary',
    '已完成': 'success'
  }

  return types[status] || 'info'
}

// 时间格式化
function formatTime(value) {
  if (!value) return '-'

  return String(value).replace('T', ' ').slice(0, 19)
}

onMounted(loadRepairs)
</script>

<style scoped>
.my-repair-page {
  max-width: 1450px;
  margin: 0 auto;
}

.page-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
}

.page-heading h2 {
  margin: 0 0 9px;
  color: #263d55;
  font-size: 23px;
}

.page-heading p {
  margin: 0;
  color: #98a5b4;
  font-size: 13px;
}

.statistics {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  min-height: 105px;
  padding: 18px;
  margin-bottom: 15px;
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 11px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 11px;
  flex-shrink: 0;
}

.stat-number {
  color: #30465e;
  font-size: 25px;
  font-weight: 650;
}

.stat-label {
  margin-top: 6px;
  color: #98a5b4;
  font-size: 12px;
}

.search-card {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 19px;
  margin-bottom: 20px;
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 11px;
}

.table-card {
  padding: 23px;
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 11px;
}

.table-heading {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-heading h3 {
  margin: 0 0 8px;
  color: #30465e;
  font-size: 16px;
}

.table-heading span {
  color: #98a5b4;
  font-size: 12px;
}

.equipment-name strong,
.equipment-name span {
  display: block;
}

.equipment-name strong {
  color: #344b63;
  font-size: 13px;
}

.equipment-name span {
  margin-top: 5px;
  color: #98a5b4;
  font-size: 11px;
}

.detail-content {
  padding: 5px 0;
}

.detail-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 20px;
  background: #f5f8fc;
  border-radius: 8px;
}

.detail-status span {
  color: #718096;
  font-size: 13px;
}

@media (max-width: 768px) {
  .page-heading {
    align-items: flex-start;
    gap: 12px;
  }

  .table-card {
    padding: 12px;
    overflow-x: auto;
  }

  .search-card {
    padding: 12px;
  }

  .search-card .el-input,
  .search-card .el-select {
    width: 100% !important;
    max-width: none !important;
  }
}
</style>
