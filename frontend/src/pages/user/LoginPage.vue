<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <router-link to="/" class="text-2xl font-bold text-gray-900 inline-flex items-center gap-2">
          <span class="text-primary-500">⚡</span> LookForBest
        </router-link>
        <p class="text-gray-500 mt-2">登录您的账户</p>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <form @submit.prevent="handleLogin" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">用户名或邮箱</label>
            <input
              v-model="form.usernameOrEmail"
              type="text"
              required
              placeholder="请输入用户名或邮箱"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
            <input
              v-model="form.password"
              type="password"
              required
              placeholder="请输入密码"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>

          <div v-if="error" class="bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">
            {{ error }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-primary-500 text-white py-2.5 rounded-xl font-medium text-sm hover:bg-primary-600 transition-colors disabled:opacity-60"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- 分割线 -->
        <div class="flex items-center gap-3 my-6">
          <div class="flex-1 h-px bg-gray-100" />
          <span class="text-xs text-gray-400">或使用社交账号登录</span>
          <div class="flex-1 h-px bg-gray-100" />
        </div>

        <!-- 社交登录按钮 -->
        <div class="space-y-3">
          <!-- Google 登录 -->
          <button
            type="button"
            :disabled="oauthLoading"
            @click="handleGoogleLogin"
            class="w-full flex items-center justify-center gap-3 px-4 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-60"
          >
            <svg class="w-5 h-5" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            使用 Google 登录
          </button>

          <!-- 微信扫码登录 -->
          <button
            type="button"
            :disabled="oauthLoading"
            @click="handleWechatLogin"
            class="w-full flex items-center justify-center gap-3 px-4 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors disabled:opacity-60"
          >
            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="#07C160">
              <path d="M8.69 11.52c-.57 0-1.03-.46-1.03-1.03s.46-1.03 1.03-1.03 1.03.46 1.03 1.03-.46 1.03-1.03 1.03zm4.62 0c-.57 0-1.03-.46-1.03-1.03s.46-1.03 1.03-1.03 1.03.46 1.03 1.03-.46 1.03-1.03 1.03zM11 2C5.48 2 1 6.48 1 12s4.48 10 10 10 10-4.48 10-10S16.52 2 11 2zm.5 14.5c-3.59 0-6.5-2.5-6.5-5.5S7.91 5.5 11.5 5.5 18 8 18 11s-2.91 5.5-6.5 5.5zm5.26 1.35c-.28-.57-.88-.93-1.53-.93-.39 0-.76.12-1.08.34l-.32-.67c.46-.27.99-.43 1.55-.43.97 0 1.86.5 2.38 1.29l-.99.4zM15.9 14.1c-.38 0-.69-.31-.69-.69s.31-.69.69-.69.69.31.69.69-.31.69-.69.69zm2.95.07c-.38 0-.69-.31-.69-.69s.31-.69.69-.69.69.31.69.69-.31.69-.69.69z"/>
            </svg>
            微信扫码登录
          </button>
        </div>

        <!-- 微信 QR 弹窗 -->
        <div v-if="wechatQrUrl" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="wechatQrUrl = ''">
          <div class="bg-white rounded-2xl p-8 text-center shadow-xl max-w-xs w-full">
            <p class="text-sm font-medium text-gray-700 mb-4">请使用微信扫码登录</p>
            <img :src="wechatQrUrl" alt="微信二维码" class="w-48 h-48 mx-auto" />
            <button @click="wechatQrUrl = ''" class="mt-4 text-xs text-gray-400 hover:text-gray-600">取消</button>
          </div>
        </div>

        <p class="text-center text-sm text-gray-500 mt-6">
          还没有账户？
          <router-link to="/register" class="text-primary-500 font-medium hover:underline">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare const google: any

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = reactive({ usernameOrEmail: '', password: '' })
const error = ref('')
const loading = ref(false)
const oauthLoading = ref(false)
const wechatQrUrl = ref('')

const redirectTo = () => router.push((route.query.redirect as string) || '/')

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(form.usernameOrEmail, form.password)
    redirectTo()
  } catch (e: any) {
    error.value = e?.message || '用户名或密码错误'
  } finally {
    loading.value = false
  }
}

// ─── Google 登录 ─────────────────────────────────────────────────────────────
function handleGoogleLogin() {
  oauthLoading.value = true
  // 使用 Google Identity Services (GIS) 弹窗模式
  if (typeof google === 'undefined') {
    loadGoogleScript().then(initGoogleSignIn)
  } else {
    initGoogleSignIn()
  }
}

function loadGoogleScript(): Promise<void> {
  return new Promise((resolve) => {
    if (document.getElementById('google-gsi-script')) { resolve(); return }
    const script = document.createElement('script')
    script.id = 'google-gsi-script'
    script.src = 'https://accounts.google.com/gsi/client'
    script.onload = () => resolve()
    document.head.appendChild(script)
  })
}

function initGoogleSignIn() {
  google.accounts.id.initialize({
    // 通过环境变量注入 Google Client ID
    client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID || '',
    callback: async (response: { credential: string }) => {
      try {
        await authStore.loginWithOAuth('google', response.credential)
        redirectTo()
      } catch (e: any) {
        error.value = e?.message || 'Google 登录失败'
      } finally {
        oauthLoading.value = false
      }
    },
  })
  google.accounts.id.prompt()
}

// ─── 微信扫码登录 ─────────────────────────────────────────────────────────────
function handleWechatLogin() {
  // 跳转到微信 OAuth 授权页（redirect_uri 需在微信开放平台配置）
  const appId = import.meta.env.VITE_WECHAT_WEB_APP_ID
  if (!appId) {
    error.value = '微信登录暂未配置'
    return
  }
  const redirectUri = encodeURIComponent(`${window.location.origin}/auth/wechat/callback`)
  const scope = 'snsapi_login'
  const state = Math.random().toString(36).slice(2)
  sessionStorage.setItem('wechat_oauth_state', state)
  window.location.href =
    `https://open.weixin.qq.com/connect/qrconnect?appid=${appId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}&state=${state}#wechat_redirect`
}
</script>
