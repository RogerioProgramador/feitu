import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '../api/http'
import { usuarioApi } from '../api/usuarioApi'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('feitu_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('feitu_refresh_token'))
  const email = ref<string | null>(localStorage.getItem('feitu_email'))
  const horarioNotificacao = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)

  async function login(emailInput: string, senha: string) {
    const res = await http.post<{ token: string; refreshToken: string }>('/auth/login', { email: emailInput, senha })
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    email.value = emailInput
    localStorage.setItem('feitu_token', res.data.token)
    localStorage.setItem('feitu_refresh_token', res.data.refreshToken)
    localStorage.setItem('feitu_email', emailInput)
  }

  async function register(emailInput: string, senha: string, codigoConvite: string) {
    const res = await http.post<{ token: string; refreshToken: string }>('/auth/register', { email: emailInput, senha, codigoConvite })
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    email.value = emailInput
    localStorage.setItem('feitu_token', res.data.token)
    localStorage.setItem('feitu_refresh_token', res.data.refreshToken)
    localStorage.setItem('feitu_email', emailInput)
  }

  async function logout() {
    const rt = localStorage.getItem('feitu_refresh_token')
    // Limpar estado local imediatamente para que a UI reaja
    token.value = null
    refreshToken.value = null
    email.value = null
    horarioNotificacao.value = null
    localStorage.removeItem('feitu_token')
    localStorage.removeItem('feitu_refresh_token')
    localStorage.removeItem('feitu_email')
    // Revogar no servidor (melhor esforço — não bloqueia o logout local)
    if (rt) {
      try {
        await http.post('/auth/logout', { refreshToken: rt })
      } catch { /* sem ação — sessão local já foi limpa */ }
    }
  }

  async function carregarPerfil() {
    try {
      const perfil = await usuarioApi.me()
      if (perfil.horarioNotificacao) {
        horarioNotificacao.value = perfil.horarioNotificacao.slice(0, 5)
      }
    } catch {
      // sem notificação configurada
    }
  }

  return { token, refreshToken, email, horarioNotificacao, isAuthenticated, login, register, logout, carregarPerfil }
})
