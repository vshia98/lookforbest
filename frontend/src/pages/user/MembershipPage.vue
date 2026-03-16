<template>
  <div class="max-w-5xl mx-auto px-4 py-8 space-y-10">
    <!-- 当前会员状态 -->
    <section class="bg-dark-50 rounded-2xl shadow p-6">
      <h2 class="text-lg font-semibold text-white mb-4">我的会员</h2>
      <div v-if="membershipLoading" class="text-gray-400 text-sm">加载中...</div>
      <div v-else-if="currentMembership" class="flex items-center gap-6">
        <div>
          <span
            class="inline-block px-3 py-1 rounded-full text-sm font-semibold"
            :class="planBadgeClass(currentMembership.plan?.nameEn)"
          >
            {{ currentMembership.plan?.name }}
          </span>
        </div>
        <div class="text-sm text-gray-600">
          <template v-if="currentMembership.daysRemaining === -1">
            有效期：<span class="font-semibold text-green-600">永久</span>
          </template>
          <template v-else>
            到期时间：<span class="font-semibold">{{ formatDate(currentMembership.endDate) }}</span>
            &nbsp;（剩余 <span class="font-semibold text-orange-500">{{ currentMembership.daysRemaining }}</span> 天）
          </template>
        </div>
        <div class="ml-auto">
          <ul class="flex flex-wrap gap-2">
            <li
              v-for="feat in currentMembership.plan?.features"
              :key="feat"
              class="text-xs bg-dark-100 text-gray-300 px-2 py-1 rounded"
            >{{ feat }}</li>
          </ul>
        </div>
      </div>
      <div v-else class="text-sm text-gray-500">
        您尚未开通会员，当前使用<span class="font-semibold mx-1">免费版</span>功能。
      </div>
    </section>

    <!-- 套餐对比 -->
    <section>
      <h2 class="text-lg font-semibold text-white mb-6">选择套餐</h2>
      <div v-if="plansLoading" class="text-gray-400 text-sm">加载中...</div>
      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div
          v-for="plan in plans"
          :key="plan.id"
          class="relative bg-dark-50 rounded-2xl shadow border-2 p-6 flex flex-col"
          :class="plan.nameEn === 'Pro' ? 'border-primary-500' : 'border-white/[0.04]'"
        >
          <!-- 推荐徽章 -->
          <span
            v-if="plan.nameEn === 'Pro'"
            class="absolute -top-3 left-1/2 -translate-x-1/2 bg-primary text-[#1a1a1a] text-xs font-bold px-3 py-1 rounded-full shadow"
          >推荐</span>

          <div class="mb-4">
            <h3 class="text-xl font-bold text-white">{{ plan.name }}</h3>
            <p class="text-sm text-gray-500 mt-1">{{ plan.description }}</p>
          </div>

          <div class="mb-6">
            <span class="text-4xl font-extrabold text-white">
              {{ plan.priceCny == 0 ? '免费' : '¥' + plan.priceCny }}
            </span>
            <span v-if="plan.priceCny > 0" class="text-sm text-gray-500 ml-1">
              / {{ plan.durationDays }}天
            </span>
          </div>

          <ul class="flex-1 space-y-2 mb-6">
            <li v-for="feat in plan.features" :key="feat" class="flex items-center gap-2 text-sm text-gray-300">
              <span class="text-green-500 font-bold">✓</span> {{ feat }}
            </li>
            <li class="flex items-center gap-2 text-sm text-gray-400">
              <span class="text-gray-300">✗</span>
              最多对比 {{ plan.maxCompareRobots >= 999 ? '无限' : plan.maxCompareRobots }} 款
            </li>
            <li class="flex items-center gap-2 text-sm text-gray-400">
              <span :class="plan.apiAccess ? 'text-green-500' : 'text-gray-300'">{{ plan.apiAccess ? '✓' : '✗' }}</span>
              API 访问
            </li>
          </ul>

          <button
            @click="handleUpgrade(plan)"
            :disabled="isCurrentPlan(plan) || upgrading"
            class="w-full py-2 rounded-lg font-semibold text-sm transition-colors"
            :class="isCurrentPlan(plan)
              ? 'bg-dark-100 text-gray-400 cursor-not-allowed'
              : 'bg-primary text-[#1a1a1a] hover:bg-primary-400'"
          >
            {{ isCurrentPlan(plan) ? '当前套餐' : '升级' }}
          </button>
        </div>
      </div>
    </section>

    <!-- 支付弹窗 -->
    <div v-if="pendingOrder" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div class="bg-dark-50 rounded-2xl shadow-xl p-8 w-full max-w-sm text-center space-y-4">
        <h3 class="text-lg font-bold text-white">确认订单</h3>
        <p class="text-sm text-gray-600">套餐：<span class="font-semibold">{{ pendingOrder.planName }}</span></p>
        <p class="text-2xl font-extrabold text-white">¥{{ pendingOrder.amountCny }}</p>
        <p class="text-xs text-gray-400">订单号：{{ pendingOrder.orderNo }}</p>
        <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-xs text-yellow-400">
          仅供演示 — 点击"模拟支付"将立即激活会员
        </div>
        <div class="flex gap-3">
          <button
            @click="pendingOrder = null"
            class="flex-1 py-2 rounded-lg border border-white/[0.06] text-sm text-gray-600 hover:bg-dark-100"
          >取消</button>
          <button
            @click="doSimulatePay"
            :disabled="paying"
            class="flex-1 py-2 rounded-lg bg-green-600 text-[#1a1a1a] text-sm font-semibold hover:bg-green-700 disabled:opacity-60"
          >
            {{ paying ? '处理中...' : '模拟支付' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 我的订单 -->
    <section class="bg-dark-50 rounded-2xl shadow p-6">
      <h2 class="text-lg font-semibold text-white mb-4">我的订单</h2>
      <div v-if="ordersLoading" class="text-gray-400 text-sm">加载中...</div>
      <div v-else-if="orders.length === 0" class="text-sm text-gray-400">暂无订单</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="border-b border-white/[0.04] text-gray-500">
              <th class="py-2 pr-4">订单号</th>
              <th class="py-2 pr-4">套餐</th>
              <th class="py-2 pr-4">金额</th>
              <th class="py-2 pr-4">状态</th>
              <th class="py-2 pr-4">时间</th>
              <th class="py-2 pr-4">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id" class="border-b border-white/[0.04] hover:bg-dark-100">
              <td class="py-2 pr-4 font-mono text-xs text-gray-600">{{ order.orderNo }}</td>
              <td class="py-2 pr-4 text-gray-300">{{ order.planName }}</td>
              <td class="py-2 pr-4 text-white font-medium">¥{{ order.amountCny }}</td>
              <td class="py-2 pr-4">
                <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="orderStatusClass(order.status)">
                  {{ orderStatusLabel(order.status) }}
                </span>
              </td>
              <td class="py-2 pr-4 text-gray-500 text-xs">{{ formatDate(order.createdAt) }}</td>
              <td class="py-2 pr-4">
                <button
                  v-if="order.status === 'pending'"
                  @click="payOrder(order)"
                  class="text-xs px-3 py-1 bg-primary text-[#1a1a1a] rounded-lg font-medium hover:bg-primary-400 transition-colors"
                >
                  去支付
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { membershipService } from '@/services/robots'

const plans = ref<any[]>([])
const plansLoading = ref(false)
const currentMembership = ref<any>(null)
const membershipLoading = ref(false)
const orders = ref<any[]>([])
const ordersLoading = ref(false)
const upgrading = ref(false)
const paying = ref(false)
const pendingOrder = ref<any>(null)

async function loadPlans() {
  plansLoading.value = true
  try {
    const res = await membershipService.getPlans()
    plans.value = res.data ?? []
  } finally {
    plansLoading.value = false
  }
}

async function loadMembership() {
  membershipLoading.value = true
  try {
    const res = await membershipService.getMyMembership()
    currentMembership.value = res.data?.membership ?? null
  } catch {
    currentMembership.value = null
  } finally {
    membershipLoading.value = false
  }
}

async function loadOrders() {
  ordersLoading.value = true
  try {
    const res = await membershipService.getMyOrders()
    orders.value = res.data ?? []
  } finally {
    ordersLoading.value = false
  }
}

async function handleUpgrade(plan: any) {
  upgrading.value = true
  try {
    const res = await membershipService.createOrder({ planId: plan.id, paymentMethod: 'manual' })
    const order = res.data
    if (order.status === 'paid') {
      // 免费套餐直接激活
      await loadMembership()
      await loadOrders()
    } else {
      pendingOrder.value = order
    }
  } finally {
    upgrading.value = false
  }
}

function payOrder(order: any) {
  pendingOrder.value = order
}

async function doSimulatePay() {
  if (!pendingOrder.value) return
  paying.value = true
  try {
    await membershipService.simulatePay(pendingOrder.value.orderNo)
    pendingOrder.value = null
    await loadMembership()
    await loadOrders()
  } finally {
    paying.value = false
  }
}

function isCurrentPlan(plan: any) {
  return currentMembership.value?.plan?.id === plan.id
}

function planBadgeClass(nameEn?: string) {
  if (nameEn === 'Enterprise') return 'bg-purple-900/30 text-purple-400'
  if (nameEn === 'Pro') return 'bg-blue-900/30 text-blue-400'
  return 'bg-dark-100 text-gray-600'
}

function orderStatusClass(status: string) {
  const map: Record<string, string> = {
    paid: 'bg-green-900/30 text-green-400',
    pending: 'bg-yellow-900/30 text-yellow-400',
    failed: 'bg-red-900/30 text-red-400',
    refunded: 'bg-dark-100 text-gray-600',
    cancelled: 'bg-dark-100 text-gray-400'
  }
  return map[status] ?? 'bg-dark-100 text-gray-600'
}

function orderStatusLabel(status: string) {
  const map: Record<string, string> = {
    paid: '已支付',
    pending: '待支付',
    failed: '失败',
    refunded: '已退款',
    cancelled: '已取消'
  }
  return map[status] ?? status
}

function formatDate(d?: string) {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(async () => {
  await Promise.all([loadPlans(), loadMembership(), loadOrders()])
})
</script>
