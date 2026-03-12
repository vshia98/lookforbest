/**
 * uni.request 封装，统一处理 JWT token、错误、响应格式
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api.lookforbest.com/api/v1'

interface ApiResponse<T = unknown> {
  success: boolean
  code: number
  message: string
  data: T
  timestamp: number
}

function getToken(): string | null {
  return uni.getStorageSync('access_token') || null
}

function saveTokens(accessToken: string, refreshToken: string) {
  uni.setStorageSync('access_token', accessToken)
  uni.setStorageSync('refresh_token', refreshToken)
}

function clearTokens() {
  uni.removeStorageSync('access_token')
  uni.removeStorageSync('refresh_token')
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = uni.getStorageSync('refresh_token')
  if (!refreshToken) return null
  return new Promise((resolve) => {
    uni.request({
      url: `${BASE_URL}/auth/refresh`,
      method: 'POST',
      data: { refreshToken },
      success: (res: any) => {
        const newToken = res.data?.data?.accessToken
        if (newToken) {
          uni.setStorageSync('access_token', newToken)
          resolve(newToken)
        } else {
          clearTokens()
          resolve(null)
        }
      },
      fail: () => {
        clearTokens()
        resolve(null)
      },
    })
  })
}

export function request<T = unknown>(
  url: string,
  options: {
    method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
    data?: Record<string, unknown>
    needAuth?: boolean
  } = {}
): Promise<T> {
  const { method = 'GET', data, needAuth = false } = options
  const token = getToken()
  const header: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (token) header['Authorization'] = `Bearer ${token}`

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: async (res: any) => {
        if (res.statusCode === 401 && needAuth) {
          // 尝试刷新 token 后重试
          const newToken = await refreshAccessToken()
          if (newToken) {
            header['Authorization'] = `Bearer ${newToken}`
            uni.request({
              url: `${BASE_URL}${url}`,
              method,
              data,
              header,
              success: (retryRes: any) => {
                const body = retryRes.data as ApiResponse<T>
                if (body.success) resolve(body.data)
                else reject(new Error(body.message))
              },
              fail: reject,
            })
          } else {
            uni.navigateTo({ url: '/pages/auth/login' })
            reject(new Error('请先登录'))
          }
          return
        }
        const body = res.data as ApiResponse<T>
        if (body.success) resolve(body.data)
        else reject(new Error(body.message || '请求失败'))
      },
      fail: (err) => reject(new Error(err.errMsg || '网络错误')),
    })
  })
}

export const http = {
  get: <T>(url: string, params?: Record<string, unknown>) => {
    const query = params
      ? '?' + new URLSearchParams(params as Record<string, string>).toString()
      : ''
    return request<T>(url + query, { method: 'GET' })
  },
  post: <T>(url: string, data?: Record<string, unknown>, needAuth = false) =>
    request<T>(url, { method: 'POST', data, needAuth }),
  put: <T>(url: string, data?: Record<string, unknown>) =>
    request<T>(url, { method: 'PUT', data, needAuth: true }),
  delete: <T>(url: string) =>
    request<T>(url, { method: 'DELETE', needAuth: true }),
  saveTokens,
  clearTokens,
  getToken,
}
