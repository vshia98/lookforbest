<template>
  <view class="page">
    <view class="logo-wrap">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="brand">LookForBest</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="field-label">邮箱</text>
        <input
          class="field-input"
          type="text"
          placeholder="请输入邮箱"
          placeholder-class="field-ph"
          :value="email"
          @input="(e: any) => email = e.detail.value"
        />
      </view>

      <view class="field">
        <text class="field-label">密码</text>
        <input
          class="field-input"
          type="safe-password"
          placeholder="请输入密码"
          placeholder-class="field-ph"
          :value="password"
          @input="(e: any) => password = e.detail.value"
          password
        />
      </view>

      <view
        class="login-btn"
        :class="{ 'btn-loading': loading }"
        @tap="doLogin"
      >
        <text class="login-btn-text">{{ loading ? '登录中…' : '登录' }}</text>
      </view>

      <!-- 分割线 -->
      <view class="divider-row">
        <view class="divider-line" />
        <text class="divider-text">或</text>
        <view class="divider-line" />
      </view>

      <!-- 微信一键登录 -->
      <button
        class="wechat-btn"
        :disabled="wxLoading"
        open-type="getPhoneNumber"
        @tap="doWechatLogin"
      >
        <image class="wechat-icon" src="/static/icons/wechat.png" mode="aspectFit" />
        <text class="wechat-btn-text">{{ wxLoading ? '登录中…' : '微信一键登录' }}</text>
      </button>

      <view class="register-row" @tap="goRegister">
        <text class="register-text">没有账号？去注册</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { http } from '../../services/http'

const email = ref('')
const password = ref('')
const loading = ref(false)
const wxLoading = ref(false)

async function doLogin() {
  if (!email.value.trim()) {
    uni.showToast({ title: '请输入邮箱', icon: 'none' })
    return
  }
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const data = await http.post<{ accessToken: string; refreshToken: string }>(
      '/auth/login',
      { email: email.value.trim(), password: password.value }
    )
    if (data?.accessToken) {
      http.saveTokens(data.accessToken, data.refreshToken || '')
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '登录失败，请检查账号密码', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function doWechatLogin() {
  wxLoading.value = true
  try {
    // 调用 wx.login 获取 code
    const loginRes = await new Promise<UniApp.LoginRes>((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: resolve,
        fail: reject,
      })
    })
    const code = loginRes.code
    if (!code) throw new Error('微信授权失败，code 为空')

    // 发送给后端换取 JWT
    const data = await http.post<{ accessToken: string; refreshToken: string }>(
      '/auth/oauth/login',
      { provider: 'wechat_miniapp', code }
    )
    if (data?.accessToken) {
      http.saveTokens(data.accessToken, data.refreshToken || '')
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    }
  } catch (e: any) {
    uni.showToast({ title: e.message || '微信登录失败，请重试', icon: 'none' })
  } finally {
    wxLoading.value = false
  }
}

function goRegister() {
  uni.showToast({ title: '注册功能开发中', icon: 'none' })
}
</script>

<style scoped>
.page {
  background: #ffffff;
  min-height: 100vh;
  padding: 0 40rpx;
}

.logo-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0 60rpx;
}
.logo {
  width: 140rpx;
  height: 140rpx;
}
.brand {
  font-size: 42rpx;
  font-weight: bold;
  color: #6366F1;
  margin-top: 20rpx;
}

.form {
  margin-top: 20rpx;
}

.field {
  margin-bottom: 32rpx;
}
.field-label {
  font-size: 26rpx;
  color: #555;
  margin-bottom: 12rpx;
  display: block;
}
.field-input {
  width: 100%;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
  font-size: 30rpx;
  color: #333;
  box-sizing: border-box;
}
.field-ph {
  color: #bbb;
  font-size: 28rpx;
}

.login-btn {
  background: #6366F1;
  border-radius: 48rpx;
  padding: 28rpx;
  text-align: center;
  margin-top: 48rpx;
}
.btn-loading {
  opacity: 0.7;
}
.login-btn-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.divider-row {
  display: flex;
  align-items: center;
  margin: 40rpx 0 32rpx;
}
.divider-line {
  flex: 1;
  height: 1rpx;
  background: #e5e5e5;
}
.divider-text {
  font-size: 24rpx;
  color: #bbb;
  padding: 0 20rpx;
}

.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #07C160;
  border-radius: 48rpx;
  padding: 24rpx;
  border: none;
}
.wechat-btn[disabled] {
  opacity: 0.7;
}
.wechat-icon {
  width: 44rpx;
  height: 44rpx;
  margin-right: 16rpx;
}
.wechat-btn-text {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.register-row {
  margin-top: 32rpx;
  text-align: center;
  padding: 16rpx;
}
.register-text {
  font-size: 26rpx;
  color: #6366F1;
}
</style>
