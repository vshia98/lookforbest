<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <LoadingSpinner v-if="loading" />

    <template v-else-if="manufacturer">
      <!-- 厂商信息头部 -->
      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-8 mb-8">
        <div class="flex items-center gap-6">
          <div class="w-20 h-20 bg-gray-50 rounded-xl flex items-center justify-center overflow-hidden">
            <img v-if="manufacturer.logoUrl" :src="manufacturer.logoUrl" :alt="manufacturer.name" class="max-w-full max-h-full object-contain" />
            <span v-else class="text-4xl">🏭</span>
          </div>
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-gray-900">{{ manufacturer.name }}</h1>
            <p v-if="manufacturer.nameEn && manufacturer.nameEn !== manufacturer.name" class="text-gray-400 mt-0.5">{{ manufacturer.nameEn }}</p>
            <div class="flex items-center gap-4 mt-2">
              <span class="text-sm text-gray-500">🌍 {{ manufacturer.country }}</span>
              <a v-if="manufacturer.websiteUrl" :href="manufacturer.websiteUrl" target="_blank" rel="noopener" class="text-sm text-primary-500 hover:underline">
                官方网站 →
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 产品列表 -->
      <h2 class="text-xl font-bold text-gray-800 mb-5">旗下产品 ({{ total }})</h2>
      <LoadingSpinner v-if="robotsLoading" />
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <RobotCard v-for="robot in robots" :key="robot.id" :robot="robot" />
      </div>

      <div v-if="robots.length === 0 && !robotsLoading" class="text-center py-12 text-gray-400">
        暂无产品数据
      </div>
    </template>

    <div v-else class="text-center py-20">
      <p class="text-gray-500">未找到该厂商</p>
      <router-link to="/robots" class="text-primary-500 hover:underline mt-4 block">返回机器人库</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import RobotCard from '@/components/robot/RobotCard.vue'
import { manufacturerService } from '@/services/manufacturers'
import { robotService } from '@/services/robots'
import type { RobotListItem } from '@/types/robot'

const route = useRoute()
const manufacturer = ref<any>(null)
const robots = ref<RobotListItem[]>([])
const total = ref(0)
const loading = ref(true)
const robotsLoading = ref(false)

onMounted(async () => {
  const id = parseInt(route.params.id as string)
  try {
    const mfrRes = await manufacturerService.getById(id)
    manufacturer.value = mfrRes.data
    robotsLoading.value = true
    const robotRes = await robotService.getList({ manufacturerId: id, size: 20 })
    robots.value = robotRes.data.content
    total.value = robotRes.data.total
  } finally {
    loading.value = false
    robotsLoading.value = false
  }
})
</script>
