import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RobotSummary } from '../types/robot'

const MAX_COMPARE = 2 // 小程序屏幕限制最多2款

export const useCompareStore = defineStore('compare', () => {
  const items = ref<RobotSummary[]>([])

  function add(robot: RobotSummary) {
    if (items.value.length >= MAX_COMPARE) {
      uni.showToast({ title: `最多对比${MAX_COMPARE}款机器人`, icon: 'none' })
      return false
    }
    if (items.value.find((r) => r.id === robot.id)) {
      uni.showToast({ title: '已在对比列表中', icon: 'none' })
      return false
    }
    items.value.push(robot)
    return true
  }

  function remove(robotId: number) {
    items.value = items.value.filter((r) => r.id !== robotId)
  }

  function clear() {
    items.value = []
  }

  const count = () => items.value.length
  const hasRobot = (id: number) => items.value.some((r) => r.id === id)

  return { items, add, remove, clear, count, hasRobot }
})
