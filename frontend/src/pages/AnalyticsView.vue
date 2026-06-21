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
    <header class="flex items-center justify-between px-4 pt-4 pb-2">
      <RouterLink to="/workspaces" class="text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text text-sm">
        ← Voltar
      </RouterLink>
      <h1 class="text-lg font-semibold text-feitu-text dark:text-night-text">Resumo</h1>
      <div class="w-12" />
    </header>

    <!-- Navegação de data -->
    <div class="flex items-center justify-center gap-4 py-3">
      <button @click="anteriorDia" class="text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text px-2">‹</button>
      <span class="text-sm font-medium text-feitu-text dark:text-night-text">{{ formatarDataBR(data) }}</span>
      <button
        @click="proximoDia"
        :disabled="data >= hojeISO()"
        class="text-feitu-text/60 dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text px-2 disabled:opacity-30"
      >›</button>
    </div>

    <div v-if="carregando" class="p-8 text-center text-feitu-text/40 dark:text-night-text/40 text-sm">Carregando...</div>
    <div v-else-if="erro" class="p-8 text-center text-red-400 text-sm">{{ erro }}</div>

    <div v-else-if="resumo" class="px-4 pb-8 space-y-6">
      <!-- Total -->
      <div class="bg-white dark:bg-night-surface rounded-2xl shadow-sm p-5 text-center">
        <p class="text-xs text-feitu-text/50 dark:text-night-text/50 mb-1">Total do dia</p>
        <p class="text-3xl font-mono font-semibold text-feitu-text dark:text-night-text">{{ totalFormatado }}</p>
      </div>

      <!-- Barra por workspace -->
      <div v-if="barras.length" class="bg-white dark:bg-night-surface rounded-2xl shadow-sm p-5 space-y-3">
        <p class="text-xs font-medium text-feitu-text/50 dark:text-night-text/50 mb-1">Por workspace</p>
        <div v-for="b in barras" :key="b.workspaceId" class="space-y-1">
          <div class="flex justify-between text-xs text-feitu-text dark:text-night-text">
            <span>{{ b.nome }}</span>
            <span class="font-mono text-feitu-text/60 dark:text-night-text/60">{{ formatarTempo(b.segundos) }}</span>
          </div>
          <div class="h-2 bg-feitu-bg dark:bg-night-bg rounded-full overflow-hidden">
            <div
              class="h-full rounded-full transition-all"
              :style="{ width: b.pct + '%', backgroundColor: b.cor ?? '#A7C7E7' }"
            />
          </div>
        </div>
      </div>

      <!-- Tarefa mais longa -->
      <div v-if="resumo.tarefaMaisLonga" class="bg-feitu-peach/40 dark:bg-feitu-peach/20 rounded-2xl p-4">
        <p class="text-xs text-feitu-text/50 dark:text-night-text/50 mb-1">Tarefa mais longa</p>
        <p class="text-sm font-medium text-feitu-text dark:text-night-text">{{ resumo.tarefaMaisLonga.nome }}</p>
        <p class="text-xs font-mono text-feitu-text/60 dark:text-night-text/60 mt-0.5">
          {{ formatarTempo(resumo.tarefaMaisLonga.segundos) }}
        </p>
      </div>

      <!-- Timeline -->
      <div v-if="resumo.timeline.length" class="bg-white dark:bg-night-surface rounded-2xl shadow-sm p-5">
        <p class="text-xs font-medium text-feitu-text/50 dark:text-night-text/50 mb-3">Timeline</p>
        <div class="space-y-2">
          <div
            v-for="(item, idx) in resumo.timeline"
            :key="item.inicio"
            class="flex items-start gap-3"
          >
            <div
              class="w-1 self-stretch rounded-full flex-shrink-0 mt-0.5"
              :style="{ backgroundColor: item.workspaceCor ?? '#A7C7E7' }"
            />
            <div class="flex-1 min-w-0">
              <p
                class="text-sm truncate"
                :class="idx > 0 && resumo.timeline[idx - 1].tarefaId === item.tarefaId
                  ? 'text-feitu-text/50 dark:text-night-text/50 italic'
                  : 'text-feitu-text dark:text-night-text'"
              >{{ item.tarefaNome }}</p>
              <p class="text-xs text-feitu-text/50 dark:text-night-text/50 font-mono">
                {{ horaMinuto(item.inicio) }} – {{ horaMinuto(item.fim) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Estado vazio -->
      <div
        v-if="!barras.length && !resumo.timeline.length"
        class="text-center text-feitu-text/40 dark:text-night-text/40 text-sm py-8"
      >
        Nenhum tempo registrado neste dia
      </div>
    </div>
  </div>
</template>
