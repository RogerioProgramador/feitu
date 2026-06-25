import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { Tema } from '../types'

export const useSettingsStore = defineStore('settings', () => {
  const tema = ref<Tema>((localStorage.getItem('feitu_tema') as Tema) ?? 'sistema')
  const fusoHorario = ref<'BR' | 'UTC'>(
    (localStorage.getItem('feitu_fuso') as 'BR' | 'UTC') ?? 'BR',
  )

  function aplicarTema(t?: Tema) {
    const atual = t ?? tema.value
    if (atual === 'escuro') {
      document.documentElement.classList.add('dark')
    } else if (atual === 'claro') {
      document.documentElement.classList.remove('dark')
    } else {
      const prefereDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      document.documentElement.classList.toggle('dark', prefereDark)
    }
  }

  watch(tema, (val) => {
    localStorage.setItem('feitu_tema', val)
    aplicarTema(val)
  })

  watch(fusoHorario, (val) => {
    localStorage.setItem('feitu_fuso', val)
  })

  // Reage a mudanças de sistema ao vivo
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
    if (tema.value === 'sistema') aplicarTema()
  })

  aplicarTema()

  return { tema, fusoHorario, aplicarTema }
})
