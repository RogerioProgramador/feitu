<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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
const noteOpen = ref(false)
const noteEditMode = ref(false)
const noteText = ref('')
const confirmDeleteOpen = ref(false)

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

watch(noteOpen, (open) => {
  if (open) {
    noteText.value = props.tarefa.descricao ?? ''
    noteEditMode.value = !props.tarefa.descricao
  }
})

async function salvarNota() {
  await store.atualizarDescricao(props.workspaceId, props.tarefa.id, noteText.value || null)
  noteOpen.value = false
  noteEditMode.value = false
}

const acaoPrincipal = computed(() =>
  props.tarefa.estado === 'RUNNING'
    ? () => store.pausarTimer(props.workspaceId, props.tarefa.id)
    : () => store.iniciarTimer(props.workspaceId, props.tarefa.id),
)

const cardClass = computed(() => {
  if (props.tarefa.estado === 'RUNNING')
    return 'bg-gradient-to-b from-[#FFF4EC] to-[#FFEFE3] border-[1.5px] border-[rgba(224,123,79,.32)] dark:from-[#2A2015] dark:to-[#271D10] dark:border-[rgba(224,123,79,.2)]'
  return 'bg-white dark:bg-night-card border border-[rgba(54,51,46,.07)] dark:border-[rgba(255,255,255,.06)] shadow-[0_1px_2px_rgba(58,55,51,.04)]'
})
</script>

