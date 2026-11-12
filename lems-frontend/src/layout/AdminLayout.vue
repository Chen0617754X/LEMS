
<template>
  <div class="admin-layout">

    <!-- 左侧导航栏 -->
    <aside class="sidebar" :class="{ collapsed }">

      <div class="sidebar-logo">
        <div class="logo-icon">
          <el-icon :size="24"><Monitor /></el-icon>
        </div>
        <div v-if="!collapsed" class="logo-text">
          <strong>LEMS</strong>
          <span>设备管理系统</span>
        </div>
      </div>

      <div class="menu-title" v-if="!collapsed">工作台</div>

      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        background-color="#173754"
        text-color="#C2D3E3"
        active-text-color="#FFFFFF"
      >
        <el-menu-item index="/">
          <el-icon><Odometer /></el-icon>
          <template #title>系统首页</template>
        </el-menu-item>

        <el-menu-item index="/equipment">
          <el-icon><Box /></el-icon>
          <template #title>设备管理</template>
        </el-menu-item>

        <el-menu-item index="/category">
          <el-icon><Grid /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>

        <el-menu-item index="/repair">
          <el-icon><Tools /></el-icon>
          <template #title>维修管理</template>
        </el-menu-item>

        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-bottom">
        <div class="system-version" v-if="!collapsed">
          <span class="online-dot"></span>
          系统运行正常
          <small>LEMS v1.0</small>
        </div>
      </div>

    </aside>

    <!-- 右侧主体 -->
    <div class="main-container">

      <!-- 顶部导航 -->
      <header class="topbar">

        <div class="topbar-left">
          <el-button
            text
            class="collapse-button"
            @click="collapsed = !collapsed"
          >
            <el-icon :size="20">
              <Expand v-if="collapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>

          <div class="breadcrumb">
            <span>LEMS</span>
            <el-icon><ArrowRight /></el-icon>
            <strong>{{ route.meta.title || '系统首页' }}</strong>
          </div>
        </div>

        <div class="topbar-right">

          <div class="user-info">
            <el-avatar :size="34" class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>

            <div class="user-detail">
              <strong>{{ userStore.username || '用户' }}</strong>
              <span>
                {{ userStore.isAdmin ? '系统管理员' : '普通用户' }}
              </span>
            </div>
          </div>

          <el-dropdown @command="handleCommand">
            <el-button text class="user-dropdown">
              <el-icon><ArrowDown /></el-icon>
            </el-button>

            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

        </div>

      </header>

      <!-- 页面内容 -->
      <main class="page-container">
        <router-view />
      </main>

      <!-- 底部 -->
      <footer class="page-footer">
        LEMS 实验室设备管理系统
        <span>© 2026</span>
      </footer>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  Monitor,
  Odometer,
  Box,
  Grid,
  Tools,
  User,
  UserFilled,
  Expand,
  Fold,
  ArrowRight,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)

// 用户菜单操作
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定退出当前账号吗？',
        '退出登录',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      await userStore.logout()

      ElMessage.success('已安全退出')

      await router.replace('/login')

    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        console.error('退出登录失败：', error)
      }
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f3f6fa;
}

/* 左侧导航 */
.sidebar {
  width: 238px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: #173754;
  transition: width 0.25s;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-logo {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 19px;
  border-bottom: 1px solid rgba(255,255,255,0.09);
}

.logo-icon {
  width: 39px;
  height: 39px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  color: #ffffff;
  background: #2878bd;
}

.logo-text strong {
  display: block;
  color: #ffffff;
  font-size: 20px;
  letter-spacing: 2px;
}

.logo-text span {
  display: block;
  margin-top: 4px;
  color: #a9bfd3;
  font-size: 11px;
}

.menu-title {
  padding: 25px 22px 12px;
  color: #829bb2;
  font-size: 12px;
}

.sidebar :deep(.el-menu) {
  border-right: none;
}

.sidebar :deep(.el-menu-item) {
  height: 50px;
  margin: 4px 10px;
  border-radius: 7px;
}

.sidebar :deep(.el-menu-item:hover) {
  background: #244b6c !important;
}

.sidebar :deep(.el-menu-item.is-active) {
  background: #2878bd !important;
}

.sidebar :deep(.el-menu-item .el-icon) {
  font-size: 19px;
}

.sidebar-bottom {
  margin-top: auto;
  padding: 18px;
}

.system-version {
  padding: 12px;
  border-radius: 8px;
  background: rgba(255,255,255,0.07);
  color: #c5d7e7;
  font-size: 11px;
}

.online-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-right: 6px;
  border-radius: 50%;
  background: #36c98f;
}

.system-version small {
  display: block;
  margin-top: 8px;
  color: #829bb2;
}

/* 主体 */
.main-container {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* 顶部 */
.topbar {
  height: 76px;
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  background: #ffffff;
  border-bottom: 1px solid #e9eef4;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.collapse-button {
  color: #52667b;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #93a1b1;
  font-size: 13px;
}

.breadcrumb strong {
  color: #344b63;
  font-size: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  color: #2878bd;
  background: #eaf3fc;
}

.user-detail strong,
.user-detail span {
  display: block;
}

.user-detail strong {
  color: #344b63;
  font-size: 13px;
}

.user-detail span {
  margin-top: 4px;
  color: #95a3b3;
  font-size: 11px;
}

.user-dropdown {
  color: #718096;
}

/* 内容区 */
.page-container {
  flex: 1;
  padding: 26px 30px;
}

.page-footer {
  padding: 15px 30px;
  color: #9aa8b7;
  font-size: 11px;
  background: #ffffff;
  border-top: 1px solid #edf0f4;
}

.page-footer span {
  float: right;
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }

  .sidebar .logo-text,
  .sidebar .menu-title,
  .sidebar .system-version {
    display: none;
  }

  .topbar {
    padding: 0 12px;
  }

  .page-container {
    padding: 16px;
  }
}
</style>
