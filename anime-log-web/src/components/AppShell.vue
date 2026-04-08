<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { avatarFallback } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isAuthPage = computed(() => route.meta.layout === 'auth')

const navItems = [
  { label: '工作台', to: '/' },
  { label: '番剧搜索', to: '/anime-search' },
  { label: '我的追番', to: '/library' },
  { label: '个人中心', to: '/profile' },
]

async function handleLogout() {
  authStore.logout()
  await router.replace('/login')
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
    <header class="app-header">
      <RouterLink class="brand" to="/">
        <span class="brand-mark">追</span>
        <div>
          <strong>追番日志馆</strong>
          <p>把每一集的情绪认真收藏起来</p>
        </div>
      </RouterLink>

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
        <RouterLink class="ghost-button header-cta" to="/library">继续记录</RouterLink>
        <button
          class="user-chip"
          type="button"
          :aria-label="`退出当前账号 ${authStore.userInfo?.nickname ?? ''}`"
          @click="handleLogout"
        >
          <span class="avatar-badge">{{ avatarFallback(authStore.userInfo?.nickname) }}</span>
          <span class="user-chip-copy">
            <strong>{{ authStore.userInfo?.nickname ?? '未登录' }}</strong>
            <small>点击退出</small>
          </span>
        </button>
      </div>
    </header>

    <main id="main-content" class="page-shell" tabindex="-1">
      <RouterView />
    </main>
  </div>
</template>
