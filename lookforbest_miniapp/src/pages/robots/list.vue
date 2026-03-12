<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-wrap">
      <input
        class="search-input"
        :value="searchKeyword"
        placeholder="搜索机器人、品牌…"
        placeholder-class="search-ph"
        @input="onSearchInput"
        @confirm="onSearchConfirm"
      />
      <text v-if="searchKeyword" class="search-clear" @tap="clearSearch">✕</text>
      <text class="filter-icon" @tap="showFilter = true">筛选</text>
    </view>

    <!-- 筛选面板 -->
    <filter-panel
      v-model:visible="showFilter"
      :categories="categories"
      :model-value="activeFilter"
      @confirm="onFilterConfirm"
    />

    <!-- 分类标签栏 -->
    <scroll-view scroll-x class="cat-scroll">
      <view class="cat-inner">
        <view
          class="cat-item"
          :class="{ active: activeCatId === 0 }"
          @tap="selectCategory(0, '')"
        >
          <text class="cat-text">全部</text>
        </view>
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="cat-item"
          :class="{ active: activeCatId === cat.id }"
          @tap="selectCategory(cat.id, cat.name)"
        >
          <text class="cat-text">{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view class="list-wrap">
      <view
        v-for="robot in robotStore.robots"
        :key="robot.id"
        @tap="goDetail(robot.id)"
      >
        <robot-card :robot="robot" />
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="robotStore.loading" class="loading-wrap">
      <text class="loading-text">加载中…</text>
    </view>

    <!-- 没有更多 -->
    <view v-if="!robotStore.hasMore && robotStore.robots.length > 0" class="no-more">
      <text class="no-more-text">— 已全部加载 —</text>
    </view>

    <!-- 空状态 -->
    <view v-if="!robotStore.loading && robotStore.robots.length === 0" class="empty">
      <text class="empty-text">未找到匹配的机器人</text>
      <text class="empty-sub">试试其他关键词或分类</text>
    </view>

    <!-- CompareBar -->
    <compare-bar />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad, onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { useRobotStore } from '../../stores/robot'
import { robotService } from '../../services/robot.service'
import RobotCard from '../../components/RobotCard.vue'
import CompareBar from '../../components/CompareBar.vue'
import FilterPanel from '../../components/FilterPanel.vue'

const robotStore = useRobotStore()

interface Category {
  id: number
  name: string
  slug?: string
}
const categories = ref<Category[]>([])
const activeCatId = ref(0)
const searchKeyword = ref('')
const showFilter = ref(false)
const activeFilter = ref<{ payloadMin?: number; payloadMax?: number; dof?: number; categoryId?: number }>({})
let debounceTimer: ReturnType<typeof setTimeout> | null = null

onLoad((options?: AnyObject) => {
  if (options?.q) {
    searchKeyword.value = options.q
    robotStore.updateFilter({ q: options.q })
  } else if (options?.categoryId) {
    const catId = Number(options.categoryId)
    activeCatId.value = catId
    robotStore.updateFilter({ categoryId: catId })
  } else {
    robotStore.fetchRobots(true)
  }
  loadCategories()
})

onPullDownRefresh(() => {
  robotStore.fetchRobots(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})

onReachBottom(() => {
  if (robotStore.hasMore && !robotStore.loading) {
    robotStore.fetchRobots()
  }
})

async function loadCategories() {
  try {
    const data = await robotService.categories() as Category[]
    categories.value = data || []
  } catch (_) {
    // silent
  }
}

function onSearchInput(e: any) {
  const val = e.detail.value as string
  searchKeyword.value = val
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    robotStore.updateFilter({ q: val || undefined, page: 0 })
  }, 300)
}

function onSearchConfirm() {
  if (debounceTimer) clearTimeout(debounceTimer)
  robotStore.updateFilter({ q: searchKeyword.value || undefined, page: 0 })
}

function clearSearch() {
  searchKeyword.value = ''
  robotStore.updateFilter({ q: undefined, page: 0 })
}

function selectCategory(id: number, _name: string) {
  activeCatId.value = id
  robotStore.updateFilter({ categoryId: id || undefined, page: 0 })
}

function onFilterConfirm(filter: { payloadMin?: number; payloadMax?: number; dof?: number; categoryId?: number }) {
  activeFilter.value = filter
  robotStore.updateFilter({
    payloadMin: filter.payloadMin,
    payloadMax: filter.payloadMax,
    dof: filter.dof,
    categoryId: filter.categoryId || activeCatId.value || undefined,
    page: 0
  })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/robots/detail?id=${id}` })
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 180rpx;
}

.search-wrap {
  background: #fff;
  padding: 16rpx 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
}
.search-input {
  flex: 1;
  background: #f5f5f5;
  border-radius: 32rpx;
  padding: 14rpx 28rpx;
  font-size: 28rpx;
  color: #333;
}
.search-ph {
  color: #bbb;
  font-size: 28rpx;
}
.search-clear {
  font-size: 32rpx;
  color: #999;
  padding: 0 12rpx;
}
.filter-icon {
  font-size: 26rpx;
  color: #6366F1;
  padding: 0 8rpx;
  white-space: nowrap;
}

.cat-scroll {
  background: #fff;
  white-space: nowrap;
  border-bottom: 1rpx solid #f0f0f0;
}
.cat-inner {
  display: flex;
  flex-direction: row;
  padding: 0 16rpx;
}
.cat-item {
  padding: 20rpx 28rpx;
  display: inline-flex;
  align-items: center;
  border-bottom: 4rpx solid transparent;
}
.cat-item.active {
  border-bottom-color: #6366F1;
}
.cat-text {
  font-size: 26rpx;
  color: #666;
  white-space: nowrap;
}
.cat-item.active .cat-text {
  color: #6366F1;
  font-weight: bold;
}

.list-wrap {
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.loading-wrap {
  padding: 40rpx;
  text-align: center;
}
.loading-text {
  font-size: 26rpx;
  color: #999;
}

.no-more {
  padding: 32rpx;
  text-align: center;
}
.no-more-text {
  font-size: 24rpx;
  color: #ccc;
}

.empty {
  padding: 120rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-text {
  font-size: 32rpx;
  color: #555;
  font-weight: bold;
}
.empty-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 16rpx;
}
</style>
