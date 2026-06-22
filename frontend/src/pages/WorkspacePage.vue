<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '../stores/workspaceStore'
import TabBar from '../components/TabBar.vue'
import TaskList from '../components/TaskList.vue'
import HistoricoView from '../components/HistoricoView.vue'

const wsStore = useWorkspaceStore()
const router = useRouter()
const view = ref<'main' | 'history'>('main')

const workspaceAtivo = computed(() =>
  wsStore.workspaces.find((w) => w.id === wsStore.ativoId) ?? null,
)

onMounted(() => wsStore.carregar())
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-lg mx-auto">

    <!-- Header -->
    <header class="flex items-center justify-between px-4 pt-5 pb-3">
      <div class="flex items-center gap-[10px]">
        <!-- Arc+check logo -->
        <div class="flex-shrink-0 w-[34px] h-[34px] rounded-[11px] bg-[#EAF1F8] flex items-center justify-center">
          <svg width="28" height="28" viewBox="0 0 56 56" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle
              cx="28" cy="28" r="18"
              stroke="#5E8BB6"
              stroke-width="4"
              stroke-linecap="round"
              stroke-dasharray="87 26"
              fill="none"
              transform="rotate(-46 28 28)"
            />
            <path d="M19 28l6 6 12-12" stroke="#E07B4F" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="font-display italic text-[26px] text-feitu-text dark:text-night-text leading-none select-none">Feitu</span>
      </div>

      <div class="flex items-center gap-[8px]">
        <!-- Resumo (analytics) -->
        <RouterLink
          to="/analytics"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] bg-white dark:bg-night-surface dark:border-[rgba(255,255,255,.06)] text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Resumo"
        >
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
          </svg>
        </RouterLink>

        <!-- Settings -->
        <button
          @click="router.push('/settings')"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] bg-white dark:bg-night-surface dark:border-[rgba(255,255,255,.06)] text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Configurações"
        >
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="18" x2="14" y2="18"/>
            <circle cx="20" cy="18" r="2"/><circle cx="20" cy="12" r="2"/><circle cx="8" cy="6" r="2"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- Main view -->
    <template v-if="view === 'main'">
      <TabBar />

      <div class="flex-1 overflow-y-auto">
        <TaskList
          v-if="wsStore.ativoId"
          :workspace-id="wsStore.ativoId"
          :workspace-cor="workspaceAtivo?.cor ?? '#A7C7E7'"
          @abrir-historico="view = 'history'"
        />
        <div v-else class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">
          Crie um workspace para começar
        </div>
      </div>
    </template>

    <!-- Histórico sub-view -->
    <HistoricoView
      v-else
      :workspace-id="wsStore.ativoId ?? ''"
      :workspace-cor="workspaceAtivo?.cor ?? '#A7C7E7'"
      @voltar="view = 'main'"
    />

  </div>
</template>
