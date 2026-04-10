<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { getTimeline } from '@/api/episodeLog'
import LogTimelineItem from '@/components/LogTimelineItem.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import type { EpisodeLog } from '@/types/models'

const filters = reactive({
  pageNum: 1,
  pageSize: 8,
})

const logs = ref<EpisodeLog[]>([])
const total = ref(0)
const loading = ref(false)
const errorMessage = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / filters.pageSize)))

async function loadLogs() {
  loading.value = true
  errorMessage.value = ''

  try {
    const page = await getTimeline({
      pageNum: filters.pageNum,
      pageSize: filters.pageSize,
    })
    logs.value = page.list
    total.value = page.total
  } catch (error) {
    logs.value = []
    total.value = 0
    errorMessage.value = error instanceof Error ? error.message : '加载日志总览失败。'
  } finally {
    loading.value = false
  }
}

watch(
  () => filters.pageNum,
  () => {
    void loadLogs()
  },
)

onMounted(() => {
  void loadLogs()
})
</script>

<template>
  <div class="page-stack">
    <section class="split-hero">
      <SectionTitle
        eyebrow="Episode Archive"
        title="已记录集数"
        description="这里汇总你记下的所有单集日志，方便按时间回看每一集留下的感受与片段。"
      />
      <div class="soft-panel soft-panel--stack">
        <strong>{{ total }} 条已记录内容</strong>
        <p>这个页面专注于“集数记录”本身；如果要改追番状态和进度，仍然去“我的追番”或番剧详情页。</p>
      </div>
    </section>

    <p v-if="errorMessage" class="form-message form-message--error">{{ errorMessage }}</p>

    <section class="content-panel">
      <div v-if="loading" class="skeleton-list" aria-hidden="true">
        <div v-for="index in 4" :key="index" class="skeleton-card skeleton-card--compact" />
      </div>
      <div v-else-if="!logs.length" class="empty-state">
        <p>你还没有记录过任何集数，去任意番剧详情页写下第一条单集日志吧。</p>
        <RouterLink class="primary-button" to="/library">去我的追番</RouterLink>
      </div>
      <div v-else class="timeline-list">
        <article v-for="item in logs" :key="item.id" class="log-entry-wrap">
          <LogTimelineItem :item="item" />
          <div class="timeline-entry-actions">
            <RouterLink v-if="item.animeId" class="ghost-button" :to="`/anime/${item.animeId}`">
              查看对应番剧
            </RouterLink>
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
