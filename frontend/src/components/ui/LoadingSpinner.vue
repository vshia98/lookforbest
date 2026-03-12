<template>
  <div class="flex items-center justify-center py-16">
    <div class="flex flex-col items-center gap-3">
      <div class="w-10 h-10 border-4 border-gray-200 border-t-primary-500 rounded-full animate-spin"></div>
      <p v-if="text" class="text-sm text-gray-500">{{ text }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ text?: string }>()
</script>
