<template>
  <view v-if="compareStore.count() > 0" class="compare-bar" @tap="goCompare">
    <view class="bar-left">
      <text class="bar-count">已选 {{ compareStore.count() }} 款</text>
      <view class="avatar-row">
        <image
          v-for="robot in compareStore.items"
          :key="robot.id"
          class="avatar"
          :src="robot.coverImageUrl || '/static/logo.png'"
          mode="aspectFill"
        />
      </view>
    </view>
    <view class="bar-btn">
      <text class="bar-btn-text">去对比</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useCompareStore } from '../stores/compare'

const compareStore = useCompareStore()

function goCompare() {
  uni.navigateTo({ url: '/pages/compare/index' })
}
</script>

<style scoped>
.compare-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: 110rpx;
  background: #1a1a1a;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.25);
}
.bar-left {
  display: flex;
  flex-direction: column;
}
.bar-count {
  font-size: 26rpx;
  color: #fff;
  font-weight: bold;
}
.avatar-row {
  display: flex;
  flex-direction: row;
  gap: 8rpx;
  margin-top: 8rpx;
}
.avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 8rpx;
  border: 2rpx solid #fff;
}
.bar-btn {
  background: #6366F1;
  border-radius: 32rpx;
  padding: 16rpx 36rpx;
}
.bar-btn-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
}
</style>
