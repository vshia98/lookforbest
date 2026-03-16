<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-white">广告管理</h1>
      <button
        @click="openCreateModal"
        class="bg-primary hover:bg-primary text-[#1a1a1a] text-sm px-4 py-2 rounded-lg transition-colors"
      >
        + 新建广告
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-dark-50 rounded-xl p-5  border border-white/[0.04]">
        <p class="text-sm text-gray-500">广告总数</p>
        <p class="text-2xl font-bold text-white mt-1">{{ stats.total }}</p>
      </div>
      <div class="bg-dark-50 rounded-xl p-5  border border-white/[0.04]">
        <p class="text-sm text-gray-500">投放中</p>
        <p class="text-2xl font-bold text-green-600 mt-1">{{ stats.active }}</p>
      </div>
      <div class="bg-dark-50 rounded-xl p-5  border border-white/[0.04]">
        <p class="text-sm text-gray-500">总点击数</p>
        <p class="text-2xl font-bold text-blue-400 mt-1">{{ stats.totalClicks }}</p>
      </div>
      <div class="bg-dark-50 rounded-xl p-5  border border-white/[0.04]">
        <p class="text-sm text-gray-500">平均CTR</p>
        <p class="text-2xl font-bold text-amber-600 mt-1">{{ stats.avgCtr }}%</p>
      </div>
    </div>

    <!-- 广告列表 -->
    <div class="bg-dark-50 rounded-xl  border border-white/[0.04] overflow-hidden">
      <div class="px-6 py-4 border-b border-white/[0.04]">
        <h2 class="font-semibold text-gray-300">广告列表</h2>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-dark-100">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">标题</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">位置</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">投放期</th>
              <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">状态</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">展示</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">点击</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">CTR</th>
              <th class="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/[0.04]">
            <tr v-if="loading" class="text-center">
              <td colspan="9" class="py-10 text-gray-400">加载中...</td>
            </tr>
            <tr v-else-if="ads.length === 0">
              <td colspan="9" class="py-10 text-center text-gray-400">暂无广告数据</td>
            </tr>
            <tr
              v-for="ad in ads"
              :key="ad.id"
              class="hover:bg-dark-100 transition-colors"
            >
              <td class="px-4 py-3">
                <div class="flex items-center gap-3">
                  <img
                    v-if="ad.imageUrl"
                    :src="ad.imageUrl"
                    class="w-10 h-7 object-cover rounded"
                    :alt="ad.title"
                  />
                  <div v-else class="w-10 h-7 bg-dark-100 rounded flex items-center justify-center text-gray-400 text-xs">图</div>
                  <span class="font-medium text-white truncate max-w-[160px]">{{ ad.title }}</span>
                </div>
              </td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded text-xs font-medium" :class="positionClass(ad.position)">
                  {{ positionLabel(ad.position) }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-400">{{ adTypeLabel(ad.adType) }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">
                <span v-if="ad.startDate || ad.endDate">
                  {{ ad.startDate || '∞' }} ~ {{ ad.endDate || '∞' }}
                </span>
                <span v-else class="text-gray-400">长期</span>
              </td>
              <td class="px-4 py-3 text-center">
                <button
                  @click="toggleActive(ad)"
                  class="w-11 h-6 rounded-full transition-colors relative"
                  :class="ad.isActive ? 'bg-green-900/200' : 'bg-gray-300'"
                >
                  <span
                    class="absolute top-0.5 w-5 h-5 bg-dark-50 rounded-full shadow transition-transform"
                    :class="ad.isActive ? 'translate-x-5' : 'translate-x-0.5'"
                  />
                </button>
              </td>
              <td class="px-4 py-3 text-right text-gray-400">{{ ad.impressionCount?.toLocaleString() ?? 0 }}</td>
              <td class="px-4 py-3 text-right text-gray-400">{{ ad.clickCount?.toLocaleString() ?? 0 }}</td>
              <td class="px-4 py-3 text-right font-medium" :class="ctrColor(calcCtr(ad))">
                {{ calcCtr(ad) }}%
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center justify-center gap-2">
                  <button
                    @click="openEditModal(ad)"
                    class="text-primary hover:text-primary text-xs px-2 py-1 rounded hover:bg-primary/10 transition-colors"
                  >
                    编辑
                  </button>
                  <button
                    @click="confirmDelete(ad)"
                    class="text-red-400 hover:text-red-600 text-xs px-2 py-1 rounded hover:bg-red-900/20 transition-colors"
                  >
                    删除
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- CTR 报告 -->
    <div class="bg-dark-50 rounded-xl  border border-white/[0.04] overflow-hidden">
      <div class="px-6 py-4 border-b border-white/[0.04] flex items-center justify-between">
        <h2 class="font-semibold text-gray-300">CTR 报告</h2>
        <button @click="loadCtrReport" class="text-xs text-primary hover:underline">刷新</button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-dark-100">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">广告标题</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">展示次数</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">点击次数</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">CTR</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/[0.04]">
            <tr v-if="ctrReport.length === 0">
              <td colspan="4" class="py-8 text-center text-gray-400">暂无数据</td>
            </tr>
            <tr v-for="row in ctrReport" :key="row.adId" class="hover:bg-dark-100">
              <td class="px-4 py-3 text-white">{{ row.title }}</td>
              <td class="px-4 py-3 text-right text-gray-400">{{ Number(row.impressions).toLocaleString() }}</td>
              <td class="px-4 py-3 text-right text-gray-400">{{ Number(row.clicks).toLocaleString() }}</td>
              <td class="px-4 py-3 text-right font-medium" :class="ctrColor(Number(row.ctr).toFixed(2))">
                {{ Number(row.ctr).toFixed(2) }}%
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新建/编辑广告弹窗 -->
    <Teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
        @click.self="showModal = false"
      >
        <div class="bg-dark-50 rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
          <div class="px-6 py-5 border-b border-white/[0.04] flex items-center justify-between">
            <h3 class="text-lg font-semibold text-white">{{ editingAd ? '编辑广告' : '新建广告' }}</h3>
            <button @click="showModal = false" class="text-gray-400 hover:text-gray-400 text-xl leading-none">&times;</button>
          </div>
          <form @submit.prevent="submitForm" class="px-6 py-5 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">标题 <span class="text-red-500">*</span></label>
              <input v-model="form.title" required class="input-dark" placeholder="广告标题" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">描述</label>
              <textarea v-model="form.description" rows="2" class="input-dark" placeholder="广告描述（可选）" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">图片URL</label>
              <input v-model="form.imageUrl" class="input-dark" placeholder="https://..." />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">跳转链接</label>
              <input v-model="form.linkUrl" class="input-dark" placeholder="https://..." />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">广告位置 <span class="text-red-500">*</span></label>
                <select v-model="form.position" required class="input-dark">
                  <option value="">请选择</option>
                  <option value="home_banner">首页横幅</option>
                  <option value="list_promoted">列表推广</option>
                  <option value="detail_sidebar">详情侧边</option>
                  <option value="search_top">搜索顶部</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">广告类型</label>
                <select v-model="form.adType" class="input-dark">
                  <option value="banner">横幅</option>
                  <option value="card">卡片</option>
                  <option value="text">文字</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">开始日期</label>
                <input v-model="form.startDate" type="date" class="input-dark" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">结束日期</label>
                <input v-model="form.endDate" type="date" class="input-dark" />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-300 mb-1">优先级</label>
                <input v-model.number="form.priority" type="number" class="input-dark" placeholder="0" />
              </div>
              <div class="flex items-end pb-1">
                <label class="flex items-center gap-2 cursor-pointer">
                  <input v-model="form.isActive" type="checkbox" class="w-4 h-4 rounded accent-primary-500" />
                  <span class="text-sm text-gray-300">立即投放</span>
                </label>
              </div>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button
                type="button"
                @click="showModal = false"
                class="px-4 py-2 text-sm text-gray-400 border border-white/[0.06] rounded-lg hover:bg-dark-100"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="submitting"
                class="px-4 py-2 text-sm bg-primary hover:bg-primary text-[#1a1a1a] rounded-lg disabled:opacity-60 transition-colors"
              >
                {{ submitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import http from '@/services/api'

interface Ad {
  id: number
  title: string
  description?: string
  imageUrl?: string
  linkUrl?: string
  position: string
  adType: string
  priority: number
  isActive: boolean
  startDate?: string
  endDate?: string
  manufacturerId?: number
  impressionCount: number
  clickCount: number
  createdAt: string
}

const ads = ref<Ad[]>([])
const ctrReport = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const editingAd = ref<Ad | null>(null)

const form = reactive({
  title: '',
  description: '',
  imageUrl: '',
  linkUrl: '',
  position: '',
  adType: 'banner',
  priority: 0,
  isActive: true,
  startDate: '',
  endDate: '',
  manufacturerId: undefined as number | undefined
})

const stats = computed(() => {
  const total = ads.value.length
  const active = ads.value.filter(a => a.isActive).length
  const totalClicks = ads.value.reduce((sum, a) => sum + (a.clickCount || 0), 0)
  const ctrs = ads.value.filter(a => a.impressionCount > 0).map(a => a.clickCount / a.impressionCount * 100)
  const avgCtr = ctrs.length > 0 ? (ctrs.reduce((s, c) => s + c, 0) / ctrs.length).toFixed(2) : '0.00'
  return { total, active, totalClicks, avgCtr }
})

function calcCtr(ad: Ad): string {
  if (!ad.impressionCount) return '0.00'
  return (ad.clickCount / ad.impressionCount * 100).toFixed(2)
}

function ctrColor(ctr: string) {
  const v = parseFloat(ctr)
  if (v >= 5) return 'text-green-600'
  if (v >= 2) return 'text-amber-500'
  return 'text-gray-500'
}

function positionLabel(pos: string) {
  const map: Record<string, string> = {
    home_banner: '首页横幅',
    list_promoted: '列表推广',
    detail_sidebar: '详情侧边',
    search_top: '搜索顶部'
  }
  return map[pos] || pos
}

function positionClass(pos: string) {
  const map: Record<string, string> = {
    home_banner: 'bg-blue-900/30 text-blue-400',
    list_promoted: 'bg-purple-900/30 text-purple-400',
    detail_sidebar: 'bg-orange-900/30 text-orange-400',
    search_top: 'bg-green-900/30 text-green-400'
  }
  return map[pos] || 'bg-dark-100 text-gray-400'
}

function adTypeLabel(type: string) {
  const map: Record<string, string> = { banner: '横幅', card: '卡片', text: '文字' }
  return map[type] || type
}

async function loadAds() {
  loading.value = true
  try {
    const res = await http.get('/admin/ads')
    ads.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadCtrReport() {
  try {
    const res = await http.get('/admin/ads/ctr-report')
    ctrReport.value = res.data || []
  } catch {
    ctrReport.value = []
  }
}

function openCreateModal() {
  editingAd.value = null
  Object.assign(form, {
    title: '', description: '', imageUrl: '', linkUrl: '',
    position: '', adType: 'banner', priority: 0, isActive: true,
    startDate: '', endDate: '', manufacturerId: undefined
  })
  showModal.value = true
}

function openEditModal(ad: Ad) {
  editingAd.value = ad
  Object.assign(form, {
    title: ad.title,
    description: ad.description || '',
    imageUrl: ad.imageUrl || '',
    linkUrl: ad.linkUrl || '',
    position: ad.position,
    adType: ad.adType,
    priority: ad.priority,
    isActive: ad.isActive,
    startDate: ad.startDate || '',
    endDate: ad.endDate || '',
    manufacturerId: ad.manufacturerId
  })
  showModal.value = true
}

async function submitForm() {
  submitting.value = true
  try {
    const payload = {
      ...form,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      description: form.description || null,
      imageUrl: form.imageUrl || null,
      linkUrl: form.linkUrl || null,
      manufacturerId: form.manufacturerId || null
    }
    if (editingAd.value) {
      await http.put(`/admin/ads/${editingAd.value.id}`, payload)
    } else {
      await http.post('/admin/ads', payload)
    }
    showModal.value = false
    await loadAds()
  } finally {
    submitting.value = false
  }
}

async function toggleActive(ad: Ad) {
  try {
    await http.put(`/admin/ads/${ad.id}`, {
      title: ad.title,
      description: ad.description,
      imageUrl: ad.imageUrl,
      linkUrl: ad.linkUrl,
      position: ad.position,
      adType: ad.adType,
      priority: ad.priority,
      isActive: !ad.isActive,
      startDate: ad.startDate || null,
      endDate: ad.endDate || null,
      manufacturerId: ad.manufacturerId || null
    })
    ad.isActive = !ad.isActive
  } catch {
    // revert not needed since we haven't updated locally yet
  }
}

async function confirmDelete(ad: Ad) {
  if (!confirm(`确定删除广告「${ad.title}」吗？`)) return
  await http.delete(`/admin/ads/${ad.id}`)
  await loadAds()
}

onMounted(() => {
  loadAds()
  loadCtrReport()
})
</script>
