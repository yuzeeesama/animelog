<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { followExternalAnime, getAnimeDetail, getExternalAnimeDetail } from '@/api/anime'
import {
  createEpisodeLog,
  deleteEpisodeLog,
  getAnimeLogs,
  updateEpisodeLog,
} from '@/api/episodeLog'
import { createUserAnime, getUserAnimeList, updateUserAnime } from '@/api/userAnime'
import LogTimelineItem from '@/components/LogTimelineItem.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { WATCH_STATUS_OPTIONS } from '@/constants/anime'
import type {
  Anime,
  EpisodeLog,
  EpisodeLogPayload,
  UserAnime,
  UserAnimePayload,
} from '@/types/models'
import { clampProgress, formatDate } from '@/utils/format'
import {
  firstValidationMessage,
  type ValidationErrors,
  validateEpisodeLogForm,
  validateUserAnimeForm,
} from '@/utils/validation'

const route = useRoute()
const animeId = computed(() => Number(route.params.id))
const externalProvider = computed(() => String(route.params.provider || ''))
const externalSubjectId = computed(() => Number(route.params.subjectId))
const isExternalRoute = computed(() => Boolean(route.params.provider && route.params.subjectId))

const anime = ref<Anime | null>(null)
const userAnime = ref<UserAnime | null>(null)
const logs = ref<EpisodeLog[]>([])
const loading = ref(true)
const logsLoading = ref(true)
const message = ref('')
const isError = ref(false)
const savingFollow = ref(false)
const savingLog = ref(false)
const removingLogId = ref<number | null>(null)

const followForm = reactive<UserAnimePayload>({
  animeId: animeId.value,
  watchStatus: 0,
  currentEpisode: 0,
  score: undefined,
  isFavorite: 0,
  startDate: '',
  finishDate: '',
  remark: '',
})

const logForm = reactive<EpisodeLogPayload>({
  animeId: animeId.value,
  userAnimeId: undefined,
  episodeNo: 1,
  episodeTitle: '',
  content: '',
  moodTag: '',
  score: undefined,
  isHighlight: 0,
  watchedAt: '',
})

const editingLogId = ref<number | null>(null)
const followErrors = ref<ValidationErrors<keyof UserAnimePayload>>({})
const logErrors = ref<ValidationErrors<keyof EpisodeLogPayload>>({})

const progressPercent = computed(() => {
  if (!userAnime.value) return 0
  return clampProgress(userAnime.value.currentEpisode, userAnime.value.totalEpisodes)
})

const canWriteLog = computed(() => Boolean(userAnime.value && anime.value?.id))

function setMessage(next: string, error = false) {
  message.value = next
  isError.value = error
}

async function findCurrentUserAnime(targetAnime: Anime) {
  if (!targetAnime.id) {
    return null
  }

  let pageNum = 1
  let totalPages = 1

  while (pageNum <= totalPages) {
    const page = await getUserAnimeList({
      keyword: targetAnime.name,
      pageNum,
      pageSize: 20,
    })

    totalPages = Math.max(1, Math.ceil(page.total / 20))
    const matched = page.list.find((item) => item.animeId === targetAnime.id)
    if (matched) return matched
    pageNum += 1
  }

  return null
}

function syncFollowForm() {
  Object.assign(followForm, {
    animeId: anime.value?.id ?? animeId.value,
    watchStatus: userAnime.value?.watchStatus ?? 0,
    currentEpisode: userAnime.value?.currentEpisode ?? 0,
    score: userAnime.value?.score,
    isFavorite: userAnime.value?.isFavorite ?? 0,
    startDate: userAnime.value?.startDate ?? '',
    finishDate: userAnime.value?.finishDate ?? '',
    remark: userAnime.value?.remark ?? '',
  })
}

function resetLogForm() {
  editingLogId.value = null
  logErrors.value = {}
  Object.assign(logForm, {
    animeId: anime.value?.id ?? animeId.value,
    userAnimeId: userAnime.value?.id,
    episodeNo: Math.max(1, (userAnime.value?.currentEpisode ?? 0) + 1),
    episodeTitle: '',
    content: '',
    moodTag: '',
    score: undefined,
    isHighlight: 0,
    watchedAt: '',
  })
}

