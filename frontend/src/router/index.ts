import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/pages/home/HomePage.vue') },
        { path: 'robots', name: 'robot-list', component: () => import('@/pages/robots/RobotListPage.vue') },
        { path: 'robots/:slug', name: 'robot-detail', component: () => import('@/pages/robots/RobotDetailPage.vue') },
        { path: 'compare', name: 'compare', component: () => import('@/pages/robots/ComparePage.vue') },
        { path: 'manufacturers/:id', name: 'manufacturer', component: () => import('@/pages/robots/ManufacturerPage.vue') },
        { path: 'user/profile', name: 'user-profile', component: () => import('@/pages/user/ProfilePage.vue'), meta: { requiresAuth: true } },
        { path: 'user/favorites', name: 'user-favorites', component: () => import('@/pages/user/FavoritesPage.vue'), meta: { requiresAuth: true } },
        { path: 'user/notifications', name: 'user-notifications', component: () => import('@/pages/user/NotificationsPage.vue'), meta: { requiresAuth: true } },
        { path: 'user/membership', name: 'membership', component: () => import('@/pages/user/MembershipPage.vue'), meta: { requiresAuth: true } },
        { path: 'reviews', name: 'reviews', component: () => import('@/pages/robots/ReviewListPage.vue') },
        { path: 'reviews/create', name: 'review-create', component: () => import('@/pages/robots/ReviewCreatePage.vue') },
        { path: 'reviews/:id', name: 'review-detail', component: () => import('@/pages/robots/ReviewDetailPage.vue') },
        { path: 'case-studies', name: 'case-studies', component: () => import('@/pages/robots/CaseStudyListPage.vue') },
        { path: 'case-studies/:id', name: 'case-study-detail', component: () => import('@/pages/robots/CaseStudyDetailPage.vue') },
        { path: 'manufacturer-portal/apply', name: 'manufacturer-apply', component: () => import('@/pages/manufacturers/PortalApplyPage.vue'), meta: { requiresAuth: true } },
        { path: 'manufacturer-portal/dashboard', name: 'manufacturer-dashboard', component: () => import('@/pages/manufacturers/ManufacturerDashboardPage.vue'), meta: { requiresAuth: true } },
        { path: 'about', name: 'about', component: () => import('@/pages/about/AboutPage.vue') },
        { path: 'contact', name: 'contact', component: () => import('@/pages/about/ContactPage.vue') }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('@/pages/admin/DashboardPage.vue') },
        { path: 'robots', name: 'admin-robots', component: () => import('@/pages/admin/RobotsPage.vue') },
        { path: 'robots/create', name: 'admin-robot-create', component: () => import('@/pages/admin/RobotFormPage.vue') },
        { path: 'robots/:id/edit', name: 'admin-robot-edit', component: () => import('@/pages/admin/RobotFormPage.vue') },
        { path: 'manufacturers', name: 'admin-manufacturers', component: () => import('@/pages/admin/ManufacturersPage.vue') },
        { path: 'inquiries', name: 'admin-inquiries', component: () => import('@/pages/admin/InquiriesPage.vue') },
        { path: 'announcement', name: 'admin-announcement', component: () => import('@/pages/admin/AnnouncementPage.vue') },
        { path: 'crawler', name: 'admin-crawler', component: () => import('@/pages/admin/CrawlerPage.vue') },
        { path: 'ugc', name: 'admin-ugc', component: () => import('@/pages/admin/UgcModerationPage.vue') },
        { path: 'ads', name: 'admin-ads', component: () => import('@/pages/admin/AdManagementPage.vue') },
        { path: 'manufacturer-applications', name: 'admin-manufacturer-applications', component: () => import('@/pages/admin/ManufacturerApplicationsPage.vue') },
        { path: 'membership', name: 'admin-membership', component: () => import('@/pages/admin/MembershipAdminPage.vue') },
        { path: 'settings', name: 'admin-settings', component: () => import('@/pages/admin/SiteSettingsPage.vue') },
        { path: 'selector-tool', name: 'admin-selector-tool', component: () => import('@/pages/admin/SelectorToolPage.vue') }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/pages/user/LoginPage.vue') },
    { path: '/register', name: 'register', component: () => import('@/pages/user/RegisterPage.vue') },
    { path: '/auth/wechat/callback', name: 'wechat-callback', component: () => import('@/pages/user/WechatCallbackPage.vue') }
  ],
  scrollBehavior: () => ({ top: 0 })
})

function getRoleFromToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload.role ?? null
  } catch {
    return null
  }
}

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('access_token')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin) {
    if (!token) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
    const role = getRoleFromToken(token)
    if (role !== 'admin' && role !== 'superadmin') {
      // 有 token 但不是管理员，跳回首页
      next({ name: 'home' })
      return
    }
  }

  next()
})

export default router
