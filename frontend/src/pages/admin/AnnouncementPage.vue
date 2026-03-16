<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-white">系统公告</h2>
        <p class="text-gray-500 text-sm mt-1">向全站用户发送通知公告</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：发布表单 -->
      <div class="lg:col-span-2">
        <div class="card rounded-2xl p-6 relative overflow-hidden">
          <!-- 装饰光效 -->
          <div class="absolute -top-12 -right-12 w-32 h-32 bg-primary/8 rounded-full blur-[60px] pointer-events-none"></div>

          <div class="relative space-y-5">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-lg">📢</span>
              <h3 class="font-semibold text-white">发布新公告</h3>
            </div>

            <!-- 公告类型 -->
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-2">公告类型</label>
              <div class="flex gap-2">
                <button
                  v-for="t in announcementTypes" :key="t.value"
                  type="button"
                  @click="form.type = t.value"
                  class="px-4 py-2 rounded-lg text-sm border transition-all"
                  :class="form.type === t.value
                    ? 'bg-primary/10 text-primary border-primary/30'
                    : 'border-white/[0.06] text-gray-400 hover:border-white/10 hover:text-gray-300'"
                >
                  {{ t.icon }} {{ t.label }}
                </button>
              </div>
            </div>

            <!-- 标题 -->
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">公告标题 <span class="text-accent-red">*</span></label>
              <input
                v-model="form.title"
                type="text"
                maxlength="200"
                placeholder="请输入公告标题"
                class="input-dark"
              />
              <div class="flex justify-between mt-1">
                <span class="text-xs text-gray-600">简明扼要，突出重点</span>
                <span class="text-xs" :class="form.title.length > 150 ? 'text-accent-red' : 'text-gray-600'">{{ form.title.length }}/200</span>
              </div>
            </div>

            <!-- 内容 -->
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">公告内容</label>
              <textarea
                v-model="form.content"
                rows="6"
                maxlength="2000"
                placeholder="请输入公告详细内容（可选）"
                class="input-dark"
              ></textarea>
              <div class="flex justify-between mt-1">
                <span class="text-xs text-gray-600">支持详细描述，用户将在通知中心查看</span>
                <span class="text-xs" :class="form.content.length > 1800 ? 'text-accent-red' : 'text-gray-600'">{{ form.content.length }}/2000</span>
              </div>
            </div>

            <!-- 预览 -->
            <div v-if="form.title.trim()" class="bg-dark-100 rounded-xl p-4 border border-white/[0.04]">
              <div class="text-xs text-gray-500 mb-2">预览效果</div>
              <div class="flex items-start gap-2">
                <span class="text-base mt-0.5">{{ currentTypeIcon }}</span>
                <div>
                  <p class="text-sm font-medium text-white">{{ form.title }}</p>
                  <p v-if="form.content" class="text-xs text-gray-400 mt-1 line-clamp-2">{{ form.content }}</p>
                  <p class="text-xs text-gray-600 mt-1">刚刚</p>
                </div>
              </div>
            </div>

            <!-- 提交 -->
            <div class="flex items-center gap-3">
              <button
                @click="submit"
                :disabled="submitting || !form.title.trim()"
                class="btn-primary px-8 py-2.5 text-sm disabled:opacity-50"
              >
                {{ submitting ? '发送中...' : '发布公告' }}
              </button>
              <button
                v-if="form.title || form.content"
                @click="form = { title: '', content: '', type: 'info' }"
                class="px-4 py-2.5 text-sm text-gray-500 hover:text-gray-300 transition-colors"
              >
                清空
              </button>
            </div>

            <!-- 状态消息 -->
            <Transition name="fade">
              <div v-if="successMsg" class="flex items-center gap-2 text-sm text-primary bg-primary/10 border border-primary/20 rounded-xl p-3">
                <span>✅</span> {{ successMsg }}
              </div>
            </Transition>
            <Transition name="fade">
              <div v-if="errorMsg" class="flex items-center gap-2 text-sm text-accent-red bg-accent-red/10 border border-accent-red/20 rounded-xl p-3">
                <span>❌</span> {{ errorMsg }}
              </div>
            </Transition>
          </div>
        </div>
      </div>

      <!-- 右侧：历史公告 & 统计 -->
      <div class="space-y-4">
        <!-- 发送统计 -->
        <div class="card rounded-2xl p-5">
          <h3 class="text-sm font-semibold text-white mb-3">发送统计</h3>
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-dark-100 rounded-lg p-3 text-center border border-white/[0.04]">
              <div class="text-xl font-bold text-primary">{{ stats.totalSent }}</div>
              <div class="text-xs text-gray-500 mt-0.5">累计发送</div>
            </div>
            <div class="bg-dark-100 rounded-lg p-3 text-center border border-white/[0.04]">
              <div class="text-xl font-bold text-accent-gold">{{ stats.thisMonth }}</div>
              <div class="text-xs text-gray-500 mt-0.5">本月发送</div>
            </div>
          </div>
        </div>

        <!-- 最近公告 -->
        <div class="card rounded-2xl p-5">
          <h3 class="text-sm font-semibold text-white mb-3">最近发送</h3>
          <div v-if="recentAnnouncements.length === 0" class="text-center py-6 text-gray-600 text-sm">
            暂无历史公告
          </div>
          <div v-else class="space-y-3">
            <div
              v-for="(ann, i) in recentAnnouncements" :key="i"
              class="bg-dark-100 rounded-lg p-3 border border-white/[0.04]"
            >
              <p class="text-sm text-white font-medium line-clamp-1">{{ ann.title }}</p>
              <p v-if="ann.content" class="text-xs text-gray-500 mt-1 line-clamp-2">{{ ann.content }}</p>
              <p class="text-xs text-gray-600 mt-1.5">{{ formatTime(ann.createdAt) }}</p>
            </div>
          </div>
        </div>

        <!-- 发送提示 -->
        <div class="card rounded-2xl p-5">
          <h3 class="text-sm font-semibold text-white mb-2">发送须知</h3>
          <ul class="text-xs text-gray-500 space-y-1.5">
            <li class="flex items-start gap-1.5">
              <span class="text-primary mt-0.5">•</span>
              公告将发送给全部注册用户
            </li>
            <li class="flex items-start gap-1.5">
              <span class="text-primary mt-0.5">•</span>
              用户在通知中心查看公告内容
            </li>
            <li class="flex items-start gap-1.5">
              <span class="text-primary mt-0.5">•</span>
              重要公告建议标题包含【重要】标识
            </li>
            <li class="flex items-start gap-1.5">
              <span class="text-primary mt-0.5">•</span>
              发送后不可撤回，请仔细确认
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { notificationService } from '@/services/notifications'

