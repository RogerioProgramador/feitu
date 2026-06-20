<script setup lang="ts">
import { computed, watch } from 'vue'
import { useTarefaStore } from '../stores/tarefaStore'
import TaskItem from './TaskItem.vue'
import CompletedSection from './CompletedSection.vue'

const props = defineProps<{ workspaceId: string }>()

const store = useTarefaStore()

const ativas = computed(() =>
  (store.tarefas[props.workspaceId] ?? []).filter((t) => t.estado !== 'DONE'),
)
const todas = computed(() => store.tarefas[props.workspaceId] ?? [])

watch(
  () => props.workspaceId,
  (id) => { if (id) store.carregar(id) },
  { immediate: true },
)

async function novaTarefa() {
  await store.criar(props.workspaceId)
}
</script>

<template>
  <div class="flex flex-col gap-2 p-4">
    <TaskItem
      v-for="t in ativas"
      :key="t.id"
      :tarefa="t"
      :workspace-id="workspaceId"
    />

    <button
      @click="novaTarefa"
      class="flex items-center gap-2 px-4 py-3 text-feitu-text/50 hover:text-feitu-text text-sm rounded-2xl hover:bg-white/60 transition text-left"
    >
      <span class="text-lg leading-none">+</span>
      <span>Nova tarefa</span>
    </button>

    <CompletedSection :workspace-id="workspaceId" :tarefas="todas" />
  </div>
</template>
