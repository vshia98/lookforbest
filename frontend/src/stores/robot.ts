import { defineStore } from 'pinia'
import { ref } from 'vue'
import { robotService } from '@/services/robots'
import type { RobotListItem, RobotListParams } from '@/types/robot'

export const useRobotStore = defineStore('robot', () => {
  const robots = ref<RobotListItem[]>([])
  const total = ref(0)
  const loading = ref(false)
  const compareList = ref<RobotListItem[]>([])

  async function fetchRobots(params: RobotListParams) {
    loading.value = true
    try {
      const res = await robotService.getList(params)
      robots.value = res.data.content
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  function addToCompare(robot: RobotListItem) {
    if (compareList.value.length >= 4) return false
    if (compareList.value.find(r => r.id === robot.id)) return false
    compareList.value.push(robot)
    return true
  }

  function removeFromCompare(robotId: number) {
    compareList.value = compareList.value.filter(r => r.id !== robotId)
  }

  return { robots, total, loading, compareList, fetchRobots, addToCompare, removeFromCompare }
})
