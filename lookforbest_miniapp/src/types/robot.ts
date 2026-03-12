/** 机器人列表摘要 */
export interface RobotSummary {
  id: number
  name: string
  slug: string
  coverImageUrl?: string
  manufacturer: { id: number; name: string; logoUrl?: string; country?: string }
  category: { id: number; name: string; slug?: string }
  payloadKg?: number
  reachMm?: number
  dof?: number
  has3dModel: boolean
  priceRange?: string
  viewCount: number
  favoriteCount: number
}

/** 机器人详情 */
export interface Robot extends RobotSummary {
  nameEn?: string
  modelNumber: string
  description?: string
  model3dUrl?: string
  hasVideo: boolean
  specs: RobotSpecs
  applicationDomains: { id: number; name: string }[]
  images: RobotMedia[]
  videos: RobotMedia[]
  documents: RobotMedia[]
  isFavorited: boolean
  status: string
  releaseYear?: number
}

export interface RobotSpecs {
  payloadKg?: number
  reachMm?: number
  dof?: number
  repeatabilityMm?: number
  maxSpeedDegS?: number
  weightKg?: number
  ipRating?: string
  mounting?: string
}

export interface RobotMedia {
  id: number
  url: string
  thumbnailUrl?: string
  title?: string
  durationS?: number
}

/** 分页响应 */
export interface PagedData<T> {
  content: T[]
  page: number
  size: number
  total: number
  totalPages: number
  hasNext: boolean
}

/** 筛选参数 */
export interface RobotFilter {
  q?: string
  categoryId?: number
  manufacturerId?: number
  domainIds?: string
  payloadMin?: number
  payloadMax?: number
  reachMin?: number
  reachMax?: number
  dofMin?: number
  dofMax?: number
  has3dModel?: boolean
  priceRange?: string
  sort?: 'relevance' | 'newest' | 'popular'
  page?: number
  size?: number
}
