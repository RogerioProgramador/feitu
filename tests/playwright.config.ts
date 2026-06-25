import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  workers: 1,           // testes compartilham um único usuário de teste — sequencial evita conflitos
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? 'github' : 'html',
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:8080',
    ...devices['iPhone 14'],  // viewport mobile-first (390×844, touch, isMobile)
    browserName: 'chromium',  // webkit não está disponível no runner de CI; chromium é suficiente para smoke
    locale: 'pt-BR',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
})
