
import { defineStore } from 'pinia'
import request from '../api/request'

export const useUserStore = defineStore('user', {
    state: () => ({
        userInfo: null,
        isLoggedIn: false
    }),

    getters: {
        username: state => state.userInfo?.username || '',
        role: state => state.userInfo?.role || '',
        isAdmin: state => state.userInfo?.role === 'ADMIN'
    },

    actions: {
        // 用户登录
        async login(form) {
            const result = await request.post(
                '/api/user/login',
                form
            )

            if (result.code === 200) {
                this.userInfo = result.data
                this.isLoggedIn = true
            }

            return result
        },

        // 获取当前登录用户
        async fetchProfile() {
            const result = await request.get('/api/user/profile')

            if (result.code === 200) {
                this.userInfo = result.data
                this.isLoggedIn = true
                return result.data
            }

            return null
        },

        // 用户退出
        async logout() {
            try {
                await request.post('/api/user/logout')
            } finally {
                this.userInfo = null
                this.isLoggedIn = false
            }
        },

        // 清除本地登录状态
        clearUser() {
            this.userInfo = null
            this.isLoggedIn = false
        }
    }
})
