<template>
  <div class="min-h-screen flex flex-col bg-dark">
    <AppHeader />
    <main class="flex-1">
      <router-view />
    </main>
    <AppFooter />
    <CompareBar />
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import CompareBar from '@/components/robot/CompareBar.vue'
</script>
