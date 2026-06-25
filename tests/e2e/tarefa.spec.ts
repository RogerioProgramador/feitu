import { test, expect } from '@playwright/test'
import { login } from '../helpers/login'

test.describe('fluxo de tarefas', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)

    const semWorkspace = await page.getByText('Crie um workspace').isVisible().catch(() => false)
    test.skip(semWorkspace, 'Usuário de teste precisa ter ao menos 1 workspace configurado')
  })

  test('cria tarefa pontual via quick-add e exclui', async ({ page }) => {
    const nome = `E2E-${Date.now()}`

    // Abre inline quick-add
    await page.getByText('Nova tarefa').click()
    await page.getByPlaceholder('Nome da tarefa...').fill(nome)
    await page.keyboard.press('Enter')

    // Tarefa aparece na lista
    await expect(page.getByText(nome)).toBeVisible()

    // Exclui via ⋮ → "Excluir tarefa"
    const row = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
    await row.getByRole('button').last().click()
    await page.getByText('Excluir tarefa').first().click()

    await expect(page.getByText(nome)).not.toBeVisible()
  })

  test('marca tarefa como feita e reabre via checkbox', async ({ page }) => {
    const nome = `E2E-done-${Date.now()}`

    await page.getByText('Nova tarefa').click()
    await page.getByPlaceholder('Nome da tarefa...').fill(nome)
    await page.keyboard.press('Enter')

    const row = page.locator('div.flex.items-center').filter({ hasText: nome }).first()
    const checkbox = row.getByRole('button').first()

    // Tarefa começa sem concluída
    await expect(checkbox).not.toHaveClass(/bg-feitu-teal/)

    // Marca como feita
    await checkbox.click()
    await expect(checkbox).toHaveClass(/bg-feitu-teal/)

    // Reabre
    await checkbox.click()
    await expect(checkbox).not.toHaveClass(/bg-feitu-teal/)

    // Cleanup
    await row.getByRole('button').last().click()
    await page.getByText('Excluir tarefa').first().click()
  })
})
