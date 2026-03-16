<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white">机器人评测</h1>
        <p class="text-gray-500 mt-1">共 {{ total }} 篇评测</p>
      </div>
      <router-link
        to="/reviews/create"
        class="inline-flex items-center gap-2 bg-primary hover:bg-primary-400 text-[#1a1a1a] px-4 py-2 rounded-lg text-sm font-medium transition-colors"
      >
        ✍️ 写评测
      </router-link>
    </div>

    <!-- 评分筛选 -->
    <div class="flex gap-2 mb-6">
      <button
        @click="selectRating(null)"
        class="px-4 py-1.5 rounded-full text-sm border transition-colors"
        :class="selectedRating === null ? 'bg-primary text-[#1a1a1a] border-primary' : 'border-white/[0.06] text-gray-400 hover:border-primary/40'"
      >
        全部
      </button>
      <button
        v-for="r in [5, 4, 3, 2, 1]"
        :key="r"
        @click="selectRating(r)"
        class="px-4 py-1.5 rounded-full text-sm border transition-colors"
        :class="selectedRating === r ? 'bg-primary text-[#1a1a1a] border-primary' : 'border-white/[0.06] text-gray-400 hover:border-primary/40'"
      >
        {{ '★'.repeat(r) }}{{ '☆'.repeat(5 - r) }}
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="reviews.length === 0" class="text-center py-20">
      <div class="text-5xl mb-4">📝</div>
      <p class="text-gray-500">暂无评测，成为第一个评测者</p>
      <router-link to="/reviews/create" class="mt-4 inline-block text-blue-500 hover:underline text-sm">写第一篇评测</router-link>
    </div>

    <!-- 评测列表 -->
    <div v-else class="grid grid-cols-1 gap-4">
      <router-link
        v-for="review in reviews"
        :key="review.id"
        :to="`/reviews/${review.id}`"
        class="bg-dark-50 rounded-xl border border-white/[0.04]  p-5 hover:shadow-md transition-shadow block"
      >
        <div class="flex items-start gap-4">
          <!-- 头像 -->
          <img
            :src="review.authorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(review.authorName)}&background=random`"
            :alt="review.authorName"
            class="w-10 h-10 rounded-full object-cover flex-shrink-0"
          />
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="text-sm font-medium text-gray-300">{{ review.authorName }}</span>
              <span class="text-yellow-400 text-sm">{{ '★'.repeat(review.rating) }}{{ '☆'.repeat(5 - review.rating) }}</span>
              <span class="text-xs text-gray-400">{{ formatDate(review.createdAt) }}</span>
            </div>
            <h3 class="font-semibold text-white mt-1 truncate">{{ review.title }}</h3>
            <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ excerpt(review.content) }}</p>
            <div class="flex items-center gap-4 mt-2">
              <span class="text-xs text-blue-400 bg-blue-50 px-2 py-0.5 rounded">{{ review.robotName }}</span>
              <span class="text-xs text-gray-400">👍 {{ review.likeCount }}</span>
            </div>
          </div>
        </div>
      </router-link>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
      <button
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
        class="px-4 py-2 rounded-lg border border-white/[0.06] text-sm disabled:opacity-40 hover:bg-dark-100"
      >
        上一页
      </button>
      <span class="text-sm text-gray-500">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage >= totalPages - 1"
        @click="changePage(currentPage + 1)"
        class="px-4 py-2 rounded-lg border border-white/[0.06] text-sm disabled:opacity-40 hover:bg-dark-100"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { reviewService } from '@/services/robots'

const reviews = ref<any[]>([])
const total = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const selectedRating = ref<number | null>(null)

function excerpt(content: string) {
  const stripped = content.replace(/[#*`_>[\]]/g, '').trim()
  return stripped.length > 100 ? stripped.slice(0, 100) + '...' : stripped
}

function formatDate(dt: string) {
  return new Date(dt).toLocaleDateString('zh-CN')
}

function selectRating(r: number | null) {
  selectedRating.value = r
  currentPage.value = 0
  loadReviews()
}

async function loadReviews() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: 20 }
    if (selectedRating.value !== null) params.rating = selectedRating.value
    const res = await reviewService.list(params)
    reviews.value = res.data.content
    total.value = res.data.total
    totalPages.value = res.data.totalPages
  } finally {
    loading.value = false
  }
}

function changePage(page: number) {
  currentPage.value = page
  loadReviews()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(loadReviews)
</script>
