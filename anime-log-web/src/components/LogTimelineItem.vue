<script setup lang="ts">
import type { EpisodeLog } from '@/types/models'
import { formatDateTime } from '@/utils/format'

defineProps<{
  item: EpisodeLog
}>()
</script>

<template>
  <article class="timeline-item">
    <div class="timeline-dot" aria-hidden="true" />
    <div class="timeline-card">
      <div class="timeline-card-head">
        <div>
          <p class="timeline-kicker">{{ item.animeName || '追番日志' }}</p>
          <h3>第 {{ item.episodeNo }} 集 {{ item.episodeTitle || '' }}</h3>
        </div>
        <span class="timeline-date">{{ formatDateTime(item.watchedAt || item.createdAt) }}</span>
      </div>
      <p class="timeline-content">{{ item.content }}</p>
      <div class="timeline-tags">
        <span v-if="item.moodTag" class="soft-tag">{{ item.moodTag }}</span>
        <span v-if="item.score !== undefined && item.score !== null" class="soft-tag">评分 {{ item.score }}</span>
        <span v-if="item.isHighlight" class="soft-tag soft-tag--highlight">神回</span>
      </div>
    </div>
  </article>
</template>
