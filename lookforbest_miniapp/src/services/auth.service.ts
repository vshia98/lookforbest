import { http } from './http'

export interface User {
  id: number
  email: string
  username: string
  displayName?: string
  role: string
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: User
}

export const authService = {
  login(email: string, password: string) {
    return http.post<AuthTokens>('/auth/login', { email, password }).then((data) => {
      http.saveTokens(data.accessToken, data.refreshToken)
      return data
    })
  },

  register(email: string, password: string, username: string, displayName?: string) {
    return http
      .post<AuthTokens>('/auth/register', { email, password, username, displayName })
      .then((data) => {
        http.saveTokens(data.accessToken, data.refreshToken)
        return data
      })
  },

  getCurrentUser() {
    return http.get<User>('/users/me')
  },

  logout() {
    http.clearTokens()
    uni.reLaunch({ url: '/pages/index/index' })
  },

  isLoggedIn() {
    return !!http.getToken()
  },
}
