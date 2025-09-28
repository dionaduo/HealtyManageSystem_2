import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { getToken, removeToken } from '@/utils/auth' // 导入工具函数
import router from '@/router'

const api = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器 - 使用getToken
api.interceptors.request.use(config => {
    const token = getToken() // 使用统一的getToken函数
    if (token) {
        config.headers.Authorization = token // getToken已经包含Bearer前缀
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器
api.interceptors.response.use(response => {
    return response.data
}, error => {
    const authStore = useAuthStore()

    if (error.response) {
        switch (error.response.status) {
            case 401:
                console.error('未授权，请重新登录')
                removeToken() // 使用统一的removeToken
                authStore.logout()
                router.push('/login')
                break
            case 403:
                console.error('拒绝访问')
                break
            case 404:
                console.error('请求资源不存在')
                break
            case 500:
                console.error('服务器错误')
                break
            default:
                console.error('请求错误', error.message)
        }
    } else if (error.request) {
        console.error('网络错误，请检查网络连接')
    } else {
        console.error('请求配置错误', error.message)
    }

    return Promise.reject(error)
})

export default api