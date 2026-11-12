
<template>
  <div class="user-page">

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>统一管理系统用户账号、基本资料和账号状态。</p>
      </div>

      <el-button
        type="primary"
        size="large"
        @click="openAddDialog"
      >
        <el-icon><Plus /></el-icon>
        新增用户
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

    <!-- 用户列表 -->
    <div class="content-panel">

      <div class="panel-header">
        <div>
          <h3>用户信息列表</h3>
          <span>查看、搜索和维护系统用户信息</span>
        </div>

        <el-button
          text
          :loading="loading"
          @click="loadUsers"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area">

        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索用户名、姓名或手机号"
          clearable
          style="width: 260px"
          @keyup.enter="currentPage = 1"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select
          v-model="searchForm.role"
          placeholder="全部角色"
          clearable
          style="width: 140px"
        >
          <el-option label="管理员" value="ADMIN" />
          <el-option label="普通用户" value="USER" />
        </el-select>

        <el-select
          v-model="searchForm.status"
          placeholder="全部状态"
          clearable
          style="width: 140px"
        >
          <el-option label="正常" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>

        <el-button type="primary" @click="currentPage = 1">
          <el-icon><Search /></el-icon>
          查询
        </el-button>

        <el-button @click="resetSearch">
          重置
        </el-button>

      </div>

      <!-- 用户表格 -->
      <el-table
        :data="paginatedUsers"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无用户数据"
      >

        <el-table-column
          prop="id"
          label="用户 ID"
          width="85"
          align="center"
        />

        <el-table-column
          label="用户信息"
          min-width="190"
        >
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar
                :size="38"
                :style="{
                  background: row.role === 'ADMIN'
                    ? '#e9e3fc'
                    : '#e5f1fc',
                  color: row.role === 'ADMIN'
                    ? '#8064c5'
                    : '#2878bd'
                }"
              >
                {{ (row.realName || row.username || '?').charAt(0) }}
              </el-avatar>

              <div>
                <strong>{{ row.realName || '未填写姓名' }}</strong>
                <small>{{ row.username }}</small>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="phone"
          label="手机号码"
          min-width="135"
        >
          <template #default="{ row }">
            {{ row.phone || '未填写' }}
          </template>
        </el-table-column>

        <el-table-column
          label="用户角色"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.role === 'ADMIN' ? 'warning' : 'primary'"
              effect="light"
            >
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="账号状态"
          width="110"
          align="center"
        >
          <template #default="{ row }">
            <el-tag
              :type="Number(row.status) === 1 ? 'success' : 'danger'"
            >
              {{ Number(row.status) === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          label="注册时间"
          min-width="175"
        >
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column
          label="操作"
          width="210"
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

            <el-button
              type="primary"
              link
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>

            <el-dropdown
              @command="command => handleCommand(command, row)"
            >
              <el-button type="primary" link>
                更多
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-button>

              <template #dropdown>
                <el-dropdown-menu>

                  <el-dropdown-item
                    :command="Number(row.status) === 1 ? 'disable' : 'enable'"
                  >
                    {{ Number(row.status) === 1 ? '禁用账号' : '启用账号' }}
                  </el-dropdown-item>

                  <el-dropdown-item
                    command="delete"
                    divided
                    style="color: #f56c6c"
                  >
                    删除用户
                  </el-dropdown-item>

                </el-dropdown-menu>
              </template>
            </el-dropdown>

          </template>
        </el-table-column>

      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <span>共 {{ filteredUsers.length }} 条用户记录</span>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredUsers.length"
          layout="sizes, prev, pager, next, jumper"
          background
        />
      </div>

    </div>

    <!-- 新增用户 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增用户"
      width="520px"
      destroy-on-close
      @closed="resetAddForm"
    >

      <el-alert
        title="新用户将自动设置为普通用户，账号默认启用。"
        type="info"
        :closable="false"
        show-icon
        class="dialog-alert"
      />

      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addRules"
        label-width="95px"
      >

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="addForm.username"
            placeholder="4到20位字母、数字或下划线"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="登录密码" prop="password">
          <el-input
            v-model="addForm.password"
            type="password"
            show-password
            placeholder="请输入8到72位密码"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="addForm.realName"
            placeholder="请输入真实姓名"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input
            v-model="addForm.phone"
            placeholder="请输入手机号码"
            maxlength="20"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitAdd"
        >
          确认新增
        </el-button>
      </template>

    </el-dialog>

    <!-- 编辑用户 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="520px"
      destroy-on-close
      @closed="resetEditForm"
    >

      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="95px"
      >

        <el-form-item label="用户名">
          <el-input
            :model-value="editForm.username"
            disabled
          />
        </el-form-item>

        <el-form-item label="用户角色">
          <el-input
            :model-value="editForm.role === 'ADMIN' ? '管理员' : '普通用户'"
            disabled
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="editForm.realName"
            placeholder="请输入真实姓名"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input
            v-model="editForm.phone"
            placeholder="请输入手机号码"
            maxlength="20"
          />
        </el-form-item>

      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="submitLoading"
          @click="submitEdit"
        >
          保存修改
        </el-button>
      </template>

    </el-dialog>

    <!-- 用户详情 -->
    <el-dialog
      v-model="detailVisible"
      title="用户详细信息"
      width="520px"
    >

      <el-descriptions
        v-if="currentUser"
        :column="1"
        border
      >

        <el-descriptions-item label="用户 ID">
          {{ currentUser.id }}
        </el-descriptions-item>

        <el-descriptions-item label="用户名">
          {{ currentUser.username }}
        </el-descriptions-item>

        <el-descriptions-item label="真实姓名">
          {{ currentUser.realName || '未填写' }}
        </el-descriptions-item>

        <el-descriptions-item label="手机号码">
          {{ currentUser.phone || '未填写' }}
        </el-descriptions-item>

        <el-descriptions-item label="用户角色">
          {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
        </el-descriptions-item>

        <el-descriptions-item label="账号状态">
          <el-tag :type="Number(currentUser.status) === 1 ? 'success' : 'danger'">
            {{ Number(currentUser.status) === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="注册时间">
          {{ formatDate(currentUser.createTime) }}
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
  User,
  UserFilled,
  CircleCheck,
  Warning,
  ArrowDown
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'
import userApi from '../api/user'

// 当前用户
const userStore = useUserStore()

// 数据
const userList = ref([])

const loading = ref(false)
const submitLoading = ref(false)

// 搜索
const searchForm = reactive({
  keyword: '',
  role: '',
  status: null
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailVisible = ref(false)

const addFormRef = ref(null)
const editFormRef = ref(null)

const currentUser = ref(null)

// 新增表单
const createAddForm = () => ({
  username: '',
  password: '',
  realName: '',
  phone: ''
})

const addForm = reactive(createAddForm())

// 编辑表单
const createEditForm = () => ({
  id: null,
  username: '',
  realName: '',
  phone: '',
  role: ''
})

const editForm = reactive(createEditForm())

// 新增验证
const addRules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    },
    {
      pattern: /^[a-zA-Z0-9_]{4,20}$/,
      message: '用户名必须为4到20位字母、数字或下划线',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入登录密码',
      trigger: 'blur'
    },
    {
      min: 8,
      max: 72,
      message: '密码长度必须为8到72位',
      trigger: 'blur'
    }
  ],
  realName: [
    {
      max: 50,
      message: '真实姓名不能超过50个字符',
      trigger: 'blur'
    }
  ],
  phone: [
    {
      max: 20,
      message: '手机号码不能超过20个字符',
      trigger: 'blur'
    }
  ]
}

// 编辑验证
const editRules = {
  realName: [
    {
      max: 50,
      message: '真实姓名不能超过50个字符',
      trigger: 'blur'
    }
  ],
  phone: [
    {
      max: 20,
      message: '手机号码不能超过20个字符',
      trigger: 'blur'
    }
  ]
}

// 解析接口响应
const extractList = result => {
  if (result?.code !== 200) {
    throw new Error(result?.message || '数据获取失败')
  }

  if (Array.isArray(result.data)) {
    return result.data
  }

  return result.data?.records || result.data?.list || []
}

// 统计
const statistics = computed(() => [
  {
    title: '用户总数',
    value: userList.value.length,
    description: '系统注册用户',
    icon: User,
    color: '#2878bd',
    background: '#e8f2fc'
  },
  {
    title: '管理员',
    value: userList.value.filter(
      item => item.role === 'ADMIN'
    ).length,
    description: '系统管理账号',
    icon: UserFilled,
    color: '#8b70d6',
    background: '#f0ebfc'
  },
  {
    title: '普通用户',
    value: userList.value.filter(
      item => item.role === 'USER'
    ).length,
    description: '普通系统账号',
    icon: CircleCheck,
    color: '#19a879',
    background: '#e7f7f0'
  },
  {
    title: '禁用用户',
    value: userList.value.filter(
      item => Number(item.status) === 0
    ).length,
    description: '当前已禁用账号',
    icon: Warning,
    color: '#e49a35',
    background: '#fff4e4'
  }
])

// 搜索过滤
const filteredUsers = computed(() => {
  const keyword = searchForm.keyword.trim().toLowerCase()

  return userList.value.filter(item => {

    const matchKeyword = !keyword ||
      String(item.username || '').toLowerCase().includes(keyword) ||
      String(item.realName || '').toLowerCase().includes(keyword) ||
      String(item.phone || '').toLowerCase().includes(keyword)

    const matchRole = !searchForm.role ||
      item.role === searchForm.role

    const matchStatus = searchForm.status === null ||
      Number(item.status) === searchForm.status

    return matchKeyword && matchRole && matchStatus
  })
})

// 分页数据
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value

  return filteredUsers.value.slice(
    start,
    start + pageSize.value
  )
})

// 时间格式
const formatDate = value => {
  if (!value) return '-'

  return String(value).replace('T', ' ').slice(0, 19)
}

// 加载用户
const loadUsers = async () => {
  loading.value = true

  try {
    const result = await userApi.getAll()

    userList.value = extractList(result)

    currentPage.value = 1

  } catch (error) {
    console.error('用户数据加载失败：', error)

    if (error?.response?.status === 403) {
      ElMessage.error('当前账号没有管理员权限')
    } else if (error?.response?.status === 401) {
      ElMessage.error('登录状态已失效，请重新登录')
    } else {
      ElMessage.error('用户数据加载失败，请检查后端接口')
    }
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = null
  currentPage.value = 1
}

// 打开新增弹窗
const openAddDialog = () => {
  Object.assign(addForm, createAddForm())
  addDialogVisible.value = true
}

// 重置新增表单
const resetAddForm = () => {
  addFormRef.value?.clearValidate()
  Object.assign(addForm, createAddForm())
}

// 新增用户
const submitAdd = async () => {
  try {
    await addFormRef.value.validate()

    submitLoading.value = true

    const data = {
      username: addForm.username.trim(),
      password: addForm.password,
      realName: addForm.realName.trim(),
      phone: addForm.phone.trim()
    }

    const result = await userApi.register(data)

    if (result.code !== 200 && result.code !== 201) {
      throw new Error(result.message || '新增用户失败')
    }

    ElMessage.success('用户新增成功')

    addDialogVisible.value = false

    await loadUsers()

  } catch (error) {
    console.error('新增用户失败：', error)

    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error?.message && error.message !== 'validate') {
      ElMessage.error(error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// 编辑用户
const openEditDialog = async row => {
  try {
    const result = await userApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '获取用户信息失败')
    }

    Object.assign(
      editForm,
      createEditForm(),
      result.data
    )

    editDialogVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('获取用户信息失败')
  }
}

// 重置编辑表单
const resetEditForm = () => {
  editFormRef.value?.clearValidate()
  Object.assign(editForm, createEditForm())
}

// 提交修改
const submitEdit = async () => {
  try {
    await editFormRef.value.validate()

    submitLoading.value = true

    const data = {
      realName: editForm.realName.trim(),
      phone: editForm.phone.trim()
    }

    const result = await userApi.update(
      editForm.id,
      data
    )

    if (result.code !== 200) {
      throw new Error(result.message || '修改用户失败')
    }

    ElMessage.success('用户信息修改成功')

    editDialogVisible.value = false

    await loadUsers()

  } catch (error) {
    console.error('修改用户失败：', error)

    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else if (error?.message && error.message !== 'validate') {
      ElMessage.error(error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
const viewDetail = async row => {
  try {
    const result = await userApi.getById(row.id)

    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || '获取用户详情失败')
    }

    currentUser.value = result.data
    detailVisible.value = true

  } catch (error) {
    console.error(error)
    ElMessage.error('获取用户详情失败')
  }
}

// 启用或禁用账号
const changeStatus = async (row, status) => {
  const action = status === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定${action}用户「${row.username}」吗？`,
      `${action}确认`,
      {
        confirmButtonText: `确认${action}`,
        cancelButtonText: '取消',
        type: status === 1 ? 'info' : 'warning'
      }
    )

    const result = await userApi.updateStatus(
      row.id,
      status
    )

    if (result.code !== 200) {
      throw new Error(result.message || `${action}失败`)
    }

    ElMessage.success(`用户已${action}`)

    await loadUsers()

  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
      ElMessage.error(
        error?.response?.data?.message || `${action}用户失败`
      )
    }
  }
}

// 删除用户
const handleDelete = async row => {
  // 防止删除当前登录账号
  if (String(row.id) === String(userStore.userInfo?.id)) {
    ElMessage.warning('不能删除当前登录账号')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.username}」吗？此操作不可恢复。`,
      '删除用户确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await userApi.delete(row.id)

    if (result.code !== 200) {
      throw new Error(result.message || '删除用户失败')
    }

    ElMessage.success('用户删除成功')

    await loadUsers()

  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
      ElMessage.error(
        error?.response?.data?.message || '删除用户失败'
      )
    }
  }
}

// 更多操作
const handleCommand = (command, row) => {
  if (command === 'enable') {
    changeStatus(row, 1)
  } else if (command === 'disable') {
    changeStatus(row, 0)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

// 初始化
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-page {
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
  align-items: center;
  justify-content: center;
  border-radius: 11px;
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
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  gap: 11px;
}

.user-info strong,
.user-info small {
  display: block;
}

.user-info strong {
  color: #344b63;
  font-size: 13px;
}

.user-info small {
  margin-top: 5px;
  color: #9aa8b7;
  font-size: 11px;
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

.dialog-alert {
  margin-bottom: 22px;
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
