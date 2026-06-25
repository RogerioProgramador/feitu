import { defineStore } from 'pinia'
import { ref } from 'vue'
import { analyticsApi } from '../api/analyticsApi'
import type { DailySummary } from '../types'

export const useAnalyticsStore = defineStore('analytics', () => {
  const resumo = ref<DailySummary | null>(null)
  const carregando = ref(false)
  const erro = ref('')

  async function carregar(date: string) {
    carregando.value = true
    erro.value = ''
    try {
      resumo.value = await analyticsApi.diario(date)
    } catch {
      erro.value = 'Erro ao carregar resumo'
    } finally {
      carregando.value = false
    }
  }

  return { resumo, carregando, erro, carregar }
})
