<script setup lang="ts">
import { computed } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import { useSettingsStore } from '../stores/settingsStore'
import { formatarTempo } from '../utils/formatarTempo'

const props = defineProps<{ workspaceId: string; workspaceCor: string }>()
const emit = defineEmits<{ (e: 'voltar'): void }>()

const store = useTarefaStore()
const settings = useSettingsStore()
const tz = computed(() => settings.fusoHorario === 'BR' ? 'America/Sao_Paulo' : 'UTC')

function utcMs(iso: string) {
  return new Date(iso.endsWith('Z') ? iso : iso + 'Z').getTime()
}

function diaCalendario(iso: string): string {
  return new Date(utcMs(iso)).toLocaleDateString('pt-BR', { timeZone: tz.value })
}

function isHoje(iso: string): boolean {
  return diaCalendario(iso) === new Date().toLocaleDateString('pt-BR', { timeZone: tz.value })
}

function hora(iso: string) {
  return new Date(utcMs(iso)).toLocaleTimeString('pt-BR', {
    hour: '2-digit', minute: '2-digit', timeZone: tz.value,
  })
}

function labelDia(diaStr: string): string {
  const hoje = new Date().toLocaleDateString('pt-BR', { timeZone: tz.value })
  const ontem = new Date(Date.now() - 86400000).toLocaleDateString('pt-BR', { timeZone: tz.value })
  if (diaStr === hoje) return 'Hoje'
  if (diaStr === ontem) return 'Ontem'
  return diaStr
}

interface GrupoDia {
  diaStr: string
  label: string
  totalSegundos: number
  tarefas: typeof historico.value
}

const historico = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter(
    (t) => t.estado === 'DONE' && t.concluidoEm && !isHoje(t.concluidoEm),
  ).sort((a, b) => utcMs(b.concluidoEm!) - utcMs(a.concluidoEm!)),
)

const grupos = computed((): GrupoDia[] => {
  const map = new Map<string, GrupoDia>()
  for (const t of historico.value) {
    const diaStr = diaCalendario(t.concluidoEm!)
    if (!map.has(diaStr)) {
      map.set(diaStr, { diaStr, label: labelDia(diaStr), totalSegundos: 0, tarefas: [] })
    }
    const g = map.get(diaStr)!
    g.tarefas.push(t)
    g.totalSegundos += t.tempoTotalSegundos
  }
  return [...map.values()]
})
</script>

<template>
  <div class="flex flex-col flex-1 overflow-hidden">
    <!-- Header com back -->
    <div class="flex items-center gap-3 px-4 pt-3 pb-4">
      <button
        @click="emit('voltar')"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-white dark:bg-night-surface text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h2 class="text-[17px] font-semibold text-feitu-text dark:text-night-text">Histórico</h2>
    </div>

    <!-- Empty state -->
    <div v-if="!historico.length" class="flex-1 flex flex-col items-center justify-center gap-4 px-8 pb-12">
      <div class="w-16 h-16 rounded-[20px] bg-[#EFEAE0] dark:bg-[rgba(255,255,255,.05)] flex items-center justify-center">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#B0A89B" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
        </svg>
      </div>
      <p class="text-[14px] text-[#A39C90] text-center">Nenhuma tarefa concluída em dias anteriores</p>
    </div>

    <!-- Grupos por data -->
    <div v-else class="flex-1 overflow-y-auto px-4 pb-6 space-y-5">
      <div v-for="grupo in grupos" :key="grupo.diaStr">
        <!-- Cabeçalho do dia -->
        <div class="flex items-center justify-between mb-2">
          <span class="text-[13px] font-semibold text-feitu-text dark:text-night-text">{{ grupo.label }}</span>
          <span class="text-[12px] font-medium text-feitu-blue-deep dark:text-[#93B7DB] tabular-nums">{{ formatarTempo(grupo.totalSegundos) }}</span>
        </div>

        <!-- Tarefas do dia -->
        <div class="space-y-[6px]">
          <div
            v-for="t in grupo.tarefas"
            :key="t.id"
            class="flex gap-[12px] px-[14px] py-[12px] rounded-[15px] bg-white dark:bg-night-surface border border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]"
          >
            <!-- dot cor workspace -->
            <div
              class="w-[8px] h-[8px] rounded-full flex-shrink-0 mt-[5px]"
              :style="{ backgroundColor: workspaceCor }"
            />

            <div class="flex-1 min-w-0">
              <p class="text-[13.5px] font-medium text-feitu-text/70 dark:text-night-text/70 truncate">{{ t.nome }}</p>
              <p class="text-[11.5px] text-[#B0A89B] mt-[1px]" v-if="t.concluidoEm">{{ hora(t.concluidoEm) }}</p>
            </div>

            <span class="text-[14px] font-medium text-feitu-blue-deep dark:text-[#93B7DB] tabular-nums flex-shrink-0 self-center">
              {{ formatarTempo(t.tempoTotalSegundos) }}
            </span>

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
    </div>
  </div>
</template>
