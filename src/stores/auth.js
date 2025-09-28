import { defineStore } from 'pinia'
import api from '@/api/auth'
import {getToken, setToken, removeToken, setUserInfo, removeUserInfo, getUserInfo} from '@/utils/auth'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        token: getToken(), // 使用工具函数初始化
        isLoading: false,
        error: null,
        loginUsername: localStorage.getItem('loginUsername') || null
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        userInitial: (state) => state.loginUsername?.charAt(0)?.toUpperCase() || '未'
    },
    actions: {
        async login(credentials) {
            this.isLoading = true
            this.error = null
            try {
                const response = await api.login(credentials)
                this.token = response.data.token
                this.user = response.data
                this.loginUsername = credentials.username

                // 使用工具函数存储
                setToken(response.data.token)
                setUserInfo(response.data)
                localStorage.setItem('loginUsername', credentials.username)

                return { success: true }
            } catch (error) {
                console.error("登录错误详情:", error)
                const errorMessage = error.response?.data?.msg || error.response?.data || '登录失败，请检查用户名和密码'
                this.error = errorMessage
                return { success: false, error: errorMessage }
            } finally {
                this.isLoading = false
            }
        },

        async register(credentials) {
            this.isLoading = true
            this.error = null
            try {
                const response = await api.register(credentials)
                if (response.code === 200) {
                    return {
                        success: true,
                        message: response.msg || '注册成功',
                        data: response.data
                    }
                } else {
                    return {
                        success: false,
                        error: response.msg || '注册失败'
                    }
                }
            } catch (error) {
                console.error("注册错误详情:", error)
                const errorMessage = error.response?.data?.msg ||
                    error.response?.data ||
                    '注册失败，请稍后重试'
                this.error = errorMessage
                return { success: false, error: errorMessage }
            } finally {
                this.isLoading = false
            }
        },

        logout() {
            this.token = null
            this.user = null
            this.loginUsername = null
            // 使用工具函数清理
            removeToken()
            removeUserInfo()
            localStorage.removeItem('loginUsername')
        },

        initialize() {
            try {
                const token = getToken() // 使用工具函数获取
                const userInfo = getUserInfo() // 使用工具函数获取
                const loginUsername = localStorage.getItem('loginUsername')

                if (token) this.token = token
                if (userInfo) this.user = userInfo
                if (loginUsername) this.loginUsername = loginUsername
            } catch (error) {
                console.error("初始化认证状态失败:", error)
                this.logout()
            }
        },

        clearError() {
            this.error = null
        }
    }
})