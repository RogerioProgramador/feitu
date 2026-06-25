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
  return d.toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })
})

onMounted(() => wsStore.carregar())
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header: data + botões de ação (sem logo) -->
    <div class="flex items-start justify-between px-[22px] pt-[8px] pb-4">
      <div>
        <p class="text-[27px] font-medium text-feitu-text dark:text-night-text leading-[1.05] tracking-[-0.01em] capitalize">
          {{ diaSemana }}
        </p>
        <div class="flex items-center gap-[9px] mt-[6px]">
          <span class="flex-shrink-0 w-[20px] h-[3px] rounded-[2px] bg-feitu-blue-deep dark:bg-feitu-blue"></span>
          <span class="text-[14px] text-[#8C857B] dark:text-[#8E8A9A] leading-none">{{ dataFormatada }}</span>
        </div>
      </div>

      <div class="flex items-center gap-[8px] mt-[8px]">
        <!-- Resumo: checklist-with-checks icon -->
        <RouterLink
          to="/analytics"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-[#F7F4EE] dark:bg-night-surface text-[#5E5A52] dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Resumo"
        >
          <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 6h11M9 12h11M9 18h11" stroke-width="2"/>
            <path d="M4 6l1 1 1.6-1.8M4 12l1 1 1.6-1.8M4 18l1 1 1.6-1.8" stroke-width="1.8"/>
          </svg>
        </RouterLink>

        <!-- Configurações: sliders icon -->
        <button
          @click="router.push('/settings')"
          class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-[#F7F4EE] dark:bg-night-surface text-[#5E5A52] dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
          title="Configurações"
        >
          <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M3 8h18M3 16h18"/>
            <circle cx="9" cy="8" r="2.6" fill="#F7F4EE" stroke="currentColor" stroke-width="2"/>
            <circle cx="15" cy="16" r="2.6" fill="#F7F4EE" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
      </div>
    </div>

    <TabBar />

    <div class="flex-1 overflow-y-auto">
      <TaskList
        v-if="wsStore.ativoId"
        :workspace-id="wsStore.ativoId"
        :date="hoje"
      />

      <!-- Empty state -->
      <div
        v-else
        class="flex flex-col items-center justify-center px-10 pb-[60px] text-center"
        style="min-height: 400px;"
      >
        <div class="w-[104px] h-[104px] rounded-[28px] bg-[#F7F4EE] dark:bg-night-surface border border-[rgba(54,51,46,.07)] dark:border-[rgba(255,255,255,.05)] flex items-center justify-center shadow-[0_12px_30px_-16px_rgba(54,51,46,.3)]">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <rect x="3.5" y="6" width="17" height="13" rx="3" stroke="#A7C7E7" stroke-width="2"/>
            <path d="M3.5 10h17" stroke="#A7C7E7" stroke-width="2"/>
            <path d="M9 4.5h6" stroke="#FFDAC1" stroke-width="2.4" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="text-[20px] font-medium text-feitu-text dark:text-night-text mt-[26px] leading-[1.3]">
          Crie um workspace para começar
        </p>
        <p class="text-[14.5px] text-[#8C857B] dark:text-night-text/50 mt-[10px] leading-[1.55]">
          Organize suas tarefas por contexto — trabalho, estudos, casa.
        </p>
        <button
          @click="wsStore.criandoNovo = true"
          class="flex items-center gap-[9px] mt-[26px] px-[22px] py-[14px] rounded-[14px] bg-feitu-blue-deep text-white text-[15px] font-semibold shadow-[0_10px_24px_-10px_rgba(94,139,182,.6)]"
        >
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Criar workspace
        </button>
      </div>
    </div>

  </div>
</template>
