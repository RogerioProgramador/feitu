<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import { formatarTempo } from '../utils/formatarTempo'
import type { Tarefa } from '../types'

const props = defineProps<{ workspaceId: string; tarefas: Tarefa[] }>()
const store = useTarefaStore()

const abrirRecentes = ref(false)
const abrirHistorico = ref(false)
const confirmandoDeleteId = ref<string | null>(null)

const limite24h = Date.now() - 24 * 60 * 60 * 1000

const concluidas = computed(() => props.tarefas.filter((t) => t.estado === 'DONE'))
const recentes = computed(() =>
  concluidas.value.filter(
    (t) => !t.concluidoEm || new Date(t.concluidoEm).getTime() > limite24h,
  ),
)
const historico = computed(() =>
  concluidas.value.filter(
    (t) => t.concluidoEm && new Date(t.concluidoEm).getTime() <= limite24h,
  ),
)

function hora(t: Tarefa) {
  if (!t.concluidoEm) return ''
  return new Date(t.concluidoEm).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

function dataHora(t: Tarefa) {
  if (!t.concluidoEm) return ''
  return new Date(t.concluidoEm).toLocaleString('pt-BR', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
  })
}

async function deletar(id: string) {
  confirmandoDeleteId.value = null
  await store.deletar(props.workspaceId, id)
}
</script>

<template>
  <div v-if="concluidas.length" class="mt-4 space-y-1">

    <!-- ── Concluídas (< 24h) ────────────────────────────── -->
    <div v-if="recentes.length">
      <button
        @click="abrirRecentes = !abrirRecentes"
        class="w-full flex items-center gap-2 text-sm text-feitu-text/50 hover:text-feitu-text transition px-1 py-1.5"
      >
        <span class="text-xs w-3">{{ abrirRecentes ? '▾' : '▸' }}</span>
        <span>Concluídas ({{ recentes.length }})</span>
      </button>

      <div v-if="abrirRecentes" class="space-y-1.5 mt-1">
        <div
          v-for="t in recentes"
          :key="t.id"
          class="flex items-center gap-2 px-3 py-2.5 bg-feitu-teal/20 rounded-2xl"
        >
          <div class="flex-1 min-w-0">
            <span class="text-sm text-feitu-text/55 line-through truncate block">{{ t.nome }}</span>
          </div>
          <span class="font-mono text-xs text-feitu-text/40 tabular-nums flex-shrink-0">{{ formatarTempo(t.tempoTotalSegundos) }}</span>
          <span class="text-xs text-feitu-text/30 flex-shrink-0 hidden sm:block">{{ hora(t) }}</span>

          <template v-if="confirmandoDeleteId === t.id">
            <button
              @click="deletar(t.id)"
              class="h-8 px-2 flex-shrink-0 flex items-center justify-center rounded-xl bg-red-100 text-red-600 text-xs font-medium hover:bg-red-200 transition"
            >apagar</button>
            <button
              @click="confirmandoDeleteId = null"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-feitu-bg text-feitu-text/50 text-xs hover:bg-gray-100 transition"
            >✗</button>
          </template>
          <template v-else>
            <button
              @click="store.reativar(workspaceId, t.id)"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-white text-feitu-text/50 text-sm hover:shadow hover:text-feitu-text transition"
              title="Reativar"
            >↺</button>
            <button
              @click="confirmandoDeleteId = t.id"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl text-feitu-text/20 text-sm hover:text-red-400 hover:bg-red-50 transition"
              title="Deletar"
            >✕</button>
          </template>
        </div>
      </div>
    </div>

    <!-- ── Histórico (≥ 24h) ─────────────────────────────── -->
    <div v-if="historico.length">
      <button
        @click="abrirHistorico = !abrirHistorico"
        class="w-full flex items-center gap-2 text-sm text-feitu-text/35 hover:text-feitu-text transition px-1 py-1.5"
      >
        <span class="text-xs w-3">{{ abrirHistorico ? '▾' : '▸' }}</span>
        <span>Histórico ({{ historico.length }})</span>
      </button>

      <div v-if="abrirHistorico" class="space-y-1.5 mt-1">
        <div
          v-for="t in historico"
          :key="t.id"
          class="flex items-center gap-2 px-3 py-2.5 bg-feitu-bg border border-feitu-text/8 rounded-2xl"
        >
          <div class="flex-1 min-w-0">
            <span class="text-sm text-feitu-text/35 line-through truncate block">{{ t.nome }}</span>
          </div>
          <span class="font-mono text-xs text-feitu-text/25 tabular-nums flex-shrink-0">{{ formatarTempo(t.tempoTotalSegundos) }}</span>
          <span class="text-xs text-feitu-text/20 flex-shrink-0 hidden sm:block">{{ dataHora(t) }}</span>

          <template v-if="confirmandoDeleteId === t.id">
            <button
              @click="deletar(t.id)"
              class="h-8 px-2 flex-shrink-0 flex items-center justify-center rounded-xl bg-red-100 text-red-600 text-xs font-medium hover:bg-red-200 transition"
            >apagar</button>
            <button
              @click="confirmandoDeleteId = null"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-feitu-bg text-feitu-text/50 text-xs hover:bg-gray-100 transition"
            >✗</button>
          </template>
          <template v-else>
            <button
              @click="store.reativar(workspaceId, t.id)"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl bg-white text-feitu-text/35 text-sm hover:shadow hover:text-feitu-text transition"
              title="Reativar"
            >↺</button>
            <button
              @click="confirmandoDeleteId = t.id"
              class="h-8 w-8 flex-shrink-0 flex items-center justify-center rounded-xl text-feitu-text/15 text-sm hover:text-red-400 hover:bg-red-50 transition"
              title="Deletar"
            >✕</button>
          </template>
        </div>
      </div>
    </div>

  </div>
</template>
