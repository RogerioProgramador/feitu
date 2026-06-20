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
  if (!nome) return
  await store.criar(nome, proximaCor())
  novoNome.value = ''
  criando.value = false
}

function cancelarNovo() {
  novoNome.value = ''
  criando.value = false
}
</script>

<template>
  <div class="flex items-center gap-1 px-2 pt-2 overflow-x-auto no-scrollbar">
    <button
      v-for="ws in store.workspaces"
      :key="ws.id"
      @click="store.selecionar(ws.id)"
      class="flex-shrink-0 px-4 py-2 rounded-t-xl text-sm font-medium transition-all"
      :class="
        store.ativoId === ws.id
          ? 'text-feitu-text border-b-2 border-feitu-text bg-white shadow-sm'
          : 'text-feitu-text/60 hover:text-feitu-text'
      "
      :style="store.ativoId === ws.id ? { borderBottomColor: ws.cor ?? '#A7C7E7' } : {}"
    >
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
        class="flex-shrink-0 px-3 py-1.5 text-sm border border-feitu-blue rounded-xl outline-none w-28"
      />
    </template>

    <button
      v-else
      @click="criando = true"
      class="flex-shrink-0 px-3 py-2 text-feitu-text/50 hover:text-feitu-text text-lg leading-none rounded-xl hover:bg-white transition"
      title="Novo workspace"
    >
      +
    </button>
  </div>
</template>
