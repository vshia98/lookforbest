<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <!-- 页头 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">机器人产品库</h1>
      <p class="text-gray-500 mt-1">共 {{ total }} 款产品</p>
    </div>

    <div class="flex gap-6">
      <!-- 筛选面板 -->
      <FilterPanel
        v-model="filters"
        :categories="facets.categories"
        :manufacturers="facets.manufacturers"
        @apply="loadRobots"
        class="hidden lg:block"
      />

      <!-- 主内容 -->
      <div class="flex-1 min-w-0">
        <!-- 排序 & 搜索 -->
        <div class="flex items-center gap-3 mb-5">
          <SearchBox
            v-model="filters.q"
            class="flex-1"
            placeholder="搜索机器人名称、型号、厂商..."
            @search="onSearch"
          />
          <select
            v-model="filters.sort"
            @change="loadRobots"
            class="border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="relevance">综合排序</option>
            <option value="newest">最新上架</option>
            <option value="popular">最受欢迎</option>
          </select>
        </div>

        <!-- 加载中 -->
        <LoadingSpinner v-if="loading" text="加载中..." />

        <!-- 空状态 -->
        <div v-else-if="robots.length === 0" class="text-center py-20">
          <div class="text-5xl mb-4">🤖</div>
          <p class="text-gray-500">未找到符合条件的机器人</p>
          <button @click="resetFilters" class="mt-4 text-primary-500 hover:underline text-sm">清除筛选条件</button>
        </div>

        <!-- 机器人网格（每6个插入一个推广卡片） -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
          <template v-for="(robot, index) in robots" :key="robot.id">
            <AdCard
              v-if="index > 0 && index % 6 === 0 && listAds.length > 0"
              :ad="listAds[(index / 6 - 1) % listAds.length]"
            />
            <RobotCard
              :robot="robot"
              :highlights="highlightMap[String(robot.id)]"
            />
          </template>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
          <button
            :disabled="currentPage === 0"
            @click="changePage(currentPage - 1)"
            class="px-4 py-2 rounded-lg border border-gray-200 text-sm disabled:opacity-40 hover:bg-gray-50"
          >
            上一页
          </button>
          <template v-for="p in pageNumbers" :key="p">
            <span v-if="p === '...'" class="px-2 text-gray-400">...</span>
            <button
              v-else
              @click="changePage(p as number)"
              class="w-9 h-9 rounded-lg text-sm"
              :class="p === currentPage ? 'bg-primary-500 text-white' : 'border border-gray-200 hover:bg-gray-50'"
            >
              {{ (p as number) + 1 }}
            </button>
          </template>
          <button
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
            class="px-4 py-2 rounded-lg border border-gray-200 text-sm disabled:opacity-40 hover:bg-gray-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import RobotCard from '@/components/robot/RobotCard.vue'
import AdCard from '@/components/ui/AdCard.vue'
import FilterPanel from '@/components/robot/FilterPanel.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import SearchBox from '@/components/ui/SearchBox.vue'
import { robotService, searchService, adService } from '@/services/robots'
import type { RobotListItem } from '@/types/robot'

const route = useRoute()
const router = useRouter()

const robots = ref<RobotListItem[]>([])
const total = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const facets = reactive({ categories: [] as any[], manufacturers: [] as any[] })
const highlightMap = ref<Record<string, Record<string, string[]>>>({})
const listAds = ref<any[]>([])

const filters = reactive<Record<string, any>>({
  q: route.query.q as string || '',
  sort: 'relevance',
  categoryId: undefined,
  manufacturerId: undefined,
  payloadMin: undefined,
  payloadMax: undefined,
  reachMin: undefined,
  reachMax: undefined,
  has3dModel: undefined
})

const pageNumbers = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const cur = currentPage.value
  if (total <= 7) {
    for (let i = 0; i < total; i++) pages.push(i)
  } else {
    pages.push(0)
    if (cur > 2) pages.push('...')
    for (let i = Math.max(1, cur - 1); i <= Math.min(total - 2, cur + 1); i++) pages.push(i)
    if (cur < total - 3) pages.push('...')
    pages.push(total - 1)
  }
  return pages
})

function onSearch(q: string) {
  filters.q = q
  currentPage.value = 0
  loadRobots()
}

async function loadRobots() {
  loading.value = true
  highlightMap.value = {}
  try {
    const params: any = { ...filters, page: currentPage.value, size: 18 }
    Object.keys(params).forEach(k => { if (params[k] === undefined || params[k] === '') delete params[k] })

    // 有搜索词时走 ES 搜索接口，否则走普通列表
    if (params.q) {
      const res = await searchService.search(params)
      const esData = res.data
      // ES 接口返回结构: { hits, total, page, size, totalPages }
      robots.value = (esData.hits || []).map((h: any) => ({
        id: Number(h.id),
        name: h.name,
        nameEn: h.nameEn,
        slug: h.slug,
        modelNumber: h.modelNumber,
        coverImageUrl: h.coverImageUrl,
        priceRange: h.priceRange || 'inquiry',
        has3dModel: h.has3dModel ?? false,
        viewCount: h.viewCount ?? 0,
        isFeatured: h.isFeatured ?? false,
        manufacturer: { id: h.manufacturerId || 0, name: h.manufacturerName || '', country: '' },
        category: { id: h.categoryId || 0, name: h.categoryName || '', slug: '' }
      }))
      total.value = esData.total || 0
      totalPages.value = esData.totalPages || 1
      // 收集高亮片段
      const map: Record<string, Record<string, string[]>> = {}
      ;(esData.hits || []).forEach((h: any) => {
        if (h.highlights) map[String(h.id)] = h.highlights
      })
      highlightMap.value = map
    } else {
      const res = await robotService.getList(params)
      robots.value = res.data.content
      total.value = res.data.total
      totalPages.value = res.data.totalPages
      if (res.data.facets) {
        facets.categories = res.data.facets.categories || []
        facets.manufacturers = res.data.facets.manufacturers || []
      }
    }
  } finally {
    loading.value = false
  }
}

function changePage(page: number) {
  currentPage.value = page
  loadRobots()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function resetFilters() {
  Object.keys(filters).forEach(k => { filters[k] = undefined })
  filters.sort = 'relevance'
  filters.q = ''
  loadRobots()
}

watch(() => route.query.q, (q) => {
  if (q) {
    filters.q = q as string
    loadRobots()
  }
})

async function loadListAds() {
  try {
    const res = await adService.getAds('list_promoted')
    listAds.value = res.data || []
  } catch {
    listAds.value = []
  }
}

onMounted(() => {
  loadRobots()
  loadListAds()
})
</script>

<style scoped>
:deep(.highlight) {
  background-color: #fef08a;
  color: #713f12;
  border-radius: 2px;
  padding: 0 2px;
  font-style: normal;
}
</style>
