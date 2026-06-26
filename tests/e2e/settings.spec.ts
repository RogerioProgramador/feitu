import { test, expect } from '@playwright/test'
import { login } from '../helpers/login'

test.describe('configurações — workspaces', () => {
  test('cria workspace e aparece na lista', async ({ page }) => {
    await login(page)
    await page.goto('/settings')

    const nome = `CI-WS-${Date.now()}`

    await page.getByText('Criar workspace').click()
    await page.getByPlaceholder('Nome do workspace...').fill(nome)
    await page.keyboard.press('Enter')

    await expect(page.getByText(nome)).toBeVisible()

    // Cleanup: exclui o workspace criado
    const wsRow = page.locator('div.flex.items-center').filter({ hasText: nome })
    await wsRow.getByRole('button').click()
    await page.getByRole('button', { name: 'Deletar' }).click()

    await expect(page.getByText(nome)).not.toBeVisible()
  })
})

test('logout redireciona para /login', async ({ page }) => {
  await login(page)
  await page.goto('/settings')
  await page.getByText('Sair').click()

  await expect(page).toHaveURL(/\/login/)
})
