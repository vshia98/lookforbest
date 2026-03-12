<template>
  <div>
    <!-- Hero Section -->
    <section class="bg-gradient-to-br from-gray-900 via-gray-800 to-primary-900 text-white py-24">
      <div class="max-w-4xl mx-auto px-4 text-center">
        <h1 class="text-5xl font-bold mb-4 leading-tight">
          发现全球最佳<span class="text-primary-400">机器人</span>
        </h1>
        <p class="text-xl text-gray-300 mb-10">
          专业机器人产品数据库 · 涵盖工业臂、协作机器人、AMR、人形机器人等
        </p>
        <form @submit.prevent="handleSearch" class="flex gap-3 max-w-xl mx-auto">
          <input
            v-model="q"
            type="text"
            placeholder="搜索型号、厂商、应用场景..."
            class="flex-1 px-5 py-3 rounded-xl text-gray-900 text-sm focus:outline-none focus:ring-2 focus:ring-primary-400"
          />
          <button
            type="submit"
            class="bg-primary-500 hover:bg-primary-400 text-white px-6 py-3 rounded-xl font-medium text-sm transition-colors"
          >
            搜索
          </button>
        </form>
        <div class="flex justify-center gap-6 mt-8 text-sm text-gray-400">
          <span>🤖 1,200+ 机器人产品</span>
          <span>🏭 200+ 厂商</span>
          <span>🌏 覆盖全球市场</span>
        </div>
      </div>
    </section>

    <!-- 广告横幅 -->
    <section class="max-w-7xl mx-auto px-4 pt-8">
      <AdBanner position="home_banner" />
    </section>

    <!-- 分类导航 -->
    <section class="max-w-7xl mx-auto px-4 py-12">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">按分类浏览</h2>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <router-link
          v-for="cat in categories"
          :key="cat.slug"
          :to="{ name: 'robot-list', query: { category: cat.slug } }"
          class="bg-white rounded-xl p-5 text-center shadow-sm border border-gray-100 hover:shadow-md hover:border-primary-200 transition-all group"
        >
          <div class="text-3xl mb-2">{{ cat.icon }}</div>
          <div class="text-sm font-medium text-gray-700 group-hover:text-primary-600">{{ cat.name }}</div>
        </router-link>
      </div>
    </section>

    <!-- 最新机器人 -->
    <section class="max-w-7xl mx-auto px-4 pb-16">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-gray-800">最新上架</h2>
        <router-link to="/robots" class="text-sm text-primary-500 hover:underline">查看全部 →</router-link>
      </div>
      <LoadingSpinner v-if="loading" />
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <RobotCard v-for="robot in robots" :key="robot.id" :robot="robot" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import RobotCard from '@/components/robot/RobotCard.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import AdBanner from '@/components/ui/AdBanner.vue'
import { useRobotStore } from '@/stores/robot'

const router = useRouter()
const robotStore = useRobotStore()

const q = ref('')
const loading = ref(false)
const robots = ref(robotStore.robots)

const categories = [
  { slug: 'cobot', name: '协作机器人', icon: '🦾' },
  { slug: 'industrial-arm', name: '工业机械臂', icon: '🏗️' },
  { slug: 'amr', name: '移动机器人', icon: '🚗' },
  { slug: 'humanoid', name: '人形机器人', icon: '🚶' },
  { slug: 'welding', name: '焊接机器人', icon: '🔥' },
  { slug: 'scara', name: 'SCARA', icon: '⚙️' }
]

function handleSearch() {
  if (q.value.trim()) {
    router.push({ name: 'robot-list', query: { q: q.value.trim() } })
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await robotStore.fetchRobots({ sort: 'newest', size: 8 })
    robots.value = robotStore.robots
  } finally {
    loading.value = false
  }
})
</script>
