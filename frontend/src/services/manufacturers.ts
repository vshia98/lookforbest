import http from './api'

export const manufacturerService = {
  getList(params?: { page?: number; size?: number; q?: string }) {
    return http.get<any, any>('/manufacturers', { params })
  },

  getById(id: number) {
    return http.get<any, any>(`/manufacturers/${id}`)
  },

  create(data: any) {
    return http.post<any, any>('/manufacturers', data)
  },

  update(id: number, data: any) {
    return http.put<any, any>(`/manufacturers/${id}`, data)
  },

  delete(id: number) {
    return http.delete<any, any>(`/manufacturers/${id}`)
  }
}
