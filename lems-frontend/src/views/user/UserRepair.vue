
<template>
  <div class="repair-page">

    <!-- 页面标题 -->
    <div class="page-heading">
      <div>
        <h2>在线报修</h2>
        <p>提交设备故障信息，管理员将及时安排维修处理。</p>
      </div>
      <el-tag type="success" effect="light">用户报修服务</el-tag>
    </div>

    <!-- 报修流程 -->
    <div class="steps-card">
      <el-steps :active="1" align-center finish-status="success">
        <el-step title="选择设备" description="选择故障设备" />
        <el-step title="填写信息" description="描述设备故障" />
        <el-step title="提交申请" description="等待管理员处理" />
      </el-steps>
    </div>

    <el-row :gutter="20">

      <!-- 报修表单 -->
      <el-col :xs="24" :lg="16">
        <div class="form-card">
          <div class="card-heading">
            <div>
              <h3>填写报修信息</h3>
              <p>请准确填写设备信息及故障情况</p>
            </div>
            <el-icon :size="25" color="#3478e5">
              <EditPen />
            </el-icon>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            v-loading="loading"
          >

          <!-- 设备选择 -->
          <el-form-item label="选择报修设备" prop="equipmentId">
            <el-select
              v-model="form.equipmentId"
              placeholder="请选择需要维修的设备"
              filterable
              clearable
              style="width: 100%"
              :loading="loading"
            >
              <el-option
                v-for="item in equipmentList"
                :key="item.id"
                :label="`${item.equipmentNo} - ${item.equipmentName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <!-- 设备基本信息 -->
          <div v-if="selectedEquipment" class="equipment-info">
            <div class="equipment-info-title">
              <el-icon><Monitor /></el-icon>
              已选择设备
            </div>

            <el-descriptions :column="2" size="small">
              <el-descriptions-item label="设备编号">
                {{ selectedEquipment.equipmentNo }}
              </el-descriptions-item>

              <el-descriptions-item label="设备名称">
                {{ selectedEquipment.equipmentName }}
              </el-descriptions-item>

              <el-descriptions-item label="品牌">
                {{ selectedEquipment.brand || '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="型号">
                {{ selectedEquipment.model || '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="存放位置">
                {{ selectedEquipment.location || '-' }}
              </el-descriptions-item>

              <el-descriptions-item label="设备状态">
                {{ selectedEquipment.status || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

            <!-- 设备基本信息 -->
            <div v-if="selectedEquipment" class="equipment-info">
              <div class="equipment-info-title">
                <el-icon><Monitor /></el-icon>
                已选择设备
              </div>

              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="设备编号">
                  {{ selectedEquipment.equipmentNo }}
                </el-descriptions-item>

                <el-descriptions-item label="设备名称">
                  {{ selectedEquipment.equipmentName }}
                </el-descriptions-item>

                <el-descriptions-item label="品牌">
                  {{ selectedEquipment.brand || '-' }}
                </el-descriptions-item>

                <el-descriptions-item label="型号">
                  {{ selectedEquipment.model || '-' }}
                </el-descriptions-item>

                <el-descriptions-item label="存放位置">
                  {{ selectedEquipment.location || '-' }}
                </el-descriptions-item>

                <el-descriptions-item label="设备状态">
                  {{ selectedEquipment.status || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <!-- 故障描述 -->
            <el-form-item label="故障描述" prop="faultDescription">
              <el-input
                v-model="form.faultDescription"
                type="textarea"
                :rows="7"
                maxlength="5000"
                show-word-limit
                resize="vertical"
                placeholder="请详细描述设备出现的故障，例如：设备无法正常启动、运行过程中出现异常噪声、显示屏无法显示等。"
              />
            </el-form-item>

            <!-- 提交提示 -->
            <div class="form-notice">
              <el-icon><InfoFilled /></el-icon>
              提交后，维修状态将自动设置为「待维修」，管理员处理后可以在维修记录中查看进度。
            </div>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <el-button @click="resetForm">
                重置
              </el-button>

              <el-button
                type="primary"
                :loading="submitting"
                @click="submitRepair"
              >
                <el-icon><Promotion /></el-icon>
                提交报修申请
              </el-button>
            </div>

          </el-form>
        </div>
      </el-col>

      <!-- 右侧提示 -->
      <el-col :xs="24" :lg="8">

        <div class="tips-card">
          <div class="tips-icon">
            <el-icon :size="30"><Tools /></el-icon>
          </div>

          <h3>报修须知</h3>
          <ul>
            <li>请选择实际发生故障的设备。</li>
            <li>请尽可能详细地描述设备故障现象。</li>
            <li>提交后，系统会自动生成维修记录。</li>
            <li>管理员处理后，可以查看维修状态和结果。</li>
          </ul>
        </div>

        <div class="status-card">
          <h3>维修状态说明</h3>

          <div class="status-item">
            <el-tag type="warning">待维修</el-tag>
            <span>申请已提交，等待管理员安排。</span>
          </div>

          <div class="status-item">
            <el-tag type="primary">维修中</el-tag>
            <span>维修人员正在处理故障。</span>
          </div>

          <div class="status-item">
            <el-tag type="success">已完成</el-tag>
            <span>设备维修工作已经完成。</span>
          </div>
        </div>

      </el-col>
    </el-row>

  </div>
</template>

<script setup>

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  EditPen,
  Monitor,
  InfoFilled,
  Promotion,
  Tools
} from '@element-plus/icons-vue'

import equipmentApi from '../../api/equipment'
import repairApi from '../../api/repair'

const router = useRouter()

const formRef = ref(null)

const loading = ref(false)
const submitting = ref(false)

// 设备列表
const equipmentList = ref([])

// 报修表单
const form = ref({
  equipmentId: null,
  faultDescription: ''
})

// 当前选择的设备
const selectedEquipment = computed(() => {
  return equipmentList.value.find(
    item => Number(item.id) === Number(form.value.equipmentId)
  ) || null
})

// 表单验证
const rules = {
  equipmentId: [
    {
      required: true,
      message: '请选择需要维修的设备',
      trigger: 'change'
    }
  ],
  faultDescription: [
    {
      required: true,
      message: '请填写故障描述',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 5000,
      message: '故障描述长度应为5至5000个字符',
      trigger: 'blur'
    }
  ]
}

// 加载设备列表
async function loadEquipment() {
  loading.value = true

  try {
    const result = await equipmentApi.getAll()

    if (result?.code !== 200) {
      throw new Error(result?.message || '设备列表获取失败')
    }

    equipmentList.value = Array.isArray(result.data)
      ? result.data
      : result.data?.records || result.data?.list || []

  } catch (error) {
    console.error('设备列表加载失败：', error)
    ElMessage.error('设备列表加载失败，请检查服务器连接')
  } finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  formRef.value?.resetFields()

  form.value = {
    equipmentId: null,
    faultDescription: ''
  }
}

// 提交报修
async function submitRepair() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    const result = await repairApi.add({
      equipmentId: Number(form.value.equipmentId),
      faultDescription: form.value.faultDescription.trim()
    })

    if (result?.code !== 200 && result?.code !== 201) {
      throw new Error(result?.message || '报修申请提交失败')
    }

    ElMessage.success('报修申请提交成功！')

    resetForm()

    await router.push('/user/my-repairs')

  } catch (error) {
    console.error('报修提交失败：', error)

    ElMessage.error(
      error.response?.data?.message ||
      error.message ||
      '报修申请提交失败，请稍后重试'
    )
  } finally {
    submitting.value = false
  }
}

onMounted(loadEquipment)

</script>

<style scoped>
.repair-page {
  max-width: 1450px;
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
  color: #263d55;
}

.page-heading p {
  margin: 0;
  color: #98a5b4;
  font-size: 13px;
}

.steps-card,
.form-card,
.tips-card,
.status-card {
  background: white;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.steps-card {
  padding: 26px 35px;
}

.card-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.card-heading h3,
.tips-card h3,
.status-card h3 {
  margin: 0 0 9px;
  color: #30465e;
  font-size: 16px;
}

.card-heading p {
  margin: 0;
  color: #98a5b4;
  font-size: 12px;
}

.equipment-info {
  padding: 18px;
  margin-bottom: 23px;
  border-radius: 9px;
  background: #f5f8fc;
}

.equipment-info-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  color: #3478e5;
  font-size: 13px;
  font-weight: 600;
}

.form-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 13px;
  margin-bottom: 24px;
  border-radius: 8px;
  background: #edf5fc;
  color: #66819a;
  font-size: 12px;
  line-height: 1.8;
}

.form-notice :deep(.el-icon) {
  margin-top: 2px;
  flex-shrink: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 右侧说明 */
.tips-card {
  background: linear-gradient(145deg, #173754, #286a9c);
  color: white;
  border: none;
}

.tips-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 55px;
  height: 55px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: rgba(255,255,255,0.13);
}

.tips-card h3 {
  color: white;
}

.tips-card ul {
  padding-left: 18px;
  margin: 18px 0 0;
}

.tips-card li {
  padding: 7px 0;
  color: #d4e3ef;
  font-size: 12px;
  line-height: 1.7;
}

.status-card {
  padding: 22px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 0;
  border-bottom: 1px solid #f0f2f5;
}

.status-item:last-child {
  border-bottom: none;
}

.status-item span {
  color: #8290a1;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .steps-card,
  .form-card,
  .tips-card,
  .status-card {
    padding: 16px;
  }

  .steps-card {
    padding: 22px 12px;
  }
}
</style>
