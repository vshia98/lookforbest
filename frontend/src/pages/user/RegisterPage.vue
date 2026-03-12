<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <router-link to="/" class="text-2xl font-bold text-gray-900 inline-flex items-center gap-2">
          <span class="text-primary-500">⚡</span> LookForBest
        </router-link>
        <p class="text-gray-500 mt-2">创建新账户</p>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <form @submit.prevent="handleRegister" class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">用户名</label>
            <input
              v-model="form.username"
              type="text"
              required
              minlength="3"
              placeholder="3-20个字符"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">邮箱</label>
            <input
              v-model="form.email"
              type="email"
              required
              placeholder="your@email.com"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
            <input
              v-model="form.password"
              type="password"
              required
              minlength="8"
              placeholder="至少8个字符"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1.5">确认密码</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              required
              placeholder="再次输入密码"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div v-if="error" class="bg-red-50 text-red-600 text-sm px-4 py-3 rounded-xl">{{ error }}</div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-primary-500 text-white py-2.5 rounded-xl font-medium text-sm hover:bg-primary-600 transition-colors disabled:opacity-60"
          >
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <p class="text-center text-sm text-gray-500 mt-6">
          已有账户？
          <router-link to="/login" class="text-primary-500 font-medium hover:underline">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  error.value = ''
  loading.value = true
  try {
    await authStore.register(form.username, form.email, form.password)
    router.push('/')
  } catch (e: any) {
    error.value = e?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
