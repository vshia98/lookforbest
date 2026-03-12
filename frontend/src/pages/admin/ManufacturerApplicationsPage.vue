<template>
  <div>
    <h1 class="text-xl font-semibold text-gray-800 mb-6">厂商申请管理</h1>

    <!-- 状态筛选 -->
    <div class="flex gap-2 mb-4">
      <select
        v-model="statusFilter"
        @change="currentPage = 0; loadData()"
        class="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <option value="pending">待审核</option>
        <option value="approved">已通过</option>
        <option value="rejected">已拒绝</option>
      </select>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="flex justify-center py-10">
      <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
    </div>

    <!-- 表格 -->
    <div v-else class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">公司名称</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">申请人邮箱</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">联系人</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">国家</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">申请时间</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-500 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-if="items.length === 0">
            <td colspan="7" class="text-center py-10 text-gray-400">暂无数据</td>
          </tr>
          <template v-for="item in items" :key="item.id">
            <tr
              class="hover:bg-gray-50 cursor-pointer"
              @click="toggleExpand(item.id)"
            >
              <td class="px-4 py-3 font-medium text-gray-800">
                {{ item.companyName }}
                <span v-if="item.companyNameEn" class="text-xs text-gray-400 ml-1">{{ item.companyNameEn }}</span>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ item.user?.email || item.contactEmail }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.contactPerson }}</td>
              <td class="px-4 py-3 text-gray-600">{{ item.country }}</td>
              <td class="px-4 py-3 text-gray-400">{{ formatDate(item.createdAt) }}</td>
              <td class="px-4 py-3">
                <span :class="statusClass(item.status)" class="inline-block text-xs font-medium px-2 py-0.5 rounded-full">
                  {{ statusLabel(item.status) }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div v-if="item.status === 'pending'" class="flex gap-2" @click.stop>
                  <button
                    @click="handleApprove(item)"
                    :disabled="actionLoading === item.id"
                    class="text-xs bg-green-50 text-green-700 hover:bg-green-100 border border-green-200 px-2.5 py-1 rounded-md transition-colors disabled:opacity-50"
                  >通过</button>
                  <button
                    @click="openRejectModal(item)"
                    :disabled="actionLoading === item.id"
                    class="text-xs bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 px-2.5 py-1 rounded-md transition-colors disabled:opacity-50"
                  >拒绝</button>
                </div>
                <span v-else class="text-gray-400 text-xs">—</span>
              </td>
            </tr>
            <!-- 展开详情行 -->
            <tr v-if="expandedId === item.id" class="bg-blue-50">
              <td colspan="7" class="px-6 py-4">
                <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 text-sm">
                  <div>
                    <span class="text-gray-500">联系邮箱：</span>
                    <span class="text-gray-800">{{ item.contactEmail }}</span>
                  </div>
                  <div v-if="item.contactPhone">
                    <span class="text-gray-500">联系电话：</span>
                    <span class="text-gray-800">{{ item.contactPhone }}</span>
                  </div>
                  <div v-if="item.websiteUrl">
                    <span class="text-gray-500">官网：</span>
                    <a :href="item.websiteUrl" target="_blank" class="text-blue-600 hover:underline">{{ item.websiteUrl }}</a>
                  </div>
                  <div v-if="item.businessLicense">
                    <span class="text-gray-500">营业执照：</span>
                    <a :href="item.businessLicense" target="_blank" class="text-blue-600 hover:underline">查看图片</a>
                  </div>
                  <div v-if="item.manufacturerId">
                    <span class="text-gray-500">认领厂商ID：</span>
                    <span class="text-gray-800">#{{ item.manufacturerId }}</span>
                  </div>
                  <div v-if="item.rejectReason">
                    <span class="text-gray-500">拒绝原因：</span>
                    <span class="text-red-700">{{ item.rejectReason }}</span>
                  </div>
                </div>
                <div v-if="item.description" class="mt-3">
                  <p class="text-gray-500 text-sm mb-1">申请说明：</p>
                  <p class="text-gray-700 text-sm bg-white rounded-lg px-3 py-2 border border-gray-200">{{ item.description }}</p>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="px-4 py-3 border-t border-gray-100 flex items-center justify-between">
        <p class="text-sm text-gray-500">共 {{ total }} 条</p>
        <div class="flex gap-2">
          <button
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="text-sm px-3 py-1 border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >上一页</button>
          <span class="text-sm px-3 py-1 text-gray-600">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button
            @click="changePage(currentPage + 1)"
            :disabled="currentPage >= totalPages - 1"
            class="text-sm px-3 py-1 border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50"
          >下一页</button>
        </div>
      </div>
    </div>

    <!-- 拒绝弹窗 -->
    <div v-if="rejectModal.visible" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-xl shadow-lg w-full max-w-md p-6">
        <h3 class="text-base font-semibold text-gray-800 mb-4">拒绝申请</h3>
        <p class="text-sm text-gray-600 mb-3">公司：{{ rejectModal.item?.companyName }}</p>
        <textarea
          v-model="rejectModal.reason"
          rows="4"
          placeholder="请填写拒绝原因（将通知申请人）"
          class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-400 resize-none mb-4"
        />
        <div class="flex gap-3 justify-end">
          <button
            @click="rejectModal.visible = false"
            class="text-sm px-4 py-2 border border-gray-200 rounded-lg hover:bg-gray-50"
          >取消</button>
          <button
            @click="handleReject"
            :disabled="actionLoading === rejectModal.item?.id"
            class="text-sm px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
          >确认拒绝</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { manufacturerPortalService } from '@/services/robots'

const loading = ref(false)
const items = ref<any[]>([])
const total = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)
const statusFilter = ref('pending')
const expandedId = ref<number | null>(null)
const actionLoading = ref<number | null>(null)

const rejectModal = ref({
  visible: false,
  item: null as any,
  reason: ''
})

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await manufacturerPortalService.listApplications(statusFilter.value, currentPage.value, 20)
    const page = res.data
    items.value = page.content ?? []
    total.value = page.totalElements ?? 0
    totalPages.value = page.totalPages ?? 0
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

function changePage(page: number) {
  currentPage.value = page
  loadData()
}

async function handleApprove(item: any) {
  actionLoading.value = item.id
  try {
    await manufacturerPortalService.approveApplication(item.id)
    await loadData()
  } catch (e: any) {
    alert(e?.response?.data?.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

function openRejectModal(item: any) {
  rejectModal.value = { visible: true, item, reason: '' }
}

async function handleReject() {
  const item = rejectModal.value.item
  if (!item) return
  actionLoading.value = item.id
  try {
    await manufacturerPortalService.rejectApplication(item.id, rejectModal.value.reason)
    rejectModal.value.visible = false
    await loadData()
  } catch (e: any) {
    alert(e?.response?.data?.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

function statusLabel(status: string): string {
  const map: Record<string, string> = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
  return map[status] ?? status
}

function statusClass(status: string): string {
  const map: Record<string, string> = {
    pending: 'bg-yellow-50 text-yellow-700',
    approved: 'bg-green-50 text-green-700',
    rejected: 'bg-red-50 text-red-700'
  }
  return map[status] ?? 'bg-gray-50 text-gray-700'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
</script>
