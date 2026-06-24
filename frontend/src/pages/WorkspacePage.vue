<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '../stores/workspaceStore'
import TabBar from '../components/TabBar.vue'
import TaskList from '../components/TaskList.vue'
import { hojeISO } from '../utils/formatarData'

const wsStore = useWorkspaceStore()
const router = useRouter()

const hoje = hojeISO()

const diaSemana = computed(() => {
  const d = new Date(hoje + 'T12:00:00')
  return d.toLocaleDateString('pt-BR', { weekday: 'long' })
    .replace(/^\w/, (c) => c.toUpperCase())
})

const dataFormatada = computed(() => {
  const d = new Date(hoje + 'T12:00:00')
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' }).replace('.', '')
})

onMounted(() => wsStore.carregar())
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header -->
    <header class="flex items-center justify-between px-4 pt-5 pb-3">
      <div class="flex items-center gap-[10px]">
        <div class="flex-shrink-0 w-[34px] h-[34px] rounded-[11px] bg-[#EAF1F8] dark:bg-[#1A2330] flex items-center justify-center">
          <svg width="28" height="28" viewBox="0 0 56 56" fill="none">
            <circle cx="28" cy="28" r="18" stroke="#5E8BB6" stroke-width="4" stroke-linecap="round"
              stroke-dasharray="87 26" fill="none" transform="rotate(-46 28 28)"/>
            <path d="M19 28l6 6 12-12" stroke="#E07B4F" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="font-display italic text-[26px] text-feitu-text dark:text-night-text leading-none select-none">Feitu</span>
      </div>

      <div class="flex items-center gap-[8px]">
        <RouterLink
          to="/analytics"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] bg-white dark:bg-night-surface dark:border-[rgba(255,255,255,.06)] text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Resumo"
        >
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
          </svg>
        </RouterLink>

        <button
          @click="router.push('/settings')"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] bg-white dark:bg-night-surface dark:border-[rgba(255,255,255,.06)] text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Configurações"
        >
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- Data em destaque -->
    <div class="px-4 pb-3">
      <p class="text-[28px] font-semibold text-feitu-text dark:text-night-text leading-tight capitalize">{{ diaSemana }}</p>
      <div class="flex items-center gap-2 mt-[2px]">
        <p class="text-[13px] text-[#8C857B] dark:text-night-text/50">{{ dataFormatada }}</p>
        <span class="flex-1 h-[1px] bg-[rgba(54,51,46,.1)] dark:bg-[rgba(255,255,255,.06)]"/>
      </div>
    </div>

    <TabBar />

    <div class="flex-1 overflow-y-auto">
      <TaskList
        v-if="wsStore.ativoId"
        :workspace-id="wsStore.ativoId"
        :date="hoje"
      />
      <div v-else class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">
        Crie um workspace para começar
      </div>
    </div>

  </div>
</template>
