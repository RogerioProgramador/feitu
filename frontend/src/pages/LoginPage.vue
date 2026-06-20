<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const senha = ref('')
const erro = ref('')
const carregando = ref(false)

async function entrar() {
  erro.value = ''
  carregando.value = true
  try {
    await auth.login(email.value, senha.value)
    router.push('/workspaces')
  } catch (e: any) {
    erro.value = e.response?.data?.detail ?? 'Credenciais inválidas'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-feitu-bg px-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-sm p-8 space-y-6">
      <h1 class="text-2xl font-semibold text-feitu-text text-center">Feitu</h1>

      <form @submit.prevent="entrar" class="space-y-4">
        <div>
          <label class="block text-sm text-feitu-text mb-1">E-mail</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full border border-feitu-blue rounded-xl px-3 py-2 outline-none focus:ring-2 focus:ring-feitu-blue text-sm"
          />
        </div>

        <div>
          <label class="block text-sm text-feitu-text mb-1">Senha</label>
          <input
            v-model="senha"
            type="password"
            required
            class="w-full border border-feitu-blue rounded-xl px-3 py-2 outline-none focus:ring-2 focus:ring-feitu-blue text-sm"
          />
        </div>

        <p v-if="erro" class="text-red-500 text-sm">{{ erro }}</p>

        <button
          type="submit"
          :disabled="carregando"
          class="w-full bg-feitu-blue text-feitu-text font-medium py-2 rounded-xl hover:opacity-90 transition disabled:opacity-50"
        >
          {{ carregando ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <p class="text-sm text-center text-feitu-text">
        Sem conta?
        <RouterLink to="/register" class="underline">Criar conta</RouterLink>
      </p>
    </div>
  </div>
</template>
