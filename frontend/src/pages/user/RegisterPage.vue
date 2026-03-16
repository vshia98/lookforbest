<template>
  <div class="min-h-screen bg-dark flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <router-link to="/" class="text-2xl font-bold text-white inline-flex items-center gap-2">
          <span class="text-primary drop-shadow-[0_0_8px_rgba(0,255,209,0.5)]">⚡</span> 寻优
        </router-link>
        <p class="text-gray-500 mt-2">创建新账户</p>
      </div>

      <div class="card rounded-2xl p-8 relative overflow-hidden">
        <div class="absolute -top-16 -left-16 w-40 h-40 bg-accent-blue/10 rounded-full blur-[80px] pointer-events-none"></div>
        <form @submit.prevent="handleRegister" class="relative space-y-5">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">用户名</label>
            <input v-model="form.username" type="text" required minlength="3" placeholder="3-20个字符" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">邮箱</label>
            <input v-model="form.email" type="email" required placeholder="your@email.com" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">密码</label>
            <input v-model="form.password" type="password" required minlength="8" placeholder="至少8个字符" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">确认密码</label>
            <input v-model="form.confirmPassword" type="password" required placeholder="再次输入密码" class="input-dark" />
          </div>

          <div v-if="error" class="bg-accent-red/10 text-accent-red text-sm px-4 py-3 rounded-xl border border-accent-red/20">{{ error }}</div>

          <button type="submit" :disabled="loading" class="w-full btn-primary py-3 text-sm disabled:opacity-60">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <p class="text-center text-sm text-gray-500 mt-6">
          已有账户？
          <router-link to="/login" class="text-primary font-medium hover:text-primary-400 transition-colors">立即登录</router-link>
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
