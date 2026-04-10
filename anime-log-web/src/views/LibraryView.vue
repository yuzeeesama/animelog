<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { deleteUserAnime, getUserAnimeList, updateUserAnimeProgress } from '@/api/userAnime'
import AnimeCard from '@/components/AnimeCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { WATCH_STATUS_OPTIONS } from '@/constants/anime'
import type { UserAnime, WatchStatus } from '@/types/models'

const route = useRoute()
const router = useRouter()

function parseWatchStatusQuery(value: unknown): WatchStatus | undefined {
  const normalized = Array.isArray(value) ? value[0] : value

  if (normalized === undefined || normalized === null || normalized === '') {
    return undefined
  }

  const parsed = Number(normalized)
  if (Number.isInteger(parsed) && parsed >= 0 && parsed <= 4) {
    return parsed as WatchStatus
  }

  return undefined
}

const filters = reactive({
  watchStatus: parseWatchStatusQuery(route.query.watchStatus),
  keyword: '',
  pageNum: 1,
  pageSize: 6,
})

const library = ref<UserAnime[]>([])
const total = ref(0)
const loading = ref(false)
const message = ref('')
const isError = ref(false)
const progressUpdatingId = ref<number | null>(null)
const removingId = ref<number | null>(null)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / filters.pageSize)))

function setMessage(next: string, error = false) {
  message.value = next
  isError.value = error
}

async function loadLibrary() {
  loading.value = true

  try {
    const data = await getUserAnimeList(filters)
    library.value = data.list
    total.value = data.total
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '加载我的追番失败。', true)
  } finally {
    loading.value = false
  }
}

async function updateProgress(item: UserAnime, delta: 1 | -1) {
  if (delta > 0 && item.currentEpisode >= item.totalEpisodes && item.totalEpisodes > 0) {
    setMessage(`《${item.animeName}》已经更新到最后一集了。`, true)
    return
  }

  if (delta < 0 && item.currentEpisode <= 0) {
    setMessage(`《${item.animeName}》已经是第 0 集了。`, true)
    return
  }

  progressUpdatingId.value = item.id

  try {
    await updateUserAnimeProgress(item.id, item.currentEpisode + delta)
    setMessage('修改成功')
    await loadLibrary()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '更新进度失败。', true)
  } finally {
    progressUpdatingId.value = null
  }
}

async function removeItem(item: UserAnime) {
  const confirmed = window.confirm(`确定要从追番列表中移除《${item.animeName}》吗？`)
  if (!confirmed) return

  removingId.value = item.id

  try {
    await deleteUserAnime(item.id)
    setMessage('删除成功')
    await loadLibrary()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '删除追番记录失败。', true)
  } finally {
    removingId.value = null
  }
}

watch(
  () => [filters.watchStatus, filters.keyword, filters.pageNum],
  () => {
    void loadLibrary()
  },
)

watch(
  () => route.query.watchStatus,
  (watchStatus) => {
    const nextStatus = parseWatchStatusQuery(watchStatus)
    if (filters.watchStatus !== nextStatus) {
      filters.watchStatus = nextStatus
      filters.pageNum = 1
    }
  },
)

watch(
  () => filters.watchStatus,
  (watchStatus) => {
    const currentStatus = parseWatchStatusQuery(route.query.watchStatus)
    if (currentStatus === watchStatus) return

    void router.replace({
      query: {
        ...route.query,
        watchStatus: watchStatus === undefined ? undefined : String(watchStatus),
      },
    })
  },
)

onMounted(() => {
  void loadLibrary()
})
</script>

<template>
  <div class="page-stack">
    <section class="split-hero">
      <SectionTitle
        eyebrow="Library"
        title="我的追番"
        description="集中管理已有追番记录，快速调整进度，或进入详情页继续编辑。"
      />
      <div class="soft-panel soft-panel--stack">
        <strong>这里专注于维护已经加入列表的作品。</strong>
        <p>点“编辑”会进入详情页修改状态、评分、日期和备注；新增追番仍在番剧搜索页完成。</p>
      </div>
    </section>

    <section class="content-panel">
      <div class="toolbar">
        <div class="filter-group">
          <button
            class="filter-chip"
            :class="{ 'is-active': filters.watchStatus === undefined }"
            type="button"
            @click="filters.watchStatus = undefined; filters.pageNum = 1"
          >
            全部
          </button>
          <button
            v-for="status in WATCH_STATUS_OPTIONS"
            :key="status.value"
            class="filter-chip"
            :class="{ 'is-active': filters.watchStatus === status.value }"
            type="button"
            @click="filters.watchStatus = status.value; filters.pageNum = 1"
          >
            {{ status.label }}
          </button>
        </div>

        <label class="search-field">
          <input v-model.trim="filters.keyword" type="search" placeholder="搜索我的番剧名称" />
        </label>
      </div>

      <p v-if="message" class="form-message" :class="isError ? 'form-message--error' : 'form-message--success'">
        {{ message }}
      </p>

      <div v-if="loading" class="skeleton-list" aria-hidden="true">
        <div v-for="index in 3" :key="index" class="skeleton-card" />
      </div>
      <div v-else-if="!library.length" class="empty-state">
        <p>当前筛选条件下还没有追番记录。</p>
        <RouterLink class="primary-button" to="/anime-search">去番剧搜索新增追番</RouterLink>
      </div>

      <div v-else class="anime-list">
        <article v-for="item in library" :key="item.id" class="library-row">
          <AnimeCard :item="item" />
          <div class="library-actions">
            <RouterLink class="ghost-button" :to="`/anime/${item.animeId}`">编辑</RouterLink>
            <button
              class="ghost-button"
              type="button"
              :disabled="progressUpdatingId === item.id"
              @click="updateProgress(item, -1)"
            >
              {{ progressUpdatingId === item.id ? '更新中...' : '-1 集' }}
            </button>
            <button
              class="ghost-button"
              type="button"
              :disabled="progressUpdatingId === item.id"
              @click="updateProgress(item, 1)"
            >
              {{ progressUpdatingId === item.id ? '更新中...' : '+1 集' }}
            </button>
            <button
              class="ghost-button ghost-button--danger"
              type="button"
              :disabled="removingId === item.id"
              @click="removeItem(item)"
            >
              {{ removingId === item.id ? '移除中...' : '移除' }}
            </button>
          </div>
        </article>
      </div>

      <div class="pager">
        <button class="ghost-button" type="button" :disabled="filters.pageNum <= 1" @click="filters.pageNum -= 1">
          上一页
        </button>
        <span>第 {{ filters.pageNum }} / {{ totalPages }} 页</span>
        <button
          class="ghost-button"
          type="button"
          :disabled="filters.pageNum >= totalPages"
          @click="filters.pageNum += 1"
        >
          下一页
        </button>
      </div>
    </section>
  </div>
</template>
