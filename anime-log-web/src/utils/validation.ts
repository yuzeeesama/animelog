import type {
  AnimePayload,
  EpisodeLogPayload,
  UserAnimePayload,
  UserLoginPayload,
  UserPasswordPayload,
  UserProfilePayload,
  UserRegisterPayload,
} from '@/types/models'

export type ValidationErrors<T extends string = string> = Partial<Record<T, string>>

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function isBlank(value?: string | null) {
  return !value || !value.trim()
}

function hasLengthOver(value: string | undefined, max: number) {
  return Boolean(value && value.trim().length > max)
}

function isValidUrl(value: string) {
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

function isValidScore(value?: number) {
  return value === undefined || value === null || (value >= 0 && value <= 10)
}

export function validateLoginForm(form: UserLoginPayload) {
  const errors: ValidationErrors<'username' | 'password'> = {}

  if (isBlank(form.username)) errors.username = '用户名不能为空'
  if (isBlank(form.password)) errors.password = '密码不能为空'

  return errors
}

export function validateRegisterForm(
  form: UserRegisterPayload & { confirmPassword: string },
) {
  const errors: ValidationErrors<'username' | 'nickname' | 'email' | 'password' | 'confirmPassword'> = {}

  if (isBlank(form.username)) errors.username = '用户名不能为空'
  else if (hasLengthOver(form.username, 50)) errors.username = '用户名长度不能超过50'

  if (isBlank(form.nickname)) errors.nickname = '昵称不能为空'
  else if (hasLengthOver(form.nickname, 50)) errors.nickname = '昵称长度不能超过50'

  if (!isBlank(form.email)) {
    const email = form.email ?? ''
    if (hasLengthOver(email, 100)) errors.email = '邮箱长度不能超过100'
    else if (!EMAIL_RE.test(email.trim())) errors.email = '邮箱格式不正确'
  }

  if (isBlank(form.password)) errors.password = '密码不能为空'
  else if (form.password.length < 6 || form.password.length > 20) {
    errors.password = '密码长度需在6-20位之间'
  }

  if (isBlank(form.confirmPassword)) errors.confirmPassword = '请再次输入密码'
  else if (form.password !== form.confirmPassword) errors.confirmPassword = '两次输入的密码不一致'

  return errors
}

export function validateProfileForm(form: UserProfilePayload) {
  const errors: ValidationErrors<'nickname' | 'avatarUrl' | 'email' | 'bio'> = {}

  if (isBlank(form.nickname)) errors.nickname = '昵称不能为空'
  else if (hasLengthOver(form.nickname, 50)) errors.nickname = '昵称长度不能超过50'

  if (!isBlank(form.avatarUrl)) {
    const avatarUrl = form.avatarUrl ?? ''
    if (hasLengthOver(avatarUrl, 255)) errors.avatarUrl = '头像地址长度不能超过255'
    else if (!isValidUrl(avatarUrl.trim())) errors.avatarUrl = '头像地址格式不正确'
  }

  if (!isBlank(form.email)) {
    const email = form.email ?? ''
    if (hasLengthOver(email, 100)) errors.email = '邮箱长度不能超过100'
    else if (!EMAIL_RE.test(email.trim())) errors.email = '邮箱格式不正确'
  }

  if (hasLengthOver(form.bio, 255)) errors.bio = '个人简介长度不能超过255'

  return errors
}

export function validatePasswordForm(form: UserPasswordPayload & { confirmPassword: string }) {
  const errors: ValidationErrors<'oldPassword' | 'newPassword' | 'confirmPassword'> = {}

  if (isBlank(form.oldPassword)) errors.oldPassword = '旧密码不能为空'

  if (isBlank(form.newPassword)) errors.newPassword = '新密码不能为空'
  else if (form.newPassword.length < 6 || form.newPassword.length > 20) {
    errors.newPassword = '新密码长度需在6-20位之间'
  }

  if (isBlank(form.confirmPassword)) errors.confirmPassword = '请再次输入新密码'
  else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的新密码不一致'
  }

  return errors
}

