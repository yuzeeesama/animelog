<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { RouterLink } from 'vue-router'
import { followExternalAnime, searchAnime } from '@/api/anime'
import { createUserAnime, getUserAnimeList } from '@/api/userAnime'
import SectionTitle from '@/components/SectionTitle.vue'
import type { Anime, UserAnime } from '@/types/models'
import { useAnimeSearchStore } from '@/stores/animeSearch'

const searchStore = useAnimeSearchStore()
searchStore.hydrate()

const { keyword, results, message, isError, hasSearched, globalSearchToken } = storeToRefs(searchStore)

const loading = ref(false)
const followLoadingId = ref<number | null>(null)
const loadingFollowed = ref(false)
const handledGlobalToken = ref(0)

const hasKeyword = computed(() => Boolean(keyword.value.trim()))
const fallbackNotice = computed(() =>
  results.value.some((item) => item.fallbackSource)
    ? 'Bangumi API 暂时不可用，当前结果已切换到备用数据源。'
    : '',
)

function setMessage(next: string, error = false) {
  searchStore.setSearchState({
    message: next,
    isError: error,
  })
}

async function loadFollowedAnimeMap(targets: Anime[]) {
  const candidates = targets.filter((item) => item.id != null)
  if (candidates.length === 0) {
    searchStore.setFollowedAnimeIds([])
    return
  }

  loadingFollowed.value = true
  const matchedIds = new Set<number>()
  let pageNum = 1
  let totalPages = 1
  const byId = new Map<number, Anime>(
    candidates.map((item) => [item.id as number, item]),
  )

  try {
    while (pageNum <= totalPages) {
      const page = await getUserAnimeList({
        pageNum,
        pageSize: 100,
      })

      totalPages = Math.max(1, Math.ceil(page.total / 100))
      page.list.forEach((item: UserAnime) => {
        if (byId.has(item.animeId)) {
          matchedIds.add(item.animeId)
        }
      })
      pageNum += 1
    }

    searchStore.setFollowedAnimeIds(Array.from(matchedIds))
  } finally {
    loadingFollowed.value = false
  }
}

async function runSearch() {
  const nextKeyword = keyword.value.trim()
  if (!nextKeyword) {
    searchStore.clearSearchState()
    setMessage('请输入番剧名称后再搜索。', true)
    return
  }

  loading.value = true

  try {
    const animeList = await searchAnime(nextKeyword)
    searchStore.setSearchState({
      keyword: nextKeyword,
      results: animeList,
      followedAnimeIds: [],
      hasSearched: true,
    })
    void loadFollowedAnimeMap(animeList)

    if (animeList.length === 0) {
      setMessage('暂时没有找到匹配的番剧，可以换个别名或日文名试试。', true)
    } else {
      setMessage(`找到 ${animeList.length} 部相关番剧，可以先看信息，再决定要不要追。`)
    }
  } catch (error) {
    searchStore.setSearchState({
      keyword: nextKeyword,
      results: [],
      followedAnimeIds: [],
      hasSearched: true,
    })
    setMessage(error instanceof Error ? error.message : '搜索番剧失败。', true)
  } finally {
    loading.value = false
  }
}

async function followAnime(anime: Anime) {
  const animeKey = anime.id ?? anime.sourceSubjectId ?? null
  followLoadingId.value = animeKey

  try {
    if (anime.external && anime.sourceProvider && anime.sourceSubjectId && !anime.id) {
      const localAnimeId = await followExternalAnime({
        sourceProvider: anime.sourceProvider,
        sourceSubjectId: anime.sourceSubjectId,
        watchStatus: 0,
        currentEpisode: 0,
        isFavorite: 0,
        remark: '',
      })
      anime.id = localAnimeId
      searchStore.setSearchState({ results: results.value })
    } else if (anime.id) {
      await createUserAnime({
        animeId: anime.id,
        watchStatus: 0,
        currentEpisode: 0,
        isFavorite: 0,
        remark: '',
      })
    } else {
      throw new Error('当前番剧缺少可追番的标识')
    }
    if (anime.id) {
      searchStore.markAnimeFollowed(anime.id)
    }
    setMessage(`《${anime.name}》已经加入你的追番列表。`)
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '加入追番失败。', true)
  } finally {
    followLoadingId.value = null
  }
}

function isFollowed(anime: Anime) {
  return anime.id != null && searchStore.followedAnimeIds.includes(anime.id)
}

