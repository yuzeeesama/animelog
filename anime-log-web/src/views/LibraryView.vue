<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { createAnime, followExternalAnime, searchAnime } from '@/api/anime'
import {
  createUserAnime,
  deleteUserAnime,
  getUserAnimeList,
  updateUserAnime,
  updateUserAnimeProgress,
} from '@/api/userAnime'
import AnimeCard from '@/components/AnimeCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { WATCH_STATUS_OPTIONS } from '@/constants/anime'
import type {
  Anime,
  AnimePayload,
  UserAnime,
  UserAnimePayload,
  WatchStatus,
} from '@/types/models'
import {
  firstValidationMessage,
  type ValidationErrors,
  validateAnimeForm,
  validateUserAnimeForm,
} from '@/utils/validation'

const filters = reactive({
  watchStatus: undefined as WatchStatus | undefined,
  keyword: '',
  pageNum: 1,
  pageSize: 6,
})

const library = ref<UserAnime[]>([])
const total = ref(0)
const loading = ref(false)
const message = ref('')
const isError = ref(false)

const animeSearchKeyword = ref('')
const animeSearchResults = ref<Anime[]>([])
const searchingAnime = ref(false)
const showAnimeCreateForm = ref(false)
const creatingAnime = ref(false)
const savingEditor = ref(false)
const advancingId = ref<number | null>(null)
const removingId = ref<number | null>(null)

const editor = reactive<UserAnimePayload>({
  animeId: 0,
  watchStatus: 0,
  currentEpisode: 0,
  score: undefined,
  isFavorite: 0,
  startDate: '',
  finishDate: '',
  remark: '',
})

const animeDraft = reactive<AnimePayload>({
  name: '',
  originalName: '',
  coverUrl: '',
  totalEpisodes: 12,
  type: '',
  sourceType: '',
  releaseYear: undefined,
  season: '',
  synopsis: '',
})

const selectedUserAnimeId = ref<number | null>(null)
const selectedAnimeName = ref('')
const editorErrors = ref<ValidationErrors<keyof UserAnimePayload>>({})
const animeDraftErrors = ref<ValidationErrors<keyof AnimePayload>>({})
const selectedAnime = ref<Anime | null>(null)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / filters.pageSize)))
const editorMode = computed(() => (selectedUserAnimeId.value ? '保存修改' : '加入追番'))
const hasSearchKeyword = computed(() => Boolean(animeSearchKeyword.value.trim()))

function setMessage(next: string, error = false) {
  message.value = next
  isError.value = error
}

function clearEditorErrors() {
  editorErrors.value = {}
}

function clearAnimeDraftErrors() {
  animeDraftErrors.value = {}
}

function resetAnimeDraft() {
  Object.assign(animeDraft, {
    name: animeSearchKeyword.value.trim(),
    originalName: '',
    coverUrl: '',
    totalEpisodes: 12,
    type: '',
    sourceType: '',
    releaseYear: undefined,
    season: '',
    synopsis: '',
  })
  clearAnimeDraftErrors()
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

async function runAnimeSearch() {
  const keyword = animeSearchKeyword.value.trim()

  if (!keyword) {
    animeSearchResults.value = []
    showAnimeCreateForm.value = false
    setMessage('请输入番剧名称后再搜索。', true)
    return
  }

  searchingAnime.value = true
  clearAnimeDraftErrors()

  try {
    const results = await searchAnime(keyword)
    animeSearchResults.value = results
    showAnimeCreateForm.value = results.length === 0
    if (results.length === 0) {
      resetAnimeDraft()
      setMessage('番剧库里暂时没有这部作品，可以直接补充资料。')
    } else {
      setMessage(`找到 ${results.length} 条匹配结果，请选择你要加入的番剧。`)
    }
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '搜索番剧失败。', true)
  } finally {
    searchingAnime.value = false
  }
}

function openAnimeCreateForm() {
  showAnimeCreateForm.value = true
  resetAnimeDraft()
}

function selectAnime(anime: Anime) {
  selectedAnime.value = anime
  editor.animeId = anime.id ?? 0
  selectedAnimeName.value = anime.name
  showAnimeCreateForm.value = false
  editorErrors.value = {
    ...editorErrors.value,
    animeId: undefined,
  }
  setMessage(`已选择《${anime.name}》，继续填写追番状态即可。`)
}

