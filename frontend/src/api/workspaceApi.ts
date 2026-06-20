import http from './http'
import type { Workspace } from '../types'

export const workspaceApi = {
  listar: () => http.get<Workspace[]>('/workspaces').then((r) => r.data),
  criar: (nome: string, cor: string) => http.post<Workspace>('/workspaces', { nome, cor }).then((r) => r.data),
  atualizar: (id: string, nome: string, cor: string) =>
    http.put<Workspace>(`/workspaces/${id}`, { nome, cor }).then((r) => r.data),
  reordenar: (ids: string[]) => http.put('/workspaces/reorder', { ids }),
  deletar: (id: string) => http.delete(`/workspaces/${id}`),
}
