import { describe, it, expect } from 'vitest'
import { getApiErrorMessage } from '../apiError'

describe('getApiErrorMessage', () => {
  it('retorna detail da resposta quando presente', () => {
    const err = { response: { data: { detail: 'E-mail já cadastrado' } } }
    expect(getApiErrorMessage(err, 'Fallback')).toBe('E-mail já cadastrado')
  })

  it('retorna fallback quando não há response', () => {
    expect(getApiErrorMessage(new Error('network'), 'Fallback')).toBe('Fallback')
  })

  it('retorna fallback quando response não tem detail', () => {
    const err = { response: { data: {} } }
    expect(getApiErrorMessage(err, 'Fallback')).toBe('Fallback')
  })

  it('retorna fallback para null', () => {
    expect(getApiErrorMessage(null, 'Fallback')).toBe('Fallback')
  })

  it('retorna fallback para undefined', () => {
    expect(getApiErrorMessage(undefined, 'Fallback')).toBe('Fallback')
  })
})
