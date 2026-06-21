<script setup lang="ts">
import { useSettingsStore } from './stores/settingsStore'
useSettingsStore()
</script>

<template>
  <div class="min-h-screen bg-feitu-bg dark:bg-night-bg">
    <RouterView />
  </div>
</template>
