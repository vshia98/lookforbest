<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-white">厂商管理</h2>
      <button
        @click="openForm(null)"
        class="bg-primary text-[#1a1a1a] text-sm px-4 py-2 rounded-lg hover:bg-primary transition-colors"
      >
        + 添加厂商
      </button>
    </div>

    <!-- 搜索 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.04] p-4 mb-5">
      <div class="flex gap-3">
        <input
          v-model="query"
          @keyup.enter="loadManufacturers"
          type="text"
          placeholder="搜索厂商名称..."
          class="input-dark flex-1"
        />
        <button
          @click="loadManufacturers"
          class="bg-primary text-[#1a1a1a] px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-400 transition-colors whitespace-nowrap"
        >
          搜索
        </button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.04]  overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center py-16">
        <div class="animate-spin w-6 h-6 border-2 border-primary-500 border-t-transparent rounded-full"></div>
      </div>

      <table v-else class="w-full text-sm">
        <thead>
          <tr class="border-b border-white/[0.04] bg-dark-100">
            <th class="text-left py-3 px-4 text-gray-500 font-medium">ID</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">厂商</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">英文名</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">国家</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">官网</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="m in manufacturers"
            :key="m.id"
            class="border-b border-white/[0.04] hover:bg-dark-100 transition-colors"
          >
            <td class="py-3 px-4 text-gray-400">#{{ m.id }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center gap-3">
                <img
                  v-if="m.logoUrl"
                  :src="m.logoUrl"
                  :alt="m.name"
                  class="w-8 h-8 object-contain rounded"
                />
                <div v-else class="w-8 h-8 bg-dark-100 rounded flex items-center justify-center text-gray-400 text-xs">🏭</div>
                <span class="font-medium text-white">{{ m.name }}</span>
              </div>
            </td>
            <td class="py-3 px-4 text-gray-400">{{ m.nameEn || '-' }}</td>
            <td class="py-3 px-4 text-gray-400">{{ m.country || '-' }}</td>
            <td class="py-3 px-4">
              <a v-if="m.websiteUrl" :href="m.websiteUrl" target="_blank" class="text-primary hover:underline text-xs">
                访问官网
              </a>
              <span v-else class="text-gray-400">-</span>
            </td>
            <td class="py-3 px-4">
              <div class="flex items-center gap-2">
                <button
                  @click="openForm(m)"
                  class="text-primary hover:text-primary text-xs px-2 py-1 rounded hover:bg-primary/10 transition-colors"
                >
                  编辑
                </button>
                <button
                  @click="deleteTarget = m"
                  class="text-red-500 hover:text-red-400 text-xs px-2 py-1 rounded hover:bg-red-900/20 transition-colors"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="manufacturers.length === 0">
            <td colspan="6" class="py-16 text-center text-gray-400">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div v-if="total > pageSize" class="flex items-center justify-between px-4 py-3 border-t border-white/[0.04]">
        <p class="text-sm text-gray-500">共 {{ total }} 条</p>
        <div class="flex items-center gap-2">
          <button :disabled="page <= 1" @click="page--; loadManufacturers()" class="px-3 py-1 text-sm border border-white/[0.06] rounded-lg disabled:opacity-40 hover:bg-dark-100 transition-colors">上一页</button>
          <span class="text-sm text-gray-500">{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="page++; loadManufacturers()" class="px-3 py-1 text-sm border border-white/[0.06] rounded-lg disabled:opacity-40 hover:bg-dark-100 transition-colors">下一页</button>
        </div>
      </div>
    </div>

    <!-- 编辑/创建弹窗 -->
    <div
      v-if="showForm"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      @click.self="showForm = false"
    >
      <div class="bg-dark-50 rounded-xl shadow-xl p-6 max-w-md w-full mx-4">
        <h3 class="font-bold text-white mb-5">{{ editTarget ? '编辑厂商' : '添加厂商' }}</h3>
        <form @submit.prevent="submitForm" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm text-gray-400 mb-1">中文名称 <span class="text-red-500">*</span></label>
              <input v-model="formData.name" required class="input-dark" />
            </div>
            <div>
              <label class="block text-sm text-gray-400 mb-1">英文名称</label>
              <input v-model="formData.nameEn" class="input-dark" />
            </div>
            <div>
              <label class="block text-sm text-gray-400 mb-1">国家/地区</label>
              <input v-model="formData.country" class="input-dark" />
            </div>
            <div>
              <label class="block text-sm text-gray-400 mb-1">官网URL</label>
              <input v-model="formData.websiteUrl" type="url" class="input-dark" placeholder="https://..." />
            </div>
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">Logo</label>
            <div class="flex items-center gap-3">
              <!-- 预览 -->
              <div class="w-14 h-14 bg-dark-100 rounded-lg border border-white/[0.06] flex items-center justify-center overflow-hidden shrink-0">
                <img v-if="formData.logoUrl" :src="formData.logoUrl" class="max-w-full max-h-full object-contain" />
                <span v-else class="text-gray-600 text-xl">🏭</span>
              </div>
              <div class="flex-1 space-y-2">
                <!-- 上传按钮 -->
                <label class="inline-flex items-center gap-2 px-3 py-1.5 text-xs bg-dark-100 border border-white/[0.06] rounded-lg cursor-pointer hover:bg-white/[0.04] transition-colors text-gray-300">
                  <span>{{ uploading ? '上传中...' : '选择图片' }}</span>
                  <input type="file" accept="image/*" class="hidden" @change="handleLogoUpload" :disabled="uploading" />
                </label>
                <!-- URL 手动输入 -->
                <input v-model="formData.logoUrl" type="text" class="input-dark text-xs" placeholder="或直接输入 URL" />
              </div>
            </div>
          </div>
          <p v-if="formError" class="text-red-500 text-sm">{{ formError }}</p>
          <div class="flex gap-3 justify-end pt-2">
            <button type="button" @click="showForm = false" class="px-4 py-2 text-sm border border-white/[0.06] rounded-lg hover:bg-dark-100 transition-colors">取消</button>
            <button type="submit" :disabled="formSubmitting" class="px-4 py-2 text-sm bg-primary text-[#1a1a1a] rounded-lg hover:bg-primary transition-colors disabled:opacity-50">
              {{ formSubmitting ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="deleteTarget" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="deleteTarget = null">
      <div class="bg-dark-50 rounded-xl shadow-xl p-6 max-w-sm w-full mx-4">
        <h3 class="font-bold text-white mb-2">确认删除</h3>
        <p class="text-gray-500 text-sm mb-6">确定要删除厂商「{{ deleteTarget.name }}」吗？</p>
        <div class="flex gap-3 justify-end">
          <button @click="deleteTarget = null" class="px-4 py-2 text-sm border border-white/[0.06] rounded-lg hover:bg-dark-100 transition-colors">取消</button>
          <button @click="confirmDelete" :disabled="deleting" class="px-4 py-2 text-sm bg-red-900/200 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { manufacturerService } from '@/services/manufacturers'
import http from '@/services/api'

const loading = ref(false)
const manufacturers = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const query = ref('')

const showForm = ref(false)
const editTarget = ref<any>(null)
const formSubmitting = ref(false)
const formError = ref('')
const uploading = ref(false)
const formData = reactive({ name: '', nameEn: '', country: '', websiteUrl: '', logoUrl: '' })

async function handleLogoUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res: any = await http.post('/files/upload/admin', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    formData.logoUrl = res.data?.url || res.url || ''
  } catch (err: any) {
    formError.value = '图片上传失败: ' + (err?.message || '未知错误')
  } finally {
    uploading.value = false
    // 清空 input 允许重复选择同一文件
    ;(e.target as HTMLInputElement).value = ''
  }
}

const deleteTarget = ref<any>(null)
const deleting = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize))

