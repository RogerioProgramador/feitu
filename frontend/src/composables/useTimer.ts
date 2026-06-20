import { ref, watch, onUnmounted } from 'vue'
import type { TarefaEstado } from '../types'

export function useTimer(
  tempoInicial: () => number,
  estado: () => TarefaEstado,
) {
  const segundos = ref(tempoInicial())
  let interval: ReturnType<typeof setInterval> | null = null
  let inicioLocal = 0

  function parar() {
    if (interval !== null) {
      clearInterval(interval)
      interval = null
    }
  }

  function iniciar(base: number) {
    parar()
    inicioLocal = Date.now()
    interval = setInterval(() => {
      segundos.value = base + Math.floor((Date.now() - inicioLocal) / 1000)
    }, 1000)
  }

  watch(
    [() => estado(), () => tempoInicial()],
    ([novoEstado, novoBase]) => {
      segundos.value = novoBase
      if (novoEstado === 'RUNNING') iniciar(novoBase)
      else parar()
    },
    { immediate: true },
  )

  onUnmounted(parar)

  return { segundos }
}
