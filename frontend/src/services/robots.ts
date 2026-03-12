import http from './api'
import type { RobotListParams, RobotListResponse, RobotDetail } from '@/types/robot'

export const robotService = {
  getList(params: RobotListParams): Promise<RobotListResponse> {
    return http.get('/robots', { params })
  },

  getById(id: number): Promise<{ data: RobotDetail }> {
    return http.get(`/robots/${id}`)
  },

  getBySlug(slug: string): Promise<{ data: RobotDetail }> {
    return http.get(`/robots/slug/${slug}`)
  },

  getSimilar(id: number, size = 6): Promise<{ data: RobotDetail[] }> {
    return http.get(`/robots/${id}/similar`, { params: { size } })
  },

  getRecommend(id: number, size = 6, strategy = 'hybrid'): Promise<{ data: RobotDetail[] }> {
    return http.get(`/robots/${id}/recommend`, { params: { size, strategy } })
  },

  compare(robotIds: number[]): Promise<any> {
    return http.post('/compare', { robotIds })
  }
}

export const adminService = {
  getStats(): Promise<any> {
    return http.get('/admin/stats')
  },

  batchDelete(ids: number[]): Promise<any> {
    return http.delete('/admin/robots/batch', { data: { ids } })
  },

  batchUpdateStatus(ids: number[], status: string): Promise<any> {
    return http.patch('/admin/robots/batch/status', { ids, status })
  },

  createRobot(payload: any): Promise<any> {
    return http.post('/robots', payload)
  },

  updateRobot(id: number, payload: any): Promise<any> {
    return http.put(`/robots/${id}`, payload)
  },

  deleteRobot(id: number): Promise<any> {
    return http.delete(`/robots/${id}`)
  },

  getAnalyticsOverview(): Promise<any> {
    return http.get('/admin/analytics/overview')
  },

  getAnalyticsDailyTrends(days = 30): Promise<any> {
    return http.get('/admin/analytics/daily-trends', { params: { days } })
  },

  getAnalyticsTopRobots(days = 7, limit = 10): Promise<any> {
    return http.get('/admin/analytics/top-robots', { params: { days, limit } })
  },

  getAnalyticsTopKeywords(days = 7, limit = 20): Promise<any> {
    return http.get('/admin/analytics/top-keywords', { params: { days, limit } })
  }
}

export const reviewService = {
  list(params: Record<string, any>): Promise<any> {
    return http.get('/reviews', { params })
  },
  getById(id: number): Promise<any> {
    return http.get(`/reviews/${id}`)
  },
  create(data: any): Promise<any> {
    return http.post('/reviews', data)
  },
  submit(id: number): Promise<any> {
    return http.post(`/reviews/${id}/submit`)
  },
  deleteReview(id: number): Promise<any> {
    return http.delete(`/reviews/${id}`)
  },
  like(id: number): Promise<any> {
    return http.post(`/reviews/${id}/like`)
  },
  report(id: number, reason: string): Promise<any> {
    return http.post(`/reviews/${id}/report`, { reason })
  },
  myReviews(params: any): Promise<any> {
    return http.get('/reviews/my', { params })
  }
}

export const caseStudyService = {
  list(params: Record<string, any>): Promise<any> {
    return http.get('/case-studies', { params })
  },
  getById(id: number): Promise<any> {
    return http.get(`/case-studies/${id}`)
  },
  create(data: any): Promise<any> {
    return http.post('/case-studies', data)
  },
  submit(id: number): Promise<any> {
    return http.post(`/case-studies/${id}/submit`)
  },
  like(id: number): Promise<any> {
    return http.post(`/case-studies/${id}/like`)
  }
}

export const adService = {
  getAds(position: string): Promise<any> {
    return http.get('/ads', { params: { position } })
  },
  recordClick(id: number): Promise<any> {
    return http.post(`/ads/${id}/click`)
  }
}

export const manufacturerPortalService = {
  getMyApplication(): Promise<any> {
    return http.get('/manufacturer-portal/my-application')
  },
  apply(data: any): Promise<any> {
    return http.post('/manufacturer-portal/apply', data)
  },
  getDashboard(): Promise<any> {
    return http.get('/manufacturer-portal/dashboard')
  },
  updateProfile(data: any): Promise<any> {
    return http.put('/manufacturer-portal/profile', data)
  },
  listApplications(status = 'pending', page = 0, size = 20): Promise<any> {
    return http.get('/admin/manufacturer-applications', { params: { status, page, size } })
  },
  approveApplication(id: number): Promise<any> {
    return http.patch(`/admin/manufacturer-applications/${id}/approve`)
  },
  rejectApplication(id: number, reason: string): Promise<any> {
    return http.patch(`/admin/manufacturer-applications/${id}/reject`, { reason })
  }
}

export const membershipService = {
  getPlans(): Promise<any> {
    return http.get('/membership/plans')
  },
  getMyMembership(): Promise<any> {
    return http.get('/membership/my')
  },
  createOrder(data: { planId: number, paymentMethod: string }): Promise<any> {
    return http.post('/membership/orders', data)
  },
  getMyOrders(): Promise<any> {
    return http.get('/membership/orders')
  },
  simulatePay(orderNo: string): Promise<any> {
    return http.post(`/membership/orders/${orderNo}/simulate-pay`)
  },
  // Admin
  adminListPlans(): Promise<any> {
    return http.get('/admin/membership/plans')
  },
  adminCreatePlan(data: any): Promise<any> {
    return http.post('/admin/membership/plans', data)
  },
  adminUpdatePlan(id: number, data: any): Promise<any> {
    return http.put(`/admin/membership/plans/${id}`, data)
  },
  adminListOrders(params?: { status?: string, page?: number, size?: number }): Promise<any> {
    return http.get('/admin/membership/orders', { params })
  },
  adminActivate(orderNo: string, data: { userId: number, planId: number, durationDays?: number }): Promise<any> {
    return http.post(`/admin/membership/orders/${orderNo}/activate`, data)
  }
}

export const searchService = {
  /** ES 全文搜索 */
  search(params: Record<string, any>): Promise<any> {
    return http.get('/search', { params })
  },

  /** 搜索联想 */
  suggest(q: string): Promise<{ data: string[] }> {
    return http.get('/search/suggest', { params: { q } })
  },

  /** 热搜词 */
  hotKeywords(): Promise<{ data: string[] }> {
    return http.get('/search/hot')
  }
}
