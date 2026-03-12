<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-gray-800">询价管理</h2>
      <div class="flex items-center gap-2">
        <span class="text-sm text-gray-500">共 {{ total }} 条询价</span>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">加载中...</div>

    <div v-else>
      <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">机器人</th>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">厂商</th>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">联系方式</th>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">状态</th>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">提交时间</th>
              <th class="px-4 py-3 text-left text-gray-500 font-medium">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in inquiries" :key="item.id" class="border-b border-gray-50 last:border-0 hover:bg-gray-50">
              <td class="px-4 py-3">
                <router-link :to="`/robots/${item.robotSlug}`" target="_blank" class="font-medium text-primary-500 hover:underline">
                  {{ item.robotName }}
                </router-link>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ item.manufacturerName }}</td>
              <td class="px-4 py-3">
                <div class="text-gray-700">{{ item.contactName || '-' }}</div>
                <div class="text-gray-400 text-xs">{{ item.contactEmail }}</div>
                <div v-if="item.contactPhone" class="text-gray-400 text-xs">{{ item.contactPhone }}</div>
              </td>
              <td class="px-4 py-3">
                <span :class="statusClass(item.status)" class="px-2 py-0.5 rounded-full text-xs font-medium">
                  {{ statusLabel(item.status) }}
                </span>
              </td>
              <td class="px-4 py-3 text-gray-400 text-xs">{{ formatDate(item.createdAt) }}</td>
              <td class="px-4 py-3">
                <button @click="openDetail(item)" class="text-primary-500 hover:underline text-xs mr-3">查看/回复</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-5">
        <button
          v-for="p in totalPages" :key="p"
          @click="loadPage(p - 1)"
          class="px-3 py-1.5 rounded-lg text-sm border transition-colors"
          :class="p - 1 === currentPage ? 'bg-primary-500 text-white border-primary-500' : 'border-gray-200 text-gray-600 hover:bg-gray-50'"
        >{{ p }}</button>
      </div>
    </div>

    <!-- 详情/回复侧抽屉 -->
    <div v-if="selectedInquiry" class="fixed inset-0 z-50 flex justify-end" @click.self="selectedInquiry = null">
      <div class="bg-white w-full max-w-md h-full shadow-2xl overflow-y-auto p-6">
        <button @click="selectedInquiry = null" class="text-gray-400 hover:text-gray-600 text-xl mb-4">&times; 关闭</button>
        <h3 class="font-bold text-gray-800 mb-1">询价详情</h3>
        <p class="text-sm text-gray-400 mb-4">{{ selectedInquiry.robotName }} - {{ selectedInquiry.manufacturerName }}</p>

        <div class="space-y-3 mb-6">
          <div class="bg-gray-50 rounded-xl p-4">
            <p class="text-xs text-gray-400 mb-1">询价内容</p>
            <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ selectedInquiry.message }}</p>
          </div>
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div><span class="text-gray-400">姓名：</span>{{ selectedInquiry.contactName || '-' }}</div>
            <div><span class="text-gray-400">邮箱：</span>{{ selectedInquiry.contactEmail }}</div>
            <div><span class="text-gray-400">电话：</span>{{ selectedInquiry.contactPhone || '-' }}</div>
            <div><span class="text-gray-400">公司：</span>{{ selectedInquiry.contactCompany || '-' }}</div>
          </div>
        </div>

        <!-- 已有回复 -->
        <div v-if="selectedInquiry.replyContent" class="bg-blue-50 rounded-xl p-4 mb-5">
          <p class="text-xs text-blue-400 mb-1">已回复于 {{ formatDate(selectedInquiry.repliedAt || '') }}</p>
          <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ selectedInquiry.replyContent }}</p>
        </div>

        <!-- 状态更新 -->
        <div class="mb-5">
          <label class="block text-sm font-medium text-gray-700 mb-2">更新状态</label>
          <div class="flex gap-2 flex-wrap">
            <button v-for="s in ['pending','contacted','replied','closed']" :key="s"
              @click="updateStatus(s)"
              :disabled="selectedInquiry.status === s"
              class="px-3 py-1.5 rounded-lg text-xs border transition-colors"
              :class="selectedInquiry.status === s ? 'bg-gray-100 text-gray-400 cursor-default' : 'border-gray-200 text-gray-600 hover:bg-gray-50'"
            >{{ statusLabel(s) }}</button>
          </div>
        </div>

        <!-- 回复表单 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">回复内容</label>
          <textarea
            v-model="replyContent"
            rows="5"
            placeholder="输入回复内容..."
            class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"
          ></textarea>
          <p v-if="replyError" class="text-xs text-red-500 mt-1">{{ replyError }}</p>
          <button
            @click="submitReply"
            :disabled="replySubmitting || !replyContent.trim()"
            class="mt-3 w-full py-2.5 bg-primary-500 text-white rounded-xl text-sm font-medium hover:bg-primary-600 transition-colors disabled:opacity-50"
          >
            {{ replySubmitting ? '提交中...' : '提交回复' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { inquiryService, type InquiryDTO } from '@/services/inquiries'

const inquiries = ref<InquiryDTO[]>([])
const loading = ref(true)
const total = ref(0)
const totalPages = ref(1)
const currentPage = ref(0)

const selectedInquiry = ref<InquiryDTO | null>(null)
const replyContent = ref('')
const replySubmitting = ref(false)
const replyError = ref('')

function statusLabel(s: string) {
  const map: Record<string, string> = { pending: '待处理', contacted: '已联系', replied: '已回复', closed: '已关闭' }
  return map[s] ?? s
}

function statusClass(s: string) {
  const map: Record<string, string> = {
    pending: 'bg-yellow-100 text-yellow-700',
    contacted: 'bg-blue-100 text-blue-700',
    replied: 'bg-green-100 text-green-700',
    closed: 'bg-gray-100 text-gray-500'
  }
  return map[s] ?? 'bg-gray-100 text-gray-500'
}

function formatDate(d: string) {
  if (!d) return '-'
  return new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

async function loadPage(page: number) {
  loading.value = true
  try {
    const res: any = await inquiryService.adminList(page, 20)
    const data = res.data || res
    inquiries.value = data.content || []
    total.value = data.total || 0
    totalPages.value = data.totalPages || 1
    currentPage.value = page
  } finally {
    loading.value = false
  }
}

function openDetail(item: InquiryDTO) {
  selectedInquiry.value = { ...item }
  replyContent.value = item.replyContent || ''
  replyError.value = ''
}

async function updateStatus(status: string) {
  if (!selectedInquiry.value) return
  try {
    const res: any = await inquiryService.adminUpdateStatus(selectedInquiry.value.id, status)
    const updated = res.data || res
    selectedInquiry.value = updated
    const idx = inquiries.value.findIndex(i => i.id === updated.id)
    if (idx !== -1) inquiries.value[idx] = updated
  } catch {}
}

async function submitReply() {
  if (!selectedInquiry.value || !replyContent.value.trim()) return
  replyError.value = ''
  replySubmitting.value = true
  try {
    const res: any = await inquiryService.adminReply(selectedInquiry.value.id, replyContent.value)
    const updated = res.data || res
    selectedInquiry.value = updated
    const idx = inquiries.value.findIndex(i => i.id === updated.id)
    if (idx !== -1) inquiries.value[idx] = updated
  } catch (e: any) {
    replyError.value = e?.response?.data?.message || '提交失败'
  } finally {
    replySubmitting.value = false
  }
}

onMounted(() => loadPage(0))
</script>
