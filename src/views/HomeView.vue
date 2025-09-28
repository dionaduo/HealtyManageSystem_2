<template>
  <div class="home-container">
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <span class="logo-icon">❤️</span>
            <h1 class="title">健康颐养平台后台管理系统</h1>
          </div>
          <div class="user-info">
            <el-dropdown trigger="click">
              <div class="user-dropdown-trigger">
                <el-avatar
                    :size="40"
                    :src="getUserAvatarUrl(authStore.user?.avatar)"
                    class="user-avatar"
                >
                  {{ authStore.userInitial }}
                </el-avatar>
                <div class="user-details">
                  <span class="username">{{ authStore.loginUsername || '未登录' }}</span>
                  <span v-if="authStore.user?.roles" class="user-role">
                    {{ authStore.user.roles === 1 ? '管理员' : '普通用户' }}
                  </span>
                </div>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleProfile">
                    <el-icon><User /></el-icon>
                    个人资料
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleSettings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="confirmLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside
            :width="sidebarWidth"
            class="sidebar"
            :class="{ 'sidebar-collapsed': isSidebarCollapsed }"
            @mouseenter="handleSidebarMouseEnter"
            @mouseleave="handleSidebarMouseLeave"
        >
          <div class="sidebar-header">
            <h3 v-show="!isSidebarCollapsed">导航菜单</h3>
            <div class="sidebar-toggle" @click="toggleSidebar">
              <el-icon v-if="isSidebarCollapsed"><Expand /></el-icon>
              <el-icon v-else><Fold /></el-icon>
            </div>
          </div>
          <el-menu
              :default-active="activeMenu"
              class="el-menu-vertical-demo"
              @select="handleMenuSelect"
              router
              unique-opened
              :collapse="isSidebarCollapsed"
          >
            <el-sub-menu index="1" >
              <template #title>
                <el-icon><House /></el-icon>
                <span v-show="!isSidebarCollapsed">🏠 基础管理</span>
              </template>
              <el-menu-item
                  v-for="(item, index) in basicManagementItems"
                  :key="index"
                  :index="item.target"
                  :route="{ name: item.target }"
              >
                <template #title>
                  <span class="menu-item-icon">{{ item.icon }}</span>
                  <span>{{ item.name }}</span>
                </template>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="volunteer" :route="{ name: 'volunteer' }">
              <el-icon><User /></el-icon>
              <span v-show="!isSidebarCollapsed">👥 志愿者管理</span>
            </el-menu-item>
            <el-menu-item index="order" :route="{ name: 'order' }">
              <el-icon><Document /></el-icon>
              <span v-show="!isSidebarCollapsed">📋 订单管理</span>
            </el-menu-item>
            <el-menu-item index="file" :route="{ name: 'archive' }">
              <el-icon><Files /></el-icon>
              <span v-show="!isSidebarCollapsed">📝 档案管理</span>
            </el-menu-item>
          </el-menu>

          <div class="sidebar-footer" v-show="!isSidebarCollapsed">
            <div class="system-info">
              <div class="info-item">
                <span class="label">系统版本</span>
                <span class="value">v2.1.0</span>
              </div>
              <div class="info-item">
                <span class="label">在线用户</span>
                <span class="value">23</span>
              </div>
            </div>
          </div>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="main-content" :class="{ 'main-content-expanded': isSidebarCollapsed }">
          <div class="content-wrapper">
            <router-view v-slot="{ Component }">
              <transition name="fade-slide" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>

            <!-- 空状态提示 -->
            <div v-if="showDashboard" class="empty-state">
              <el-empty description="请从左侧菜单选择一个功能开始操作">
                <template #image>
                  <div class="empty-image">
                    <el-icon><DataAnalysis /></el-icon>
                  </div>
                </template>
                <h2>欢迎使用健康颐养平台管理系统</h2>
                <p>选择左侧菜单开始您的工作，或查看下面的快速统计</p>

                <div class="quick-stats">
                  <el-row :gutter="20">
                    <el-col :span="6">
                      <div class="stat-card">
                        <div class="stat-icon" style="background: #e6f7ff;">
                          <el-icon><User /></el-icon>
                        </div>
                        <div class="stat-content">
                          <div class="stat-value">1,234</div>
                          <div class="stat-label">注册用户</div>
                        </div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="stat-card">
                        <div class="stat-icon" style="background: #f6ffed;">
                          <el-icon><SetUp /></el-icon>
                        </div>
                        <div class="stat-content">
                          <div class="stat-value">567</div>
                          <div class="stat-label">服务订单</div>
                        </div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="stat-card">
                        <div class="stat-icon" style="background: #fff2e8;">
                          <el-icon><Picture /></el-icon>
                        </div>
                        <div class="stat-content">
                          <div class="stat-value">89</div>
                          <div class="stat-label">广告活动</div>
                        </div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="stat-card">
                        <div class="stat-icon" style="background: #f9f0ff;">
                          <el-icon><TrendCharts /></el-icon>
                        </div>
                        <div class="stat-content">
                          <div class="stat-value">98%</div>
                          <div class="stat-label">未使用率</div>
                        </div>
                      </div>
                    </el-col>
                  </el-row>
                </div>
              </el-empty>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  User,
  Setting,
  SwitchButton,
  ArrowDown,
  House,
  Document,
  Files,
  DataAnalysis,
  SetUp,
  Picture,
  TrendCharts,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏状态
