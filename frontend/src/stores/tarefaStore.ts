import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tarefaApi } from '../api/tarefaApi'
import type { Tarefa } from '../types'

export const useTarefaStore = defineStore('tarefa', () => {
  const tarefas = ref<Record<string, Tarefa[]>>({})

  async function carregar(workspaceId: string) {
    tarefas.value[workspaceId] = await tarefaApi.listar(workspaceId)
  }

  async function criar(workspaceId: string, nome?: string) {
    const t = await tarefaApi.criar(workspaceId, nome)
    tarefas.value[workspaceId] = [...(tarefas.value[workspaceId] ?? []), t]
    return t
  }

  function atualizar(workspaceId: string, tarefa: Tarefa) {
    const lista = tarefas.value[workspaceId] ?? []
    const idx = lista.findIndex((t) => t.id === tarefa.id)
    if (idx !== -1) lista[idx] = tarefa
    else lista.push(tarefa)
    tarefas.value[workspaceId] = [...lista]
  }

  async function renomear(workspaceId: string, id: string, nome: string) {
    const t = await tarefaApi.renomear(id, nome)
    atualizar(workspaceId, t)
  }

  async function iniciarTimer(workspaceId: string, id: string) {
    const t = await tarefaApi.iniciarTimer(id)
    atualizar(workspaceId, t)
  }

  async function pausarTimer(workspaceId: string, id: string) {
    const t = await tarefaApi.pausarTimer(id)
    atualizar(workspaceId, t)
  }

  async function pararTimer(workspaceId: string, id: string) {
    const t = await tarefaApi.pararTimer(id)
    atualizar(workspaceId, t)
  }

  async function reativar(workspaceId: string, id: string) {
    const t = await tarefaApi.reativar(id)
    atualizar(workspaceId, t)
  }

  async function deletar(workspaceId: string, id: string) {
    await tarefaApi.deletar(id)
    tarefas.value[workspaceId] = (tarefas.value[workspaceId] ?? []).filter((t) => t.id !== id)
  }

  async function atualizarDescricao(workspaceId: string, id: string, descricao: string | null) {
    const t = await tarefaApi.atualizarDescricao(id, descricao)
    atualizar(workspaceId, t)
  }

  return { tarefas, carregar, criar, renomear, iniciarTimer, pausarTimer, pararTimer, reativar, deletar, atualizarDescricao }
})
