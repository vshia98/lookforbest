import http from './api'

export const favoritesService = {
  getList(params?: { page?: number; size?: number }) {
    return http.get<any, any>('/user/favorites', { params })
  },

  add(robotId: number) {
    return http.post<any, any>(`/user/favorites/${robotId}`)
  },

  remove(robotId: number) {
    return http.delete<any, any>(`/user/favorites/${robotId}`)
  }
}
