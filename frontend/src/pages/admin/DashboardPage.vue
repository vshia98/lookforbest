<template>
  <div>
    <h2 class="text-xl font-bold text-white mb-6">仪表盘</h2>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
      <div v-for="card in statCards" :key="card.label" class="bg-dark-50 rounded-xl border border-white/[0.04]  p-5">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center text-lg" :class="card.iconBg">{{ card.icon }}</div>
          <div>
            <p class="text-2xl font-bold text-white">
              <span v-if="loadingStats" class="inline-block w-8 h-5 bg-dark-100 rounded animate-pulse"></span>
              <span v-else>{{ card.value }}</span>
            </p>
            <p class="text-sm text-gray-500">{{ card.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
      <!-- 状态分布 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">机器人状态分布</h3>
        <div class="space-y-3">
          <div v-for="item in statusDistribution" :key="item.label" class="flex items-center gap-3">
            <div class="w-2.5 h-2.5 rounded-full flex-shrink-0" :class="item.color"></div>
            <span class="text-sm text-gray-400 flex-1">{{ item.label }}</span>
            <span class="text-sm font-medium text-white">{{ item.value }}</span>
            <div class="w-24 bg-dark-100 rounded-full h-1.5">
              <div class="h-1.5 rounded-full transition-all" :class="item.color" :style="{ width: item.pct + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分类分布 Top 5 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">分类分布（Top 5）</h3>
        <div v-if="loadingStats" class="space-y-2">
          <div v-for="i in 5" :key="i" class="h-4 bg-dark-100 rounded animate-pulse"></div>
        </div>
        <div v-else class="space-y-3">
          <div v-for="item in topCategories" :key="item.name" class="flex items-center gap-3">
            <span class="text-sm text-gray-400 flex-1 truncate">{{ item.name }}</span>
            <span class="text-sm font-medium text-white">{{ item.count }}</span>
            <div class="w-24 bg-dark-100 rounded-full h-1.5">
              <div class="h-1.5 rounded-full bg-primary-400 transition-all" :style="{ width: item.pct + '%' }"></div>
            </div>
          </div>
          <p v-if="topCategories.length === 0" class="text-sm text-gray-400 text-center py-2">暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 热门机器人 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6 mb-5">
      <h3 class="font-semibold text-white mb-4">热门机器人（按浏览量 Top 10）</h3>
      <div v-if="loadingStats" class="space-y-2">
        <div v-for="i in 5" :key="i" class="h-8 bg-dark-100 rounded animate-pulse"></div>
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-white/[0.04]">
              <th class="text-left py-2 text-gray-500 font-medium">#</th>
              <th class="text-left py-2 text-gray-500 font-medium">机器人</th>
              <th class="text-right py-2 text-gray-500 font-medium">浏览量</th>
              <th class="text-right py-2 text-gray-500 font-medium">收藏量</th>
              <th class="text-right py-2 text-gray-500 font-medium">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, i) in topRobots" :key="r.id" class="border-b border-white/[0.04] hover:bg-dark-100">
              <td class="py-2 text-gray-400 w-8">{{ i + 1 }}</td>
              <td class="py-2 text-white">
                <router-link :to="`/admin/robots/${r.id}/edit`" class="hover:text-primary hover:underline">{{ r.name }}</router-link>
              </td>
              <td class="py-2 text-right text-gray-400">{{ r.viewCount?.toLocaleString() }}</td>
              <td class="py-2 text-right text-gray-400">{{ r.favoriteCount?.toLocaleString() }}</td>
              <td class="py-2 text-right">
                <span class="px-2 py-0.5 text-xs rounded-full" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
              </td>
            </tr>
            <tr v-if="topRobots.length === 0">
              <td colspan="5" class="py-8 text-center text-gray-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===== 用户行为分析 ===== -->
    <div class="mt-8 mb-2 flex items-center justify-between">
      <h2 class="text-xl font-bold text-white">用户行为分析</h2>
      <select v-model="analyticsDays" @change="loadAnalytics" class="input-dark text-sm">
        <option :value="7">近 7 天</option>
        <option :value="14">近 14 天</option>
        <option :value="30">近 30 天</option>
      </select>
    </div>

    <!-- 行为概览卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-5">
      <div v-for="card in analyticsCards" :key="card.label" class="bg-dark-50 rounded-xl border border-white/[0.04]  p-5">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg flex items-center justify-center text-lg" :class="card.iconBg">{{ card.icon }}</div>
          <div>
            <p class="text-2xl font-bold text-white">
              <span v-if="loadingAnalytics" class="inline-block w-10 h-5 bg-dark-100 rounded animate-pulse"></span>
              <span v-else>{{ card.value }}</span>
            </p>
            <p class="text-sm text-gray-500">{{ card.label }}</p>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
      <!-- 每日行为趋势 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">每日行为趋势（近 {{ analyticsDays }} 天）</h3>
        <div v-if="loadingAnalytics" class="h-32 bg-dark-100 rounded animate-pulse"></div>
        <div v-else-if="dailyTrends.length === 0" class="h-32 flex items-center justify-center text-sm text-gray-400">暂无数据</div>
        <div v-else class="relative">
          <div class="flex items-end gap-1 h-32">
            <div
              v-for="item in dailyTrends"
              :key="item.date"
              class="flex-1 bg-primary-400 rounded-t hover:bg-primary transition-colors cursor-default group relative"
              :style="{ height: barHeight(item.count, maxDailyCount) + '%', minHeight: '2px' }"
            >
              <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-1 hidden group-hover:block bg-gray-800 text-white text-xs rounded px-2 py-1 whitespace-nowrap z-10">
                {{ item.date }}<br/>{{ item.count }} 次
              </div>
            </div>
          </div>
          <div class="flex justify-between mt-1 text-xs text-gray-400">
            <span>{{ dailyTrends[0]?.date }}</span>
            <span>{{ dailyTrends[dailyTrends.length - 1]?.date }}</span>
          </div>
        </div>
      </div>

      <!-- 行为类型分布 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">行为类型分布</h3>
        <div v-if="loadingAnalytics" class="space-y-2">
          <div v-for="i in 4" :key="i" class="h-4 bg-dark-100 rounded animate-pulse"></div>
        </div>
        <div v-else class="space-y-3">
          <div v-for="item in actionDistribution" :key="item.actionType" class="flex items-center gap-3">
            <span class="text-lg w-6 text-center">{{ actionIcon(item.actionType) }}</span>
            <span class="text-sm text-gray-400 flex-1">{{ actionLabel(item.actionType) }}</span>
            <span class="text-sm font-medium text-white">{{ Number(item.count).toLocaleString() }}</span>
            <div class="w-24 bg-dark-100 rounded-full h-1.5">
              <div class="h-1.5 rounded-full bg-indigo-400 transition-all" :style="{ width: actionPct(item.count) + '%' }"></div>
            </div>
          </div>
          <p v-if="actionDistribution.length === 0" class="text-sm text-gray-400 text-center py-2">暂无数据</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
      <!-- 热门机器人（行为日志） -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">热门机器人（近 {{ analyticsDays }} 天浏览）</h3>
        <div v-if="loadingAnalytics" class="space-y-2">
          <div v-for="i in 5" :key="i" class="h-6 bg-dark-100 rounded animate-pulse"></div>
        </div>
        <div v-else class="space-y-2">
          <div v-for="(item, i) in topRobotsByLog" :key="item.robotId" class="flex items-center gap-3">
            <span class="text-xs text-gray-400 w-5 text-right">{{ i + 1 }}</span>
            <span class="text-sm text-gray-300 flex-1 truncate">{{ item.robotName }}</span>
            <span class="text-sm font-medium text-white">{{ Number(item.viewCount).toLocaleString() }}</span>
            <div class="w-16 bg-dark-100 rounded-full h-1.5">
              <div class="h-1.5 rounded-full bg-rose-400 transition-all" :style="{ width: robotPct(item.viewCount) + '%' }"></div>
            </div>
          </div>
          <p v-if="topRobotsByLog.length === 0" class="text-sm text-gray-400 text-center py-4">暂无数据</p>
        </div>
      </div>

      <!-- 热门搜索词 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">热门搜索词（近 {{ analyticsDays }} 天）</h3>
        <div v-if="loadingAnalytics" class="space-y-2">
          <div v-for="i in 5" :key="i" class="h-6 bg-dark-100 rounded animate-pulse"></div>
        </div>
        <div v-else class="space-y-2">
          <div v-for="(item, i) in topKeywords" :key="item.keyword" class="flex items-center gap-3">
            <span class="text-xs text-gray-400 w-5 text-right">{{ i + 1 }}</span>
            <span class="text-sm text-gray-300 flex-1 truncate">{{ item.keyword }}</span>
            <span class="text-sm font-medium text-white">{{ Number(item.count).toLocaleString() }}</span>
            <div class="w-16 bg-dark-100 rounded-full h-1.5">
              <div class="h-1.5 rounded-full bg-amber-400 transition-all" :style="{ width: keywordPct(item.count) + '%' }"></div>
            </div>
          </div>
          <p v-if="topKeywords.length === 0" class="text-sm text-gray-400 text-center py-4">暂无搜索数据</p>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
      <h3 class="font-semibold text-white mb-4">快速操作</h3>
      <div class="flex flex-wrap gap-3">
        <router-link to="/admin/robots/create" class="bg-primary text-[#1a1a1a] text-sm px-4 py-2 rounded-lg hover:bg-primary transition-colors">
          + 添加机器人
        </router-link>
        <router-link to="/admin/manufacturers" class="bg-dark-100 text-gray-300 text-sm px-4 py-2 rounded-lg hover:bg-white/[0.06] transition-colors">
          管理厂商
        </router-link>
        <router-link to="/robots" target="_blank" class="bg-dark-100 text-gray-300 text-sm px-4 py-2 rounded-lg hover:bg-white/[0.06] transition-colors">
          查看前台 →
        </router-link>
        <button @click="loadAll" class="bg-dark-100 text-gray-300 text-sm px-4 py-2 rounded-lg hover:bg-white/[0.06] transition-colors">
          刷新数据
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminService } from '@/services/robots'

