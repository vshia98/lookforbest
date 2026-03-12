import http from './api'

export interface InquiryCreatePayload {
  robotId: number
  message: string
  contactName?: string
  contactEmail: string
  contactPhone?: string
  contactCompany?: string
}

export interface InquiryDTO {
  id: number
  robotId: number
  robotName: string
  robotSlug: string
  manufacturerId: number
  manufacturerName: string
  message: string
  contactName?: string
  contactEmail: string
  contactPhone?: string
  contactCompany?: string
  status: 'pending' | 'contacted' | 'replied' | 'closed'
  replyContent?: string
  repliedAt?: string
  createdAt: string
}

export const inquiryService = {
  create(payload: InquiryCreatePayload): Promise<{ data: InquiryDTO }> {
    return http.post('/inquiries', payload)
  },

  getMyInquiries(page = 0, size = 20): Promise<{ data: any }> {
    return http.get('/inquiries/my', { params: { page, size } })
  },

  getById(id: number): Promise<{ data: InquiryDTO }> {
    return http.get(`/inquiries/${id}`)
  },

  // Admin
  adminList(page = 0, size = 20): Promise<{ data: any }> {
    return http.get('/inquiries/admin', { params: { page, size } })
  },

  adminReply(id: number, replyContent: string): Promise<{ data: InquiryDTO }> {
    return http.post(`/inquiries/admin/${id}/reply`, { replyContent })
  },

  adminUpdateStatus(id: number, status: string): Promise<{ data: InquiryDTO }> {
    return http.patch(`/inquiries/admin/${id}/status`, null, { params: { status } })
  }
}