const isSidebarCollapsed = ref(false)
const sidebarWidth = computed(() => isSidebarCollapsed.value ? '64px' : '280px')
let autoHideTimer = null
let isHovering = ref(false)
let isInteracting = ref(false)

// 判断是否显示仪表盘
const showDashboard = computed(() => {
  return route.path === '/home' || route.path === '/home/'
})

const activeMenu = computed(() => {
  return route.name || '1'
})

// 菜单项配置
const basicManagementItems = [
  { name: '服务管理', target: 'service', icon: '🔧'},
  {name: '广告管理', target: 'adv', icon: '📊'},
  {name: '用户管理', target: 'user', icon: '👤'},
  {name: '辖区管理', target: 'neighborhood', icon: '🏢'},
  {name:'社区管理', target: 'communityInfo', icon: '🏠'}
]

// 获取用户头像URL
const getUserAvatarUrl = (avatar) => {
  if (!avatar) return '';

  // 如果已经是完整的data URL，直接返回
  if (avatar.startsWith('data:image')) {
    return avatar;
  }

  // 如果是纯Base64（没有data:image前缀），添加前缀
  if (avatar.startsWith('/9j/') || avatar.startsWith('iVBORw') ||
      (avatar.length > 100 && !avatar.includes(' '))) {
    // 尝试判断图片类型
    let mimeType = 'image/jpeg';
    if (avatar.startsWith('iVBORw')) {
      mimeType = 'image/png';
    } else if (avatar.startsWith('R0lGOD') || avatar.startsWith('/9j/')) {
      mimeType = 'image/jpeg';
    }
    return `data:${mimeType};base64,${avatar}`;
  }

  // 如果是网络URL
  return `${avatar}?t=${new Date().getTime()}`;
}

const handleProfile = () => {
  ElMessage.info('打开个人资料')
}

const handleSettings = () => {
  ElMessage.info('打开系统设置')
}

// 确认退出登录
const confirmLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger',
    center: true
  }).then(() => {
    authStore.logout()
    router.push('/login')
  }).catch(() => {
    // 取消操作
  })
}

// 重置自动隐藏计时器
const resetAutoHideTimer = () => {
  // 清除之前的定时器
  if (autoHideTimer) {
    clearTimeout(autoHideTimer)
    autoHideTimer = null
  }

  // 如果侧边栏展开且不在交互状态，设置5秒后自动隐藏
  if (!isSidebarCollapsed.value && !isHovering.value && !isInteracting.value) {
    autoHideTimer = setTimeout(() => {
      isSidebarCollapsed.value = true
    }, 5000)
  }
}

// 处理鼠标进入侧边栏
const handleSidebarMouseEnter = () => {
  isHovering.value = true
  resetAutoHideTimer() // 清除当前计时
}

// 处理鼠标离开侧边栏
const handleSidebarMouseLeave = () => {
  isHovering.value = false
  resetAutoHideTimer() // 重新开始计时
}

// 处理侧边栏交互（点击菜单等）
const handleSidebarInteraction = () => {
  isInteracting.value = true
  resetAutoHideTimer() // 清除当前计时

  // 设置一个短暂的延迟后重置交互状态
  setTimeout(() => {
    isInteracting.value = false
    resetAutoHideTimer() // 重新开始计时
  }, 2000) // 2秒后认为交互结束
}

// 切换侧边栏
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
  resetAutoHideTimer()
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  console.log('菜单选择:', index)
  handleSidebarInteraction() // 标记为交互状态
}

// 组件挂载时设置5秒后自动隐藏侧边栏
onMounted(() => {
  // 初始状态为展开
  isSidebarCollapsed.value = false
  resetAutoHideTimer()
})

// 组件卸载前清除定时器
onUnmounted(() => {
  if (autoHideTimer) {
    clearTimeout(autoHideTimer)
    autoHideTimer = null
  }
})
</script>

