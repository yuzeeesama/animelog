<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAnimeSearchStore } from '@/stores/animeSearch'
import { avatarFallback } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const searchStore = useAnimeSearchStore()
searchStore.hydrate()

const isAuthPage = computed(() => route.meta.layout === 'auth')
const isDetailPage = computed(
  () => route.name === 'anime-detail' || route.name === 'anime-external-detail',
)
const globalSearchKeyword = computed({
  get: () => searchStore.keyword,
  set: (value: string) => {
    searchStore.setSearchState({ keyword: value })
  },
})

const navItems = [
  { label: '工作台', to: '/' },
  { label: '番剧搜索', to: '/anime-search' },
  { label: '我的追番', to: '/library' },
  { label: '个人中心', to: '/profile' },
]

const mobileTabs = [
  { label: '工作台', shortLabel: '工作台', to: '/' },
  { label: '搜索', shortLabel: '搜索', to: '/anime-search' },
  { label: '追番', shortLabel: '追番', to: '/library' },
  { label: '我的', shortLabel: '我的', to: '/profile' },
]

const routeTitles: Record<string, string> = {
  dashboard: '工作台',
  'anime-search': '搜索番剧',
  library: '我的追番',
  profile: '个人中心',
  'anime-detail': '番剧详情',
  'anime-external-detail': '番剧详情',
  login: '登录',
  register: '注册',
}

const userNickname = computed(() => authStore.userInfo?.nickname ?? '未登录')
const mobileTitle = computed(() => {
  const routeName = typeof route.name === 'string' ? route.name : ''
  return routeTitles[routeName] ?? '追番日志馆'
})

async function handleLogout() {
  authStore.logout()
  await router.replace('/login')
}

async function handleGlobalSearch() {
  const nextKeyword = globalSearchKeyword.value.trim()
  if (!nextKeyword) return

  searchStore.requestGlobalSearch(nextKeyword)

  if (route.path !== '/anime-search') {
    await router.push('/anime-search')
  }
}

async function handleMobileBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }

  await router.replace('/')
}
</script>

<template>
  <div v-if="isAuthPage" class="auth-layout">
    <RouterView />
  </div>

  <div v-else class="app-shell">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <div class="app-shell-glow app-shell-glow--left" aria-hidden="true" />
    <div class="app-shell-glow app-shell-glow--right" aria-hidden="true" />

    <header class="mobile-topbar">
      <button
        v-if="isDetailPage"
        class="mobile-icon-button"
        type="button"
        aria-label="返回上一页"
        @click="handleMobileBack"
      >
        返回
      </button>
      <RouterLink v-else class="mobile-brand" to="/">
        <span class="brand-mark">追</span>
        <div class="mobile-brand-copy">
          <strong>追番日志馆</strong>
          <small>{{ mobileTitle }}</small>
        </div>
      </RouterLink>

      <div v-if="isDetailPage" class="mobile-page-heading">
        <span class="section-eyebrow">Detail</span>
        <strong>{{ mobileTitle }}</strong>
      </div>

      <RouterLink class="mobile-user-link" to="/profile" aria-label="打开个人中心">
        <span class="avatar-badge">{{ avatarFallback(authStore.userInfo?.nickname) }}</span>
      </RouterLink>
    </header>

    <aside class="app-sidebar" aria-label="应用导航">
      <div class="sidebar-surface">
        <RouterLink class="brand" to="/">
          <span class="brand-mark">追</span>
          <div>
            <strong>追番日志馆</strong>
            <p>把每一集的情绪认真收藏起来</p>
          </div>
        </RouterLink>

        <form class="sidebar-search" @submit.prevent="handleGlobalSearch">
          <label class="search-field search-field--stack">
            <span class="sidebar-label">全局番剧搜索</span>
            <input
              v-model.trim="globalSearchKeyword"
              type="search"
              placeholder="搜索作品名 / 别名 / 原名"
            />
          </label>
          <button class="primary-button search-submit" type="submit" :disabled="!globalSearchKeyword.trim()">
            搜索番剧
          </button>
        </form>

        <nav class="main-nav" aria-label="主要导航">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            active-class="is-active"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="header-actions">
          <span class="sidebar-label">快捷操作</span>
          <RouterLink class="ghost-button header-cta" to="/library">继续记录</RouterLink>
        </div>

        <section class="sidebar-account" aria-label="当前账号">
          <div class="user-chip">
            <span class="avatar-badge">{{ avatarFallback(authStore.userInfo?.nickname) }}</span>
            <span class="user-chip-copy">
              <strong>{{ userNickname }}</strong>
              <small>保持记录，慢慢补番</small>
            </span>
          </div>
          <button
            class="ghost-button ghost-button--danger sidebar-logout"
            type="button"
            :aria-label="`退出当前账号 ${userNickname}`"
            @click="handleLogout"
          >
            退出登录
          </button>
        </section>
      </div>
    </aside>

    <main id="main-content" class="page-shell" tabindex="-1">
      <RouterView />
    </main>

    <nav class="mobile-tabbar" aria-label="底部导航">
      <RouterLink
        v-for="item in mobileTabs"
        :key="item.to"
        :to="item.to"
        class="mobile-tab"
        active-class="is-active"
      >
        <span class="mobile-tab-label">{{ item.shortLabel }}</span>
      </RouterLink>
    </nav>
  </div>
</template>
