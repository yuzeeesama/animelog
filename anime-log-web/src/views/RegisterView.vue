<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { registerUser } from '@/api/user'
import { firstValidationMessage, validateRegisterForm } from '@/utils/validation'

const router = useRouter()

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const message = ref('')
const isError = ref(false)
const loading = ref(false)
const fieldErrors = ref<
  Partial<Record<'username' | 'nickname' | 'email' | 'password' | 'confirmPassword', string>>
>({})

async function submit() {
  message.value = ''
  isError.value = false
  fieldErrors.value = validateRegisterForm(form)

  if (Object.keys(fieldErrors.value).length > 0) {
    isError.value = true
    message.value = firstValidationMessage(fieldErrors.value)
    return
  }

  loading.value = true

  try {
    await registerUser({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      email: form.email.trim() || undefined,
      password: form.password,
    })
    isError.value = false
    message.value = '注册成功，正在跳转到登录页...'
    window.setTimeout(() => {
      void router.push('/login')
    }, 700)
  } catch (error) {
    isError.value = true
    message.value = error instanceof Error ? error.message : '注册失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-poster auth-poster--secondary">
      <span class="auth-badge">Create Archive</span>
      <h1>从第一集开始，为你的追番记忆建立索引。</h1>
      <p>注册后，你可以记录每一部番剧的进度、感想、心情标签和属于自己的时间线。</p>
      <div class="poster-grid">
        <span />
        <span />
        <span />
        <span />
      </div>
    </div>

    <div class="auth-panel">
      <div class="section-title">
        <span class="section-eyebrow">新建档案</span>
        <h2>创建账号</h2>
        <p>只需要几个基础信息，就能开始整理你的追番生活。</p>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" placeholder="用于登录" />
          <small v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</small>
        </label>
        <label>
          <span>昵称</span>
          <input v-model.trim="form.nickname" type="text" placeholder="展示给你自己的名字" />
          <small v-if="fieldErrors.nickname" class="field-error">{{ fieldErrors.nickname }}</small>
        </label>
        <label>
          <span>邮箱</span>
          <input v-model.trim="form.email" type="email" placeholder="可选，用于资料展示" />
          <small v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</small>
        </label>
        <label>
          <span>密码</span>
          <input v-model.trim="form.password" type="password" placeholder="6-20 位密码" />
          <small v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</small>
        </label>
        <label>
          <span>确认密码</span>
          <input v-model.trim="form.confirmPassword" type="password" placeholder="再输入一次密码" />
          <small v-if="fieldErrors.confirmPassword" class="field-error">{{ fieldErrors.confirmPassword }}</small>
        </label>

        <p
          v-if="message"
          class="form-message"
          :class="isError ? 'form-message--error' : 'form-message--success'"
        >
          {{ message }}
        </p>

        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '注册中...' : '创建账号' }}
        </button>
      </form>

      <p class="auth-footer">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </section>
</template>