async function loadDetail() {
  loading.value = true
  logsLoading.value = true

  try {
    const animeDetail = isExternalRoute.value
      ? await getExternalAnimeDetail(externalProvider.value, externalSubjectId.value)
      : await getAnimeDetail(animeId.value)
    anime.value = animeDetail

    if (animeDetail.id) {
      const [matchedUserAnime, animeLogs] = await Promise.all([
        findCurrentUserAnime(animeDetail),
        getAnimeLogs(animeDetail.id),
      ])
      userAnime.value = matchedUserAnime
      logs.value = animeLogs
    } else {
      userAnime.value = null
      logs.value = []
    }

    syncFollowForm()
    resetLogForm()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '加载番剧详情失败。', true)
  } finally {
    loading.value = false
    logsLoading.value = false
  }
}

async function saveFollow() {
  const resolvedAnimeId = anime.value?.id ?? 0
  const payload: UserAnimePayload = {
    animeId: resolvedAnimeId,
    watchStatus: followForm.watchStatus,
    currentEpisode: Number(followForm.currentEpisode),
    score:
      followForm.score === undefined || followForm.score === null || followForm.score === 0
        ? undefined
        : Number(followForm.score),
    isFavorite: followForm.isFavorite ?? 0,
    startDate: followForm.startDate || null,
    finishDate: followForm.finishDate || null,
    remark: followForm.remark?.trim() || '',
  }

  const shouldFollowExternal = Boolean(
    !userAnime.value &&
      anime.value?.external &&
      anime.value?.sourceProvider &&
      anime.value?.sourceSubjectId &&
      !anime.value?.id,
  )

  const errors = validateUserAnimeForm({
    ...payload,
    animeId: shouldFollowExternal ? 1 : payload.animeId,
  })
  followErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  savingFollow.value = true

  try {
    if (userAnime.value) {
      await updateUserAnime(userAnime.value.id, payload)
      setMessage('修改成功')
    } else if (shouldFollowExternal && anime.value?.sourceProvider && anime.value.sourceSubjectId) {
      await followExternalAnime({
        sourceProvider: anime.value.sourceProvider,
        sourceSubjectId: anime.value.sourceSubjectId,
        watchStatus: payload.watchStatus,
        currentEpisode: payload.currentEpisode,
        score: payload.score,
        isFavorite: payload.isFavorite,
        startDate: payload.startDate,
        finishDate: payload.finishDate,
        remark: payload.remark,
      })
      setMessage('添加成功')
    } else {
      await createUserAnime(payload)
      setMessage('添加成功')
    }

    await loadDetail()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '保存追番状态失败。', true)
  } finally {
    savingFollow.value = false
  }
}

function editLog(item: EpisodeLog) {
  editingLogId.value = item.id
  logErrors.value = {}
  Object.assign(logForm, {
    animeId: anime.value?.id ?? animeId.value,
    userAnimeId: userAnime.value?.id,
    episodeNo: item.episodeNo,
    episodeTitle: item.episodeTitle ?? '',
    content: item.content,
    moodTag: item.moodTag ?? '',
    score: item.score,
    isHighlight: item.isHighlight ?? 0,
    watchedAt: item.watchedAt ? item.watchedAt.slice(0, 16) : '',
  })
}

async function saveLog() {
  if (!anime.value?.id || !userAnime.value) {
    setMessage('请先将这部番剧加入我的追番，再记录单集日志。', true)
    return
  }

  const payload: EpisodeLogPayload = {
    animeId: anime.value.id,
    userAnimeId: userAnime.value?.id,
    episodeNo: Number(logForm.episodeNo),
    episodeTitle: logForm.episodeTitle?.trim() || undefined,
    content: logForm.content.trim(),
    moodTag: logForm.moodTag?.trim() || undefined,
    score:
      logForm.score === undefined || logForm.score === null || logForm.score === 0
        ? undefined
        : Number(logForm.score),
    isHighlight: logForm.isHighlight ?? 0,
    watchedAt: logForm.watchedAt || undefined,
  }

  const errors = validateEpisodeLogForm(payload)
  logErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  savingLog.value = true

  try {
    if (editingLogId.value) {
      await updateEpisodeLog(editingLogId.value, payload)
      setMessage('修改成功')
    } else {
      await createEpisodeLog(payload)
      setMessage('保存成功')
    }

    await loadDetail()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '保存日志失败。', true)
  } finally {
    savingLog.value = false
  }
}

