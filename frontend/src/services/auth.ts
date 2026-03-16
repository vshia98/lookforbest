import http from './api'

type AuthResponse = { data: { accessToken: string; refreshToken: string; user: any } }

export const authService = {
  login(data: { usernameOrEmail: string; password: string }) {
    return http.post<any, AuthResponse>('/auth/login', data)
  },

  register(data: { username: string; email: string; password: string }) {
    return http.post<any, AuthResponse>('/auth/register', data)
  },

  me() {
    return http.get<any, { data: any }>('/users/me')
  },

  refreshToken() {
    return http.post<any, { data: { accessToken: string } }>('/auth/refresh')
  },

  oauthLogin(data: { provider: string; code: string }) {
    return http.post<any, AuthResponse>('/auth/oauth/login', data)
  }
}
