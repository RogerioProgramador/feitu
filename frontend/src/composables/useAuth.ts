import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

export function useAuth() {
  const auth = useAuthStore()
  const router = useRouter()

  const isAuthenticated = computed(() => auth.isAuthenticated)

  function logout() {
    auth.logout()
    router.push('/login')
  }

  return { isAuthenticated, login: auth.login, register: auth.register, logout }
}
