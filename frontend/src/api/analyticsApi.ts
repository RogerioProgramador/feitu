import http from './http'
import type { DailySummary } from '../types'

export const analyticsApi = {
  diario: (date?: string) =>
    http
      .get<DailySummary>('/analytics/daily', { params: date ? { date } : {} })
      .then((r) => r.data),
}
