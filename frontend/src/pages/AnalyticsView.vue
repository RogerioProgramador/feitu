<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { analyticsApi } from '../api/analyticsApi'
import { formatarDataBR, hojeISO } from '../utils/formatarData'
import type { DailySummary } from '../types'

const data = ref(hojeISO())
const resumo = ref<DailySummary | null>(null)
const carregando = ref(false)
const erro = ref('')

async function carregar() {
  carregando.value = true
  erro.value = ''
  try {
    resumo.value = await analyticsApi.diario(data.value)
  } catch {
    erro.value = 'Erro ao carregar resumo'
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)

const estado = computed<'vazio' | 'parcial' | 'tudo'>(() => {
  if (!resumo.value || resumo.value.totalTarefas === 0) return 'vazio'
  if (resumo.value.concluidas === resumo.value.totalTarefas) return 'tudo'
  return 'parcial'
})

const pctConcluidas = computed(() => {
  if (!resumo.value || resumo.value.totalTarefas === 0) return 0
  return Math.round((resumo.value.concluidas / resumo.value.totalTarefas) * 100)
})

function anteriorDia() {
  const d = new Date(data.value + 'T12:00:00')
  d.setDate(d.getDate() - 1)
  data.value = d.toISOString().slice(0, 10)
  carregar()
}

function proximoDia() {
  if (data.value >= hojeISO()) return
  const d = new Date(data.value + 'T12:00:00')
  d.setDate(d.getDate() + 1)
  data.value = d.toISOString().slice(0, 10)
  carregar()
}

function diaSemana(iso: string) {
  const d = new Date(iso + 'T12:00:00')
  return d.toLocaleDateString('pt-BR', { weekday: 'short' }).replace('.', '')
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header -->
    <header class="flex items-center gap-3 px-4 pt-5 pb-4">
      <RouterLink
        to="/workspaces"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-white dark:bg-night-surface text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </RouterLink>
      <h1 class="text-[17px] font-semibold text-feitu-text dark:text-night-text flex-1">Resumo</h1>
    </header>

    <!-- Nav de data -->
    <div class="flex items-center justify-center gap-2 px-4 pb-4">
      <div class="flex items-center gap-1 bg-white dark:bg-night-surface rounded-[13px] px-[9px] py-[7px] shadow-[0_1px_3px_rgba(54,51,46,.06)]">
        <button
          @click="anteriorDia"
          class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] bg-[#F4F1EA] dark:bg-night-card text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>
        <span class="px-3 text-[13.5px] font-semibold text-feitu-text dark:text-night-text min-w-[130px] text-center capitalize">
          {{ diaSemana(data) }}, {{ formatarDataBR(data) }}
        </span>
        <button
          @click="proximoDia"
          :disabled="data >= hojeISO()"
          class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] bg-[#F4F1EA] dark:bg-night-card text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition disabled:opacity-30"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
    </div>

    <div v-if="carregando" class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">Carregando...</div>
    <div v-else-if="erro" class="p-8 text-center text-red-400 text-sm">{{ erro }}</div>

    <div v-else-if="resumo" class="flex-1 overflow-y-auto px-4 pb-8">

      <!-- Estado C: sem tarefas -->
      <div v-if="estado === 'vazio'" class="flex flex-col items-center gap-4 py-16">
        <div class="w-16 h-16 rounded-[20px] bg-[#EFEAE0] dark:bg-[rgba(255,255,255,.05)] flex items-center justify-center">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#B0A89B" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <p class="text-[14px] text-[#A39C90] text-center">Nenhuma tarefa neste dia</p>
      </div>

      <!-- Estado B: tudo feito -->
      <div v-else-if="estado === 'tudo'" class="space-y-5">
        <div class="flex flex-col items-center gap-3 py-8">
          <div class="w-24 h-24 rounded-full bg-feitu-teal flex items-center justify-center">
            <svg class="animate-feitu-spring" width="48" height="48" viewBox="0 0 24 24"
              fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <p class="text-[22px] font-medium text-feitu-text dark:text-night-text">Tudo feito hoje.</p>
          <p class="text-[14px] text-[#8C857B] dark:text-night-text/50">{{ resumo.concluidas }} tarefas concluídas</p>
        </div>

        <!-- Lista completa -->
        <div v-if="resumo.recorrentes.length" class="space-y-[6px]">
          <p class="text-[11px] font-semibold text-feitu-blue-deep dark:text-feitu-blue tracking-widest uppercase mb-2">Recorrentes</p>
          <div v-for="t in resumo.recorrentes" :key="t.id" class="flex items-center gap-3 py-2">
            <div class="w-6 h-6 rounded-full bg-feitu-teal flex items-center justify-center flex-shrink-0">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="text-[13.5px] line-through text-[#A49C90] dark:text-night-text/35">{{ t.nome }}</span>
          </div>
        </div>
        <div v-if="resumo.pontuais.length" class="space-y-[6px]">
          <p class="text-[11px] font-semibold text-feitu-lavender-deep dark:text-feitu-lavender tracking-widest uppercase mb-2">Pontuais</p>
          <div v-for="t in resumo.pontuais" :key="t.id" class="flex items-center gap-3 py-2">
            <div class="w-6 h-6 rounded-full bg-feitu-teal flex items-center justify-center flex-shrink-0">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span class="text-[13.5px] line-through text-[#A49C90] dark:text-night-text/35">{{ t.nome }}</span>
          </div>
        </div>
      </div>

      <!-- Estado A: parcial -->
      <div v-else class="space-y-5">

        <!-- Barra de progresso -->
        <div class="bg-white dark:bg-night-surface rounded-[18px] p-5">
          <div class="flex justify-between items-baseline mb-2">
            <p class="text-[13px] font-medium text-feitu-text dark:text-night-text">
              {{ resumo.concluidas }} de {{ resumo.totalTarefas }} concluídas
            </p>
            <span class="text-[13px] font-semibold text-feitu-teal-deep dark:text-feitu-teal">{{ pctConcluidas }}%</span>
          </div>
          <div class="h-[10px] bg-[#F1EDE4] dark:bg-night-card rounded-full overflow-hidden">
            <div
              class="h-full bg-feitu-teal rounded-full transition-all duration-500"
              :style="{ width: pctConcluidas + '%' }"
            />
          </div>
        </div>

        <!-- Recorrentes -->
        <div v-if="resumo.recorrentes.length" class="space-y-[6px]">
          <p class="text-[11px] font-semibold text-feitu-blue-deep dark:text-feitu-blue tracking-widest uppercase mb-2">Recorrentes</p>
          <div v-for="t in resumo.recorrentes" :key="t.id" class="flex items-center gap-3 py-2">
            <div
              class="w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 transition-all"
              :class="t.concluida
                ? 'bg-feitu-teal'
                : 'border-[1.5px] border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.2)]'"
            >
              <svg v-if="t.concluida" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span
              class="text-[13.5px] transition-colors"
              :class="t.concluida
                ? 'line-through text-[#A49C90] dark:text-night-text/35'
                : 'text-feitu-text dark:text-night-text'"
            >{{ t.nome }}</span>
          </div>
        </div>

        <!-- Pontuais -->
        <div v-if="resumo.pontuais.length" class="space-y-[6px]">
          <p class="text-[11px] font-semibold text-feitu-lavender-deep dark:text-feitu-lavender tracking-widest uppercase mb-2">Pontuais</p>
          <div v-for="t in resumo.pontuais" :key="t.id" class="flex items-center gap-3 py-2">
            <div
              class="w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 transition-all"
              :class="t.concluida
                ? 'bg-feitu-teal'
                : 'border-[1.5px] border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.2)]'"
            >
              <svg v-if="t.concluida" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <span
              class="text-[13.5px] transition-colors"
              :class="t.concluida
                ? 'line-through text-[#A49C90] dark:text-night-text/35'
                : 'text-feitu-text dark:text-night-text'"
            >{{ t.nome }}</span>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>
