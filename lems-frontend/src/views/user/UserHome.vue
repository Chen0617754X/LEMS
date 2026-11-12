
<template>
  <div class="user-home">

    <!-- 欢迎区域 -->
    <section class="welcome-banner">
      <div class="welcome-content">
        <div class="welcome-label">LEMS · LABORATORY EQUIPMENT MANAGEMENT</div>
        <h2>欢迎回来，{{ userStore.username || '用户' }}！</h2>
        <p>欢迎使用实验室设备管理系统，您可以在这里查询设备、提交维修申请和跟踪维修进度。</p>
        <el-button type="primary" @click="goTo('/user/equipment')">
          <el-icon><Search /></el-icon>
          浏览设备
        </el-button>
      </div>
      <div class="welcome-icon">
        <el-icon :size="105"><Monitor /></el-icon>
      </div>
    </section>

    <!-- 数据统计 -->
    <el-row :gutter="18" class="statistics">

      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon blue">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <span>系统设备总数</span>
            <strong>{{ equipmentCount }}</strong>
            <small>实验室登记设备</small>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon orange">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <span>我的报修总数</span>
            <strong>{{ repairCount }}</strong>
            <small>累计提交申请</small>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon purple">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="stat-info">
            <span>维修处理中</span>
            <strong>{{ processingCount }}</strong>
            <small>等待维修或正在维修</small>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card">
          <div class="stat-icon green">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <span>已完成维修</span>
            <strong>{{ completedCount }}</strong>
            <small>已完成的维修申请</small>
          </div>
        </div>
      </el-col>

    </el-row>

    <!-- 主体区域 -->
    <el-row :gutter="18">

      <!-- 快捷服务 -->
      <el-col :xs="24" :lg="14">
        <section class="content-card">
          <div class="section-heading">
            <div>
              <h3>快捷服务</h3>
              <p>选择您需要使用的服务</p>
            </div>
          </div>

          <div class="service-grid">

            <div class="service-item" @click="goTo('/user/equipment')">
              <div class="service-icon blue">
                <el-icon><Search /></el-icon>
              </div>
              <div>
                <strong>设备查询</strong>
                <p>浏览设备信息、型号及存放位置</p>
              </div>
              <el-icon class="service-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="service-item" @click="goTo('/user/repair')">
              <div class="service-icon orange">
                <el-icon><EditPen /></el-icon>
              </div>
              <div>
                <strong>在线报修</strong>
                <p>提交设备故障及维修申请</p>
              </div>
              <el-icon class="service-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="service-item" @click="goTo('/user/my-repairs')">
              <div class="service-icon purple">
                <el-icon><Tickets /></el-icon>
              </div>
              <div>
                <strong>我的维修记录</strong>
                <p>查看报修记录和维修处理进度</p>
              </div>
              <el-icon class="service-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="service-item" @click="goTo('/user/profile')">
              <div class="service-icon green">
                <el-icon><User /></el-icon>
              </div>
              <div>
                <strong>个人信息</strong>
                <p>查看当前账户的基本信息</p>
              </div>
              <el-icon class="service-arrow"><ArrowRight /></el-icon>
            </div>

          </div>
        </section>
      </el-col>

      <!-- 维修状态分布 -->
      <el-col :xs="24" :lg="10">
        <section class="content-card status-card">
          <div class="section-heading">
            <div>
              <h3>维修进度概览</h3>
              <p>个人维修申请状态统计</p>
            </div>
          </div>

          <div class="progress-item">
            <div class="progress-heading">
              <span>待维修</span>
              <strong>{{ pendingCount }} 条</strong>
            </div>
            <el-progress
              :percentage="getPercentage(pendingCount)"
              :stroke-width="10"
              color="#e6a23c"
              :show-text="false"
            />
          </div>

          <div class="progress-item">
            <div class="progress-heading">
              <span>维修中</span>
              <strong>{{ repairingCount }} 条</strong>
            </div>
            <el-progress
              :percentage="getPercentage(repairingCount)"
              :stroke-width="10"
              color="#409eff"
              :show-text="false"
            />
          </div>

          <div class="progress-item">
            <div class="progress-heading">
              <span>已完成</span>
              <strong>{{ completedCount }} 条</strong>
            </div>
            <el-progress
              :percentage="getPercentage(completedCount)"
              :stroke-width="10"
              color="#67c23a"
              :show-text="false"
            />
          </div>

          <div class="status-footer">
            <span>维修申请总数</span>
            <strong>{{ repairCount }}</strong>
          </div>
        </section>
      </el-col>

    </el-row>

    <!-- 最近报修 -->
    <section class="content-card recent-card">
      <div class="section-heading">
        <div>
          <h3>最近报修记录</h3>
          <p>您最近提交的设备维修申请</p>
        </div>

        <el-button
          type="primary"
          link
          @click="goTo('/user/my-repairs')"
        >
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <el-table
        :data="recentRepairs"
        v-loading="loading"
        stripe
        empty-text="暂无维修记录"
        style="width: 100%"
      >
        <el-table-column
          prop="equipmentNo"
          label="设备编号"
          min-width="120"
        />

        <el-table-column
          prop="equipmentName"
          label="设备名称"
          min-width="140"
        />

        <el-table-column
          prop="faultDescription"
          label="故障描述"
          min-width="180"
          show-overflow-tooltip
        />

        <el-table-column label="维修状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.repairStatus)">
              {{ row.repairStatus || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="reportTime"
          label="报修时间"
          min-width="170"
        />
      </el-table>
    </section>

    <!-- 页面底部提示 -->
    <div class="notice">
      <el-icon><InfoFilled /></el-icon>
      如发现设备故障，请及时提交维修申请，以便管理员安排维修处理。
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  Monitor,
  Search,
  Box,
  Document,
  Tools,
  CircleCheck,
  ArrowRight,
  EditPen,
  Tickets,
  User,
  InfoFilled
} from '@element-plus/icons-vue'

import { useUserStore } from '../../stores/user'
import equipmentApi from '../../api/equipment'
import request from '../../api/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const equipmentCount = ref(0)
const repairList = ref([])

// 维修总数
const repairCount = computed(() => repairList.value.length)

// 各维修状态统计
const pendingCount = computed(() =>
  repairList.value.filter(
    item => item.repairStatus === '待维修'
  ).length
)

const repairingCount = computed(() =>
  repairList.value.filter(
    item => item.repairStatus === '维修中'
  ).length
)

const completedCount = computed(() =>
  repairList.value.filter(
    item => item.repairStatus === '已完成'
  ).length
)

const processingCount = computed(() =>
  pendingCount.value + repairingCount.value
)

// 最近五条维修记录
const recentRepairs = computed(() =>
  [...repairList.value]
    .sort((a, b) =>
      new Date(b.reportTime || 0) - new Date(a.reportTime || 0)
    )
    .slice(0, 5)
)

// 统一检查接口返回结果
function getResponseData(result) {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }

  return result.data
}