function fillEditor(item?: UserAnime) {
  clearEditorErrors()

  if (!item) {
    selectedUserAnimeId.value = null
    selectedAnime.value = null
    selectedAnimeName.value = ''
    Object.assign(editor, {
      animeId: 0,
      watchStatus: 0,
      currentEpisode: 0,
      score: undefined,
      isFavorite: 0,
      startDate: '',
      finishDate: '',
      remark: '',
    })
    return
  }

  selectedUserAnimeId.value = item.id
  selectedAnime.value = null
  selectedAnimeName.value = item.animeName
  Object.assign(editor, {
    animeId: item.animeId,
    watchStatus: item.watchStatus,
    currentEpisode: item.currentEpisode,
    score: item.score,
    isFavorite: item.isFavorite ?? 0,
    startDate: item.startDate ?? '',
    finishDate: item.finishDate ?? '',
    remark: item.remark ?? '',
  })
}

async function submitAnimeDraft() {
  const payload: AnimePayload = {
    name: animeDraft.name.trim(),
    originalName: animeDraft.originalName?.trim() || undefined,
    coverUrl: animeDraft.coverUrl?.trim() || undefined,
    totalEpisodes: Number(animeDraft.totalEpisodes),
    type: animeDraft.type?.trim() || undefined,
    sourceType: animeDraft.sourceType?.trim() || undefined,
    releaseYear:
      animeDraft.releaseYear === undefined || animeDraft.releaseYear === null || Number.isNaN(Number(animeDraft.releaseYear))
        ? undefined
        : Number(animeDraft.releaseYear),
    season: animeDraft.season?.trim() || undefined,
    synopsis: animeDraft.synopsis?.trim() || undefined,
  }

  const errors = validateAnimeForm(payload)
  animeDraftErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  creatingAnime.value = true

  try {
    await createAnime(payload)
    setMessage('新增成功')
    animeSearchKeyword.value = payload.name
    const results = await searchAnime(payload.name)
    animeSearchResults.value = results
    const matched = results.find((item) => item.name === payload.name) ?? results[0]
    if (matched) {
      selectAnime(matched)
    }
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '新增番剧失败。', true)
  } finally {
    creatingAnime.value = false
  }
}

async function submitEditor() {
  const payload: UserAnimePayload = {
    animeId: editor.animeId,
    watchStatus: editor.watchStatus,
    currentEpisode: Number(editor.currentEpisode),
    score:
      editor.score === undefined || editor.score === null || editor.score === 0
        ? undefined
        : Number(editor.score),
    isFavorite: editor.isFavorite ?? 0,
    startDate: editor.startDate || null,
    finishDate: editor.finishDate || null,
    remark: editor.remark?.trim() || '',
  }

  const shouldFollowExternal = Boolean(
    !selectedUserAnimeId.value &&
      selectedAnime.value?.external &&
      selectedAnime.value.sourceProvider &&
      selectedAnime.value.sourceSubjectId &&
      !selectedAnime.value.id,
  )

  const errors = validateUserAnimeForm({
    ...payload,
    animeId: shouldFollowExternal ? 1 : payload.animeId,
  })
  editorErrors.value = errors

  if (Object.keys(errors).length > 0) {
    setMessage(firstValidationMessage(errors), true)
    return
  }

  savingEditor.value = true

  try {
    if (selectedUserAnimeId.value) {
      await updateUserAnime(selectedUserAnimeId.value, payload)
      setMessage('修改成功')
    } else if (shouldFollowExternal && selectedAnime.value?.sourceProvider && selectedAnime.value.sourceSubjectId) {
      const localAnimeId = await followExternalAnime({
        sourceProvider: selectedAnime.value.sourceProvider,
        sourceSubjectId: selectedAnime.value.sourceSubjectId,
        watchStatus: payload.watchStatus,
        currentEpisode: payload.currentEpisode,
        score: payload.score,
        isFavorite: payload.isFavorite,
        startDate: payload.startDate,
        finishDate: payload.finishDate,
        remark: payload.remark,
      })
      selectedAnime.value.id = localAnimeId
      editor.animeId = localAnimeId
      setMessage('添加成功')
    } else {
      await createUserAnime(payload)
      setMessage('添加成功')
    }

    fillEditor()
    await loadLibrary()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '保存追番记录失败。', true)
  } finally {
    savingEditor.value = false
  }
}

