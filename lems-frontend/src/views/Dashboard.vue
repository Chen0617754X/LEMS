
<template>
  <div class="dashboard">

    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h2>系统首页</h2>
        <p>欢迎使用 LEMS 实验室设备管理系统，以下是当前设备管理概况。</p>
      </div>

      <el-button
        type="primary"
        plain
        :loading="loading"
        @click="loadDashboard"
      >
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 数据统计卡片 -->
    <el-row :gutter="18" class="statistics-row">

    <el-col
        :xs="12"
        :sm="8"
        :md="8"
        :lg="4"
        v-for="item in statistics"
        :key="item.title"
    >
        <div class="stat-card">

        <div class="stat-info">
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
            <el-icon :size="25">
            <component :is="item.icon" />
            </el-icon>
        </div>

        </div>
    </el-col>

    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="18" class="chart-row">

      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>设备分类占比</h3>
              <span>各类设备数量分布情况</span>
            </div>
          </div>

          <div ref="categoryChartRef" class="chart-container"></div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-header">
            <div>
              <h3>设备状态统计</h3>
              <span>不同设备状态的数量分布</span>
            </div>
          </div>

          <div ref="statusChartRef" class="chart-container"></div>
        </div>
      </el-col>

    </el-row>

    <!-- 最近设备 -->
    <div class="panel table-panel">
      <div class="panel-header">
        <div>
          <h3>最近添加的设备</h3>
          <span>系统中最新录入的设备信息</span>
        </div>

        <el-button
          type="primary"
          link
          @click="$router.push('/equipment')"
        >
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-table
        :data="recentEquipment"
        stripe
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无设备数据"
      >
        <el-table-column prop="equipmentNo" label="设备编号" min-width="120" />

        <el-table-column prop="equipmentName" label="设备名称" min-width="150" />

        <el-table-column prop="brand" label="品牌" min-width="100" />

        <el-table-column prop="model" label="型号" min-width="100" />

        <el-table-column prop="location" label="存放位置" min-width="120" />

        <el-table-column label="设备状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ row.status || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 最近维修 -->
    <div class="panel table-panel">
      <div class="panel-header">
        <div>
          <h3>最近维修记录</h3>
          <span>设备维修情况与处理进度</span>
        </div>

        <el-button
          type="primary"
          link
          @click="$router.push('/repair')"
        >
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-table
        :data="recentRepairs"
        stripe
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无维修记录"
      >
        <el-table-column prop="equipmentNo" label="设备编号" min-width="120" />

        <el-table-column prop="equipmentName" label="设备名称" min-width="140" />

        <el-table-column prop="faultDescription" label="故障描述" min-width="180" show-overflow-tooltip />

        <el-table-column prop="repairPerson" label="维修人员" min-width="110" />

        <el-table-column label="维修状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getRepairStatusType(row.repairStatus)">
              {{ row.repairStatus || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="reportTime" label="报修时间" min-width="170" />
      </el-table>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

import {
  Refresh,
  ArrowRight,
  Box,
  CircleCheck,
  Tools,
  Grid,
  User
} from '@element-plus/icons-vue'

import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()

// 数据
const equipmentList = ref([])
const categoryList = ref([])
const repairList = ref([])
const userList = ref([])


// 正常设备数量
const normalCount = computed(() => {
  return equipmentList.value.filter(
    item => item.status === '正常'
  ).length
})

// 维修中及待维修设备数量
const repairingCount = computed(() => {
  return equipmentList.value.filter(
    item => ['维修中', '待维修'].includes(item.status)
  ).length
})


const loading = ref(false)

// 图表引用
const categoryChartRef = ref(null)
const statusChartRef = ref(null)

let categoryChart = null
let statusChart = null

// 兼容统一 ApiResponse 和常见列表结构
const getList = (result) => {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }

  const data = result.data

  if (Array.isArray(data)) return data
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list

  return []
}

// 统计数据
const statistics = computed(() => [
  {
    title: '设备总数',
    value: equipmentList.value.length,
    description: '系统登记的设备数量',
    icon: Box,
    color: '#2878BD',
    background: '#E8F2FC'
  },
  {
    title: '正常设备',
    value: normalCount.value,
    description: '当前状态正常的设备',
    icon: CircleCheck,
    color: '#19A879',
    background: '#E7F7F0'
  },
  {
    title: '维修中设备',
    value: repairingCount.value,
    description: '待维修及维修中的设备',
    icon: Tools,
    color: '#E49A35',
    background: '#FFF4E4'
  },
  {
    title: '设备分类',
    value: categoryList.value.length,
    description: '系统已建立的设备分类',
    icon: Grid,
    color: '#8B70D6',
    background: '#F0EBFC'
  },
  {
    title: '维修记录',
    value: repairList.value.length,
    description: '系统累计维修记录',
    icon: Tools,
    color: '#3985C7',
    background: '#E8F2FC'
  },
  {
    title: '系统用户',
    value: userList.value.length,
    description: '系统注册用户数量',
    icon: User,
    color: '#19A879',
    background: '#E7F7F0'
  }
])

// 获取数据
const loadDashboard = async () => {
  loading.value = true

  try {
    // 获取设备、分类、维修数据
    const [equipmentRes, categoryRes, repairRes] =
      await Promise.all([
        request.get('/api/equipment'),
        request.get('/api/category'),
        request.get('/api/repair')
      ])

    equipmentList.value = getList(equipmentRes)
    categoryList.value = getList(categoryRes)
    repairList.value = getList(repairRes)

    // 获取用户数据
    // 用户接口仅管理员可以访问
    try {
      const userRes = await request.get('/api/user')
      userList.value = getList(userRes)
    } catch (error) {
      userList.value = []

      if (error?.response?.status === 403) {
        console.info('当前用户没有查看用户统计的权限')
      } else {
        console.warn('用户统计数据获取失败：', error)
      }
    }

    await nextTick()
    renderCharts()

  } catch (error) {
    console.error('首页数据加载失败：', error)
    ElMessage.error('首页数据加载失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

// 设备状态颜色
const getStatusType = (status) => {
  const map = {
    正常: 'success',
    维修中: 'warning',
    待维修: 'danger',
    报废: 'info'
  }

  return map[status] || 'info'
}

// 维修状态颜色
const getRepairStatusType = (status) => {
  const map = {
    已完成: 'success',
    维修中: 'warning',
    待维修: 'danger'
  }

  return map[status] || 'info'
}

// 最近添加的设备
const recentEquipment = computed(() => {
  return [...equipmentList.value]
    .sort((a, b) =>
      new Date(b.purchaseDate || 0) - new Date(a.purchaseDate || 0)
    )
    .slice(0, 5)
})


// 最近维修记录
const recentRepairs = computed(() => {
  return [...repairList.value]
    .sort((a, b) => {
      return new Date(b.reportTime || 0) -
             new Date(a.reportTime || 0)
    })
    .slice(0, 5)
})


// 绘制图表
const renderCharts = () => {
  if (!categoryChartRef.value || !statusChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }

  // 分类统计
  const categoryCounts = new Map()

  categoryList.value.forEach(category => {
    categoryCounts.set(category.categoryName, 0)
  })

  equipmentList.value.forEach(item => {
    const category = categoryList.value.find(
      c => String(c.id ?? c.categoryId) === String(item.categoryId)
    )

    const name = category?.categoryName || '未分类'

    categoryCounts.set(name, (categoryCounts.get(name) || 0) + 1)
  })

  const categoryData = [...categoryCounts.entries()].map(([name, value]) => ({
    name,
    value
  }))

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 台 ({d}%)'
    },
    legend: {
      bottom: 0,
      type: 'scroll'
    },
    series: [{
      type: 'pie',
      radius: ['43%', '68%'],
      center: ['50%', '44%'],
      data: categoryData.length ? categoryData : [],
      label: {
        formatter: '{b}\n{d}%'
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 15,
          shadowColor: 'rgba(0,0,0,0.15)'
        }
      }
    }]
  })

  // 设备状态统计
  const statusNames = ['正常', '待维修', '维修中', '报废', '其他']

  const statusCounts = statusNames.map(name => {
    if (name === '其他') {
      return equipmentList.value.filter(item =>
        !statusNames.slice(0, 4).includes(item.status)
      ).length
    }

    return equipmentList.value.filter(item => item.status === name).length
  })

  statusChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: 35,
      right: 20,
      top: 25,
      bottom: 35
    },
    xAxis: {
      type: 'category',
      data: statusNames,
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [{
      type: 'bar',
      data: statusCounts,
      barWidth: 35,
      itemStyle: {
        borderRadius: [6, 6, 0, 0]
      },
      label: {
        show: true,
        position: 'top'
      }
    }]
  })
}

