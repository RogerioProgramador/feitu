import { test, expect } from '@playwright/test'

test('login inválido exibe mensagem de erro e permanece na tela', async ({ page }) => {
  await page.goto('/login')
  await page.fill('input[type="email"]', 'nao-existe@feitu.app')
  await page.fill('input[type="password"]', 'senha-errada-123')
  await page.click('button[type="submit"]')

  await expect(page.locator('p.text-red-500')).toBeVisible()
  expect(page.url()).not.toContain('/workspaces')
})

test('login válido navega para home', async ({ page }) => {
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) {
    test.skip(true, 'E2E_EMAIL / E2E_PASSWORD não definidos')
    return
  }

  await page.goto('/login')
  await page.fill('input[type="email"]', email)
  await page.fill('input[type="password"]', senha)
  await page.click('button[type="submit"]')

  await page.waitForURL('**/workspaces')
  expect(page.url()).toContain('/workspaces')
})

test('back após login não retorna para /login', async ({ page }) => {
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) {
    test.skip(true, 'E2E_EMAIL / E2E_PASSWORD não definidos')
    return
  }

  await page.goto('/login')
  await page.fill('input[type="email"]', email)
  await page.fill('input[type="password"]', senha)
  await page.click('button[type="submit"]')
  await page.waitForURL('**/workspaces')

  await page.goBack()

  await expect(page).not.toHaveURL(/\/login/)
})
