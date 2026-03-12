import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/auth'

export interface User {
  id: number
  username: string
  email: string
  role: 'user' | 'admin'
  avatarUrl?: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('access_token'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  async function login(usernameOrEmail: string, password: string) {
    const res = await authService.login({ usernameOrEmail, password })
    token.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('access_token', res.data.accessToken)
  }

  async function register(username: string, email: string, password: string) {
    const res = await authService.register({ username, email, password })
    token.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('access_token', res.data.accessToken)
  }

  async function loginWithOAuth(provider: string, code: string) {
    const res = await authService.oauthLogin({ provider, code })
    token.value = res.data.accessToken
    user.value = res.data.user
    localStorage.setItem('access_token', res.data.accessToken)
    if (res.data.refreshToken) {
      localStorage.setItem('refresh_token', res.data.refreshToken)
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const res = await authService.me()
      user.value = res.data
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('access_token')
  }

  // 初始化时尝试获取当前用户
  if (token.value) {
    fetchCurrentUser()
  }

  return { user, token, isLoggedIn, isAdmin, login, register, logout, fetchCurrentUser, loginWithOAuth }
})
