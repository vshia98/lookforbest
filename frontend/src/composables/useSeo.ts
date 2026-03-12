import { onUnmounted, watch } from 'vue'
import type { Ref } from 'vue'

interface SeoMeta {
  title?: string
  description?: string
  keywords?: string
  ogTitle?: string
  ogDescription?: string
  ogImage?: string
  ogUrl?: string
  canonicalUrl?: string
  jsonLd?: Record<string, unknown>
}

/** 设置或更新 <meta> 标签 */
function setMeta(name: string, content: string, attr: 'name' | 'property' = 'name') {
  let el = document.querySelector(`meta[${attr}="${name}"]`) as HTMLMetaElement | null
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute(attr, name)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

/** 移除 <meta> 标签 */
function removeMeta(name: string, attr: 'name' | 'property' = 'name') {
  document.querySelector(`meta[${attr}="${name}"]`)?.remove()
}

/** 设置或更新 <link> 标签 */
function setLink(rel: string, href: string) {
  let el = document.querySelector(`link[rel="${rel}"]`) as HTMLLinkElement | null
  if (!el) {
    el = document.createElement('link')
    el.rel = rel
    document.head.appendChild(el)
  }
  el.href = href
}

/** 设置 JSON-LD 结构化数据 */
function setJsonLd(data: Record<string, unknown>) {
  let el = document.querySelector('script[type="application/ld+json"]#page-jsonld') as HTMLScriptElement | null
  if (!el) {
    el = document.createElement('script')
    el.type = 'application/ld+json'
    el.id = 'page-jsonld'
    document.head.appendChild(el)
  }
  el.textContent = JSON.stringify(data)
}

function removeJsonLd() {
  document.querySelector('script[type="application/ld+json"]#page-jsonld')?.remove()
}

const defaultTitle = 'LookForBest - 全球机器人展示平台'
const defaultDescription = '搜罗全球机器人，查找、对比、选择最适合您的机器人'

/** SEO 头部信息管理 composable */
export function useSeo(metaRef: Ref<SeoMeta | null>) {
  function apply(meta: SeoMeta | null) {
    if (!meta) {
      document.title = defaultTitle
      return
    }

    const title = meta.title ? `${meta.title} | LookForBest` : defaultTitle
    const desc = meta.description || defaultDescription

    document.title = title
    setMeta('description', desc)
    if (meta.keywords) setMeta('keywords', meta.keywords)

    // Open Graph
    setMeta('og:title', meta.ogTitle || title, 'property')
    setMeta('og:description', meta.ogDescription || desc, 'property')
    setMeta('og:type', 'website', 'property')
    if (meta.ogImage) setMeta('og:image', meta.ogImage, 'property')
    if (meta.ogUrl) setMeta('og:url', meta.ogUrl, 'property')

    // Twitter Card
    setMeta('twitter:card', 'summary_large_image')
    setMeta('twitter:title', meta.ogTitle || title)
    setMeta('twitter:description', meta.ogDescription || desc)
    if (meta.ogImage) setMeta('twitter:image', meta.ogImage)

    // Canonical
    if (meta.canonicalUrl) setLink('canonical', meta.canonicalUrl)

    // JSON-LD
    if (meta.jsonLd) setJsonLd(meta.jsonLd)
    else removeJsonLd()
  }

  const stop = watch(metaRef, apply, { immediate: true })

  onUnmounted(() => {
    stop()
    // 恢复默认值
    document.title = defaultTitle
    removeMeta('keywords')
    removeJsonLd()
  })
}

/** 生成机器人产品的 JSON-LD 结构化数据 */
export function buildRobotJsonLd(robot: {
  name: string
  nameEn?: string
  description?: string
  coverImageUrl?: string
  manufacturer?: { name: string; websiteUrl?: string }
  releaseYear?: number
  priceUsdFrom?: number
  slug: string
}): Record<string, unknown> {
  const base = `${window.location.origin}/robots/${robot.slug}`
  return {
    '@context': 'https://schema.org',
    '@type': 'Product',
    name: robot.nameEn || robot.name,
    description: robot.description?.slice(0, 300) || '',
    image: robot.coverImageUrl ? [robot.coverImageUrl] : [],
    url: base,
    brand: robot.manufacturer ? {
      '@type': 'Brand',
      name: robot.manufacturer.name,
      url: robot.manufacturer.websiteUrl
    } : undefined,
    ...(robot.releaseYear ? { releaseDate: String(robot.releaseYear) } : {}),
    ...(robot.priceUsdFrom ? {
      offers: {
        '@type': 'Offer',
        priceCurrency: 'USD',
        price: robot.priceUsdFrom,
        availability: 'https://schema.org/InStock'
      }
    } : {})
  }
}
