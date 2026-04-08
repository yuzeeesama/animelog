import type { WatchStatus } from '@/types/models'

export const WATCH_STATUS_OPTIONS: Array<{ label: string; value: WatchStatus }> = [
  { label: '想看', value: 0 },
  { label: '在看', value: 1 },
  { label: '看完', value: 2 },
  { label: '搁置', value: 3 },
  { label: '弃番', value: 4 },
]

export const WATCH_STATUS_META: Record<
  WatchStatus,
  { label: string; shortLabel: string; tone: string }
> = {
  0: { label: '想看', shortLabel: '想看', tone: 'dream' },
  1: { label: '在看', shortLabel: '在看', tone: 'active' },
  2: { label: '看完', shortLabel: '看完', tone: 'done' },
  3: { label: '搁置', shortLabel: '搁置', tone: 'muted' },
  4: { label: '弃番', shortLabel: '弃番', tone: 'drop' },
}
