import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '@/components/AppShell.vue'
import AnimeDetailView from '@/views/AnimeDetailView.vue'
import AnimeSearchView from '@/views/AnimeSearchView.vue'
import DashboardView from '@/views/DashboardView.vue'
import LibraryView from '@/views/LibraryView.vue'
import LoginView from '@/views/LoginView.vue'
import ProfileView from '@/views/ProfileView.vue'
import RegisterView from '@/views/RegisterView.vue'
import { getStoredToken } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppShell,
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
          meta: { requiresAuth: true },
        },
        {
          path: 'library',
          name: 'library',
          component: LibraryView,
          meta: { requiresAuth: true },
        },
        {
          path: 'anime-search',
          name: 'anime-search',
          component: AnimeSearchView,
          meta: { requiresAuth: true },
        },
        {
          path: 'anime/external/:provider/:subjectId',
          name: 'anime-external-detail',
          component: AnimeDetailView,
          meta: { requiresAuth: true },
        },
        {
          path: 'anime/:id',
          name: 'anime-detail',
          component: AnimeDetailView,
          meta: { requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/login',
      component: AppShell,
      children: [
        {
          path: '',
          name: 'login',
          component: LoginView,
          meta: { layout: 'auth' },
        },
      ],
    },
    {
      path: '/register',
      component: AppShell,
      children: [
        {
          path: '',
          name: 'register',
          component: RegisterView,
          meta: { layout: 'auth' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const hasToken = Boolean(getStoredToken())

  if (to.meta.requiresAuth && !hasToken) {
    return '/login'
  }

  if ((to.path === '/login' || to.path === '/register') && hasToken) {
    return '/'
  }

  return true
})

export default router
