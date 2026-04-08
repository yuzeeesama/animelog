import { http } from '@/lib/http'
import type { PageResult } from '@/types/api'
import type { EpisodeLog, EpisodeLogPayload, EpisodeLogQuery } from '@/types/models'

export function createEpisodeLog(payload: EpisodeLogPayload) {
  return http.post<null>('/episode-log', payload)
}

export function getEpisodeLogDetail(id: number) {
  return http.get<EpisodeLog>(`/episode-log/${id}`)
}

export function updateEpisodeLog(id: number, payload: EpisodeLogPayload) {
  return http.put<null>(`/episode-log/${id}`, payload)
}

export function deleteEpisodeLog(id: number) {
  return http.delete<null>(`/episode-log/${id}`)
}

export function getAnimeLogs(animeId: number) {
  return http.get<EpisodeLog[]>(`/episode-log/anime/${animeId}`)
}

export function getTimeline(query: EpisodeLogQuery) {
  return http.get<PageResult<EpisodeLog>>('/episode-log/timeline', query)
}

export function getHighlightLogs(query: EpisodeLogQuery) {
  return http.get<PageResult<EpisodeLog>>('/episode-log/highlight/list', query)
}
