<template>
  <view class="page">
    <!-- 未登录 -->
    <view v-if="!isLoggedIn" class="no-login">
      <text class="no-login-icon">🔒</text>
      <text class="no-login-text">请先登录</text>
      <text class="no-login-sub">登录后查看您收藏的机器人</text>
      <view class="login-btn" @tap="goLogin">
        <text class="login-btn-text">去登录</text>
      </view>
    </view>

    <!-- 已登录 -->
    <view v-else>
      <!-- 加载中 -->
      <view v-if="loading" class="loading-wrap">
        <text class="loading-text">加载中…</text>
      </view>

      <!-- 空收藏 -->
      <view v-else-if="favorites.length === 0" class="empty">
        <text class="empty-icon">💙</text>
        <text class="empty-text">暂无收藏</text>
        <text class="empty-sub">收藏感兴趣的机器人，方便随时查看</text>
        <view class="go-btn" @tap="goList">
          <text class="go-btn-text">去发现机器人</text>
        </view>
      </view>

      <!-- 列表 -->
      <view v-else class="list-wrap">
        <view
          v-for="robot in favorites"
          :key="robot.id"
          @tap="goDetail(robot.id)"
          @longpress="onLongPress(robot.id)"
        >
          <robot-card :robot="robot" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { http } from '../../services/http'
import RobotCard from '../../components/RobotCard.vue'
import type { RobotSummary } from '../../types/robot'

const isLoggedIn = ref(false)
const loading = ref(false)
const favorites = ref<RobotSummary[]>([])

onShow(() => {
  checkLogin()
})

onPullDownRefresh(() => {
  if (isLoggedIn.value) {
    loadFavorites().finally(() => {
      uni.stopPullDownRefresh()
    })
  } else {
    uni.stopPullDownRefresh()
  }
})

function checkLogin() {
  const token = uni.getStorageSync('access_token')
  isLoggedIn.value = !!token
  if (isLoggedIn.value) {
    loadFavorites()
  }
}

async function loadFavorites() {
  loading.value = true
  try {
    const data = await http.get<RobotSummary[]>('/users/me/favorites')
    favorites.value = data || []
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function removeFavorite(robotId: number) {
  try {
    await http.delete(`/users/me/favorites/${robotId}`)
    favorites.value = favorites.value.filter((r) => r.id !== robotId)
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '操作失败', icon: 'none' })
  }
}

function onLongPress(robotId: number) {
  uni.showActionSheet({
    itemList: ['取消收藏'],
    success: (res) => {
      if (res.tapIndex === 0) {
        removeFavorite(robotId)
      }
    }
  })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goList() {
  uni.switchTab({ url: '/pages/robots/list' })
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/robots/detail?id=${id}` })
}
</script>

<style scoped>
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60rpx;
}

.no-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 40rpx;
}
.no-login-icon {
  font-size: 100rpx;
}
.no-login-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-top: 24rpx;
}
.no-login-sub {
  font-size: 26rpx;
  color: #999;
  margin-top: 12rpx;
  text-align: center;
}
.login-btn {
  margin-top: 48rpx;
  background: #6366F1;
  border-radius: 48rpx;
  padding: 20rpx 60rpx;
}
.login-btn-text {
  font-size: 30rpx;
  color: #fff;
}

.loading-wrap {
  padding: 80rpx;
  text-align: center;
}
.loading-text {
  font-size: 28rpx;
  color: #999;
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
.go-btn {
  margin-top: 48rpx;
  background: #6366F1;
  border-radius: 48rpx;
  padding: 20rpx 60rpx;
}
.go-btn-text {
  font-size: 30rpx;
  color: #fff;
}

.list-wrap {
  padding: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
</style>
