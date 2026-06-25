<script setup lang="ts">
import { useWorkspaceStore } from '../stores/workspaceStore'

const store = useWorkspaceStore()

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
  </div>
</template>
