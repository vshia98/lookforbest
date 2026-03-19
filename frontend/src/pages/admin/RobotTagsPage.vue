<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-white">标签管理</h2>
      <button
        class="px-4 py-2 text-sm bg-primary text-[#1a1a1a] rounded-lg hover:bg-primary transition-colors"
        @click="startCreate"
      >
        新增标签
      </button>
    </div>

    <div class="bg-dark-50 rounded-xl border border-white/[0.04] overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-dark-100 text-gray-400">
          <tr>
            <th class="px-4 py-2 text-left w-16">ID</th>
            <th class="px-4 py-2 text-left">中文名称</th>
            <th class="px-4 py-2 text-left">英文名称</th>
            <th class="px-4 py-2 text-left">Slug</th>
            <th class="px-4 py-2 text-right w-24">引用次数</th>
            <th class="px-4 py-2 text-left w-48">创建时间</th>
            <th class="px-4 py-2 text-right w-32">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="px-4 py-6 text-center text-gray-500">加载中...</td>
          </tr>
          <tr v-else-if="tags.length === 0">
            <td colspan="7" class="px-4 py-6 text-center text-gray-500">暂无标签</td>
          </tr>
          <tr
            v-for="tag in tags"
            :key="tag.id"
            class="border-t border-white/[0.04] hover:bg-dark-100"
          >
            <td class="px-4 py-2 text-gray-500">#{{ tag.id }}</td>
            <td class="px-4 py-2 text-white">
              {{ tag.name }}
            </td>
            <td class="px-4 py-2 text-gray-300">
              {{ tag.nameEn || '-' }}
            </td>
            <td class="px-4 py-2 text-xs text-gray-500">
              {{ tag.slug }}
            </td>
            <td class="px-4 py-2 text-right text-gray-300">
              {{ tag.usageCount ?? 0 }}
            </td>
            <td class="px-4 py-2 text-xs text-gray-500">
              {{ formatDate(tag.createdAt) }}
            </td>
            <td class="px-4 py-2 text-right">
              <button
                class="text-xs text-primary hover:underline mr-3"
                @click="startEdit(tag)"
              >
                编辑
              </button>
              <button
                class="text-xs text-accent-red hover:underline"
                @click="confirmDelete(tag)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 弹窗：新增/编辑 -->
    <div v-if="showDialog" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="bg-dark-100 rounded-xl border border-white/[0.08] w-full max-w-md p-6">
        <h3 class="text-lg font-semibold text-white mb-4">
          {{ editingTag?.id ? '编辑标签' : '新增标签' }}
        </h3>

        <div class="space-y-4">
          <div>
            <label class="block text-xs text-gray-400 mb-1">中文名称</label>
            <input v-model="form.name" class="input-dark w-full" />
          </div>
          <div>
            <label class="block text-xs text-gray-400 mb-1">英文名称</label>
            <input v-model="form.nameEn" class="input-dark w-full" />
          </div>
          <div>
            <label class="block text-xs text-gray-400 mb-1">Slug（可选，如不填将沿用现有）</label>
            <input v-model="form.slug" class="input-dark w-full" />
          </div>
        </div>

        <p v-if="error" class="text-xs text-accent-red mt-3">{{ error }}</p>

        <div class="flex justify-end gap-3 mt-6">
          <button class="px-4 py-2 text-sm text-gray-400 hover:text-white" @click="closeDialog">
            取消
          </button>
          <button
            class="px-4 py-2 text-sm bg-primary text-[#1a1a1a] rounded-lg hover:bg-primary transition-colors disabled:opacity-60"
            :disabled="submitting"
            @click="save"
          >
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="deleteTarget" class="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
      <div class="bg-dark-100 rounded-xl border border-white/[0.08] w-full max-w-sm p-6">
        <h3 class="text-lg font-semibold text-white mb-2">删除标签</h3>
        <p class="text-sm text-gray-400 mb-4">
          确认删除标签
          <span class="font-semibold text-white">{{ deleteTarget.name }}</span>
          吗？已关联的机器人将不再显示该标签。
        </p>
        <div class="flex justify-end gap-3">
          <button class="px-4 py-2 text-sm text-gray-400 hover:text-white" @click="deleteTarget = null">
            取消
          </button>
          <button
            class="px-4 py-2 text-sm bg-accent-red text-white rounded-lg hover:brightness-110 transition-colors disabled:opacity-60"
            :disabled="submitting"
            @click="doDelete"
          >
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import http from '@/services/api'

interface RobotTagAdmin {
  id: number
  name: string
  nameEn?: string | null
  slug: string
  usageCount?: number
  createdAt?: string
}

const tags = ref<RobotTagAdmin[]>([])
const loading = ref(false)
const showDialog = ref(false)
const submitting = ref(false)
const error = ref('')
const editingTag = ref<RobotTagAdmin | null>(null)
const deleteTarget = ref<RobotTagAdmin | null>(null)

const form = reactive({
  name: '',
  nameEn: '',
  slug: ''
})

function resetForm() {
  form.name = ''
  form.nameEn = ''
  form.slug = ''
  error.value = ''
}

function startCreate() {
  editingTag.value = null
  resetForm()
  showDialog.value = true
}

function startEdit(tag: RobotTagAdmin) {
  editingTag.value = tag
  form.name = tag.name
  form.nameEn = tag.nameEn || ''
  form.slug = tag.slug || ''
  error.value = ''
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function confirmDelete(tag: RobotTagAdmin) {
  deleteTarget.value = tag
}

async function loadTags() {
  loading.value = true
  try {
    const res = await http.get<any, any>('/admin/robot-tags')
    tags.value = res.data || res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.name.trim()) {
    error.value = '中文名称不能为空'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const payload: any = {
      name: form.name.trim(),
      nameEn: form.nameEn.trim() || null,
      slug: form.slug.trim() || undefined
    }
    if (editingTag.value?.id) {
      payload.id = editingTag.value.id
    }
    await http.post('/admin/robot-tags', payload)
    showDialog.value = false
    await loadTags()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '保存失败，请重试'
  } finally {
    submitting.value = false
  }
}

async function doDelete() {
  if (!deleteTarget.value) return
  submitting.value = true
  try {
    await http.delete(`/admin/robot-tags/${deleteTarget.value.id}`)
    deleteTarget.value = null
    await loadTags()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

function formatDate(value?: string) {
  if (!value) return ''
  try {
    const d = new Date(value)
    if (Number.isNaN(d.getTime())) return value
    return d.toLocaleString()
  } catch {
    return value
  }
}

onMounted(loadTags)
</script>

<style scoped>
.input-dark {
  @apply w-full px-3 py-2 rounded-lg bg-dark-200 border border-white/[0.06] text-sm text-gray-100 outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary/40;
}
</style>

