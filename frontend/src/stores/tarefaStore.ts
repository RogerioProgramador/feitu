import { defineStore } from 'pinia'
import { ref } from 'vue'
import { tarefaApi } from '../api/tarefaApi'
import type { Tarefa, TarefaCreateRequest } from '../types'

export const useTarefaStore = defineStore('tarefa', () => {
  const tarefas = ref<Record<string, Tarefa[]>>({})

  async function carregar(workspaceId: string, date?: string) {
    tarefas.value[workspaceId] = await tarefaApi.listar(workspaceId, date)
  }

  async function criar(workspaceId: string, req: TarefaCreateRequest, date?: string) {
    const t = await tarefaApi.criar(workspaceId, req, date)
    tarefas.value[workspaceId] = [...(tarefas.value[workspaceId] ?? []), t]
    return t
  }

  function _atualizar(workspaceId: string, tarefa: Tarefa) {
    const lista = tarefas.value[workspaceId] ?? []
    const idx = lista.findIndex((t) => t.id === tarefa.id)
    if (idx !== -1) lista[idx] = tarefa
    else lista.push(tarefa)
    tarefas.value[workspaceId] = [...lista]
  }

  async function renomear(workspaceId: string, id: string, nome: string) {
    const t = await tarefaApi.renomear(id, nome)
    _atualizar(workspaceId, t)
  }

  async function concluir(workspaceId: string, id: string, date?: string) {
    const t = await tarefaApi.concluir(id, date)
    _atualizar(workspaceId, t)
  }

  async function reabrir(workspaceId: string, id: string, date?: string) {
    const t = await tarefaApi.reabrir(id, date)
    _atualizar(workspaceId, t)
  }

  async function deletar(workspaceId: string, id: string) {
    await tarefaApi.deletar(id)
    tarefas.value[workspaceId] = (tarefas.value[workspaceId] ?? []).filter((t) => t.id !== id)
  }

  async function atualizarDescricao(workspaceId: string, id: string, descricao: string | null) {
    const t = await tarefaApi.atualizarDescricao(id, descricao)
    _atualizar(workspaceId, t)
  }

  return { tarefas, carregar, criar, renomear, concluir, reabrir, deletar, atualizarDescricao }
})
