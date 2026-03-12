<template>
  <div class="bg-white rounded-xl border border-gray-100 p-5">
    <div class="flex items-center justify-between mb-4">
      <h3 class="font-semibold text-gray-800">价格趋势（近90天）</h3>
      <div v-if="latestPrice" class="text-right">
        <div class="text-2xl font-bold text-blue-600">¥{{ latestPrice.toLocaleString() }}</div>
        <div v-if="priceChange !== null" class="text-sm" :class="priceChange <= 0 ? 'text-green-600' : 'text-red-500'">
          {{ priceChange <= 0 ? '↓' : '↑' }} {{ Math.abs(priceChange).toFixed(1) }}%
        </div>
      </div>
    </div>

    <!-- 无数据 -->
    <div v-if="chartData.length === 0" class="h-40 flex items-center justify-center text-gray-400 text-sm bg-gray-50 rounded-lg">
      暂无价格历史数据
    </div>

    <!-- 简易价格折线图（Canvas） -->
    <canvas v-else ref="canvas" class="w-full" height="140" />

    <!-- 降价提醒 -->
    <div class="mt-4 pt-4 border-t border-gray-100">
      <div v-if="!alertSet" class="flex items-center gap-2">
        <input
          v-model="alertPriceInput"
          type="number"
          placeholder="目标价格（元）"
          class="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:border-blue-400"
        />
        <button
          @click="setAlert"
          :disabled="!alertPriceInput"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-40 transition"
        >
          设置降价提醒
        </button>
      </div>
      <div v-else class="flex items-center justify-between text-sm">
        <span class="text-green-600">✓ 已设置降价提醒：低于 ¥{{ alertPrice }} 通知我</span>
        <button @click="cancelAlert" class="text-gray-400 hover:text-red-500 transition">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'

interface PricePoint {
  price: number
  recordedAt: string
}

const props = defineProps<{
  robotId: number
}>()

const chartData = ref<PricePoint[]>([])
const canvas = ref<HTMLCanvasElement | null>(null)
const alertSet = ref(false)
const alertPrice = ref<number | null>(null)
const alertPriceInput = ref<number | null>(null)

const latestPrice = computed(() => chartData.value.at(-1)?.price ?? null)
const priceChange = computed(() => {
  if (chartData.value.length < 2) return null
  const first = chartData.value[0].price
  const last = chartData.value.at(-1)!.price
  return ((last - first) / first) * 100
})

onMounted(async () => {
  await loadPriceHistory()
  await nextTick()
  drawChart()
})

watch(() => props.robotId, async () => {
  await loadPriceHistory()
  await nextTick()
  drawChart()
})

async function loadPriceHistory() {
  try {
    const res = await fetch(`/api/v1/robots/${props.robotId}/price-history`)
    if (res.ok) {
      const data = await res.json()
      chartData.value = data.map((d: any) => ({ price: Number(d.price), recordedAt: d.recordedAt }))
    }
  } catch {}
}

function drawChart() {
  if (!canvas.value || chartData.value.length === 0) return
  const ctx = canvas.value.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const rect = canvas.value.getBoundingClientRect()
  canvas.value.width = rect.width * dpr
  canvas.value.height = 140 * dpr
  ctx.scale(dpr, dpr)

  const W = rect.width
  const H = 140
  const pad = { top: 10, right: 10, bottom: 24, left: 50 }
  const innerW = W - pad.left - pad.right
  const innerH = H - pad.top - pad.bottom

  const prices = chartData.value.map(d => d.price)
  const minP = Math.min(...prices) * 0.98
  const maxP = Math.max(...prices) * 1.02

  const x = (i: number) => pad.left + (i / (prices.length - 1)) * innerW
  const y = (p: number) => pad.top + innerH - ((p - minP) / (maxP - minP)) * innerH

  // Grid lines
  ctx.strokeStyle = '#f3f4f6'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const yy = pad.top + (i / 4) * innerH
    ctx.beginPath(); ctx.moveTo(pad.left, yy); ctx.lineTo(W - pad.right, yy); ctx.stroke()
    const label = (maxP - (i / 4) * (maxP - minP)).toFixed(0)
    ctx.fillStyle = '#9ca3af'; ctx.font = '10px sans-serif'; ctx.textAlign = 'right'
    ctx.fillText('¥' + label, pad.left - 4, yy + 4)
  }

  // Area fill
  const grad = ctx.createLinearGradient(0, pad.top, 0, pad.top + innerH)
  grad.addColorStop(0, 'rgba(59,130,246,0.15)')
  grad.addColorStop(1, 'rgba(59,130,246,0)')
  ctx.beginPath()
  ctx.moveTo(x(0), y(prices[0]))
  for (let i = 1; i < prices.length; i++) ctx.lineTo(x(i), y(prices[i]))
  ctx.lineTo(x(prices.length - 1), pad.top + innerH)
  ctx.lineTo(x(0), pad.top + innerH)
  ctx.closePath()
  ctx.fillStyle = grad; ctx.fill()

  // Line
  ctx.beginPath(); ctx.strokeStyle = '#3b82f6'; ctx.lineWidth = 2; ctx.lineJoin = 'round'
  ctx.moveTo(x(0), y(prices[0]))
  for (let i = 1; i < prices.length; i++) ctx.lineTo(x(i), y(prices[i]))
  ctx.stroke()

  // Dots on first and last
  for (const idx of [0, prices.length - 1]) {
    ctx.beginPath(); ctx.arc(x(idx), y(prices[idx]), 4, 0, Math.PI * 2)
    ctx.fillStyle = '#3b82f6'; ctx.fill()
    ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(x(idx), y(prices[idx]), 2, 0, Math.PI * 2); ctx.fill()
  }

  // X axis labels
  if (chartData.value.length >= 2) {
    ctx.fillStyle = '#9ca3af'; ctx.font = '10px sans-serif'; ctx.textAlign = 'left'
    const firstDate = new Date(chartData.value[0].recordedAt).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    const lastDate = new Date(chartData.value.at(-1)!.recordedAt).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    ctx.fillText(firstDate, pad.left, H - 4)
    ctx.textAlign = 'right'
    ctx.fillText(lastDate, W - pad.right, H - 4)
  }
}

async function setAlert() {
  if (!alertPriceInput.value) return
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`/api/v1/robots/${props.robotId}/price-alert`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': `Bearer ${token}` } : {}) },
      body: JSON.stringify({ targetPrice: alertPriceInput.value }),
    })
    if (res.ok) {
      alertPrice.value = alertPriceInput.value
      alertSet.value = true
    }
  } catch {}
}

async function cancelAlert() {
  try {
    const token = localStorage.getItem('token')
    await fetch(`/api/v1/robots/${props.robotId}/price-alert`, {
      method: 'DELETE',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
    })
    alertSet.value = false
    alertPrice.value = null
    alertPriceInput.value = null
  } catch {}
}
</script>
