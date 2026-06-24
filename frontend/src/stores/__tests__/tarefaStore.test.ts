import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock localStorage
const storageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: (k: string) => store[k] ?? null,
    setItem: (k: string, v: string) => { store[k] = v },
    removeItem: (k: string) => { delete store[k] },
    clear: () => { store = {} },
  }
})()
vi.stubGlobal('localStorage', storageMock)

// Mock tarefaApi
vi.mock('../../api/tarefaApi', () => ({
  tarefaApi: {
    listar: vi.fn(),
    criar: vi.fn(),
    concluir: vi.fn(),
    reabrir: vi.fn(),
    deletar: vi.fn(),
    renomear: vi.fn(),
    atualizarDescricao: vi.fn(),
  },
}))

import { useTarefaStore } from '../tarefaStore'
import { tarefaApi } from '../../api/tarefaApi'
import type { Tarefa } from '../../types'

const mockTarefa: Tarefa = {
  id: 'abc-123',
  nome: 'Tarefa X',
  tipo: 'PONTUAL',
  concluida: false,
  concluidaEm: null,
  data: '2026-06-24',
  diasSemana: [],
  horario: null,
  criadoEm: '2026-06-24T10:00:00',
}

describe('tarefaStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('carregar popula tarefas[workspaceId]', async () => {
    vi.mocked(tarefaApi.listar).mockResolvedValue([mockTarefa])

    const store = useTarefaStore()
    await store.carregar('ws-1', '2026-06-24')

    expect(tarefaApi.listar).toHaveBeenCalledWith('ws-1', '2026-06-24')
    expect(store.tarefas['ws-1']).toHaveLength(1)
    expect(store.tarefas['ws-1'][0].nome).toBe('Tarefa X')
  })

  it('concluir chama api.concluir e atualiza state', async () => {
    const concluida: Tarefa = { ...mockTarefa, concluida: true }
    vi.mocked(tarefaApi.concluir).mockResolvedValue(concluida)

    const store = useTarefaStore()
    store.tarefas['ws-1'] = [mockTarefa]
    await store.concluir('ws-1', 'abc-123', '2026-06-24')

    expect(tarefaApi.concluir).toHaveBeenCalledWith('abc-123', '2026-06-24')
    expect(store.tarefas['ws-1'][0].concluida).toBe(true)
  })

  it('reabrir chama api.reabrir e atualiza state', async () => {
    const tarefaConcluida: Tarefa = { ...mockTarefa, concluida: true }
    const reaberta: Tarefa = { ...mockTarefa, concluida: false }
    vi.mocked(tarefaApi.reabrir).mockResolvedValue(reaberta)

    const store = useTarefaStore()
    store.tarefas['ws-1'] = [tarefaConcluida]
    await store.reabrir('ws-1', 'abc-123', '2026-06-24')

    expect(tarefaApi.reabrir).toHaveBeenCalledWith('abc-123', '2026-06-24')
    expect(store.tarefas['ws-1'][0].concluida).toBe(false)
  })

  it('criar adiciona nova tarefa ao state', async () => {
    const nova: Tarefa = { ...mockTarefa, id: 'nova-456', nome: 'Nova' }
    vi.mocked(tarefaApi.criar).mockResolvedValue(nova)

    const store = useTarefaStore()
    store.tarefas['ws-1'] = []
    await store.criar('ws-1', { nome: 'Nova', tipo: 'PONTUAL' }, '2026-06-24')

    expect(store.tarefas['ws-1']).toHaveLength(1)
    expect(store.tarefas['ws-1'][0].id).toBe('nova-456')
  })
})
