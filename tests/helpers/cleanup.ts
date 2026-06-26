import { request } from '@playwright/test'

interface TaskResponse {
  id: string
  nome: string
}

interface WorkspaceResponse {
  id: string
}

/**
 * Retorna hoje no fuso America/Sao_Paulo (UTC-3), igual ao hojeISO() do frontend.
 * O app cria tarefas com essa data — o cleanup precisa usar a mesma referência.
 */
export function hojeUTC3(): string {
  return new Date().toLocaleDateString('sv-SE', { timeZone: 'America/Sao_Paulo' })
}

/**
 * Remove via API todas as tarefas cujo nome começa com 'E2E-'.
 * Chamado no global-setup para garantir estado limpo antes de cada run.
 */
export async function limparTarefasE2E(): Promise<void> {
  const baseURL = process.env.BASE_URL ?? 'http://localhost:8080'
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) return

  const api = await request.newContext({ baseURL })
  try {
    const loginRes = await api.post('/api/auth/login', { data: { email, senha } })
    if (!loginRes.ok()) return
    const { token } = (await loginRes.json()) as { token: string }
    const headers = { Authorization: `Bearer ${token}` }

    const wsRes = await api.get('/api/workspaces', { headers })
    if (!wsRes.ok()) return
    const workspaces = (await wsRes.json()) as WorkspaceResponse[]

    // usa a data em UTC-3, igual ao hojeISO() do frontend
    const date = hojeUTC3()

    for (const ws of workspaces) {
      const tasksRes = await api.get(`/api/workspaces/${ws.id}/tarefas`, { headers, params: { date } })
      if (!tasksRes.ok()) continue
      const tasks = (await tasksRes.json()) as TaskResponse[]
      for (const task of tasks) {
        if (task.nome.startsWith('E2E-')) {
          await api.delete(`/api/tarefas/${task.id}`, { headers })
        }
      }
    }
  } finally {
    await api.dispose()
  }
}
