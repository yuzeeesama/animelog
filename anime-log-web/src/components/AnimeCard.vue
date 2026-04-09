<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { WATCH_STATUS_META } from '@/constants/anime'
import type { UserAnime } from '@/types/models'
import { clampProgress, formatDateTime } from '@/utils/format'

const props = defineProps<{
  item: UserAnime
}>()

const progress = computed(() => clampProgress(props.item.currentEpisode, props.item.totalEpisodes))
</script>

<template>
  <article class="anime-card">
    <div class="anime-poster-wrap">
      <img
        v-if="item.coverUrl"
        :src="item.coverUrl"
        :alt="item.animeName"
        class="anime-poster"
      />
      <div v-else class="anime-poster anime-poster--placeholder">{{ item.animeName.slice(0, 1) }}</div>
    </div>

    <div class="anime-card-body">
      <div class="anime-card-head">
        <div class="anime-card-copy">
          <span class="status-pill" :data-tone="WATCH_STATUS_META[item.watchStatus].tone">
            {{ WATCH_STATUS_META[item.watchStatus].label }}
          </span>
          <h3>{{ item.animeName }}</h3>
        </div>
        <RouterLink class="anime-card-link text-link" :to="`/anime/${item.animeId}`">查看详情</RouterLink>
      </div>

      <p class="anime-meta">
        {{ item.type || '未分类' }} · {{ item.currentEpisode }}/{{ item.totalEpisodes }} 集
        <span v-if="item.score"> · 评分 {{ item.score }}</span>
      </p>

      <div class="progress-track">
        <span class="progress-fill" :style="{ width: `${progress}%` }" />
      </div>

      <div class="anime-card-footer">
        <span>{{ progress }}% 已完成</span>
        <span>{{ formatDateTime(item.lastWatchTime) }}</span>
      </div>
    </div>
  </article>
</template>
