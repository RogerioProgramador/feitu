<script setup lang="ts">
import { ref } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import type { Tarefa } from '../types'

const props = defineProps<{ tarefa: Tarefa; workspaceId: string; date?: string }>()

const store = useTarefaStore()
const carregando = ref(false)
const expandido = ref(false)

async function toggleConcluida() {
  if (carregando.value) return
  carregando.value = true
  try {
    if (props.tarefa.concluida) {
      await store.reabrir(props.workspaceId, props.tarefa.id, props.date)
    } else {
      await store.concluir(props.workspaceId, props.tarefa.id, props.date)
    }
  } finally {
    carregando.value = false
  }
}

async function deletar() {
  if (carregando.value) return
  carregando.value = true
  try {
    await store.deletar(props.workspaceId, props.tarefa.id)
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div
    class="rounded-[16px] bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)] shadow-[0_1px_2px_rgba(54,51,46,.03)]"
  >
    <!-- Linha principal -->
    <div class="flex items-center gap-[14px] px-[15px] py-[13px]">

      <!-- Checkbox 28×28 -->
      <button
        @click="toggleConcluida"
        :disabled="carregando"
        class="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center transition-all disabled:opacity-50"
        :class="tarefa.concluida
          ? 'bg-feitu-teal border-none'
          : 'border-[2px] border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.2)] bg-transparent'"
      >
        <svg
          v-if="tarefa.concluida"
          class="animate-feitu-pop"
          width="15" height="15" viewBox="0 0 24 24"
          fill="none" stroke="#2F7D63" stroke-width="2.8"
          stroke-linecap="round" stroke-linejoin="round"
        >
          <path d="M5 12.5l4.5 4.5L19 7"/>
        </svg>
      </button>

      <!-- Nome (clique expande/recolhe a desc) -->
      <button
        @click="expandido = !expandido"
        class="flex-1 min-w-0 text-left bg-transparent border-none p-0 cursor-pointer"
        style="font: 500 15px/1.25 'Space Grotesk';"
        :class="tarefa.concluida
          ? 'text-[#A49C90] dark:text-night-text/40'
          : 'text-feitu-text dark:text-night-text'"
      >
        <span class="block truncate">{{ tarefa.nome }}</span>
      </button>

      <!-- Horário (apenas recorrentes, à direita) -->
      <span
        v-if="tarefa.tipo === 'RECORRENTE' && tarefa.horario"
        class="flex-shrink-0 text-[13px] font-medium text-[#A49C90] dark:text-[#7C7888] leading-none"
      >{{ tarefa.horario }}</span>
    </div>

    <!-- Área expandida: descrição + ação de deletar -->
    <div
      v-if="expandido"
      class="animate-feitu-fade px-[15px] pb-[12px]"
      style="padding-left: 57px;"
    >
      <p
        v-if="tarefa.descricao"
        class="text-[13.5px] leading-[1.5] text-[#6E6A62] dark:text-night-text/60 mb-[8px]"
      >{{ tarefa.descricao }}</p>

      <button
        @click="deletar"
        :disabled="carregando"
        class="flex items-center gap-[5px] text-[12px] text-[#B0A89B] dark:text-night-text/30 hover:text-red-400 dark:hover:text-red-400 transition disabled:opacity-40"
      >
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/>
        </svg>
        Deletar
      </button>
    </div>
  </div>
</template>
