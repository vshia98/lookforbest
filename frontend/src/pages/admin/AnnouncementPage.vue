<template>
  <div class="max-w-2xl">
    <h2 class="text-xl font-bold text-gray-800 mb-6">发布系统公告</h2>

    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
      <p class="text-sm text-gray-500 mb-5">公告将以站内通知形式发送给所有已注册用户。</p>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">公告标题 <span class="text-red-500">*</span></label>
          <input
            v-model="form.title"
            type="text"
            maxlength="200"
            placeholder="请输入公告标题"
            class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">公告内容</label>
          <textarea
            v-model="form.content"
            rows="5"
            maxlength="2000"
            placeholder="请输入公告内容（可选）"
            class="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"
          ></textarea>
          <p class="text-xs text-gray-400 mt-1 text-right">{{ form.content.length }}/2000</p>
        </div>
        <div v-if="successMsg" class="text-sm text-green-600 bg-green-50 rounded-lg p-3">{{ successMsg }}</div>
        <div v-if="errorMsg" class="text-sm text-red-600 bg-red-50 rounded-lg p-3">{{ errorMsg }}</div>
        <button
          @click="submit"
          :disabled="submitting || !form.title.trim()"
          class="px-6 py-2.5 bg-primary-500 text-white rounded-xl text-sm font-medium hover:bg-primary-600 transition-colors disabled:opacity-50"
        >
          {{ submitting ? '发送中...' : '发布公告' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { notificationService } from '@/services/notifications'

const form = ref({ title: '', content: '' })
const submitting = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

async function submit() {
  if (!form.value.title.trim()) return
  successMsg.value = ''
  errorMsg.value = ''
  submitting.value = true
  try {
    await notificationService.broadcast(form.value.title, form.value.content || undefined)
    successMsg.value = '公告已成功发布给全部用户！'
    form.value = { title: '', content: '' }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '发送失败'
  } finally {
    submitting.value = false
  }
}
</script>
