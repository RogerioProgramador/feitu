<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import { formatarTempo } from '../utils/formatarTempo'
import type { Tarefa } from '../types'

const props = defineProps<{ workspaceId: string; tarefas: Tarefa[] }>()

const store = useTarefaStore()
const aberto = ref(true)

const concluidas = computed(() => props.tarefas.filter((t) => t.estado === 'DONE'))

function horaConclusao(t: Tarefa) {
  if (!t.concluidoEm) return ''
  return new Date(t.concluidoEm).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div v-if="concluidas.length" class="mt-4">
    <button
      @click="aberto = !aberto"
      class="flex items-center gap-2 text-sm text-feitu-text/60 hover:text-feitu-text transition px-1 mb-2"
    >
      <span>{{ aberto ? '▾' : '▸' }}</span>
      <span>Concluídas ({{ concluidas.length }})</span>
    </button>

    <div v-if="aberto" class="space-y-2">
      <div
        v-for="t in concluidas"
        :key="t.id"
        class="flex items-center gap-3 px-4 py-3 bg-feitu-teal/30 rounded-2xl group"
      >
        <span class="flex-1 text-sm text-feitu-text/70 line-through truncate">{{ t.nome }}</span>
        <span class="font-mono text-xs text-feitu-text/50 tabular-nums">
          {{ formatarTempo(t.tempoTotalSegundos) }}
        </span>
        <span class="text-xs text-feitu-text/40">{{ horaConclusao(t) }}</span>
        <button
          @click="store.reativar(workspaceId, t.id)"
          class="opacity-0 group-hover:opacity-100 text-xs px-2 py-1 rounded-lg bg-white hover:shadow transition"
          title="Reativar"
        >↺</button>
      </div>
    </div>
  </div>
</template>
