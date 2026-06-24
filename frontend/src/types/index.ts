export type TipoTarefa = 'PONTUAL' | 'RECORRENTE'

export interface Workspace {
  id: string
  nome: string
  cor: string | null
  ordem: number
}

export interface Tarefa {
  id: string
  nome: string
  tipo: TipoTarefa
  concluida: boolean
  concluidaEm: string | null
  data: string | null
  diasSemana: string[]
  horario: string | null
  criadoEm: string
  descricao?: string | null
}

export interface TarefaCreateRequest {
  nome?: string
  descricao?: string | null
  tipo?: TipoTarefa
  diasSemana?: string[]
  horario?: string | null
}

export interface Usuario {
  id: string
  email: string
  horarioNotificacao: string
}

export interface TarefaItemResponse {
  id: string
  nome: string
  concluida: boolean
  tipo: TipoTarefa
}

export interface DailySummary {
  data: string
  totalTarefas: number
  concluidas: number
  recorrentes: TarefaItemResponse[]
  pontuais: TarefaItemResponse[]
}
