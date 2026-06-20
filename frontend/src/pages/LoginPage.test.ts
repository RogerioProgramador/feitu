import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHashHistory } from 'vue-router'
import LoginPage from './LoginPage.vue'

const mockLogin = vi.fn()
vi.mock('../stores/authStore', () => ({
  useAuthStore: () => ({ login: mockLogin, isAuthenticated: false }),
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: { template: '<div/>' } },
    { path: '/login', component: LoginPage },
    { path: '/workspaces', component: { template: '<div/>' } },
    { path: '/register', component: { template: '<div/>' } },
  ],
})

function mountPage() {
  return mount(LoginPage, {
    global: { plugins: [createPinia(), router] },
  })
}

describe('LoginPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockLogin.mockReset()
  })

  it('renderiza campos de email e senha', () => {
    const w = mountPage()
    expect(w.find('input[type="email"]').exists()).toBe(true)
    expect(w.find('input[type="password"]').exists()).toBe(true)
  })

  it('exibe erro em caso de falha no login', async () => {
    mockLogin.mockRejectedValueOnce({
      response: { data: { detail: 'Credenciais inválidas' } },
    })
    const w = mountPage()
    await w.find('input[type="email"]').setValue('a@test.com')
    await w.find('input[type="password"]').setValue('senha1234')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(w.text()).toContain('Credenciais inválidas')
  })

  it('chama login com email e senha', async () => {
    mockLogin.mockResolvedValueOnce(undefined)
    const w = mountPage()
    await w.find('input[type="email"]').setValue('b@test.com')
    await w.find('input[type="password"]').setValue('senha1234')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect(mockLogin).toHaveBeenCalledWith('b@test.com', 'senha1234')
  })
})
