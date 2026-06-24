<script setup lang="ts">
import { ref } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import type { Tarefa } from '../types'

const props = defineProps<{ tarefa: Tarefa; workspaceId: string; date?: string }>()

const store = useTarefaStore()
const carregando = ref(false)
const editando = ref(false)
const nomeEdit = ref(props.tarefa.nome)
const noteOpen = ref(false)
const noteText = ref(props.tarefa.descricao ?? '')
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

async function salvarNome() {
  editando.value = false
  const novo = nomeEdit.value.trim()
  if (novo && novo !== props.tarefa.nome) {
    await store.renomear(props.workspaceId, props.tarefa.id, novo)
  }
}

async function salvarNota() {
  await store.atualizarDescricao(props.workspaceId, props.tarefa.id, noteText.value || null)
  noteOpen.value = false
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
    class="rounded-[16px] bg-white dark:bg-night-card border border-[rgba(54,51,46,.07)] dark:border-[rgba(255,255,255,.05)] shadow-[0_1px_2px_rgba(58,55,51,.04)] overflow-hidden"
  >
    <div class="flex items-center gap-3 px-4 py-[13px]">
      <!-- Checkbox 28×28 -->
      <button
        @click="toggleConcluida"
        :disabled="carregando"
        class="flex-shrink-0 w-7 h-7 rounded-full border-[1.5px] flex items-center justify-center transition-all disabled:opacity-50"
        :class="tarefa.concluida
          ? 'bg-feitu-teal border-feitu-teal-deep'
          : 'bg-transparent border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.2)] hover:border-feitu-teal-deep'"
      >
        <svg
          v-if="tarefa.concluida"
          class="animate-feitu-pop"
          width="14" height="14" viewBox="0 0 24 24"
          fill="none" stroke="#2F7D63" stroke-width="3"
          stroke-linecap="round" stroke-linejoin="round"
        >
          <polyline points="20 6 9 17 4 12"/>
        </svg>
      </button>

      <!-- Nome -->
      <div class="flex-1 min-w-0">
        <input
          v-if="editando"
          v-model="nomeEdit"
          @blur="salvarNome"
          @keydown.enter="salvarNome"
          @keydown.esc="editando = false"
          class="w-full text-[14px] font-medium text-feitu-text dark:text-night-text outline-none border-b border-feitu-blue bg-transparent"
          v-focus
        />
        <span
          v-else
          @click="editando = true"
          class="text-[14px] font-medium cursor-text select-none block truncate leading-snug transition-colors"
          :class="tarefa.concluida
            ? 'line-through text-[#A49C90] dark:text-night-text/35'
            : 'text-feitu-text dark:text-night-text'"
        >{{ tarefa.nome }}</span>

        <!-- Horário (recorrentes) -->
        <span
          v-if="tarefa.tipo === 'RECORRENTE' && tarefa.horario"
          class="text-[11px] text-[#A39C90] dark:text-night-text/40"
        >{{ tarefa.horario }}</span>
      </div>

      <!-- Expand -->
      <button
        @click="expandido = !expandido"
        class="flex-shrink-0 w-7 h-7 flex items-center justify-center rounded-full text-[#C4BDB0] dark:text-night-text/30 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="5" r="1" fill="currentColor" stroke="none"/>
          <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/>
          <circle cx="12" cy="19" r="1" fill="currentColor" stroke="none"/>
        </svg>
      </button>
    </div>

    <!-- Barra de ações (expandido) -->
    <div v-if="expandido" class="animate-feitu-fade flex items-center gap-[6px] px-4 pb-[12px]">
      <button
        @click="noteOpen = true"
        class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] border transition"
        :class="tarefa.descricao
          ? 'border-[rgba(138,95,192,.28)] bg-[#F3ECFB] text-[#8A5FC0] dark:bg-[rgba(138,95,192,.15)] dark:border-[rgba(138,95,192,.25)]'
          : 'border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-white dark:bg-night-surface text-[#A39C90]'"
        title="Nota"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
        </svg>
      </button>

      <button
        @click="deletar"
        :disabled="carregando"
        class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-white dark:bg-night-surface text-[#B0A89B] hover:text-red-400 hover:border-red-200 transition disabled:opacity-40"
        title="Deletar"
      >
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>
  </div>

  <!-- Note bottom sheet -->
  <Teleport to="body">
    <div v-if="noteOpen" class="fixed inset-0 z-30 flex items-end" @click.self="noteOpen = false">
      <div class="absolute inset-0 bg-black/25 dark:bg-black/50 animate-feitu-dim" @click="noteOpen = false"/>
      <div class="relative w-full max-w-lg mx-auto bg-white dark:bg-night-surface rounded-t-[28px] p-5 pb-8 shadow-xl animate-feitu-sheet">
        <div class="w-10 h-1 rounded-full bg-[#DDD8CE] dark:bg-night-card mx-auto mb-4"/>
        <p class="text-[13px] font-semibold text-feitu-text dark:text-night-text mb-2">Nota — {{ tarefa.nome }}</p>
        <textarea
          v-model="noteText"
          rows="5"
          placeholder="Adicione uma nota..."
          class="w-full text-[13.5px] text-feitu-text dark:text-night-text bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.08)] dark:border-[rgba(255,255,255,.06)] rounded-[14px] p-3 outline-none resize-none placeholder:text-[#C4BDB0]"
        />
        <div class="flex gap-2 mt-3">
          <button
            @click="noteOpen = false"
            class="flex-1 py-[10px] rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] text-[#8C857B] dark:text-night-text/60 text-[13.5px] font-medium"
          >Cancelar</button>
          <button
            @click="salvarNota"
            class="flex-1 py-[10px] rounded-[12px] bg-feitu-blue-deep text-white text-[13.5px] font-semibold"
          >Salvar</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
