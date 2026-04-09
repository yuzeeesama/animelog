<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Anime } from '@/types/models'
import { formatDate } from '@/utils/format'

const props = defineProps<{
  item: Anime
}>()

const detailPath = computed(() => {
  if (props.item.id) {
    return `/anime/${props.item.id}`
  }

  if (props.item.sourceProvider && props.item.sourceSubjectId) {
    return `/anime/external/${props.item.sourceProvider}/${props.item.sourceSubjectId}`
  }

  return '/anime-search'
})

const metaText = computed(() => {
  const parts = [props.item.type || '动画']

  if (props.item.totalEpisodes) {
    parts.push(`${props.item.totalEpisodes} 集`)
  }

  if (props.item.airDate) {
    parts.push(`开播 ${formatDate(props.item.airDate)}`)
  }

  return parts.join(' · ')
})
</script>

<template>
  <article class="broadcast-card">
    <RouterLink class="broadcast-card-cover-link" :to="detailPath" :aria-label="`查看 ${item.name} 详情`">
      <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.name" class="broadcast-card-cover" />
      <div v-else class="broadcast-card-cover broadcast-card-cover--placeholder">
        {{ item.name.slice(0, 1) }}
      </div>
    </RouterLink>

    <div class="broadcast-card-body">
      <div class="broadcast-card-copy">
        <span class="section-eyebrow">On Air Today</span>
        <h3>{{ item.name }}</h3>
        <p v-if="item.originalName" class="broadcast-card-original">{{ item.originalName }}</p>
        <p class="anime-meta">{{ metaText }}</p>
      </div>

      <div class="broadcast-card-footer">
        <span v-if="item.ratingScore" class="soft-tag">Bangumi {{ Number(item.ratingScore).toFixed(1) }}</span>
        <RouterLink class="text-link" :to="detailPath">查看详情</RouterLink>
      </div>
    </div>
  </article>
</template>
