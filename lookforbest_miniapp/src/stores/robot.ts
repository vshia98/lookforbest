import { defineStore } from 'pinia'
import { ref } from 'vue'
import { robotService } from '../services/robot.service'
import type { RobotSummary, RobotFilter, PagedData } from '../types/robot'

export const useRobotStore = defineStore('robot', () => {
  const robots = ref<RobotSummary[]>([])
  const loading = ref(false)
  const hasMore = ref(true)
  const filter = ref<RobotFilter>({ page: 0, size: 20 })

  async function fetchRobots(reset = false) {
    if (loading.value) return
    loading.value = true
    try {
      if (reset) {
        filter.value.page = 0
        robots.value = []
        hasMore.value = true
      }
      const data: PagedData<RobotSummary> = await robotService.list(filter.value)
      robots.value = reset ? data.content : [...robots.value, ...data.content]
      hasMore.value = data.hasNext
      filter.value.page = (filter.value.page ?? 0) + 1
    } finally {
      loading.value = false
    }
  }

  function updateFilter(newFilter: Partial<RobotFilter>) {
    filter.value = { ...filter.value, ...newFilter, page: 0 }
    fetchRobots(true)
  }

  return { robots, loading, hasMore, filter, fetchRobots, updateFilter }
})
