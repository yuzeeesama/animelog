import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfo, loginUser } from '@/api/user'
import type { AuthSession, UserInfo, UserLoginPayload } from '@/types/models'

const STORAGE_KEY = 'anime-log-auth'

function parseStoredSession(): AuthSession | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as AuthSession
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export function getStoredSession() {
  return parseStoredSession()
}

export function getStoredToken() {
  return parseStoredSession()?.token ?? ''
}

export function persistStoredSession(session: AuthSession) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearStoredSession() {
  localStorage.removeItem(STORAGE_KEY)
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const userInfo = ref<UserInfo | null>(null)
  const hydrated = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))

  function hydrate() {
    if (hydrated.value) return
    const stored = getStoredSession()
    if (stored) {
      token.value = stored.token
      userInfo.value = stored.userInfo
    }
    hydrated.value = true
  }

  function setSession(session: AuthSession) {
    token.value = session.token
    userInfo.value = session.userInfo
    persistStoredSession(session)
  }

  async function login(payload: UserLoginPayload) {
    const session = await loginUser(payload)
    setSession(session)
    return session
  }

  async function refreshUserInfo() {
    const latest = await getUserInfo()
    userInfo.value = latest

    if (token.value) {
      persistStoredSession({
        token: token.value,
        userInfo: latest,
      })
    }

    return latest
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    clearStoredSession()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    hydrate,
    login,
    logout,
    setSession,
    refreshUserInfo,
  }
})
