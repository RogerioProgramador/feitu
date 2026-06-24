import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

const isTesting = process.env.VITEST === 'true' || process.env.NODE_ENV === 'test'

export default defineConfig({
  plugins: [
    vue(),
    ...(isTesting ? [] : [
      VitePWA({
        registerType: 'autoUpdate',
        includeAssets: ['favicon.svg'],
        pwaAssets: {
          preset: 'minimal',
          image: 'public/favicon.svg',
        },
        manifest: {
          name: 'Feitu',
          short_name: 'Feitu',
          description: 'Checklist diário com timer integrado',
          theme_color: '#F9F6F0',
          background_color: '#F9F6F0',
          display: 'standalone',
          start_url: '/',
          icons: [
            { src: 'pwa-192x192.png', sizes: '192x192', type: 'image/png', purpose: 'any' },
            { src: 'pwa-512x512.png', sizes: '512x512', type: 'image/png', purpose: 'any maskable' },
          ],
        },
        workbox: {
          navigateFallback: '/index.html',
          runtimeCaching: [
            {
              urlPattern: /^\/api\//,
              handler: 'NetworkFirst',
              options: { cacheName: 'api-cache' },
            },
          ],
        },
      }),
    ]),
  ],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    env: {
      VITEST: 'true',
    },
  },
})
