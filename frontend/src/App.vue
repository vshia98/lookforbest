<template>
  <router-view />
  <AiChatWidget />
  <LoginModal
    :visible="favoritesStore.showLoginModal"
    title="收藏需要登录"
    message="登录后即可收藏喜欢的机器人"
    @close="favoritesStore.showLoginModal = false"
    @success="favoritesStore.showLoginModal = false"
  />
</template>

<script setup lang="ts">
import AiChatWidget from '@/components/ai/AiChatWidget.vue'
import LoginModal from '@/components/ui/LoginModal.vue'
import { useFavoritesStore } from '@/stores/favorites'

const favoritesStore = useFavoritesStore()
</script>
