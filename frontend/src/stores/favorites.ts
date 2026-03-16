import { defineStore } from 'pinia'
import { ref } from 'vue'
import { favoritesService } from '@/services/favorites'
import { useAuthStore } from '@/stores/auth'
import type { RobotListItem } from '@/types/robot'

export const useFavoritesStore = defineStore('favorites', () => {
  const favoriteIds = ref<Set<number>>(new Set())
  const favorites = ref<RobotListItem[]>([])
  const loading = ref(false)
  /** 控制登录弹框 */
  const showLoginModal = ref(false)

  function isFavorited(robotId: number) {
    return favoriteIds.value.has(robotId)
  }

  async function fetchFavorites() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) return
    loading.value = true
    try {
      const res = await favoritesService.getList()
      favorites.value = res.data.content
      favoriteIds.value = new Set(res.data.content.map((r: RobotListItem) => r.id))
    } finally {
      loading.value = false
    }
  }

  async function toggle(robotId: number): Promise<boolean> {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn) {
      showLoginModal.value = true
      return false
    }

    try {
      if (isFavorited(robotId)) {
        await favoritesService.remove(robotId)
        favoriteIds.value.delete(robotId)
        favorites.value = favorites.value.filter(r => r.id !== robotId)
      } else {
        await favoritesService.add(robotId)
        favoriteIds.value.add(robotId)
      }
      return true
    } catch (e) {
      console.error('收藏操作失败:', e)
      return false
    }
  }

  return { favorites, loading, showLoginModal, isFavorited, fetchFavorites, toggle }
})
