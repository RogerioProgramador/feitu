import { defineStore } from 'pinia'
import { ref } from 'vue'
import { workspaceApi } from '../api/workspaceApi'
import type { Workspace } from '../types'

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const ativoId = ref<string | null>(null)
  const criandoNovo = ref(false)

  async function carregar() {
    workspaces.value = await workspaceApi.listar()
    if (workspaces.value.length && !ativoId.value) {
      ativoId.value = workspaces.value[0].id
    }
  }

  async function criar(nome: string, cor: string) {
    const ws = await workspaceApi.criar(nome, cor)
    workspaces.value.push(ws)
    ativoId.value = ws.id
  }

  async function atualizar(id: string, nome: string, cor: string) {
    const ws = await workspaceApi.atualizar(id, nome, cor)
    const idx = workspaces.value.findIndex((w) => w.id === id)
    if (idx !== -1) workspaces.value[idx] = ws
  }

  async function reordenar(ids: string[]) {
    await workspaceApi.reordenar(ids)
    workspaces.value.sort((a, b) => ids.indexOf(a.id) - ids.indexOf(b.id))
  }

  async function deletar(id: string) {
    await workspaceApi.deletar(id)
    workspaces.value = workspaces.value.filter((w) => w.id !== id)
    if (ativoId.value === id) {
      ativoId.value = workspaces.value[0]?.id ?? null
    }
  }

  function selecionar(id: string) {
    ativoId.value = id
  }

  return { workspaces, ativoId, criandoNovo, carregar, criar, atualizar, reordenar, deletar, selecionar }
})
