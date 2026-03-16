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
        class="input-dark w-full pl-4 pr-10"
      />
      <button
        @click="submit"
        class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-primary transition-colors"
      >
        🔍
      </button>
    </div>

    <!-- 联想下拉 -->
    <div
      v-if="showDropdown && (suggestions.length > 0 || hotKeywords.length > 0)"
      class="absolute left-0 right-0 top-full mt-1 bg-dark-50 border border-white/[0.06] rounded-xl shadow-xl z-50 max-h-64 overflow-y-auto backdrop-blur-xl"
    >
      <!-- 联想词 -->
      <template v-if="suggestions.length > 0">
        <div class="px-3 py-1.5 text-xs text-gray-500 font-medium border-b border-white/[0.04]">联想搜索</div>
        <button
          v-for="(item, idx) in suggestions"
          :key="'s' + idx"
          @mousedown.prevent="selectSuggestion(item)"
          class="w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-white/[0.04] hover:text-primary flex items-center gap-2 transition-colors"
          :class="{ 'bg-primary/10 text-primary': activeIndex === idx }"
        >
          <span class="text-gray-600 text-xs">🔍</span>
          <span v-html="highlightMatch(item)" />
        </button>
      </template>

      <!-- 热搜词 -->
      <template v-else-if="!query && hotKeywords.length > 0">
        <div class="px-3 py-1.5 text-xs text-gray-500 font-medium border-b border-white/[0.04]">热门搜索</div>
        <button
          v-for="(kw, idx) in hotKeywords"
          :key="'h' + idx"
          @mousedown.prevent="selectSuggestion(kw)"
          class="w-full text-left px-4 py-2.5 text-sm text-gray-300 hover:bg-white/[0.04] hover:text-primary flex items-center gap-2 transition-colors"
        >
          <span class="text-accent-orange text-xs">🔥</span>
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
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark class="bg-primary/20 text-primary rounded px-0.5">$1</mark>')
}
</script>
