<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSettingsStore } from '../stores/settingsStore'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const settings = useSettingsStore()
const authStore = useAuthStore()

type Tema = 'claro' | 'escuro' | 'sistema'
const TEMAS: { value: Tema; label: string }[] = [
  { value: 'claro', label: 'Claro' },
  { value: 'escuro', label: 'Escuro' },
  { value: 'sistema', label: 'Sistema' },
]

function sair() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header -->
    <header class="flex items-center gap-3 px-4 pt-5 pb-4">
      <button
        @click="router.back()"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-white dark:bg-night-surface text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h1 class="text-[17px] font-semibold text-feitu-text dark:text-night-text">Configurações</h1>
    </header>

    <div class="flex-1 overflow-y-auto px-4 pb-8 space-y-3">

      <!-- Tema -->
      <div class="bg-white dark:bg-night-surface rounded-[18px] p-5">
        <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em] mb-3">Tema</p>
        <div class="flex bg-feitu-surface dark:bg-night-card rounded-[12px] p-[3px]">
          <button
            v-for="t in TEMAS"
            :key="t.value"
            @click="settings.tema = t.value"
            class="flex-1 py-2 rounded-[10px] text-[13px] font-medium transition-all"
            :class="settings.tema === t.value
              ? 'bg-white dark:bg-night-surface shadow-sm text-feitu-blue-deep dark:text-feitu-blue'
              : 'text-[#8C857B] dark:text-night-text/50'"
          >{{ t.label }}</button>
        </div>
      </div>

      <!-- Fuso horário -->
      <div class="bg-white dark:bg-night-surface rounded-[18px] p-5">
        <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em] mb-1">Fuso horário</p>
        <p class="text-[12px] text-[#9A9388] mb-3">Define o horário base do app</p>
        <div class="flex gap-2">
          <button
            @click="settings.fusoHorario = 'BR'"
            class="flex-1 py-[9px] px-3 rounded-[11px] text-[13px] font-medium transition"
            :class="settings.fusoHorario === 'BR'
              ? 'bg-feitu-blue-deep text-white shadow-sm'
              : 'bg-[#F0EDE6] dark:bg-night-card text-[#8C857B]'"
          >Brasil (GMT-3)</button>
          <button
            @click="settings.fusoHorario = 'UTC'"
            class="flex-1 py-[9px] px-3 rounded-[11px] text-[13px] font-medium transition"
            :class="settings.fusoHorario === 'UTC'
              ? 'bg-feitu-blue-deep text-white shadow-sm'
              : 'bg-[#F0EDE6] dark:bg-night-card text-[#8C857B]'"
          >UTC (GMT+0)</button>
        </div>
      </div>

      <!-- Conta -->
      <div class="bg-white dark:bg-night-surface rounded-[18px] overflow-hidden">
        <div class="px-5 py-4 border-b border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]">
          <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em]">Conta</p>
        </div>
        <div class="flex items-center gap-3 px-5 py-4">
          <div class="w-10 h-10 rounded-full bg-feitu-blue flex items-center justify-center flex-shrink-0">
            <span class="text-[16px] font-semibold text-feitu-blue-deep">
              {{ authStore.email?.[0]?.toUpperCase() ?? '?' }}
            </span>
          </div>
          <p class="text-[13.5px] text-[#6E6A62] dark:text-night-text/60 truncate">{{ authStore.email }}</p>
        </div>
        <button
          @click="sair"
          class="w-full px-5 py-[14px] text-[14px] font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-900/15 transition text-left border-t border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]"
        >Sair da conta</button>
      </div>

    </div>
  </div>
</template>
