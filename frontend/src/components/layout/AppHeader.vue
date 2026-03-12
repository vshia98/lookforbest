<template>
  <header class="bg-white shadow-sm sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2 font-bold text-xl text-gray-900">
          <span class="text-primary-500">⚡</span>
          <span>LookForBest</span>
        </router-link>

        <!-- 搜索栏 -->
        <form @submit.prevent="handleSearch" class="hidden md:flex flex-1 max-w-lg mx-8">
          <div class="relative w-full">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索机器人、厂商、型号..."
              class="w-full pl-4 pr-10 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
            <button type="submit" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary-500">
              🔍
            </button>
          </div>
        </form>

        <!-- 导航菜单 -->
        <nav class="flex items-center gap-4">
          <router-link to="/robots" class="text-sm text-gray-600 hover:text-primary-600 font-medium">机器人库</router-link>
          <router-link to="/compare" class="text-sm text-gray-600 hover:text-primary-600 font-medium relative">
            对比
            <span v-if="compareCount > 0" class="absolute -top-2 -right-3 bg-primary-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
              {{ compareCount }}
            </span>
          </router-link>

          <!-- 深色模式切换 -->
          <button
            @click="themeStore.toggle()"
            class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
            :title="themeStore.isDark ? '切换亮色模式' : '切换深色模式'"
          >
            <span v-if="themeStore.isDark">☀️</span>
            <span v-else>🌙</span>
          </button>

          <!-- 语言切换 -->
          <button
            @click="toggleLocale"
            class="text-xs font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200 border border-gray-200 dark:border-gray-600 px-2 py-1 rounded transition-colors"
            :title="currentLocale === 'zh' ? 'Switch to English' : '切换中文'"
          >
            {{ currentLocale === 'zh' ? 'EN' : '中' }}
          </button>

          <!-- 未登录 -->
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="text-sm text-gray-600 hover:text-primary-600">登录</router-link>
            <router-link to="/register" class="bg-primary-500 text-white text-sm px-4 py-2 rounded-lg hover:bg-primary-600 transition-colors">
              注册
            </router-link>
          </template>

          <!-- 已登录 -->
          <template v-else>
            <!-- 通知铃铛 -->
            <div class="relative" ref="notifRef">
              <button
                @click="toggleNotifPanel"
                class="relative w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
                title="通知"
              >
                🔔
                <span v-if="unreadCount > 0" class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full min-w-[16px] h-4 flex items-center justify-center px-0.5 leading-none">
                  {{ unreadCount > 99 ? '99+' : unreadCount }}
                </span>
              </button>

              <!-- 通知面板 -->
              <div v-if="showNotifPanel" class="absolute right-0 top-10 w-80 bg-white shadow-xl rounded-xl border border-gray-100 z-50 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                  <span class="font-semibold text-sm text-gray-800">通知</span>
                  <button v-if="unreadCount > 0" @click="markAllRead" class="text-xs text-primary-500 hover:underline">全部已读</button>
                </div>
                <div v-if="notifications.length === 0" class="text-center py-8 text-gray-400 text-sm">暂无通知</div>
                <ul v-else class="max-h-80 overflow-y-auto divide-y divide-gray-50">
                  <li
                    v-for="n in notifications"
                    :key="n.id"
                    @click="markRead(n)"
                    class="px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors"
                    :class="n.isRead ? '' : 'bg-blue-50'"
                  >
                    <div class="flex items-start gap-2">
                      <span class="text-base mt-0.5">{{ typeIcon(n.type) }}</span>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-gray-800 truncate">{{ n.title }}</p>
                        <p v-if="n.content" class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ n.content }}</p>
                        <p class="text-xs text-gray-400 mt-1">{{ formatTime(n.createdAt) }}</p>
                      </div>
                      <div v-if="!n.isRead" class="w-2 h-2 bg-blue-500 rounded-full mt-1.5 flex-shrink-0"></div>
                    </div>
                  </li>
                </ul>
                <div class="border-t border-gray-100 px-4 py-2.5">
                  <router-link to="/user/notifications" @click="showNotifPanel = false" class="text-xs text-primary-500 hover:underline">查看全部通知</router-link>
                </div>
              </div>
            </div>

            <div class="relative" ref="menuRef">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center gap-2 text-sm text-gray-700 hover:text-gray-900"
              >
                <div class="w-8 h-8 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-medium text-xs">
                  {{ authStore.user?.username?.charAt(0).toUpperCase() }}
                </div>
                <span class="hidden md:inline">{{ authStore.user?.username }}</span>
                <span class="text-xs">▼</span>
              </button>
              <div v-if="showUserMenu" class="absolute right-0 top-10 w-44 bg-white shadow-lg rounded-lg border border-gray-100 py-1 z-50">
                <router-link to="/user/favorites" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                  ⭐ 我的收藏
                </router-link>
                <router-link to="/user/notifications" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                  🔔 通知中心
                </router-link>
                <router-link v-if="authStore.isAdmin" to="/admin" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
                  ⚙️ 管理后台
                </router-link>
                <hr class="my-1 border-gray-100" />
                <button @click="logout" class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-500 hover:bg-red-50">
                  → 退出登录
                </button>
              </div>
            </div>
          </template>
        </nav>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useRobotStore } from '@/stores/robot'
