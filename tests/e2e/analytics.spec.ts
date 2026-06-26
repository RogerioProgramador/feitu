import { test, expect, request } from '@playwright/test'
import { login } from '../helpers/login'

async function deletarTarefaPorNome(nome: string): Promise<void> {
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
    const workspaces = (await wsRes.json()) as { id: string }[]

    for (const ws of workspaces) {
      const tasksRes = await api.get(`/api/workspaces/${ws.id}/tarefas`, { headers })
      if (!tasksRes.ok()) continue
      const tasks = (await tasksRes.json()) as { id: string; nome: string }[]
      for (const task of tasks) {
        if (task.nome === nome) {
          await api.delete(`/api/tarefas/${task.id}`, { headers })
        }
      }
    }
  } finally {
    await api.dispose()
  }
}

test('analytics renderiza corretamente após navegação autenticada', async ({ page }) => {
  const jsErrors: string[] = []
  page.on('pageerror', (e) => jsErrors.push(e.message))

  await login(page)
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  expect(jsErrors).toHaveLength(0)
})

test('overflow horizontal ausente na página de analytics', async ({ page }) => {
  await login(page)
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  const overflow = await page.evaluate(() => document.body.scrollWidth - window.innerWidth)
  expect(overflow).toBeLessThanOrEqual(2)
})

test('analytics exibe estado vazio quando não há tarefas no dia', async ({ page }) => {
  await login(page)
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  await expect(page.getByText('Nenhuma tarefa neste dia')).toBeVisible()
})

test('analytics exibe estado parcial quando há tarefas pendentes', async ({ page }) => {
  const nome = `E2E-parcial-${Date.now()}`
  await login(page)

  try {
    await page.getByText('Nova tarefa').click()
    await page.getByPlaceholder('Nome da tarefa...').fill(nome)
    await page.keyboard.press('Enter')
    await expect(page.getByText(nome)).toBeVisible()

    await page.locator('a[title="Resumo"]').click()
    await page.waitForURL('**/analytics')

    await expect(page.getByText(/de \d+ tarefas concluídas/)).toBeVisible()
  } finally {
    // cleanup via API garante remoção mesmo se a assertion falhou
    await deletarTarefaPorNome(nome)
  }
})

test('analytics exibe estado tudo feito quando todas as tarefas são concluídas', async ({ page }) => {
  const nome = `E2E-tudo-${Date.now()}`
  await login(page)

  try {
    await page.getByText('Nova tarefa').click()
    await page.getByPlaceholder('Nome da tarefa...').fill(nome)
    await page.keyboard.press('Enter')
    await expect(page.getByText(nome)).toBeVisible()

    const row = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
    const checkbox = row.getByRole('button').first()
    await checkbox.click()
    await expect(checkbox).toHaveClass(/bg-feitu-teal/)
    // aguarda PATCH de toggle chegar ao banco antes de navegar para analytics
    await page.waitForLoadState('networkidle')

    await page.locator('a[title="Resumo"]').click()
    await page.waitForURL('**/analytics')

    await expect(page.getByText('Tudo feito hoje.')).toBeVisible()
  } finally {
    // cleanup via API garante remoção mesmo se a assertion falhou
    await deletarTarefaPorNome(nome)
  }
})
