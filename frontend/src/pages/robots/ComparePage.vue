<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-800 mb-2">产品对比</h1>
    <p class="text-gray-500 mb-8 text-sm">最多支持4款机器人同时对比</p>

    <div v-if="compareList.length === 0" class="text-center py-20">
      <div class="text-6xl mb-4">🤖</div>
      <p class="text-gray-500 mb-6">还没有添加对比产品</p>
      <router-link to="/robots" class="bg-primary-500 text-white px-6 py-2.5 rounded-xl text-sm hover:bg-primary-600 transition-colors">
        浏览机器人库
      </router-link>
    </div>

    <div v-else class="overflow-x-auto">
      <table class="w-full min-w-max bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <!-- 产品头部 -->
        <thead>
          <tr class="border-b border-gray-100">
            <th class="w-44 px-4 py-4 text-left text-sm font-medium text-gray-500 bg-gray-50">对比项</th>
            <th
              v-for="robot in compareList"
              :key="robot.id"
              class="px-4 py-4 text-center"
            >
              <div class="relative">
                <button
                  @click="robotStore.removeFromCompare(robot.id)"
                  class="absolute -top-2 -right-2 w-5 h-5 bg-gray-200 rounded-full text-gray-500 hover:bg-red-100 hover:text-red-500 text-xs flex items-center justify-center"
                >
                  ✕
                </button>
                <div class="w-24 h-20 bg-gray-100 rounded-xl flex items-center justify-center mx-auto mb-2 overflow-hidden">
                  <img v-if="robot.coverImageUrl" :src="robot.coverImageUrl" :alt="robot.name" class="w-full h-full object-cover" />
                  <span v-else class="text-3xl">🤖</span>
                </div>
                <router-link :to="{ name: 'robot-detail', params: { slug: robot.slug } }" class="text-sm font-semibold text-gray-800 hover:text-primary-600 block">
                  {{ robot.name }}
                </router-link>
                <span class="text-xs text-gray-400">{{ robot.manufacturer.name }}</span>
              </div>
            </th>
            <!-- 空槽 -->
            <th v-for="i in (4 - compareList.length)" :key="`empty-${i}`" class="px-4 py-4">
              <router-link to="/robots" class="flex flex-col items-center gap-2 text-gray-300 hover:text-primary-400 transition-colors">
                <div class="w-24 h-20 border-2 border-dashed border-gray-200 rounded-xl flex items-center justify-center text-3xl hover:border-primary-300">+</div>
                <span class="text-xs">添加产品</span>
              </router-link>
            </th>
          </tr>
        </thead>
        <tbody>
          <!-- 分类 -->
          <tr class="border-b border-gray-50">
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">分类</td>
            <td v-for="robot in compareList" :key="robot.id" class="px-4 py-3 text-sm text-center text-gray-700">
              {{ robot.category.name }}
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`c-empty-${i}`" class="px-4 py-3"></td>
          </tr>

          <!-- 载荷 -->
          <tr class="border-b border-gray-50">
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">最大载荷</td>
            <td
              v-for="robot in compareList"
              :key="robot.id"
              class="px-4 py-3 text-sm text-center font-medium"
              :class="highlight(compareList.map(r => r.payloadKg), robot.payloadKg, 'max')"
            >
              {{ robot.payloadKg ? `${robot.payloadKg} kg` : '-' }}
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`l-empty-${i}`" class="px-4 py-3"></td>
          </tr>

          <!-- 工作半径 -->
          <tr class="border-b border-gray-50">
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">工作半径</td>
            <td
              v-for="robot in compareList"
              :key="robot.id"
              class="px-4 py-3 text-sm text-center font-medium"
              :class="highlight(compareList.map(r => r.reachMm), robot.reachMm, 'max')"
            >
              {{ robot.reachMm ? `${robot.reachMm} mm` : '-' }}
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`r-empty-${i}`" class="px-4 py-3"></td>
          </tr>

          <!-- 自由度 -->
          <tr class="border-b border-gray-50">
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">自由度</td>
            <td
              v-for="robot in compareList"
              :key="robot.id"
              class="px-4 py-3 text-sm text-center font-medium"
              :class="highlight(compareList.map(r => r.dof), robot.dof, 'max')"
            >
              {{ robot.dof ? `${robot.dof} DOF` : '-' }}
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`d-empty-${i}`" class="px-4 py-3"></td>
          </tr>

          <!-- 重复定位精度 -->
          <tr class="border-b border-gray-50">
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">重复定位精度</td>
            <td
              v-for="robot in compareList"
              :key="robot.id"
              class="px-4 py-3 text-sm text-center font-medium"
              :class="highlight(compareList.map(r => r.repeatabilityMm), robot.repeatabilityMm, 'min')"
            >
              {{ robot.repeatabilityMm ? `±${robot.repeatabilityMm} mm` : '-' }}
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`rep-empty-${i}`" class="px-4 py-3"></td>
          </tr>

          <!-- 是否有3D模型 -->
          <tr>
            <td class="px-4 py-3 text-sm text-gray-500 bg-gray-50">3D模型</td>
            <td v-for="robot in compareList" :key="robot.id" class="px-4 py-3 text-sm text-center">
              <span :class="robot.has3dModel ? 'text-green-500' : 'text-gray-300'">
                {{ robot.has3dModel ? '✓ 有' : '✗ 无' }}
              </span>
            </td>
            <td v-for="i in (4 - compareList.length)" :key="`3d-empty-${i}`" class="px-4 py-3"></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRobotStore } from '@/stores/robot'

const robotStore = useRobotStore()
const compareList = computed(() => robotStore.compareList)

function highlight(values: (number | undefined)[], current: number | undefined, mode: 'max' | 'min') {
  if (current === undefined || current === null) return ''
  const nums = values.filter(v => v !== undefined && v !== null) as number[]
  if (nums.length < 2) return ''
  const best = mode === 'max' ? Math.max(...nums) : Math.min(...nums)
  return current === best ? 'text-green-600 bg-green-50' : ''
}
</script>
