import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import http from '../api/http'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('feitu_token'))
  const email = ref<string | null>(localStorage.getItem('feitu_email'))

  const isAuthenticated = computed(() => !!token.value)

  async function login(emailInput: string, senha: string) {
    const res = await http.post<{ token: string }>('/auth/login', { email: emailInput, senha })
    token.value = res.data.token
    email.value = emailInput
    localStorage.setItem('feitu_token', res.data.token)
    localStorage.setItem('feitu_email', emailInput)
  }

  async function register(emailInput: string, senha: string, codigoConvite: string) {
    const res = await http.post<{ token: string }>('/auth/register', { email: emailInput, senha, codigoConvite })
    token.value = res.data.token
    email.value = emailInput
    localStorage.setItem('feitu_token', res.data.token)
    localStorage.setItem('feitu_email', emailInput)
  }

  function logout() {
    token.value = null
    email.value = null
    localStorage.removeItem('feitu_token')
    localStorage.removeItem('feitu_email')
  }

  return { token, email, isAuthenticated, login, register, logout }
})
