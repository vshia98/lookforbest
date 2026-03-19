<template>
  <aside class="w-64 flex-shrink-0">
    <div class="bg-dark-50 rounded-xl border border-white/[0.04] p-5 sticky top-20">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-white">
          {{ currentLocale === 'en' ? 'Filters' : '筛选条件' }}
        </h3>
        <button @click="reset" class="text-xs text-primary hover:underline">
          {{ t('common.reset') }}
        </button>
      </div>

      <!-- 分类 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">
          {{ currentLocale === 'en' ? 'Robot category' : '机器人分类' }}
        </h4>
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
            <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">
              {{ facetLabel(cat) }}
            </span>
            <span class="ml-auto text-xs text-gray-600">({{ cat.count }})</span>
          </label>
        </div>
      </div>

      <!-- 载荷范围 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">
          {{ t('filter.payloadRange') }}
        </h4>
        <div class="flex gap-2">
          <input
            v-model.number="localFilters.payloadMin"
            type="number"
            :placeholder="currentLocale === 'en' ? 'Min' : '最小'"
            class="input-dark text-xs py-1.5 px-2"
          />
          <span class="text-gray-600 self-center">-</span>
          <input
            v-model.number="localFilters.payloadMax"
            type="number"
            :placeholder="currentLocale === 'en' ? 'Max' : '最大'"
            class="input-dark text-xs py-1.5 px-2"
          />
        </div>
      </div>

      <!-- 工作半径 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">
          {{ t('filter.reachRange') }}
        </h4>
        <div class="flex gap-2">
          <input
            v-model.number="localFilters.reachMin"
            type="number"
            :placeholder="currentLocale === 'en' ? 'Min' : '最小'"
            class="input-dark text-xs py-1.5 px-2"
          />
          <span class="text-gray-600 self-center">-</span>
          <input
            v-model.number="localFilters.reachMax"
            type="number"
            :placeholder="currentLocale === 'en' ? 'Max' : '最大'"
            class="input-dark text-xs py-1.5 px-2"
          />
        </div>
      </div>

      <!-- 厂商 -->
      <div class="mb-5">
        <h4 class="text-sm font-medium text-gray-300 mb-2">
          {{ currentLocale === 'en' ? 'Manufacturer' : '厂商' }}
        </h4>
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
            <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">
              {{ facetLabel(mfr) }}
            </span>
            <span class="ml-auto text-xs text-gray-600">({{ mfr.count }})</span>
          </label>
        </div>
      </div>

      <!-- 是否有3D模型 -->
      <div class="mb-5">
        <label class="flex items-center gap-2 cursor-pointer group">
          <input type="checkbox" v-model="localFilters.has3dModel" class="text-primary rounded focus:ring-primary/40 bg-dark-100 border-white/10" />
          <span class="text-sm text-gray-400 group-hover:text-primary transition-colors">
            {{ currentLocale === 'en' ? 'Has 3D model' : '有3D模型' }}
          </span>
        </label>
      </div>

      <button
        @click="apply"
        class="w-full btn-primary py-2 text-sm"
      >
        {{ currentLocale === 'en' ? 'Apply filters' : '应用筛选' }}
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'

interface FacetItem {
  id: number;
  name: string;
  nameEn?: string;
  count: number;
}

const props = defineProps<{
  modelValue: Record<string, any>
  categories?: FacetItem[]
  manufacturers?: FacetItem[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: Record<string, any>): void
  (e: 'apply'): void
}>()

const { t, locale } = useI18n()
const currentLocale = computed(() => (locale.value || 'zh') as 'zh' | 'en')

const localFilters = reactive({ ...props.modelValue })

watch(localFilters, (val) => emit('update:modelValue', { ...val }))

function facetLabel(item: FacetItem) {
  if (currentLocale.value === 'en' && item.nameEn) {
    return item.nameEn
  }
  return item.name
}

function apply() {
  emit('apply')
}

function reset() {
  Object.keys(localFilters).forEach(k => { (localFilters as any)[k] = undefined })
  emit('update:modelValue', {})
  emit('apply')
}
</script>
