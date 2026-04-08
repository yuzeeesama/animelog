<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { getTimeline } from '@/api/episodeLog'
import { getUserAnimeList, getUserAnimeStatistics } from '@/api/userAnime'
import AnimeCard from '@/components/AnimeCard.vue'
import LogTimelineItem from '@/components/LogTimelineItem.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { WATCH_STATUS_META } from '@/constants/anime'
import { useAuthStore } from '@/stores/auth'
import type { EpisodeLog, UserAnime, UserAnimeStatistics, WatchStatus } from '@/types/models'

const authStore = useAuthStore()
const statistics = ref<UserAnimeStatistics | null>(null)
const watchingList = ref<UserAnime[]>([])
const timeline = ref<EpisodeLog[]>([])
const loading = ref(true)
const errorMessage = ref('')

const statCards = computed(() => {
  const data = statistics.value
  if (!data) return []

  return [
    { key: 'wantWatchCount', label: '想看', value: data.wantWatchCount, status: 0 as WatchStatus },
    { key: 'watchingCount', label: '在看', value: data.watchingCount, status: 1 as WatchStatus },
    { key: 'watchedCount', label: '看完', value: data.watchedCount, status: 2 as WatchStatus },
    { key: 'totalEpisodesWatched', label: '已记录集数', value: data.totalEpisodesWatched, status: 1 as WatchStatus },
  ]
})

const completionRate = computed(() => {
  const data = statistics.value
  if (!data) return 0

  const totalTracked =
    data.wantWatchCount +
    data.watchingCount +
    data.watchedCount +
    data.shelvedCount +
    data.droppedCount

  if (!totalTracked) return 0

  return Math.round((data.watchedCount / totalTracked) * 100)
})

const weeklyInsight = computed(() => {
  const latest = timeline.value[0]

  if (!latest) {
    return '还没有留下任何观看痕迹，今天适合从一部想看的作品开始。'
  }

  return latest.isHighlight
    ? `最近一次记录来自《${latest.animeName || '追番日志'}》，而且你把它标成了神回。`
    : `最近一次更新停在《${latest.animeName || '追番日志'}》第 ${latest.episodeNo} 集。`
})

const dashboardHighlights = computed(() => {
  const data = statistics.value

  return [
    {
      label: '完结率',
      value: `${completionRate.value}%`,
      description: data ? '已看完作品占当前收藏总量的比例' : '加载后展示你账号下的完结进度',
    },
    {
      label: '日志密度',
      value: `${timeline.value.length} 条`,
      description: timeline.value.length
        ? '最近几条追番记录已经同步到时间线面板'
        : '还没有近期日志，可以从任一番剧详情页开始记录',
    },
  ]
})

async function load() {
  loading.value = true
  errorMessage.value = ''

  try {
    const [stats, library, logs] = await Promise.all([
      getUserAnimeStatistics(),
      getUserAnimeList({ watchStatus: 1, pageNum: 1, pageSize: 4 }),
      getTimeline({ pageNum: 1, pageSize: 5 }),
    ])

    statistics.value = stats
    watchingList.value = library.list
    timeline.value = logs.list
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载工作台失败。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page-stack">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="hero-badge">Worktable</span>
        <h1>把进度、心情和每一集的余韵，写进一条持续生长的追番时间线。</h1>
        <p>
          {{
            authStore.userInfo?.nickname
              ? `${authStore.userInfo.nickname}，继续上次的观看记录吧。`
              : '继续整理你的番剧列表和单集感想。'
          }}
        </p>
        <div class="hero-actions">
          <RouterLink class="primary-button" to="/library">管理我的追番</RouterLink>
          <RouterLink class="ghost-button" to="/profile">编辑个人档案</RouterLink>
        </div>
        <div class="hero-metrics" aria-label="首页摘要">
          <div class="hero-metric-card">
            <span>完结率</span>
            <strong>{{ completionRate }}%</strong>
            <p>让“想看”和“看完”之间的距离更清晰。</p>
          </div>
          <div class="hero-metric-card">
            <span>本周提示</span>
            <strong>{{ timeline.length }} 条新线索</strong>
            <p>{{ weeklyInsight }}</p>
          </div>
        </div>
      </div>

      <div class="hero-aside">
        <div class="hero-note">
          <span>最近节奏</span>
          <strong>{{ watchingList.length }} 部在看</strong>
          <p>把正在追的作品保持在视线中央，切换进度不需要多余操作。</p>
        </div>
        <div class="hero-orbit" />
        <div class="hero-surface" aria-hidden="true" />
      </div>
    </section>

    <p v-if="errorMessage" class="form-message form-message--error">{{ errorMessage }}</p>

    <section class="stats-grid">
      <article v-for="card in statCards" :key="card.key" class="stat-card">
        <span class="status-pill" :data-tone="WATCH_STATUS_META[card.status].tone">
          {{ card.label }}
        </span>
        <strong>{{ card.value }}</strong>
        <p>当前账号下已沉淀的追番记录</p>
      </article>
    </section>

    <section class="dashboard-band">
      <article v-for="item in dashboardHighlights" :key="item.label" class="insight-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.description }}</p>
      </article>
    </section>

    <section class="content-grid">
      <div class="content-panel">
        <SectionTitle
          eyebrow="Now Watching"
          title="继续中的番剧"
          description="把正在看的内容留在首页，一眼就能回到当前进度。"
        />

        <div v-if="loading" class="skeleton-list" aria-hidden="true">
          <div v-for="index in 3" :key="index" class="skeleton-card" />
        </div>
        <div v-else-if="!watchingList.length" class="empty-state">
          你还没有“在看”的番剧，先去我的追番页添加一部吧。
        </div>
        <div v-else class="anime-list">
          <AnimeCard v-for="item in watchingList" :key="item.id" :item="item" />
        </div>
      </div>

      <div class="content-panel">
        <SectionTitle
          eyebrow="Timeline"
          title="最近日志"
          description="每一条日志都是追番记忆的书签。"
        />

        <div v-if="loading" class="skeleton-list" aria-hidden="true">
          <div v-for="index in 4" :key="index" class="skeleton-card skeleton-card--compact" />
        </div>
        <div v-else-if="!timeline.length" class="empty-state">
          还没有任何日志，去番剧详情页写下你的第一条观后感。
        </div>
        <div v-else class="timeline-list">
          <LogTimelineItem v-for="item in timeline" :key="item.id" :item="item" />
        </div>
      </div>
    </section>
  </div>
</template>
