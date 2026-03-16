<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-white mb-6">我的收藏</h1>

    <LoadingSpinner v-if="favoritesStore.loading" text="加载收藏..." />

    <div v-else-if="favoritesStore.favorites.length === 0" class="text-center py-20">
      <div class="text-5xl mb-4">⭐</div>
      <p class="text-gray-500 mb-6">还没有收藏任何机器人</p>
      <router-link to="/robots" class="bg-primary text-[#1a1a1a] px-6 py-2.5 rounded-xl text-sm hover:bg-primary transition-colors">
        浏览机器人库
      </router-link>
    </div>

    <div v-else>
      <p class="text-gray-500 mb-5 text-sm">共 {{ favoritesStore.favorites.length }} 个收藏</p>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <RobotCard v-for="robot in favoritesStore.favorites" :key="robot.id" :robot="robot" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import RobotCard from '@/components/robot/RobotCard.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import { useFavoritesStore } from '@/stores/favorites'

const favoritesStore = useFavoritesStore()

onMounted(() => favoritesStore.fetchFavorites())
</script>
