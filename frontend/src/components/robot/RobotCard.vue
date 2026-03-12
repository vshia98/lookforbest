<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow group">
    <!-- 封面图 -->
    <router-link :to="{ name: 'robot-detail', params: { slug: robot.slug } }">
      <div class="relative h-48 bg-gray-100 overflow-hidden">
        <img
          v-if="robot.coverImageUrl"
          :src="robot.coverImageUrl"
          :alt="robot.name"
          class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        <div v-else class="w-full h-full flex items-center justify-center text-5xl">🤖</div>
        <div class="absolute top-2 left-2 flex gap-1">
          <span class="bg-primary-500 text-white text-xs px-2 py-0.5 rounded-full">{{ robot.category.name }}</span>
          <span v-if="robot.has3dModel" class="bg-purple-500 text-white text-xs px-2 py-0.5 rounded-full">3D</span>
        </div>
        <button
          @click.prevent="toggleFavorite"
          class="absolute top-2 right-2 w-8 h-8 bg-white rounded-full shadow flex items-center justify-center text-gray-400 hover:text-red-500 transition-colors"
        >
          {{ isFavorited ? '❤️' : '🤍' }}
        </button>
      </div>
    </router-link>

    <div class="p-4">
      <!-- 厂商 -->
      <div class="flex items-center gap-2 mb-2">
        <img v-if="robot.manufacturer.logoUrl" :src="robot.manufacturer.logoUrl" :alt="robot.manufacturer.name" class="h-4 w-auto object-contain" />
        <span class="text-xs text-gray-500">{{ robot.manufacturer.name }}</span>
        <span class="text-xs text-gray-300 ml-auto">{{ robot.manufacturer.country }}</span>
      </div>

      <!-- 名称 -->
      <router-link :to="{ name: 'robot-detail', params: { slug: robot.slug } }">
        <h3
          v-if="highlights?.name"
          class="font-semibold text-gray-900 hover:text-primary-600 transition-colors line-clamp-1"
          v-html="highlights.name[0]"
        />
        <h3 v-else class="font-semibold text-gray-900 hover:text-primary-600 transition-colors line-clamp-1">{{ robot.name }}</h3>
      </router-link>
      <p v-if="robot.nameEn && robot.nameEn !== robot.name" class="text-xs text-gray-400 mt-0.5">{{ robot.nameEn }}</p>
      <!-- 搜索结果高亮描述片段 -->
      <p
        v-if="highlights?.description"
        class="text-xs text-gray-500 mt-1 line-clamp-2"
        v-html="highlights.description[0]"
      />

      <!-- 核心参数 -->
      <div class="mt-3 grid grid-cols-3 gap-2 text-center">
        <div v-if="robot.payloadKg" class="bg-gray-50 rounded-lg p-2">
          <div class="text-sm font-semibold text-gray-800">{{ robot.payloadKg }}kg</div>
          <div class="text-xs text-gray-400">载荷</div>
        </div>
        <div v-if="robot.reachMm" class="bg-gray-50 rounded-lg p-2">
          <div class="text-sm font-semibold text-gray-800">{{ robot.reachMm }}mm</div>
          <div class="text-xs text-gray-400">工作半径</div>
        </div>
        <div v-if="robot.dof" class="bg-gray-50 rounded-lg p-2">
          <div class="text-sm font-semibold text-gray-800">{{ robot.dof }} DOF</div>
          <div class="text-xs text-gray-400">自由度</div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="mt-4 flex items-center justify-between">
        <span class="text-xs text-gray-400">👁 {{ robot.viewCount }}</span>
        <button
          @click="handleAddToCompare"
          :disabled="isInCompare"
          class="text-xs px-3 py-1 rounded-full border transition-colors"
          :class="isInCompare ? 'border-primary-500 text-primary-500 bg-primary-50 cursor-default' : 'border-gray-300 text-gray-600 hover:border-primary-400 hover:text-primary-500'"
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
  background-color: #fef08a;
  color: #713f12;
  border-radius: 2px;
  padding: 0 2px;
  font-style: normal;
}
</style>
