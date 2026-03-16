<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-white">应用案例</h1>
      <p class="text-gray-500 mt-1">共 {{ total }} 个案例</p>
    </div>

    <!-- 行业筛选 -->
    <div class="flex gap-2 flex-wrap mb-6">
      <button
        v-for="ind in industries"
        :key="ind.value"
        @click="selectIndustry(ind.value)"
        class="px-4 py-1.5 rounded-full text-sm border transition-colors"
        :class="selectedIndustry === ind.value ? 'bg-primary text-[#1a1a1a] border-primary' : 'border-white/[0.06] text-gray-400 hover:border-primary/40'"
      >
        {{ ind.label }}
      </button>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="cases.length === 0" class="text-center py-20">
      <div class="text-5xl mb-4">📋</div>
      <p class="text-gray-500">暂无案例</p>
    </div>

    <!-- 案例列表 -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">
      <router-link
        v-for="cs in cases"
        :key="cs.id"
        :to="`/case-studies/${cs.id}`"
        class="bg-dark-50 rounded-xl border border-white/[0.04]  p-5 hover:shadow-md transition-shadow block"
      >
        <div class="flex items-start justify-between mb-3">
          <span
            v-if="cs.industry"
            class="text-xs bg-green-900/30 text-green-400 px-2 py-0.5 rounded-full"
          >
            {{ cs.industry }}
          </span>
          <span class="text-xs text-gray-400">{{ formatDate(cs.createdAt) }}</span>
        </div>

        <h3 class="font-semibold text-white mb-2 line-clamp-2">{{ cs.title }}</h3>
        <p class="text-sm text-gray-500 line-clamp-3 mb-3">{{ excerpt(cs.content) }}</p>

        <div class="flex items-center gap-2 flex-wrap mb-3">
          <span
            v-for="robotId in (cs.robotIds || []).slice(0, 3)"
            :key="robotId"
            class="text-xs bg-blue-50 text-blue-400 px-2 py-0.5 rounded"
          >
            机器人 #{{ robotId }}
          </span>
          <span v-if="(cs.robotIds || []).length > 3" class="text-xs text-gray-400">+{{ cs.robotIds.length - 3 }}</span>
        </div>

        <div class="flex items-center gap-2 border-t border-white/[0.04] pt-3">
          <img
            :src="cs.authorAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(cs.authorName)}&background=random`"
            class="w-6 h-6 rounded-full object-cover"
            :alt="cs.authorName"
          />
          <span class="text-xs text-gray-500">{{ cs.authorName }}</span>
          <span class="ml-auto text-xs text-gray-400">👍 {{ cs.likeCount }}</span>
        </div>
      </router-link>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-8">
      <button
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
        class="px-4 py-2 rounded-lg border border-white/[0.06] text-sm disabled:opacity-40 hover:bg-dark-100"
      >
        上一页
      </button>
      <span class="text-sm text-gray-500">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage >= totalPages - 1"
        @click="changePage(currentPage + 1)"
        class="px-4 py-2 rounded-lg border border-white/[0.06] text-sm disabled:opacity-40 hover:bg-dark-100"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { caseStudyService } from '@/services/robots'

const cases = ref<any[]>([])
const total = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const loading = ref(false)
const selectedIndustry = ref<string>('')

const industries = [
  { label: '全部', value: '' },
  { label: '制造业', value: '制造业' },
  { label: '物流仓储', value: '物流仓储' },
  { label: '医疗健康', value: '医疗健康' },
  { label: '零售服务', value: '零售服务' },
  { label: '农业', value: '农业' },
  { label: '教育科研', value: '教育科研' },
  { label: '其他', value: '其他' }
]

function excerpt(content: string) {
  const stripped = content.replace(/[#*`_>[\]]/g, '').trim()
  return stripped.length > 120 ? stripped.slice(0, 120) + '...' : stripped
}

function formatDate(dt: string) {
  return new Date(dt).toLocaleDateString('zh-CN')
}

function selectIndustry(v: string) {
  selectedIndustry.value = v
  currentPage.value = 0
  loadCases()
}

async function loadCases() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: 18 }
    if (selectedIndustry.value) params.industry = selectedIndustry.value
    const res = await caseStudyService.list(params)
    cases.value = res.data.content
    total.value = res.data.total
    totalPages.value = res.data.totalPages
  } finally {
    loading.value = false
  }
}

function changePage(page: number) {
  currentPage.value = page
  loadCases()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(loadCases)
</script>