<style scoped>
@import url('@/assets/StyleCss.css');
/* 特有样式 */
.home-container {
  background: #f5f7fa;
}

/* 头部样式 */
.header {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  position: relative;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 24px;
  animation: heartbeat 1.5s ease-in-out infinite;
}

@keyframes heartbeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.user-dropdown-trigger:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: transform 0.3s ease;
}

.user-dropdown-trigger:hover .user-avatar {
  transform: scale(1.05);
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: white;
  font-size: 14px;
}

.user-role {
  font-size: 12px;
  opacity: 0.8;
  color: white;
}

.dropdown-icon {
  transition: transform 0.3s ease;
}

.user-dropdown-trigger:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 侧边栏样式 */
.sidebar {
  height: calc(100vh - 64px);
  background: #2c3e50;
  border-right: 1px solid #e6e8f0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  flex-direction: column;
  ::-webkit-scrollbar{
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: white;
  font-size: 16px;
  font-weight: 600;
  transition: opacity 0.3s ease;
}

.sidebar-toggle {
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

:deep(.el-menu) {
  border-right: none;
  background: #2c3e50;
  flex: 1;
  transition: width 0.3s ease;
  /* 隐藏滚动条 */
  overflow-y: hidden;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: white !important;
  height: 50px;
  line-height: 50px;
  white-space: nowrap;
  border-radius: 0 24px 24px 0;
  margin: 4px 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(52, 152, 219, 0.2) !important;
  transform: translateX(4px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%) !important;
  color: white !important;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.menu-item-icon {
  margin-right: 8px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.system-info {
  color: rgba(255, 255, 255, 0.7);
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 500;
}

.value {
  color: #3498db;
  font-weight: 600;
}

/* 主内容区样式 */
.main-content {
  background: #f5f7fa;
  padding: 0;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.main-content-expanded {
  margin-left: 0;
}

.content-wrapper {
  height: calc(100vh - 64px);
  padding: 24px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

.content-wrapper::-webkit-scrollbar {
  width: 8px;
}

.content-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.empty-state {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-image {
  font-size: 80px;
  color: #667eea;
  margin-bottom: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.quick-stats {
  margin-top: 40px;
}

.stat-col {
  display: flex;
  justify-content: center;
}

.stat-card {
  width: 100%;
  max-width: 250px;
  background: white;
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: 0.5s;
}

.stat-card:hover::before {
  left: 100%;
}

.stat-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.15);
}

.card-1 {
  border-top: 4px solid #409eff;
}

.card-2 {
  border-top: 4px solid #67c23a;
}

.card-3 {
  border-top: 4px solid #e6a23c;
}

.card-4 {
  border-top: 4px solid #909399;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.icon-1 {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.icon-2 {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.icon-3 {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.icon-4 {
  background: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
  transition: color 0.3s ease;
}

.stat-card:hover .stat-value {
  color: #3498db;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 侧边栏折叠状态下的样式 */
.sidebar-collapsed :deep(.el-sub-menu__title span),
.sidebar-collapsed :deep(.el-menu-item span) {
  display: none;
}

.sidebar-collapsed :deep(.el-sub-menu__icon-arrow) {
  display: none;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .title {
    font-size: 18px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 20px;
  }
}

@media (max-width: 992px) {
  .header-content {
    padding: 0 16px;
  }

  .title {
    font-size: 16px;
  }

  .user-details {
    display: none;
  }

  .quick-stats .el-row {
    gap: 15px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
  }

  .logo-icon {
    font-size: 20px;
  }

  .title {
    font-size: 14px;
  }

  .sidebar {
    position: absolute;
    z-index: 999;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.2);
  }

  .content-wrapper {
    padding: 16px;
  }

  .quick-stats {
    margin-top: 20px;
  }

  .stat-col {
    margin-bottom: 15px;
  }

  .stat-card {
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .header {
    padding: 0 10px;
  }

  .logo-icon {
    font-size: 18px;
  }

  .title {
    font-size: 12px;
  }

  .content-wrapper {
    padding: 12px;
  }

  .el-row {
    flex-direction: column;
    gap: 15px !important;
  }

  .el-col {
    width: 100% !important;
    max-width: 100% !important;
  }

  .stat-card {
    max-width: 100%;
  }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .home-container,
  .main-content,
  .content-wrapper {
    background: #1a1a1a;
    color: #e0e0e0;
  }

  .stat-card {
    background: #2d2d2d;
  }

  .stat-value {
    color: #ffffff;
  }

  .stat-label {
    color: #b0b0b0;
  }
}

/* 减少动画支持 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }

  .stat-card:hover,
  .user-dropdown-trigger:hover,
  .sidebar-toggle:hover {
    transform: none;
  }
}
</style>
