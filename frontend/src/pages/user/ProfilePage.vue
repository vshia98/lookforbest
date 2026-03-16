<template>
  <div class="max-w-2xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-white mb-8">个人资料</h1>

    <div class="bg-dark-50 rounded-2xl border border-white/[0.04]  p-8">
      <div class="flex items-center gap-5 mb-8">
        <div class="w-16 h-16 bg-primary-100 text-primary rounded-full flex items-center justify-center text-2xl font-bold">
          {{ authStore.user?.username?.charAt(0).toUpperCase() }}
        </div>
        <div>
          <p class="font-semibold text-white text-lg">{{ authStore.user?.username }}</p>
          <p class="text-gray-500 text-sm">{{ authStore.user?.email }}</p>
          <span class="inline-block mt-1 text-xs bg-primary/10 text-primary px-2 py-0.5 rounded-full">
            {{ authStore.isAdmin ? '管理员' : '普通用户' }}
          </span>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <router-link to="/user/favorites" class="flex items-center gap-3 p-4 bg-dark-100 rounded-xl hover:bg-dark-100 transition-colors">
          <span class="text-2xl">⭐</span>
          <div>
            <p class="font-medium text-white text-sm">我的收藏</p>
            <p class="text-xs text-gray-400">管理收藏的机器人</p>
          </div>
        </router-link>
        <router-link to="/compare" class="flex items-center gap-3 p-4 bg-dark-100 rounded-xl hover:bg-dark-100 transition-colors">
          <span class="text-2xl">⚖️</span>
          <div>
            <p class="font-medium text-white text-sm">产品对比</p>
            <p class="text-xs text-gray-400">对比多款机器人</p>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
