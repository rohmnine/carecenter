import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/admin'
      },
      {
        path: 'admin',
        name: 'admin',
        component: () => import('../views/AdminDashboard.vue')
      },
      {
        path: 'teacher',
        name: 'teacher',
        component: () => import('../views/TeacherDashboard.vue')
      },
      {
        path: 'parent',
        name: 'parent',
        component: () => import('../views/ParentDashboard.vue')
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('../views/MessageBoard.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const loggedIn = localStorage.getItem('token')

  if (to.matched.some(record => record.meta.requiresAuth) && !loggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
