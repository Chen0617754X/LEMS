
import { createRouter, createWebHistory } from 'vue-router'

import { useUserStore } from '../stores/user'

import AdminLayout from '../layout/AdminLayout.vue'
import UserLayout from '../layout/UserLayout.vue'

const routes = [
  // 统一登录入口
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '用户登录' }
  },

  // 管理员后台
  {
    path: '/',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      roles: ['ADMIN']
    },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '系统首页' }
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('../views/Equipment.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('../views/Category.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('../views/Repair.vue'),
        meta: { title: '维修管理' }
      },
      {
        path: 'users',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },

  // 普通用户门户
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/home',
    meta: {
      requiresAuth: true,
      roles: ['USER']
    },
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('../views/user/UserHome.vue'),
        meta: { title: '用户首页' }
      },
      {
        path: 'equipment',
        name: 'UserEquipment',
        component: () => import('../views/user/UserEquipment.vue'),
        meta: { title: '设备查询' }
      },
      {
            path: 'repair',
            name: 'UserRepair',
            component: () => import('../views/user/UserRepair.vue'),
            meta: { title: '在线报修' }
        },
        {
            path: 'my-repairs',
            name: 'MyRepair',
            component: () => import('../views/user/MyRepair.vue'),
            meta: { title: '我的维修记录' }
        }
    ]
  },

  // 未匹配的地址
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 根据角色决定登录后的首页
function getHomePath(userStore) {
  return userStore.isAdmin ? '/' : '/user/home'
}

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 恢复登录状态
  if (!userStore.isLoggedIn) {
    try {
      await userStore.fetchProfile()
    } catch {
      userStore.clearUser()
    }
  }

  // 已登录用户访问登录页
  if (to.path === '/login') {
    return userStore.isLoggedIn
      ? getHomePath(userStore)
      : true
  }

  // 未登录用户不能访问内部页面
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return '/login'
  }

  // 角色权限判断
  if (to.meta.roles && userStore.isLoggedIn) {
    if (!to.meta.roles.includes(userStore.role)) {
      return getHomePath(userStore)
    }
  }

  document.title = `${to.meta.title || 'LEMS'} - 设备管理系统`

  return true
})

export default router
