export function formatDateTime(value?: string | null) {
  if (!value) return '未记录'

  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

export function formatDate(value?: string | null) {
  if (!value) return '未设置'
  return value.replace('T', ' ').slice(0, 10)
}

export function clampProgress(current: number, total: number) {
  if (!total) return 0
  return Math.max(0, Math.min(100, Math.round((current / total) * 100)))
}

export function avatarFallback(name?: string) {
  if (!name) return '追'
  return name.trim().charAt(0).toUpperCase()
}
