<script setup lang="ts">
import { ref } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import type { Tarefa } from '../types'

const props = defineProps<{ tarefa: Tarefa; workspaceId: string; date?: string }>()

const store = useTarefaStore()
const carregando = ref(false)
const menuAberto = ref(false)
const sheetAberta = ref(false)
const editando = ref(false)
const descricaoEdit = ref('')

async function toggleConcluida() {
  if (carregando.value) return
  carregando.value = true
  try {
    if (props.tarefa.concluida) {
      await store.reabrir(props.workspaceId, props.tarefa.id, props.date)
    } else {
      await store.concluir(props.workspaceId, props.tarefa.id, props.date)
    }
  } finally {
    carregando.value = false
  }
}

function abrirSheet() {
  menuAberto.value = false
  sheetAberta.value = true
  editando.value = false
}

function fecharSheet() {
  sheetAberta.value = false
  editando.value = false
}

function iniciarEdicao() {
  descricaoEdit.value = props.tarefa.descricao ?? ''
  editando.value = true
}

async function salvarDescricao() {
  if (carregando.value) return
  carregando.value = true
  try {
    await store.atualizarDescricao(props.workspaceId, props.tarefa.id, descricaoEdit.value || null)
    editando.value = false
  } finally {
    carregando.value = false
  }
}

