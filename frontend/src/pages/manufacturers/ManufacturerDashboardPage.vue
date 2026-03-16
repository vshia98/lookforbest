<template>
  <div class="max-w-5xl mx-auto py-10 px-4">
    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>

    <!-- 无权限 -->
    <div v-else-if="!manufacturer" class="text-center py-20">
      <p class="text-gray-500 text-lg mb-4">您尚未拥有厂商门户</p>
      <router-link
        to="/manufacturer-portal/apply"
        class="inline-block bg-primary text-[#1a1a1a] px-6 py-2.5 rounded-lg text-sm font-medium hover:bg-primary-400"
      >立即申请入驻</router-link>
    </div>

    <template v-else>
      <!-- 厂商信息头部 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-6 mb-6">
        <div class="flex items-start gap-4">
          <img
            v-if="manufacturer.logoUrl"
            :src="manufacturer.logoUrl"
            :alt="manufacturer.name"
            class="w-16 h-16 rounded-xl object-cover border border-white/[0.04]"
          />
          <div v-else class="w-16 h-16 rounded-xl bg-blue-900/30 flex items-center justify-center text-2xl font-bold text-blue-400">
            {{ manufacturer.name?.charAt(0) }}
          </div>
          <div class="flex-1">
            <div class="flex items-center gap-2 flex-wrap">
              <h1 class="text-xl font-bold text-white">{{ manufacturer.name }}</h1>
              <span v-if="manufacturer.nameEn" class="text-gray-400 text-sm">{{ manufacturer.nameEn }}</span>
              <span
                v-if="manufacturer.isVerified"
                class="inline-flex items-center gap-1 bg-blue-50 text-blue-400 text-xs font-medium px-2 py-0.5 rounded-full"
              >
                ✓ 已认证
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ manufacturer.country }}</p>
            <a
              v-if="manufacturer.websiteUrl"
              :href="manufacturer.websiteUrl"
              target="_blank"
              class="text-sm text-blue-400 hover:underline mt-0.5 inline-block"
            >{{ manufacturer.websiteUrl }}</a>
          </div>
          <router-link
            to="/manufacturer-portal/edit"
            class="text-sm text-blue-400 hover:text-blue-800 border border-blue-200 px-3 py-1.5 rounded-lg hover:bg-blue-50"
          >编辑资料</router-link>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5">
          <p class="text-sm text-gray-500 mb-1">机器人产品</p>
          <p class="text-3xl font-bold text-white">{{ manufacturer.robotCount }}</p>
        </div>
        <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5">
          <p class="text-sm text-gray-500 mb-1">收到询价</p>
          <p class="text-3xl font-bold text-white">{{ manufacturer.inquiryCount }}</p>
        </div>
        <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5">
          <p class="text-sm text-gray-500 mb-1">产品总浏览量</p>
          <p class="text-3xl font-bold text-white">{{ manufacturer.viewCount.toLocaleString() }}</p>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
        <router-link
          :to="`/manufacturers/${manufacturer.id}`"
          class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5 flex items-center gap-3 hover:border-blue-300 transition-colors"
        >
          <span class="text-2xl">🤖</span>
          <div>
            <p class="font-medium text-white text-sm">管理机器人</p>
            <p class="text-xs text-gray-400">查看和管理产品列表</p>
          </div>
        </router-link>
        <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5 flex items-center gap-3 cursor-not-allowed opacity-60">
          <span class="text-2xl">📧</span>
          <div>
            <p class="font-medium text-white text-sm">查看询价</p>
            <p class="text-xs text-gray-400">{{ manufacturer.inquiryCount }} 条询价</p>
          </div>
        </div>
        <router-link
          to="/manufacturer-portal/apply"
          class="bg-dark-50 rounded-xl border border-white/[0.06]  p-5 flex items-center gap-3 hover:border-blue-300 transition-colors"
        >
          <span class="text-2xl">✏️</span>
          <div>
            <p class="font-medium text-white text-sm">编辑资料</p>
            <p class="text-xs text-gray-400">更新公司信息</p>
          </div>
        </router-link>
      </div>

      <!-- 联系信息 -->
      <div class="bg-dark-50 rounded-xl border border-white/[0.06]  p-6">
        <h2 class="text-base font-semibold text-white mb-4">联系信息</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <div v-if="manufacturer.contactPerson">
            <span class="text-gray-500">联系人：</span>
            <span class="text-white">{{ manufacturer.contactPerson }}</span>
          </div>
          <div v-if="manufacturer.contactEmail">
            <span class="text-gray-500">邮箱：</span>
            <span class="text-white">{{ manufacturer.contactEmail }}</span>
          </div>
          <div v-if="manufacturer.contactPhone">
            <span class="text-gray-500">电话：</span>
            <span class="text-white">{{ manufacturer.contactPhone }}</span>
          </div>
          <div v-if="manufacturer.companySize">
            <span class="text-gray-500">公司规模：</span>
            <span class="text-white">{{ manufacturer.companySize }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { manufacturerPortalService } from '@/services/robots'

const loading = ref(true)
const manufacturer = ref<any>(null)

onMounted(async () => {
  try {
    const res = await manufacturerPortalService.getDashboard()
    manufacturer.value = res.data
  } catch {
    manufacturer.value = null
  } finally {
    loading.value = false
  }
})
</script>