async function loadManufacturers() {
  loading.value = true
  try {
    const res = await manufacturerService.getList({ page: page.value - 1, size: pageSize, q: query.value || undefined })
    const paged = (res as any).data
    manufacturers.value = paged?.content ?? []
    total.value = paged?.total ?? 0
  } catch (e) {
    console.error('loadManufacturers error:', e)
    manufacturers.value = []
  } finally {
    loading.value = false
  }
}

function openForm(m: any) {
  editTarget.value = m
  formError.value = ''
  if (m) {
    Object.assign(formData, { name: m.name, nameEn: m.nameEn || '', country: m.country || '', websiteUrl: m.websiteUrl || '', logoUrl: m.logoUrl || '' })
  } else {
    Object.assign(formData, { name: '', nameEn: '', country: '', websiteUrl: '', logoUrl: '' })
  }
  showForm.value = true
}

async function submitForm() {
  formSubmitting.value = true
  formError.value = ''
  try {
    if (editTarget.value) {
      await manufacturerService.update(editTarget.value.id, formData)
    } else {
      await manufacturerService.create(formData)
    }
    showForm.value = false
    await loadManufacturers()
  } catch (e: any) {
    formError.value = e?.response?.data?.message || '保存失败'
  } finally {
    formSubmitting.value = false
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await manufacturerService.delete(deleteTarget.value.id)
    deleteTarget.value = null
    await loadManufacturers()
  } catch (e) {
    console.error(e)
  } finally {
    deleting.value = false
  }
}

onMounted(loadManufacturers)
</script>
