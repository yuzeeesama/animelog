<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { firstValidationMessage, validateLoginForm } from '@/utils/validation'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

const fieldErrors = ref<Partial<Record<'username' | 'password', string>>>({})
const errorMessage = ref('')
const loading = ref(false)

async function submit() {
  errorMessage.value = ''
  fieldErrors.value = validateLoginForm(form)

  if (Object.keys(fieldErrors.value).length > 0) {
    errorMessage.value = firstValidationMessage(fieldErrors.value)
    return
  }

  loading.value = true

  try {
    await authStore.login(form)
    await authStore.refreshUserInfo()
    await router.replace('/')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-poster">
      <span class="auth-badge">Anime Journal</span>
      <h1>让每一次追番，都留下可回看的温度。</h1>
      <p>
        在这里记录看到哪一集、当下的心情和那些只属于你的观后感，把零散片段整理成一册完整的追番手账。
      </p>
      <div class="poster-orbs">
        <span />
        <span />
        <span />
      </div>
    </div>

    <div class="auth-panel">
      <div class="section-title">
        <span class="section-eyebrow">欢迎回来</span>
        <h2>登录追番日志馆</h2>
        <p>进入你的工作台，继续上一次的追番轨迹。</p>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" autocomplete="username" placeholder="例如：frieren" />
          <small v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</small>
        </label>

        <label>
          <span>密码</span>
          <input
            v-model.trim="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入登录密码"
          />
          <small v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</small>
        </label>

        <p v-if="errorMessage" class="form-message form-message--error">{{ errorMessage }}</p>

        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录并进入工作台' }}
        </button>
      </form>

      <p class="auth-footer">
        还没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </div>
  </section>
</template>
