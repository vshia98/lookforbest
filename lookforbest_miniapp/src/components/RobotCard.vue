<template>
  <view class="card" @tap="$emit('tap', robot)">
    <view class="cover-wrap">
      <image
        class="cover"
        :src="robot.coverImageUrl || '/static/logo.png'"
        mode="aspectFill"
      />
      <view v-if="robot.has3dModel" class="tag-3d">
        <text class="tag-3d-text">3D</text>
      </view>
    </view>
    <view class="info">
      <text class="name">{{ robot.name }}</text>
      <text class="mfr">{{ robot.manufacturer.name }}</text>
      <view class="chips">
        <text v-if="robot.payloadKg" class="chip">载荷 {{ robot.payloadKg }}kg</text>
        <text v-if="robot.reachMm" class="chip">臂展 {{ robot.reachMm }}mm</text>
        <text v-if="robot.dof" class="chip">{{ robot.dof }}轴</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { RobotSummary } from '../types/robot'

const props = defineProps<{
  robot: RobotSummary
  showCompare?: boolean
}>()

defineEmits<{
  (e: 'tap', robot: RobotSummary): void
}>()
</script>

<style scoped>
.card {
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.cover-wrap {
  position: relative;
  flex-shrink: 0;
}
.cover {
  width: 200rpx;
  height: 160rpx;
  display: block;
}
.tag-3d {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  background: #3B82F6;
  border-radius: 8rpx;
  padding: 2rpx 10rpx;
}
.tag-3d-text {
  color: #fff;
  font-size: 18rpx;
  font-weight: bold;
}

.info {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.name {
  font-size: 28rpx;
  font-weight: bold;
  color: #1a1a1a;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.mfr {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}
.chips {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}
.chip {
  background: #f0f0ff;
  color: #6366F1;
  border-radius: 12rpx;
  padding: 4rpx 14rpx;
  font-size: 20rpx;
}
</style>
