import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('../../api/analyticsApi', () => ({
  analyticsApi: { diario: vi.fn() },
}))

import { useAnalyticsStore } from '../analyticsStore'
import { analyticsApi } from '../../api/analyticsApi'
import type { DailySummary } from '../../types'

const mockResumo: DailySummary = {
  data: '2026-06-24',
  totalTarefas: 3,
  concluidas: 1,
  recorrentes: [{ id: 'r1', nome: 'Rec', concluida: true, tipo: 'RECORRENTE' }],
  pontuais: [],
}

describe('analyticsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('carregar popula resumo com sucesso', async () => {
    vi.mocked(analyticsApi.diario).mockResolvedValue(mockResumo)

    const store = useAnalyticsStore()
    await store.carregar('2026-06-24')

    expect(analyticsApi.diario).toHaveBeenCalledWith('2026-06-24')
    expect(store.resumo).toEqual(mockResumo)
    expect(store.carregando).toBe(false)
    expect(store.erro).toBe('')
  })

  it('carregar define erro em caso de falha', async () => {
    vi.mocked(analyticsApi.diario).mockRejectedValue(new Error('network'))

    const store = useAnalyticsStore()
    await store.carregar('2026-06-24')

    expect(store.resumo).toBeNull()
    expect(store.erro).toBe('Erro ao carregar resumo')
    expect(store.carregando).toBe(false)
  })

  it('carregando é true durante a requisição', async () => {
    let resolve!: (v: DailySummary) => void
    vi.mocked(analyticsApi.diario).mockReturnValue(new Promise(r => { resolve = r }))

    const store = useAnalyticsStore()
    const promise = store.carregar('2026-06-24')
    expect(store.carregando).toBe(true)

    resolve(mockResumo)
    await promise
    expect(store.carregando).toBe(false)
  })

  it('carregar limpa erro anterior antes de nova chamada', async () => {
    vi.mocked(analyticsApi.diario)
      .mockRejectedValueOnce(new Error('falha'))
      .mockResolvedValueOnce(mockResumo)

    const store = useAnalyticsStore()
    await store.carregar('2026-06-24')
    expect(store.erro).toBeTruthy()

    await store.carregar('2026-06-24')
    expect(store.erro).toBe('')
    expect(store.resumo).toEqual(mockResumo)
  })
})
