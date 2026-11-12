
<template>
  <div class="login-page">

    <!-- 左侧品牌展示区 -->
    <section class="brand-section">
      <div class="brand-content">

        <div class="brand-logo">
          <div class="logo-icon">
            <el-icon :size="30"><Monitor /></el-icon>
          </div>
          <div>
            <div class="logo-name">LEMS</div>
            <div class="logo-subtitle">EQUIPMENT MANAGEMENT SYSTEM</div>
          </div>
        </div>

        <div class="brand-main">
          <div class="brand-tag">LABORATORY MANAGEMENT</div>

          <h1>
            智慧管理实验室设备
            <br />
            <span>让设备管理更加高效</span>
          </h1>

          <p class="brand-description">
            集设备管理、分类管理、维修维护与用户权限管理于一体，
            为实验室提供便捷、安全、高效的信息化管理服务。
          </p>

          <div class="feature-list">
            <div class="feature-item">
              <el-icon><Box /></el-icon>
              <div>
                <strong>设备管理</strong>
                <span>设备信息统一维护</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon><Collection /></el-icon>
              <div>
                <strong>分类管理</strong>
                <span>设备分类清晰管理</span>
              </div>
            </div>

            <div class="feature-item">
              <el-icon><Tools /></el-icon>
              <div>
                <strong>维修管理</strong>
                <span>维修过程及时跟踪</span>
              </div>
            </div>
          </div>
        </div>

        <div class="brand-footer">
          Laboratory Equipment Management System
          <span>© 2026 LEMS</span>
        </div>

      </div>

      <!-- 装饰背景 -->
      <div class="circle circle-one"></div>
      <div class="circle circle-two"></div>
      <div class="circle circle-three"></div>
    </section>

    <!-- 右侧登录区 -->
    <section class="form-section">

      <div class="form-container">

        <div class="form-heading">
          <div class="welcome-tag">WELCOME BACK</div>
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息，进入 LEMS 管理系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >

          <el-form-item prop="username">
            <label class="form-label">用户名</label>

            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
              :prefix-icon="User"
              autocomplete="username"
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="form-label">登录密码</label>

            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入登录密码"
              size="large"
              show-password
              :prefix-icon="Lock"
              autocomplete="current-password"
            />
          </el-form-item>

          <div class="login-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>管理员与普通用户使用统一登录入口</span>
          </div>

          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '正在登录...' : '登 录' }}
            <el-icon v-if="!loading"><Right /></el-icon>
          </el-button>

        </el-form>

        <div class="form-bottom">
          <div class="security-info">
            <el-icon><Lock /></el-icon>
            <span>安全登录 · 权限管理 · 数据保护</span>
          </div>

          <p>
            尚未拥有账号？
            <span>请联系系统管理员开通</span>
          </p>
        </div>

      </div>

      <div class="right-footer">
        LEMS 实验室设备管理系统
      </div>

    </section>

  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  Monitor,
  Box,
  Collection,
  Tools,
  User,
  Lock,
  InfoFilled,
  Right
} from '@element-plus/icons-vue'

import { useUserStore } from '../stores/user'

// 路由
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)

// 登录按钮状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
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
      message: '密码长度为8到72个字符',
      trigger: 'blur'
    }
  ]
}

// 登录操作
const handleLogin = async () => {

  if (loading.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()

    loading.value = true

    // 调用 Pinia 登录方法
    const result = await userStore.login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })

    if (result.code === 200) {

      ElMessage.success('登录成功，欢迎进入 LEMS 系统！')

      // 跳转系统首页
      // 根据用户角色跳转
        if (userStore.isAdmin) {
        await router.push('/')
        } else {
        await router.push('/user/home')
        }

    } else {

      ElMessage.error(result.message || '登录失败')
    }

  } catch (error) {

    // Axios 请求错误已经由响应拦截器处理
    // 表单验证错误不需要额外提示
    if (error?.response) {
      console.error('登录请求失败：', error.response.data)
    } else if (error?.message) {
      console.error('登录失败：', error.message)
    }

  } finally {

    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: #f4f7fb;
}

