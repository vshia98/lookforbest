<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <!-- 页头 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-white">{{ t('nav.robots') }}</h1>
      <p class="text-gray-500 mt-1">
        {{ t('common.all') }} {{ total }} {{ t('robots.label') || '' }}
      </p>
    </div>

    <!-- 顶部广告 -->
    <AdBanner position="list_top" class="mb-5" />

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
            :placeholder="t('home.searchPlaceholder')"
            @search="onSearch"
          />
          <select
            v-model="filters.sort"
            @change="loadRobots"
            class="bg-dark-50 border border-white/[0.06] rounded-lg px-3 py-2.5 text-sm text-gray-300 focus:outline-none focus:ring-2 focus:ring-primary/40"
          >
            <option value="relevance">{{ t('filter.sort_relevance') }}</option>
            <option value="newest">{{ t('filter.sort_newest') }}</option>
            <option value="popular">{{ t('filter.sort_viewCount') }}</option>
          </select>
        </div>

        <!-- 加载中 -->
        <LoadingSpinner v-if="loading" :text="t('common.loading')" />

        <!-- 空状态 -->
        <div v-else-if="robots.length === 0" class="text-center py-20">
          <div class="text-5xl mb-4">🤖</div>
          <p class="text-gray-500">{{ t('common.noData') }}</p>
          <button @click="resetFilters" class="mt-4 text-primary hover:underline text-sm">
            {{ t('filter.reset') || t('common.reset') }}
          </button>
        </div>

        <!-- 机器人网格 -->
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
            class="px-4 py-2 rounded-lg border border-white/10 text-sm text-gray-400 disabled:opacity-40 hover:border-primary/30 hover:text-primary transition-all"
          >
            {{ t('common.back') }}
          </button>
          <template v-for="p in pageNumbers" :key="p">
            <span v-if="p === '...'" class="px-2 text-gray-600">...</span>
            <button
              v-else
              @click="changePage(p as number)"
              class="w-9 h-9 rounded-lg text-sm transition-all"
              :class="p === currentPage ? 'bg-primary text-gray-900 font-bold' : 'border border-white/10 text-gray-400 hover:border-primary/30 hover:text-primary'"
            >
              {{ (p as number) + 1 }}
            </button>
          </template>
          <button
            :disabled="currentPage >= totalPages - 1"
            @click="changePage(currentPage + 1)"
            class="px-4 py-2 rounded-lg border border-white/10 text-sm text-gray-400 disabled:opacity-40 hover:border-primary/30 hover:text-primary transition-all"
          >
            {{ t('common.more') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import RobotCard from '@/components/robot/RobotCard.vue'
import AdCard from '@/components/ui/AdCard.vue'
import AdBanner from '@/components/ui/AdBanner.vue'
import FilterPanel from '@/components/robot/FilterPanel.vue'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import SearchBox from '@/components/ui/SearchBox.vue'
import http from '@/services/api'
import { robotService, searchService, adService } from '@/services/robots'
import type { RobotListItem } from '@/types/robot'

const { t } = useI18n()

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

const pendingCategorySlug = ref(route.query.category as string || '')

const filters = reactive<Record<string, any>>({
  q: route.query.q as string || '',
  sort: 'relevance',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
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
    // 如果有 category slug 但还没解析成 ID，查分类接口匹配
    if (pendingCategorySlug.value && !filters.categoryId) {
      try {
        const catRes: any = await http.get('/categories')
        const cats = catRes.data || catRes || []
        const slug = pendingCategorySlug.value
        const match = cats.find((c: any) => c.slug === slug || c.name === slug)
        if (match) filters.categoryId = match.id
      } catch {}
      pendingCategorySlug.value = ''
    }

    const params: any = { ...filters, page: currentPage.value, size: 18 }
    Object.keys(params).forEach(k => { if (params[k] === undefined || params[k] === '') delete params[k] })

    if (params.q) {
      const res = await searchService.search(params)
      const esData = res.data
      robots.value = (esData.hits || []).map((h: any) => ({
        id: Number(h.id),
        name: h.name,
        nameEn: h.nameEn,
        // 简短描述：中英文都带上，卡片组件会根据当前语言选择 description 或 descriptionEn
        description: h.description || '',
        descriptionEn: h.descriptionEn || '',
        slug: h.slug,
        modelNumber: h.modelNumber,
        coverImageUrl: h.coverImageUrl,
        priceRange: h.priceRange || 'inquiry',
        has3dModel: h.has3dModel ?? false,
        viewCount: h.viewCount ?? 0,
        isFeatured: h.isFeatured ?? false,
        manufacturer: {
          id: h.manufacturerId || 0,
          name: h.manufacturerName || '',
          nameEn: h.manufacturerNameEn || '',
          country: '',
        },
        category: {
          id: h.categoryId || 0,
          name: h.categoryName || '',
          nameEn: h.categoryNameEn || '',
          slug: '',
        }
      }))
      total.value = esData.total || 0
      totalPages.value = esData.totalPages || 1
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
    currentPage.value = 0
    loadRobots()
  }
})

watch(() => route.query.category, (cat) => {
  if (cat) {
    pendingCategorySlug.value = cat as string
    filters.categoryId = undefined
    currentPage.value = 0
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
  background-color: rgba(0, 255, 209, 0.15);
  color: #77F8D3;
  border-radius: 2px;
  padding: 0 2px;
  font-style: normal;
}
</style>
