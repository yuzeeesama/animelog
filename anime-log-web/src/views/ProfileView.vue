<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { updateUserPassword, updateUserProfile } from '@/api/user'
import SectionTitle from '@/components/SectionTitle.vue'
import { useAuthStore } from '@/stores/auth'
import {
  firstValidationMessage,
  type ValidationErrors,
  validatePasswordForm,
  validateProfileForm,
} from '@/utils/validation'

const authStore = useAuthStore()
const router = useRouter()

const profileForm = reactive({
  nickname: '',
  avatarUrl: '',
  email: '',
  bio: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(true)
const message = ref('')
const isError = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileErrors = ref<ValidationErrors<keyof typeof profileForm>>({})
const passwordErrors = ref<ValidationErrors<keyof typeof passwordForm>>({})

function setMessage(next: string, error = false) {
  message.value = next
  isError.value = error
}

async function hydratePage() {
  loading.value = true

  try {
    await authStore.refreshUserInfo()
    const info = authStore.userInfo
    Object.assign(profileForm, {
      nickname: info?.nickname ?? '',
      avatarUrl: info?.avatarUrl ?? '',
      email: info?.email ?? '',
      bio: info?.bio ?? '',
    })
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '加载个人中心失败。', true)
  } finally {
    loading.value = false
  }
}

async function submitProfile() {
  const payload = {
    nickname: profileForm.nickname.trim(),
    avatarUrl: profileForm.avatarUrl.trim() || undefined,
    email: profileForm.email.trim() || undefined,
    bio: profileForm.bio.trim() || undefined,
  }

  const errors = validateProfileForm(payload)
  profileErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  savingProfile.value = true

  try {
    await updateUserProfile(payload)
    await authStore.refreshUserInfo()
    setMessage('修改成功')
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '更新资料失败。', true)
  } finally {
    savingProfile.value = false
  }
}

async function submitPassword() {
  const payload = {
    oldPassword: passwordForm.oldPassword,
    newPassword: passwordForm.newPassword,
    confirmPassword: passwordForm.confirmPassword,
  }

  const errors = validatePasswordForm(payload)
  passwordErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  savingPassword.value = true

  try {
    await updateUserPassword({
      oldPassword: payload.oldPassword,
      newPassword: payload.newPassword,
    })
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    })
    passwordErrors.value = {}
    setMessage('修改成功')
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '修改密码失败。', true)
  } finally {
    savingPassword.value = false
  }
}

async function handleLogout() {
  authStore.logout()
  await router.replace('/login')
}

onMounted(() => {
  void hydratePage()
})
</script>

<template>
  <div class="page-stack">
    <section class="split-hero">
      <SectionTitle
        eyebrow="Profile"
        title="个人中心"
        description="在这里维护个人资料与账号安全设置，不处理番剧内容。"
      />
      <div class="profile-summary">
        <div class="profile-avatar">
          <img
            v-if="authStore.userInfo?.avatarUrl"
            :src="authStore.userInfo.avatarUrl"
            :alt="authStore.userInfo.nickname"
          />
          <span v-else>{{ authStore.userInfo?.nickname?.slice(0, 1) || '追' }}</span>
        </div>
        <div>
          <strong>{{ authStore.userInfo?.nickname }}</strong>
          <p>@{{ authStore.userInfo?.username }}</p>
        </div>
      </div>
    </section>

    <section class="mobile-profile-actions">
      <button class="ghost-button ghost-button--danger mobile-profile-logout" type="button" @click="handleLogout">
        退出登录
      </button>
    </section>

    <p v-if="message" class="form-message" :class="isError ? 'form-message--error' : 'form-message--success'">
      {{ message }}
    </p>

    <template v-if="loading">
      <div class="skeleton-list" aria-hidden="true">
        <div v-for="index in 4" :key="index" class="skeleton-card skeleton-card--compact" />
      </div>
    </template>

    <template v-else>
      <section class="content-grid">
        <div class="content-panel">
          <SectionTitle eyebrow="Edit Profile" title="修改资料" description="更新昵称、头像、邮箱和个人简介。" />

          <div class="form-stack">
            <label>
              <span>昵称</span>
              <input v-model.trim="profileForm.nickname" type="text" />
              <small v-if="profileErrors.nickname" class="field-error">{{ profileErrors.nickname }}</small>
            </label>
            <label>
              <span>头像地址</span>
              <input v-model.trim="profileForm.avatarUrl" type="url" placeholder="https://example.com/avatar.jpg" />
              <small v-if="profileErrors.avatarUrl" class="field-error">{{ profileErrors.avatarUrl }}</small>
            </label>
            <label>
              <span>邮箱</span>
              <input v-model.trim="profileForm.email" type="email" />
              <small v-if="profileErrors.email" class="field-error">{{ profileErrors.email }}</small>
            </label>
            <label>
              <span>个人简介</span>
              <textarea v-model.trim="profileForm.bio" rows="4" placeholder="说说你最近沉迷的类型和本季偏好" />
              <small v-if="profileErrors.bio" class="field-error">{{ profileErrors.bio }}</small>
            </label>
            <button class="primary-button" type="button" :disabled="savingProfile" @click="submitProfile">
              {{ savingProfile ? '保存中...' : '保存资料' }}
            </button>
          </div>
        </div>

        <div class="content-panel">
          <SectionTitle eyebrow="Security" title="账号安全" description="更新当前账号密码，保持登录信息安全。" />

          <div class="form-stack">
            <label>
              <span>旧密码</span>
              <input v-model.trim="passwordForm.oldPassword" type="password" />
              <small v-if="passwordErrors.oldPassword" class="field-error">{{ passwordErrors.oldPassword }}</small>
            </label>
            <label>
              <span>新密码</span>
              <input v-model.trim="passwordForm.newPassword" type="password" />
              <small v-if="passwordErrors.newPassword" class="field-error">{{ passwordErrors.newPassword }}</small>
            </label>
            <label>
              <span>确认新密码</span>
              <input v-model.trim="passwordForm.confirmPassword" type="password" />
              <small v-if="passwordErrors.confirmPassword" class="field-error">{{ passwordErrors.confirmPassword }}</small>
            </label>
            <button class="primary-button" type="button" :disabled="savingPassword" @click="submitPassword">
              {{ savingPassword ? '更新中...' : '更新密码' }}
            </button>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
