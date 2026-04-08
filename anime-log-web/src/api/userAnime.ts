import { http } from '@/lib/http'
import type { PageResult } from '@/types/api'
import type {
  UserAnime,
  UserAnimePayload,
  UserAnimeQuery,
  UserAnimeStatistics,
} from '@/types/models'

export function createUserAnime(payload: UserAnimePayload) {
  return http.post<null>('/user-anime', payload)
}

export function getUserAnimeList(query: UserAnimeQuery) {
  return http.get<PageResult<UserAnime>>('/user-anime/list', query)
}

export function getUserAnimeDetail(id: number) {
  return http.get<UserAnime>(`/user-anime/${id}`)
}

export function updateUserAnime(id: number, payload: UserAnimePayload) {
  return http.put<null>(`/user-anime/${id}`, payload)
}

export function updateUserAnimeProgress(id: number, currentEpisode: number) {
  return http.put<null>(`/user-anime/${id}/progress`, { currentEpisode })
}

export function deleteUserAnime(id: number) {
  return http.delete<null>(`/user-anime/${id}`)
}

export function getUserAnimeStatistics() {
  return http.get<UserAnimeStatistics>('/user-anime/statistics')
}
