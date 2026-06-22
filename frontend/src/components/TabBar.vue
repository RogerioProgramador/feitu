<script setup lang="ts">
import { ref } from 'vue'
import { useWorkspaceStore } from '../stores/workspaceStore'

const store = useWorkspaceStore()

const criando = ref(false)
const novoNome = ref('')

const CORES = ['#A7C7E7', '#B5EAD7', '#FFDAC1', '#E2C6FF', '#FFD1DC', '#C7E6A7']

function proximaCor() {
  return CORES[store.workspaces.length % CORES.length]
}

async function confirmarNovo() {
  const nome = novoNome.value.trim()
  if (!nome) { cancelarNovo(); return }
  await store.criar(nome, proximaCor())
  novoNome.value = ''
  criando.value = false
}

function cancelarNovo() {
  novoNome.value = ''
  criando.value = false
}

function hexToRgb(hex: string) {
  const h = hex.replace('#', '')
  const r = parseInt(h.substring(0, 2), 16)
  const g = parseInt(h.substring(2, 4), 16)
  const b = parseInt(h.substring(4, 6), 16)
  return `${r},${g},${b}`
}
</script>

<template>
  <div class="flex items-center gap-[6px] px-4 pt-3 pb-2 overflow-x-auto no-scrollbar">
    <button
      v-for="ws in store.workspaces"
      :key="ws.id"
      @click="store.selecionar(ws.id)"
      class="flex-shrink-0 flex items-center gap-[7px] px-[15px] py-[8px] rounded-[11px] border text-[13.5px] font-semibold transition-all"
      :class="
        store.ativoId === ws.id
          ? 'text-feitu-text dark:text-night-text'
          : 'border-transparent bg-transparent text-[#8C857B] dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text'
      "
      :style="store.ativoId === ws.id ? {
        borderColor: `rgba(${hexToRgb(ws.cor ?? '#A7C7E7')},.2)`,
        backgroundColor: `rgba(${hexToRgb(ws.cor ?? '#A7C7E7')},.12)`,
      } : {}"
    >
      <span
        class="w-[7px] h-[7px] rounded-full flex-shrink-0 transition-opacity"
        :class="store.ativoId === ws.id ? 'opacity-100' : 'opacity-50'"
        :style="{ backgroundColor: ws.cor ?? '#A7C7E7' }"
      />
      {{ ws.nome }}
    </button>

    <template v-if="criando">
      <input
        v-focus
        v-model="novoNome"
        @keydown.enter="confirmarNovo"
        @keydown.esc="cancelarNovo"
        @blur="confirmarNovo"
        placeholder="Nome..."
        class="flex-shrink-0 px-3 py-1.5 text-[13.5px] border border-feitu-blue rounded-[11px] outline-none w-28 bg-white dark:bg-night-surface text-feitu-text dark:text-night-text"
      />
    </template>

    <button
      v-else
      @click="criando = true"
      class="flex-shrink-0 w-[34px] h-[34px] flex items-center justify-center rounded-[11px] border border-dashed border-[rgba(54,51,46,.25)] dark:border-[rgba(255,255,255,.15)] text-[#8C857B] dark:text-night-text/50 hover:text-feitu-text dark:hover:text-night-text hover:border-[rgba(54,51,46,.5)] transition text-lg leading-none"
      title="Novo workspace"
    >+</button>
  </div>
</template>