// -------- 基础统计 --------
const loadingStats = ref(false)
const stats = ref<any>(null)

const statCards = computed(() => {
  const s = stats.value
  return [
    { label: '机器人总数', icon: '🤖', iconBg: 'bg-primary-100 text-primary', value: s?.totalRobots ?? '-' },
    { label: '厂商总数', icon: '🏭', iconBg: 'bg-green-900/30 text-green-600', value: s?.totalManufacturers ?? '-' },
    { label: '注册用户', icon: '👤', iconBg: 'bg-blue-900/30 text-blue-400', value: s?.totalUsers ?? '-' },
    { label: '总浏览量', icon: '👁', iconBg: 'bg-purple-900/30 text-purple-600', value: s?.totalViews?.toLocaleString() ?? '-' },
  ]
})

const statusDistribution = computed(() => {
  const s = stats.value
  if (!s) return []
  const total = s.totalRobots || 1
  return [
    { label: '已上线', value: s.activeRobots ?? 0, color: 'bg-green-400', pct: Math.round(((s.activeRobots ?? 0) / total) * 100) },
    { label: '已停产', value: s.discontinuedRobots ?? 0, color: 'bg-gray-400', pct: Math.round(((s.discontinuedRobots ?? 0) / total) * 100) },
    { label: '即将上市', value: s.upcomingRobots ?? 0, color: 'bg-yellow-400', pct: Math.round(((s.upcomingRobots ?? 0) / total) * 100) },
  ]
})

