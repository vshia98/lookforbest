<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <LoadingSpinner v-if="loading" text="加载机器人信息..." />

    <div v-else-if="!robot" class="text-center py-20">
      <div class="text-5xl mb-4">😕</div>
      <p class="text-gray-500">未找到该机器人</p>
      <router-link to="/robots" class="text-primary-500 hover:underline mt-4 block">返回机器人库</router-link>
    </div>

    <template v-else>
      <!-- 面包屑 -->
      <nav class="text-sm text-gray-400 mb-6 flex items-center gap-2">
        <router-link to="/" class="hover:text-gray-600">首页</router-link>
        <span>/</span>
        <router-link to="/robots" class="hover:text-gray-600">机器人库</router-link>
        <span>/</span>
        <span class="text-gray-700">{{ robot.name }}</span>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-10">
        <!-- 左：图片 -->
        <div>
          <div class="bg-gray-100 rounded-2xl overflow-hidden aspect-video flex items-center justify-center">
            <img
              v-if="currentImage"
              :src="currentImage"
              :alt="robot.name"
              class="w-full h-full object-contain"
            />
            <div v-else class="text-7xl">🤖</div>
          </div>
          <!-- 缩略图 -->
          <div v-if="robot.images.length > 1" class="flex gap-2 mt-3 overflow-x-auto pb-1">
            <button
              v-for="img in robot.images"
              :key="img.id"
              @click="currentImage = img.url"
              class="flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors"
              :class="currentImage === img.url ? 'border-primary-500' : 'border-gray-200'"
            >
              <img :src="img.thumbnailUrl || img.url" :alt="img.altText || ''" class="w-full h-full object-cover" />
            </button>
          </div>
        </div>

        <!-- 右：基本信息 -->
        <div>
          <!-- 厂商 -->
          <router-link :to="{ name: 'manufacturer', params: { id: robot.manufacturer.id } }" class="flex items-center gap-2 mb-3">
            <img v-if="robot.manufacturer.logoUrl" :src="robot.manufacturer.logoUrl" class="h-6 w-auto" :alt="robot.manufacturer.name" />
            <span class="text-sm text-gray-500 hover:text-primary-600">{{ robot.manufacturer.name }}</span>
          </router-link>

          <h1 class="text-3xl font-bold text-gray-900 mb-1">{{ robot.name }}</h1>
          <p v-if="robot.nameEn && robot.nameEn !== robot.name" class="text-gray-400 mb-3">{{ robot.nameEn }}</p>

          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mb-5">
            <span class="bg-primary-50 text-primary-600 text-xs px-3 py-1 rounded-full">{{ robot.category.name }}</span>
            <span v-for="domain in robot.applicationDomains" :key="domain.id" class="bg-gray-100 text-gray-600 text-xs px-3 py-1 rounded-full">
              {{ domain.name }}
            </span>
            <span v-if="robot.releaseYear" class="bg-gray-100 text-gray-600 text-xs px-3 py-1 rounded-full">{{ robot.releaseYear }}年发布</span>
          </div>

          <!-- 核心参数 -->
          <div class="grid grid-cols-2 gap-3 mb-6">
            <div v-if="robot.specs.payloadKg" class="bg-gray-50 rounded-xl p-3">
              <div class="text-lg font-bold text-gray-800">{{ robot.specs.payloadKg }} kg</div>
              <div class="text-xs text-gray-500 mt-0.5">最大载荷</div>
            </div>
            <div v-if="robot.specs.reachMm" class="bg-gray-50 rounded-xl p-3">
              <div class="text-lg font-bold text-gray-800">{{ robot.specs.reachMm }} mm</div>
              <div class="text-xs text-gray-500 mt-0.5">工作半径</div>
            </div>
            <div v-if="robot.specs.dof" class="bg-gray-50 rounded-xl p-3">
              <div class="text-lg font-bold text-gray-800">{{ robot.specs.dof }} DOF</div>
              <div class="text-xs text-gray-500 mt-0.5">自由度</div>
            </div>
            <div v-if="robot.specs.repeatabilityMm" class="bg-gray-50 rounded-xl p-3">
              <div class="text-lg font-bold text-gray-800">±{{ robot.specs.repeatabilityMm }} mm</div>
              <div class="text-xs text-gray-500 mt-0.5">重复定位精度</div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex flex-wrap gap-3">
            <button
              @click="toggleFavorite"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl border text-sm font-medium transition-colors"
              :class="isFavorited ? 'border-red-300 text-red-500 bg-red-50' : 'border-gray-200 text-gray-600 hover:border-red-300'"
            >
              {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>
            <button
              @click="addToCompare"
              :disabled="isInCompare"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl border text-sm font-medium transition-colors"
              :class="isInCompare ? 'border-primary-300 text-primary-500 bg-primary-50 cursor-default' : 'border-gray-200 text-gray-600 hover:border-primary-300'"
            >
              {{ isInCompare ? '已加入对比' : '+ 加入对比' }}
            </button>
            <button
              @click="showInquiryModal = true"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-orange-500 text-white text-sm font-medium hover:bg-orange-600 transition-colors"
            >
              📧 询价/联系厂商
            </button>
            <a v-if="robot.manufacturer.websiteUrl" :href="robot.manufacturer.websiteUrl" target="_blank" rel="noopener"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary-500 text-white text-sm font-medium hover:bg-primary-600 transition-colors">
              官网 →
            </a>
          </div>
        </div>
      </div>

      <!-- 3D 模型查看 -->
      <div class="mb-8">
        <div class="flex items-center gap-2 mb-3">
          <h2 class="font-semibold text-gray-800">3D 模型查看</h2>
          <span class="text-xs text-gray-400 bg-gray-100 rounded-full px-2 py-0.5">可旋转 · 可缩放</span>
        </div>
        <ModelViewer :robotId="robot.id" :robotName="robot.name" />
      </div>

      <!-- 详细参数 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 参数表格 -->
        <div class="lg:col-span-2">
          <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mb-6">
            <div class="px-6 py-4 border-b border-gray-100">
              <h2 class="font-semibold text-gray-800">技术参数</h2>
            </div>
            <table class="w-full text-sm">
              <tbody>
                <tr v-for="(spec, key) in visibleSpecs" :key="key" class="border-b border-gray-50 last:border-0">
                  <td class="px-6 py-3 text-gray-500 w-1/3">{{ specLabels[key] || key }}</td>
                  <td class="px-6 py-3 font-medium text-gray-800">{{ spec }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 产品描述 -->
          <div v-if="robot.description" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 mb-6">
            <h2 class="font-semibold text-gray-800 mb-3">产品描述</h2>
            <p class="text-sm text-gray-600 leading-relaxed">{{ robot.description }}</p>
          </div>
        </div>

        <!-- 厂商信息 -->
        <div>
          <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-4">
            <h3 class="font-semibold text-gray-800 mb-3">厂商信息</h3>
            <div class="flex items-center gap-3 mb-3">
              <img v-if="robot.manufacturer.logoUrl" :src="robot.manufacturer.logoUrl" class="h-8 w-auto" :alt="robot.manufacturer.name" />
              <div>
                <p class="font-medium text-gray-800 text-sm">{{ robot.manufacturer.name }}</p>
                <p class="text-xs text-gray-400">{{ robot.manufacturer.country }}</p>
              </div>
            </div>
            <router-link
              :to="{ name: 'manufacturer', params: { id: robot.manufacturer.id } }"
              class="w-full text-center block py-2 border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors mb-2"
            >
              查看厂商全部产品
            </router-link>
            <button
              @click="showInquiryModal = true"
              class="w-full py-2 bg-orange-500 text-white rounded-lg text-sm font-medium hover:bg-orange-600 transition-colors"
            >
              📧 发起询价
            </button>
          </div>

          <!-- 价格趋势 -->
          <PriceChart :robot-id="robot.id" class="mb-4" />

          <!-- 资料文档 -->
          <div v-if="robot.documents.length > 0" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
            <h3 class="font-semibold text-gray-800 mb-3">技术资料</h3>
            <ul class="space-y-2">
              <li v-for="doc in robot.documents" :key="doc.id">
                <a :href="doc.url" target="_blank" rel="noopener" class="flex items-center gap-2 text-sm text-primary-500 hover:underline">
                  📄 {{ doc.title }}
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- AI 智能推荐 -->
      <div class="mt-10">
        <div class="flex items-center gap-2 mb-5">
          <h2 class="text-xl font-bold text-gray-800">为你推荐</h2>
          <span v-if="recommendStrategy" class="text-xs text-gray-400 bg-gray-100 rounded-full px-2 py-0.5">
            {{ strategyLabel(recommendStrategy) }}
          </span>
        </div>
        <div v-if="loadingRecommend" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <div v-for="i in 4" :key="i" class="h-48 bg-gray-100 rounded-xl animate-pulse"></div>
        </div>
        <div v-else-if="recommendations.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <RobotCard v-for="r in recommendations" :key="r.id" :robot="r" />
        </div>
        <div v-else-if="robot.similarRobots && robot.similarRobots.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <RobotCard v-for="r in robot.similarRobots.slice(0, 4)" :key="r.id" :robot="r" />
        </div>
        <p v-else class="text-sm text-gray-400">暂无推荐</p>
      </div>
    </template>
  </div>

  <!-- 询价弹窗 -->
  <Teleport to="body">
    <div v-if="showInquiryModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" @click.self="showInquiryModal = false">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6 relative">
        <button @click="showInquiryModal = false" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        <h2 class="text-lg font-bold text-gray-800 mb-1">发起询价</h2>
        <p class="text-sm text-gray-400 mb-5">向 <strong>{{ robot?.manufacturer.name }}</strong> 咨询 {{ robot?.name }} 相关问题</p>

        <!-- 成功提示 -->
        <div v-if="inquirySuccess" class="text-center py-8">
          <div class="text-5xl mb-3">✅</div>
          <p class="font-semibold text-gray-800 mb-1">询价已提交！</p>
          <p class="text-sm text-gray-400">厂商将尽快通过邮件与您联系。</p>
          <button @click="showInquiryModal = false; inquirySuccess = false" class="mt-4 px-6 py-2 bg-primary-500 text-white rounded-xl text-sm">关闭</button>
        </div>

        <form v-else @submit.prevent="submitInquiry" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">询价内容 <span class="text-red-500">*</span></label>
            <textarea
              v-model="inquiryForm.message"
              rows="4"
              placeholder="请描述您的需求，例如：采购数量、应用场景、期望价格区间等..."
              class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400 resize-none"
              required
              minlength="10"
              maxlength="2000"
            ></textarea>
            <p class="text-xs text-gray-400 mt-1 text-right">{{ inquiryForm.message.length }}/2000</p>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">联系人姓名</label>
              <input v-model="inquiryForm.contactName" type="text" placeholder="您的姓名" class="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">联系邮箱 <span class="text-red-500">*</span></label>
              <input v-model="inquiryForm.contactEmail" type="email" placeholder="your@email.com" class="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" required />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
              <input v-model="inquiryForm.contactPhone" type="tel" placeholder="手机或固话" class="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">所在公司</label>
              <input v-model="inquiryForm.contactCompany" type="text" placeholder="公司名称" class="w-full border border-gray-200 rounded-xl px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-400" />
            </div>
          </div>
          <p v-if="inquiryError" class="text-sm text-red-500">{{ inquiryError }}</p>
          <button
            type="submit"
            :disabled="inquirySubmitting"
            class="w-full py-3 bg-orange-500 text-white rounded-xl font-medium text-sm hover:bg-orange-600 transition-colors disabled:opacity-50"
          >
            {{ inquirySubmitting ? '提交中...' : '提交询价' }}
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import RobotCard from '@/components/robot/RobotCard.vue'
import ModelViewer from '@/components/robot/ModelViewer.vue'
import PriceChart from '@/components/robot/PriceChart.vue'
import { robotService } from '@/services/robots'
import { inquiryService } from '@/services/inquiries'
import { useRobotStore } from '@/stores/robot'
import { useFavoritesStore } from '@/stores/favorites'
import { analytics } from '@/utils/analytics'
import { useSeo, buildRobotJsonLd } from '@/composables/useSeo'
import type { RobotDetail } from '@/types/robot'

const route = useRoute()
const robotStore = useRobotStore()
const favoritesStore = useFavoritesStore()

const robot = ref<RobotDetail | null>(null)
const loading = ref(true)
const currentImage = ref('')
const recommendations = ref<RobotDetail[]>([])
const recommendStrategy = ref('')
const loadingRecommend = ref(false)

// 询价弹窗状态
const showInquiryModal = ref(false)
const inquirySuccess = ref(false)
const inquirySubmitting = ref(false)
const inquiryError = ref('')
const inquiryForm = ref({
  message: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  contactCompany: ''
})

// SEO meta
const seoMeta = computed(() => {
  const r = robot.value
  if (!r) return null
  const origin = window.location.origin
  return {
    title: `${r.name}${r.nameEn ? ' - ' + r.nameEn : ''}`,
    description: r.description?.slice(0, 155) || `查看 ${r.name} 机器人详情、参数规格及价格信息`,
    keywords: `${r.name},${r.nameEn || ''},机器人,${r.manufacturer?.name || ''},${r.category?.name || ''}`,
    ogTitle: r.nameEn || r.name,
    ogDescription: r.description?.slice(0, 155),
    ogImage: r.coverImageUrl || undefined,
    ogUrl: `${origin}/robots/${r.slug}`,
    canonicalUrl: `${origin}/robots/${r.slug}`,
    jsonLd: buildRobotJsonLd({
      name: r.name,
      nameEn: r.nameEn,
      description: r.description,
      coverImageUrl: r.coverImageUrl,
      manufacturer: r.manufacturer,
      releaseYear: r.releaseYear,
      priceUsdFrom: r.priceUsdFrom,
      slug: r.slug
    })
  }
})

useSeo(seoMeta)

const specLabels: Record<string, string> = {
  payloadKg: '最大载荷 (kg)', reachMm: '工作半径 (mm)', dof: '自由度',
  repeatabilityMm: '重复定位精度 (mm)', maxSpeedDegS: '最大速度 (°/s)',
  weightKg: '机器人自重 (kg)', ipRating: '防护等级', mounting: '安装方式',
  maxSpeedMs: '最大速度 (m/s)', maxLoadKg: '最大载重 (kg)',
  batteryLifeH: '续航时间 (h)', navigationType: '导航方式',
  heightMm: '高度 (mm)', walkingSpeedMs: '行走速度 (m/s)'
}

const visibleSpecs = computed(() => {
  if (!robot.value) return {}
  return Object.fromEntries(
    Object.entries(robot.value.specs).filter(([, v]) => v !== undefined && v !== null)
  )
})

const isInCompare = computed(() => robot.value ? robotStore.compareList.some(r => r.id === robot.value!.id) : false)
const isFavorited = computed(() => robot.value ? favoritesStore.isFavorited(robot.value.id) : false)

function addToCompare() {
  if (robot.value) robotStore.addToCompare(robot.value as any)
}

function toggleFavorite() {
  if (robot.value) favoritesStore.toggle(robot.value.id)
}

function strategyLabel(s: string) {
  const map: Record<string, string> = { hybrid: 'AI混合推荐', text: '文本相似', collab: '协同过滤', popular: '热门推荐' }
  return map[s] ?? s
}

async function submitInquiry() {
  if (!robot.value) return
  inquiryError.value = ''
  inquirySubmitting.value = true
  try {
    await inquiryService.create({
      robotId: robot.value.id,
      message: inquiryForm.value.message,
      contactName: inquiryForm.value.contactName || undefined,
      contactEmail: inquiryForm.value.contactEmail,
      contactPhone: inquiryForm.value.contactPhone || undefined,
      contactCompany: inquiryForm.value.contactCompany || undefined
    })
    inquirySuccess.value = true
    inquiryForm.value = { message: '', contactName: '', contactEmail: '', contactPhone: '', contactCompany: '' }
  } catch (e: any) {
    inquiryError.value = e?.response?.data?.message || '提交失败，请稍后重试'
  } finally {
    inquirySubmitting.value = false
  }
}

async function loadRecommendations(id: number, slug: string) {
  loadingRecommend.value = true
  try {
    const res: any = await robotService.getRecommend(id, 4)
    const data = res.data || res
    recommendations.value = Array.isArray(data) ? data : (data.items || [])
    recommendStrategy.value = data.strategy || 'popular'
    analytics.viewRobot(id, slug)
  } catch {
    recommendations.value = []
  } finally {
    loadingRecommend.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const slug = route.params.slug as string
    const res = await robotService.getBySlug(slug)
    robot.value = res.data
    if (robot.value.images.length > 0) {
      currentImage.value = robot.value.images[0].url
    } else if (robot.value.coverImageUrl) {
      currentImage.value = robot.value.coverImageUrl
    }
    await loadRecommendations(robot.value.id, slug)
  } finally {
    loading.value = false
  }
})
</script>
