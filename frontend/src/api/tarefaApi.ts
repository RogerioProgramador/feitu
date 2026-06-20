import http from './http'
import type { Tarefa } from '../types'

export const tarefaApi = {
  listar: (workspaceId: string) =>
    http.get<Tarefa[]>(`/workspaces/${workspaceId}/tarefas`).then((r) => r.data),
  criar: (workspaceId: string, nome?: string) =>
    http.post<Tarefa>(`/workspaces/${workspaceId}/tarefas`, { nome }).then((r) => r.data),
  renomear: (id: string, nome: string) =>
    http.patch<Tarefa>(`/tarefas/${id}/nome`, { nome }).then((r) => r.data),
  iniciarTimer: (id: string) => http.post<Tarefa>(`/tarefas/${id}/iniciar`).then((r) => r.data),
  pausarTimer: (id: string) => http.post<Tarefa>(`/tarefas/${id}/pausar`).then((r) => r.data),
  pararTimer: (id: string) => http.post<Tarefa>(`/tarefas/${id}/parar`).then((r) => r.data),
  reativar: (id: string) => http.post<Tarefa>(`/tarefas/${id}/reativar`).then((r) => r.data),
  deletar: (id: string) => http.delete(`/tarefas/${id}`),
}
