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

// Mock matchMedia
const mockMatchMedia = vi.fn().mockReturnValue({ matches: false, addEventListener: vi.fn() })
Object.defineProperty(window, 'matchMedia', { writable: true, value: mockMatchMedia })

import { useSettingsStore } from '../settingsStore'

describe('settingsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    storageMock.clear()
    document.documentElement.classList.remove('dark')
    mockMatchMedia.mockReturnValue({ matches: false, addEventListener: vi.fn() })
  })

  it('aplicarTema("escuro") adiciona classe dark', () => {
    const store = useSettingsStore()
    store.aplicarTema('escuro')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('aplicarTema("claro") remove classe dark', () => {
    document.documentElement.classList.add('dark')
    const store = useSettingsStore()
    store.aplicarTema('claro')
    expect(document.documentElement.classList.contains('dark')).toBe(false)
  })

  it('aplicarTema("sistema") usa prefers-color-scheme dark', () => {
    mockMatchMedia.mockReturnValue({ matches: true, addEventListener: vi.fn() })
    const store = useSettingsStore()
    store.aplicarTema('sistema')
    expect(document.documentElement.classList.contains('dark')).toBe(true)
  })

  it('persiste tema no localStorage ao trocar', async () => {
    const store = useSettingsStore()
    store.tema = 'escuro'
    await Promise.resolve()
    expect(storageMock.getItem('feitu_tema')).toBe('escuro')
  })
})
