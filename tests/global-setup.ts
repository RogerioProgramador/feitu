import { request } from '@playwright/test'

export default async function globalSetup(): Promise<void> {
  const baseURL = process.env.BASE_URL ?? 'http://localhost:8080'
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD

  if (!email || !senha) return

  const api = await request.newContext({ baseURL })

  const loginRes = await api.post('/api/auth/login', { data: { email, senha } })
  if (!loginRes.ok()) {
    throw new Error(`global-setup: login CI falhou (${loginRes.status()})`)
  }
  const { token } = (await loginRes.json()) as { token: string }

  const wsRes = await api.get('/api/workspaces', {
    headers: { Authorization: `Bearer ${token}` },
  })
  const workspaces = (await wsRes.json()) as { id: string }[]

  if (workspaces.length === 0) {
    const createRes = await api.post('/api/workspaces', {
      data: { nome: 'CI', cor: '#A7C7E7' },
      headers: { Authorization: `Bearer ${token}` },
    })
    if (!createRes.ok()) {
      throw new Error(`global-setup: falha ao criar workspace CI (${createRes.status()})`)
    }
  }

  await api.dispose()
}
