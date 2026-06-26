import { test, expect } from '@playwright/test'
import { login } from '../helpers/login'

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
  await login(page)

  const nome = `E2E-parcial-${Date.now()}`
  await page.getByText('Nova tarefa').click()
  await page.getByPlaceholder('Nome da tarefa...').fill(nome)
  await page.keyboard.press('Enter')
  await expect(page.getByText(nome)).toBeVisible()

  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  await expect(page.getByText(/de \d+ tarefas concluídas/)).toBeVisible()

  // Cleanup
  await page.goto('/workspaces')
  const row = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
  await row.getByRole('button').last().click()
  await page.getByText('Excluir tarefa').first().click()
})

test('analytics exibe estado tudo feito quando todas as tarefas são concluídas', async ({ page }) => {
  await login(page)

  const nome = `E2E-tudo-${Date.now()}`
  await page.getByText('Nova tarefa').click()
  await page.getByPlaceholder('Nome da tarefa...').fill(nome)
  await page.keyboard.press('Enter')
  await expect(page.getByText(nome)).toBeVisible()

  const row = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
  const checkbox = row.getByRole('button').first()
  await checkbox.click()
  await expect(checkbox).toHaveClass(/bg-feitu-teal/)
  // aguarda o PATCH de toggle chegar ao banco antes de abrir analytics
  await page.waitForLoadState('networkidle')

  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  await expect(page.getByText('Tudo feito hoje.')).toBeVisible()

  // Cleanup
  await page.goto('/workspaces')
  const rowCleanup = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
  await rowCleanup.getByRole('button').last().click()
  await page.getByText('Excluir tarefa').first().click()
})
