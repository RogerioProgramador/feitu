<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import TaskItem from './TaskItem.vue'
import CriacaoAvancadaSheet from './CriacaoAvancadaSheet.vue'

const props = defineProps<{ workspaceId: string; date?: string }>()

const store = useTarefaStore()
const sheetOpen = ref(false)
const recorrentesExpand = ref(true)
const pontuaisExpand = ref(true)

const recorrentes = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter((t) => t.tipo === 'RECORRENTE'),
)
const pontuais = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter((t) => t.tipo === 'PONTUAL'),
)

watch(
  () => [props.workspaceId, props.date] as const,
  ([id, date]) => { if (id) store.carregar(id, date) },
  { immediate: true },
)

async function quickAdd() {
  await store.criar(props.workspaceId, { nome: '', tipo: 'PONTUAL' }, props.date)
}
</script>

<template>
  <div class="flex flex-col gap-[10px] p-4">

    <!-- Seção Recorrentes -->
    <div v-if="recorrentes.length > 0">
      <button
        @click="recorrentesExpand = !recorrentesExpand"
        class="flex items-center gap-1 mb-2 text-[11px] font-semibold text-feitu-blue-deep dark:text-feitu-blue tracking-widest uppercase select-none"
      >
        <svg
          width="11" height="11" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2.5" stroke-linecap="round"
          class="transition-transform"
          :class="recorrentesExpand ? 'rotate-90' : ''"
        >
          <polyline points="9 18 15 12 9 6"/>
        </svg>
        Recorrentes
      </button>
      <div v-if="recorrentesExpand" class="flex flex-col gap-[8px] animate-feitu-fade">
        <TaskItem
          v-for="t in recorrentes"
          :key="t.id"
          :tarefa="t"
          :workspace-id="workspaceId"
          :date="date"
        />
      </div>
    </div>

    <!-- Seção Pontuais -->
    <div :class="recorrentes.length > 0 ? 'mt-1' : ''">
      <button
        v-if="pontuais.length > 0"
        @click="pontuaisExpand = !pontuaisExpand"
        class="flex items-center gap-1 mb-2 text-[11px] font-semibold text-feitu-lavender-deep dark:text-feitu-lavender tracking-widest uppercase select-none"
      >
        <svg
          width="11" height="11" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2.5" stroke-linecap="round"
          class="transition-transform"
          :class="pontuaisExpand ? 'rotate-90' : ''"
        >
          <polyline points="9 18 15 12 9 6"/>
        </svg>
        Pontuais
      </button>
      <div v-if="pontuaisExpand" class="flex flex-col gap-[8px] animate-feitu-fade">
        <TaskItem
          v-for="t in pontuais"
          :key="t.id"
          :tarefa="t"
          :workspace-id="workspaceId"
          :date="date"
        />
      </div>
    </div>

    <!-- Adicionar -->
    <div class="flex gap-[8px] mt-1">
      <button
        @click="quickAdd"
        class="flex-1 flex items-center gap-2 px-4 py-3 text-feitu-text/50 dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text text-[13.5px] rounded-[15px] hover:bg-white/60 dark:hover:bg-night-surface/60 transition text-left border border-dashed border-[rgba(54,51,46,.15)] dark:border-[rgba(255,255,255,.08)]"
      >
        <span class="text-lg leading-none">+</span>
        <span>Nova tarefa</span>
      </button>
      <button
        @click="sheetOpen = true"
        class="px-3 py-3 rounded-[15px] border border-dashed border-[rgba(54,51,46,.15)] dark:border-[rgba(255,255,255,.08)] text-feitu-text/40 dark:text-night-text/40 hover:text-feitu-text dark:hover:text-night-text transition"
        title="Criação avançada"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M12 5v14M5 12h14"/>
        </svg>
      </button>
    </div>
  </div>

  <CriacaoAvancadaSheet
    v-if="sheetOpen"
    :workspace-id="workspaceId"
    :date="date"
    @fechar="sheetOpen = false"
    @criada="sheetOpen = false"
  />
</template>
