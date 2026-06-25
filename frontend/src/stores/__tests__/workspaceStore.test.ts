import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

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

vi.mock('../../api/workspaceApi', () => ({
  workspaceApi: {
    listar: vi.fn(),
    criar: vi.fn(),
    atualizar: vi.fn(),
    reordenar: vi.fn(),
    deletar: vi.fn(),
  },
}))

import { useWorkspaceStore } from '../workspaceStore'
import { workspaceApi } from '../../api/workspaceApi'
import type { Workspace } from '../../types'

const ws1: Workspace = { id: 'ws-1', nome: 'Work', cor: '#A7C7E7', ordem: 1 }
const ws2: Workspace = { id: 'ws-2', nome: 'Personal', cor: '#B5EAD7', ordem: 2 }

describe('workspaceStore', () => {
  beforeEach(() => {
    storageMock.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // --- carregar ---

  it('carregar popula lista e seleciona o primeiro', async () => {
    vi.mocked(workspaceApi.listar).mockResolvedValue([ws1, ws2])

    const store = useWorkspaceStore()
    await store.carregar()

    expect(store.workspaces).toHaveLength(2)
    expect(store.ativoId).toBe('ws-1')
  })

  it('carregar não sobrescreve ativoId se já definido', async () => {
    vi.mocked(workspaceApi.listar).mockResolvedValue([ws1, ws2])

    const store = useWorkspaceStore()
    store.selecionar('ws-2')
    await store.carregar()

    expect(store.ativoId).toBe('ws-2')
  })

  // --- criar ---

  it('criar adiciona workspace e o seleciona como ativo', async () => {
    const novo: Workspace = { id: 'ws-novo', nome: 'Novo', cor: '#A7C7E7', ordem: 1 }
    vi.mocked(workspaceApi.criar).mockResolvedValue(novo)

    const store = useWorkspaceStore()
    await store.criar('Novo', '#A7C7E7')

    expect(store.workspaces).toHaveLength(1)
    expect(store.ativoId).toBe('ws-novo')
  })

  // --- deletar ---

  it('deletar remove workspace da lista', async () => {
    vi.mocked(workspaceApi.deletar).mockResolvedValue(undefined as never)

    const store = useWorkspaceStore()
    store.workspaces = [ws1, ws2]
    store.selecionar('ws-2')
    await store.deletar('ws-2')

    expect(store.workspaces).toHaveLength(1)
    expect(store.workspaces[0].id).toBe('ws-1')
  })

  it('deletar workspace ativo faz fallback para o primeiro restante', async () => {
    vi.mocked(workspaceApi.deletar).mockResolvedValue(undefined as never)

    const store = useWorkspaceStore()
    store.workspaces = [ws1, ws2]
    store.selecionar('ws-1')
    await store.deletar('ws-1')

    expect(store.ativoId).toBe('ws-2')
  })

  it('deletar último workspace define ativoId como null', async () => {
    vi.mocked(workspaceApi.deletar).mockResolvedValue(undefined as never)

    const store = useWorkspaceStore()
    store.workspaces = [ws1]
    store.selecionar('ws-1')
    await store.deletar('ws-1')

    expect(store.workspaces).toHaveLength(0)
    expect(store.ativoId).toBeNull()
  })

  // --- reordenar ---

  it('reordenar reorganiza workspaces na nova ordem', async () => {
    const ws3: Workspace = { id: 'ws-3', nome: 'Side', cor: '#FFDAC1', ordem: 3 }
    vi.mocked(workspaceApi.reordenar).mockResolvedValue(undefined as never)

    const store = useWorkspaceStore()
    store.workspaces = [ws1, ws2, ws3]
    await store.reordenar(['ws-3', 'ws-1', 'ws-2'])

    expect(store.workspaces[0].id).toBe('ws-3')
    expect(store.workspaces[1].id).toBe('ws-1')
    expect(store.workspaces[2].id).toBe('ws-2')
  })

  // --- selecionar ---

  it('selecionar atualiza ativoId', () => {
    const store = useWorkspaceStore()
    store.selecionar('ws-2')
    expect(store.ativoId).toBe('ws-2')
  })
})