// 获取设备数量
async function loadEquipment() {
  try {
    const result = await equipmentApi.getAll()
    const list = getResponseData(result) || []

    equipmentCount.value = list.length
  } catch (error) {
    console.error('设备统计获取失败：', error)
  }
}

// 获取当前用户维修记录
async function loadRepairs() {
  loading.value = true

  try {
    const result = await request.get('/api/repair/my')

    repairList.value = getResponseData(result) || []
  } catch (error) {
    console.error('维修记录获取失败：', error)
    ElMessage.warning('维修记录加载失败，请刷新页面重试')
  } finally {
    loading.value = false
  }
}

// 维修状态对应的进度百分比
function getPercentage(count) {
  if (repairCount.value === 0) {
    return 0
  }

  return Math.round(count / repairCount.value * 100)
}

// 状态标签颜色
function getStatusType(status) {
  const types = {
    '待维修': 'warning',
    '维修中': 'primary',
    '已完成': 'success'
  }

  return types[status] || 'info'
}

// 页面跳转
function goTo(path) {
  router.push(path)
}

// 初始化
onMounted(() => {
  loadEquipment()
  loadRepairs()
})
</script>

<style scoped>
.user-home {
  max-width: 1500px;
  margin: 0 auto;
}

/* 欢迎区域 */
.welcome-banner {
  min-height: 205px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30px 40px;
  margin-bottom: 22px;
  border-radius: 14px;
  overflow: hidden;
  color: white;
  background: linear-gradient(115deg, #173754, #286a9c);
}

.welcome-content {
  max-width: 680px;
}

.welcome-label {
  margin-bottom: 14px;
  font-size: 10px;
  letter-spacing: 2px;
  color: #b9d4e9;
}

.welcome-content h2 {
  margin: 0 0 12px;
  font-size: 26px;
}

.welcome-content p {
  margin: 0 0 22px;
  color: #d2e2ee;
  font-size: 13px;
  line-height: 1.8;
}

.welcome-content :deep(.el-button) {
  border: none;
  background: white;
  color: #235c89;
}

.welcome-icon {
  margin-right: 35px;
  color: rgba(255,255,255,0.16);
}

/* 数据统计 */
.statistics {
  margin-bottom: 6px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  min-height: 112px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  background: white;
}

.stat-icon,
.service-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 11px;
}

