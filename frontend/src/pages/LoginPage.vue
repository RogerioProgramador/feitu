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
    router.replace('/workspaces')
  } catch (e: any) {
    erro.value = e.response?.data?.detail ?? 'Credenciais inválidas'
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-feitu-bg dark:bg-night-bg px-4">
    <div class="w-full max-w-sm space-y-8">

      <!-- Logo + wordmark -->
      <div class="flex flex-col items-center gap-3">
        <div class="w-[52px] h-[52px] rounded-[17px] bg-[#EAF1F8] flex items-center justify-center">
          <svg width="40" height="40" viewBox="0 0 56 56" fill="none">
            <circle cx="28" cy="28" r="18" stroke="#5E8BB6" stroke-width="4" stroke-linecap="round"
              stroke-dasharray="87 26" fill="none" transform="rotate(-46 28 28)"/>
            <path d="M19 28l6 6 12-12" stroke="#E07B4F" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="font-display italic text-[32px] text-feitu-text dark:text-night-text leading-none">Feitu</h1>
        <p class="text-[13.5px] text-[#9A9388]">Checklist diário de tarefas</p>
      </div>

      <!-- Card -->
      <div class="bg-white dark:bg-night-surface rounded-[22px] p-7 shadow-[0_2px_16px_rgba(54,51,46,.07)] dark:shadow-none">
        <form @submit.prevent="entrar" class="space-y-[14px]">
          <div>
            <label class="block text-[12.5px] font-semibold text-[#8C857B] mb-[6px] uppercase tracking-[.04em]">E-mail</label>
            <input
              v-model="email"
              type="email"
              required
              placeholder="seu@email.com"
              class="w-full border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[13px] px-4 py-[11px] outline-none focus:border-feitu-blue-deep focus:ring-2 focus:ring-[rgba(94,139,182,.2)] text-[14px] bg-[#F7F4EE] dark:bg-night-card text-feitu-text dark:text-night-text placeholder:text-[#C4BDB0] transition"
            />
          </div>

          <div>
            <label class="block text-[12.5px] font-semibold text-[#8C857B] mb-[6px] uppercase tracking-[.04em]">Senha</label>
            <input
              v-model="senha"
              type="password"
              required
              placeholder="••••••••"
              class="w-full border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[13px] px-4 py-[11px] outline-none focus:border-feitu-blue-deep focus:ring-2 focus:ring-[rgba(94,139,182,.2)] text-[14px] bg-[#F7F4EE] dark:bg-night-card text-feitu-text dark:text-night-text placeholder:text-[#C4BDB0] transition"
            />
          </div>

          <p v-if="erro" class="text-[12.5px] text-red-500 bg-red-50 dark:bg-red-950/30 rounded-[10px] px-3 py-2">{{ erro }}</p>

          <button
            type="submit"
            :disabled="carregando"
            class="w-full bg-feitu-blue-deep text-white font-semibold text-[14.5px] py-[13px] rounded-[14px] hover:opacity-90 transition disabled:opacity-50 mt-2"
          >
            {{ carregando ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </div>

      <p class="text-[13.5px] text-center text-[#9A9388]">
        Sem conta?
        <RouterLink to="/register" class="text-feitu-blue-deep font-semibold hover:underline">Criar conta</RouterLink>
      </p>
    </div>
  </div>
</template>
