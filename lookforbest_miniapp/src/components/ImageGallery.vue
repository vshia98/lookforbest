<template>
  <view class="gallery">
    <!-- swiper 主展示 -->
    <swiper
      class="swiper"
      :current="current"
      :indicator-dots="images.length > 1"
      indicator-color="rgba(255,255,255,0.5)"
      indicator-active-color="#ffffff"
      circular
      @change="onSwiperChange"
    >
      <swiper-item v-for="(img, idx) in images.slice(0, maxCount)" :key="idx">
        <image
          class="swiper-img"
          :src="img"
          mode="aspectFit"
          @tap="onTapImage(idx)"
        />
      </swiper-item>
      <swiper-item v-if="images.length === 0">
        <image class="swiper-img" :src="placeholder" mode="aspectFit" />
      </swiper-item>
    </swiper>

    <!-- 计数器 -->
    <view v-if="images.length > 1" class="counter">
      <text class="counter-text">{{ current + 1 }} / {{ Math.min(images.length, maxCount) }}</text>
    </view>

    <!-- 缩略图行 -->
    <scroll-view v-if="showThumbs && images.length > 1" scroll-x class="thumbs-scroll">
      <view class="thumbs-inner">
        <image
          v-for="(img, idx) in images.slice(0, maxCount)"
          :key="idx"
          class="thumb"
          :class="{ 'thumb-active': current === idx }"
          :src="img"
          mode="aspectFill"
          @tap="current = idx"
        />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  images: string[]
  placeholder?: string
  maxCount?: number
  showThumbs?: boolean
}>()

const maxCount = props.maxCount ?? 6
const placeholder = props.placeholder ?? '/static/logo.png'
const showThumbs = props.showThumbs ?? false

const current = ref(0)

function onSwiperChange(e: any) {
  current.value = e.detail.current as number
}

function onTapImage(idx: number) {
  const urls = props.images.slice(0, maxCount)
  // #ifdef H5
  uni.previewImage({ urls, current: urls[idx] })
  // #endif
  // #ifndef H5
  uni.previewImage({ urls, current: urls[idx] })
  // #endif
}
</script>

<style scoped>
.gallery {
  position: relative;
}

.swiper {
  width: 100%;
  height: 500rpx;
  background: #000;
}
.swiper-img {
  width: 100%;
  height: 500rpx;
  display: block;
}

.counter {
  position: absolute;
  top: 20rpx;
  right: 24rpx;
  background: rgba(0, 0, 0, 0.45);
  border-radius: 24rpx;
  padding: 6rpx 20rpx;
}
.counter-text {
  color: #fff;
  font-size: 24rpx;
}

.thumbs-scroll {
  background: #1a1a1a;
  white-space: nowrap;
}
.thumbs-inner {
  display: flex;
  flex-direction: row;
  padding: 12rpx 16rpx;
  gap: 12rpx;
}
.thumb {
  width: 100rpx;
  height: 80rpx;
  border-radius: 8rpx;
  opacity: 0.6;
  border: 2rpx solid transparent;
  flex-shrink: 0;
}
.thumb-active {
  opacity: 1;
  border-color: #6366F1;
}
</style>
