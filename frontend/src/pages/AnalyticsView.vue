<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { analyticsApi } from '../api/analyticsApi'
import { formatarTempo } from '../utils/formatarTempo'
import { formatarDataBR, hojeISO } from '../utils/formatarData'
import { useSettingsStore } from '../stores/settingsStore'
import type { DailySummary } from '../types'

const settings = useSettingsStore()
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

const totalFormatado = computed(() =>
  resumo.value ? formatarTempo(resumo.value.totalSegundos) : '00:00:00',
)

const barras = computed(() => {
  if (!resumo.value?.porcWorkspace.length) return []
  const max = resumo.value.porcWorkspace[0].segundos
  return resumo.value.porcWorkspace.map((w) => ({
    ...w,
    pct: max > 0 ? Math.round((w.segundos / max) * 100) : 0,
  }))
})

function horaMinuto(iso: string) {
  const tz = settings.fusoHorario === 'BR' ? 'America/Sao_Paulo' : 'UTC'
  return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: tz })
}

function anteriorDia() {
  const d = new Date(data.value + 'T12:00:00')
  d.setDate(d.getDate() - 1)
  data.value = d.toISOString().slice(0, 10)
  carregar()
}

function proximoDia() {
  const hoje = hojeISO()
  if (data.value >= hoje) return
  const d = new Date(data.value + 'T12:00:00')
  d.setDate(d.getDate() + 1)
  data.value = d.toISOString().slice(0, 10)
  carregar()
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-lg mx-auto">

    <!-- Header -->
    <header class="flex items-center gap-3 px-4 pt-5 pb-4">
      <RouterLink
        to="/workspaces"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-white dark:bg-night-surface text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </RouterLink>
      <h1 class="text-[17px] font-semibold text-feitu-text dark:text-night-text flex-1">Resumo do dia</h1>
    </header>

    <!-- Nav de data -->
    <div class="flex items-center justify-center gap-2 px-4 pb-4">
      <div class="flex items-center gap-1 bg-white dark:bg-night-surface rounded-[13px] px-[9px] py-[7px] shadow-[0_1px_3px_rgba(54,51,46,.06)]">
        <button
          @click="anteriorDia"
          class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] bg-[#F4F1EA] dark:bg-night-card text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>
        <span class="px-3 text-[13.5px] font-semibold text-feitu-text dark:text-night-text min-w-[110px] text-center">{{ formatarDataBR(data) }}</span>
        <button
          @click="proximoDia"
          :disabled="data >= hojeISO()"
          class="w-[30px] h-[30px] flex items-center justify-center rounded-[9px] bg-[#F4F1EA] dark:bg-night-card text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition disabled:opacity-30"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
    </div>

    <div v-if="carregando" class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">Carregando...</div>
    <div v-else-if="erro" class="p-8 text-center text-red-400 text-sm">{{ erro }}</div>

    <div v-else-if="resumo" class="flex-1 overflow-y-auto px-4 pb-8 space-y-4">

      <!-- Card total — gradiente lavender -->
      <div class="bg-gradient-to-br from-[#EFE3FB] to-[#F6EFFC] dark:from-[#1E1828] dark:to-[#241C2E] border border-[rgba(138,95,192,.16)] dark:border-[rgba(138,95,192,.12)] rounded-[22px] p-[22px]">
        <p class="text-[12px] uppercase tracking-[.04em] text-[#8A5FC0] dark:text-[#B48FE0] font-semibold mb-1">Total do dia</p>
        <p class="text-[46px] font-semibold text-[#5B3E86] dark:text-[#C9A5F5] tabular-nums leading-none">{{ totalFormatado }}</p>
      </div>

      <!-- Barras por workspace -->
      <div v-if="barras.length" class="bg-white dark:bg-night-surface rounded-[18px] p-5 space-y-[14px]">
        <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em]">Por workspace</p>
        <div v-for="b in barras" :key="b.workspaceId" class="space-y-[6px]">
          <div class="flex justify-between text-[13px]">
            <span class="text-feitu-text dark:text-night-text font-medium">{{ b.nome }}</span>
            <span class="tabular-nums text-[#8C857B]">{{ formatarTempo(b.segundos) }}</span>
          </div>
          <div class="h-[9px] bg-[#F1EDE4] dark:bg-night-card rounded-[6px] overflow-hidden">
            <div
              class="h-full rounded-[6px] transition-all"
              :style="{ width: b.pct + '%', backgroundColor: b.cor ?? '#A7C7E7' }"
            />
          </div>
        </div>
      </div>

      <!-- Tarefa mais longa -->
      <div v-if="resumo.tarefaMaisLonga" class="bg-white dark:bg-night-surface rounded-[18px] p-5">
        <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em] mb-3">Tarefa mais longa</p>
        <div class="flex items-center gap-3">
          <div class="w-11 h-11 rounded-[14px] bg-[#FFF1E8] dark:bg-[rgba(224,123,79,.12)] flex items-center justify-center flex-shrink-0">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#E07B4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-[14px] font-medium text-feitu-text dark:text-night-text truncate">{{ resumo.tarefaMaisLonga.nome }}</p>
            <p class="text-[18px] font-semibold text-[#CF6A36] tabular-nums leading-tight">{{ formatarTempo(resumo.tarefaMaisLonga.segundos) }}</p>
          </div>
        </div>
      </div>

      <!-- Timeline -->
      <div v-if="resumo.timeline.length" class="bg-white dark:bg-night-surface rounded-[18px] p-5">
        <p class="text-[11.5px] font-semibold text-[#9A9388] uppercase tracking-[.05em] mb-4">Timeline</p>
        <div class="space-y-[10px]">
          <div v-for="(item, idx) in resumo.timeline" :key="item.inicio" class="flex items-start gap-3">
            <!-- Pipe colorida -->
            <div class="flex flex-col items-center self-stretch flex-shrink-0">
              <div
                class="w-[3px] flex-1 rounded-[3px] min-h-[36px]"
                :style="{ backgroundColor: (idx > 0 && resumo.timeline[idx - 1].tarefaId === item.tarefaId) ? '#D8E4F1' : (item.workspaceCor ?? '#A7C7E7') }"
              />
            </div>
            <!-- Horário + nome -->
            <div class="flex-1 min-w-0 pb-1">
              <p
                class="text-[13px] truncate leading-snug"
                :class="(idx > 0 && resumo.timeline[idx - 1].tarefaId === item.tarefaId)
                  ? 'text-[#A39C90] italic'
                  : 'text-feitu-text dark:text-night-text font-medium'"
              >{{ item.tarefaNome }}</p>
              <p class="text-[11.5px] text-[#A39C90] tabular-nums mt-[1px]">
                {{ horaMinuto(item.inicio) }} – {{ horaMinuto(item.fim) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div
        v-if="!barras.length && !resumo.timeline.length"
        class="flex flex-col items-center gap-4 py-12"
      >
        <div class="w-16 h-16 rounded-[20px] bg-[#EFEAE0] dark:bg-[rgba(255,255,255,.05)] flex items-center justify-center">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#B0A89B" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <p class="text-[14px] text-[#A39C90] text-center">Nenhum tempo registrado neste dia</p>
      </div>

    </div>
  </div>
</template>
