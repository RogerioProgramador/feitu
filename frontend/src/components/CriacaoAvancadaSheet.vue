<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import type { TipoTarefa } from '../types'

const props = defineProps<{ workspaceId: string; date?: string }>()
const emit = defineEmits<{
  (e: 'fechar'): void
  (e: 'criada'): void
}>()

const store = useTarefaStore()
const nome = ref('')
const descricao = ref('')
const tipo = ref<TipoTarefa>('PONTUAL')
const diasSelecionados = ref<string[]>([])
const horarioAtivo = ref(false)
const hora = ref('07')
const minuto = ref('00')
const salvando = ref(false)

const DIAS = ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB', 'DOM'] as const
const DIAS_API = ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SAB', 'DOM'] as const

const todosDias = computed(
  () => diasSelecionados.value.length === 7,
)

const podeSubmeter = computed(() => {
  if (!nome.value.trim()) return false
  if (tipo.value === 'RECORRENTE' && diasSelecionados.value.length === 0) return false
  return true
})

function toggleDia(apiDia: string) {
  const idx = diasSelecionados.value.indexOf(apiDia)
  if (idx >= 0) {
    diasSelecionados.value.splice(idx, 1)
  } else {
    diasSelecionados.value.push(apiDia)
  }
}

function toggleTodosDias() {
  if (todosDias.value) {
    diasSelecionados.value = []
  } else {
    diasSelecionados.value = [...DIAS_API]
  }
}

async function criar() {
  if (!podeSubmeter.value || salvando.value) return
  salvando.value = true
  try {
    const horario = horarioAtivo.value && tipo.value === 'RECORRENTE'
      ? `${hora.value.padStart(2, '0')}:${minuto.value.padStart(2, '0')}`
      : null

    await store.criar(
      props.workspaceId,
      {
        nome: nome.value.trim() || 'Nova tarefa',
        descricao: descricao.value || null,
        tipo: tipo.value,
        diasSemana: tipo.value === 'RECORRENTE' ? diasSelecionados.value : undefined,
        horario,
      },
      props.date,
    )
    emit('criada')
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-40 flex items-end">
      <!-- Fundo escurecido -->
      <div
        class="absolute inset-0 bg-[rgba(40,37,33,.4)] dark:bg-black/60 animate-feitu-dim"
        @click="emit('fechar')"
      />

      <!-- Sheet -->
      <div class="relative w-full max-w-lg mx-auto bg-white dark:bg-night-surface rounded-t-[28px] p-5 pb-10 shadow-xl animate-feitu-sheet">
        <!-- Handle -->
        <div class="w-10 h-1 rounded-full bg-[#DDD8CE] dark:bg-night-card mx-auto mb-5"/>

        <!-- Nome -->
        <label class="block text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 mb-1">
          NOME <span class="text-feitu-peach-deep">*</span>
        </label>
        <input
          v-model="nome"
          placeholder="Nome da tarefa"
          class="w-full text-[15px] font-medium text-feitu-text dark:text-night-text bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.07)] rounded-[14px] px-4 py-3 outline-none placeholder:text-[#C4BDB0] mb-4"
          autofocus
        />

        <!-- Descrição -->
        <label class="block text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 mb-1">DESCRIÇÃO</label>
        <textarea
          v-model="descricao"
          placeholder="Opcional"
          rows="2"
          class="w-full text-[13.5px] text-feitu-text dark:text-night-text bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.07)] rounded-[14px] px-4 py-3 outline-none resize-none placeholder:text-[#C4BDB0] mb-4"
        />

        <!-- Toggle Pontual / Recorrente -->
        <label class="block text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 mb-2">TIPO</label>
        <div class="flex bg-feitu-surface dark:bg-night-card rounded-[12px] p-[3px] mb-4">
          <button
            v-for="t in (['PONTUAL', 'RECORRENTE'] as TipoTarefa[])"
            :key="t"
            @click="tipo = t"
            class="flex-1 py-2 rounded-[10px] text-[13px] font-medium transition-all"
            :class="tipo === t
              ? 'bg-white dark:bg-night-surface shadow-sm text-feitu-blue-deep dark:text-feitu-blue'
              : 'text-[#8C857B] dark:text-night-text/50'"
          >{{ t === 'PONTUAL' ? 'Pontual' : 'Recorrente' }}</button>
        </div>

        <!-- Opções recorrente -->
        <div v-if="tipo === 'RECORRENTE'" class="animate-feitu-fade mb-4">
          <!-- Dias da semana -->
          <label class="block text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 mb-2">DIAS DA SEMANA</label>
          <div class="flex flex-wrap gap-[6px] mb-3">
            <button
              v-for="(dia, i) in DIAS"
              :key="dia"
              @click="toggleDia(DIAS_API[i])"
              class="px-3 py-1.5 rounded-full text-[12px] font-semibold transition-all"
              :class="diasSelecionados.includes(DIAS_API[i])
                ? 'bg-feitu-blue-deep text-white'
                : 'bg-feitu-surface dark:bg-night-card text-[#6E6A62] dark:text-night-text/50 border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)]'"
            >{{ dia }}</button>

            <!-- Todos os dias -->
            <button
              @click="toggleTodosDias"
              class="px-3 py-1.5 rounded-full text-[12px] font-semibold transition-all"
              :class="todosDias
                ? 'bg-feitu-teal-deep text-white'
                : 'bg-feitu-surface dark:bg-night-card text-[#6E6A62] dark:text-night-text/50 border border-dashed border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.15)]'"
            >Todos</button>
          </div>

          <!-- Toggle horário -->
          <div class="flex items-center gap-3 mb-3">
            <span class="text-[13px] text-[#6E6A62] dark:text-night-text/60">Horário</span>
            <button
              @click="horarioAtivo = !horarioAtivo"
              class="relative w-11 h-6 rounded-full transition-colors"
              :class="horarioAtivo ? 'bg-feitu-blue-deep' : 'bg-[rgba(54,51,46,.18)] dark:bg-[rgba(255,255,255,.1)]'"
            >
              <span
                class="absolute top-[3px] w-[18px] h-[18px] rounded-full bg-white shadow transition-all"
                :class="horarioAtivo ? 'left-[26px]' : 'left-[3px]'"
              />
            </button>

            <!-- Inputs hora quando ativo -->
            <div v-if="horarioAtivo" class="flex items-center gap-1 animate-feitu-fade">
              <input
                v-model="hora"
                type="number" min="0" max="23"
                class="w-12 text-center text-[14px] font-semibold text-feitu-text dark:text-night-text bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[8px] py-1 outline-none"
              />
              <span class="text-[14px] font-semibold text-[#8C857B]">:</span>
              <input
                v-model="minuto"
                type="number" min="0" max="59"
                class="w-12 text-center text-[14px] font-semibold text-feitu-text dark:text-night-text bg-feitu-surface dark:bg-night-card border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[8px] py-1 outline-none"
              />
            </div>
          </div>
        </div>

        <!-- Botão criar -->
        <button
          @click="criar"
          :disabled="!podeSubmeter || salvando"
          class="w-full py-[14px] rounded-[16px] text-[15px] font-semibold text-white transition-all"
          :class="podeSubmeter
            ? 'bg-feitu-blue-deep shadow-[0_4px_16px_rgba(94,139,182,.4)]'
            : 'bg-feitu-blue-deep/30 cursor-not-allowed'"
        >{{ salvando ? 'Criando...' : 'Criar Tarefa' }}</button>
      </div>
    </div>
  </Teleport>
</template>
