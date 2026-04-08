import { http } from '@/lib/http'
import type { PageResult } from '@/types/api'
import type { Anime, AnimePayload, ExternalAnimeFollowPayload } from '@/types/models'

export function createAnime(payload: AnimePayload) {
  return http.post<null>('/anime', payload)
}

export function getAnimeList(params: {
  pageNum?: number
  pageSize?: number
  name?: string
  type?: string
}) {
  return http.get<PageResult<Anime>>('/anime/list', params)
}

export function getAnimeDetail(id: number) {
  return http.get<Anime>(`/anime/${id}`)
}

export function getExternalAnimeDetail(sourceProvider: string, sourceSubjectId: number) {
  return http.get<Anime>(`/anime/external/${sourceProvider}/${sourceSubjectId}`)
}

export function searchAnime(keyword: string) {
  return http.get<Anime[]>('/anime/search', { keyword })
}

export function followExternalAnime(payload: ExternalAnimeFollowPayload) {
  return http.post<number>('/anime/follow-external', payload)
}
