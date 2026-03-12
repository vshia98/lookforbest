<template>
  <Transition name="slide-up">
    <div v-if="compareList.length > 0" class="fixed bottom-0 left-0 right-0 bg-white shadow-[0_-4px_24px_rgba(0,0,0,0.12)] border-t border-gray-200 z-50">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center gap-4">
        <div class="flex items-center gap-3 flex-1">
          <span class="text-sm font-medium text-gray-700">对比栏 ({{ compareList.length }}/4)</span>
          <div class="flex gap-3">
            <div
              v-for="robot in compareList"
              :key="robot.id"
              class="flex items-center gap-2 bg-gray-50 rounded-lg px-3 py-1.5"
            >
              <span class="text-sm text-gray-800 max-w-28 truncate">{{ robot.name }}</span>
              <button @click="remove(robot.id)" class="text-gray-400 hover:text-red-500 text-xs">✕</button>
            </div>
            <!-- 空占位 -->
            <div
              v-for="i in (4 - compareList.length)"
              :key="i"
              class="flex items-center justify-center w-32 h-9 border-2 border-dashed border-gray-200 rounded-lg text-xs text-gray-400"
            >
              + 添加对比
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <button @click="clearAll" class="text-sm text-gray-500 hover:text-gray-700">清空</button>
          <router-link
            to="/compare"
            class="bg-primary-500 text-white text-sm px-5 py-2 rounded-lg hover:bg-primary-600 transition-colors font-medium"
          >
            开始对比
          </router-link>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRobotStore } from '@/stores/robot'

const robotStore = useRobotStore()
const compareList = computed(() => robotStore.compareList)

function remove(id: number) {
  robotStore.removeFromCompare(id)
}

function clearAll() {
  robotStore.compareList.splice(0)
}
</script>

<style scoped>
.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.3s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
}
</style>
