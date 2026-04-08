import type { PageResult } from './api'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
  email?: string
  bio?: string
  role?: number
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface Anime {
  id?: number | null
  name: string
  originalName?: string
  coverUrl?: string
  totalEpisodes: number
  type?: string
  sourceType?: string
  releaseYear?: number
  season?: string
  synopsis?: string
  sourceProvider?: string
  sourceSubjectId?: number
  external?: boolean
  fallbackSource?: string
  airDate?: string
  ratingScore?: number
  createdAt?: string
}

export type WatchStatus = 0 | 1 | 2 | 3 | 4

export interface UserAnime {
  id: number
  animeId: number
  animeName: string
  coverUrl?: string
  totalEpisodes: number
  type?: string
  watchStatus: WatchStatus
  currentEpisode: number
  score?: number
  isFavorite?: number
  startDate?: string | null
  finishDate?: string | null
  lastWatchTime?: string
  remark?: string
}

export interface UserAnimeStatistics {
  wantWatchCount: number
  watchingCount: number
  watchedCount: number
  shelvedCount: number
  droppedCount: number
  totalEpisodesWatched: number
}

export interface EpisodeLog {
  id: number
  animeId: number
  userAnimeId?: number
  episodeNo: number
  episodeTitle?: string
  content: string
  moodTag?: string
  score?: number
  isHighlight?: number
  watchedAt?: string
  createdAt?: string
  animeName?: string
  coverUrl?: string
}

export interface AuthSession {
  token: string
  userInfo: UserInfo
}

export interface UserRegisterPayload {
  username: string
  password: string
  nickname: string
  email?: string
}

export interface UserLoginPayload {
  username: string
  password: string
}

export interface UserProfilePayload {
  nickname: string
  avatarUrl?: string
  email?: string
  bio?: string
}

export interface UserPasswordPayload {
  oldPassword: string
  newPassword: string
}

export interface AnimePayload {
  name: string
  originalName?: string
  coverUrl?: string
  totalEpisodes: number
  type?: string
  sourceType?: string
  releaseYear?: number
  season?: string
  synopsis?: string
}

export interface UserAnimePayload {
  animeId: number
  watchStatus: WatchStatus
  currentEpisode: number
  score?: number
  isFavorite?: number
  startDate?: string | null
  finishDate?: string | null
  remark?: string
}

export interface ExternalAnimeFollowPayload {
  sourceProvider: string
  sourceSubjectId: number
  watchStatus: WatchStatus
  currentEpisode: number
  score?: number
  isFavorite?: number
  startDate?: string | null
  finishDate?: string | null
  remark?: string
}

export interface UserAnimeQuery {
  watchStatus?: WatchStatus
  pageNum?: number
  pageSize?: number
  keyword?: string
}

export interface EpisodeLogPayload {
  animeId: number
  userAnimeId?: number
  episodeNo: number
  episodeTitle?: string
  content: string
  moodTag?: string
  score?: number
  isHighlight?: number
  watchedAt?: string
}

export interface EpisodeLogQuery {
  pageNum?: number
  pageSize?: number
  animeId?: number
}

export type PagedUserAnime = PageResult<UserAnime>
export type PagedEpisodeLog = PageResult<EpisodeLog>
