<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h2 class="text-xl font-bold text-white">站点设置</h2>
        <p class="text-gray-500 text-sm mt-1">管理联系方式、站点信息等全局配置</p>
      </div>
      <button
        @click="save"
        :disabled="saving"
        class="btn-primary px-6 py-2.5 text-sm disabled:opacity-50"
      >
        {{ saving ? '保存中...' : '保存设置' }}
      </button>
    </div>

    <div v-if="loading" class="text-gray-500 text-sm">加载中...</div>

    <div v-else class="space-y-6">
      <!-- 联系信息 -->
      <div class="card rounded-xl p-6">
        <h3 class="font-semibold text-white mb-4 flex items-center gap-2">
          <span class="text-primary">📧</span> 联系信息
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">商务合作邮箱</label>
            <input v-model="form.contact_business_email" type="email" class="input-dark" placeholder="business@example.com" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">技术支持邮箱</label>
            <input v-model="form.contact_support_email" type="email" class="input-dark" placeholder="support@example.com" />
          </div>
        </div>
      </div>

      <!-- 社区交流 -->
      <div class="card rounded-xl p-6">
        <h3 class="font-semibold text-white mb-4 flex items-center gap-2">
          <span class="text-primary">💬</span> 社区交流
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">微信号</label>
            <input v-model="form.contact_wechat_id" class="input-dark" placeholder="微信号" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">微信二维码</label>
            <div class="flex items-center gap-3">
              <div class="w-20 h-20 bg-dark-100 rounded-lg border border-white/[0.06] flex items-center justify-center overflow-hidden shrink-0">
                <img v-if="form.contact_wechat_qrcode" :src="form.contact_wechat_qrcode" class="w-full h-full object-contain" />
                <span v-else class="text-gray-600 text-2xl">💬</span>
              </div>
              <div class="flex-1 space-y-2">
                <label class="inline-flex items-center gap-2 px-3 py-1.5 text-xs bg-dark-100 border border-white/[0.06] rounded-lg cursor-pointer hover:bg-white/[0.04] transition-colors text-gray-300">
                  <span>{{ uploading ? '上传中...' : '上传二维码' }}</span>
                  <input type="file" accept="image/*" class="hidden" @change="handleQrcodeUpload" :disabled="uploading" />
                </label>
                <input v-model="form.contact_wechat_qrcode" class="input-dark text-xs" placeholder="或输入图片 URL" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 站点信息 -->
      <div class="card rounded-xl p-6">
        <h3 class="font-semibold text-white mb-4 flex items-center gap-2">
          <span class="text-primary">⚡</span> 站点信息
        </h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">站点名称</label>
            <input v-model="form.site_name" class="input-dark" placeholder="寻优" />
          </div>
          <div>
            <label class="block text-sm text-gray-400 mb-1">站点描述</label>
            <input v-model="form.site_description" class="input-dark" placeholder="全球机器人展示平台" />
          </div>
        </div>
      </div>
    </div>

    <!-- 保存状态 -->
    <Transition name="fade">
      <div v-if="successMsg" class="fixed bottom-6 right-6 bg-primary/10 border border-primary/20 text-primary text-sm px-5 py-3 rounded-xl shadow-lg">
        ✅ {{ successMsg }}
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import http from '@/services/api'

const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)
const successMsg = ref('')

const form = reactive<Record<string, string>>({
  contact_business_email: '',
  contact_support_email: '',
  contact_wechat_id: '',
  contact_wechat_qrcode: '',
  site_name: '',
  site_description: '',
})

async function load() {
  loading.value = true
  try {
    const res: any = await http.get('/settings')
    const data = res.data || res || {}
    Object.keys(form).forEach(k => {
      if (data[k] !== undefined) form[k] = data[k] || ''
    })
  } catch {}
  loading.value = false
}

async function save() {
  saving.value = true
  try {
    await http.put('/settings', { ...form })
    successMsg.value = '设置已保存'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch (e: any) {
    alert('保存失败: ' + (e?.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

async function handleQrcodeUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res: any = await http.post('/files/upload/admin', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.contact_wechat_qrcode = res.data?.url || ''
  } catch {
    alert('上传失败')
  } finally {
    uploading.value = false
    ;(e.target as HTMLInputElement).value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
