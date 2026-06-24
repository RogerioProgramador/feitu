<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { analyticsApi } from '../api/analyticsApi'
import { hojeISO } from '../utils/formatarData'
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

function navLabel(iso: string): string {
  const d = new Date(iso + 'T12:00:00')
  const dia = d.toLocaleDateString('pt-BR', { weekday: 'short' }).replace('.', '')
  const diaNum = String(d.getDate()).padStart(2, '0')
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const cap = dia.charAt(0).toUpperCase() + dia.slice(1)
  return `${cap}, ${diaNum}/${mes}`
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header com nav de data integrado -->
    <div class="flex items-center justify-between px-[18px] pt-[6px] pb-[8px]">
      <RouterLink
        to="/workspaces"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-[#F7F4EE] dark:bg-night-surface text-[#5E5A52] dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M15 5l-7 7 7 7"/>
        </svg>
      </RouterLink>

      <span class="text-[17px] font-semibold text-feitu-text dark:text-night-text leading-none">Resumo</span>

      <!-- Nav de data: [< Ter, 24/06 >] -->
      <div class="flex items-center gap-[4px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] rounded-[11px] px-[8px] py-[6px]">
        <button
          @click="anteriorDia"
          class="w-[22px] h-[22px] flex items-center justify-center text-[#8C857B] dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text transition"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 5l-7 7 7 7"/>
          </svg>
        </button>
        <span class="text-[13px] font-medium text-feitu-text dark:text-night-text leading-none px-[2px]">{{ navLabel(data) }}</span>
        <button
          @click="proximoDia"
          :disabled="data >= hojeISO()"
          class="w-[22px] h-[22px] flex items-center justify-center text-[#8C857B] dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text transition disabled:opacity-25"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </div>

    <div v-if="carregando" class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">Carregando...</div>
    <div v-else-if="erro" class="p-8 text-center text-red-400 text-sm">{{ erro }}</div>

    <div v-else-if="resumo" class="flex-1 overflow-y-auto px-[22px] pb-7 pt-[14px]">

      <!-- Estado C: sem tarefas -->
      <div v-if="estado === 'vazio'" class="flex flex-col items-center gap-4 py-16">
        <div class="w-[104px] h-[104px] rounded-[28px] bg-[#F7F4EE] dark:bg-night-surface border border-[rgba(54,51,46,.07)] flex items-center justify-center shadow-[0_12px_30px_-16px_rgba(54,51,46,.3)]">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <rect x="3.5" y="6" width="17" height="13" rx="3" stroke="#A7C7E7" stroke-width="2"/>
            <path d="M3.5 10h17" stroke="#A7C7E7" stroke-width="2"/>
            <path d="M9 4.5h6" stroke="#FFDAC1" stroke-width="2.4" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="text-[14px] text-[#A39C90] text-center">Nenhuma tarefa neste dia</p>
      </div>

      <!-- Estado B: tudo feito -->
      <div v-else-if="estado === 'tudo'">
        <div class="flex flex-col items-center pb-[22px] pt-[26px]">
          <div
            class="w-[96px] h-[96px] rounded-full bg-feitu-teal flex items-center justify-center"
            style="box-shadow: 0 12px 30px -10px rgba(47,125,99,.4);"
          >
            <svg
              class="animate-feitu-spring"
              width="48" height="48" viewBox="0 0 24 24"
              fill="none" stroke="#2F7D63" stroke-width="2.6"
              stroke-linecap="round" stroke-linejoin="round"
            >
              <path d="M5 12.5l4.5 4.5L19 7"/>
            </svg>
          </div>
          <p class="text-[22px] font-medium text-feitu-text dark:text-night-text mt-[18px]">Tudo feito hoje.</p>
          <p class="text-[14px] text-[#8C857B] dark:text-night-text/50 mt-[6px]">{{ resumo.concluidas }} tarefas concluídas</p>
        </div>

        <div v-if="resumo.recorrentes.length" class="mb-[18px]">
          <p class="text-[13px] font-semibold text-feitu-blue-deep dark:text-feitu-blue tracking-[.04em] uppercase mb-[8px] mt-[6px]">Recorrentes</p>
          <div class="flex flex-col">
            <div v-for="t in resumo.recorrentes" :key="t.id" class="flex items-center gap-[13px] py-[8px]">
              <span class="flex-shrink-0 w-[24px] h-[24px] rounded-full bg-feitu-teal flex items-center justify-center">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><path d="M5 12.5l4.5 4.5L19 7"/></svg>
              </span>
              <span class="flex-1 text-[14.5px] font-medium text-[#6E6A62] dark:text-night-text/60">{{ t.nome }}</span>
            </div>
          </div>
        </div>

        <div v-if="resumo.pontuais.length">
          <p class="text-[13px] font-semibold text-[#8A5FC0] dark:text-feitu-lavender tracking-[.04em] uppercase mb-[8px] mt-[14px]">Pontuais</p>
          <div class="flex flex-col">
            <div v-for="t in resumo.pontuais" :key="t.id" class="flex items-center gap-[13px] py-[8px]">
              <span class="flex-shrink-0 w-[24px] h-[24px] rounded-full bg-feitu-teal flex items-center justify-center">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round"><path d="M5 12.5l4.5 4.5L19 7"/></svg>
              </span>
              <span class="flex-1 text-[14.5px] font-medium text-[#6E6A62] dark:text-night-text/60">{{ t.nome }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Estado A: parcial -->
      <div v-else>
        <!-- Barra de progresso -->
        <div class="mb-[20px]">
          <div class="flex justify-between items-baseline mb-[9px]">
            <span class="text-[14px] font-medium text-[#6E6A62] dark:text-night-text/60">
              {{ resumo.concluidas }} de {{ resumo.totalTarefas }} tarefas concluídas
            </span>
          </div>
          <div class="h-[8px] rounded-[5px] overflow-hidden" style="background: rgba(54,51,46,.08);">
            <div
              class="h-full bg-feitu-teal rounded-[5px] transition-all duration-500"
              :style="{ width: pctConcluidas + '%' }"
            />
          </div>
        </div>

        <!-- Recorrentes -->
        <div v-if="resumo.recorrentes.length" class="mb-[18px]">
          <p class="text-[13px] font-semibold text-feitu-blue-deep dark:text-feitu-blue tracking-[.04em] uppercase mb-[10px] mt-[6px]">Recorrentes</p>
          <div class="flex flex-col">
            <div v-for="t in resumo.recorrentes" :key="t.id" class="flex items-center gap-[13px] py-[9px] px-[4px]">
              <span
                class="flex-shrink-0 w-[26px] h-[26px] rounded-full flex items-center justify-center"
                :style="t.concluida
                  ? 'background: #B5EAD7;'
                  : 'border: 2px solid rgba(54,51,46,.2);'"
              >
                <svg v-if="t.concluida" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round">
                  <path d="M5 12.5l4.5 4.5L19 7"/>
                </svg>
              </span>
              <span
                class="flex-1 text-[15px] font-medium"
                :style="t.concluida ? 'color: #6E6A62;' : 'color: #36332E;'"
              >{{ t.nome }}</span>
            </div>
          </div>
        </div>

        <!-- Pontuais -->
        <div v-if="resumo.pontuais.length">
          <p class="text-[13px] font-semibold text-[#8A5FC0] dark:text-feitu-lavender tracking-[.04em] uppercase mb-[10px] mt-[6px]">Pontuais</p>
          <div class="flex flex-col">
            <div v-for="t in resumo.pontuais" :key="t.id" class="flex items-center gap-[13px] py-[9px] px-[4px]">
              <span
                class="flex-shrink-0 w-[26px] h-[26px] rounded-full flex items-center justify-center"
                :style="t.concluida
                  ? 'background: #B5EAD7;'
                  : 'border: 2px solid rgba(54,51,46,.2);'"
              >
                <svg v-if="t.concluida" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round">
                  <path d="M5 12.5l4.5 4.5L19 7"/>
                </svg>
              </span>
              <span
                class="flex-1 text-[15px] font-medium"
                :style="t.concluida ? 'color: #6E6A62;' : 'color: #36332E;'"
              >{{ t.nome }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
