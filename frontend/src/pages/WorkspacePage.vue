<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useWorkspaceStore } from '../stores/workspaceStore'
import TabBar from '../components/TabBar.vue'
import TaskList from '../components/TaskList.vue'
import SettingsModal from '../components/SettingsModal.vue'

const wsStore = useWorkspaceStore()
const showSettings = ref(false)

const workspaceAtivo = computed(() =>
  wsStore.workspaces.find((w) => w.id === wsStore.ativoId) ?? null,
)

onMounted(() => wsStore.carregar())
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-lg mx-auto">
    <header class="flex items-center justify-between px-4 pt-4 pb-2">
      <div class="flex items-center gap-2">
        <svg width="30" height="30" viewBox="0 0 30 30" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <rect width="30" height="30" rx="9" fill="#A7C7E7"/>
          <path d="M8 8L8 22" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M8 8L18 8" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M8 15L15 15" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M15 13.5L18 17.5L24 10" stroke="white" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="font-display italic font-semibold text-xl text-feitu-text dark:text-night-text tracking-tight leading-none select-none">Feitu</span>
      </div>

      <div class="flex items-center gap-2">
        <RouterLink
          to="/analytics"
          class="text-sm font-medium text-feitu-text/70 dark:text-night-text/70 hover:text-feitu-text dark:hover:text-night-text border border-feitu-text/20 dark:border-night-text/20 rounded-xl px-3 py-1.5 transition hover:border-feitu-text/40 dark:hover:border-night-text/40"
        >
          Resumo
        </RouterLink>

        <button
          @click="showSettings = true"
          class="h-9 w-9 flex items-center justify-center rounded-xl text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text hover:bg-white/60 dark:hover:bg-night-surface/60 transition"
          title="Configurações"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        </button>
      </div>
    </header>

    <TabBar />

    <div
      class="h-0.5 mx-4 rounded-full transition-colors"
      :style="{ backgroundColor: workspaceAtivo?.cor ?? '#A7C7E7' }"
    />

    <div class="flex-1 overflow-y-auto">
      <TaskList v-if="wsStore.ativoId" :workspace-id="wsStore.ativoId" />
      <div v-else class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">
        Crie um workspace para começar
      </div>
    </div>

    <SettingsModal v-if="showSettings" @close="showSettings = false" />
  </div>
</template>