async function excluirTarefa() {
  if (carregando.value) return
  carregando.value = true
  try {
    await store.deletar(props.workspaceId, props.tarefa.id)
    sheetAberta.value = false
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div
    class="rounded-[16px] border shadow-[0_1px_2px_rgba(54,51,46,.03)] transition-all relative"
    :class="menuAberto
      ? 'bg-[#FCFAF6] shadow-[0_6px_18px_-10px_rgba(54,51,46,.35)]'
      : 'bg-feitu-surface dark:bg-night-card border-[rgba(54,51,46,.06)] dark:border-[rgba(255,255,255,.05)]'"
    :style="menuAberto ? 'border-color: #5E8BB6;' : ''"
  >
    <!-- Linha principal -->
    <div class="flex items-center gap-[14px] pl-[15px] pr-[8px] py-[13px]">

      <!-- Checkbox 28×28 -->
      <button
        @click="toggleConcluida"
        :disabled="carregando"
        class="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center transition-all disabled:opacity-50"
        :class="tarefa.concluida
          ? 'bg-feitu-teal border-none'
          : 'border-[2px] border-[rgba(54,51,46,.22)] dark:border-[rgba(255,255,255,.2)] bg-transparent'"
      >
        <svg
          v-if="tarefa.concluida"
          class="animate-feitu-pop"
          width="15" height="15" viewBox="0 0 24 24"
          fill="none" stroke="#2F7D63" stroke-width="2.8"
          stroke-linecap="round" stroke-linejoin="round"
        >
          <path d="M5 12.5l4.5 4.5L19 7"/>
        </svg>
      </button>

      <!-- Nome -->
      <span
        class="flex-1 min-w-0 block truncate"
        style="font: 500 15px/1.25 'Space Grotesk';"
        :class="tarefa.concluida
          ? 'text-[#A49C90] dark:text-night-text/40'
          : 'text-feitu-text dark:text-night-text'"
      >{{ tarefa.nome }}</span>

      <!-- Horário (apenas recorrentes) -->
      <span
        v-if="tarefa.tipo === 'RECORRENTE' && tarefa.horario"
        class="flex-shrink-0 text-[13px] font-medium text-[#A49C90] dark:text-[#7C7888] leading-none"
      >{{ tarefa.horario }}</span>

      <!-- ⋮ button -->
      <button
        @click.stop="menuAberto = true"
        class="flex-shrink-0 self-stretch flex items-center justify-center w-[30px] transition-colors"
        :style="menuAberto
          ? 'border-left: 1px solid rgba(94,139,182,.25); color: #5E8BB6;'
          : 'border-left: 1px solid rgba(54,51,46,.07); color: #C4BDB1;'"
      >
        <svg width="4" height="16" viewBox="0 0 4 16" fill="currentColor">
          <circle cx="2" cy="2" r="1.7"/>
          <circle cx="2" cy="8" r="1.7"/>
          <circle cx="2" cy="14" r="1.7"/>
        </svg>
      </button>
    </div>

    <!-- Popover -->
    <template v-if="menuAberto">
      <div class="fixed inset-0 z-40" @click="menuAberto = false"></div>
      <div
        class="absolute right-0 z-50 overflow-hidden"
        style="top: calc(100% + 8px); width: 236px; background: #FFFFFF; border: 1px solid rgba(54,51,46,.08); border-radius: 16px; box-shadow: 0 18px 44px -16px rgba(54,51,46,.45);"
      >
        <button
          @click.stop="abrirSheet"
          class="w-full flex items-center gap-[13px] border-none cursor-pointer hover:bg-[rgba(54,51,46,.03)] transition"
          style="padding: 14px 16px; background: transparent;"
        >
          <span class="flex-shrink-0 flex" style="color: #5E8BB6;">
            <svg width="19" height="19" viewBox="0 0 24 24" fill="none"><path d="M5 19h3l9-9-3-3-9 9v3z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 7l3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </span>
          <span style="font: 500 15px/1 'Space Grotesk'; color: #36332E;">Ver descrição</span>
        </button>
        <div style="height: 1px; background: rgba(54,51,46,.07);"></div>
        <button
          @click.stop="excluirTarefa"
          :disabled="carregando"
          class="w-full flex items-center gap-[13px] border-none cursor-pointer hover:bg-[rgba(224,123,79,.04)] transition disabled:opacity-50"
          style="padding: 14px 16px; background: transparent;"
        >
          <span class="flex-shrink-0 flex" style="color: #E07B4F;">
            <svg width="19" height="19" viewBox="0 0 24 24" fill="none"><path d="M5 7h14M9 7V5h6v2M7 7l1 12h8l1-12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </span>
          <span style="font: 500 15px/1 'Space Grotesk'; color: #E07B4F;">Excluir tarefa</span>
        </button>
      </div>
    </template>

    <!-- Bottom sheet -->
    <Teleport to="body">
      <template v-if="sheetAberta">
        <div
          class="fixed inset-0 z-40 animate-feitu-dim"
          style="background: rgba(40,37,33,.42);"
          @click="fecharSheet"
        ></div>
        <div
          class="fixed left-0 right-0 bottom-0 z-50 animate-feitu-sheet"
          style="background: #F7F4EE; border-radius: 24px 24px 0 0; padding: 10px 22px 26px; box-shadow: 0 -16px 40px -16px rgba(54,51,46,.4);"
        >
          <!-- Handle -->
          <div class="flex justify-center" style="padding: 6px 0 16px;">
            <span style="width: 42px; height: 5px; border-radius: 3px; background: rgba(54,51,46,.18); display: block;"></span>
          </div>

          <!-- Cabeçalho -->
          <div class="flex items-start gap-[12px]" style="margin-bottom: 20px;">
            <span
              class="flex-shrink-0 w-[28px] h-[28px] rounded-full flex items-center justify-center mt-[1px]"
              :class="tarefa.concluida ? 'bg-feitu-teal' : 'border-[2px] border-[rgba(54,51,46,.22)]'"
            >
              <svg v-if="tarefa.concluida" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#2F7D63" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 12.5l4.5 4.5L19 7"/>
              </svg>
            </span>
            <div class="flex-1 min-w-0">
              <div style="font: 600 19px/1.2 'Space Grotesk'; color: #36332E;">{{ tarefa.nome }}</div>
              <div class="flex items-center gap-[7px]" style="margin-top: 5px;">
                <span class="w-[7px] h-[7px] rounded-full flex-shrink-0" style="background: #5E8BB6;"></span>
                <span style="font: 500 13px/1 'Space Grotesk'; color: #8C857B;">
                  {{ tarefa.tipo === 'RECORRENTE' ? 'Recorrente' : 'Pontual' }}{{ tarefa.horario ? ` · ${tarefa.horario}` : '' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Cabeçalho da descrição -->
          <div class="flex items-center justify-between" style="margin-bottom: 9px;">
            <span style="font: 600 12px/1 'Space Grotesk'; color: #8C857B; letter-spacing: .04em;">DESCRIÇÃO</span>
            <button
              v-if="!editando"
              @click="iniciarEdicao"
              class="flex items-center gap-[6px] transition hover:opacity-70"
              style="padding: 6px 12px; border-radius: 20px; border: 1px solid rgba(94,139,182,.4); color: #5E8BB6; font: 600 12.5px 'Space Grotesk'; background: transparent; cursor: pointer;"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M5 19h3l9-9-3-3-9 9v3z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 7l3 3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
              Editar
            </button>
          </div>

          <!-- View mode -->
          <div
            v-if="!editando"
            style="background: #E8E4DC; border: 1px solid rgba(54,51,46,.08); border-radius: 14px; padding: 14px 16px; font: 400 14.5px/1.6 'Space Grotesk'; color: #4A463F; margin-bottom: 18px; min-height: 56px;"
          >
            {{ tarefa.descricao || 'Sem descrição.' }}
          </div>

          <!-- Edit mode -->
          <div v-else style="margin-bottom: 16px;">
            <textarea
              v-focus
              v-model="descricaoEdit"
              rows="4"
              style="width: 100%; background: #fff; border: 1.5px solid #5E8BB6; border-radius: 14px; padding: 14px 16px; font: 400 14.5px/1.6 'Space Grotesk'; color: #4A463F; outline: none; box-shadow: 0 0 0 4px rgba(94,139,182,.12); resize: none; box-sizing: border-box;"
            ></textarea>
            <div class="flex gap-[10px]" style="margin-top: 10px;">
              <button
                @click="editando = false"
                style="flex: none; padding: 13px 22px; border-radius: 14px; border: 1px solid rgba(54,51,46,.14); color: #6E6A62; font: 600 15px 'Space Grotesk'; background: transparent; cursor: pointer;"
              >Cancelar</button>
              <button
                @click="salvarDescricao"
                :disabled="carregando"
                style="flex: 1; padding: 13px; text-align: center; border-radius: 14px; background: #5E8BB6; color: #fff; font: 600 15px 'Space Grotesk'; border: none; box-shadow: 0 10px 24px -10px rgba(94,139,182,.6); cursor: pointer;"
                :style="carregando ? 'opacity: 0.5;' : ''"
              >Salvar</button>
            </div>
          </div>

          <!-- Separador -->
          <div style="height: 1px; background: rgba(54,51,46,.1); margin-bottom: 6px;"></div>

          <!-- Excluir tarefa -->
          <button
            @click="excluirTarefa"
            :disabled="carregando"
            class="flex items-center gap-[12px] transition hover:opacity-70 disabled:opacity-40"
            style="padding: 13px 4px 2px; background: transparent; border: none; cursor: pointer;"
          >
            <svg width="19" height="19" viewBox="0 0 24 24" fill="none" style="color: #E07B4F;"><path d="M5 7h14M9 7V5h6v2M7 7l1 12h8l1-12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            <span style="font: 600 15px/1 'Space Grotesk'; color: #E07B4F;">Excluir tarefa</span>
          </button>
        </div>
      </template>
    </Teleport>
  </div>
</template>
