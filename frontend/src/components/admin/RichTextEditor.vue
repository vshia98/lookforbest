<template>
  <div class="border border-gray-200 rounded-lg overflow-hidden focus-within:ring-2 focus-within:ring-primary-500">
    <!-- 工具栏 -->
    <div class="flex flex-wrap items-center gap-0.5 p-1.5 border-b border-gray-100 bg-gray-50">
      <button
        v-for="btn in toolbar"
        :key="btn.cmd"
        type="button"
        :title="btn.label"
        @mousedown.prevent="exec(btn.cmd, btn.value)"
        class="w-7 h-7 flex items-center justify-center rounded text-sm text-gray-600 hover:bg-gray-200 transition-colors"
        :class="{ 'bg-gray-200': isActive(btn.cmd) }"
      >
        <span v-html="btn.icon"></span>
      </button>
      <div class="w-px h-5 bg-gray-200 mx-0.5"></div>
      <select
        @change="exec('formatBlock', ($event.target as HTMLSelectElement).value)"
        class="text-xs border-0 bg-transparent text-gray-600 focus:outline-none cursor-pointer"
      >
        <option value="p">正文</option>
        <option value="h2">标题2</option>
        <option value="h3">标题3</option>
        <option value="h4">标题4</option>
      </select>
    </div>

    <!-- 编辑区 -->
    <div
      ref="editorRef"
      contenteditable="true"
      class="min-h-32 p-3 text-sm text-gray-800 focus:outline-none prose prose-sm max-w-none"
      @input="onInput"
      @paste="onPaste"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const editorRef = ref<HTMLDivElement | null>(null)
const internalUpdate = ref(false)

const toolbar = [
  { cmd: 'bold', label: '加粗', icon: '<b>B</b>' },
  { cmd: 'italic', label: '斜体', icon: '<i>I</i>' },
  { cmd: 'underline', label: '下划线', icon: '<u>U</u>' },
  { cmd: 'strikeThrough', label: '删除线', icon: '<s>S</s>' },
  { cmd: 'insertUnorderedList', label: '无序列表', icon: '&#8226;&#8212;' },
  { cmd: 'insertOrderedList', label: '有序列表', icon: '1&#8212;' },
  { cmd: 'indent', label: '增加缩进', icon: '&#x21E5;' },
  { cmd: 'outdent', label: '减少缩进', icon: '&#x21E4;' },
  { cmd: 'removeFormat', label: '清除格式', icon: 'T&#x0338;' },
]

function exec(cmd: string, value?: string) {
  document.execCommand(cmd, false, value)
  editorRef.value?.focus()
  emitValue()
}

function isActive(cmd: string): boolean {
  try {
    return document.queryCommandState(cmd)
  } catch {
    return false
  }
}

function onInput() {
  emitValue()
}

function onPaste(e: ClipboardEvent) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text/plain') || ''
  document.execCommand('insertText', false, text)
}

function emitValue() {
  if (editorRef.value) {
    internalUpdate.value = true
    emit('update:modelValue', editorRef.value.innerHTML)
  }
}

onMounted(() => {
  if (editorRef.value && props.modelValue) {
    editorRef.value.innerHTML = props.modelValue
  }
})

watch(() => props.modelValue, (val) => {
  if (internalUpdate.value) {
    internalUpdate.value = false
    return
  }
  if (editorRef.value && editorRef.value.innerHTML !== val) {
    editorRef.value.innerHTML = val || ''
  }
})
</script>
