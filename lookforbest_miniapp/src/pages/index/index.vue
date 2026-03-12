<template>
  <view class="page">
    <!-- 搜索框 -->
    <view class="search-bar" @tap="goSearch">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索机器人、品牌…</text>
      </view>
    </view>

    <!-- 对比悬浮按钮 -->
    <view
      v-if="compareStore.count() > 0"
      class="compare-float"
      @tap="goCompare"
    >
      <text class="compare-float-text">已选 {{ compareStore.count() }} 款</text>
      <view class="compare-badge">
        <text class="compare-badge-text">去对比</text>
      </view>
    </view>

    <!-- 热门分类 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">热门分类</text>
      </view>
      <view class="categories-grid">
        <view
          v-for="cat in categories.slice(0, 8)"
          :key="cat.id"
          class="category-tag"
          @tap="goCategoryList(cat)"
        >
          <text class="category-tag-text">{{ cat.name }}</text>
        </view>
      </view>
    </view>

    <!-- 精选机器人 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">精选机器人</text>
        <text class="section-more" @tap="goRobotList">查看全部 ›</text>
      </view>
      <scroll-view scroll-x class="h-scroll">
        <view class="h-scroll-inner">
          <view
            v-for="robot in robotStore.robots.slice(0, 6)"
            :key="robot.id"
            class="featured-card"
            @tap="goDetail(robot.id)"
          >
            <image
              class="featured-cover"
              :src="robot.coverImageUrl || '/static/logo.png'"
              mode="aspectFill"
            />
            <view v-if="robot.has3dModel" class="tag-3d">
              <text class="tag-3d-text">3D</text>
            </view>
            <view class="featured-info">
              <text class="featured-name">{{ robot.name }}</text>
              <text class="featured-mfr">{{ robot.manufacturer.name }}</text>
              <view class="featured-chips">
                <text v-if="robot.payloadKg" class="chip">{{ robot.payloadKg }}kg</text>
                <text v-if="robot.reachMm" class="chip">{{ robot.reachMm }}mm</text>
                <text v-if="robot.dof" class="chip">{{ robot.dof }}轴</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
      <view v-if="robotStore.loading && robotStore.robots.length === 0" class="loading-wrap">
        <text class="loading-text">加载中…</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useRobotStore } from '../../stores/robot'
import { useCompareStore } from '../../stores/compare'
import { robotService } from '../../services/robot.service'

const robotStore = useRobotStore()
const compareStore = useCompareStore()

interface Category {
  id: number
  name: string
  slug?: string
}
const categories = ref<Category[]>([])

async function loadCategories() {
  try {
    const data = await robotService.categories() as Category[]
    categories.value = data || []
  } catch (_) {
    // silent
  }
}

onLoad(() => {
  robotStore.fetchRobots(true)
  loadCategories()
})

onShow(() => {
  // 响应式自动更新对比数量
})

function goSearch() {
  uni.navigateTo({ url: '/pages/robots/list' })
}

function goRobotList() {
  uni.switchTab({ url: '/pages/robots/list' })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/robots/detail?id=${id}` })
}

function goCategoryList(cat: Category) {
  uni.navigateTo({
    url: `/pages/robots/list?categoryId=${cat.id}&categoryName=${encodeURIComponent(cat.name)}`
  })
}

function goCompare() {
  uni.navigateTo({ url: '/pages/compare/index' })
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.search-bar {
  background: #6366F1;
  padding: 20rpx 30rpx 28rpx;
}
.search-input-wrap {
  background: #ffffff;
  border-radius: 40rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16rpx 28rpx;
}
.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  color: #999;
}
.search-placeholder {
  font-size: 28rpx;
  color: #bbb;
}

.compare-float {
  position: fixed;
  right: 24rpx;
  bottom: 160rpx;
  background: #6366F1;
  border-radius: 48rpx;
  padding: 16rpx 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 99;
  box-shadow: 0 4rpx 16rpx rgba(99, 102, 241, 0.4);
}
.compare-float-text {
  color: #fff;
  font-size: 22rpx;
}
.compare-badge {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 4rpx 12rpx;
  margin-top: 6rpx;
}
.compare-badge-text {
  color: #6366F1;
  font-size: 22rpx;
  font-weight: bold;
}

.section {
  background: #ffffff;
  margin: 16rpx 0;
  padding: 24rpx 0;
}
.section-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx 20rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}
.section-more {
  font-size: 26rpx;
  color: #6366F1;
}

.categories-grid {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  padding: 0 20rpx;
  gap: 16rpx;
}
.category-tag {
  background: #f0f0ff;
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
}
.category-tag-text {
  font-size: 26rpx;
  color: #6366F1;
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
.featured-card {
  display: inline-flex;
  flex-direction: column;
  width: 240rpx;
  background: #f8f8f8;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
}
.featured-cover {
  width: 240rpx;
  height: 180rpx;
  display: block;
}
.tag-3d {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background: #3B82F6;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
}
.tag-3d-text {
  color: #fff;
  font-size: 18rpx;
  font-weight: bold;
}
.featured-info {
  padding: 12rpx 16rpx;
}
.featured-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}
.featured-mfr {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
  display: block;
}
.featured-chips {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 10rpx;
}
.chip {
  background: #efefef;
  border-radius: 12rpx;
  padding: 4rpx 12rpx;
  font-size: 20rpx;
  color: #555;
}

.loading-wrap {
  padding: 40rpx;
  text-align: center;
}
.loading-text {
  font-size: 26rpx;
  color: #999;
}
</style>
