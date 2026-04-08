import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { Anime, AnimeSearchSession } from '@/types/models'

const defaultSession = (): AnimeSearchSession => ({
  keyword: '',
  results: [],
  message: '',
  isError: false,
  followedAnimeIds: [],
  hasSearched: false,
})

export const useAnimeSearchStore = defineStore('animeSearch', () => {
  const keyword = ref('')
  const results = ref<Anime[]>([])
  const message = ref('')
  const isError = ref(false)
  const followedAnimeIds = ref<number[]>([])
  const hasSearched = ref(false)
  const globalSearchToken = ref(0)
  const preserveForNextDetailReturn = ref(false)
  const pendingGlobalSearch = ref(false)

  function hydrate() {
    return
  }

  function setSearchState(next: Partial<AnimeSearchSession>) {
    if (next.keyword !== undefined) keyword.value = next.keyword
    if (next.results !== undefined) results.value = next.results
    if (next.message !== undefined) message.value = next.message
    if (next.isError !== undefined) isError.value = next.isError
    if (next.followedAnimeIds !== undefined) followedAnimeIds.value = next.followedAnimeIds
    if (next.hasSearched !== undefined) hasSearched.value = next.hasSearched
  }

  function setFollowedAnimeIds(ids: number[]) {
    followedAnimeIds.value = ids
  }

  function markAnimeFollowed(id: number) {
    if (!followedAnimeIds.value.includes(id)) {
      followedAnimeIds.value = [...followedAnimeIds.value, id]
    }
  }

  function requestGlobalSearch(nextKeyword: string) {
    keyword.value = nextKeyword.trim()
    message.value = ''
    isError.value = false
    pendingGlobalSearch.value = true
    globalSearchToken.value += 1
  }

  function consumePendingGlobalSearch() {
    const current = pendingGlobalSearch.value
    pendingGlobalSearch.value = false
    return current
  }

  function markPreserveDetailReturn() {
    preserveForNextDetailReturn.value = true
  }

  function consumePreserveDetailReturn() {
    const current = preserveForNextDetailReturn.value
    preserveForNextDetailReturn.value = false
    return current
  }

  function clearSearchState() {
    const empty = defaultSession()
    keyword.value = empty.keyword
    results.value = empty.results
    message.value = empty.message
    isError.value = empty.isError
    followedAnimeIds.value = empty.followedAnimeIds
    hasSearched.value = empty.hasSearched
  }

  return {
    keyword,
    results,
    message,
    isError,
    followedAnimeIds,
    hasSearched,
    globalSearchToken,
    hydrate,
    setSearchState,
    setFollowedAnimeIds,
    markAnimeFollowed,
    requestGlobalSearch,
    consumePendingGlobalSearch,
    markPreserveDetailReturn,
    consumePreserveDetailReturn,
    clearSearchState,
  }
})