function detailRoute(anime: Anime) {
  if (anime.external && anime.sourceProvider && anime.sourceSubjectId) {
    return `/anime/external/${anime.sourceProvider}/${anime.sourceSubjectId}`
  }
  return anime.id ? `/anime/${anime.id}` : '/anime-search'
}

watch(
  globalSearchToken,
  (token) => {
    if (token <= handledGlobalToken.value) return
    handledGlobalToken.value = token
    searchStore.consumePendingGlobalSearch()
    void runSearch()
  },
  { immediate: true },
)
</script>

<template>
  <div class="page-stack">
    <section class="split-hero">
      <SectionTitle
        eyebrow="Search"
        title="搜索番剧信息"
        description="输入作品名、别名或原名，快速了解番剧资料，再决定是否加入你的追番列表。"
      />
      <div class="soft-panel soft-panel--stack">
        <strong>这里更像一个番剧情报入口。</strong>
        <p>先搜索、先了解，再一键追番，不用先切到编辑页。</p>
      </div>
    </section>

    <section class="content-panel">
      <div class="toolbar toolbar--search-page">
        <label class="search-field search-field--wide">
          <input
            v-model.trim="keyword"
            type="search"
            placeholder="例如：芙莉莲 / Frieren / 葬送のフリーレン"
            @keyup.enter="runSearch"
          />
        </label>
        <button class="primary-button" type="button" :disabled="loading" @click="runSearch">
          {{ loading ? '搜索中...' : '搜索番剧' }}
        </button>
      </div>

      <p v-if="message" class="form-message" :class="isError ? 'form-message--error' : 'form-message--success'">
        {{ message }}
      </p>
      <p v-if="fallbackNotice" class="status-note">{{ fallbackNotice }}</p>
      <p v-if="results.length && loadingFollowed" class="status-note">正在补充你的追番状态标记…</p>

      <div v-if="loading" class="skeleton-list" aria-hidden="true">
        <div v-for="index in 4" :key="index" class="skeleton-card skeleton-card--compact" />
      </div>

      <div v-else-if="hasSearched && hasKeyword && !results.length" class="empty-state">
        没有找到相关番剧，试试别名、原名，或者换更短一点的关键词。
      </div>

      <div v-else-if="results.length" class="search-explorer-grid">
        <article v-for="anime in results" :key="anime.sourceSubjectId ?? anime.id ?? anime.name" class="search-explorer-card">
          <div class="search-explorer-poster">
            <img v-if="anime.coverUrl" :src="anime.coverUrl" :alt="anime.name" />
            <div v-else class="detail-cover-placeholder">{{ anime.name.slice(0, 1) }}</div>
          </div>

          <div class="search-explorer-body">
            <div class="search-explorer-head">
              <div>
                <span class="section-eyebrow">Anime Info</span>
                <h2>{{ anime.name }}</h2>
                <p class="detail-subtitle">
                  {{ anime.originalName || '暂无原始标题' }}
                </p>
              </div>
              <span class="soft-tag">{{ anime.type || '未分类' }}</span>
            </div>

            <div class="detail-facts">
              <span>年份 {{ anime.releaseYear || '未知' }}</span>
              <span>季度 {{ anime.season || '未设置' }}</span>
              <span>放送 {{ anime.airDate || '未知' }}</span>
              <span>集数 {{ anime.totalEpisodes || 0 }}</span>
              <span>来源 {{ anime.sourceType || '未设置' }}</span>
              <span>评分 {{ anime.ratingScore ?? '暂无' }}</span>
            </div>

            <p class="search-explorer-description">
              {{ anime.synopsis || '这部番剧暂时还没有简介，可以先点详情继续查看。' }}
            </p>

            <div class="search-explorer-actions">
              <RouterLink class="ghost-button" :to="detailRoute(anime)">查看详情</RouterLink>
              <RouterLink
                v-if="isFollowed(anime)"
                class="ghost-button"
                to="/library"
              >
                已在追番中
              </RouterLink>
              <button
                v-else
                class="primary-button"
                type="button"
                :disabled="followLoadingId === (anime.id ?? anime.sourceSubjectId ?? null)"
                @click="followAnime(anime)"
              >
                {{ followLoadingId === (anime.id ?? anime.sourceSubjectId ?? null) ? '加入中...' : '追番' }}
              </button>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        先输入你想了解的番剧名称，这里会展示简介、年份、季度、集数，并支持直接开始追番。
      </div>
    </section>
  </div>
</template>
