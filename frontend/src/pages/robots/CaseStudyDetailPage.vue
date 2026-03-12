<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="cs" class="space-y-6">
      <router-link to="/case-studies" class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700">
        ← 返回案例列表
      </router-link>

      <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
        <!-- 头部 -->
        <div class="flex items-start gap-4 mb-4">
          <img
            :src="cs.authorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(cs.authorName)}&background=random`"
            :alt="cs.authorName"
            class="w-12 h-12 rounded-full object-cover flex-shrink-0"
          />
          <div class="flex-1">
            <span class="font-medium text-gray-700">{{ cs.authorName }}</span>
            <div class="flex items-center gap-3 mt-1 text-xs text-gray-400">
              <span v-if="cs.industry" class="bg-green-100 text-green-700 px-2 py-0.5 rounded-full">{{ cs.industry }}</span>
              <span>{{ formatDate(cs.createdAt) }}</span>
              <span>👁 {{ cs.viewCount }} 浏览</span>
            </div>
          </div>
          <button
            @click="toggleLike"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-sm transition-colors"
            :class="cs.liked ? 'bg-red-50 border-red-200 text-red-500' : 'border-gray-200 text-gray-500 hover:border-red-200 hover:text-red-400'"
          >
            {{ cs.liked ? '❤️' : '🤍' }} {{ cs.likeCount }}
          </button>
        </div>

        <h1 class="text-2xl font-bold text-gray-800 mb-4">{{ cs.title }}</h1>

        <!-- 关联机器人 -->
        <div v-if="cs.robotIds && cs.robotIds.length > 0" class="flex gap-2 flex-wrap mb-4">
          <span class="text-sm text-gray-500">关联机器人：</span>
          <span
            v-for="robotId in cs.robotIds"
            :key="robotId"
            class="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded"
          >
            #{{ robotId }}
          </span>
        </div>

        <!-- 内容 -->
        <div class="prose max-w-none text-gray-700 whitespace-pre-wrap leading-relaxed text-sm">{{ cs.content }}</div>

        <!-- 图片 -->
        <div v-if="cs.images && cs.images.length > 0" class="mt-6 grid grid-cols-2 sm:grid-cols-3 gap-3">
          <img
            v-for="(img, i) in cs.images"
            :key="i"
            :src="img"
            :alt="`图片${i + 1}`"
            class="w-full h-40 object-cover rounded-lg border border-gray-100 cursor-pointer hover:opacity-90 transition-opacity"
            @click="previewImage = img"
          />
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
import { caseStudyService } from '@/services/robots'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const cs = ref<any>(null)
const loading = ref(false)
const previewImage = ref<string | null>(null)

function formatDate(dt: string) {
  return new Date(dt).toLocaleDateString('zh-CN')
}

async function toggleLike() {
  if (!authStore.isLoggedIn) {
    alert('请先登录')
    return
  }
  const res = await caseStudyService.like(Number(route.params.id))
  if (cs.value) {
    cs.value.liked = res.data.liked
    cs.value.likeCount += res.data.liked ? 1 : -1
  }
}

async function loadCase() {
  loading.value = true
  try {
    const res = await caseStudyService.getById(Number(route.params.id))
    cs.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(loadCase)
</script>
