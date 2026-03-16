<template>
  <div>
    <!-- Hero Section -->
    <section class="relative overflow-hidden py-28">
      <!-- Background glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-primary/5 via-transparent to-transparent"></div>
      <div class="absolute top-20 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-primary/5 rounded-full blur-[120px]"></div>
      <div class="relative max-w-4xl mx-auto px-4 text-center">
        <h1 class="text-5xl font-bold mb-4 leading-tight text-white">
          发现全球最佳<span class="text-primary drop-shadow-[0_0_20px_rgba(0,255,209,0.3)]">机器人</span>
        </h1>
        <p class="text-xl text-gray-400 mb-10">
          专业机器人产品数据库 · 涵盖工业臂、协作机器人、AMR、人形机器人等
        </p>
        <form @submit.prevent="handleSearch" class="flex gap-3 max-w-xl mx-auto">
          <input
            v-model="q"
            type="text"
            placeholder="搜索型号、厂商、应用场景..."
            class="input-dark flex-1 px-5 py-3.5 rounded-xl"
          />
          <button
            type="submit"
            class="btn-primary px-8 py-3.5 rounded-xl"
          >
            搜索
          </button>
        </form>
        <div class="flex justify-center gap-6 mt-8 text-sm text-gray-500">
          <span><span class="text-primary">🤖</span> 1,200+ 机器人产品</span>
          <span><span class="text-primary">🏭</span> 200+ 厂商</span>
          <span><span class="text-primary">🌏</span> 覆盖全球市场</span>
        </div>
      </div>
    </section>

    <!-- Ad banner -->
    <section class="max-w-7xl mx-auto px-4 pt-4">
      <AdBanner position="home_banner" />
    </section>

    <!-- Categories -->
    <section class="max-w-7xl mx-auto px-4 py-12">
      <h2 class="text-2xl font-bold text-white mb-6">按分类浏览</h2>
      <div class="grid grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-3">
        <router-link
          v-for="cat in categories"
          :key="cat.slug"
          :to="{ name: 'robot-list', query: { category: cat.slug } }"
          class="card-glow rounded-xl p-5 text-center group cursor-pointer"
        >
          <div class="text-3xl mb-2 group-hover:scale-110 transition-transform duration-300">{{ cat.icon }}</div>
          <div class="text-sm font-medium text-gray-400 group-hover:text-primary transition-colors">{{ cat.name }}</div>
        </router-link>
      </div>
    </section>

    <!-- Latest robots -->
    <section class="max-w-7xl mx-auto px-4 pb-16">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-white">最新上架</h2>
        <router-link to="/robots" class="text-sm text-primary hover:text-primary-400 transition-colors">查看全部 →</router-link>
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
  { slug: 'humanoid', name: '人形机器人', icon: '🚶' },
  { slug: 'wheeled', name: '轮式机器人', icon: '🛞' },
  { slug: 'quadruped', name: '四足机器人', icon: '🐕' },
  { slug: 'industrial-arm', name: '工业机械臂', icon: '🦾' },
  { slug: 'mini-desktop', name: '迷你/桌面机器人', icon: '🤖' },
  { slug: 'bionic', name: '仿生机器人', icon: '🦎' },
  { slug: 'dexterous-hand', name: '灵巧手', icon: '🖐️' },
  { slug: 'specialized', name: '特种机器人', icon: '🔧' },
  { slug: 'educational', name: '教育机器人', icon: '📚' },
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
