import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import HomeView from '../views/HomeView.vue'
import LoginView from '../service/LoginView.vue'
import RegisterView from '../service/RegisterView.vue'
import AdvView from "@/views/AdvView.vue";
import ServiceManage from "@/views/ServiceManage.vue";
import VolunteerManage from "@/views/VolunteerManage.vue";
import CommunityInfoView from "@/views/CommunityInfoView.vue";
import OrderManage from "@/views/OrderManage.vue";


const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'login',
        component: LoginView,
        meta: { guestOnly: true }
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterView,
        meta: { guestOnly: true }
    },
    {
        path: '/home',
        name: 'home',
        component: HomeView,
        meta: { requiresAuth: true },
        children: [
            {
                path: 'service',
                name: 'service',
                component: ServiceManage
            },
            {
                path: 'adv',
                name: 'adv',
                component: AdvView
            },
            {
                path: 'user',
                name: 'user',
                component: () => import('@/views/UserView.vue')
            },
            {
                path: 'neighborhood',
                name: 'neighborhood',
                component: () => import('@/views/NeighborhoodView.vue')
            },
            {
                path: 'communityInfo',
                name: 'communityInfo',
                component: CommunityInfoView
            },
            {
                path: '/volunteer',
                name: 'volunteer',
                component: VolunteerManage
            },
            {
                path: '/order',
                name: 'order',
                component: OrderManage
            },
            {
                path:'archive',
                name: 'archive',
                component: () => import('@/views/ArchiveView.vue')
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})
router.onError((error) => {
    console.log('路由错误:', error)
})

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    // 如果路由需要认证且用户未登录
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login')
    }
    // 如果路由仅限游客且用户已登录
    else if (to.meta.guestOnly && authStore.isAuthenticated) {
        next('/home')
    }
    // 其他情况正常导航
    else {
        next()
    }
})
export default router