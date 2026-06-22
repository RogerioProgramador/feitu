<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSettingsStore } from '../stores/settingsStore'
import { useAuthStore } from '../stores/authStore'
import { useWorkspaceStore } from '../stores/workspaceStore'

const router = useRouter()
const settings = useSettingsStore()
const authStore = useAuthStore()
const wsStore = useWorkspaceStore()

async function deletarWorkspace(id: string) {
  await wsStore.deletar(id)
}

function sair() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-lg mx-auto">

    <!-- Header -->
    <header class="flex items-center gap-3 px-4 pt-5 pb-4">
      <button
        @click="router.back()"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-white dark:bg-night-surface text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h1 class="text-[17px] font-semibold text-feitu-text dark:text-night-text">Configurações</h1>
    </header>

    <div class="flex-1 overflow-y-auto px-4 pb-8 space-y-3">

      <!-- Preferências -->
      <div class="bg-white dark:bg-night-surface rounded-[18px] overflow-hidden">
        <div class="px-5 py-4 border-b border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]">
          <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em]">Preferências</p>
        </div>

        <!-- Modo noturno -->
        <div class="flex items-center justify-between px-5 py-4">
          <div>
            <p class="text-[14px] font-medium text-feitu-text dark:text-night-text">Modo noturno</p>
            <p class="text-[12px] text-[#9A9388] mt-[2px]">Tema escuro para o app</p>
          </div>
          <button
            @click="settings.modoNoturno = !settings.modoNoturno"
            class="relative inline-flex h-[28px] w-[48px] flex-shrink-0 items-center rounded-[16px] transition-colors"
            :class="settings.modoNoturno ? 'bg-feitu-blue-deep' : 'bg-[#E0DACE] dark:bg-night-text/20'"
          >
            <span
              class="inline-block h-[20px] w-[20px] transform rounded-full bg-white shadow transition-transform"
              :class="settings.modoNoturno ? 'translate-x-[24px]' : 'translate-x-[4px]'"
            />
          </button>
        </div>

        <!-- Fuso horário -->
        <div class="px-5 py-4 border-t border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]">
          <p class="text-[14px] font-medium text-feitu-text dark:text-night-text mb-1">Fuso horário</p>
          <p class="text-[12px] text-[#9A9388] mb-3">Define o horário base do app</p>
          <div class="flex gap-2">
            <button
              @click="settings.fusoHorario = 'BR'"
              class="flex-1 py-[9px] px-3 rounded-[11px] text-[13px] font-medium transition"
              :class="settings.fusoHorario === 'BR'
                ? 'bg-feitu-blue-deep text-white shadow-sm'
                : 'bg-[#F0EDE6] dark:bg-night-card text-[#8C857B] hover:bg-[#E8E3D8]'"
            >Brasil (GMT-3)</button>
            <button
              @click="settings.fusoHorario = 'UTC'"
              class="flex-1 py-[9px] px-3 rounded-[11px] text-[13px] font-medium transition"
              :class="settings.fusoHorario === 'UTC'
                ? 'bg-feitu-blue-deep text-white shadow-sm'
                : 'bg-[#F0EDE6] dark:bg-night-card text-[#8C857B] hover:bg-[#E8E3D8]'"
            >UTC (GMT+0)</button>
          </div>
        </div>
      </div>

      <!-- Workspaces -->
      <div v-if="wsStore.workspaces.length" class="bg-white dark:bg-night-surface rounded-[18px] overflow-hidden">
        <div class="px-5 py-4 border-b border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]">
          <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em]">Workspaces</p>
        </div>

        <div
          v-for="(ws, i) in wsStore.workspaces"
          :key="ws.id"
          class="flex items-center gap-3 px-5 py-[13px] transition"
          :class="i > 0 ? 'border-t border-[rgba(54,51,46,.05)] dark:border-[rgba(255,255,255,.04)]' : ''"
        >
          <span class="w-3 h-3 rounded-full flex-shrink-0" :style="{ backgroundColor: ws.cor ?? '#A7C7E7' }"/>
          <span class="flex-1 text-[14px] text-feitu-text dark:text-night-text">{{ ws.nome }}</span>
          <button
            @click="deletarWorkspace(ws.id)"
            class="w-[28px] h-[28px] flex items-center justify-center rounded-[8px] text-[#B0A89B] hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition"
            title="Deletar workspace"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Sair -->
      <div class="bg-white dark:bg-night-surface rounded-[18px] overflow-hidden">
        <button
          @click="sair"
          class="w-full px-5 py-[15px] text-[14px] font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-900/15 transition text-left"
        >
          Sair da conta
        </button>
      </div>

    </div>
  </div>
</template>
