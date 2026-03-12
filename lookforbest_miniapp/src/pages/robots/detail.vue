<template>
  <view class="page" v-if="robot">
    <!-- 图片轮播 -->
    <image-gallery
      :images="robot.images.length > 0 ? robot.images.map((i: any) => i.url) : [robot.coverImageUrl || '/static/logo.png']"
      :placeholder="robot.coverImageUrl || '/static/logo.png'"
      :max-count="6"
    />

    <!-- 基本信息 -->
    <view class="info-card">
      <view class="name-row">
        <text class="robot-name">{{ robot.name }}</text>
        <view class="fav-btn" @tap="toggleFavorite">
          <text class="fav-icon">{{ isFavorited ? '❤️' : '🤍' }}</text>
        </view>
      </view>
      <text v-if="robot.nameEn" class="name-en">{{ robot.nameEn }}</text>
      <view class="meta-row">
        <text class="meta-item">{{ robot.manufacturer.name }}</text>
        <text class="meta-sep">·</text>
        <text class="meta-item">{{ robot.category.name }}</text>
        <text v-if="robot.releaseYear" class="meta-sep">·</text>
        <text v-if="robot.releaseYear" class="meta-item">{{ robot.releaseYear }}年</text>
      </view>

      <!-- 核心参数速览 -->
      <view class="params-row">
        <view v-if="robot.specs.payloadKg" class="param-item">
          <text class="param-value">{{ robot.specs.payloadKg }}kg</text>
          <text class="param-label">额定载荷</text>
        </view>
        <view v-if="robot.specs.reachMm" class="param-item">
          <text class="param-value">{{ robot.specs.reachMm }}mm</text>
          <text class="param-label">最大臂展</text>
        </view>
        <view v-if="robot.specs.dof" class="param-item">
          <text class="param-value">{{ robot.specs.dof }}</text>
          <text class="param-label">自由度</text>
        </view>
      </view>

      <!-- 对比按钮 -->
      <view
        class="compare-btn"
        :class="{ 'compare-btn-active': isInCompare }"
        @tap="toggleCompare"
      >
        <text class="compare-btn-text">{{ isInCompare ? '✓ 已加入对比' : '+ 加入对比' }}</text>
      </view>
    </view>

    <!-- 完整技术参数 -->
    <view class="specs-card">
      <view class="card-header" @tap="specsExpanded = !specsExpanded">
        <text class="card-title">技术参数</text>
        <text class="card-arrow">{{ specsExpanded ? '▲' : '▼' }}</text>
      </view>
      <view v-if="specsExpanded" class="specs-table">
        <view v-if="robot.specs.payloadKg" class="spec-row">
          <text class="spec-key">额定载荷</text>
          <text class="spec-val">{{ robot.specs.payloadKg }} kg</text>
        </view>
        <view v-if="robot.specs.reachMm" class="spec-row">
          <text class="spec-key">最大臂展</text>
          <text class="spec-val">{{ robot.specs.reachMm }} mm</text>
        </view>
        <view v-if="robot.specs.dof" class="spec-row">
          <text class="spec-key">自由度</text>
          <text class="spec-val">{{ robot.specs.dof }} 轴</text>
        </view>
        <view v-if="robot.specs.repeatabilityMm" class="spec-row">
          <text class="spec-key">重复定位精度</text>
          <text class="spec-val">±{{ robot.specs.repeatabilityMm }} mm</text>
        </view>
        <view v-if="robot.specs.maxSpeedDegS" class="spec-row">
          <text class="spec-key">最大速度</text>
          <text class="spec-val">{{ robot.specs.maxSpeedDegS }} °/s</text>
        </view>
        <view v-if="robot.specs.weightKg" class="spec-row">
          <text class="spec-key">机器人自重</text>
          <text class="spec-val">{{ robot.specs.weightKg }} kg</text>
        </view>
        <view v-if="robot.specs.ipRating" class="spec-row">
          <text class="spec-key">IP 等级</text>
          <text class="spec-val">{{ robot.specs.ipRating }}</text>
        </view>
        <view v-if="robot.specs.mounting" class="spec-row">
          <text class="spec-key">安装方式</text>
          <text class="spec-val">{{ robot.specs.mounting }}</text>
        </view>
      </view>
    </view>

    <!-- 描述 -->
    <view v-if="robot.description" class="desc-card">
      <text class="card-title">产品介绍</text>
      <text class="desc-text" :class="{ 'desc-clamp': !descExpanded }">
        {{ robot.description }}
      </text>
      <text class="desc-toggle" @tap="descExpanded = !descExpanded">
        {{ descExpanded ? '收起' : '展开全文' }}
      </text>
    </view>

    <!-- 相关推荐 -->
    <view v-if="similarRobots.length > 0" class="similar-section">
      <text class="card-title similar-title">相关推荐</text>
      <scroll-view scroll-x class="h-scroll">
        <view class="h-scroll-inner">
          <view
            v-for="r in similarRobots"
            :key="r.id"
            class="similar-card"
            @tap="goDetail(r.id)"
          >
            <image
              class="similar-cover"
              :src="r.coverImageUrl || '/static/logo.png'"
              mode="aspectFill"
            />
            <view class="similar-info">
              <text class="similar-name">{{ r.name }}</text>
              <text class="similar-mfr">{{ r.manufacturer.name }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>

  <!-- 加载中 -->
  <view v-else class="loading-page">
    <text class="loading-text">加载中…</text>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { robotService } from '../../services/robot.service'
import { useCompareStore } from '../../stores/compare'
import { http } from '../../services/http'
import type { Robot, RobotSummary } from '../../types/robot'

const compareStore = useCompareStore()
const robot = ref<Robot | null>(null)
const similarRobots = ref<RobotSummary[]>([])
const specsExpanded = ref(true)
const descExpanded = ref(false)
const isFavorited = ref(false)

const isInCompare = computed(() => {
  if (!robot.value) return false
  return compareStore.hasRobot(robot.value.id)
})

onLoad(async (options?: AnyObject) => {
  const id = Number(options?.id)
  if (!id) return
  try {
    const data = await robotService.detail(id)
    robot.value = data
    isFavorited.value = data.isFavorited
    loadSimilar(id)
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  }
})

async function loadSimilar(id: number) {
  try {
    const data = await robotService.similar(id)
    similarRobots.value = data || []
  } catch (_) {
    // silent
  }
}

async function toggleFavorite() {
  const token = uni.getStorageSync('access_token')
  if (!token) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  try {
    if (isFavorited.value) {
      await http.delete(`/users/me/favorites/${robot.value!.id}`)
      isFavorited.value = false
      uni.showToast({ title: '已取消收藏', icon: 'none' })
    } else {
      await http.post('/users/me/favorites', { robotId: robot.value!.id }, false)
      isFavorited.value = true
      uni.showToast({ title: '已收藏', icon: 'success' })
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

function toggleCompare() {
  if (!robot.value) return
  if (isInCompare.value) {
    compareStore.remove(robot.value.id)
  } else {
    compareStore.add(robot.value as unknown as RobotSummary)
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/robots/detail?id=${id}` })
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  padding-bottom: 60rpx;
}

.swiper {
  width: 750rpx;
  height: 460rpx;
  background: #111;
}
.swiper-img {
  width: 750rpx;
  height: 460rpx;
}

.info-card {
  background: #fff;
  padding: 28rpx 30rpx 20rpx;
  margin-bottom: 16rpx;
}
.name-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: flex-start;
}
.robot-name {
  font-size: 38rpx;
  font-weight: bold;
  color: #1a1a1a;
  flex: 1;
  line-height: 1.3;
}
.fav-btn {
  padding: 8rpx 12rpx;
}
.fav-icon {
  font-size: 40rpx;
}
.name-en {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
  display: block;
}
.meta-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 12rpx;
  flex-wrap: wrap;
}
.meta-item {
  font-size: 26rpx;
  color: #666;
}
.meta-sep {
  font-size: 24rpx;
  color: #ccc;
  margin: 0 10rpx;
}

.params-row {
  display: flex;
  flex-direction: row;
  margin-top: 24rpx;
  background: #f8f8f8;
  border-radius: 16rpx;
  padding: 20rpx 0;
}
.param-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.param-value {
  font-size: 34rpx;
  font-weight: bold;
  color: #6366F1;
}
.param-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

.compare-btn {
  margin-top: 20rpx;
  border: 2rpx solid #6366F1;
  border-radius: 40rpx;
  padding: 16rpx;
  text-align: center;
}
.compare-btn-active {
  background: #6366F1;
}
.compare-btn-text {
  font-size: 28rpx;
  color: #6366F1;
}
.compare-btn-active .compare-btn-text {
  color: #fff;
}

.specs-card {
  background: #fff;
  margin-bottom: 16rpx;
}
.card-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a1a;
}
.card-arrow {
  font-size: 24rpx;
  color: #999;
}
.specs-table {
  padding: 0 30rpx 20rpx;
}
.spec-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.spec-key {
  font-size: 26rpx;
  color: #666;
}
.spec-val {
  font-size: 26rpx;
  color: #1a1a1a;
  font-weight: 500;
}

.desc-card {
  background: #fff;
  padding: 24rpx 30rpx;
  margin-bottom: 16rpx;
}
.desc-text {
  font-size: 26rpx;
  color: #444;
  line-height: 1.7;
  margin-top: 16rpx;
  display: block;
}
.desc-clamp {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}
.desc-toggle {
  font-size: 26rpx;
  color: #6366F1;
  margin-top: 12rpx;
  display: block;
}

.similar-section {
  background: #fff;
  padding: 24rpx 0;
}
.similar-title {
  padding: 0 30rpx 16rpx;
  display: block;
}
.h-scroll {
  width: 100%;
  white-space: nowrap;
}
.h-scroll-inner {
  display: flex;
  flex-direction: row;
  padding: 0 20rpx;
  gap: 20rpx;
}
.similar-card {
  display: inline-flex;
  flex-direction: column;
  width: 200rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  overflow: hidden;
  flex-shrink: 0;
}
.similar-cover {
  width: 200rpx;
  height: 150rpx;
}
.similar-info {
  padding: 10rpx 12rpx;
}
.similar-name {
  font-size: 24rpx;
  font-weight: bold;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.similar-mfr {
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.loading-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
.loading-text {
  font-size: 28rpx;
  color: #999;
}
</style>
