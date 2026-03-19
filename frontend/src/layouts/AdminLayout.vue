<template>
  <div class="min-h-screen flex bg-dark-200">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-dark-100 border-r border-white/[0.04] text-white flex flex-col">
      <div class="px-6 py-5 border-b border-white/[0.06]">
        <router-link to="/" class="text-xl font-bold text-white flex items-center gap-2">
          <span class="text-primary drop-shadow-[0_0_6px_rgba(0,255,209,0.4)]">⚡</span>
          <span>寻优 / RobotLas</span>
        </router-link>
        <p class="text-gray-500 text-xs mt-1">管理后台</p>
      </div>
      <nav class="flex-1 px-4 py-4 space-y-1">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          :class="[
            'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all',
            isActive(item.to) ? 'bg-primary/10 text-primary border border-primary/20' : 'text-gray-400 hover:bg-white/[0.04] hover:text-white'
          ]"
        >
          <span>{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
      <div class="px-4 py-4 border-t border-white/[0.06]">
        <router-link to="/" class="flex items-center gap-2 text-sm text-gray-500 hover:text-primary transition-colors">
          <span>←</span> 返回前台
        </router-link>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="flex-1 flex flex-col min-w-0">
      <header class="bg-dark-50 border-b border-white/[0.04] px-6 py-4 flex items-center justify-between">
        <h1 class="text-lg font-semibold text-white">管理控制台</h1>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">管理员</span>
          <button @click="logout" class="text-sm text-accent-red hover:brightness-125 transition-all">退出</button>
        </div>
      </header>
      <main class="flex-1 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { to: '/admin', icon: '📊', label: '仪表盘' },
  { to: '/admin/robots', icon: '🤖', label: '机器人管理' },
  { to: '/admin/robot-tags', icon: '🏷️', label: '标签管理' },
  { to: '/admin/manufacturers', icon: '🏭', label: '厂商管理' },
  { to: '/admin/inquiries', icon: '📧', label: '询价管理' },
  { to: '/admin/announcement', icon: '📢', label: '系统公告' },
  { to: '/admin/crawler', icon: '🕷️', label: '爬虫管理' },
  { to: '/admin/ugc', icon: '✍️', label: '内容审核' },
  { to: '/admin/ads', icon: '💰', label: '广告管理' },
  { to: '/admin/manufacturer-applications', icon: '📋', label: '厂商申请' },
  { to: '/admin/membership', icon: '👑', label: '会员管理' },
  { to: '/admin/settings', icon: '⚙️', label: '站点设置' }
]

function isActive(to: string): boolean {
  if (to === '/admin') return route.path === '/admin'
  return route.path.startsWith(to)
}

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>
