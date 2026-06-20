<script setup lang="ts">
import { ref } from 'vue'
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

async function acao(fn: () => Promise<void>) {
  if (carregando.value) return
  carregando.value = true
  try {
    await fn()
  } finally {
    carregando.value = false
  }
}

async function salvarNome() {
  editando.value = false
  if (nomeEdit.value.trim() && nomeEdit.value !== props.tarefa.nome) {
    await store.renomear(props.workspaceId, props.tarefa.id, nomeEdit.value.trim())
  }
}
</script>

<template>
  <div
    class="flex items-center gap-3 px-4 py-3 bg-white rounded-2xl shadow-sm hover:shadow transition group"
  >
    <!-- Nome -->
    <div class="flex-1 min-w-0">
      <input
        v-if="editando"
        v-model="nomeEdit"
        @blur="salvarNome"
        @keydown.enter="salvarNome"
        @keydown.esc="editando = false"
        class="w-full text-sm text-feitu-text outline-none border-b border-feitu-blue"
        v-focus
      />
      <span
        v-else
        @click="editando = true"
        class="text-sm text-feitu-text truncate block cursor-text"
      >
        {{ tarefa.nome }}
      </span>
    </div>

    <!-- Tempo -->
    <span
      class="font-mono text-sm tabular-nums"
      :class="tarefa.estado === 'RUNNING' ? 'text-feitu-text' : 'text-feitu-text/50'"
    >
      {{ formatarTempo(segundos) }}
    </span>

    <!-- Confirmação de delete inline -->
    <div v-if="confirmandoDelete" class="flex items-center gap-1">
      <span class="text-xs text-red-400 mr-1">apagar?</span>
      <button
        @click="acao(() => store.deletar(workspaceId, tarefa.id))"
        :disabled="carregando"
        class="px-2 py-1 text-xs rounded-lg bg-red-100 text-red-600 hover:bg-red-200 transition disabled:opacity-40"
        title="Confirmar"
      >✓</button>
      <button
        @click="confirmandoDelete = false"
        class="px-2 py-1 text-xs rounded-lg bg-gray-100 hover:bg-gray-200 transition"
        title="Cancelar"
      >✗</button>
    </div>

    <!-- Botões de ação -->
    <div v-else class="flex items-center gap-1 transition opacity-100 md:opacity-0 md:group-hover:opacity-100">
      <template v-if="tarefa.estado === 'IDLE'">
        <button
          @click="acao(() => store.iniciarTimer(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-feitu-teal hover:opacity-80 transition disabled:opacity-40"
          title="Iniciar"
        >▶</button>
      </template>

      <template v-if="tarefa.estado === 'RUNNING'">
        <button
          @click="acao(() => store.pausarTimer(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-feitu-peach hover:opacity-80 transition disabled:opacity-40"
          title="Pausar"
        >⏸</button>
        <button
          @click="acao(() => store.pararTimer(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-red-100 hover:opacity-80 transition disabled:opacity-40"
          title="Parar"
        >⏹</button>
      </template>

      <template v-if="tarefa.estado === 'PAUSED'">
        <button
          @click="acao(() => store.iniciarTimer(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-feitu-teal hover:opacity-80 transition disabled:opacity-40"
          title="Retomar"
        >▶</button>
        <button
          @click="acao(() => store.pararTimer(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-red-100 hover:opacity-80 transition disabled:opacity-40"
          title="Parar"
        >⏹</button>
      </template>

      <template v-if="tarefa.estado === 'DONE'">
        <button
          @click="acao(() => store.reativar(workspaceId, tarefa.id))"
          :disabled="carregando"
          class="px-2 py-1 text-xs rounded-lg bg-gray-100 hover:opacity-80 transition disabled:opacity-40"
          title="Reativar"
        >↺</button>
      </template>

      <button
        @click="confirmandoDelete = true"
        :disabled="carregando"
        class="px-2 py-1 text-xs rounded-lg text-feitu-text/30 hover:text-red-400 hover:bg-red-50 transition disabled:opacity-40"
        title="Deletar"
      >✕</button>
    </div>
  </div>
</template>
