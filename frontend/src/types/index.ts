export type TarefaEstado = 'IDLE' | 'RUNNING' | 'PAUSED' | 'DONE'

export interface Workspace {
  id: string
  nome: string
  cor: string | null
  ordem: number
}

export interface Tarefa {
  id: string
  nome: string
  estado: TarefaEstado
  tempoTotalSegundos: number
  criadoEm: string
  concluidoEm: string | null
  descricao?: string | null
}

export interface Usuario {
  id: string
  email: string
  horarioNotificacao: string
}

export interface WorkspaceSummary {
  workspaceId: string
  nome: string
  cor: string | null
  segundos: number
}

export interface TimelineItem {
  tarefaId: string
  tarefaNome: string
  workspaceCor: string | null
  inicio: string
  fim: string
}

export interface TarefaResumo {
  id: string
  nome: string
  segundos: number
}

export interface DailySummary {
  data: string
  totalSegundos: number
  porcWorkspace: WorkspaceSummary[]
  timeline: TimelineItem[]
  tarefaMaisLonga: TarefaResumo | null
}
