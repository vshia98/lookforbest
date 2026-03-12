<template>
  <div class="max-w-2xl mx-auto py-10 px-4">
    <h1 class="text-2xl font-bold text-gray-800 mb-2">厂商入驻申请</h1>
    <p class="text-gray-500 text-sm mb-8">填写以下信息完成厂商认证入驻，审核通过后即可管理您的机器人产品。</p>

    <!-- 已有申请状态卡片 -->
    <div v-if="existingApplication" class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
      <div class="flex items-center gap-3 mb-4">
        <span v-if="existingApplication.status === 'approved'" class="text-2xl">✅</span>
        <span v-else-if="existingApplication.status === 'rejected'" class="text-2xl">❌</span>
        <span v-else class="text-2xl">⏳</span>
        <div>
          <h2 class="text-lg font-semibold text-gray-800">
            <span v-if="existingApplication.status === 'approved'">申请已通过</span>
            <span v-else-if="existingApplication.status === 'rejected'">申请已被拒绝</span>
            <span v-else>申请已提交，等待审核</span>
          </h2>
          <p class="text-sm text-gray-500">公司名称：{{ existingApplication.companyName }}</p>
        </div>
      </div>
      <div
        v-if="existingApplication.status === 'pending'"
        class="bg-yellow-50 border border-yellow-200 rounded-lg px-4 py-3 text-sm text-yellow-800"
      >
        您的申请正在审核中，我们会尽快处理并通过邮件通知您。
      </div>
      <div
        v-else-if="existingApplication.status === 'approved'"
        class="bg-green-50 border border-green-200 rounded-lg px-4 py-3 text-sm text-green-800"
      >
        恭喜！您的入驻申请已通过，现在可以
        <router-link to="/manufacturer-portal/dashboard" class="underline font-medium">进入厂商门户</router-link>
        管理您的产品。
      </div>
      <div
        v-else-if="existingApplication.status === 'rejected'"
        class="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-800"
      >
        <p class="font-medium mb-1">拒绝原因：</p>
        <p>{{ existingApplication.rejectReason || '暂无说明' }}</p>
        <button
          @click="existingApplication = null"
          class="mt-3 text-sm text-red-700 underline hover:text-red-900"
        >重新申请</button>
      </div>
      <p class="text-xs text-gray-400 mt-3">申请时间：{{ formatDate(existingApplication.createdAt) }}</p>
    </div>

    <!-- 申请表单 -->
    <div v-else class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
      <form @submit.prevent="handleSubmit" class="space-y-5">
        <!-- 公司名称 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">公司名称 <span class="text-red-500">*</span></label>
            <input
              v-model="form.companyName"
              type="text"
              required
              placeholder="请输入中文公司名称"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">英文名称</label>
            <input
              v-model="form.companyNameEn"
              type="text"
              placeholder="English company name"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <!-- 国家 & 官网 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">国家/地区 <span class="text-red-500">*</span></label>
            <select
              v-model="form.country"
              required
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">请选择</option>
              <option value="China">中国</option>
              <option value="United States">美国</option>
              <option value="Japan">日本</option>
              <option value="Germany">德国</option>
              <option value="South Korea">韩国</option>
              <option value="United Kingdom">英国</option>
              <option value="France">法国</option>
              <option value="Canada">加拿大</option>
              <option value="Australia">澳大利亚</option>
              <option value="Other">其他</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">官网地址</label>
            <input
              v-model="form.websiteUrl"
              type="url"
              placeholder="https://example.com"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <!-- 联系人 & 邮箱 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">联系人 <span class="text-red-500">*</span></label>
            <input
              v-model="form.contactPerson"
              type="text"
              required
              placeholder="请输入联系人姓名"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">联系邮箱 <span class="text-red-500">*</span></label>
            <input
              v-model="form.contactEmail"
              type="email"
              required
              placeholder="contact@example.com"
              class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <!-- 联系电话 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">联系电话</label>
          <input
            v-model="form.contactPhone"
            type="tel"
            placeholder="+86 138 0000 0000"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <!-- 营业执照 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">营业执照图片 URL</label>
          <input
            v-model="form.businessLicense"
            type="url"
            placeholder="https://your-cdn.com/license.jpg"
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p class="text-xs text-gray-400 mt-1">请上传营业执照图片至图床后填入链接地址</p>
        </div>

        <!-- 申请说明 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">申请说明</label>
          <textarea
            v-model="form.description"
            rows="4"
            placeholder="请简要介绍您的公司及产品，以便我们更快审核..."
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMsg" class="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-700">
          {{ errorMsg }}
        </div>

        <button
          type="submit"
          :disabled="submitting"
          class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium py-2.5 rounded-lg text-sm transition-colors"
        >
          <span v-if="submitting">提交中...</span>
          <span v-else>提交申请</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { manufacturerPortalService } from '@/services/robots'

const existingApplication = ref<any>(null)
const submitting = ref(false)
const errorMsg = ref('')

const form = ref({
  companyName: '',
  companyNameEn: '',
  country: '',
  websiteUrl: '',
  contactPerson: '',
  contactEmail: '',
  contactPhone: '',
  businessLicense: '',
  description: ''
})

onMounted(async () => {
  try {
    const res = await manufacturerPortalService.getMyApplication()
    if (res.data) {
      existingApplication.value = res.data
    }
  } catch {
    // 未登录或无申请记录，不做处理
  }
})

async function handleSubmit() {
  errorMsg.value = ''
  submitting.value = true
  try {
    const res = await manufacturerPortalService.apply(form.value)
    existingApplication.value = res.data
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
</script>
