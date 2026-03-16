import http from './api'

export const favoritesService = {
  getList(params?: { page?: number; size?: number }) {
    return http.get<any, any>('/users/me/favorites', { params })
  },

  add(robotId: number) {
    return http.post<any, any>(`/users/me/favorites/${robotId}`)
  },

  remove(robotId: number) {
    return http.delete<any, any>(`/users/me/favorites/${robotId}`)
  }
}
