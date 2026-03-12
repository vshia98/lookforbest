<template>
  <component
    :is="ad.linkUrl ? 'a' : 'div'"
    :href="ad.linkUrl || undefined"
    :target="ad.linkUrl ? '_blank' : undefined"
    rel="noopener noreferrer"
    class="block relative bg-white rounded-xl border border-amber-200 shadow-sm hover:shadow-md transition-shadow overflow-hidden cursor-pointer group"
    @click="handleClick"
  >
    <!-- 推广标识 -->
    <span class="absolute top-2 right-2 z-10 bg-amber-400 text-white text-xs font-medium px-2 py-0.5 rounded-full">
      推广
    </span>

    <!-- 图片 -->
    <div class="aspect-video overflow-hidden bg-gray-50">
      <img
        v-if="ad.imageUrl"
        :src="ad.imageUrl"
        :alt="ad.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-amber-50 to-orange-50">
        <span class="text-amber-300 text-4xl">💰</span>
      </div>
    </div>

    <!-- 标题 -->
    <div class="px-4 py-3">
      <p class="text-sm font-medium text-gray-800 line-clamp-2">{{ ad.title }}</p>
      <p class="text-xs text-amber-500 mt-1">赞助内容</p>
    </div>
  </component>
</template>

<script setup lang="ts">
import { adService } from '@/services/robots'

const props = defineProps<{
  ad: {
    id: number
    title: string
    imageUrl?: string
    linkUrl?: string
    adType?: string
  }
}>()

function handleClick() {
  adService.recordClick(props.ad.id).catch(() => {})
}
</script>
