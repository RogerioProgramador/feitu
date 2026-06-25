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
const tipo = ref<TipoTarefa>('RECORRENTE')
const diasSelecionados = ref<string[]>([])
const horarioAtivo = ref(true)
const hora = ref('07')
const minuto = ref('00')
const salvando = ref(false)
const erro = ref('')

const DIAS_LABEL = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom']
const DIAS_API   = ['SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SAB', 'DOM'] as const

const todosDias = computed(() => diasSelecionados.value.length === 7)

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
  erro.value = ''
  try {
    const horario = horarioAtivo.value && tipo.value === 'RECORRENTE'
      ? `${String(hora.value).padStart(2, '0')}:${String(minuto.value).padStart(2, '0')}`
      : null

    await store.criar(
      props.workspaceId,
      {
        nome: nome.value.trim(),
        descricao: descricao.value || null,
        tipo: tipo.value,
        diasSemana: tipo.value === 'RECORRENTE' ? diasSelecionados.value : undefined,
        horario,
      },
      props.date,
    )
    emit('criada')
  } catch (e: any) {
    erro.value = e.response?.data?.detail ?? 'Erro ao criar tarefa'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-40">
      <!-- Fundo escurecido -->
      <div
        class="absolute inset-0 bg-[rgba(40,37,33,.4)] dark:bg-black/60 animate-feitu-dim"
        @click="emit('fechar')"
      />

      <!-- Sheet -->
      <div
        class="absolute left-0 right-0 bottom-0 bg-[#F7F4EE] dark:bg-night-surface rounded-t-[24px] shadow-[0_-16px_40px_-16px_rgba(54,51,46,.4)] animate-feitu-sheet"
        style="padding: 10px 22px 26px;"
      >
        <!-- Handle -->
        <div class="flex justify-center" style="padding: 6px 0 16px;">
          <span class="w-[42px] h-[5px] rounded-[3px] bg-[rgba(54,51,46,.18)] dark:bg-night-card"></span>
        </div>

        <!-- Título -->
        <p class="text-[19px] font-semibold text-feitu-text dark:text-night-text mb-[18px] leading-none">Nova Tarefa</p>

        <!-- NOME -->
        <p class="text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 tracking-[.04em] mb-[7px]">
          NOME <span class="text-feitu-peach-deep">*</span>
        </p>
        <input
          v-model="nome"
          placeholder="Nome da tarefa"
          autofocus
          class="w-full border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] bg-feitu-bg dark:bg-night-card rounded-[13px] outline-none mb-[14px]"
          style="padding: 12px 14px; font: 500 15px 'Space Grotesk'; color: #36332E;"
        />

        <!-- DESCRIÇÃO -->
        <p class="text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 tracking-[.04em] mb-[7px]">DESCRIÇÃO</p>
        <textarea
          v-model="descricao"
          placeholder="Opcional"
          rows="2"
          class="w-full border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] bg-feitu-bg dark:bg-night-card rounded-[13px] outline-none resize-none mb-[14px]"
          style="padding: 12px 14px; font: 400 14px/1.4 'Space Grotesk'; color: #36332E; min-height: 54px;"
        />

        <!-- TIPO -->
        <p class="text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 tracking-[.04em] mb-[7px]">TIPO</p>
        <div class="flex gap-[6px] rounded-[13px] mb-[16px]" style="background: #E8E4DC; padding: 4px;">
          <button
            v-for="t in (['PONTUAL', 'RECORRENTE'] as TipoTarefa[])"
            :key="t"
            @click="tipo = t"
            class="flex-1 rounded-[10px] transition-all cursor-pointer border-none"
            style="padding: 11px; font: 600 13.5px 'Space Grotesk';"
            :style="tipo === t
              ? 'background: #F7F4EE; color: #5E8BB6; box-shadow: 0 1px 3px rgba(54,51,46,.1);'
              : 'background: transparent; color: #8C857B;'"
          >{{ t === 'PONTUAL' ? 'Pontual' : 'Recorrente' }}</button>
        </div>

        <!-- Opções recorrente -->
        <div v-if="tipo === 'RECORRENTE'" class="animate-feitu-fade">
          <p class="text-[12px] font-semibold text-[#8C857B] dark:text-night-text/50 tracking-[.04em] mb-[9px]">FREQUÊNCIA</p>

          <!-- 7 chips em flex:1 -->
          <div class="flex gap-[6px] mb-[9px]">
            <button
              v-for="(label, i) in DIAS_LABEL"
              :key="label"
              @click="toggleDia(DIAS_API[i])"
              class="flex-1 rounded-[11px] border-none cursor-pointer transition-all"
              style="padding: 10px 0; text-align: center; font: 600 12.5px 'Space Grotesk';"
              :style="diasSelecionados.includes(DIAS_API[i])
                ? 'background: #5E8BB6; color: #fff;'
                : 'background: #E8E4DC; color: #6E6A62;'"
            >{{ label }}</button>
          </div>

          <!-- Todos os dias (full-width separado) -->
          <button
            @click="toggleTodosDias"
            class="w-full rounded-[11px] border-none cursor-pointer transition-all mb-[18px]"
            style="padding: 11px; text-align: center; font: 600 13px 'Space Grotesk';"
            :style="todosDias
              ? 'background: #5E8BB6; color: #fff; border: 1.5px solid #5E8BB6;'
              : 'background: transparent; color: #5E8BB6; border: 1.5px dashed rgba(94,139,182,.5);'"
          >Todos os dias</button>

          <!-- Horário toggle -->
          <div class="flex items-center justify-between">
            <span style="font: 600 14px/1 'Space Grotesk'; color: #36332E;" class="dark:text-night-text">Horário</span>
            <button
              @click="horarioAtivo = !horarioAtivo"
              class="relative rounded-full border-none cursor-pointer transition-all"
              style="width: 46px; height: 27px; padding: 0;"
              :style="{ background: horarioAtivo ? '#5E8BB6' : 'rgba(54,51,46,.18)' }"
            >
              <span
                class="absolute rounded-full bg-white transition-all"
                style="top: 3px; width: 21px; height: 21px; box-shadow: 0 1px 3px rgba(0,0,0,.2);"
                :style="{ left: horarioAtivo ? '22px' : '3px' }"
              />
            </button>
          </div>

          <!-- Inputs de hora (linha separada, maior) -->
          <div v-if="horarioAtivo" class="flex items-center gap-[8px] mt-[12px] animate-feitu-fade">
            <input
              v-model="hora"
              type="number" min="0" max="23"
              class="w-[60px] text-center border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[11px] outline-none bg-feitu-bg dark:bg-night-card"
              style="padding: 11px 0; font: 600 18px 'Space Grotesk'; color: #36332E;"
            />
            <span style="font: 600 18px 'Space Grotesk'; color: #8C857B;">:</span>
            <input
              v-model="minuto"
              type="number" min="0" max="59"
              class="w-[60px] text-center border border-[rgba(54,51,46,.12)] dark:border-[rgba(255,255,255,.08)] rounded-[11px] outline-none bg-feitu-bg dark:bg-night-card"
              style="padding: 11px 0; font: 600 18px 'Space Grotesk'; color: #36332E;"
            />
          </div>
        </div>

        <!-- Erro ao criar -->
        <p v-if="erro" class="text-[12.5px] text-red-500 bg-red-50 dark:bg-red-950/30 rounded-[10px] px-3 py-2 mt-[14px]">{{ erro }}</p>

        <!-- Botão criar -->
        <button
          @click="criar"
          :disabled="!podeSubmeter || salvando"
          class="w-full rounded-[14px] border-none cursor-pointer transition-all"
          style="margin-top: 22px; padding: 15px; text-align: center; font: 600 15.5px 'Space Grotesk'; color: #fff;"
          :style="podeSubmeter
            ? 'background: #5E8BB6; box-shadow: 0 8px 20px -8px rgba(94,139,182,.6); cursor: pointer;'
            : 'background: rgba(94,139,182,.3); cursor: not-allowed;'"
        >{{ salvando ? 'Criando...' : 'Criar Tarefa' }}</button>
      </div>
    </div>
  </Teleport>
</template>
