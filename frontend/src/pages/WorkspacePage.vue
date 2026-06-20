<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '../stores/workspaceStore'
import { useAuthStore } from '../stores/authStore'
import TabBar from '../components/TabBar.vue'
import TaskList from '../components/TaskList.vue'

const router = useRouter()
const wsStore = useWorkspaceStore()
const authStore = useAuthStore()

const workspaceAtivo = computed(() =>
  wsStore.workspaces.find((w) => w.id === wsStore.ativoId) ?? null,
)

onMounted(() => wsStore.carregar())

function sair() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg flex flex-col max-w-lg mx-auto">
    <header class="flex items-center justify-between px-4 pt-4 pb-2">
      <h1 class="text-xl font-semibold text-feitu-text">Feitu</h1>
      <div class="flex items-center gap-3">
        <RouterLink to="/analytics" class="text-sm text-feitu-text/60 hover:text-feitu-text">
          Resumo
        </RouterLink>
        <button @click="sair" class="text-sm text-feitu-text/60 hover:text-feitu-text">
          Sair
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
      <div v-else class="p-8 text-center text-feitu-text/40 text-sm">
        Crie um workspace para começar
      </div>
    </div>
  </div>
</template>
