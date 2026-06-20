<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const senha = ref('')
const confirmar = ref('')
const erro = ref('')
const carregando = ref(false)

async function criar() {
  erro.value = ''
  if (senha.value.length < 8) {
    erro.value = 'A senha deve ter pelo menos 8 caracteres'
    return
  }
  if (senha.value !== confirmar.value) {
    erro.value = 'As senhas não coincidem'
    return
  }
  carregando.value = true
  try {
    await auth.register(email.value, senha.value)
    router.push('/workspaces')
  } catch (e: any) {
    erro.value = e.response?.data?.detail ?? 'Erro ao criar conta'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-feitu-bg px-4">
    <div class="w-full max-w-sm bg-white rounded-2xl shadow-sm p-8 space-y-6">
      <h1 class="text-2xl font-semibold text-feitu-text text-center">Criar conta</h1>

      <form @submit.prevent="criar" class="space-y-4">
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
            minlength="8"
            class="w-full border border-feitu-blue rounded-xl px-3 py-2 outline-none focus:ring-2 focus:ring-feitu-blue text-sm"
          />
        </div>

        <div>
          <label class="block text-sm text-feitu-text mb-1">Confirmar senha</label>
          <input
            v-model="confirmar"
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
          {{ carregando ? 'Criando...' : 'Criar conta' }}
        </button>
      </form>

      <p class="text-sm text-center text-feitu-text">
        Já tem conta?
        <RouterLink to="/login" class="underline">Entrar</RouterLink>
      </p>
    </div>
  </div>
</template>
