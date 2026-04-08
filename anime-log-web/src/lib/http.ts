import router from '@/router'
import type { ApiResponse } from '@/types/api'
import { clearStoredSession, getStoredToken } from '@/stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

function withBase(path: string) {
  if (path.startsWith('http')) return path
  if (path.startsWith('/api')) return path
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

function buildQuery(params?: object) {
  if (!params) return ''

  const search = new URLSearchParams()

  Object.entries(params as Record<string, unknown>).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    search.append(key, String(value))
  })

  const query = search.toString()
  return query ? `?${query}` : ''
}

async function request<T>(
  path: string,
  init?: RequestInit,
  query?: object,
) {
  const token = getStoredToken()
  const headers = new Headers(init?.headers)

  if (!(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${withBase(path)}${buildQuery(query)}`, {
    ...init,
    headers,
  })

  let payload: ApiResponse<T> | null = null

  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new Error('请求失败，请稍后重试')
    }
  }

  if (response.status === 401 || payload?.code === 401) {
    clearStoredSession()

    if (router.currentRoute.value.path !== '/login') {
      void router.replace('/login')
    }

    throw new Error(payload?.message ?? '未登录或登录已失效')
  }

  if (!response.ok) {
    throw new Error(payload?.message ?? '请求失败，请稍后重试')
  }

  if (!payload) {
    throw new Error('返回数据格式不正确')
  }

  if (payload.code !== 200) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export const http = {
  get<T>(path: string, query?: object) {
    return request<T>(path, { method: 'GET' }, query)
  },
  post<T>(path: string, body?: unknown) {
    return request<T>(path, {
      method: 'POST',
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  },
  put<T>(path: string, body?: unknown) {
    return request<T>(path, {
      method: 'PUT',
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  },
  delete<T>(path: string) {
    return request<T>(path, { method: 'DELETE' })
  },
}
