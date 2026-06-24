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
const addingInline = ref(false)
const inlineNome = ref('')

const recorrentes = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter((t) => t.tipo === 'RECORRENTE'),
)
const pontuais = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter((t) => t.tipo === 'PONTUAL'),
)

const recCount = computed(() => {
  const done = recorrentes.value.filter((t) => t.concluida).length
  return `${done} de ${recorrentes.value.length} feitas`
})
const ponCount = computed(() => {
  const done = pontuais.value.filter((t) => t.concluida).length
  return `${done} de ${pontuais.value.length} feitas`
})

watch(
  () => [props.workspaceId, props.date] as const,
  ([id, date]) => { if (id) store.carregar(id, date) },
  { immediate: true },
)

async function commitInline() {
  const nome = inlineNome.value.trim()
  if (!nome) {
    addingInline.value = false
    inlineNome.value = ''
    return
  }
  await store.criar(props.workspaceId, { nome, tipo: 'PONTUAL' }, props.date)
  inlineNome.value = ''
  addingInline.value = false
}
</script>

<template>
  <div class="flex flex-col gap-0 px-[18px] pb-7">

    <!-- Seção Recorrentes -->
    <div v-if="recorrentes.length > 0">
      <button
        @click="recorrentesExpand = !recorrentesExpand"
        class="flex items-center gap-[9px] w-full bg-transparent border-none px-[4px] py-[8px] cursor-pointer"
        style="padding-top: 8px; padding-bottom: 4px;"
      >
        <span
          class="flex items-center transition-transform duration-200"
          :style="{ transform: recorrentesExpand ? 'rotate(0deg)' : 'rotate(-90deg)' }"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#8C857B" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9l6 6 6-6"/>
          </svg>
        </span>
        <span class="text-[15px] font-semibold text-feitu-text dark:text-night-text leading-none">Recorrentes</span>
        <span class="text-[12.5px] font-medium text-[#A49C90] dark:text-[#7C7888] whitespace-nowrap leading-none">{{ recCount }}</span>
      </button>

      <div v-if="recorrentesExpand" class="flex flex-col gap-[8px] mt-[8px] mb-[4px] animate-feitu-fade">
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
    <div>
      <button
        v-if="pontuais.length > 0"
        @click="pontuaisExpand = !pontuaisExpand"
        class="flex items-center gap-[9px] w-full bg-transparent border-none px-[4px] cursor-pointer"
        :style="{ paddingTop: recorrentes.length > 0 ? '14px' : '8px', paddingBottom: '4px' }"
      >
        <span
          class="flex items-center transition-transform duration-200"
          :style="{ transform: pontuaisExpand ? 'rotate(0deg)' : 'rotate(-90deg)' }"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#8C857B" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9l6 6 6-6"/>
          </svg>
        </span>
        <span class="text-[15px] font-semibold text-feitu-text dark:text-night-text leading-none">Pontuais</span>
        <span class="text-[12.5px] font-medium text-[#A49C90] dark:text-[#7C7888] whitespace-nowrap leading-none">{{ ponCount }}</span>
      </button>

      <div v-if="pontuaisExpand" class="flex flex-col gap-[8px] mt-[8px] mb-[4px] animate-feitu-fade">
        <TaskItem
          v-for="t in pontuais"
          :key="t.id"
          :tarefa="t"
          :workspace-id="workspaceId"
          :date="date"
        />
      </div>
    </div>

    <!-- Add row -->
    <div class="mt-[10px]">
      <!-- Inline quick-add input -->
      <div
        v-if="addingInline"
        class="flex items-center gap-[10px] bg-feitu-surface dark:bg-night-card border border-feitu-blue-deep/40 rounded-[16px] px-[13px] py-[11px] animate-feitu-fade"
      >
        <span class="flex-shrink-0 w-7 h-7 rounded-full border-[2px] border-[rgba(54,51,46,.18)] dark:border-[rgba(255,255,255,.15)]"></span>
        <input
          v-model="inlineNome"
          v-focus
          placeholder="Nome da tarefa..."
          class="flex-1 min-w-0 bg-transparent outline-none text-[15px] font-medium text-feitu-text dark:text-night-text placeholder:text-[#C4BDB0] dark:placeholder:text-night-text/30"
          @keydown.enter="commitInline"
          @keydown.esc="addingInline = false; inlineNome = ''"
        />
        <button
          @click="commitInline"
          class="flex-shrink-0 w-[30px] h-[30px] rounded-[9px] bg-feitu-blue-deep flex items-center justify-center"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M5 12.5l4.5 4.5L19 7"/>
          </svg>
        </button>
      </div>

      <!-- Botões de adicionar (padrão) -->
      <div v-else class="flex items-center gap-[8px]">
        <button
          @click="addingInline = true"
          class="flex-1 flex items-center gap-[9px] px-[15px] py-[13px] rounded-[16px] border-[1.5px] border-dashed border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.16)] bg-transparent text-[#8C857B] dark:text-night-text/50 cursor-pointer"
          style="font: 500 14.5px 'Space Grotesk';"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          Nova tarefa
        </button>
        <button
          @click="sheetOpen = true"
          title="Criação avançada"
          class="flex-shrink-0 w-[52px] self-stretch flex items-center justify-center rounded-[16px] border-[1.5px] border-dashed border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.16)] bg-transparent text-[#8C857B] dark:text-night-text/50 cursor-pointer"
        >
          <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M4 8h11M4 16h7"/>
            <circle cx="18" cy="8" r="2.6" fill="transparent" stroke="currentColor" stroke-width="2"/>
            <circle cx="14" cy="16" r="2.6" fill="transparent" stroke="currentColor" stroke-width="2"/>
          </svg>
        </button>
      </div>
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
