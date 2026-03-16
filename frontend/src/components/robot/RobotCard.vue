<template>
  <div class="card-glow rounded-xl group">
    <!-- Cover image -->
    <router-link :to="{ name: 'robot-detail', params: { slug: robot.slug } }">
      <div class="relative h-48 overflow-hidden rounded-t-xl bg-dark-100">
        <!-- 光晕 -->
        <div class="absolute -top-10 -right-10 w-32 h-32 bg-primary/10 rounded-full blur-[60px] group-hover:bg-primary/20 transition-all duration-700"></div>
        <div class="absolute -bottom-10 -left-10 w-28 h-28 bg-accent-blue/8 rounded-full blur-[50px] group-hover:bg-accent-blue/15 transition-all duration-700"></div>
        <!-- 网格 -->
        <div class="absolute inset-0 opacity-[0.025]" style="background-image: linear-gradient(rgba(0,255,209,.2) 1px, transparent 1px), linear-gradient(90deg, rgba(0,255,209,.2) 1px, transparent 1px); background-size: 24px 24px;"></div>
        <img
          v-if="robot.coverImageUrl"
          :src="robot.coverImageUrl"
          :alt="robot.name"
          class="relative w-full h-full object-contain p-3 group-hover:scale-105 transition-transform duration-500"
        />
        <div v-else class="relative w-full h-full flex items-center justify-center text-5xl text-gray-600">🤖</div>
        <!-- 底部渐隐 -->
        <div class="absolute bottom-0 left-0 right-0 h-12 bg-gradient-to-t from-dark-50 to-transparent pointer-events-none"></div>
        <div class="absolute top-2 left-2 flex gap-1">
          <span class="bg-primary/90 text-dark text-xs px-2.5 py-0.5 rounded-lg font-medium backdrop-blur-sm">{{ robot.category?.name }}</span>
          <span v-if="robot.has3dModel" class="bg-accent-blue/90 text-white text-xs px-2 py-0.5 rounded-lg font-medium backdrop-blur-sm">3D</span>
        </div>
        <button
          @click.prevent="toggleFavorite"
          class="absolute top-2 right-2 w-8 h-8 bg-dark/60 backdrop-blur-sm rounded-full flex items-center justify-center text-gray-400 hover:text-accent-red transition-colors border border-white/10"
        >
          {{ isFavorited ? '❤️' : '🤍' }}
        </button>
      </div>
    </router-link>

    <div class="p-4">
      <!-- Manufacturer -->
      <div class="flex items-center gap-2 mb-2">
        <img v-if="robot.manufacturer?.logoUrl" :src="robot.manufacturer.logoUrl" :alt="robot.manufacturer.name" class="h-4 w-auto object-contain brightness-90" />
        <span class="text-xs text-gray-500">{{ robot.manufacturer?.name }}</span>
        <span class="text-xs text-gray-600 ml-auto">{{ robot.manufacturer?.country }}</span>
      </div>

      <!-- Name -->
      <router-link :to="{ name: 'robot-detail', params: { slug: robot.slug } }">
        <h3
          v-if="highlights?.name"
          class="font-semibold text-white hover:text-primary transition-colors line-clamp-1"
          v-html="highlights.name[0]"
        />
        <h3 v-else class="font-semibold text-white hover:text-primary transition-colors line-clamp-1">{{ robot.name }}</h3>
      </router-link>
      <p v-if="robot.subtitle" class="text-xs text-gray-500 mt-0.5 line-clamp-1">{{ robot.subtitle }}</p>
      <p v-else-if="robot.nameEn && robot.nameEn !== robot.name" class="text-xs text-gray-500 mt-0.5">{{ robot.nameEn }}</p>
      <p
        v-if="highlights?.description"
        class="text-xs text-gray-500 mt-1 line-clamp-2"
        v-html="highlights.description[0]"
      />

      <!-- Specs -->
      <div class="mt-3 grid grid-cols-3 gap-2 text-center">
        <div v-if="robot.payloadKg" class="bg-dark-100 rounded-lg p-2 border border-white/[0.04]">
          <div class="text-sm font-semibold text-primary-400">{{ robot.payloadKg }}kg</div>
          <div class="text-xs text-gray-600">载荷</div>
        </div>
        <div v-if="robot.reachMm" class="bg-dark-100 rounded-lg p-2 border border-white/[0.04]">
          <div class="text-sm font-semibold text-primary-400">{{ robot.reachMm }}mm</div>
          <div class="text-xs text-gray-600">工作半径</div>
        </div>
        <div v-if="robot.dof" class="bg-dark-100 rounded-lg p-2 border border-white/[0.04]">
          <div class="text-sm font-semibold text-primary-400">{{ robot.dof }} DOF</div>
          <div class="text-xs text-gray-600">自由度</div>
        </div>
      </div>

      <!-- Footer -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-xs text-gray-600">👁 {{ robot.viewCount }}</span>
        <button
          @click="handleAddToCompare"
          :disabled="isInCompare"
          class="text-xs px-3 py-1 rounded-full border transition-all"
          :class="isInCompare ? 'border-primary/50 text-primary bg-primary/10 cursor-default' : 'border-white/10 text-gray-500 hover:border-primary/40 hover:text-primary'"
        >
          {{ isInCompare ? '已对比' : '+ 对比' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RobotListItem } from '@/types/robot'
import { useRobotStore } from '@/stores/robot'
import { useFavoritesStore } from '@/stores/favorites'

const props = defineProps<{
  robot: RobotListItem
  highlights?: Record<string, string[]>
}>()

const robotStore = useRobotStore()
const favoritesStore = useFavoritesStore()

const isInCompare = computed(() => robotStore.compareList.some(r => r.id === props.robot.id))
const isFavorited = computed(() => favoritesStore.isFavorited(props.robot.id))

function handleAddToCompare() {
  if (!isInCompare.value) {
    robotStore.addToCompare(props.robot)
  }
}

function toggleFavorite() {
  favoritesStore.toggle(props.robot.id)
}
</script>

<style scoped>
:deep(em.highlight) {
  background-color: rgba(0, 255, 209, 0.15);
  color: #77F8D3;
  border-radius: 2px;
  padding: 0 2px;
  font-style: normal;
}
</style>
