import type { Page } from '@playwright/test'

export async function login(page: Page): Promise<void> {
  const email = process.env.E2E_EMAIL
  const senha = process.env.E2E_PASSWORD
  if (!email || !senha) {
    throw new Error('Defina E2E_EMAIL e E2E_PASSWORD (veja tests/.env.example)')
  }
  await page.goto('/login')
  await page.fill('input[type="email"]', email)
  await page.fill('input[type="password"]', senha)
  await page.click('button[type="submit"]')
  await page.waitForURL('**/workspaces')
}
