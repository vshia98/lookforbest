<template>
  <div v-if="ads.length > 0" class="ad-banner-wrapper relative overflow-hidden rounded-xl">
    <!-- 单张广告 -->
    <template v-if="ads.length === 1">
      <component
        :is="ads[0].linkUrl ? 'a' : 'div'"
        :href="ads[0].linkUrl || undefined"
        :target="ads[0].linkUrl ? '_blank' : undefined"
        rel="noopener noreferrer"
        class="block cursor-pointer"
        @click="handleClick(ads[0])"
      >
        <div v-if="ads[0].adType === 'text'" class="bg-gradient-to-r from-primary-50 to-blue-50 border border-primary-100 rounded-xl px-6 py-4 flex items-center justify-between">
          <span class="text-sm text-gray-700 font-medium">{{ ads[0].title }}</span>
          <span class="text-xs text-primary-500 ml-4 shrink-0">广告 →</span>
        </div>
        <div v-else class="relative">
          <img
            v-if="ads[0].imageUrl"
            :src="ads[0].imageUrl"
            :alt="ads[0].title"
            class="w-full object-cover rounded-xl"
            style="max-height: 200px;"
          />
          <div v-else class="bg-gradient-to-r from-primary-500 to-blue-600 rounded-xl px-6 py-8 text-white text-center">
            <p class="font-semibold text-lg">{{ ads[0].title }}</p>
          </div>
          <span class="absolute top-2 right-2 bg-black/40 text-white text-xs px-2 py-0.5 rounded">广告</span>
        </div>
      </component>
    </template>

    <!-- 多张轮播 -->
    <template v-else>
      <div
        class="relative"
        @mouseenter="pauseCarousel"
        @mouseleave="resumeCarousel"
      >
        <TransitionGroup name="fade" tag="div" class="relative">
          <div
            v-for="(ad, index) in ads"
            v-show="index === currentIndex"
            :key="ad.id"
          >
            <component
              :is="ad.linkUrl ? 'a' : 'div'"
              :href="ad.linkUrl || undefined"
              :target="ad.linkUrl ? '_blank' : undefined"
              rel="noopener noreferrer"
              class="block cursor-pointer"
              @click="handleClick(ad)"
            >
              <div v-if="ad.adType === 'text'" class="bg-gradient-to-r from-primary-50 to-blue-50 border border-primary-100 rounded-xl px-6 py-4 flex items-center justify-between">
                <span class="text-sm text-gray-700 font-medium">{{ ad.title }}</span>
                <span class="text-xs text-primary-500 ml-4 shrink-0">广告 →</span>
              </div>
              <div v-else class="relative">
                <img
                  v-if="ad.imageUrl"
                  :src="ad.imageUrl"
                  :alt="ad.title"
                  class="w-full object-cover rounded-xl"
                  style="max-height: 200px;"
                />
                <div v-else class="bg-gradient-to-r from-primary-500 to-blue-600 rounded-xl px-6 py-8 text-white text-center">
                  <p class="font-semibold text-lg">{{ ad.title }}</p>
                </div>
                <span class="absolute top-2 right-2 bg-black/40 text-white text-xs px-2 py-0.5 rounded">广告</span>
              </div>
            </component>
          </div>
        </TransitionGroup>

        <!-- 轮播指示点 -->
        <div class="absolute bottom-2 left-0 right-0 flex justify-center gap-1.5 z-10">
          <button
            v-for="(_, i) in ads"
            :key="i"
            class="w-2 h-2 rounded-full transition-colors"
            :class="i === currentIndex ? 'bg-white' : 'bg-white/50'"
            @click.stop="currentIndex = i"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { adService } from '@/services/robots'

const props = defineProps<{
  position: string
}>()

const ads = ref<any[]>([])
const currentIndex = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function fetchAds() {
  try {
    const res = await adService.getAds(props.position)
    ads.value = res.data || []
    if (ads.value.length > 1) {
      startCarousel()
    }
  } catch {
    ads.value = []
  }
}

function startCarousel() {
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % ads.value.length
  }, 3000)
}

function pauseCarousel() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function resumeCarousel() {
  if (ads.value.length > 1 && !timer) {
    startCarousel()
  }
}

function handleClick(ad: any) {
  adService.recordClick(ad.id).catch(() => {})
}

onMounted(fetchAds)

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
