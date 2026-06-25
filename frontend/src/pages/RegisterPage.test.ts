import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHashHistory } from 'vue-router'
import RegisterPage from './RegisterPage.vue'

const mockRegister = vi.fn()
vi.mock('../stores/authStore', () => ({
  useAuthStore: () => ({ register: mockRegister, isAuthenticated: false }),
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/register', component: RegisterPage },
    { path: '/workspaces', component: { template: '<div/>' } },
    { path: '/login', component: { template: '<div/>' } },
  ],
})

function mountPage() {
  return mount(RegisterPage, {
    global: { plugins: [createPinia(), router] },
  })
}

describe('RegisterPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockRegister.mockReset()
  })

  it('exibe erro se senha muito curta', async () => {
    const w = mountPage()
    await w.find('input[type="email"]').setValue('a@test.com')
    const inputs = w.findAll('input[type="password"]')
    await inputs[0].setValue('123')
    await inputs[1].setValue('123')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.text()).toContain('pelo menos 8')
  })

  it('exibe erro se senhas diferentes', async () => {
    const w = mountPage()
    await w.find('input[type="email"]').setValue('a@test.com')
    const inputs = w.findAll('input[type="password"]')
    await inputs[0].setValue('senha1234')
    await inputs[1].setValue('outrasenha')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.text()).toContain('não coincidem')
  })

  it('chama register com dados válidos', async () => {
    mockRegister.mockResolvedValueOnce(undefined)
    const w = mountPage()
    await w.find('input[type="email"]').setValue('b@test.com')
    const inputs = w.findAll('input[type="password"]')
    await inputs[0].setValue('senha1234')
    await inputs[1].setValue('senha1234')
    await w.find('input[type="text"]').setValue('FEITU-TEST')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(mockRegister).toHaveBeenCalledWith('b@test.com', 'senha1234', 'FEITU-TEST')
  })

  it('exibe erro de API em caso de falha no register', async () => {
    mockRegister.mockRejectedValueOnce({
      response: { data: { detail: 'Código de convite inválido' } },
    })
    const w = mountPage()
    await w.find('input[type="email"]').setValue('c@test.com')
    const inputs = w.findAll('input[type="password"]')
    await inputs[0].setValue('senha1234')
    await inputs[1].setValue('senha1234')
    await w.find('input[type="text"]').setValue('FEITU-INVALIDO')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.text()).toContain('Código de convite inválido')
  })
})
