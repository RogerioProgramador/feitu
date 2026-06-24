<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSettingsStore } from '../stores/settingsStore'
import { useAuthStore } from '../stores/authStore'
import { usuarioApi } from '../api/usuarioApi'

const router = useRouter()
const settings = useSettingsStore()
const authStore = useAuthStore()

type Tema = 'claro' | 'escuro' | 'sistema'
const TEMAS: { value: Tema; label: string }[] = [
  { value: 'claro', label: 'Claro' },
  { value: 'escuro', label: 'Escuro' },
  { value: 'sistema', label: 'Sistema' },
]

const fusoOpen = ref(false)
const horarioNotificacao = ref<string | null>(null)

const fusoLabel = ref(settings.fusoHorario === 'BR' ? 'América / São Paulo' : 'UTC')

function setFuso(fuso: 'BR' | 'UTC') {
  settings.fusoHorario = fuso
  fusoLabel.value = fuso === 'BR' ? 'América / São Paulo' : 'UTC'
  fusoOpen.value = false
}

onMounted(async () => {
  try {
    const perfil = await usuarioApi.me()
    if (perfil.horarioNotificacao) {
      horarioNotificacao.value = perfil.horarioNotificacao.slice(0, 5)
    }
  } catch {
    // sem notificação configurada
  }
})

function sair() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg flex flex-col max-w-[512px] mx-auto">

    <!-- Header -->
    <div class="flex items-center gap-[12px] px-[18px] pt-[6px] pb-[14px]">
      <button
        @click="router.back()"
        class="w-[38px] h-[38px] flex items-center justify-center rounded-[12px] border border-[rgba(54,51,46,.1)] dark:border-[rgba(255,255,255,.06)] bg-[#F7F4EE] dark:bg-night-surface text-[#5E5A52] dark:text-night-text/60 hover:text-feitu-text dark:hover:text-night-text transition"
      >
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M15 5l-7 7 7 7"/>
        </svg>
      </button>
      <span class="text-[17px] font-semibold text-feitu-text dark:text-night-text leading-none">Configurações</span>
    </div>

    <div class="flex-1 overflow-y-auto px-[22px] pb-7 pt-[6px]">

      <!-- TEMA -->
      <p class="text-[12px] font-semibold text-[#8C857B] tracking-[.06em] uppercase mt-[10px] mb-[9px]">TEMA</p>
      <div
        class="flex gap-[6px] rounded-[14px] mb-[22px]"
        style="background: #F7F4EE; border: 1px solid rgba(54,51,46,.07); padding: 5px;"
      >
        <button
          v-for="t in TEMAS"
          :key="t.value"
          @click="settings.tema = t.value"
          class="flex-1 rounded-[10px] border-none cursor-pointer transition-all"
          style="padding: 11px; font: 600 13.5px 'Space Grotesk';"
          :style="settings.tema === t.value
            ? 'background: #5E8BB6; color: #fff;'
            : 'background: transparent; color: #8C857B;'"
        >{{ t.label }}</button>
      </div>

      <!-- FUSO HORÁRIO -->
      <p class="text-[12px] font-semibold text-[#8C857B] tracking-[.06em] uppercase mt-[10px] mb-[9px]">FUSO HORÁRIO</p>
      <div class="relative mb-[14px]">
        <button
          @click="fusoOpen = !fusoOpen"
          class="w-full flex items-center justify-between rounded-[14px] border border-[rgba(54,51,46,.07)] bg-[#F7F4EE] dark:bg-night-surface dark:border-[rgba(255,255,255,.05)] cursor-pointer"
          style="padding: 14px 16px;"
        >
          <span style="font: 500 15px 'Space Grotesk'; color: #36332E;" class="dark:text-night-text">{{ fusoLabel }}</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#8C857B" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M8 9l4 4 4-4"/>
          </svg>
        </button>
        <!-- Dropdown -->
        <div
          v-if="fusoOpen"
          class="absolute left-0 right-0 top-full mt-1 rounded-[14px] border border-[rgba(54,51,46,.07)] bg-[#F7F4EE] dark:bg-night-surface shadow-[0_8px_20px_-8px_rgba(54,51,46,.2)] z-10 overflow-hidden"
        >
          <button
            @click="setFuso('BR')"
            class="w-full text-left border-none cursor-pointer hover:bg-[rgba(54,51,46,.04)] dark:hover:bg-night-card transition"
            style="padding: 13px 16px; font: 500 14px 'Space Grotesk'; color: #36332E;"
            :style="settings.fusoHorario === 'BR' ? 'color: #5E8BB6; font-weight: 600;' : ''"
          >América / São Paulo</button>
          <div style="height: 1px; background: rgba(54,51,46,.07);"></div>
          <button
            @click="setFuso('UTC')"
            class="w-full text-left border-none cursor-pointer hover:bg-[rgba(54,51,46,.04)] dark:hover:bg-night-card transition"
            style="padding: 13px 16px; font: 500 14px 'Space Grotesk'; color: #36332E;"
            :style="settings.fusoHorario === 'UTC' ? 'color: #5E8BB6; font-weight: 600;' : ''"
          >UTC</button>
        </div>
      </div>

      <!-- NOTIFICAÇÃO DIÁRIA -->
      <p class="text-[12px] font-semibold text-[#8C857B] tracking-[.06em] uppercase mt-[18px] mb-[9px]">NOTIFICAÇÃO DIÁRIA</p>
      <div
        class="flex items-center justify-between rounded-[14px] border border-[rgba(54,51,46,.07)] bg-[#F7F4EE] dark:bg-night-surface dark:border-[rgba(255,255,255,.05)] mb-[22px]"
        style="padding: 14px 16px;"
      >
        <span style="font: 500 15px 'Space Grotesk'; color: #36332E;" class="dark:text-night-text">Lembrete do dia</span>
        <span style="font: 600 16px 'Space Grotesk'; color: #5E8BB6;">{{ horarioNotificacao ?? '08:00' }}</span>
      </div>

      <!-- CONTA -->
      <p class="text-[12px] font-semibold text-[#8C857B] tracking-[.06em] uppercase mt-[10px] mb-[9px]">CONTA</p>
      <div
        class="flex flex-col gap-[14px] rounded-[14px] border border-[rgba(54,51,46,.07)] bg-[#F7F4EE] dark:bg-night-surface dark:border-[rgba(255,255,255,.05)]"
        style="padding: 16px;"
      >
        <div class="flex items-center gap-[12px]">
          <div
            class="w-[42px] h-[42px] rounded-full flex items-center justify-center flex-shrink-0"
            style="background: #EAF1F8;"
          >
            <span style="font: 600 16px 'Space Grotesk'; color: #5E8BB6;">
              {{ authStore.email?.[0]?.toUpperCase() ?? '?' }}
            </span>
          </div>
          <span style="font: 400 13px/1.2 'Space Grotesk'; color: #8C857B;">{{ authStore.email }}</span>
        </div>
        <button
          @click="sair"
          class="w-full rounded-[11px] border cursor-pointer transition"
          style="padding: 11px; font: 600 14px 'Space Grotesk'; color: #E07B4F; border-color: rgba(224,123,79,.4); background: transparent;"
        >Sair</button>
      </div>

    </div>
  </div>
</template>
