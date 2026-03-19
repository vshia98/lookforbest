<template>
  <div class="max-w-3xl">
    <div class="flex items-center gap-3 mb-6">
      <router-link to="/admin/robots" class="text-gray-400 hover:text-gray-400">← 返回列表</router-link>
      <h2 class="text-xl font-bold text-white">{{ isEdit ? '编辑机器人' : '添加机器人' }}</h2>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-16">
      <div class="animate-spin w-6 h-6 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <form v-else @submit.prevent="submit" class="space-y-5">
      <!-- 基本信息 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">基本信息</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">中文名称 <span class="text-red-500">*</span></label>
            <input v-model="form.name" required class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">英文名称</label>
            <input v-model="form.nameEn" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">型号</label>
            <input v-model="form.modelNumber" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">Slug（URL标识）</label>
            <input v-model="form.slug" class="input-dark" placeholder="自动生成" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">一句话简介（中文）</label>
            <input v-model="form.subtitle" class="input-dark" placeholder="列表卡片上的简短一句话" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">一句话简介（英文）</label>
            <input v-model="form.subtitleEn" class="input-dark" placeholder="Short English tagline for the card" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">厂商 <span class="text-red-500">*</span></label>
            <select v-model="form.manufacturerId" required class="input-dark">
              <option value="">请选择厂商</option>
              <option v-for="m in manufacturers" :key="m.id" :value="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">分类 <span class="text-red-500">*</span></label>
            <select v-model="form.categoryId" required class="input-dark">
              <option value="">请选择分类</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">发布年份</label>
            <input v-model.number="form.releaseYear" type="number" min="2000" :max="new Date().getFullYear()" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">状态</label>
            <select v-model="form.status" class="input-dark">
              <option value="active">上线</option>
              <option value="discontinued">已停产</option>
              <option value="upcoming">即将上市</option>
            </select>
          </div>
        </div>

        <!-- 封面图上传 -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">封面图</label>
          <ImageUpload v-model="form.coverImageUrl" />
        </div>

        <!-- 中文描述（富文本） -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">中文描述</label>
          <RichTextEditor v-model="form.description" />
        </div>

        <!-- 英文描述（富文本） -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">英文描述</label>
          <RichTextEditor v-model="form.descriptionEn" />
        </div>

        <!-- 产品介绍 -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">产品介绍（中文）</label>
          <RichTextEditor v-model="form.introduction" />
        </div>
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">产品介绍（英文）</label>
          <RichTextEditor v-model="form.introductionEn" />
        </div>

        <!-- 应用场景 -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">应用场景（中文）</label>
          <RichTextEditor v-model="form.applicationScenarios" />
        </div>
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">应用场景（英文）</label>
          <RichTextEditor v-model="form.applicationScenariosEn" />
        </div>

        <!-- 产品优势 -->
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">产品优势（中文）</label>
          <RichTextEditor v-model="form.advantages" />
        </div>
        <div class="mt-4">
          <label class="block text-sm text-gray-400 mb-1">产品优势（英文）</label>
          <RichTextEditor v-model="form.advantagesEn" />
        </div>
      </div>

      <!-- 技术参数 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6">
        <h3 class="font-semibold text-white mb-4">技术参数</h3>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">负载（kg）</label>
            <input v-model.number="form.specs.payloadKg" type="number" step="0.1" min="0" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">工作半径（mm）</label>
            <input v-model.number="form.specs.reachMm" type="number" min="0" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">自由度（DOF）</label>
            <input v-model.number="form.specs.dof" type="number" min="0" max="20" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">重复定位精度（mm）</label>
            <input v-model.number="form.specs.repeatabilityMm" type="number" step="0.01" min="0" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">最大速度（deg/s）</label>
            <input v-model.number="form.specs.maxSpeedDegS" type="number" min="0" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">自重（kg）</label>
            <input v-model.number="form.specs.weightKg" type="number" step="0.1" min="0" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">IP防护等级</label>
            <input v-model="form.specs.ipRating" class="input-dark" placeholder="如 IP67" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">安装方式</label>
            <input v-model="form.specs.mounting" class="input-dark" placeholder="如 floor/ceiling" />
          </div>
        </div>
      </div>

      <!-- 提交 -->
      <div class="flex items-center gap-3 justify-end">
        <router-link to="/admin/robots" class="px-5 py-2 text-sm border border-white/[0.06] rounded-lg hover:bg-dark-100 transition-colors">
          取消
        </router-link>
        <button
          type="submit"
          :disabled="submitting"
          class="px-5 py-2 text-sm bg-primary text-[#1a1a1a] rounded-lg hover:bg-primary transition-colors disabled:opacity-50"
        >
          {{ submitting ? '保存中...' : (isEdit ? '保存更改' : '创建机器人') }}
        </button>
      </div>

      <p v-if="error" class="text-red-500 text-sm text-center">{{ error }}</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/services/api'
