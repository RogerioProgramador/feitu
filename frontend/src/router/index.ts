import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/workspaces' },
    {
      path: '/login',
      component: () => import('../pages/LoginPage.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      component: () => import('../pages/RegisterPage.vue'),
      meta: { public: true },
    },
    {
      path: '/workspaces',
      component: () => import('../pages/WorkspacePage.vue'),
    },
    {
      path: '/analytics',
      component: () => import('../pages/AnalyticsView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('feitu_token')
  if (!to.meta.public && !token) return { path: '/login', replace: true }
})

export default router
