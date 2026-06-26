import { test, expect } from '@playwright/test'
import { login } from '../helpers/login'

test('login inválido exibe mensagem de erro e permanece na tela', async ({ page }) => {
  await page.goto('/login')
  await page.fill('input[type="email"]', 'nao-existe@feitu.app')
  await page.fill('input[type="password"]', 'senha-errada-123')
  await page.click('button[type="submit"]')

  await expect(page.locator('p.text-red-500')).toBeVisible()
  await expect(page).not.toHaveURL(/workspaces/)
})

test('login válido navega para home', async ({ page }) => {
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) {
    test.skip(true, 'E2E_EMAIL / E2E_PASSWORD não definidos')
    return
  }

  await login(page)

  await expect(page).toHaveURL(/workspaces/)
})

test('rota protegida redireciona para /login sem autenticação', async ({ page }) => {
  // Contexto fresco (sem token, sem Service Worker de sessão anterior)
  // garante que o guard de rota do Vue funciona para usuários não autenticados
  await page.goto('/workspaces')

  await expect(page).toHaveURL(/\/login/)
})

test('back após login não retorna para /login', async ({ page }) => {
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) {
    test.skip(true, 'E2E_EMAIL / E2E_PASSWORD não definidos')
    return
  }

  await login(page)
  await page.goBack()

  await expect(page).not.toHaveURL(/\/login/)
})