// 浏览器窗口变化时调整图表
const handleResize = () => {
  categoryChart?.resize()
  statusChart?.resize()
}

onMounted(async () => {
  await loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)

  categoryChart?.dispose()
  statusChart?.dispose()

  categoryChart = null
  statusChart = null
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.page-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
}

.page-heading h2 {
  color: #233d59;
  font-size: 23px;
  font-weight: 700;
}

.page-heading p {
  margin-top: 9px;
  color: #8998aa;
  font-size: 13px;
}

.statistics-row,
.chart-row {
  row-gap: 18px;
}

.stat-card {
  min-height: 142px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 23px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 12px;
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 22px rgba(31,65,100,0.07);
}

.stat-info span,
.stat-info strong,
.stat-info small {
  display: block;
}

.stat-info span {
  color: #8190a2;
  font-size: 13px;
}

.stat-info strong {
  margin-top: 12px;
  color: #233d59;
  font-size: 30px;
  font-weight: 700;
}

.stat-info small {
  margin-top: 8px;
  color: #a2afbd;
  font-size: 11px;
}

.stat-icon {
  width: 49px;
  height: 49px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.panel {
  padding: 23px;
  background: #ffffff;
  border: 1px solid #edf1f5;
  border-radius: 12px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.panel-header h3 {
  color: #344b63;
  font-size: 15px;
  font-weight: 700;
}

.panel-header span {
  display: block;
  margin-top: 7px;
  color: #9aa8b7;
  font-size: 11px;
}

.chart-container {
  height: 280px;
  width: 100%;
}

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

@media (max-width: 768px) {
  .page-heading {
    align-items: flex-start;
  }

  .stat-card {
    padding: 15px;
  }

  .stat-info strong {
    font-size: 24px;
  }

  .panel {
    padding: 15px;
  }
}
</style>