<template>
  <div class="rounded-[18px] overflow-hidden" :class="cardClass">
    <div class="flex items-center gap-3 px-4 py-[14px]">

      <!-- Botão principal 56×56 -->
      <div class="flex-shrink-0 relative">
        <!-- Spinner arc (só RUNNING) -->
        <svg
          v-if="tarefa.estado === 'RUNNING'"
          class="absolute -top-1 -left-1 pointer-events-none animate-feitu-spin"
          width="64" height="64" viewBox="0 0 64 64"
        >
          <circle cx="32" cy="32" r="27" fill="none" stroke="#E07B4F" stroke-width="2.5"
            stroke-linecap="round" stroke-dasharray="42 128" opacity="0.55"/>
        </svg>

        <!-- Dashed arc (só PAUSED) -->
        <svg
          v-else-if="tarefa.estado === 'PAUSED'"
          class="absolute -top-1 -left-1 pointer-events-none"
          width="64" height="64" viewBox="0 0 64 64"
        >
          <circle cx="32" cy="32" r="27" fill="none" stroke="#CF6A36" stroke-width="2"
            stroke-linecap="round" stroke-dasharray="6 8" opacity="0.35"/>
        </svg>

        <button
          @click="acao(acaoPrincipal)"
          :disabled="carregando"
          class="w-14 h-14 flex items-center justify-center rounded-full transition-all disabled:opacity-40"
          :class="{
            'bg-[#E07B4F] text-white shadow-[0_4px_14px_rgba(224,123,79,.4)]': tarefa.estado === 'RUNNING',
            'bg-[#FFE3D1] text-[#CF6A36] dark:bg-[#3A2010] dark:text-[#F0935F]': tarefa.estado === 'PAUSED',
            'bg-[#EFEAE0] text-[#5E8BB6] dark:bg-[#30303F] dark:text-[#93B7DB]': tarefa.estado === 'IDLE',
          }"
          :title="tarefa.estado === 'RUNNING' ? 'Pausar' : tarefa.estado === 'PAUSED' ? 'Retomar' : 'Iniciar'"
        >
          <!-- Pause icon (RUNNING) -->
          <svg v-if="tarefa.estado === 'RUNNING'" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <rect x="6" y="4" width="4" height="16" rx="1"/><rect x="14" y="4" width="4" height="16" rx="1"/>
          </svg>
          <!-- Play icon (IDLE or PAUSED) -->
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path d="M8 5v14l11-7z"/>
          </svg>
        </button>
      </div>

      <!-- Nome + status -->
      <div class="flex-1 min-w-0">
        <input
          v-if="editando"
          v-model="nomeEdit"
          @blur="salvarNome"
          @keydown.enter="salvarNome"
          @keydown.esc="editando = false"
          class="w-full text-[14.5px] font-medium text-feitu-text dark:text-night-text outline-none border-b border-feitu-blue bg-transparent"
          v-focus
        />
        <span
          v-else
          @click="editando = true"
          class="text-[14.5px] font-medium text-feitu-text dark:text-night-text truncate block cursor-text select-none leading-snug"
        >{{ tarefa.nome }}</span>

        <!-- Status label -->
        <div class="flex items-center gap-[5px] mt-[3px]">
          <span
            v-if="tarefa.estado === 'RUNNING'"
            class="w-[7px] h-[7px] rounded-full bg-[#E07B4F] animate-feitu-breathe flex-shrink-0"
          />
          <span
            class="text-[11.5px]"
            :class="{
              'text-[#CF6A36]': tarefa.estado === 'RUNNING',
              'text-[#A98A74]': tarefa.estado === 'PAUSED',
              'text-[#A39C90]': tarefa.estado === 'IDLE',
            }"
          >
            <template v-if="tarefa.estado === 'RUNNING'">Em andamento</template>
            <template v-else-if="tarefa.estado === 'PAUSED'">Pausada</template>
            <template v-else>Toque para iniciar</template>
          </span>
        </div>
      </div>

      <!-- Timer -->
      <span
        class="text-[20px] font-medium tabular-nums flex-shrink-0"
        :class="{
          'text-[#CF6A36]': tarefa.estado === 'RUNNING',
          'text-[#8C857B]': tarefa.estado === 'PAUSED',
          'text-[#C4BDB0] dark:text-night-text/25': tarefa.estado === 'IDLE',
        }"
      >{{ formatarTempo(segundos) }}</span>
    </div>

    <!-- Barra de ações -->
    <div class="flex items-center gap-[6px] px-4 pb-[12px]">
      <!-- Nota -->
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

      <!-- Concluir (só quando ativo) -->
      <button
        v-if="tarefa.estado !== 'IDLE'"
        @click="acao(() => store.pararTimer(workspaceId, tarefa.id))"
        :disabled="carregando"
        class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] border border-[rgba(47,125,99,.25)] bg-[#EAF6F0] text-[#2F7D63] dark:bg-[rgba(47,125,99,.12)] dark:border-[rgba(47,125,99,.2)] transition disabled:opacity-40"
        title="Concluir"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
      </button>

      <!-- Deletar -->
      <button
        @click="confirmDeleteOpen = true"
        :disabled="carregando"
        class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-white dark:bg-night-surface text-[#B0A89B] hover:text-red-400 hover:border-red-200 transition disabled:opacity-40"
        title="Deletar"
      >
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>
  </div>

  <!-- Note bottom sheet -->
  <Teleport to="body">
    <div v-if="noteOpen" class="fixed inset-0 z-30 flex items-end" @click.self="noteOpen = false">
      <div class="absolute inset-0 bg-black/25 dark:bg-black/50" @click="noteOpen = false"/>
      <div class="relative w-full max-w-lg mx-auto bg-white dark:bg-night-surface rounded-t-[28px] p-5 pb-8 shadow-xl">
        <div class="w-10 h-1 rounded-full bg-[#DDD8CE] mx-auto mb-4"/>
        <div class="flex items-center justify-between mb-3">
          <p class="text-[13px] font-semibold text-feitu-text dark:text-night-text">Nota — {{ tarefa.nome }}</p>
          <button
            v-if="!noteEditMode && noteText"
            @click="noteEditMode = true"
            class="text-[12px] font-medium text-[#8A5FC0] px-3 py-1 rounded-[8px] bg-[#F3ECFB] dark:bg-[rgba(138,95,192,.15)]"
          >Editar</button>
        </div>
        <!-- Preview mode -->
        <div
          v-if="!noteEditMode && noteText"
          class="w-full text-[13.5px] text-feitu-text dark:text-night-text bg-[#F7F4EE] dark:bg-night-card border border-[rgba(54,51,46,.08)] dark:border-[rgba(255,255,255,.06)] rounded-[14px] p-3 min-h-[80px] whitespace-pre-wrap break-words"
        >{{ noteText }}</div>
        <!-- Edit mode -->
        <textarea
          v-else
          v-model="noteText"
          rows="5"
          placeholder="Adicione uma nota ou descrição..."
          class="w-full text-[13.5px] text-feitu-text dark:text-night-text bg-[#F7F4EE] dark:bg-night-card border border-[rgba(54,51,46,.08)] dark:border-[rgba(255,255,255,.06)] rounded-[14px] p-3 outline-none resize-none placeholder:text-[#C4BDB0]"
        />
        <div class="flex gap-2 mt-3">
          <button
            @click="noteOpen = false; noteEditMode = false"
            class="flex-1 py-[10px] rounded-[12px] border border-[rgba(54,51,46,.1)] text-[#8C857B] text-[13.5px] font-medium"
          >Cancelar</button>
          <button
            v-if="noteEditMode || !noteText"
            @click="salvarNota"
            class="flex-1 py-[10px] rounded-[12px] bg-feitu-blue-deep text-white text-[13.5px] font-semibold"
          >Salvar</button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Delete confirmation sheet -->
  <Teleport to="body">
    <div v-if="confirmDeleteOpen" class="fixed inset-0 z-30 flex items-end">
      <div class="absolute inset-0 bg-black/25 dark:bg-black/50" @click="confirmDeleteOpen = false"/>
      <div class="relative w-full max-w-lg mx-auto bg-white dark:bg-night-surface rounded-t-[28px] p-5 pb-8 shadow-xl">
        <div class="w-10 h-1 rounded-full bg-[#DDD8CE] mx-auto mb-4"/>
        <p class="text-[15px] font-semibold text-feitu-text dark:text-night-text mb-1">Excluir tarefa</p>
        <p class="text-[13px] text-[#8C857B] mb-5">Tem certeza que deseja excluir "{{ tarefa.nome }}"?</p>
        <div class="flex gap-2">
          <button
            @click="confirmDeleteOpen = false"
            class="flex-1 py-[10px] rounded-[12px] border border-[rgba(54,51,46,.1)] text-[#8C857B] text-[13.5px] font-medium"
          >Cancelar</button>
          <button
            @click="acao(() => store.deletar(workspaceId, tarefa.id)); confirmDeleteOpen = false"
            class="flex-1 py-[10px] rounded-[12px] bg-red-500 text-white text-[13.5px] font-semibold"
          >Excluir</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
