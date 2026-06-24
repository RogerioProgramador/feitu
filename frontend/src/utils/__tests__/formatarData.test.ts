import { describe, it, expect, beforeEach, vi } from 'vitest'

// Mock localStorage antes de importar
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

import { formatarDataBR, hojeISO } from '../formatarData'

describe('formatarDataBR', () => {
  it('converte ISO para dd/mm/yyyy', () => {
    expect(formatarDataBR('2026-06-24')).toBe('24/06/2026')
  })

  it('preserva zeros à esquerda', () => {
    expect(formatarDataBR('2026-01-05')).toBe('05/01/2026')
  })
})

describe('hojeISO', () => {
  beforeEach(() => {
    storageMock.removeItem('feitu_fuso')
  })

  it('retorna data no formato YYYY-MM-DD', () => {
    const result = hojeISO()
    expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  })

  it('retorna data de hoje (mesmo ano)', () => {
    const now = new Date()
    const ano = now.getFullYear().toString()
    expect(hojeISO()).toContain(ano)
  })
})