const form = ref({ title: '', content: '', type: 'info' })
const submitting = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const recentAnnouncements = ref<any[]>([])
const stats = ref({ totalSent: 0, thisMonth: 0 })

const announcementTypes = [
  { value: 'info', label: '通知', icon: '📢' },
  { value: 'update', label: '更新', icon: '🆕' },
  { value: 'warning', label: '警告', icon: '⚠️' },
  { value: 'event', label: '活动', icon: '🎉' },
]

const currentTypeIcon = computed(() => {
  return announcementTypes.find(t => t.value === form.value.type)?.icon || '📢'
})

function formatTime(dt: string) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadRecent() {
  try {
    const res: any = await notificationService.getList(0, 10)
    const items = res.data?.content || res.data || []
    // 只显示系统公告类型
    recentAnnouncements.value = items.filter((n: any) => n.type === 'system' || n.type === 'announcement').slice(0, 5)
    stats.value.totalSent = items.length
    // 本月
    const now = new Date()
    stats.value.thisMonth = items.filter((n: any) => {
      const d = new Date(n.createdAt)
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
    }).length
  } catch {}
}

async function submit() {
  if (!form.value.title.trim()) return
  successMsg.value = ''
  errorMsg.value = ''
  submitting.value = true
  try {
    await notificationService.broadcast(form.value.title, form.value.content || undefined)
    successMsg.value = '公告已成功发布给全部用户！'
    recentAnnouncements.value.unshift({
      title: form.value.title,
      content: form.value.content,
      type: 'system',
      createdAt: new Date().toISOString()
    })
    stats.value.totalSent++
    stats.value.thisMonth++
    form.value = { title: '', content: '', type: 'info' }
    setTimeout(() => { successMsg.value = '' }, 5000)
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '发送失败'
    setTimeout(() => { errorMsg.value = '' }, 5000)
  } finally {
    submitting.value = false
  }
}

onMounted(loadRecent)
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