import { setLocale, getLocale } from '@/locales'
import { useThemeStore } from '@/stores/theme'
import { notificationService, type NotificationDTO } from '@/services/notifications'

const router = useRouter()
const authStore = useAuthStore()
const robotStore = useRobotStore()
const themeStore = useThemeStore()

const searchQuery = ref('')
const showUserMenu = ref(false)
const menuRef = ref<HTMLElement | null>(null)
const currentLocale = ref(getLocale())

// 通知
const showNotifPanel = ref(false)
const notifRef = ref<HTMLElement | null>(null)
const notifications = ref<NotificationDTO[]>([])
const unreadCount = ref(0)

function typeIcon(type: string) {
  if (type === 'inquiry_replied') return '📧'
  if (type === 'comment_replied') return '💬'
  return '📢'
}

function formatTime(d: string) {
  return new Date(d).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadNotifications() {
  try {
    const [listRes, countRes]: any[] = await Promise.all([
      notificationService.getList(0, 10),
      notificationService.getUnreadCount()
    ])
    notifications.value = (listRes.data?.content || listRes.data || [])
    unreadCount.value = countRes.data?.count ?? 0
  } catch {}
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

function toggleNotifPanel() {
  showNotifPanel.value = !showNotifPanel.value
  if (showNotifPanel.value) loadNotifications()
}

function toggleLocale() {
  const next = currentLocale.value === 'zh' ? 'en' : 'zh'
  setLocale(next as 'zh' | 'en')
  currentLocale.value = next
}

const compareCount = computed(() => robotStore.compareList.length)

function handleSearch() {
  if (searchQuery.value.trim()) {
    router.push({ name: 'robot-list', query: { q: searchQuery.value.trim() } })
    searchQuery.value = ''
  }
}

function logout() {
  authStore.logout()
  showUserMenu.value = false
  router.push('/')
}

function handleClickOutside(e: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(e.target as Node)) {
    showUserMenu.value = false
  }
  if (notifRef.value && !notifRef.value.contains(e.target as Node)) {
    showNotifPanel.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  if (authStore.isLoggedIn) {
    loadNotifications()
    // 每60秒轮询未读数
    const timer = setInterval(() => {
      if (authStore.isLoggedIn) {
        notificationService.getUnreadCount().then((res: any) => {
          unreadCount.value = res.data?.count ?? 0
        }).catch(() => {})
      }
    }, 60000)
    onUnmounted(() => clearInterval(timer))
  }
})

onUnmounted(() => document.removeEventListener('click', handleClickOutside))

watch(() => authStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) loadNotifications()
  else { notifications.value = []; unreadCount.value = 0 }
})
</script>
