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

vi.mock('../../api/http', () => ({
  default: { post: vi.fn() },
}))

vi.mock('../../api/usuarioApi', () => ({
  usuarioApi: { me: vi.fn() },
}))

import { useAuthStore } from '../authStore'
import http from '../../api/http'
import { usuarioApi } from '../../api/usuarioApi'

describe('authStore', () => {
  beforeEach(() => {
    storageMock.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('login armazena token, email e marca isAuthenticated', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { token: 'tok-123' } })

    const store = useAuthStore()
    await store.login('user@test.com', 'senha123')

    expect(store.token).toBe('tok-123')
    expect(store.email).toBe('user@test.com')
    expect(store.isAuthenticated).toBe(true)
    expect(storageMock.getItem('feitu_token')).toBe('tok-123')
  })

  it('logout limpa token, email e horarioNotificacao', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { token: 'tok-abc' } })

    const store = useAuthStore()
    await store.login('user@test.com', 'senha123')
    store.logout()

    expect(store.token).toBeNull()
    expect(store.email).toBeNull()
    expect(store.horarioNotificacao).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(storageMock.getItem('feitu_token')).toBeNull()
  })

  it('carregarPerfil preenche horarioNotificacao truncando segundos', async () => {
    vi.mocked(usuarioApi.me).mockResolvedValue({
      id: 'uid-1',
      email: 'user@test.com',
      horarioNotificacao: '08:30:00',
    })

    const store = useAuthStore()
    await store.carregarPerfil()

    expect(store.horarioNotificacao).toBe('08:30')
  })

  it('carregarPerfil não quebra quando API falha', async () => {
    vi.mocked(usuarioApi.me).mockRejectedValue(new Error('unauthorized'))

    const store = useAuthStore()
    await expect(store.carregarPerfil()).resolves.toBeUndefined()
    expect(store.horarioNotificacao).toBeNull()
  })

  it('register armazena token e email', async () => {
    vi.mocked(http.post).mockResolvedValue({ data: { token: 'tok-reg' } })

    const store = useAuthStore()
    await store.register('novo@test.com', 'senha123', 'FEITU-XYZ')

    expect(store.token).toBe('tok-reg')
    expect(store.email).toBe('novo@test.com')
    expect(store.isAuthenticated).toBe(true)
    expect(storageMock.getItem('feitu_token')).toBe('tok-reg')
    expect(http.post).toHaveBeenCalledWith('/auth/register', {
      email: 'novo@test.com',
      senha: 'senha123',
      codigoConvite: 'FEITU-XYZ',
    })
  })

  it('carregarPerfil mantém horarioNotificacao null quando perfil não tem horário', async () => {
    vi.mocked(usuarioApi.me).mockResolvedValue({
      id: 'uid-1',
      email: 'user@test.com',
      horarioNotificacao: null,
    })

    const store = useAuthStore()
    await store.carregarPerfil()

    expect(store.horarioNotificacao).toBeNull()
  })
})
