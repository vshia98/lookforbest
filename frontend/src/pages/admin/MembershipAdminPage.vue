<template>
  <div class="space-y-6">
    <h1 class="text-2xl font-bold text-white">会员管理</h1>

    <!-- 标签页 -->
    <div class="flex gap-1 border-b border-white/[0.06]">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        @click="activeTab = tab.key"
        class="px-4 py-2 text-sm font-medium border-b-2 transition-colors"
        :class="activeTab === tab.key
          ? 'border-primary-600 text-primary'
          : 'border-transparent text-gray-500 hover:text-gray-300'"
      >{{ tab.label }}</button>
    </div>

    <!-- Tab 1: 套餐管理 -->
    <div v-if="activeTab === 'plans'" class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-base font-semibold text-gray-300">套餐列表</h2>
        <button
          @click="openPlanForm(null)"
          class="px-4 py-2 bg-primary text-[#1a1a1a] text-sm rounded-lg hover:bg-primary-400"
        >+ 新增套餐</button>
      </div>

      <div v-if="plansLoading" class="text-gray-400 text-sm">加载中...</div>
      <div v-else class="overflow-x-auto bg-dark-50 rounded-xl shadow">
        <table class="w-full text-sm text-left">
          <thead class="bg-dark-100 border-b border-white/[0.04]">
            <tr>
              <th class="px-4 py-3 text-gray-400">ID</th>
              <th class="px-4 py-3 text-gray-400">套餐名</th>
              <th class="px-4 py-3 text-gray-400">价格(CNY)</th>
              <th class="px-4 py-3 text-gray-400">天数</th>
              <th class="px-4 py-3 text-gray-400">对比上限</th>
              <th class="px-4 py-3 text-gray-400">API</th>
              <th class="px-4 py-3 text-gray-400">状态</th>
              <th class="px-4 py-3 text-gray-400">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="plan in adminPlans" :key="plan.id" class="border-b border-white/[0.04] hover:bg-dark-100">
              <td class="px-4 py-3 text-gray-500">{{ plan.id }}</td>
              <td class="px-4 py-3 font-medium">{{ plan.name }}<span class="text-xs text-gray-400 ml-1">({{ plan.nameEn }})</span></td>
              <td class="px-4 py-3">{{ plan.priceCny == 0 ? '免费' : '¥' + plan.priceCny }}</td>
              <td class="px-4 py-3">{{ plan.durationDays == 0 ? '永久' : plan.durationDays + '天' }}</td>
              <td class="px-4 py-3">{{ plan.maxCompareRobots >= 999 ? '无限' : plan.maxCompareRobots }}</td>
              <td class="px-4 py-3">
                <span :class="plan.apiAccess ? 'text-green-600' : 'text-gray-300'">{{ plan.apiAccess ? '✓' : '✗' }}</span>
              </td>
              <td class="px-4 py-3">
                <span :class="plan.isActive ? 'text-green-600' : 'text-red-400'" class="text-xs font-medium">
                  {{ plan.isActive ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="px-4 py-3">
                <button
                  @click="openPlanForm(plan)"
                  class="text-blue-400 hover:text-blue-800 text-xs font-medium"
                >编辑</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 套餐编辑表单 -->
      <div v-if="editingPlan !== undefined" class="bg-dark-50 rounded-xl shadow p-6 space-y-4">
        <h3 class="text-base font-semibold text-white">{{ editingPlan?.id ? '编辑套餐' : '新增套餐' }}</h3>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-xs text-gray-500 mb-1 block">套餐名称（中文）</label>
            <input v-model="planForm.name" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">套餐名称（英文）</label>
            <input v-model="planForm.nameEn" class="input-dark" />
          </div>
          <div class="col-span-2">
            <label class="text-xs text-gray-500 mb-1 block">描述</label>
            <input v-model="planForm.description" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">价格（CNY）</label>
            <input v-model.number="planForm.priceCny" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">价格（USD）</label>
            <input v-model.number="planForm.priceUsd" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">有效天数（0=永久）</label>
            <input v-model.number="planForm.durationDays" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">最大对比数量</label>
            <input v-model.number="planForm.maxCompareRobots" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">每日导出次数（0=不限）</label>
            <input v-model.number="planForm.maxExportsPerDay" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 mb-1 block">排序值</label>
            <input v-model.number="planForm.sortOrder" type="number" class="input-dark" />
          </div>
          <div class="col-span-2">
            <label class="text-xs text-gray-500 mb-1 block">功能列表（逗号分隔）</label>
            <input v-model="planFeaturesStr" class="input-dark" placeholder="功能1,功能2,功能3" />
          </div>
          <div class="flex items-center gap-4">
            <label class="flex items-center gap-2 text-sm">
              <input v-model="planForm.apiAccess" type="checkbox" class="rounded" />
              开启 API 访问
            </label>
            <label class="flex items-center gap-2 text-sm">
              <input v-model="planForm.isActive" type="checkbox" class="rounded" />
              启用
            </label>
          </div>
        </div>
        <div class="flex gap-3 pt-2">
          <button
            @click="savePlan"
            :disabled="savingPlan"
            class="px-5 py-2 bg-primary text-[#1a1a1a] text-sm rounded-lg hover:bg-primary-400 disabled:opacity-60"
          >{{ savingPlan ? '保存中...' : '保存' }}</button>
          <button
            @click="editingPlan = undefined"
            class="px-5 py-2 border border-white/[0.06] text-sm text-gray-400 rounded-lg hover:bg-dark-100"
          >取消</button>
        </div>
      </div>
    </div>

    <!-- Tab 2: 订单管理 -->
    <div v-if="activeTab === 'orders'" class="space-y-4">
      <div class="flex items-center gap-3">
        <select v-model="orderStatusFilter" class="input-dark">
          <option value="">全部状态</option>
          <option value="pending">待支付</option>
          <option value="paid">已支付</option>
          <option value="cancelled">已取消</option>
          <option value="refunded">已退款</option>
          <option value="failed">失败</option>
        </select>
        <button @click="loadAdminOrders" class="px-4 py-2 bg-dark-100 text-gray-300 text-sm rounded-lg hover:bg-white/[0.06]">
          刷新
        </button>
      </div>

      <div v-if="ordersLoading" class="text-gray-400 text-sm">加载中...</div>
      <div v-else class="overflow-x-auto bg-dark-50 rounded-xl shadow">
        <table class="w-full text-sm text-left">
          <thead class="bg-dark-100 border-b border-white/[0.04]">
            <tr>
              <th class="px-4 py-3 text-gray-400">订单号</th>
              <th class="px-4 py-3 text-gray-400">用户ID</th>
              <th class="px-4 py-3 text-gray-400">套餐</th>
              <th class="px-4 py-3 text-gray-400">金额</th>
              <th class="px-4 py-3 text-gray-400">支付方式</th>
              <th class="px-4 py-3 text-gray-400">状态</th>
              <th class="px-4 py-3 text-gray-400">创建时间</th>
              <th class="px-4 py-3 text-gray-400">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in filteredAdminOrders" :key="order.id" class="border-b border-white/[0.04] hover:bg-dark-100">
              <td class="px-4 py-3 font-mono text-xs text-gray-400">{{ order.orderNo }}</td>
              <td class="px-4 py-3">{{ order.userId }}</td>
              <td class="px-4 py-3">{{ order.planName }}</td>
              <td class="px-4 py-3">¥{{ order.amountCny }}</td>
              <td class="px-4 py-3 text-gray-500">{{ order.paymentMethod }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="orderStatusClass(order.status)">
                  {{ orderStatusLabel(order.status) }}
                </span>
              </td>
              <td class="px-4 py-3 text-xs text-gray-500">{{ formatDate(order.createdAt) }}</td>
              <td class="px-4 py-3">
                <button
                  v-if="order.status !== 'paid'"
                  @click="openActivateModal(order)"
                  class="text-green-600 hover:text-green-800 text-xs font-medium"
                >手动激活</button>
              </td>
            </tr>
            <tr v-if="filteredAdminOrders.length === 0">
              <td colspan="8" class="px-4 py-8 text-center text-gray-400 text-sm">暂无订单</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="flex items-center gap-2 justify-end">
        <button
          :disabled="ordersPage === 0"
          @click="ordersPage--; loadAdminOrders()"
          class="px-3 py-1 text-sm border rounded-lg disabled:opacity-40"
        >上一页</button>
        <span class="text-sm text-gray-400">第 {{ ordersPage + 1 }} 页</span>
        <button
          @click="ordersPage++; loadAdminOrders()"
          class="px-3 py-1 text-sm border rounded-lg"
        >下一页</button>
      </div>

      <!-- 手动激活弹窗 -->
      <div v-if="activatingOrder" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-dark-50 rounded-2xl shadow-xl p-8 w-full max-w-sm space-y-4">
          <h3 class="text-lg font-bold text-white">手动激活会员</h3>
          <p class="text-sm text-gray-500">订单号：{{ activatingOrder.orderNo }}</p>
          <div>
            <label class="text-xs text-gray-500 block mb-1">用户ID</label>
            <input v-model.number="activateForm.userId" type="number" class="input-dark" />
          </div>
          <div>
            <label class="text-xs text-gray-500 block mb-1">套餐ID</label>
            <select v-model.number="activateForm.planId" class="input-dark">
              <option v-for="p in adminPlans" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>
          <div>
            <label class="text-xs text-gray-500 block mb-1">有效天数（0=永久）</label>
            <input v-model.number="activateForm.durationDays" type="number" class="input-dark" />
          </div>
          <div class="flex gap-3">
            <button @click="activatingOrder = null" class="flex-1 py-2 border border-white/[0.06] text-sm rounded-lg">取消</button>
            <button
              @click="doManualActivate"
              :disabled="activating"
              class="flex-1 py-2 bg-green-600 text-[#1a1a1a] text-sm rounded-lg hover:bg-green-700 disabled:opacity-60"
            >{{ activating ? '激活中...' : '确认激活' }}</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 3: 会员统计 -->
    <div v-if="activeTab === 'stats'" class="space-y-6">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div class="bg-dark-50 rounded-xl shadow p-6 text-center">
          <p class="text-3xl font-extrabold text-primary">{{ statsData.totalOrders }}</p>
          <p class="text-sm text-gray-500 mt-1">总订单数</p>
        </div>
        <div class="bg-dark-50 rounded-xl shadow p-6 text-center">
          <p class="text-3xl font-extrabold text-green-600">{{ statsData.paidOrders }}</p>
          <p class="text-sm text-gray-500 mt-1">已支付订单</p>
        </div>
        <div class="bg-dark-50 rounded-xl shadow p-6 text-center">
          <p class="text-3xl font-extrabold text-yellow-600">¥{{ statsData.totalRevenue }}</p>
          <p class="text-sm text-gray-500 mt-1">总收入（CNY）</p>
        </div>
      </div>

      <div class="bg-dark-50 rounded-xl shadow p-6">
        <h3 class="text-base font-semibold text-white mb-4">各套餐订单分布</h3>
        <div v-if="statsData.planDistribution.length === 0" class="text-gray-400 text-sm">暂无数据</div>
        <div v-else class="space-y-3">
          <div
            v-for="item in statsData.planDistribution"
            :key="item.planName"
            class="flex items-center gap-4"
          >
            <span class="text-sm text-gray-300 w-24">{{ item.planName }}</span>
            <div class="flex-1 bg-dark-100 rounded-full h-3">
              <div
                class="bg-primary h-3 rounded-full"
                :style="{ width: Math.round((item.count / statsData.paidOrders) * 100) + '%' }"
              ></div>
            </div>
            <span class="text-sm text-gray-400 w-12 text-right">{{ item.count }} 单</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { membershipService } from '@/services/robots'

const tabs = [
  { key: 'plans', label: '套餐管理' },
  { key: 'orders', label: '订单管理' },
  { key: 'stats', label: '会员统计' }
]
const activeTab = ref('plans')

// ---- Plans ----
const adminPlans = ref<any[]>([])
const plansLoading = ref(false)
const editingPlan = ref<any>(undefined)
const planForm = ref<any>({})
const planFeaturesStr = ref('')
const savingPlan = ref(false)

async function loadAdminPlans() {
  plansLoading.value = true
  try {
    const res = await membershipService.adminListPlans()
    adminPlans.value = res.data ?? []
  } finally {
    plansLoading.value = false
  }
}

function openPlanForm(plan: any) {
  editingPlan.value = plan ?? {}
  planForm.value = plan ? { ...plan } : {
    name: '', nameEn: '', description: '',
    priceCny: 0, priceUsd: 0, durationDays: 30,
    maxCompareRobots: 3, maxExportsPerDay: 0, sortOrder: 0,
    apiAccess: false, isActive: true, features: []
  }
  planFeaturesStr.value = (planForm.value.features ?? []).join(',')
}

async function savePlan() {
  savingPlan.value = true
  try {
    planForm.value.features = planFeaturesStr.value.split(',').map((s: string) => s.trim()).filter(Boolean)
    if (planForm.value.id) {
      await membershipService.adminUpdatePlan(planForm.value.id, planForm.value)
    } else {
      await membershipService.adminCreatePlan(planForm.value)
    }
    editingPlan.value = undefined
    await loadAdminPlans()
  } finally {
    savingPlan.value = false
  }
}

// ---- Orders ----
const adminOrders = ref<any[]>([])
const ordersLoading = ref(false)
const ordersPage = ref(0)
const orderStatusFilter = ref('')

const filteredAdminOrders = computed(() => {
  if (!orderStatusFilter.value) return adminOrders.value
  return adminOrders.value.filter(o => o.status === orderStatusFilter.value)
})

async function loadAdminOrders() {
  ordersLoading.value = true
  try {
    const res = await membershipService.adminListOrders({ page: ordersPage.value, size: 20 })
    // handle both paginated and list response
    adminOrders.value = res.data?.content ?? res.data ?? []
  } finally {
    ordersLoading.value = false
  }
}

const activatingOrder = ref<any>(null)
const activateForm = ref({ userId: 0, planId: 0, durationDays: 30 })
const activating = ref(false)

function openActivateModal(order: any) {
  activatingOrder.value = order
  activateForm.value = {
    userId: order.userId,
    planId: order.planId,
    durationDays: 30
  }
}

async function doManualActivate() {
  if (!activatingOrder.value) return
  activating.value = true
  try {
    await membershipService.adminActivate(activatingOrder.value.orderNo, activateForm.value)
    activatingOrder.value = null
    await loadAdminOrders()
  } finally {
    activating.value = false
  }
}

// ---- Stats ----
const statsData = computed(() => {
  const total = adminOrders.value.length
  const paid = adminOrders.value.filter(o => o.status === 'paid')
  const paidCount = paid.length
  const revenue = paid.reduce((sum: number, o: any) => sum + parseFloat(o.amountCny || 0), 0)

  const planMap: Record<string, number> = {}
  paid.forEach((o: any) => {
    planMap[o.planName] = (planMap[o.planName] ?? 0) + 1
  })
  const planDistribution = Object.entries(planMap).map(([planName, count]) => ({ planName, count }))

  return {
    totalOrders: total,
    paidOrders: paidCount,
    totalRevenue: revenue.toFixed(2),
    planDistribution
  }
})

function orderStatusClass(status: string) {
  const map: Record<string, string> = {
    paid: 'bg-green-900/30 text-green-400',
    pending: 'bg-yellow-900/30 text-yellow-400',
    failed: 'bg-red-900/30 text-red-400',
    refunded: 'bg-dark-100 text-gray-400',
    cancelled: 'bg-dark-100 text-gray-400'
  }
  return map[status] ?? 'bg-dark-100 text-gray-400'
}

function orderStatusLabel(status: string) {
  const map: Record<string, string> = {
    paid: '已支付', pending: '待支付', failed: '失败', refunded: '已退款', cancelled: '已取消'
  }
  return map[status] ?? status
}

function formatDate(d?: string) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

watch(activeTab, (tab) => {
  if (tab === 'orders' && adminOrders.value.length === 0) loadAdminOrders()
})

onMounted(async () => {
  await loadAdminPlans()
})
</script>
