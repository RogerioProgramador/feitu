import { test, expect } from '@playwright/test'
import { login } from '../helpers/login'

test('analytics carrega sem erros de JavaScript', async ({ page }) => {
  const jsErrors: string[] = []
  page.on('pageerror', (e) => jsErrors.push(e.message))

  await login(page)

  // Navega via ícone de resumo no header
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  expect(page.url()).toContain('/analytics')
  expect(jsErrors).toHaveLength(0)
})

test('overflow horizontal ausente na página de analytics', async ({ page }) => {
  await login(page)
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  const overflow = await page.evaluate(
    () => document.body.scrollWidth - window.innerWidth,
  )
  expect(overflow).toBeLessThanOrEqual(2)
})

test('analytics exibe estado vazio quando não há tarefas no dia', async ({ page }) => {
  await login(page)
  await page.locator('a[title="Resumo"]').click()
  await page.waitForURL('**/analytics')

  await expect(page.getByText('Nenhuma tarefa neste dia')).toBeVisible()
})
