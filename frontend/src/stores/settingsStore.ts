import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const modoNoturno = ref(localStorage.getItem('feitu_dark') === 'true')
  const fusoHorario = ref<'BR' | 'UTC'>(
    (localStorage.getItem('feitu_fuso') as 'BR' | 'UTC') ?? 'BR'
  )

  function aplicarTema() {
    document.documentElement.classList.toggle('dark', modoNoturno.value)
  }

  watch(modoNoturno, (val) => {
    localStorage.setItem('feitu_dark', String(val))
    aplicarTema()
  })

  watch(fusoHorario, (val) => {
    localStorage.setItem('feitu_fuso', val)
  })

  aplicarTema()

  return { modoNoturno, fusoHorario }
})
