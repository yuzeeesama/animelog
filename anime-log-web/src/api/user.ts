import { http } from '@/lib/http'
import type {
  LoginResponse,
  UserInfo,
  UserLoginPayload,
  UserPasswordPayload,
  UserProfilePayload,
  UserRegisterPayload,
} from '@/types/models'

export function registerUser(payload: UserRegisterPayload) {
  return http.post<null>('/user/register', payload)
}

export function loginUser(payload: UserLoginPayload) {
  return http.post<LoginResponse>('/user/login', payload)
}

export function getUserInfo() {
  return http.get<UserInfo>('/user/info')
}

export function updateUserProfile(payload: UserProfilePayload) {
  return http.put<null>('/user/profile', payload)
}

export function updateUserPassword(payload: UserPasswordPayload) {
  return http.put<null>('/user/password', payload)
}
