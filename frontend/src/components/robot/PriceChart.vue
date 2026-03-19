<template>
  <div class="bg-dark-50 rounded-xl border border-white/[0.04] p-5">
    <div class="flex items-center justify-between mb-4">
      <h3 class="font-semibold text-white">
        {{ t('robot.priceTrendTitle') }}
      </h3>
      <div v-if="latestPrice" class="text-right">
        <div class="text-2xl font-bold text-accent-gold">¥{{ latestPrice.toLocaleString() }}</div>
        <div v-if="priceChange !== null" class="text-sm" :class="priceChange <= 0 ? 'text-primary' : 'text-accent-red'">
          {{ priceChange <= 0 ? '↓' : '↑' }} {{ Math.abs(priceChange).toFixed(1) }}%
        </div>
      </div>
    </div>

    <!-- 无数据 -->
    <div v-if="chartData.length === 0" class="h-40 flex items-center justify-center text-gray-500 text-sm bg-dark-100 rounded-lg border border-white/[0.04]">
      {{ t('robot.priceHistoryEmpty') }}
    </div>

    <!-- 简易价格折线图（Canvas） -->
    <canvas v-else ref="canvas" class="w-full" height="140" />

    <!-- 降价提醒 -->
    <div class="mt-4 pt-4 border-t border-white/[0.04]">
      <div v-if="!alertSet" class="flex items-center gap-2">
        <input
          v-model="alertPriceInput"
          type="number"
          :placeholder="t('robot.priceAlertPlaceholder')"
          class="input-dark flex-1"
        />
        <button
          @click="setAlert"
          :disabled="!alertPriceInput"
          class="px-4 py-2 bg-primary text-gray-900 text-sm font-medium rounded-lg hover:bg-primary-400 disabled:opacity-40 transition-all"
        >
          {{ t('robot.priceAlertButton') }}
        </button>
      </div>
      <div v-else class="flex items-center justify-between text-sm">
        <span class="text-primary">
          {{ t('robot.priceAlertSet', { price: alertPrice }) }}
        </span>
        <button @click="cancelAlert" class="text-gray-500 hover:text-accent-red transition-colors">
          {{ t('robot.priceAlertCancel') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

interface PricePoint {
  price: number
  recordedAt: string
}

const props = defineProps<{
  robotId: number
}>()

const { t, locale } = useI18n()

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
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.04)'
  ctx.lineWidth = 1
  for (let i = 0; i <= 4; i++) {
    const yy = pad.top + (i / 4) * innerH
    ctx.beginPath(); ctx.moveTo(pad.left, yy); ctx.lineTo(W - pad.right, yy); ctx.stroke()
    const label = (maxP - (i / 4) * (maxP - minP)).toFixed(0)
    ctx.fillStyle = '#6b7280'; ctx.font = '10px sans-serif'; ctx.textAlign = 'right'
    ctx.fillText('¥' + label, pad.left - 4, yy + 4)
  }

  // Area fill
  const grad = ctx.createLinearGradient(0, pad.top, 0, pad.top + innerH)
  grad.addColorStop(0, 'rgba(0, 255, 209, 0.15)')
  grad.addColorStop(1, 'rgba(0, 255, 209, 0)')
  ctx.beginPath()
  ctx.moveTo(x(0), y(prices[0]))
  for (let i = 1; i < prices.length; i++) ctx.lineTo(x(i), y(prices[i]))
  ctx.lineTo(x(prices.length - 1), pad.top + innerH)
  ctx.lineTo(x(0), pad.top + innerH)
  ctx.closePath()
  ctx.fillStyle = grad; ctx.fill()

  // Line
  ctx.beginPath(); ctx.strokeStyle = '#00FFD1'; ctx.lineWidth = 2; ctx.lineJoin = 'round'
  ctx.moveTo(x(0), y(prices[0]))
  for (let i = 1; i < prices.length; i++) ctx.lineTo(x(i), y(prices[i]))
  ctx.stroke()

  // Glow on line
  ctx.beginPath(); ctx.strokeStyle = 'rgba(0, 255, 209, 0.3)'; ctx.lineWidth = 6; ctx.lineJoin = 'round'
  ctx.moveTo(x(0), y(prices[0]))
  for (let i = 1; i < prices.length; i++) ctx.lineTo(x(i), y(prices[i]))
  ctx.stroke()

  // Dots on first and last
  for (const idx of [0, prices.length - 1]) {
    ctx.beginPath(); ctx.arc(x(idx), y(prices[idx]), 5, 0, Math.PI * 2)
    ctx.fillStyle = '#00FFD1'; ctx.fill()
    ctx.beginPath(); ctx.arc(x(idx), y(prices[idx]), 2.5, 0, Math.PI * 2)
    ctx.fillStyle = '#1F1F23'; ctx.fill()
  }

  // X axis labels
  if (chartData.value.length >= 2) {
    ctx.fillStyle = '#6b7280'; ctx.font = '10px sans-serif'; ctx.textAlign = 'left'
    const localeCode = (locale.value || 'zh') === 'en' ? 'en-US' as const : 'zh-CN' as const
    const firstDate = new Date(chartData.value[0].recordedAt).toLocaleDateString(localeCode, { month: 'short', day: 'numeric' })
    const lastDate = new Date(chartData.value.at(-1)!.recordedAt).toLocaleDateString(localeCode, { month: 'short', day: 'numeric' })
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
