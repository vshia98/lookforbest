<template>
  <div>
    <!-- Hero Section -->
    <section class="relative overflow-hidden py-28">
      <!-- Background glow -->
      <div class="absolute inset-0 bg-gradient-to-b from-primary/5 via-transparent to-transparent"></div>
      <div class="absolute top-20 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-primary/5 rounded-full blur-[120px]"></div>
      <div class="relative max-w-4xl mx-auto px-4 text-center">
        <h1 class="text-5xl font-bold mb-4 leading-tight text-white">
          {{ t('home.title') }}
        </h1>
        <p class="text-xl text-gray-400 mb-10">
          {{ t('home.subtitle') }}
        </p>
        <form @submit.prevent="handleSearch" class="flex gap-3 max-w-xl mx-auto">
          <input
            v-model="q"
            type="text"
            :placeholder="t('home.searchPlaceholder')"
            class="input-dark flex-1 px-5 py-3.5 rounded-xl"
          />
          <button
            type="submit"
            class="btn-primary px-8 py-3.5 rounded-xl"
          >
            {{ t('common.search') }}
          </button>
        </form>
        <div class="flex justify-center gap-6 mt-8 text-sm text-gray-500">
          <span><span class="text-primary">🤖</span> 1,200+ robots</span>
          <span><span class="text-primary">🏭</span> 200+ manufacturers</span>
          <span><span class="text-primary">🌏</span> Global coverage</span>
        </div>
      </div>
    </section>

    <!-- Ad banner -->
    <section class="max-w-7xl mx-auto px-4 pt-4">
      <AdBanner position="home_banner" />
    </section>

    <!-- Categories -->
    <section class="max-w-7xl mx-auto px-4 py-12">
      <h2 class="text-2xl font-bold text-white mb-6">{{ t('home.topCategories') }}</h2>
      <div class="grid grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-3">
        <router-link
          v-for="cat in categories"
          :key="cat.slug"
          :to="{ name: 'robot-list', query: { category: cat.slug } }"
          class="card-glow rounded-xl p-5 text-center group cursor-pointer"
        >
          <div class="text-3xl mb-2 group-hover:scale-110 transition-transform duration-300">{{ cat.icon }}</div>
          <div class="text-sm font-medium text-gray-400 group-hover:text-primary transition-colors">
            {{ t('categories.' + cat.slug) }}
          </div>
        </router-link>
      </div>
    </section>

    <!-- Latest robots -->
    <section class="max-w-7xl mx-auto px-4 pb-16">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-2xl font-bold text-white">{{ t('home.featuredRobots') }}</h2>
        <router-link to="/robots" class="text-sm text-primary hover:text-primary-400 transition-colors">
          {{ t('common.more') }} →
        </router-link>
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
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import RobotCard from '@/components/robot/RobotCard.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import AdBanner from '@/components/ui/AdBanner.vue'
import { useRobotStore } from '@/stores/robot'

const { t } = useI18n()

const router = useRouter()
const robotStore = useRobotStore()

const q = ref('')
const loading = ref(false)
const robots = ref(robotStore.robots)

const categories = [
  { slug: 'humanoid', icon: '🚶' },
  { slug: 'wheeled', icon: '🛞' },
  { slug: 'quadruped', icon: '🐕' },
  { slug: 'industrial-arm', icon: '🦾' },
  { slug: 'mini-desktop', icon: '🤖' },
  { slug: 'bionic', icon: '🦎' },
  { slug: 'dexterous-hand', icon: '🖐️' },
  { slug: 'specialized', icon: '🔧' },
  { slug: 'educational', icon: '📚' },
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