const topCategories = computed(() => {
  const list: any[] = stats.value?.categoryDistribution?.slice(0, 5) || []
  const max = (list[0]?.count as number) || 1
  return list.map(item => ({ ...item, pct: Math.round((item.count / max) * 100) }))
})

const topRobots = computed(() => stats.value?.topRobots || [])

function statusLabel(s: string) {
  const map: Record<string, string> = { active: '上线', discontinued: '停产', upcoming: '即将上市' }
  return map[s] ?? s
}
function statusClass(s: string) {
  const map: Record<string, string> = {
    active: 'bg-green-900/30 text-green-400',
    discontinued: 'bg-dark-100 text-gray-500',
    upcoming: 'bg-yellow-900/30 text-yellow-400'
  }
  return map[s] ?? 'bg-dark-100 text-gray-500'
}

async function loadStats() {
  loadingStats.value = true
  try {
    const res: any = await adminService.getStats()
    stats.value = res.data ?? res ?? {}
  } catch (e) {
    console.error(e)
  } finally {
    loadingStats.value = false
  }
}

// -------- 行为分析 --------
const loadingAnalytics = ref(false)
const analyticsDays = ref(7)
const analyticsOverview = ref<any>(null)
const dailyTrends = ref<any[]>([])
const topRobotsByLog = ref<any[]>([])
const topKeywords = ref<any[]>([])

