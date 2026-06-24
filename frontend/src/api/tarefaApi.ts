import http from './http'
import type { Tarefa, TarefaCreateRequest } from '../types'

export const tarefaApi = {
  listar: (workspaceId: string, date?: string) =>
    http
      .get<Tarefa[]>(`/workspaces/${workspaceId}/tarefas`, {
        params: date ? { date } : {},
      })
      .then((r) => r.data),

  criar: (workspaceId: string, req: TarefaCreateRequest, date?: string) =>
    http
      .post<Tarefa>(`/workspaces/${workspaceId}/tarefas`, req, {
        params: date ? { date } : {},
      })
      .then((r) => r.data),

  renomear: (id: string, nome: string) =>
    http.patch<Tarefa>(`/tarefas/${id}/nome`, { nome }).then((r) => r.data),

  concluir: (id: string, date?: string) =>
    http
      .post<Tarefa>(`/tarefas/${id}/concluir`, null, {
        params: date ? { date } : {},
      })
      .then((r) => r.data),

  reabrir: (id: string, date?: string) =>
    http
      .post<Tarefa>(`/tarefas/${id}/reabrir`, null, {
        params: date ? { date } : {},
      })
      .then((r) => r.data),

  deletar: (id: string) => http.delete(`/tarefas/${id}`),

  atualizarDescricao: (id: string, descricao: string | null) =>
    http.patch<Tarefa>(`/tarefas/${id}/descricao`, { descricao }).then((r) => r.data),
}
