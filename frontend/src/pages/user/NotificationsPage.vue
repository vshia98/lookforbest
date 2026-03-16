<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-white">通知中心</h1>
      <button
        v-if="unreadCount > 0"
        @click="markAllRead"
        class="text-sm text-primary hover:text-primary-400 transition-colors"
      >全部标记已读</button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-500">加载中...</div>

    <div v-else-if="notifications.length === 0" class="text-center py-16">
      <div class="text-5xl mb-4">🔔</div>
      <p class="text-gray-500">暂无通知</p>
    </div>

    <ul v-else class="space-y-3">
      <li
        v-for="n in notifications"
        :key="n.id"
        @click="markRead(n)"
        class="card rounded-xl p-4 cursor-pointer transition-all"
        :class="n.isRead ? '' : 'border-l-4 border-l-primary'"
      >
        <div class="flex items-start gap-3">
          <span class="text-xl mt-0.5">{{ typeIcon(n.type) }}</span>
          <div class="flex-1">
            <div class="flex items-center justify-between">
              <p class="font-medium text-white text-sm">{{ n.title }}</p>
              <div v-if="!n.isRead" class="w-2 h-2 bg-primary rounded-full ml-2 flex-shrink-0 mt-1"></div>
            </div>
            <p v-if="n.content" class="text-sm text-gray-400 mt-1">{{ n.content }}</p>
            <p class="text-xs text-gray-600 mt-2">{{ formatDate(n.createdAt) }}</p>
          </div>
        </div>
      </li>
    </ul>

    <div v-if="hasNext" class="text-center mt-6">
      <button @click="loadMore" :disabled="loadingMore" class="px-6 py-2 border border-white/10 rounded-xl text-sm text-gray-400 hover:border-primary/30 hover:text-primary transition-all disabled:opacity-50">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { notificationService, type NotificationDTO } from '@/services/notifications'

const notifications = ref<NotificationDTO[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const hasNext = ref(false)
const unreadCount = ref(0)
const currentPage = ref(0)

function typeIcon(type: string) {
  if (type === 'inquiry_replied') return '📧'
  if (type === 'comment_replied') return '💬'
  return '📢'
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadPage(page: number, append = false) {
  try {
    const res: any = await notificationService.getList(page, 20)
    const data = res.data || res
    const items: NotificationDTO[] = data.content || []
    if (append) notifications.value.push(...items)
    else notifications.value = items
    hasNext.value = data.hasNext || false
    currentPage.value = page
    unreadCount.value = notifications.value.filter(n => !n.isRead).length
  } catch {}
}

async function loadMore() {
  loadingMore.value = true
  await loadPage(currentPage.value + 1, true)
  loadingMore.value = false
}

async function markRead(n: NotificationDTO) {
  if (!n.isRead) {
    await notificationService.markRead(n.id)
    n.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

async function markAllRead() {
  await notificationService.markAllRead()
  notifications.value.forEach(n => n.isRead = true)
  unreadCount.value = 0
}

onMounted(async () => {
  await loadPage(0)
  loading.value = false
})
</script>
