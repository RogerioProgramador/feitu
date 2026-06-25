<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSettingsStore } from '../stores/settingsStore'
import { useAuthStore } from '../stores/authStore'
import { useWorkspaceStore } from '../stores/workspaceStore'

const router = useRouter()
const settings = useSettingsStore()
const authStore = useAuthStore()
const wsStore = useWorkspaceStore()

import type { Tema } from '../types'

const TEMAS: { value: Tema; label: string }[] = [
  { value: 'claro', label: 'Claro' },
  { value: 'escuro', label: 'Escuro' },
  { value: 'sistema', label: 'Sistema' },
]

const fusoOpen = ref(false)
const criandoWs = ref(false)
const nomeNovoWs = ref('')
const confirmandoDeletar = ref<string | null>(null)

const CORES = ['#A7C7E7', '#B5EAD7', '#FFDAC1', '#E2C6FF', '#FFD1DC', '#C7E6A7']

const fusoLabel = ref(settings.fusoHorario === 'BR' ? 'América / São Paulo' : 'UTC')

function setFuso(fuso: 'BR' | 'UTC') {
  settings.fusoHorario = fuso
  fusoLabel.value = fuso === 'BR' ? 'América / São Paulo' : 'UTC'
  fusoOpen.value = false
}

function proximaCor() {
  return CORES[wsStore.workspaces.length % CORES.length]
}

async function criarWs() {
  const nome = nomeNovoWs.value.trim()
  if (!nome) { criandoWs.value = false; return }
  await wsStore.criar(nome, proximaCor())
  nomeNovoWs.value = ''
  criandoWs.value = false
}

function cancelarCriarWs() {
  nomeNovoWs.value = ''
  criandoWs.value = false
}

async function deletarWs(id: string) {
  await wsStore.deletar(id)
  confirmandoDeletar.value = null
}

onMounted(async () => {
  if (!wsStore.workspaces.length) await wsStore.carregar()
  await authStore.carregarPerfil()
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

      <!-- WORKSPACES -->
      <div class="flex items-center justify-between mt-[10px] mb-[9px]">
        <p class="text-[12px] font-semibold text-[#8C857B] tracking-[.06em] uppercase">WORKSPACES</p>
        <span style="font: 500 12px 'Space Grotesk'; color: #8C857B;">{{ wsStore.workspaces.length }} / 3</span>
      </div>

      <div
        v-if="wsStore.workspaces.length"
        class="rounded-[14px] border border-[rgba(54,51,46,.07)] dark:border-[rgba(255,255,255,.05)] bg-[#F7F4EE] dark:bg-night-surface overflow-hidden mb-[10px]"
      >
        <template v-for="(ws, i) in wsStore.workspaces" :key="ws.id">
          <!-- Confirmação de delete -->
          <div v-if="confirmandoDeletar === ws.id" class="px-[16px] py-[13px]">
            <p style="font: 400 13px/1.4 'Space Grotesk'; color: #6E6A62;" class="mb-[12px]">Apaga todas as tarefas. Tem certeza?</p>
            <div class="flex gap-[8px]">
              <button
                @click="confirmandoDeletar = null"
                style="flex: 1; padding: 9px; font: 600 13px 'Space Grotesk'; color: #6E6A62; border: 1px solid rgba(54,51,46,.14); border-radius: 10px; background: transparent; cursor: pointer;"
              >Cancelar</button>
              <button
                @click="deletarWs(ws.id)"
                style="flex: 1; padding: 9px; font: 600 13px 'Space Grotesk'; color: #E07B4F; border: 1px solid rgba(224,123,79,.4); border-radius: 10px; background: transparent; cursor: pointer;"
              >Deletar</button>
            </div>
          </div>
          <!-- Linha normal -->
          <div v-else class="flex items-center gap-[12px] px-[16px] py-[13px]">
            <span class="w-[11px] h-[11px] rounded-full flex-shrink-0" :style="{ backgroundColor: ws.cor ?? '#A7C7E7' }"></span>
            <span class="flex-1 min-w-0 truncate" style="font: 500 14.5px 'Space Grotesk'; color: #36332E;" :class="'dark:text-night-text'">{{ ws.nome }}</span>
            <button
              @click="confirmandoDeletar = ws.id"
              class="flex-shrink-0 transition hover:text-[#E07B4F]"
              style="color: #C4BDB1; background: transparent; border: none; cursor: pointer; display: flex; align-items: center;"
            >
              <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 7h14M9 7V5h6v2M7 7l1 12h8l1-12"/>
              </svg>
            </button>
          </div>
          <div v-if="i < wsStore.workspaces.length - 1" style="height: 1px; background: rgba(54,51,46,.06); margin: 0 16px;"></div>
        </template>
      </div>

      <!-- Input de criação inline -->
      <div v-if="criandoWs" class="mb-[10px]">
        <input
          v-focus
          v-model="nomeNovoWs"
          @keydown.enter="criarWs"
          @keydown.esc="cancelarCriarWs"
          placeholder="Nome do workspace..."
          style="width: 100%; padding: 13px 16px; font: 500 15px 'Space Grotesk'; color: #36332E; border: 1.5px solid #5E8BB6; border-radius: 14px; background: #fff; outline: none; box-shadow: 0 0 0 4px rgba(94,139,182,.12); box-sizing: border-box;"
        />
        <div class="flex gap-[8px] mt-[8px]">
          <button
            @click="cancelarCriarWs"
            style="flex: 1; padding: 12px; font: 600 14px 'Space Grotesk'; color: #6E6A62; border: 1px solid rgba(54,51,46,.14); border-radius: 13px; background: transparent; cursor: pointer;"
          >Cancelar</button>
          <button
            @click="criarWs"
            style="flex: 1; padding: 12px; font: 600 14px 'Space Grotesk'; color: #fff; background: #5E8BB6; border: none; border-radius: 13px; cursor: pointer; box-shadow: 0 10px 24px -10px rgba(94,139,182,.6);"
          >Criar</button>
        </div>
      </div>
      <!-- Botão dashed "Criar workspace" -->
      <button
        v-else
        @click="wsStore.workspaces.length < 3 ? (criandoWs = true) : null"
        :disabled="wsStore.workspaces.length >= 3"
        class="w-full mb-[8px] flex items-center justify-center gap-[8px] rounded-[13px] transition"
        style="padding: 13px; border: 1.5px dashed rgba(54,51,46,.16); font: 600 14px 'Space Grotesk'; color: #B0A99D; cursor: pointer; background: transparent;"
        :style="wsStore.workspaces.length >= 3 ? 'opacity: 0.45; cursor: not-allowed;' : ''"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
        Criar workspace
      </button>
      <p style="font: 400 12.5px/1.5 'Space Grotesk'; color: #A49C90;" class="mb-[22px]">
        Você pode ter até <strong style="color: #6E6A62; font-weight: 600;">3 workspaces</strong>. Remova um para criar outro.
      </p>

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
        <span style="font: 600 16px 'Space Grotesk'; color: #5E8BB6;">{{ authStore.horarioNotificacao ?? '08:00' }}</span>
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
