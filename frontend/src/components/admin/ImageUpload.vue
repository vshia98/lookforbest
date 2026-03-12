<template>
  <div class="space-y-2">
    <!-- 已有图片预览 -->
    <div v-if="modelValue" class="relative inline-block">
      <img :src="modelValue" alt="预览" class="h-24 w-24 object-cover rounded-lg border border-gray-200" />
      <button
        type="button"
        @click="clear"
        class="absolute -top-1.5 -right-1.5 w-5 h-5 bg-red-500 text-white rounded-full text-xs flex items-center justify-center hover:bg-red-600"
      >×</button>
    </div>

    <!-- 上传区域 -->
    <div
      class="border-2 border-dashed rounded-lg p-4 text-center cursor-pointer transition-colors"
      :class="dragging ? 'border-primary-400 bg-primary-50' : 'border-gray-200 hover:border-gray-300'"
      @click="triggerInput"
      @dragover.prevent="dragging = true"
      @dragleave="dragging = false"
      @drop.prevent="onDrop"
    >
      <div v-if="uploading" class="flex items-center justify-center gap-2 text-sm text-gray-500">
        <div class="animate-spin w-4 h-4 border-2 border-primary-500 border-t-transparent rounded-full"></div>
        上传中...
      </div>
      <div v-else class="text-sm text-gray-400">
        <span class="text-primary-500 hover:text-primary-600">点击上传</span>
        或拖拽图片到此处
        <p class="text-xs mt-1 text-gray-300">支持 JPG、PNG、WebP，最大 10MB</p>
      </div>
    </div>

    <!-- 或手动输入 URL -->
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      type="url"
      placeholder="或直接输入图片 URL"
      class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
    />

    <p v-if="error" class="text-xs text-red-500">{{ error }}</p>

    <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="onFileChange" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import http from '@/services/api'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const fileInput = ref<HTMLInputElement | null>(null)
const dragging = ref(false)
const uploading = ref(false)
const error = ref('')

function triggerInput() {
  fileInput.value?.click()
}

function clear() {
  emit('update:modelValue', '')
}

async function upload(file: File) {
  if (!file.type.startsWith('image/')) {
    error.value = '仅支持图片文件'
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    error.value = '文件不能超过 10MB'
    return
  }
  error.value = ''
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res: any = await http.post('/files/upload/admin', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const url = res.data?.url || res.url
    if (url) emit('update:modelValue', url)
    else error.value = '上传失败：返回数据异常'
  } catch (e: any) {
    error.value = e?.response?.data?.message || '上传失败'
  } finally {
    uploading.value = false
  }
}

function onFileChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) upload(file)
}

function onDrop(event: DragEvent) {
  dragging.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) upload(file)
}
</script>
