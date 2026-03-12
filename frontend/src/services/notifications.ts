import http from './api'

export interface NotificationDTO {
  id: number
  type: 'inquiry_replied' | 'comment_replied' | 'system'
  title: string
  content?: string
  isRead: boolean
  relatedId?: number
  relatedType?: string
  createdAt: string
}

export const notificationService = {
  getList(page = 0, size = 20): Promise<{ data: any }> {
    return http.get('/notifications', { params: { page, size } })
  },

  getUnreadCount(): Promise<{ data: { count: number } }> {
    return http.get('/notifications/unread-count')
  },

  markRead(id: number): Promise<void> {
    return http.patch(`/notifications/${id}/read`)
  },

  markAllRead(): Promise<void> {
    return http.patch('/notifications/read-all')
  },

  // Admin
  broadcast(title: string, content?: string): Promise<void> {
    return http.post('/notifications/admin/announcement', { title, content })
  }
}
