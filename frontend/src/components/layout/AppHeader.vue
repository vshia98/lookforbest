<template>
  <header class="glass sticky top-0 z-40">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <router-link to="/" class="flex items-center gap-2 font-bold text-xl text-white group">
          <span class="text-primary group-hover:drop-shadow-[0_0_8px_rgba(0,255,209,0.5)] transition-all">⚡</span>
          <span>寻优</span>
        </router-link>

        <!-- Search -->
        <form @submit.prevent="handleSearch" class="hidden md:flex flex-1 max-w-lg mx-8">
          <div class="relative w-full">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索机器人、厂商、型号..."
              class="input-dark w-full pl-4 pr-10"
            />
            <button type="submit" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-primary transition-colors">
              🔍
            </button>
          </div>
        </form>

        <!-- Nav -->
        <nav class="flex items-center gap-4">
          <router-link to="/robots" class="text-sm text-gray-400 hover:text-primary transition-colors font-medium">机器人库</router-link>
          <router-link to="/compare" class="text-sm text-gray-400 hover:text-primary transition-colors font-medium relative">
            对比
            <span v-if="compareCount > 0" class="absolute -top-2 -right-3 bg-primary text-dark text-xs rounded-full w-4 h-4 flex items-center justify-center font-bold">
              {{ compareCount }}
            </span>
          </router-link>

          <!-- Language -->
          <button
            @click="toggleLocale"
            class="text-xs font-medium text-gray-500 hover:text-primary border border-white/10 hover:border-primary/30 px-2 py-1 rounded-lg transition-all"
          >
            {{ currentLocale === 'zh' ? 'EN' : '中' }}
          </button>

          <!-- Not logged in -->
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="text-sm text-gray-400 hover:text-primary transition-colors">登录</router-link>
            <router-link to="/register" class="btn-primary text-sm">注册</router-link>
          </template>

          <!-- Logged in -->
          <template v-else>
            <!-- Notifications -->
            <div class="relative" ref="notifRef">
              <button
                @click="toggleNotifPanel"
                class="relative w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 hover:text-primary hover:bg-white/5 transition-all"
                title="通知"
              >
                🔔
                <span v-if="unreadCount > 0" class="absolute -top-1 -right-1 bg-accent-red text-white text-xs rounded-full min-w-[16px] h-4 flex items-center justify-center px-0.5 leading-none">
                  {{ unreadCount > 99 ? '99+' : unreadCount }}
                </span>
              </button>
              <!-- Notification panel -->
              <div v-if="showNotifPanel" class="absolute right-0 top-10 w-80 glass rounded-xl shadow-xl z-50 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-3 border-b border-white/[0.06]">
                  <span class="font-semibold text-sm text-white">通知</span>
                  <button v-if="unreadCount > 0" @click="markAllRead" class="text-xs text-primary hover:underline">全部已读</button>
                </div>
                <div v-if="notifications.length === 0" class="text-center py-8 text-gray-500 text-sm">暂无通知</div>
                <ul v-else class="max-h-80 overflow-y-auto divide-y divide-white/[0.04]">
                  <li
                    v-for="n in notifications"
                    :key="n.id"
                    @click="markRead(n)"
                    class="px-4 py-3 cursor-pointer hover:bg-white/[0.04] transition-colors"
                    :class="n.isRead ? '' : 'bg-primary/5'"
                  >
                    <div class="flex items-start gap-2">
                      <span class="text-base mt-0.5">{{ typeIcon(n.type) }}</span>
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-white truncate">{{ n.title }}</p>
                        <p v-if="n.content" class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ n.content }}</p>
                        <p class="text-xs text-gray-600 mt-1">{{ formatTime(n.createdAt) }}</p>
                      </div>
                      <div v-if="!n.isRead" class="w-2 h-2 bg-primary rounded-full mt-1.5 flex-shrink-0"></div>
                    </div>
                  </li>
                </ul>
                <div class="border-t border-white/[0.06] px-4 py-2.5">
                  <router-link to="/user/notifications" @click="showNotifPanel = false" class="text-xs text-primary hover:underline">查看全部通知</router-link>
                </div>
              </div>
            </div>

            <!-- User menu -->
            <div class="relative" ref="menuRef">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors"
              >
                <div class="w-8 h-8 bg-primary/20 text-primary rounded-full flex items-center justify-center font-medium text-xs border border-primary/30">
                  {{ authStore.user?.username?.charAt(0).toUpperCase() }}
                </div>
                <span class="hidden md:inline">{{ authStore.user?.username }}</span>
                <span class="text-xs">▼</span>
              </button>
              <div v-if="showUserMenu" class="absolute right-0 top-10 w-48 glass rounded-xl shadow-xl py-1 z-50">
                <router-link to="/user/profile" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  👤 个人资料
                </router-link>
                <router-link to="/user/favorites" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  ⭐ 我的收藏
                </router-link>
                <router-link to="/user/notifications" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  🔔 通知中心
                </router-link>
                <router-link to="/user/membership" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  👑 会员中心
                </router-link>
                <router-link to="/reviews" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  ✍️ 评测社区
                </router-link>
                <router-link to="/manufacturer-portal/apply" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  🏭 厂商入驻
                </router-link>
                <hr class="my-1 border-white/[0.06]" />
                <router-link v-if="authStore.isAdmin" to="/admin" class="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:text-primary hover:bg-white/[0.04]">
                  ⚙️ 管理后台
                </router-link>
                <button @click="logout" class="w-full flex items-center gap-2 px-4 py-2 text-sm text-accent-red hover:bg-accent-red/10">
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
import { notificationService, type NotificationDTO } from '@/services/notifications'

const router = useRouter()
const authStore = useAuthStore()
const robotStore = useRobotStore()

const searchQuery = ref('')
const showUserMenu = ref(false)
const menuRef = ref<HTMLElement | null>(null)
const currentLocale = ref(getLocale())

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