import { manufacturerService } from '@/services/manufacturers'
import ImageUpload from '@/components/admin/ImageUpload.vue'
import RichTextEditor from '@/components/admin/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const manufacturers = ref<any[]>([])
const categories = ref<any[]>([])

const form = reactive({
  name: '',
  nameEn: '',
  subtitle: '',
  subtitleEn: '',
  modelNumber: '',
  slug: '',
  manufacturerId: '' as any,
  categoryId: '' as any,
  releaseYear: null as number | null,
  status: 'active',
  coverImageUrl: '',
  description: '',
  descriptionEn: '',
   introduction: '',
   introductionEn: '',
   applicationScenarios: '',
   applicationScenariosEn: '',
   advantages: '',
   advantagesEn: '',
  specs: {
    payloadKg: null as number | null,
    reachMm: null as number | null,
    dof: null as number | null,
    repeatabilityMm: null as number | null,
    maxSpeedDegS: null as number | null,
    weightKg: null as number | null,
    ipRating: '',
    mounting: ''
  }
})

async function loadInitialData() {
  loading.value = true
  try {
    const [mRes, cRes] = await Promise.all([
      manufacturerService.getList({ size: 200 }),
      http.get<any, any>('/categories?size=100')
    ])
    manufacturers.value = mRes.data || mRes.items || []
    categories.value = cRes.data || cRes.items || []

    if (isEdit.value) {
      const res = await http.get<any, any>(`/robots/${route.params.id}`)
      const robot = res.data || res
      form.name = robot.name
      form.nameEn = robot.nameEn || ''
      form.subtitle = robot.subtitle || ''
      form.subtitleEn = robot.subtitleEn || ''
      form.modelNumber = robot.modelNumber || ''
      form.slug = robot.slug || ''
      form.manufacturerId = robot.manufacturer?.id || ''
      form.categoryId = robot.category?.id || ''
      form.releaseYear = robot.releaseYear || null
      form.status = robot.status || 'active'
      form.coverImageUrl = robot.coverImageUrl || ''
      form.description = robot.description || ''
      form.descriptionEn = robot.descriptionEn || ''
      form.introduction = robot.introduction || ''
      form.introductionEn = robot.introductionEn || ''
      form.applicationScenarios = robot.applicationScenarios || ''
      form.applicationScenariosEn = robot.applicationScenariosEn || ''
      form.advantages = robot.advantages || ''
      form.advantagesEn = robot.advantagesEn || ''
      if (robot.specs) Object.assign(form.specs, robot.specs)
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function submit() {
  submitting.value = true
  error.value = ''
  try {
    const payload = {
      ...form,
      manufacturerId: Number(form.manufacturerId),
      categoryId: Number(form.categoryId),
      specs: { ...form.specs }
    }
    if (isEdit.value) {
      await http.put(`/robots/${route.params.id}`, payload)
    } else {
      await http.post('/robots', payload)
    }
    router.push('/admin/robots')
  } catch (e: any) {
    error.value = e?.response?.data?.message || '保存失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(loadInitialData)
</script>
