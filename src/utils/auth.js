import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const UserKey = 'user-info'

// Token 相关函数
export function getToken() {
    return localStorage.getItem(TokenKey) || ''
}

export function setToken(token, expires = 7) {
    localStorage.setItem(TokenKey, token)
    Cookies.set(TokenKey, token, { expires })
}

export function removeToken() {
    localStorage.removeItem(TokenKey)
    Cookies.remove(TokenKey)
}

// 用户信息相关函数
export function getUserInfo() {
    const userInfo = localStorage.getItem(UserKey)
    return userInfo ? JSON.parse(userInfo) : null
}

export function setUserInfo(userInfo) {
    localStorage.setItem(UserKey, JSON.stringify(userInfo))
}

export function removeUserInfo() {
    localStorage.removeItem(UserKey)
}

// 工具函数
export function isLoggedIn() {
    return !!getToken()
}

export function getAuthHeaders() {
    const token = getToken()
    return token ? { Authorization: `Bearer ${token}` } : {}
}