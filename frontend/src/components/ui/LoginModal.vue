<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 backdrop-blur-sm px-4" @click.self="close">
        <div class="card rounded-2xl shadow-2xl w-full max-w-sm p-6 relative border border-white/[0.06]">
          <!-- 顶部光效 -->
          <div class="absolute -top-10 left-1/2 -translate-x-1/2 w-40 h-40 bg-primary/15 rounded-full blur-[60px] pointer-events-none"></div>
          <button @click="close" class="absolute top-4 right-4 text-gray-500 hover:text-white text-lg transition-colors">&times;</button>

          <div class="relative text-center mb-6">
            <div class="text-4xl mb-3">🔐</div>
            <h2 class="text-lg font-bold text-white">{{ title || '需要登录' }}</h2>
            <p class="text-sm text-gray-500 mt-1">{{ message || '登录后即可使用该功能' }}</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-3">
            <input
              v-model="username"
              type="text"
              placeholder="用户名 / 邮箱"
              class="input-dark"
              required
              autofocus
            />
            <input
              v-model="password"
              type="password"
              placeholder="密码"
              class="input-dark"
              required
            />
            <p v-if="error" class="text-xs text-accent-red">{{ error }}</p>
            <button
              type="submit"
              :disabled="submitting"
              class="w-full btn-primary py-3 text-sm disabled:opacity-50"
            >
              {{ submitting ? '登录中...' : '登录' }}
            </button>
          </form>

          <div class="mt-4 text-center text-sm text-gray-500">
            还没有账号？
            <router-link to="/register" @click="close" class="text-primary hover:text-primary-400 transition-colors">立即注册</router-link>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  visible: boolean
  title?: string
  message?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const submitting = ref(false)

async function handleLogin() {
  error.value = ''
  submitting.value = true
  try {
    await authStore.login(username.value, password.value)
    emit('success')
    close()
  } catch (e: any) {
    error.value = e?.message || '登录失败，请检查用户名和密码'
  } finally {
    submitting.value = false
  }
}

function close() {
  error.value = ''
  emit('close')
}
</script>

<style scoped>
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-active .card, .modal-leave-active .card {
  transition: transform 0.2s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
.modal-enter-from .card {
  transform: scale(0.95);
}
</style>
