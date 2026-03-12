<template>
  <div>
    <h1 class="text-xl font-semibold text-gray-800 mb-6">内容审核</h1>

    <!-- 选项卡 -->
    <div class="flex gap-1 bg-gray-100 rounded-lg p-1 w-fit mb-6">
      <button
        @click="activeTab = 'reviews'"
        class="px-4 py-1.5 rounded-md text-sm font-medium transition-colors"
        :class="activeTab === 'reviews' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'"
      >
        评测审核
        <span v-if="reviewPendingCount > 0" class="ml-1.5 bg-red-500 text-white text-xs rounded-full px-1.5 py-0.5">{{ reviewPendingCount }}</span>
      </button>
      <button
        @click="activeTab = 'cases'"
        class="px-4 py-1.5 rounded-md text-sm font-medium transition-colors"
        :class="activeTab === 'cases' ? 'bg-white shadow text-gray-800' : 'text-gray-500 hover:text-gray-700'"
      >
        案例审核
        <span v-if="casePendingCount > 0" class="ml-1.5 bg-red-500 text-white text-xs rounded-full px-1.5 py-0.5">{{ casePendingCount }}</span>
      </button>
    </div>

    <!-- 状态筛选 -->
    <div class="flex gap-2 mb-4">
      <select
        v-model="statusFilter"
        @change="currentPage = 0; loadData()"
        class="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <option value="pending_review">待审核</option>
        <option value="published">已发布</option>
        <option value="rejected">已拒绝</option>
        <option value="draft">草稿</option>
      </select>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="flex justify-center py-10">
      <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
    </div>

    <!-- 评测表格 -->
    <div v-else-if="activeTab === 'reviews'" class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">标题</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">作者</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">机器人</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">评分</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">提交时间</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-if="items.length === 0">
            <td colspan="7" class="text-center py-10 text-gray-400">暂无数据</td>
          </tr>
          <tr v-for="item in items" :key="item.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3">
              <a :href="`/reviews/${item.id}`" target="_blank" class="text-blue-600 hover:underline line-clamp-1 max-w-[200px] block">{{ item.title }}</a>
            </td>
            <td class="px-4 py-3 text-gray-600">{{ item.authorName }}</td>
            <td class="px-4 py-3 text-gray-600">{{ item.robotName }}</td>
            <td class="px-4 py-3">
              <span class="text-yellow-400">{{ '★'.repeat(item.rating) }}</span>
            </td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 rounded-full text-xs" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
            </td>
            <td class="px-4 py-3 text-gray-400 text-xs">{{ formatDate(item.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <button
                  v-if="item.status === 'pending_review'"
                  @click="publishReview(item.id)"
                  class="text-xs bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded transition-colors"
                >
                  发布
                </button>
                <button
                  v-if="item.status === 'pending_review'"
                  @click="openRejectModal('review', item.id)"
                  class="text-xs bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded transition-colors"
                >
                  拒绝
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 案例表格 -->
    <div v-else class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">标题</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">作者</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">行业</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">提交时间</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-if="items.length === 0">
            <td colspan="6" class="text-center py-10 text-gray-400">暂无数据</td>
          </tr>
          <tr v-for="item in items" :key="item.id" class="hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3">
              <a :href="`/case-studies/${item.id}`" target="_blank" class="text-blue-600 hover:underline line-clamp-1 max-w-[200px] block">{{ item.title }}</a>
            </td>
            <td class="px-4 py-3 text-gray-600">{{ item.authorName }}</td>
            <td class="px-4 py-3 text-gray-500">{{ item.industry || '-' }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-0.5 rounded-full text-xs" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
            </td>
            <td class="px-4 py-3 text-gray-400 text-xs">{{ formatDate(item.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-2">
                <button
                  v-if="item.status === 'pending_review'"
                  @click="publishCase(item.id)"
                  class="text-xs bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded transition-colors"
                >
                  发布
                </button>
                <button
                  v-if="item.status === 'pending_review'"
                  @click="openRejectModal('case', item.id)"
                  class="text-xs bg-red-500 hover:bg-red-600 text-white px-2 py-1 rounded transition-colors"
                >
                  拒绝
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center items-center gap-2 mt-6">
      <button
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
        class="px-4 py-2 rounded-lg border border-gray-200 text-sm disabled:opacity-40 hover:bg-gray-50"
      >
        上一页
      </button>
      <span class="text-sm text-gray-500">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage >= totalPages - 1"
        @click="changePage(currentPage + 1)"
        class="px-4 py-2 rounded-lg border border-gray-200 text-sm disabled:opacity-40 hover:bg-gray-50"
      >
        下一页
      </button>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="rejectModal.show" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">填写拒绝原因</h3>
        <textarea
          v-model="rejectModal.reason"
          rows="4"
          placeholder="请填写拒绝原因，将会通知作者..."
          class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
        ></textarea>
        <div class="flex justify-end gap-3 mt-4">
          <button @click="rejectModal.show = false" class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">取消</button>
          <button @click="confirmReject" class="px-4 py-2 bg-red-500 hover:bg-red-600 text-white text-sm rounded-lg transition-colors">确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, reactive } from 'vue'
import http from '@/services/api'

const activeTab = ref<'reviews' | 'cases'>('reviews')
const statusFilter = ref('pending_review')
const items = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const reviewPendingCount = ref(0)
const casePendingCount = ref(0)

const rejectModal = reactive({
  show: false,
  type: '' as 'review' | 'case',
  id: 0,
  reason: ''
})

function statusLabel(s: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    pending_review: '待审核',
    published: '已发布',
    rejected: '已拒绝'
  }
  return map[s] || s
}

function statusClass(s: string) {
  const map: Record<string, string> = {
    draft: 'bg-gray-100 text-gray-600',
    pending_review: 'bg-yellow-100 text-yellow-700',
    published: 'bg-green-100 text-green-700',
    rejected: 'bg-red-100 text-red-600'
  }
  return map[s] || ''
}

function formatDate(dt: string) {
  return new Date(dt).toLocaleDateString('zh-CN')
}

async function loadData() {
  loading.value = true
  try {
    const endpoint = activeTab.value === 'reviews' ? '/admin/reviews' : '/admin/case-studies'
    const res = await http.get(endpoint, { params: { status: statusFilter.value, page: currentPage.value, size: 20 } })
    items.value = res.data.content
    totalPages.value = res.data.totalPages
  } finally {
    loading.value = false
  }
}

async function loadPendingCounts() {
  const [rRes, cRes] = await Promise.all([
    http.get('/admin/reviews', { params: { status: 'pending_review', size: 1 } }),
    http.get('/admin/case-studies', { params: { status: 'pending_review', size: 1 } })
  ])
  reviewPendingCount.value = rRes.data.total || 0
  casePendingCount.value = cRes.data.total || 0
}

async function publishReview(id: number) {
  await http.patch(`/admin/reviews/${id}/publish`)
  loadData()
  loadPendingCounts()
}

async function publishCase(id: number) {
  await http.patch(`/admin/case-studies/${id}/publish`)
  loadData()
  loadPendingCounts()
}

function openRejectModal(type: 'review' | 'case', id: number) {
  rejectModal.type = type
  rejectModal.id = id
  rejectModal.reason = ''
  rejectModal.show = true
}

async function confirmReject() {
  const endpoint = rejectModal.type === 'review'
    ? `/admin/reviews/${rejectModal.id}/reject`
    : `/admin/case-studies/${rejectModal.id}/reject`
  await http.patch(endpoint, { reason: rejectModal.reason })
  rejectModal.show = false
  loadData()
  loadPendingCounts()
}

function changePage(page: number) {
  currentPage.value = page
  loadData()
}

watch(activeTab, () => {
  currentPage.value = 0
  statusFilter.value = 'pending_review'
  loadData()
})

onMounted(() => {
  loadData()
  loadPendingCounts()
})
</script>