async function removeLog(id: number) {
  const confirmed = window.confirm('确定要删除这条单集日志吗？')
  if (!confirmed) return

  removingLogId.value = id

  try {
    await deleteEpisodeLog(id)
    setMessage('删除成功')
    if (editingLogId.value === id) {
      resetLogForm()
    }
    await loadDetail()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '删除日志失败。', true)
  } finally {
    removingLogId.value = null
  }
}

onMounted(() => {
  void loadDetail()
})
</script>

<template>
  <div class="page-stack">
    <p v-if="message" class="form-message" :class="isError ? 'form-message--error' : 'form-message--success'">
      {{ message }}
    </p>
    <p v-if="anime?.fallbackSource" class="status-note">Bangumi API 暂时不可用，当前详情已切换到备用数据源。</p>

    <div v-if="loading" class="skeleton-list" aria-hidden="true">
      <div class="skeleton-card" />
      <div class="skeleton-card" />
    </div>

    <template v-else-if="anime">
      <section class="detail-hero">
        <div class="detail-cover">
          <img v-if="anime.coverUrl" :src="anime.coverUrl" :alt="anime.name" />
          <div v-else class="detail-cover-placeholder">{{ anime.name.slice(0, 1) }}</div>
        </div>

        <div class="detail-copy">
          <span class="section-eyebrow">Anime Detail</span>
          <h1>{{ anime.name }}</h1>
          <p class="detail-subtitle">
            {{ anime.originalName || '没有原始标题' }} · {{ anime.type || '未分类' }} ·
            {{ anime.totalEpisodes }} 集
          </p>
          <p class="detail-description">{{ anime.synopsis || '这部番剧暂时还没有简介。' }}</p>

          <div class="detail-facts">
            <span>年份 {{ anime.releaseYear || '未知' }}</span>
            <span>季度 {{ anime.season || '未设置' }}</span>
            <span>放送 {{ anime.airDate || '未知' }}</span>
            <span>来源 {{ anime.sourceType || '未设置' }}</span>
            <span>评分 {{ anime.ratingScore ?? '暂无' }}</span>
            <span v-if="anime.createdAt">创建于 {{ formatDate(anime.createdAt) }}</span>
          </div>

          <div v-if="userAnime" class="progress-summary">
            <div class="progress-track">
              <span class="progress-fill" :style="{ width: `${progressPercent}%` }" />
            </div>
            <p>{{ userAnime.currentEpisode }}/{{ userAnime.totalEpisodes }} 集 · {{ progressPercent }}% 已观看</p>
          </div>
          <div v-else class="status-note">
            这部番剧还没有加入你的追番列表，加入后就能继续记录进度和单集日志。
          </div>
        </div>
      </section>

      <section class="content-grid content-grid--detail">
        <div class="content-panel">
          <SectionTitle
            eyebrow="Follow"
            title="追番状态"
            description="管理观看状态、当前进度、评分和自己的备注。"
          />

          <div class="form-stack">
            <label>
              <span>追番状态</span>
              <select v-model.number="followForm.watchStatus">
                <option v-for="status in WATCH_STATUS_OPTIONS" :key="status.value" :value="status.value">
                  {{ status.label }}
                </option>
              </select>
              <small v-if="followErrors.watchStatus" class="field-error">{{ followErrors.watchStatus }}</small>
            </label>
            <label>
              <span>当前集数</span>
              <input v-model.number="followForm.currentEpisode" type="number" min="0" />
              <small v-if="followErrors.currentEpisode" class="field-error">{{ followErrors.currentEpisode }}</small>
            </label>
            <label>
              <span>评分</span>
              <input v-model.number="followForm.score" type="number" min="0" max="10" step="0.5" />
              <small v-if="followErrors.score" class="field-error">{{ followErrors.score }}</small>
            </label>
            <label class="toggle-line">
              <input v-model.number="followForm.isFavorite" :true-value="1" :false-value="0" type="checkbox" />
              <span>加入收藏</span>
            </label>
            <label>
              <span>开始日期</span>
              <input v-model="followForm.startDate" type="date" />
            </label>
            <label>
              <span>结束日期</span>
              <input v-model="followForm.finishDate" type="date" />
              <small v-if="followErrors.finishDate" class="field-error">{{ followErrors.finishDate }}</small>
            </label>
            <label>
              <span>备注</span>
              <textarea v-model.trim="followForm.remark" rows="4" placeholder="留一句给未来回看的自己" />
            </label>
            <button class="primary-button" type="button" :disabled="savingFollow" @click="saveFollow">
              {{ savingFollow ? '保存中...' : userAnime ? '保存追番状态' : '加入我的追番' }}
            </button>
          </div>
        </div>

        <div class="content-panel">
          <SectionTitle
            eyebrow="Episode Log"
            title="写单集日志"
            description="记录每一集的感受、心情标签和最想留住的片刻。"
          />

          <div class="form-stack">
            <p v-if="!canWriteLog" class="status-note">先把这部番剧加入我的追番，单集日志才会和你的进度关联起来。</p>
            <label>
              <span>第几集</span>
              <input v-model.number="logForm.episodeNo" type="number" min="1" />
              <small v-if="logErrors.episodeNo" class="field-error">{{ logErrors.episodeNo }}</small>
            </label>
            <label>
              <span>标题</span>
              <input v-model.trim="logForm.episodeTitle" type="text" placeholder="例如：真正的勇者" />
              <small v-if="logErrors.episodeTitle" class="field-error">{{ logErrors.episodeTitle }}</small>
            </label>
            <label>
              <span>日志内容</span>
              <textarea v-model.trim="logForm.content" rows="5" placeholder="写下这一集最打动你的片段" />
              <small v-if="logErrors.content" class="field-error">{{ logErrors.content }}</small>
            </label>
            <label>
              <span>心情标签</span>
              <input v-model.trim="logForm.moodTag" type="text" placeholder="例如：泪目 / 热血 / 治愈" />
              <small v-if="logErrors.moodTag" class="field-error">{{ logErrors.moodTag }}</small>
            </label>
            <label>
              <span>评分</span>
              <input v-model.number="logForm.score" type="number" min="0" max="10" step="0.5" />
              <small v-if="logErrors.score" class="field-error">{{ logErrors.score }}</small>
            </label>
            <label>
              <span>观看时间</span>
              <input v-model="logForm.watchedAt" type="datetime-local" />
            </label>
            <label class="toggle-line">
              <input v-model.number="logForm.isHighlight" :true-value="1" :false-value="0" type="checkbox" />
              <span>标记为神回</span>
            </label>

            <div class="editor-actions">
              <button class="primary-button" type="button" :disabled="savingLog" @click="saveLog">
                {{ savingLog ? '提交中...' : editingLogId ? '保存日志修改' : '保存日志' }}
              </button>
              <button class="ghost-button" type="button" :disabled="savingLog" @click="resetLogForm">清空</button>
            </div>
          </div>
        </div>
      </section>

      <section class="content-panel">
        <SectionTitle
          eyebrow="Archive"
          title="这部番的日志"
          description="按时间回看这部作品在你这里留下的所有情绪和片段。"
        />

        <div v-if="logsLoading" class="skeleton-list" aria-hidden="true">
          <div v-for="index in 3" :key="index" class="skeleton-card skeleton-card--compact" />
        </div>
        <div v-else-if="!logs.length" class="empty-state">
          还没有日志，写下第一条后这里就会开始生长。
        </div>
        <div v-else class="timeline-list">
          <div v-for="item in logs" :key="item.id" class="log-entry-wrap">
            <LogTimelineItem :item="item" />
            <div class="library-actions library-actions--compact">
              <button class="ghost-button" type="button" :disabled="savingLog" @click="editLog(item)">编辑</button>
              <button
                class="ghost-button ghost-button--danger"
                type="button"
                :disabled="removingLogId === item.id"
                @click="removeLog(item.id)"
              >
                {{ removingLogId === item.id ? '删除中...' : '删除' }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>
