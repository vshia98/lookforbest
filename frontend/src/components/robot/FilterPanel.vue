<template>
  <aside class="w-64 flex-shrink-0">
    <div class="bg-dark-50 rounded-xl border border-white/[0.04] p-5 sticky top-20">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-white">筛选条件</h3>
        <button @click="reset" class="text-xs text-primary hover:underline">重置</button>
      </div>

      <!-- 分类 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">机器人分类</h4>
        <div class="space-y-1.5">
          <label
            v-for="cat in categories"
            :key="cat.id"
            class="flex items-center gap-2 cursor-pointer group"
          >
            <input
              type="radio"
              :value="cat.id"
              v-model="localFilters.categoryId"
              class="text-primary focus:ring-primary/40 bg-dark-100 border-white/10"
            />
            <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">{{ cat.name }}</span>
            <span class="ml-auto text-xs text-gray-600">({{ cat.count }})</span>
          </label>
        </div>
      </div>

      <!-- 载荷范围 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">载荷范围 (kg)</h4>
        <div class="flex gap-2">
          <input
            v-model.number="localFilters.payloadMin"
            type="number"
            placeholder="最小"
            class="input-dark text-xs py-1.5 px-2"
          />
          <span class="text-gray-600 self-center">-</span>
          <input
            v-model.number="localFilters.payloadMax"
            type="number"
            placeholder="最大"
            class="input-dark text-xs py-1.5 px-2"
          />
        </div>
      </div>

      <!-- 工作半径 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">工作半径 (mm)</h4>
        <div class="flex gap-2">
          <input
            v-model.number="localFilters.reachMin"
            type="number"
            placeholder="最小"
            class="input-dark text-xs py-1.5 px-2"
          />
          <span class="text-gray-600 self-center">-</span>
          <input
            v-model.number="localFilters.reachMax"
            type="number"
            placeholder="最大"
            class="input-dark text-xs py-1.5 px-2"
          />
        </div>
      </div>

      <!-- 厂商 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">厂商</h4>
        <div class="space-y-1.5 max-h-40 overflow-y-auto">
          <label
            v-for="mfr in manufacturers"
            :key="mfr.id"
            class="flex items-center gap-2 cursor-pointer group"
          >
            <input
              type="radio"
              :value="mfr.id"
              v-model="localFilters.manufacturerId"
              class="text-primary focus:ring-primary/40 bg-dark-100 border-white/10"
            />
            <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">{{ mfr.name }}</span>
            <span class="ml-auto text-xs text-gray-600">({{ mfr.count }})</span>
          </label>
        </div>
      </div>

      <!-- 是否有3D模型 -->
      <div class="mb-5">
        <label class="flex items-center gap-2 cursor-pointer group">
          <input type="checkbox" v-model="localFilters.has3dModel" class="text-primary rounded focus:ring-primary/40 bg-dark-100 border-white/10" />
          <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">有3D模型</span>
        </label>
      </div>

      <button
        @click="apply"
        class="w-full btn-primary py-2 text-sm"
      >
        应用筛选
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'

interface FacetItem { id: number; name: string; count: number }

const props = defineProps<{
  modelValue: Record<string, any>
  categories?: FacetItem[]
  manufacturers?: FacetItem[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: Record<string, any>): void
  (e: 'apply'): void
}>()

const localFilters = reactive({ ...props.modelValue })

watch(localFilters, (val) => emit('update:modelValue', { ...val }))

function apply() {
  emit('apply')
}

function reset() {
  Object.keys(localFilters).forEach(k => { (localFilters as any)[k] = undefined })
  emit('update:modelValue', {})
  emit('apply')
}
</script>
