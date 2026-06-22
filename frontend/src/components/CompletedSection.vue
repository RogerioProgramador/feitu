<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import { useSettingsStore } from '../stores/settingsStore'
import { formatarTempo } from '../utils/formatarTempo'
import type { Tarefa } from '../types'

const props = defineProps<{ workspaceId: string; tarefas: Tarefa[] }>()
const emit = defineEmits<{ (e: 'abrirHistorico'): void }>()
const store = useTarefaStore()
const settings = useSettingsStore()

const tz = computed(() => settings.fusoHorario === 'BR' ? 'America/Sao_Paulo' : 'UTC')
const expandida = ref(false)

function utcMs(iso: string) {
  return new Date(iso.endsWith('Z') ? iso : iso + 'Z').getTime()
}

function diaCalendario(iso: string): string {
  return new Date(utcMs(iso)).toLocaleDateString('pt-BR', { timeZone: tz.value })
}

function isHoje(iso: string): boolean {
  return diaCalendario(iso) === new Date().toLocaleDateString('pt-BR', { timeZone: tz.value })
}

const concluidas = computed(() =>
  props.tarefas.filter((t) => t.estado === 'DONE' && (!t.concluidoEm || isHoje(t.concluidoEm))),
)

const historico = computed(() =>
  props.tarefas.filter((t) => t.estado === 'DONE' && t.concluidoEm && !isHoje(t.concluidoEm)),
)

function hora(t: Tarefa) {
  if (!t.concluidoEm) return ''
  return new Date(utcMs(t.concluidoEm)).toLocaleTimeString('pt-BR', {
    hour: '2-digit', minute: '2-digit', timeZone: tz.value,
  })
}
</script>

<template>
  <div v-if="concluidas.length || historico.length" class="mt-2 space-y-2">

    <!-- Concluídas de hoje -->
    <div v-if="concluidas.length">
      <!-- Header toggle -->
      <button
        @click="expandida = !expandida"
        class="w-full flex items-center justify-between px-[15px] py-[11px] rounded-[15px] bg-[#EAF6F0] dark:bg-[rgba(132,207,174,.1)] text-[#2F7D63] dark:text-[#84CFAE] transition"
      >
        <div class="flex items-center gap-[10px]">
          <!-- circle-check icon -->
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/><polyline points="9 12 11 14 15 10"/>
          </svg>
          <span class="text-[13.5px] font-semibold">Concluídas</span>
          <span class="text-[11.5px] font-semibold px-[7px] py-[1px] rounded-full bg-[rgba(47,125,99,.16)] dark:bg-[rgba(47,125,99,.25)]">{{ concluidas.length }}</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"
          class="transition-transform" :class="expandida ? 'rotate-180' : ''">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </button>

      <!-- Lista expandida -->
      <div v-if="expandida" class="space-y-[6px] mt-1">
        <div
          v-for="t in concluidas"
          :key="t.id"
          class="flex gap-[11px] px-[13px] py-[10px] rounded-[14px] bg-white dark:bg-night-surface border border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]"
        >
          <!-- check icon box -->
          <div class="w-6 h-6 flex-shrink-0 rounded-[8px] bg-[#DDF1E8] dark:bg-[rgba(47,125,99,.2)] flex items-center justify-center mt-0.5">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>

          <div class="flex-1 min-w-0">
            <p class="text-[13.5px] font-medium text-[#9A9388] dark:text-night-text/50 line-through truncate">{{ t.nome }}</p>
            <p class="text-[11px] text-[#B0A89B] dark:text-night-text/30 mt-[1px]">concluída às {{ hora(t) }}</p>
          </div>

          <span class="text-[12px] tabular-nums text-[#A39C90] flex-shrink-0 self-center">{{ formatarTempo(t.tempoTotalSegundos) }}</span>

          <!-- Reativar -->
          <button
            @click="store.reativar(workspaceId, t.id)"
            class="w-[28px] h-[28px] flex-shrink-0 flex items-center justify-center rounded-[8px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.08)] bg-white dark:bg-night-card text-[#A39C90] hover:text-feitu-text dark:hover:text-night-text transition self-center"
            title="Reativar"
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Botão Histórico -->
    <button
      v-if="historico.length"
      @click="emit('abrirHistorico')"
      class="w-full flex items-center gap-[12px] px-[15px] py-[12px] rounded-[15px] bg-[#EFEAE0] dark:bg-[rgba(255,255,255,.04)] text-[#6E6A62] dark:text-night-text/60 hover:bg-[#E8E3D8] dark:hover:bg-[rgba(255,255,255,.07)] transition"
    >
      <!-- clock icon box -->
      <div class="w-8 h-8 flex-shrink-0 rounded-[10px] bg-white dark:bg-night-surface flex items-center justify-center">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#8C857B" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
        </svg>
      </div>

      <div class="flex flex-col text-left flex-1">
        <span class="text-[14px] font-semibold text-[#46423B] dark:text-night-text">Histórico</span>
        <span class="text-[11.5px] text-[#9A9388]">registros por data</span>
      </div>

      <span class="text-[12px] font-semibold px-[8px] py-[2px] rounded-full bg-white dark:bg-night-surface text-[#8C857B]">{{ historico.length }}</span>

      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="9 18 15 12 9 6"/>
      </svg>
    </button>

  </div>
</template>
