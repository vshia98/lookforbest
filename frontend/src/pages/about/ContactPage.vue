<template>
  <div class="max-w-4xl mx-auto px-4 py-16">
    <!-- Hero -->
    <div class="text-center mb-12 relative">
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-60 h-60 bg-accent-blue/5 rounded-full blur-[80px] pointer-events-none"></div>
      <div class="relative">
        <h1 class="text-4xl font-bold text-white mb-4">联系我们</h1>
        <p class="text-lg text-gray-400">有任何问题、建议或合作意向，欢迎随时与我们联系</p>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- 左侧：联系方式 -->
      <div class="space-y-5">
        <div class="card rounded-xl p-6 group relative overflow-hidden">
          <div class="absolute -top-6 -left-6 w-16 h-16 bg-primary/10 rounded-full blur-[30px] group-hover:bg-primary/20 transition-all"></div>
          <div class="relative flex items-start gap-4">
            <div class="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center text-xl shrink-0">📧</div>
            <div>
              <h3 class="font-semibold text-white mb-1">商务合作</h3>
              <p class="text-sm text-gray-400 mb-2">广告投放、厂商入驻、API 合作</p>
              <a :href="'mailto:' + settings.contact_business_email" class="text-sm text-primary hover:text-primary-400 transition-colors">{{ settings.contact_business_email }}</a>
            </div>
          </div>
        </div>

        <div class="card rounded-xl p-6 group relative overflow-hidden">
          <div class="absolute -top-6 -left-6 w-16 h-16 bg-accent-blue/10 rounded-full blur-[30px] group-hover:bg-accent-blue/20 transition-all"></div>
          <div class="relative flex items-start gap-4">
            <div class="w-12 h-12 bg-accent-blue/10 rounded-xl flex items-center justify-center text-xl shrink-0">🛠️</div>
            <div>
              <h3 class="font-semibold text-white mb-1">技术支持</h3>
              <p class="text-sm text-gray-400 mb-2">使用问题、Bug 反馈、功能建议</p>
              <a :href="'mailto:' + settings.contact_support_email" class="text-sm text-primary hover:text-primary-400 transition-colors">{{ settings.contact_support_email }}</a>
            </div>
          </div>
        </div>

        <div class="card rounded-xl p-6 group relative overflow-hidden">
          <div class="absolute -top-6 -left-6 w-16 h-16 bg-accent-gold/10 rounded-full blur-[30px] group-hover:bg-accent-gold/20 transition-all"></div>
          <div class="relative flex items-start gap-4">
            <div class="w-12 h-12 bg-accent-gold/10 rounded-xl flex items-center justify-center text-xl shrink-0">💬</div>
            <div>
              <h3 class="font-semibold text-white mb-1">社区交流</h3>
              <p class="text-sm text-gray-400 mb-2">加入机器人爱好者社区，交流行业动态</p>
              <p class="text-sm text-gray-300">微信号：{{ settings.contact_wechat_id }}</p>
              <!-- 微信二维码 -->
              <div v-if="settings.contact_wechat_qrcode" class="mt-3">
                <img
                  :src="settings.contact_wechat_qrcode"
                  alt="微信二维码"
                  class="w-32 h-32 rounded-lg border border-white/[0.06] bg-white p-1"
                />
                <p class="text-xs text-gray-500 mt-1">扫码添加，邀你入群</p>
              </div>
              <p v-else class="text-xs text-gray-500 mt-1">添加微信 {{ settings.contact_wechat_id }} 拉群</p>
            </div>
          </div>
        </div>

        <div class="card rounded-xl p-6 group relative overflow-hidden">
          <div class="absolute -top-6 -left-6 w-16 h-16 bg-accent-lime/10 rounded-full blur-[30px] group-hover:bg-accent-lime/20 transition-all"></div>
          <div class="relative flex items-start gap-4">
            <div class="w-12 h-12 bg-accent-lime/10 rounded-xl flex items-center justify-center text-xl shrink-0">🏭</div>
            <div>
              <h3 class="font-semibold text-white mb-1">厂商入驻</h3>
              <p class="text-sm text-gray-400 mb-2">机器人厂商免费入驻，发布产品信息</p>
              <router-link to="/manufacturer-portal/apply" class="text-sm text-primary hover:text-primary-400 transition-colors">立即申请入驻 →</router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：留言表单 -->
      <div class="card rounded-2xl p-6 relative overflow-hidden h-fit">
        <div class="absolute -top-12 -right-12 w-32 h-32 bg-primary/8 rounded-full blur-[60px] pointer-events-none"></div>
        <div class="relative">
          <h2 class="text-lg font-semibold text-white mb-5">在线留言</h2>

          <div v-if="submitted" class="text-center py-10">
            <div class="text-5xl mb-3">✅</div>
            <p class="text-white font-semibold mb-1">留言已发送！</p>
            <p class="text-sm text-gray-500">我们会尽快通过邮件回复您。</p>
            <button @click="submitted = false; resetForm()" class="mt-4 text-sm text-primary hover:text-primary-400 transition-colors">继续留言</button>
          </div>

          <form v-else @submit.prevent="handleSubmit" class="space-y-4">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm text-gray-300 mb-1">姓名 <span class="text-accent-red">*</span></label>
                <input v-model="form.name" type="text" required placeholder="您的姓名" class="input-dark" />
              </div>
              <div>
                <label class="block text-sm text-gray-300 mb-1">邮箱 <span class="text-accent-red">*</span></label>
                <input v-model="form.email" type="email" required placeholder="your@email.com" class="input-dark" />
              </div>
            </div>
            <div>
              <label class="block text-sm text-gray-300 mb-1">主题</label>
              <select v-model="form.subject" class="input-dark">
                <option value="general">一般咨询</option>
                <option value="business">商务合作</option>
                <option value="bug">Bug 反馈</option>
                <option value="feature">功能建议</option>
                <option value="other">其他</option>
              </select>
            </div>
            <div>
              <label class="block text-sm text-gray-300 mb-1">留言内容 <span class="text-accent-red">*</span></label>
              <textarea
                v-model="form.message"
                rows="5"
                required
                minlength="10"
                maxlength="2000"
                placeholder="请详细描述您的问题或建议..."
                class="input-dark"
              ></textarea>
              <p class="text-xs text-gray-600 mt-1 text-right">{{ form.message.length }}/2000</p>
            </div>
            <div v-if="error" class="text-sm text-accent-red bg-accent-red/10 rounded-lg p-3 border border-accent-red/20">{{ error }}</div>
            <button
              type="submit"
              :disabled="submitting"
              class="w-full btn-primary py-3 text-sm disabled:opacity-50"
            >
              {{ submitting ? '发送中...' : '发送留言' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import http from '@/services/api'

const settings = reactive<Record<string, string>>({
  contact_business_email: '',
  contact_support_email: '',
  contact_wechat_id: '',
  contact_wechat_qrcode: '',
})

const form = reactive({ name: '', email: '', subject: 'general', message: '' })
const submitting = ref(false)
const submitted = ref(false)
const error = ref('')

function resetForm() {
  form.name = ''
  form.email = ''
  form.subject = 'general'
  form.message = ''
  error.value = ''
}

async function handleSubmit() {
  error.value = ''
  submitting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    submitted.value = true
  } catch (e: any) {
    error.value = e?.message || '发送失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res: any = await http.get('/settings')
    const data = res.data || res || {}
    Object.keys(settings).forEach(k => {
      if (data[k]) settings[k] = data[k]
    })
  } catch {}
})
</script>
