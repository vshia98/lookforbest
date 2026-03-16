<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="review" class="space-y-6">
      <!-- 返回 -->
      <router-link to="/reviews" class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-300">
        ← 返回评测列表
      </router-link>

      <!-- 头部信息 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <div class="flex items-start gap-4 mb-4">
          <img
            :src="review.authorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(review.authorName)}&background=random`"
            :alt="review.authorName"
            class="w-12 h-12 rounded-full object-cover flex-shrink-0"
          />
          <div class="flex-1">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="font-medium text-gray-300">{{ review.authorName }}</span>
              <span class="text-yellow-400">{{ '★'.repeat(review.rating) }}{{ '☆'.repeat(5 - review.rating) }}</span>
            </div>
            <div class="flex items-center gap-3 mt-1 text-xs text-gray-400">
              <router-link
                :to="`/robots/${review.robotSlug}`"
                class="text-blue-500 hover:underline"
              >
                {{ review.robotName }}
              </router-link>
              <span>{{ formatDate(review.createdAt) }}</span>
              <span>👁 {{ review.viewCount }} 浏览</span>
            </div>
          </div>
          <!-- 操作按钮 -->
          <div class="flex items-center gap-3">
            <button
              @click="toggleLike"
              class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-sm transition-colors"
              :class="review.liked ? 'bg-red-50 border-red-200 text-red-500' : 'border-white/[0.06] text-gray-500 hover:border-red-200 hover:text-red-400'"
            >
              {{ review.liked ? '❤️' : '🤍' }} {{ review.likeCount }}
            </button>
            <button
              @click="showReportModal = true"
              class="text-xs text-gray-400 hover:text-gray-600 px-2 py-1 rounded"
            >
              举报
            </button>
          </div>
        </div>

        <h1 class="text-2xl font-bold text-white mb-4">{{ review.title }}</h1>

        <!-- 优缺点 -->
        <div v-if="review.pros || review.cons" class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
          <div v-if="review.pros" class="bg-green-50 rounded-lg p-4">
            <h3 class="text-sm font-semibold text-green-400 mb-2">✅ 优点</h3>
            <p class="text-sm text-green-800 whitespace-pre-wrap">{{ review.pros }}</p>
          </div>
          <div v-if="review.cons" class="bg-red-50 rounded-lg p-4">
            <h3 class="text-sm font-semibold text-red-400 mb-2">❌ 缺点</h3>
            <p class="text-sm text-red-800 whitespace-pre-wrap">{{ review.cons }}</p>
          </div>
        </div>

        <!-- 正文 -->
        <div class="prose max-w-none text-gray-300 whitespace-pre-wrap leading-relaxed text-sm">{{ review.content }}</div>

        <!-- 图片 -->
        <div v-if="review.images && review.images.length > 0" class="mt-6 grid grid-cols-2 sm:grid-cols-3 gap-3">
          <img
            v-for="(img, i) in review.images"
            :key="i"
            :src="img"
            :alt="`图片${i + 1}`"
            class="w-full h-40 object-cover rounded-lg border border-white/[0.04] cursor-pointer hover:opacity-90 transition-opacity"
            @click="openImage(img)"
          />
        </div>
      </div>
    </div>

    <!-- 举报弹窗 -->
    <div v-if="showReportModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-dark-50 rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-semibold text-white mb-4">举报内容</h3>
        <textarea
          v-model="reportReason"
          placeholder="请描述举报原因..."
          rows="4"
          class="w-full border border-white/[0.06] rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 resize-none"
        ></textarea>
        <div class="flex justify-end gap-3 mt-4">
          <button @click="showReportModal = false" class="px-4 py-2 text-sm text-gray-600 hover:text-white">取消</button>
          <button @click="submitReport" class="px-4 py-2 bg-red-500 hover:bg-red-600 text-white text-sm rounded-lg transition-colors">提交举报</button>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <div v-if="previewImage" class="fixed inset-0 bg-black/80 flex items-center justify-center z-50" @click="previewImage = null">
      <img :src="previewImage" class="max-w-[90vw] max-h-[90vh] object-contain rounded-lg" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { reviewService } from '@/services/robots'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const review = ref<any>(null)
const loading = ref(false)
const showReportModal = ref(false)
const reportReason = ref('')
const previewImage = ref<string | null>(null)

function formatDate(dt: string) {
  return new Date(dt).toLocaleDateString('zh-CN')
}

function openImage(url: string) {
  previewImage.value = url
}

async function toggleLike() {
  if (!authStore.isLoggedIn) {
    alert('请先登录')
    return
  }
  const res = await reviewService.like(Number(route.params.id))
  if (review.value) {
    review.value.liked = res.data.liked
    review.value.likeCount += res.data.liked ? 1 : -1
  }
}

async function submitReport() {
  if (!reportReason.value.trim()) return
  await reviewService.report(Number(route.params.id), reportReason.value)
  showReportModal.value = false
  reportReason.value = ''
  alert('举报已提交')
}

async function loadReview() {
  loading.value = true
  try {
    const res = await reviewService.getById(Number(route.params.id))
    review.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(loadReview)
</script>
