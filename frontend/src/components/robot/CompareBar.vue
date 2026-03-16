<template>
  <Transition name="slide-up">
    <div v-if="compareList.length > 0" class="fixed bottom-0 left-0 right-0 glass shadow-[0_-4px_24px_rgba(0,0,0,0.3)] border-t border-white/[0.06] z-50">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center gap-4">
        <div class="flex items-center gap-3 flex-1">
          <span class="text-sm font-medium text-gray-300">对比栏 ({{ compareList.length }}/4)</span>
          <div class="flex gap-3">
            <div
              v-for="robot in compareList"
              :key="robot.id"
              class="flex items-center gap-2 bg-dark-100 border border-white/[0.06] rounded-lg px-3 py-1.5"
            >
              <span class="text-sm text-white max-w-28 truncate">{{ robot.name }}</span>
              <button @click="remove(robot.id)" class="text-gray-500 hover:text-accent-red text-xs transition-colors">✕</button>
            </div>
            <div
              v-for="i in (4 - compareList.length)"
              :key="i"
              class="flex items-center justify-center w-32 h-9 border-2 border-dashed border-white/10 rounded-lg text-xs text-gray-600"
            >
              + 添加对比
            </div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <button @click="clearAll" class="text-sm text-gray-500 hover:text-gray-300 transition-colors">清空</button>
          <router-link
            to="/compare"
            class="btn-primary text-sm px-5 py-2"
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
