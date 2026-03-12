<template>
  <view class="page">
    <!-- 空状态 -->
    <view v-if="compareStore.items.length === 0" class="empty">
      <text class="empty-icon">🤖</text>
      <text class="empty-text">暂无对比项</text>
      <text class="empty-sub">去机器人库选择要对比的机器人</text>
      <view class="empty-btn" @tap="goList">
        <text class="empty-btn-text">去选择</text>
      </view>
    </view>

    <!-- 对比表格 -->
    <view v-else>
      <!-- 清空按钮 -->
      <view class="toolbar">
        <text class="toolbar-count">已选 {{ compareStore.items.length }} 款机器人</text>
        <text class="toolbar-clear" @tap="clearAll">清空</text>
      </view>

      <scroll-view scroll-x class="table-scroll">
        <view class="table-wrap">
          <!-- 第一列：参数名（固定宽度）+ 右侧各机器人列 -->
          <view class="table-row table-header">
            <view class="param-col">
              <text class="param-col-title">参数</text>
            </view>
            <view
              v-for="robot in robotDetails"
              :key="robot.id"
              class="data-col"
            >
              <image
                class="col-cover"
                :src="robot.coverImageUrl || '/static/logo.png'"
                mode="aspectFill"
              />
              <text class="col-name">{{ robot.name }}</text>
              <text class="col-mfr">{{ robot.manufacturer.name }}</text>
              <view class="col-remove" @tap="removeRobot(robot.id)">
                <text class="col-remove-text">移除</text>
              </view>
            </view>
            <!-- loading placeholder -->
            <view v-if="loading" class="data-col loading-col">
              <text class="loading-text">加载中…</text>
            </view>
          </view>

          <!-- 参数行 -->
          <view
            v-for="row in paramRows"
            :key="row.key"
            class="table-row"
            :class="{ 'row-diff': isDiff(row.key) }"
          >
            <view class="param-col">
              <text class="param-name">{{ row.label }}</text>
            </view>
            <view
              v-for="robot in robotDetails"
              :key="robot.id"
              class="data-col"
            >
              <text class="data-val">{{ getSpec(robot, row.key) }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCompareStore } from '../../stores/compare'
import { robotService } from '../../services/robot.service'
import type { Robot } from '../../types/robot'

const compareStore = useCompareStore()
const robotDetails = ref<Robot[]>([])
const loading = ref(false)

interface ParamRow {
  key: string
  label: string
}

const paramRows: ParamRow[] = [
  { key: 'payloadKg', label: '额定载荷 (kg)' },
  { key: 'reachMm', label: '最大臂展 (mm)' },
  { key: 'dof', label: '自由度 (轴)' },
  { key: 'repeatabilityMm', label: '重复精度 (mm)' },
  { key: 'weightKg', label: '自重 (kg)' },
  { key: 'ipRating', label: 'IP 等级' },
  { key: 'mounting', label: '安装方式' },
  { key: 'maxSpeedDegS', label: '最大速度 (°/s)' },
]

onShow(() => {
  loadDetails()
})

async function loadDetails() {
  if (compareStore.items.length === 0) return
  loading.value = true
  try {
    const ids = compareStore.items.map((r: { id: number }) => r.id)
    const results = await Promise.all(ids.map((id) => robotService.detail(id)))
    robotDetails.value = results
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function getSpec(robot: Robot, key: string): string {
  const val = (robot.specs as Record<string, unknown>)[key]
  if (val === null || val === undefined) return '—'
  return String(val)
}

function isDiff(key: string): boolean {
  if (robotDetails.value.length < 2) return false
  const vals = robotDetails.value.map((r) => getSpec(r, key))
  return vals.some((v) => v !== vals[0])
}

function removeRobot(id: number) {
  compareStore.remove(id)
  robotDetails.value = robotDetails.value.filter((r) => r.id !== id)
}

function clearAll() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空对比列表吗？',
    success: (res) => {
      if (res.confirm) {
        compareStore.clear()
        robotDetails.value = []
      }
    }
  })
}

function goList() {
  uni.switchTab({ url: '/pages/robots/list' })
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 40rpx;
}
.empty-icon {
  font-size: 100rpx;
}
.empty-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-top: 24rpx;
}
.empty-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
  text-align: center;
}
.empty-btn {
  margin-top: 48rpx;
  background: #6366F1;
  border-radius: 48rpx;
  padding: 20rpx 60rpx;
}
.empty-btn-text {
  font-size: 30rpx;
  color: #fff;
}

.toolbar {
  background: #fff;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  margin-bottom: 16rpx;
}
.toolbar-count {
  font-size: 28rpx;
  color: #333;
}
.toolbar-clear {
  font-size: 28rpx;
  color: #ff4444;
}

.table-scroll {
  width: 750rpx;
}
.table-wrap {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: flex;
  flex-direction: row;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  min-height: 88rpx;
  align-items: stretch;
}
.table-header {
  background: #fafafa;
  min-height: 220rpx;
  align-items: flex-start;
}
.row-diff {
  background: #fffbea;
}

.param-col {
  width: 180rpx;
  flex-shrink: 0;
  padding: 20rpx 16rpx;
  border-right: 1rpx solid #f0f0f0;
  display: flex;
  align-items: center;
}
.param-col-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #555;
}
.param-name {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
}

.data-col {
  flex: 1;
  min-width: 200rpx;
  padding: 16rpx;
  border-right: 1rpx solid #f0f0f0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.col-cover {
  width: 160rpx;
  height: 120rpx;
  border-radius: 8rpx;
}
.col-name {
  font-size: 24rpx;
  font-weight: bold;
  color: #1a1a1a;
  margin-top: 10rpx;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.col-mfr {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
  text-align: center;
}
.col-remove {
  margin-top: 10rpx;
  border: 1rpx solid #ff4444;
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}
.col-remove-text {
  font-size: 20rpx;
  color: #ff4444;
}

.data-val {
  font-size: 26rpx;
  color: #333;
  text-align: center;
}

.loading-col {
  justify-content: center;
}
.loading-text {
  font-size: 26rpx;
  color: #999;
}
</style>
