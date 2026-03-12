export interface Manufacturer {
  id: number
  name: string
  nameEn?: string
  country: string
  logoUrl?: string
  websiteUrl?: string
}

export interface Category {
  id: number
  name: string
  nameEn?: string
  slug: string
}

export interface ApplicationDomain {
  id: number
  name: string
  slug: string
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
  maxSpeedMs?: number
  maxLoadKg?: number
  batteryLifeH?: number
  navigationType?: string
  heightMm?: number
  walkingSpeedMs?: number
}

export interface RobotImage {
  id: number
  url: string
  thumbnailUrl?: string
  altText?: string
  imageType: string
  sortOrder: number
}

export interface RobotVideo {
  id: number
  title?: string
  url: string
  thumbnailUrl?: string
  durationS?: number
  videoType: string
}

export interface RobotDocument {
  id: number
  title: string
  url: string
  docType: string
  language: string
}

export interface RobotListItem {
  id: number
  name: string
  nameEn?: string
  modelNumber?: string
  slug: string
  coverImageUrl?: string
  manufacturer: Manufacturer
  category: Category
  payloadKg?: number
  reachMm?: number
  dof?: number
  repeatabilityMm?: number
  has3dModel: boolean
  priceRange: string
  status: string
  viewCount: number
  favoriteCount: number
}

export interface RobotDetail extends RobotListItem {
  description?: string
  descriptionEn?: string
  releaseYear?: number
  applicationDomains: ApplicationDomain[]
  specs: RobotSpecs
  extraSpecs?: Record<string, any>
  model3dUrl?: string
  hasVideo: boolean
  images: RobotImage[]
  videos: RobotVideo[]
  documents: RobotDocument[]
  isFavorited: boolean
  isVerified: boolean
  compareCount: number
  similarRobots?: RobotListItem[]
}

export interface RobotListParams {
  q?: string
  categoryId?: number
  manufacturerId?: number
  domainIds?: number[]
  payloadMin?: number
  payloadMax?: number
  reachMin?: number
  reachMax?: number
  dofMin?: number
  dofMax?: number
  releaseYearFrom?: number
  releaseYearTo?: number
  priceRange?: string
  has3dModel?: boolean
  status?: string
  sort?: 'relevance' | 'newest' | 'popular'
  page?: number
  size?: number
}

export interface RobotListResponse {
  data: {
    content: RobotListItem[]
    page: number
    size: number
    total: number
    totalPages: number
    hasNext: boolean
    facets?: {
      categories: Array<{ id: number; name: string; count: number }>
      manufacturers: Array<{ id: number; name: string; count: number }>
    }
  }
}