.stat-icon {
  width: 47px;
  height: 47px;
  font-size: 23px;
}

.blue {
  color: #3478e5;
  background: #eaf2ff;
}

.orange {
  color: #df9a36;
  background: #fff5e6;
}

.purple {
  color: #8764d7;
  background: #f2edff;
}

.green {
  color: #38a878;
  background: #e8f7ef;
}

.stat-info span,
.stat-info strong,
.stat-info small {
  display: block;
}

.stat-info span {
  color: #8593a5;
  font-size: 12px;
}

.stat-info strong {
  margin: 5px 0;
  color: #293e56;
  font-size: 26px;
}

.stat-info small {
  color: #a4afbd;
  font-size: 11px;
}

/* 内容卡片 */
.content-card {
  padding: 23px;
  margin-bottom: 20px;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  background: white;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 23px;
}

.section-heading h3 {
  margin: 0 0 8px;
  color: #30465e;
  font-size: 16px;
}

.section-heading p {
  margin: 0;
  color: #9aa7b6;
  font-size: 12px;
}

/* 快捷服务 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 91px;
  padding: 15px;
  border: 1px solid #edf0f5;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.service-item:hover {
  border-color: #a9c9ed;
  background: #f7faff;
  transform: translateY(-2px);
}

.service-icon {
  width: 43px;
  height: 43px;
  font-size: 21px;
}

.service-item strong {
  color: #354b63;
  font-size: 13px;
}

.service-item p {
  margin: 7px 0 0;
  color: #9ba7b5;
  font-size: 11px;
  line-height: 1.5;
}

.service-arrow {
  margin-left: auto;
  color: #a9b5c2;
}

/* 维修进度 */
.progress-item {
  margin-bottom: 25px;
}

.progress-heading {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #718197;
  font-size: 13px;
}

.progress-heading strong {
  color: #344b63;
}

.status-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 18px;
  border-top: 1px solid #edf0f5;
  color: #8997a8;
  font-size: 12px;
}

.status-footer strong {
  color: #344b63;
  font-size: 19px;
}

/* 最近记录 */
.recent-card {
  margin-top: 2px;
}

.notice {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 14px 18px;
  margin-bottom: 15px;
  border-radius: 9px;
  color: #68819b;
  background: #edf5fc;
  font-size: 12px;
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 25px;
  }

  .welcome-icon {
    display: none;
  }

  .service-grid {
    grid-template-columns: 1fr;
  }

  .content-card {
    padding: 16px;
  }
}
</style>
