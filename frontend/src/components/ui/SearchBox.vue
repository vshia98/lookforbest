<template>
  <div class="relative" ref="containerRef">
    <div class="relative flex-1">
      <input
        v-model="query"
        @input="onInput"
        @keydown.enter.prevent="onEnter"
        @keydown.ArrowDown.prevent="moveDown"
        @keydown.ArrowUp.prevent="moveUp"
        @keydown.Escape="closeSuggest"
        @focus="onFocus"
        type="text"
        :placeholder="placeholder"
        class="w-full pl-4 pr-10 py-2.5 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
      />
      <button
        @click="submit"
        class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-primary-500"
      >
        🔍
      </button>
    </div>

    <!-- 联想下拉 -->
    <div
      v-if="showDropdown && (suggestions.length > 0 || hotKeywords.length > 0)"
      class="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-64 overflow-y-auto"
    >
      <!-- 联想词 -->
      <template v-if="suggestions.length > 0">
        <div class="px-3 py-1.5 text-xs text-gray-400 font-medium border-b border-gray-100">联想搜索</div>
        <button
          v-for="(item, idx) in suggestions"
          :key="'s' + idx"
          @mousedown.prevent="selectSuggestion(item)"
          class="w-full text-left px-4 py-2.5 text-sm hover:bg-gray-50 flex items-center gap-2"
          :class="{ 'bg-primary-50 text-primary-700': activeIndex === idx }"
        >
          <span class="text-gray-400 text-xs">🔍</span>
          <span v-html="highlightMatch(item)" />
        </button>
      </template>

      <!-- 热搜词（未输入时显示） -->
      <template v-else-if="!query && hotKeywords.length > 0">
        <div class="px-3 py-1.5 text-xs text-gray-400 font-medium border-b border-gray-100">热门搜索</div>
        <button
          v-for="(kw, idx) in hotKeywords"
          :key="'h' + idx"
          @mousedown.prevent="selectSuggestion(kw)"
          class="w-full text-left px-4 py-2.5 text-sm hover:bg-gray-50 flex items-center gap-2"
        >
          <span class="text-orange-400 text-xs">🔥</span>
          {{ kw }}
        </button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { searchService } from '@/services/robots'

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
}>(), {
  placeholder: '搜索机器人...'
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: string): void
  (e: 'search', val: string): void
}>()

const query = ref(props.modelValue)
const suggestions = ref<string[]>([])
const hotKeywords = ref<string[]>([])
const showDropdown = ref(false)
const activeIndex = ref(-1)
const containerRef = ref<HTMLElement | null>(null)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

onMounted(async () => {
  try {
    const res = await searchService.hotKeywords()
    hotKeywords.value = res.data || []
  } catch {}
  document.addEventListener('click', onOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onOutsideClick)
})

function onInput() {
  emit('update:modelValue', query.value)
  activeIndex.value = -1
  if (debounceTimer) clearTimeout(debounceTimer)
  if (!query.value.trim()) {
    suggestions.value = []
    return
  }
  debounceTimer = setTimeout(fetchSuggestions, 200)
}

async function fetchSuggestions() {
  if (!query.value.trim()) return
  try {
    const res = await searchService.suggest(query.value.trim())
    suggestions.value = res.data || []
  } catch {
    suggestions.value = []
  }
}

function onFocus() {
  showDropdown.value = true
}

function onEnter() {
  if (activeIndex.value >= 0 && suggestions.value[activeIndex.value]) {
    selectSuggestion(suggestions.value[activeIndex.value])
  } else {
    submit()
  }
}

function submit() {
  closeSuggest()
  emit('update:modelValue', query.value)
  emit('search', query.value)
}

function selectSuggestion(val: string) {
  query.value = val
  emit('update:modelValue', val)
  closeSuggest()
  emit('search', val)
}

function moveDown() {
  if (activeIndex.value < suggestions.value.length - 1) activeIndex.value++
}

function moveUp() {
  if (activeIndex.value > 0) activeIndex.value--
}

function closeSuggest() {
  showDropdown.value = false
  activeIndex.value = -1
}

function onOutsideClick(e: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(e.target as Node)) {
    closeSuggest()
  }
}

function highlightMatch(text: string): string {
  if (!query.value) return text
  const escaped = query.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark class="bg-yellow-100 text-yellow-900 rounded px-0.5">$1</mark>')
}
</script>
