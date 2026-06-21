<script setup lang="ts">
import { useSettingsStore } from '../stores/settingsStore'
import { useAuthStore } from '../stores/authStore'
import { useRouter } from 'vue-router'

const emit = defineEmits<{ close: [] }>()
const settings = useSettingsStore()
const authStore = useAuthStore()
const router = useRouter()

function sair() {
  authStore.logout()
  router.replace('/login')
  emit('close')
}
</script>

<template>
  <div
    class="fixed inset-0 bg-black/40 z-50 flex items-end justify-center"
    @click.self="emit('close')"
  >
    <div class="bg-feitu-bg dark:bg-night-surface rounded-t-3xl w-full max-w-lg p-6 space-y-5 shadow-xl">
      <div class="flex items-center justify-between">
        <h2 class="text-base font-semibold text-feitu-text dark:text-night-text">Configurações</h2>
        <button
          @click="emit('close')"
          class="h-8 w-8 flex items-center justify-center rounded-xl text-feitu-text/50 dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text hover:bg-feitu-text/10 dark:hover:bg-night-text/10 transition text-lg leading-none"
        >✕</button>
      </div>

      <!-- Modo noturno -->
      <div class="flex items-center justify-between py-1">
        <div>
          <p class="text-sm font-medium text-feitu-text dark:text-night-text">Modo noturno</p>
          <p class="text-xs text-feitu-text/50 dark:text-night-text/50 mt-0.5">Tema escuro para o app</p>
        </div>
        <button
          @click="settings.modoNoturno = !settings.modoNoturno"
          class="relative inline-flex h-6 w-11 flex-shrink-0 items-center rounded-full transition-colors"
          :class="settings.modoNoturno ? 'bg-feitu-blue' : 'bg-feitu-text/20 dark:bg-night-text/20'"
        >
          <span
            class="inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform"
            :class="settings.modoNoturno ? 'translate-x-6' : 'translate-x-1'"
          />
        </button>
      </div>

      <!-- Fuso horário -->
      <div class="space-y-2">
        <div>
          <p class="text-sm font-medium text-feitu-text dark:text-night-text">Fuso horário</p>
          <p class="text-xs text-feitu-text/50 dark:text-night-text/50 mt-0.5">Define o horário base do app</p>
        </div>
        <div class="flex gap-2">
          <button
            @click="settings.fusoHorario = 'BR'"
            class="flex-1 py-2 px-3 rounded-xl text-sm font-medium transition"
            :class="settings.fusoHorario === 'BR'
              ? 'bg-feitu-blue text-feitu-text shadow-sm'
              : 'bg-feitu-text/8 dark:bg-night-text/10 text-feitu-text/60 dark:text-night-text/60 hover:bg-feitu-text/12 dark:hover:bg-night-text/15'"
          >Brasil (GMT-3)</button>
          <button
            @click="settings.fusoHorario = 'UTC'"
            class="flex-1 py-2 px-3 rounded-xl text-sm font-medium transition"
            :class="settings.fusoHorario === 'UTC'
              ? 'bg-feitu-blue text-feitu-text shadow-sm'
              : 'bg-feitu-text/8 dark:bg-night-text/10 text-feitu-text/60 dark:text-night-text/60 hover:bg-feitu-text/12 dark:hover:bg-night-text/15'"
          >GMT-0 (UTC)</button>
        </div>
      </div>

      <!-- Sair -->
      <div class="pt-2 border-t border-feitu-text/10 dark:border-night-text/10">
        <button
          @click="sair"
          class="w-full py-3 text-sm font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-xl transition"
        >
          Sair da conta
        </button>
      </div>
    </div>
  </div>
</template>