export function validateAnimeForm(form: AnimePayload) {
  const errors: ValidationErrors<
    'name' | 'originalName' | 'coverUrl' | 'totalEpisodes' | 'type' | 'sourceType' | 'releaseYear' | 'season'
  > = {}

  if (isBlank(form.name)) errors.name = '番剧名称不能为空'
  else if (hasLengthOver(form.name, 100)) errors.name = '番剧名称长度不能超过100'

  if (hasLengthOver(form.originalName, 150)) errors.originalName = '原始名称长度不能超过150'

  if (!isBlank(form.coverUrl)) {
    const coverUrl = form.coverUrl ?? ''
    if (hasLengthOver(coverUrl, 255)) errors.coverUrl = '封面地址长度不能超过255'
    else if (!isValidUrl(coverUrl.trim())) errors.coverUrl = '封面地址格式不正确'
  }

  if (form.totalEpisodes === undefined || form.totalEpisodes === null || Number.isNaN(form.totalEpisodes)) {
    errors.totalEpisodes = '总集数不能为空'
  } else if (form.totalEpisodes < 0) {
    errors.totalEpisodes = '总集数不能小于0'
  }

  if (hasLengthOver(form.type, 50)) errors.type = '类型长度不能超过50'
  if (hasLengthOver(form.sourceType, 50)) errors.sourceType = '来源类型长度不能超过50'

  if (form.releaseYear !== undefined && form.releaseYear !== null) {
    if (Number.isNaN(form.releaseYear) || form.releaseYear < 1900 || form.releaseYear > 2100) {
      errors.releaseYear = '上映年份不正确'
    }
  }

  if (hasLengthOver(form.season, 20)) errors.season = '季度长度不能超过20'

  return errors
}

export function validateUserAnimeForm(form: UserAnimePayload) {
  const errors: ValidationErrors<
    'animeId' | 'watchStatus' | 'currentEpisode' | 'score' | 'isFavorite' | 'startDate' | 'finishDate' | 'remark'
  > = {}

  if (!form.animeId) errors.animeId = '请先选择一部番剧'

  if (form.watchStatus < 0 || form.watchStatus > 4) errors.watchStatus = '追番状态不正确'

  if (form.currentEpisode === undefined || Number.isNaN(form.currentEpisode)) {
    errors.currentEpisode = '当前集数不能为空'
  } else if (form.currentEpisode < 0) {
    errors.currentEpisode = '当前集数不能小于0'
  }

  if (!isValidScore(form.score)) errors.score = '评分不能小于0且不能大于10'

  if (
    form.isFavorite !== undefined &&
    form.isFavorite !== null &&
    form.isFavorite !== 0 &&
    form.isFavorite !== 1
  ) {
    errors.isFavorite = '收藏状态不正确'
  }

  if (form.startDate && form.finishDate && form.finishDate < form.startDate) {
    errors.finishDate = '完结日期不能早于开始日期'
  }

  return errors
}

export function validateEpisodeLogForm(form: EpisodeLogPayload) {
  const errors: ValidationErrors<
    'episodeNo' | 'episodeTitle' | 'content' | 'moodTag' | 'score' | 'isHighlight'
  > = {}

  if (form.episodeNo === undefined || form.episodeNo === null || Number.isNaN(form.episodeNo)) {
    errors.episodeNo = '集数不能为空'
  } else if (form.episodeNo < 1) {
    errors.episodeNo = '集数必须大于0'
  }

  if (hasLengthOver(form.episodeTitle, 100)) errors.episodeTitle = '标题长度不能超过100'
  if (isBlank(form.content)) errors.content = '日志内容不能为空'
  if (hasLengthOver(form.moodTag, 50)) errors.moodTag = '心情标签长度不能超过50'
  if (!isValidScore(form.score)) errors.score = '评分不能小于0且不能大于10'

  if (
    form.isHighlight !== undefined &&
    form.isHighlight !== null &&
    form.isHighlight !== 0 &&
    form.isHighlight !== 1
  ) {
    errors.isHighlight = '神回状态不正确'
  }

  return errors
}

export function firstValidationMessage(errors: ValidationErrors) {
  return Object.values(errors).find(Boolean) ?? ''
}
