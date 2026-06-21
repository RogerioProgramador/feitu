<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import { useTimer } from '../composables/useTimer'
import { formatarTempo } from '../utils/formatarTempo'
import type { Tarefa } from '../types'

const props = defineProps<{ tarefa: Tarefa; workspaceId: string }>()
const store = useTarefaStore()
const { segundos } = useTimer(
  () => props.tarefa.tempoTotalSegundos,
  () => props.tarefa.estado,
)

const editando = ref(false)
const nomeEdit = ref(props.tarefa.nome)
const carregando = ref(false)
const confirmandoDelete = ref(false)

async function acao(fn: () => Promise<unknown>) {
  if (carregando.value) return
  carregando.value = true
  try { await fn() } finally { carregando.value = false }
}

async function salvarNome() {
  editando.value = false
  if (nomeEdit.value.trim() && nomeEdit.value !== props.tarefa.nome) {
    await store.renomear(props.workspaceId, props.tarefa.id, nomeEdit.value.trim())
  }
}

const acaoPrincipal = computed(() =>
  props.tarefa.estado === 'RUNNING'
    ? () => store.pausarTimer(props.workspaceId, props.tarefa.id)
    : () => store.iniciarTimer(props.workspaceId, props.tarefa.id),
)

const iconePrincipal = computed(() => (props.tarefa.estado === 'RUNNING' ? '⏸' : '▶'))

const tituloPrincipal = computed(() => {
  if (props.tarefa.estado === 'RUNNING') return 'Pausar'
  if (props.tarefa.estado === 'PAUSED') return 'Retomar'
  return 'Iniciar'
})

const classBotaoPrincipal = computed(() => {
  if (props.tarefa.estado === 'RUNNING')
    return 'bg-feitu-peach text-feitu-text shadow-sm'
  if (props.tarefa.estado === 'PAUSED')
    return 'bg-feitu-blue/50 text-feitu-text dark:text-night-text'
  return 'bg-feitu-bg dark:bg-night-bg border border-feitu-text/10 dark:border-night-text/10 text-feitu-text/35 dark:text-night-text/35 hover:border-feitu-teal hover:text-feitu-text/70 dark:hover:text-night-text/70'
})
</script>

<template>
  <div class="flex items-center gap-2.5 px-3 py-2.5 bg-white dark:bg-night-surface rounded-2xl shadow-sm">
    <!-- Botão de ação primário (esquerda) -->
    <button
      @click="acao(acaoPrincipal)"
      :disabled="carregando"
      class="flex-shrink-0 h-9 w-9 flex items-center justify-center rounded-full text-sm transition-all disabled:opacity-40"
      :class="classBotaoPrincipal"
      :title="tituloPrincipal"
    >{{ iconePrincipal }}</button>

    <!-- Nome editável -->
    <div class="flex-1 min-w-0">
      <input
        v-if="editando"
        v-model="nomeEdit"
        @blur="salvarNome"
        @keydown.enter="salvarNome"
        @keydown.esc="editando = false"
        class="w-full text-sm text-feitu-text dark:text-night-text outline-none border-b border-feitu-blue bg-transparent"
        v-focus
      />
      <span
        v-else
        @click="editando = true"
        class="text-sm text-feitu-text dark:text-night-text truncate block cursor-text select-none"
      >{{ tarefa.nome }}</span>
    </div>

    <!-- Timer -->
    <span
      class="font-mono text-sm tabular-nums flex-shrink-0 min-w-[3rem] text-right"
      :class="tarefa.estado === 'RUNNING' ? 'text-feitu-text dark:text-night-text' : 'text-feitu-text/35 dark:text-night-text/35'"
    >{{ formatarTempo(segundos) }}</span>

    <!-- Estado: confirmação de delete -->
    <template v-if="confirmandoDelete">
      <button
        @click="acao(() => store.deletar(workspaceId, tarefa.id))"
        :disabled="carregando"
        class="h-8 px-2 flex-shrink-0 flex items-center justify-center rounded-xl bg-red-100 text-red-600 text-xs font-medium hover:bg-red-200 transition disabled:opacity-40"
      >apagar</button>
      <button
        @click="confirmandoDelete = false"
        class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-feitu-bg dark:bg-night-bg text-feitu-text/50 dark:text-night-text/50 text-xs hover:bg-gray-100 dark:hover:bg-night-bg/80 transition"
      >✗</button>
    </template>

    <!-- Estado: botões normais -->
    <template v-else>
      <button
        v-if="tarefa.estado === 'RUNNING' || tarefa.estado === 'PAUSED'"
        @click="acao(() => store.pararTimer(workspaceId, tarefa.id))"
        :disabled="carregando"
        class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-feitu-bg dark:bg-night-bg text-feitu-text/40 dark:text-night-text/40 text-sm hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-400 transition disabled:opacity-40"
        title="Concluir"
      >⏹</button>
      <button
        @click="confirmandoDelete = true"
        :disabled="carregando"
        class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl text-feitu-text/20 dark:text-night-text/20 text-sm hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition disabled:opacity-40"
        title="Deletar"
      >✕</button>
    </template>
  </div>
</template>
