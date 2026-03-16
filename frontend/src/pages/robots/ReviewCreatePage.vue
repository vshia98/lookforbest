<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div class="mb-6">
      <router-link to="/reviews" class="inline-flex items-center gap-1 text-sm text-gray-500 hover:text-gray-300">
        ← 返回评测列表
      </router-link>
      <h1 class="text-2xl font-bold text-white mt-2">写评测</h1>
    </div>

    <div class="bg-dark-50 rounded-xl border border-white/[0.04]  p-6 space-y-5">
      <!-- 选择机器人 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">选择机器人 <span class="text-red-500">*</span></label>
        <input
          v-model="robotSearch"
          @input="searchRobots"
          placeholder="搜索机器人名称..."
          class="input-dark"
        />
        <div v-if="robotResults.length > 0 && !form.robotId" class="mt-1 border border-white/[0.06] rounded-lg  bg-dark-50 max-h-48 overflow-y-auto z-10">
          <button
            v-for="robot in robotResults"
            :key="robot.id"
            @click="selectRobot(robot)"
            class="w-full text-left px-3 py-2 text-sm hover:bg-dark-100 border-b border-white/[0.04] last:border-0"
          >
            <span class="font-medium">{{ robot.name }}</span>
            <span class="text-gray-400 ml-2 text-xs">{{ robot.manufacturer?.name }}</span>
          </button>
        </div>
        <div v-if="selectedRobot" class="mt-2 flex items-center gap-2 text-sm">
          <span class="bg-blue-900/30 text-blue-400 px-2 py-0.5 rounded">{{ selectedRobot.name }}</span>
          <button @click="clearRobot" class="text-gray-400 hover:text-gray-600 text-xs">✕ 清除</button>
        </div>
      </div>

      <!-- 标题 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">标题 <span class="text-red-500">*</span></label>
        <input
          v-model="form.title"
          maxlength="300"
          placeholder="给这篇评测起个标题..."
          class="input-dark"
        />
      </div>

      <!-- 评分 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">综合评分 <span class="text-red-500">*</span></label>
        <div class="flex gap-2">
          <button
            v-for="star in [1, 2, 3, 4, 5]"
            :key="star"
            @click="form.rating = star"
            class="text-2xl transition-transform hover:scale-110"
          >
            {{ star <= form.rating ? '★' : '☆' }}
          </button>
          <span class="text-sm text-gray-500 self-center ml-1">{{ ratingLabels[form.rating - 1] }}</span>
        </div>
      </div>

      <!-- 优点 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">优点</label>
        <textarea
          v-model="form.pros"
          rows="3"
          placeholder="这款机器人有哪些值得称赞的地方？"
          class="input-dark"
        ></textarea>
      </div>

      <!-- 缺点 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">缺点</label>
        <textarea
          v-model="form.cons"
          rows="3"
          placeholder="有哪些需要改进的地方？"
          class="input-dark"
        ></textarea>
      </div>

      <!-- 正文 -->
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-1.5">详细评测内容 <span class="text-red-500">*</span> <span class="text-xs text-gray-400 font-normal">(支持 Markdown)</span></label>
        <textarea
          v-model="form.content"
          rows="10"
          placeholder="分享你对这款机器人的详细使用体验、测试结论、实际应用场景等..."
          class="input-dark"
        ></textarea>
      </div>

      <!-- 操作按钮 -->
      <div class="flex items-center gap-3 pt-2">
        <button
          @click="save('draft')"
          :disabled="submitting"
          class="px-4 py-2 border border-white/[0.06] rounded-lg text-sm text-gray-600 hover:bg-dark-100 disabled:opacity-50 transition-colors"
        >
          保存草稿
        </button>
        <button
          @click="save('submit')"
          :disabled="submitting"
          class="px-4 py-2 bg-primary hover:bg-primary-400 text-[#1a1a1a] text-sm rounded-lg disabled:opacity-50 transition-colors"
        >
          {{ submitting ? '提交中...' : '提交审核' }}
        </button>
      </div>
      <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { reviewService, robotService } from '@/services/robots'

const router = useRouter()
const submitting = ref(false)
const error = ref('')
const robotSearch = ref('')
const robotResults = ref<any[]>([])
const selectedRobot = ref<any>(null)

const ratingLabels = ['很差', '较差', '一般', '较好', '非常好']

const form = reactive({
  robotId: null as number | null,
  title: '',
  rating: 5,
  pros: '',
  cons: '',
  content: '',
  images: [] as string[]
})

let searchTimer: ReturnType<typeof setTimeout> | null = null

function searchRobots() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!robotSearch.value.trim()) {
    robotResults.value = []
    return
  }
  searchTimer = setTimeout(async () => {
    const res = await robotService.getList({ q: robotSearch.value, size: 10 })
    robotResults.value = res.data.content || []
  }, 300)
}

function selectRobot(robot: any) {
  selectedRobot.value = robot
  form.robotId = robot.id
  robotSearch.value = robot.name
  robotResults.value = []
}

function clearRobot() {
  selectedRobot.value = null
  form.robotId = null
  robotSearch.value = ''
}

async function save(action: 'draft' | 'submit') {
  error.value = ''
  if (!form.robotId) { error.value = '请选择机器人'; return }
  if (!form.title.trim()) { error.value = '请填写标题'; return }
  if (!form.content.trim()) { error.value = '请填写评测内容'; return }

  submitting.value = true
  try {
    const res = await reviewService.create({ ...form })
    const reviewId = res.data.id
    if (action === 'submit') {
      await reviewService.submit(reviewId)
    }
    router.push(`/reviews/${reviewId}`)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>
