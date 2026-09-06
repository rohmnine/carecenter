import Layout from '../layout/Layout.vue'
import {createRouter, createWebHistory} from "vue-router";
import { getCurrentUser, getCurrentIdentity } from '@/utils/sessionHelper'


export const constantRoutes = [
    {path: '/login', name: 'Login', component: () => import("@/views/Login")},
    {path: '/register', name: 'Register', component: () => import("@/views/Register")},
    {
        path: '/Layout', name: 'Layout', component: Layout, children: [
            //
            {path: '/home', name: 'Home', component: () => import("@/views/Home")},
            {path: '/stuInfo', name: 'StuInfo', component: () => import("@/views/StuInfo")},
            {path: '/parentInfo', name: 'ParentInfo', component: () => import("@/views/ParentInfo")},
            {path: '/buildingInfo', name: 'BuildingInfo', component: () => import("@/views/BuildingInfo")},
            {path: '/roomInfo', name: 'RoomInfo', component: () => import("@/views/RoomInfo")},
            {path: '/bedVisualization', name: 'BedVisualization', component: () => import("@/views/BedVisualization")},
            {path: '/noticeInfo', name: 'NoticeInfo', component: () => import("@/views/NoticeInfo")},
            {path: '/visitorInfo', name: 'VisitorInfo', component: () => import("@/views/VisitorInfo")},
            //
            {path: '/myRoomInfo', name: 'MyRoomInfo', component: () => import("@/views/MyRoomInfo")},
            //菜谱管理
            {path: '/randomMenu', name: 'RandomMenu', component: () => import("@/views/RandomMenu")},
            {path: '/weeklyMenu', name: 'WeeklyMenu', component: () => import("@/views/WeeklyMenu")},
            // 请假申请和通知
            {path: '/studentLeaveRequest', name: 'StudentLeaveRequest', component: () => import("@/views/StudentLeaveRequest")},
            {path: '/parentNotifications', name: 'ParentNotifications', component: () => import("@/views/ParentNotifications")},
            {path: '/adminUnauthorizedNotification', name: 'AdminUnauthorizedNotification', component: () => import("@/views/AdminUnauthorizedNotification")},
            {path: '/adminLeaveRequestManagement', name: 'AdminLeaveRequestManagement', component: () => import("@/views/AdminLeaveRequestManagement")},
            {path: '/auditCenter', name: 'AuditCenter', component: () => import("@/views/AuditCenter")},
            // 缴费管理
            {path: '/paymentManagement', name: 'PaymentManagement', component: () => import("@/views/MealBillingAdmin")},
            // 进出登记管理
            {path: '/entryExitManagement', name: 'EntryExitManagement', component: () => import("@/views/EntryExitManagement")},
            {path: '/parentEntryExitView', name: 'ParentEntryExitView', component: () => import("@/views/ParentEntryExitView")},
            {path: '/studentEntryExitView', name: 'StudentEntryExitView', component: () => import("@/views/StudentEntryExitView")},
            // 家长功能
            {path: '/parentLeaveRequest', name: 'ParentLeaveRequest', component: () => import("@/views/ParentLeaveRequest")},
            {path: '/parentDashboard', name: 'ParentDashboard', component: () => import("@/views/ParentDashboard")},
            {path: '/parentVisitorAppointment', name: 'ParentVisitorAppointment', component: () => import("@/views/ParentVisitorAppointment")},
            {path: '/parentBedSelection', name: 'ParentBedSelection', component: () => import("@/views/ParentBedSelection")},
            // 管理员 - 访客预约管理
            {path: '/adminVisitorAppointment', name: 'AdminVisitorAppointment', component: () => import("@/views/AdminVisitorAppointment")},
            {path: '/adminBedSelectionAudit', name: 'AdminBedSelectionAudit', component: () => import("@/views/AdminBedSelectionAudit")},
            // 每日菜品图片
            {path: '/dailyMenuPhotoAdmin', name: 'DailyMenuPhotoAdmin', component: () => import("@/views/DailyMenuPhotoAdmin")},
            {path: '/dailyMenuPhotoView', name: 'DailyMenuPhotoView', component: () => import("@/views/DailyMenuPhotoView")},
            // 餐费账单管理
            {path: '/mealBillingAdmin', name: 'MealBillingAdmin', component: () => import("@/views/MealBillingAdmin")},
            {path: '/mealReservationAudit', name: 'MealReservationAudit', component: () => import("@/views/MealReservationAudit")},
            {path: '/parentMealBill', name: 'ParentMealBill', component: () => import("@/views/ParentMealBill")},
            // 家长就餐预约
            {path: '/parentMealReservation', name: 'ParentMealReservation', component: () => import("@/views/ParentMealReservation")},
            // 讨论组
            {path: '/discussionGroup', name: 'DiscussionGroup', component: () => import("@/views/DiscussionGroup")},
            // 菜品建议管理
            {path: '/dishSuggestionManagement', name: 'DishSuggestionManagement', component: () => import("@/views/DishSuggestionManagement")},
            // 菜品反馈
            {path: '/dishRatingFeedback', name: 'DishRatingFeedback', component: () => import("@/views/DishRatingFeedback")},

            {path: '/selfInfo', name: 'SelfInfo', component: () => import("@/views/SelfInfo")},
        ]
    },

]
const router = createRouter({
    routes: constantRoutes,
    history: createWebHistory(process.env.BASE_URL)
})
//路由守卫
router.beforeEach((to, from, next) => {
    //to 要访问的路径
    //from 代表从哪个路径跳转而来
    // next 是函数，表示放行
    // next() 放行
    // 使用身份隔离的会话存储
    const user = getCurrentUser()
    const identity = getCurrentIdentity()
    
    if (to.path === '/login' || to.path === '/register') {
        return next();
    }
    if (!user) {
        return next('/login')
    }
    if (to.path === '/' && user) {
        return next('/home')
    }
    
    // 家长和学生权限控制
    if (identity) {
        // identity 已经是字符串，不需要再 JSON.parse
        if (identity === 'parent') {
            // 家长可以访问：首页、个人信息、每周菜谱、通知中心、进出记录、请假管理、仪表盘、访客预约、菜品查看、餐费账单、讨论组、菜品评价
            const allowedPaths = ['/home', '/selfInfo', '/weeklyMenu', '/parentNotifications', '/parentEntryExitView', '/parentLeaveRequest', '/parentDashboard', '/parentVisitorAppointment', '/parentBedSelection', '/dailyMenuPhotoView', '/parentMealBill', '/parentMealReservation', '/discussionGroup', '/dishRatingFeedback', '/Layout']
            if (!allowedPaths.includes(to.path)) {
                return next('/home')
            }
        } else if (identity === 'stu') {
            // 学生可以查看每周菜谱 + 每日菜品查看
            const studentRestrictedPaths = ['/randomMenu']
            if (studentRestrictedPaths.includes(to.path)) {
                return next('/home')
            }
        }
    }
    
    next()
})

export default router