/* 左侧品牌区域 */
.brand-section {
  position: relative;
  width: 54%;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(145deg, #102e50 0%, #164d7e 55%, #2376ae 100%);
  color: #ffffff;
}

.brand-content {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 55px 9%;
}

/* Logo */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 52px;
  height: 52px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 13px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
}

.logo-name {
  font-size: 27px;
  font-weight: 800;
  letter-spacing: 3px;
}

.logo-subtitle {
  margin-top: 4px;
  font-size: 9px;
  letter-spacing: 1.5px;
  color: #b9d8f2;
}

/* 品牌介绍 */
.brand-main {
  margin-top: auto;
  margin-bottom: auto;
  max-width: 620px;
  padding: 55px 0;
}

.brand-tag {
  display: inline-block;
  padding: 8px 14px;
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 20px;
  font-size: 11px;
  letter-spacing: 2px;
  color: #b8dcff;
}

.brand-main h1 {
  margin-top: 30px;
  font-size: clamp(32px, 3.5vw, 48px);
  line-height: 1.5;
  font-weight: 700;
  letter-spacing: 2px;
}

.brand-main h1 span {
  color: #8ccaff;
}

.brand-description {
  margin-top: 22px;
  max-width: 520px;
  color: #d1e3f3;
  font-size: 15px;
  line-height: 2;
}

/* 功能介绍 */
.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  min-width: 155px;
  border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.08);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.feature-item .el-icon {
  font-size: 25px;
  color: #8ccaff;
}

.feature-item strong,
.feature-item span {
  display: block;
}

.feature-item strong {
  font-size: 13px;
}

.feature-item span {
  margin-top: 5px;
  font-size: 10px;
  color: #c1d8ec;
}

.brand-footer {
  display: flex;
  justify-content: space-between;
  color: #a9c9e3;
  font-size: 11px;
}

/* 装饰圆形 */
.circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.1);
}

.circle-one {
  width: 500px;
  height: 500px;
  right: -230px;
  top: 8%;
}

.circle-two {
  width: 350px;
  height: 350px;
  right: -150px;
  top: 18%;
}

.circle-three {
  width: 200px;
  height: 200px;
  right: -80px;
  top: 28%;
  background: rgba(255,255,255,0.025);
}

/* 右侧登录区域 */
.form-section {
  width: 46%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  background: #f4f7fb;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

.form-heading {
  margin-bottom: 38px;
}

.welcome-tag {
  color: #2878bd;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 3px;
}

.form-heading h2 {
  margin-top: 16px;
  color: #193653;
  font-size: 34px;
  font-weight: 700;
}

.form-heading p {
  margin-top: 12px;
  color: #8492a6;
  font-size: 14px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
}

.form-label {
  display: block;
  margin-bottom: 10px;
  color: #344b63;
  font-size: 14px;
  font-weight: 600;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 48px;
  border-radius: 9px;
  box-shadow: 0 0 0 1px #dce5ef inset;
  background: #ffffff;
  transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2878bd inset;
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
}

.login-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 5px 0 25px;
  color: #8998aa;
  font-size: 12px;
}

.login-tips .el-icon {
  color: #2878bd;
}

.login-button {
  width: 100%;
  height: 49px;
  border: none;
  border-radius: 9px;
  background: linear-gradient(90deg, #1764a7, #328bd0);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 3px;
  box-shadow: 0 7px 18px rgba(40,120,189,0.2);
}

.login-button:hover {
  background: linear-gradient(90deg, #12558f, #2878bd);
}

.form-bottom {
  margin-top: 35px;
  text-align: center;
}

.security-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: #8998aa;
  font-size: 12px;
}

.security-info .el-icon {
  color: #2878bd;
}

.form-bottom p {
  margin-top: 23px;
  color: #8998aa;
  font-size: 12px;
}

.form-bottom p span {
  color: #2878bd;
}

.right-footer {
  position: absolute;
  bottom: 20px;
  color: #a0aebe;
  font-size: 11px;
}

/* 窄屏适配 */
@media (max-width: 900px) {
  .brand-section {
    width: 45%;
  }

  .form-section {
    width: 55%;
    padding: 30px;
  }

  .feature-list {
    flex-direction: column;
  }
}

@media (max-width: 650px) {
  .brand-section {
    display: none;
  }

  .form-section {
    width: 100%;
    padding: 25px;
  }
}
</style>