async function quickAdvance(item: UserAnime) {
  if (item.currentEpisode >= item.totalEpisodes && item.totalEpisodes > 0) {
    setMessage(`《${item.animeName}》已经更新到最后一集了。`, true)
    return
  }

  advancingId.value = item.id

  try {
    await updateUserAnimeProgress(item.id, item.currentEpisode + 1)
    setMessage('修改成功')
    await loadLibrary()
  } catch (error) {
    setMessage(error instanceof Error ? error.message : '更新进度失败。', true)
  } finally {
    advancingId.value = null
  }
}

async function removeItem(item: UserAnime) {
  const confirmed = window.confirm(`确定要从追番列表中移除《${item.animeName}》吗？`)
  if (!confirmed) return

  removingId.value = item.id

  try {
    await deleteUserAnime(item.id)
    setMessage('删除成功')
    if (selectedUserAnimeId.value === item.id) {
      fillEditor()
    }
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
  () => animeSearchKeyword.value,
  () => {
    if (!animeSearchKeyword.value.trim()) {
      animeSearchResults.value = []
      showAnimeCreateForm.value = false
    }
  },
)

onMounted(() => {
  fillEditor()
  void loadLibrary()
})
</script>

<template>
  <div class="page-stack">
    <section class="split-hero">
      <SectionTitle
        eyebrow="Library"
        title="我的追番"
        description="筛选、编辑进度、补充番剧资料，把正在看的作品整理成一份清晰的观看目录。"
      />
      <div class="soft-panel soft-panel--stack">
        <strong>在这里可以完成搜索、补录、加入追番和日常维护。</strong>
        <p>如果番剧库里还没有你要找的作品，也可以当场补充基础资料。</p>
      </div>
    </section>

    <section class="content-grid content-grid--library">
      <div class="content-panel">
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
          当前筛选条件下还没有追番记录，可以在右侧先搜索番剧，再把它加入列表。
        </div>

        <div v-else class="anime-list">
          <article v-for="item in library" :key="item.id" class="library-row">
            <AnimeCard :item="item" />
            <div class="library-actions">
              <button class="ghost-button" type="button" :disabled="savingEditor" @click="fillEditor(item)">
                编辑
              </button>
              <button
                class="ghost-button"
                type="button"
                :disabled="advancingId === item.id"
                @click="quickAdvance(item)"
              >
                {{ advancingId === item.id ? '更新中...' : '+1 集' }}
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
      </div>

      <aside class="content-panel">
        <SectionTitle
          eyebrow="Editor"
          :title="selectedUserAnimeId ? '编辑追番记录' : '新增追番记录'"
          description="先选中番剧，再设置状态、进度、评分和备注。"
        />

        <div class="form-stack">
          <label>
            <span>搜索番剧</span>
            <div class="inline-field">
              <input
                v-model.trim="animeSearchKeyword"
                type="search"
                placeholder="输入番剧名称，先搜索现有番剧库"
              />
              <button class="ghost-button" type="button" :disabled="searchingAnime" @click="runAnimeSearch">
                {{ searchingAnime ? '搜索中...' : '搜索' }}
              </button>
            </div>
          </label>

          <div v-if="animeSearchResults.length" class="search-results">
            <button
              v-for="anime in animeSearchResults"
              :key="anime.sourceSubjectId ?? anime.id ?? anime.name"
              class="search-result"
              type="button"
              @click="selectAnime(anime)"
            >
              <strong>{{ anime.name }}</strong>
              <span>{{ anime.type || '未分类' }} · {{ anime.totalEpisodes }} 集 · {{ anime.sourceType || '本地' }}</span>
            </button>
          </div>

          <div v-else-if="hasSearchKeyword && !searchingAnime" class="empty-state search-empty">
            <p>搜索结果里还没有这部番剧。</p>
            <button class="ghost-button" type="button" @click="openAnimeCreateForm">补充番剧资料</button>
          </div>

          <div v-if="showAnimeCreateForm" class="editor-section">
            <div class="editor-divider" />
            <div class="section-title">
              <span class="section-eyebrow">Create Anime</span>
              <h2>补充番剧资料</h2>
              <p>填完基础信息后，就能立刻把它加入你的追番列表。</p>
            </div>

            <div class="form-stack">
              <label>
                <span>番剧名称</span>
                <input v-model.trim="animeDraft.name" type="text" />
                <small v-if="animeDraftErrors.name" class="field-error">{{ animeDraftErrors.name }}</small>
              </label>
              <label>
                <span>原始名称</span>
                <input v-model.trim="animeDraft.originalName" type="text" />
                <small v-if="animeDraftErrors.originalName" class="field-error">{{ animeDraftErrors.originalName }}</small>
              </label>
              <label>
                <span>封面地址</span>
                <input v-model.trim="animeDraft.coverUrl" type="url" placeholder="https://example.com/cover.jpg" />
                <small v-if="animeDraftErrors.coverUrl" class="field-error">{{ animeDraftErrors.coverUrl }}</small>
              </label>
              <label>
                <span>总集数</span>
                <input v-model.number="animeDraft.totalEpisodes" type="number" min="0" />
                <small v-if="animeDraftErrors.totalEpisodes" class="field-error">{{ animeDraftErrors.totalEpisodes }}</small>
              </label>
              <label>
                <span>类型</span>
                <input v-model.trim="animeDraft.type" type="text" placeholder="例如：奇幻 / 校园 / 悬疑" />
                <small v-if="animeDraftErrors.type" class="field-error">{{ animeDraftErrors.type }}</small>
              </label>
              <label>
                <span>来源类型</span>
                <input v-model.trim="animeDraft.sourceType" type="text" placeholder="例如：漫画 / 小说 / 原创" />
                <small v-if="animeDraftErrors.sourceType" class="field-error">{{ animeDraftErrors.sourceType }}</small>
              </label>
              <label>
                <span>上映年份</span>
                <input v-model.number="animeDraft.releaseYear" type="number" min="1900" max="2100" />
                <small v-if="animeDraftErrors.releaseYear" class="field-error">{{ animeDraftErrors.releaseYear }}</small>
              </label>
              <label>
                <span>季度</span>
                <input v-model.trim="animeDraft.season" type="text" placeholder="例如：春 / 夏 / 秋 / 冬" />
                <small v-if="animeDraftErrors.season" class="field-error">{{ animeDraftErrors.season }}</small>
              </label>
              <label>
                <span>简介</span>
                <textarea v-model.trim="animeDraft.synopsis" rows="4" placeholder="用一句话介绍这部作品" />
              </label>

              <div class="editor-actions">
                <button class="primary-button" type="button" :disabled="creatingAnime" @click="submitAnimeDraft">
                  {{ creatingAnime ? '保存中...' : '保存番剧资料' }}
                </button>
                <button class="ghost-button" type="button" :disabled="creatingAnime" @click="showAnimeCreateForm = false">
                  暂不补充
                </button>
              </div>
            </div>
          </div>

          <label>
            <span>已选番剧</span>
            <input :value="selectedAnimeName" type="text" readonly placeholder="先搜索并选择一部番剧" />
            <small v-if="editorErrors.animeId" class="field-error">{{ editorErrors.animeId }}</small>
          </label>

          <label>
            <span>追番状态</span>
            <select v-model.number="editor.watchStatus">
              <option v-for="status in WATCH_STATUS_OPTIONS" :key="status.value" :value="status.value">
                {{ status.label }}
              </option>
            </select>
            <small v-if="editorErrors.watchStatus" class="field-error">{{ editorErrors.watchStatus }}</small>
          </label>

          <label>
            <span>当前看到第几集</span>
            <input v-model.number="editor.currentEpisode" type="number" min="0" />
            <small v-if="editorErrors.currentEpisode" class="field-error">{{ editorErrors.currentEpisode }}</small>
          </label>

          <label>
            <span>评分</span>
            <input v-model.number="editor.score" type="number" min="0" max="10" step="0.5" />
            <small v-if="editorErrors.score" class="field-error">{{ editorErrors.score }}</small>
          </label>

          <label>
            <span>开始日期</span>
            <input v-model="editor.startDate" type="date" />
          </label>

          <label>
            <span>完结日期</span>
            <input v-model="editor.finishDate" type="date" />
            <small v-if="editorErrors.finishDate" class="field-error">{{ editorErrors.finishDate }}</small>
          </label>

          <label class="toggle-line">
            <input v-model.number="editor.isFavorite" :true-value="1" :false-value="0" type="checkbox" />
            <span>加入收藏</span>
          </label>

          <label>
            <span>备注</span>
            <textarea v-model.trim="editor.remark" rows="4" placeholder="例如：准备周末一口气补完" />
          </label>

          <div class="editor-actions">
            <button class="primary-button" type="button" :disabled="savingEditor" @click="submitEditor">
              {{ savingEditor ? '提交中...' : editorMode }}
            </button>
            <button class="ghost-button" type="button" :disabled="savingEditor" @click="fillEditor()">
              清空表单
            </button>
          </div>
        </div>
      </aside>
    </section>
  </div>
</template>