const analyticsCards = computed(() => {
  const o = analyticsOverview.value
  const days = analyticsDays.value
  return [
    {
      label: `近${days}天行为数`,
      icon: '📊',
      iconBg: 'bg-indigo-100 text-indigo-600',
      value: (days <= 7 ? o?.last7dActions : o?.last30dActions)?.toLocaleString() ?? '-'
    },
    {
      label: '今日行为数',
      icon: '⚡',
      iconBg: 'bg-orange-900/30 text-orange-600',
      value: o?.todayActions?.toLocaleString() ?? '-'
    },
    {
      label: '今日活跃会话',
      icon: '🔗',
      iconBg: 'bg-teal-100 text-teal-600',
      value: o?.todaySessions?.toLocaleString() ?? '-'
    },
    {
      label: `近${days}天活跃用户`,
      icon: '👥',
      iconBg: 'bg-pink-100 text-pink-600',
      value: (days <= 7 ? o?.last7dActiveUsers : o?.todayActiveUsers)?.toLocaleString() ?? '-'
    },
  ]
})

const maxDailyCount = computed(() => Math.max(1, ...dailyTrends.value.map((d: any) => Number(d.count))))

function barHeight(count: number, max: number): number {
  return Math.max(4, Math.round((Number(count) / max) * 100))
}

const actionDistribution = computed(() => analyticsOverview.value?.last30dActionDistribution || [])

const maxActionCount = computed(() => Math.max(1, ...actionDistribution.value.map((d: any) => Number(d.count))))

function actionPct(count: number): number {
  return Math.round((Number(count) / maxActionCount.value) * 100)
}

function robotPct(count: number): number {
  const max = Math.max(1, ...topRobotsByLog.value.map((r: any) => Number(r.viewCount)))
  return Math.round((Number(count) / max) * 100)
}

function keywordPct(count: number): number {
  const max = Math.max(1, ...topKeywords.value.map((k: any) => Number(k.count)))
  return Math.round((Number(count) / max) * 100)
}

function actionLabel(t: string) {
  const map: Record<string, string> = {
    view: '浏览', favorite: '收藏', compare: '对比', search: '搜索', click: '点击', share: '分享'
  }
  return map[t] ?? t
}

function actionIcon(t: string) {
  const map: Record<string, string> = {
    view: '👁', favorite: '❤️', compare: '⚖️', search: '🔍', click: '👆', share: '📤'
  }
  return map[t] ?? '•'
}

async function loadAnalytics() {
  loadingAnalytics.value = true
  try {
    const [overviewRes, trendsRes, topRobotsRes, keywordsRes] = await Promise.all([
      adminService.getAnalyticsOverview(),
      adminService.getAnalyticsDailyTrends(analyticsDays.value),
      adminService.getAnalyticsTopRobots(analyticsDays.value, 10),
      adminService.getAnalyticsTopKeywords(analyticsDays.value, 10),
    ])
    analyticsOverview.value = overviewRes.data ?? overviewRes ?? {}
    dailyTrends.value = trendsRes.data ?? trendsRes ?? []
    topRobotsByLog.value = topRobotsRes.data ?? topRobotsRes ?? []
    topKeywords.value = keywordsRes.data ?? keywordsRes ?? []
  } catch (e) {
    console.error('Analytics load failed', e)
  } finally {
    loadingAnalytics.value = false
  }
}

function loadAll() {
  loadStats()
  loadAnalytics()
}

onMounted(loadAll)
</script>
