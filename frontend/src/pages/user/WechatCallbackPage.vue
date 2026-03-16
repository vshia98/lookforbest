<template>
  <div class="min-h-screen flex items-center justify-center bg-dark">
    <div class="text-center">
      <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
      <div v-else class="text-gray-500 text-sm">正在处理微信登录…</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const error = ref('')

onMounted(async () => {
  const params = new URLSearchParams(window.location.search)
  const code  = params.get('code')
  const state = params.get('state')
  const savedState = sessionStorage.getItem('wechat_oauth_state')

  if (!code) {
    error.value = '微信授权失败，缺少 code 参数'
    return
  }
  if (state !== savedState) {
    error.value = '微信授权 state 不匹配，请重试'
    return
  }
  sessionStorage.removeItem('wechat_oauth_state')

  try {
    await authStore.loginWithOAuth('wechat_web', code)
    router.replace('/')
  } catch (e: any) {
    error.value = e?.message || '微信登录失败，请重试'
  }
})
</script>
