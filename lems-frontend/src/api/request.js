
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 Axios 实例
const request = axios.create({
    // Spring Boot 后端地址
    baseURL: 'http://localhost:8081',

    // 请求超时时间
    timeout: 10000,

    // 允许携带 Session Cookie
    withCredentials: true,

    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        const status = error.response?.status

        if (status === 401) {
            ElMessage.warning('登录状态已失效，请重新登录')
        } else if (status === 403) {
            ElMessage.error('没有权限执行此操作')
        } else if (!error.response) {
            ElMessage.error('无法连接服务器，请检查后端是否启动')
        } else {
            ElMessage.error(
                error.response.data?.message || '请求失败'
            )
        }

        return Promise.reject(error)
    }
)

export default request
