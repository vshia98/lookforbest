<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <LoadingSpinner v-if="loading" :text="t('robot.loadingDetails') || '加载机器人信息...'" />

    <div v-else-if="!robot" class="text-center py-20">
      <div class="text-5xl mb-4">😕</div>
      <p class="text-gray-500">{{ t('robot.notFound') }}</p>
      <router-link to="/robots" class="text-primary hover:underline mt-4 block">
        {{ t('robot.backToList') }}
      </router-link>
    </div>

    <template v-else>
      <!-- 面包屑 -->
      <nav class="text-sm text-gray-500 mb-6 flex items-center gap-2">
        <router-link to="/" class="hover:text-primary transition-colors">
          {{ t('nav.home') }}
        </router-link>
        <span class="text-gray-600">/</span>
        <router-link to="/robots" class="hover:text-primary transition-colors">
          {{ t('nav.robots') }}
        </router-link>
        <span class="text-gray-600">/</span>
        <span class="text-gray-300">{{ displayName }}</span>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-10">
        <!-- 左：图片 -->
        <div>
          <div class="robot-showcase relative group rounded-2xl overflow-hidden aspect-video flex items-center justify-center">
            <!-- 底色 -->
            <div class="absolute inset-0 bg-dark-100"></div>
            <!-- 扫描线动画 -->
            <div class="absolute inset-0 scan-line opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <!-- 荧光色光晕 -->
            <div class="absolute -top-1/3 -left-1/4 w-2/3 h-2/3 bg-primary/20 rounded-full blur-[100px] group-hover:bg-primary/30 transition-all duration-700"></div>
            <div class="absolute -bottom-1/3 -right-1/4 w-2/3 h-2/3 bg-accent-blue/15 rounded-full blur-[100px] group-hover:bg-accent-blue/25 transition-all duration-700"></div>
            <!-- 网格 -->
            <div class="absolute inset-0 grid-bg"></div>
            <!-- 边框光效 -->
            <div class="absolute inset-0 rounded-2xl border border-primary/10 group-hover:border-primary/25 transition-colors duration-500"></div>
            <!-- 角标装饰 -->
            <div class="absolute top-3 left-3 w-4 h-4 border-t-2 border-l-2 border-primary/40 rounded-tl"></div>
            <div class="absolute top-3 right-3 w-4 h-4 border-t-2 border-r-2 border-primary/40 rounded-tr"></div>
            <div class="absolute bottom-3 left-3 w-4 h-4 border-b-2 border-l-2 border-primary/40 rounded-bl"></div>
            <div class="absolute bottom-3 right-3 w-4 h-4 border-b-2 border-r-2 border-primary/40 rounded-br"></div>
            <!-- 图片 -->
            <img
              v-if="currentImage"
              :src="currentImage"
              :alt="displayName"
              class="relative z-10 w-full h-full object-contain drop-shadow-[0_0_40px_rgba(0,255,209,0.1)] group-hover:drop-shadow-[0_0_60px_rgba(0,255,209,0.2)] group-hover:scale-[1.03] transition-all duration-700"
            />
            <div v-else class="relative z-10 text-7xl text-gray-600">🤖</div>
            <!-- 底部渐变 -->
            <div class="absolute bottom-0 left-0 right-0 h-1/3 bg-gradient-to-t from-dark-100/80 to-transparent z-10 pointer-events-none"></div>
          </div>
          <!-- 缩略图 -->
          <div v-if="robot.images.length > 1" class="flex gap-2 mt-3 overflow-x-auto pb-1">
            <button
              v-for="img in robot.images"
              :key="img.id"
              @click="currentImage = img.url"
              class="flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors"
              :class="currentImage === img.url ? 'border-primary' : 'border-white/10'"
            >
              <img :src="img.thumbnailUrl || img.url" :alt="img.altText || ''" class="w-full h-full object-cover" />
            </button>
          </div>
        </div>

        <!-- 右：基本信息 -->
        <div>
          <!-- 厂商 -->
          <router-link :to="{ name: 'manufacturer', params: { id: robot.manufacturer.id } }" class="flex items-center gap-2 mb-3">
            <img
              v-if="robot.manufacturer.logoUrl"
              :src="robot.manufacturer.logoUrl"
              class="h-6 w-auto brightness-90"
              :alt="displayManufacturerName"
            />
            <span class="text-sm text-gray-400 hover:text-primary transition-colors">
              {{ displayManufacturerName }}
            </span>
          </router-link>

          <h1 class="text-3xl font-bold text-white mb-1">{{ displayName }}</h1>
          <!-- 一句话简介：中英文切换，逻辑与列表卡片保持一致 -->
          <p
            v-if="currentLocale === 'en' && robot.subtitleEn"
            class="text-primary-400 text-sm mb-2"
          >
            {{ robot.subtitleEn }}
          </p>
          <p
            v-else-if="robot.subtitle"
            class="text-primary-400 text-sm mb-2"
          >
            {{ robot.subtitle }}
          </p>
          <p v-if="secondaryName" class="text-gray-500 mb-3">{{ secondaryName }}</p>

          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mb-5">
            <span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold bg-primary/15 text-primary border border-primary/20">
              {{ displayCategoryName }}
            </span>
            <span v-for="domain in robot.applicationDomains" :key="domain.id" class="tag">
              {{
                currentLocale === 'en'
                  ? (domain.nameEn || domain.name)
                  : domain.name
              }}
            </span>
            <template v-if="robot.tags && robot.tags.length > 0">
              <span v-for="(t, idx) in robot.tags" :key="idx" class="tag">
                {{
                  typeof t === 'string'
                    ? t
                    : currentLocale === 'en'
                      ? (t.nameEn || t.name)
                      : t.name
                }}
              </span>
            </template>
            <span
              v-if="openSourceLabel"
              class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-accent-lime/10 text-accent-lime border border-accent-lime/20"
            >
              {{ openSourceLabel }}
            </span>
            <span v-if="robot.releaseYear" class="tag">
              {{ currentLocale === 'en' ? robot.releaseYear : `${robot.releaseYear}年` }}
            </span>
            <span
              v-if="robot.listedDate"
              class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium bg-dark-100 text-gray-500"
            >
              {{ currentLocale === 'en' ? `Listed ${robot.listedDate}` : `收录 ${robot.listedDate}` }}
            </span>
          </div>

          <!-- 核心参数 -->
          <div class="grid grid-cols-2 gap-3 mb-6">
            <div v-if="robot.specs?.payloadKg" class="bg-dark-50 rounded-xl p-3 border border-white/[0.04]">
              <div class="text-lg font-bold text-primary-400">{{ robot.specs.payloadKg }} kg</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ t('robot.payload') }}</div>
            </div>
            <div v-if="robot.specs?.reachMm" class="bg-dark-50 rounded-xl p-3 border border-white/[0.04]">
              <div class="text-lg font-bold text-primary-400">{{ robot.specs.reachMm }} mm</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ t('robot.reach') }}</div>
            </div>
            <div v-if="robot.specs?.dof" class="bg-dark-50 rounded-xl p-3 border border-white/[0.04]">
              <div class="text-lg font-bold text-primary-400">{{ robot.specs.dof }} DOF</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ t('robot.dof') }}</div>
            </div>
            <div v-if="robot.specs?.repeatabilityMm" class="bg-dark-50 rounded-xl p-3 border border-white/[0.04]">
              <div class="text-lg font-bold text-primary-400">±{{ robot.specs.repeatabilityMm }} mm</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ t('robot.repeatability') }}</div>
            </div>
          </div>

          <!-- 价格 -->
          <div v-if="robot.priceRange && robot.priceRange !== 'inquiry'" class="mb-5 bg-dark-50 rounded-xl p-4 border border-primary/10">
            <div class="text-xs text-gray-500 mb-1">{{ t('robot.refPrice') }}</div>
            <div class="text-xl font-bold text-accent-gold">¥{{ robot.priceRange }}</div>
          </div>
          <div
            v-else-if="!isLoggedIn"
            class="mb-5 text-xs text-gray-500"
          >
            {{ t('robot.priceLoginHint') }}
          </div>

          <!-- 操作按钮 -->
          <div class="flex flex-wrap gap-3">
            <button
              @click="toggleFavorite"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl border text-sm font-medium transition-all"
              :class="isFavorited ? 'border-accent-red/50 text-accent-red bg-accent-red/10' : 'border-white/10 text-gray-400 hover:border-accent-red/30 hover:text-accent-red'"
            >
              {{ isFavorited ? '❤️ ' + t('robot.removeFromFavorites') : '🤍 ' + t('robot.addToFavorites') }}
            </button>
            <button
              @click="addToCompare"
              :disabled="isInCompare"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl border text-sm font-medium transition-all"
              :class="isInCompare ? 'border-primary/30 text-primary bg-primary/10 cursor-default' : 'border-white/10 text-gray-400 hover:border-primary/30 hover:text-primary'"
            >
              {{ isInCompare ? t('compare.title') : '+ ' + t('compare.addRobot') }}
            </button>
            <button
              @click="showInquiryModal = true"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-accent-orange text-white text-sm font-medium hover:brightness-110 transition-all"
            >
              📧 {{ t('robot.inquire') || '询价/联系厂商' }}
            </button>
            <a v-if="robot.manufacturer.websiteUrl" :href="robot.manufacturer.websiteUrl" target="_blank" rel="noopener"
              class="flex items-center gap-2 px-5 py-2.5 rounded-xl btn-primary text-sm">
              {{ t('manufacturer.website') }} →
            </a>
          </div>
        </div>
      </div>

      <!-- 3D 模型查看 -->
      <div class="mb-8">
        <div class="flex items-center gap-2 mb-3">
          <h2 class="font-semibold text-white">{{ t('robot.model3d') }}</h2>
          <span class="text-xs text-gray-500 bg-dark-50 border border-white/[0.04] rounded-full px-2 py-0.5">
            {{ t('robot.model3dHint') }}
          </span>
        </div>
        <ModelViewer :robotId="robot.id" :robotName="robot.name" />
      </div>

      <!-- 详细参数 + 侧栏 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 参数表格 -->
        <div class="lg:col-span-2 space-y-6">
          <div class="card rounded-2xl overflow-hidden">
            <div class="px-6 py-4 border-b border-white/[0.04]">
              <h2 class="font-semibold text-white">{{ t('robot.specs') }}</h2>
            </div>
            <template v-if="hasVisibleSpecs">
              <div class="px-6 pt-4 pb-5 space-y-4">
                <!-- 高亮核心参数 -->
                <div
                  v-if="highlightSpecs.length"
                  class="grid grid-cols-1 sm:grid-cols-2 gap-3"
                >
                  <div
                    v-for="([key, value], idx) in highlightSpecs"
                    :key="`${key}-${idx}`"
                    class="relative overflow-hidden rounded-xl border border-primary/25 bg-gradient-to-br from-primary/20 via-dark-50/90 to-dark-50 px-4 py-3 flex flex-col gap-1"
                  >
                    <span class="text-[11px] uppercase tracking-wide text-primary-100/80">
                      {{ specLabel(key) }}
                    </span>
                    <span class="text-xl font-semibold text-primary-100">
                      {{ value }}
                    </span>
                    <span class="pointer-events-none absolute -right-6 -top-6 h-16 w-16 rounded-full bg-primary/15 blur-2"></span>
                  </div>
                </div>

                <!-- 次级参数 -->
                <div
                  v-if="secondarySpecs.length"
                  class="border-t border-white/[0.04] pt-4 mt-1"
                >
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <div
                      v-for="([key, value], idx) in secondarySpecs"
                      :key="`${key}-secondary-${idx}`"
                      class="flex items-baseline justify-between gap-2 text-xs bg-dark-50/40 rounded-lg px-3 py-2 border border-white/[0.03]"
                    >
                      <span class="text-gray-500">
                        {{ specLabel(key) }}
                      </span>
                      <span class="text-gray-100 font-medium">
                        {{ value }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <div v-else class="px-6 py-4 text-xs text-gray-500">
              {{ t('robot.specsEmpty') }}
            </div>
            <!-- extraSpecs 详细规格表 -->
            <template v-if="robot.extraSpecs?.details?.length">
              <div class="px-6 py-3 border-t border-white/[0.04]">
                <h3 class="text-xs text-gray-500 font-medium">{{ t('robot.extraSpecsTitle') }}</h3>
              </div>
              <table class="w-full text-sm">
                <thead class="bg-dark-50 border-b border-white/[0.04] text-xs text-gray-500">
                  <tr>
                    <th class="px-6 py-2 text-left w-1/4">{{ t('robot.extraSpecsParam') }}</th>
                    <th class="px-6 py-2 text-left w-1/2">{{ t('robot.extraSpecsValue') }}</th>
                    <th class="px-6 py-2 text-left w-1/4">{{ t('robot.extraSpecsNote') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(row, i) in robot.extraSpecs.details" :key="i" class="border-b border-white/[0.04] last:border-0">
                    <td class="px-6 py-3 text-gray-500 w-1/4">{{ row['参数类别'] || row['参数'] }}</td>
                    <td class="px-6 py-3 font-medium text-primary-400">{{ row['具体规格'] || row['规格'] }}</td>
                    <td v-if="row['说明']" class="px-6 py-3 text-gray-500 text-xs">{{ row['说明'] }}</td>
                  </tr>
                </tbody>
              </table>
            </template>
          </div>

          <!-- 产品描述 -->
          <div v-if="robot.description" class="card rounded-2xl p-6">
            <h2 class="font-semibold text-white mb-3">{{ t('robot.description') }}</h2>
            <p class="text-sm text-gray-400 leading-relaxed">
              {{ currentLocale === 'en' ? (robot.descriptionEn || robot.description) : robot.description }}
            </p>
          </div>

          <!-- 产品介绍 -->
          <div v-if="robot.introduction || robot.introductionEn" class="card rounded-2xl p-6">
            <h2 class="font-semibold text-white mb-3">{{ t('robot.introduction') || t('robot.description') }}</h2>
            <p class="text-sm text-gray-400 leading-relaxed whitespace-pre-line">
              {{
                currentLocale === 'en'
                  ? (robot.introductionEn || robot.introduction)
                  : robot.introduction
              }}
            </p>
          </div>

          <!-- 应用场景 -->
          <div v-if="robot.applicationScenarios || robot.applicationScenariosEn" class="card rounded-2xl p-6">
            <h2 class="font-semibold text-white mb-3">{{ t('robot.scenarios') || '应用场景' }}</h2>
            <p class="text-sm text-gray-400 leading-relaxed whitespace-pre-line">
              {{
                currentLocale === 'en'
                  ? (robot.applicationScenariosEn || robot.applicationScenarios)
                  : robot.applicationScenarios
              }}
            </p>
          </div>

          <!-- 优势 -->
          <div v-if="robot.advantages || robot.advantagesEn" class="card rounded-2xl p-6">
            <h2 class="font-semibold text-white mb-3">{{ t('robot.advantages') || '产品优势' }}</h2>
            <p class="text-sm text-gray-400 leading-relaxed whitespace-pre-line">
              {{
                currentLocale === 'en'
                  ? (robot.advantagesEn || robot.advantages)
                  : robot.advantages
              }}
            </p>
          </div>
        </div>

        <!-- 侧栏 -->
        <div class="space-y-4">
          <!-- 厂商信息 -->
          <div class="card rounded-2xl p-5">
            <h3 class="font-semibold text-white mb-3">{{ t('manufacturer.info') || '厂商信息' }}</h3>
            <div class="flex items-center gap-3 mb-3">
              <img
                v-if="robot.manufacturer.logoUrl"
                :src="robot.manufacturer.logoUrl"
                class="h-8 w-auto brightness-90"
                :alt="displayManufacturerName"
              />
              <div>
                <p class="font-medium text-white text-sm">{{ displayManufacturerName }}</p>
                <p class="text-xs text-gray-500">
                  {{
                    currentLocale === 'en'
                      ? (robot.manufacturer.countryEn || robot.manufacturer.country)
                      : robot.manufacturer.country
                  }}
                </p>
              </div>
            </div>
            <router-link
              :to="{ name: 'manufacturer', params: { id: robot.manufacturer.id } }"
              class="w-full text-center block py-2 border border-white/10 rounded-lg text-sm text-gray-400 hover:border-primary/30 hover:text-primary transition-all mb-2"
            >
              {{ t('manufacturer.viewAllProducts') || '查看厂商全部产品' }}
            </router-link>
            <button
              @click="showInquiryModal = true"
              class="w-full py-2 bg-accent-orange text-white rounded-lg text-sm font-medium hover:brightness-110 transition-all"
            >
              📧 {{ t('robot.inquire') || '发起询价' }}
            </button>
          </div>

          <!-- 价格趋势 -->
          <PriceChart :robot-id="robot.id" />

          <!-- 资料文档 -->
          <div v-if="robot.documents.length > 0" class="card rounded-2xl p-5">
            <h3 class="font-semibold text-white mb-3">{{ t('robot.documents') }}</h3>
            <ul class="space-y-2">
              <li v-for="doc in robot.documents" :key="doc.id">
                <a
                  :href="doc.url"
                  target="_blank"
                  rel="noopener"
                  class="flex items-center gap-2 text-sm text-primary hover:text-primary-400 transition-colors"
                >
                  📄 {{ doc.title }}
                </a>
              </li>
            </ul>
          </div>
          <div
            v-else-if="!isLoggedIn"
            class="card rounded-2xl p-5"
          >
            <h3 class="font-semibold text-white mb-2">{{ t('robot.documents') }}</h3>
            <p class="text-xs text-gray-500">
              {{ t('robot.documentsLoginHint') }}
            </p>
          </div>

          <!-- 侧栏广告 -->
          <AdBanner position="detail_sidebar" />
        </div>
      </div>

      <!-- AI 智能推荐 -->
      <div class="mt-10">
        <div class="flex items-center gap-2 mb-5">
          <h2 class="text-xl font-bold text-white">{{ t('robot.recommendTitle') }}</h2>
          <span v-if="recommendStrategy" class="text-xs text-gray-500 bg-dark-50 border border-white/[0.04] rounded-full px-2 py-0.5">
            {{ strategyLabel(recommendStrategy) }}
          </span>
        </div>
        <div v-if="loadingRecommend" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <div v-for="i in 4" :key="i" class="h-48 bg-dark-50 rounded-xl animate-pulse border border-white/[0.04]"></div>
        </div>
        <div v-else-if="recommendations.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <RobotCard v-for="r in recommendations" :key="r.id" :robot="r" />
        </div>
        <div v-else-if="robot.similarRobots && robot.similarRobots.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-5">
          <RobotCard v-for="r in robot.similarRobots.slice(0, 4)" :key="r.id" :robot="r" />
        </div>
        <p v-else class="text-sm text-gray-500">{{ t('robot.noRecommend') }}</p>
      </div>
    </template>
  </div>

  <!-- 询价弹窗 -->
  <Teleport to="body">
    <div v-if="showInquiryModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm px-4" @click.self="showInquiryModal = false">
      <div class="card rounded-2xl shadow-2xl w-full max-w-lg p-6 relative border border-white/[0.06]">
        <button @click="showInquiryModal = false" class="absolute top-4 right-4 text-gray-500 hover:text-white text-xl leading-none transition-colors">&times;</button>
        <h2 class="text-lg font-bold text-white mb-1">{{ t('robot.inquire') || '发起询价' }}</h2>
        <p class="text-sm text-gray-500 mb-5">
          {{ currentLocale === 'en' ? 'Contact ' : '向 ' }}
          <strong class="text-gray-300">{{ displayManufacturerName }}</strong>
          {{ currentLocale === 'en' ? ' about ' : ' 咨询 ' }}
          {{ displayName }}
        </p>

        <!-- 成功提示 -->
        <div v-if="inquirySuccess" class="text-center py-8">
          <div class="text-5xl mb-3">✅</div>
          <p class="font-semibold text-white mb-1">
            {{ currentLocale === 'en' ? 'Inquiry submitted!' : '询价已提交！' }}
          </p>
          <p class="text-sm text-gray-500">
            {{ currentLocale === 'en' ? 'The manufacturer will contact you by email as soon as possible.' : '厂商将尽快通过邮件与您联系。' }}
          </p>
          <button @click="showInquiryModal = false; inquirySuccess = false" class="mt-4 btn-primary px-6 py-2 text-sm">
            {{ currentLocale === 'en' ? 'Close' : '关闭' }}
          </button>
        </div>

        <form v-else @submit.prevent="submitInquiry" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              {{ currentLocale === 'en' ? 'Inquiry message' : '询价内容' }}
              <span class="text-accent-red">*</span>
            </label>
            <textarea
              v-model="inquiryForm.message"
              rows="4"
              :placeholder="currentLocale === 'en'
                ? 'Describe your needs, e.g. quantity, application scenarios, budget range...'
                : '请描述您的需求，例如：采购数量、应用场景、期望价格区间等...'"
              class="input-dark resize-none"
              required
              minlength="10"
              maxlength="2000"
            ></textarea>
            <p class="text-xs text-gray-600 mt-1 text-right">{{ inquiryForm.message.length }}/2000</p>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">
                {{ currentLocale === 'en' ? 'Contact name' : '联系人姓名' }}
              </label>
              <input
                v-model="inquiryForm.contactName"
                type="text"
                :placeholder="currentLocale === 'en' ? 'Your name' : '您的姓名'"
                class="input-dark"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">
                {{ currentLocale === 'en' ? 'Email' : '联系邮箱' }}
                <span class="text-accent-red">*</span>
              </label>
              <input
                v-model="inquiryForm.contactEmail"
                type="email"
                placeholder="your@email.com"
                class="input-dark"
                required
              />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">
                {{ currentLocale === 'en' ? 'Phone' : '联系电话' }}
              </label>
              <input
                v-model="inquiryForm.contactPhone"
                type="tel"
                :placeholder="currentLocale === 'en' ? 'Mobile or landline' : '手机或固话'"
                class="input-dark"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">
                {{ currentLocale === 'en' ? 'Company' : '所在公司' }}
              </label>
              <input
                v-model="inquiryForm.contactCompany"
                type="text"
                :placeholder="currentLocale === 'en' ? 'Company name' : '公司名称'"
                class="input-dark"
              />
            </div>
          </div>
          <p v-if="inquiryError" class="text-sm text-accent-red">{{ inquiryError }}</p>
          <button
            type="submit"
            :disabled="inquirySubmitting"
            class="w-full py-3 bg-accent-orange text-white rounded-xl font-medium text-sm hover:brightness-110 transition-all disabled:opacity-50"
          >
            {{ inquirySubmitting
              ? (currentLocale === 'en' ? 'Submitting...' : '提交中...')
              : (currentLocale === 'en' ? 'Submit inquiry' : '提交询价') }}
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import LoadingSpinner from '@/components/ui/LoadingSpinner.vue'
import RobotCard from '@/components/robot/RobotCard.vue'
import ModelViewer from '@/components/robot/ModelViewer.vue'
import AdBanner from '@/components/ui/AdBanner.vue'
import PriceChart from '@/components/robot/PriceChart.vue'
import { robotService } from '@/services/robots'
import { inquiryService } from '@/services/inquiries'
import { useRobotStore } from '@/stores/robot'
import { useFavoritesStore } from '@/stores/favorites'
import { analytics } from '@/utils/analytics'
import { useSeo, buildRobotJsonLd } from '@/composables/useSeo'
import type { RobotDetail } from '@/types/robot'

const route = useRoute()
const robotStore = useRobotStore()
const favoritesStore = useFavoritesStore()
const { t, locale } = useI18n()

const robot = ref<RobotDetail | null>(null)
const loading = ref(true)
const currentImage = ref('')
const recommendations = ref<RobotDetail[]>([])
const recommendStrategy = ref('')
const loadingRecommend = ref(false)

const showInquiryModal = ref(false)
const inquirySuccess = ref(false)
const inquirySubmitting = ref(false)
const inquiryError = ref('')
const inquiryForm = ref({
  message: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  contactCompany: ''
})

const currentLocale = computed(() => (locale.value || 'zh') as 'zh' | 'en')
const isLoggedIn = computed(() => !!localStorage.getItem('access_token'))

const displayName = computed(() => {
  if (currentLocale.value === 'en' && robot.value?.nameEn) {
    return robot.value.nameEn
  }
  return robot.value?.name || ''
})

const secondaryName = computed(() => {
  if (currentLocale.value === 'en') return ''
  if (robot.value?.nameEn && robot.value.nameEn !== robot.value.name) {
    return robot.value.nameEn
  }
  return ''
})

const displayManufacturerName = computed(() => {
  const m = robot.value?.manufacturer as any
  if (!m) return ''
  if (currentLocale.value === 'en' && m.nameEn) return m.nameEn
  return m.name || ''
})

const displayCategoryName = computed(() => {
  const c = robot.value?.category as any
  if (!c) return ''
  if (currentLocale.value === 'en' && c.nameEn) return c.nameEn
  return c.name || ''
})

const openSourceLabel = computed(() => {
  const raw = robot.value?.isOpenSource
  if (!raw) return ''
  const text = String(raw).trim()
  if (currentLocale.value === 'en') {
    // 常见取值做一下中英映射，其余原样返回
    if (['开源', '是', '开放源码'].includes(text)) return 'Open-source'
    if (['闭源', '否', '商业版'].includes(text)) return 'Proprietary'
    if (['部分开源', '部分'].includes(text)) return 'Partially open-source'
    return text
  }
  return text
})

const seoMeta = computed(() => {
  const r = robot.value
  if (!r) return null
  const origin = window.location.origin
  const baseTitle = currentLocale.value === 'en'
    ? (r.nameEn || r.name)
    : r.name
  return {
    title: baseTitle,
    description: r.description?.slice(0, 155) || `查看 ${r.name} 机器人详情、参数规格及价格信息`,
    keywords: `${r.name},${r.nameEn || ''},机器人,${r.manufacturer?.name || ''},${r.category?.name || ''}`,
    ogTitle: r.nameEn || r.name,
    ogDescription: r.description?.slice(0, 155),
    ogImage: r.coverImageUrl || undefined,
    ogUrl: `${origin}/robots/${r.slug}`,
    canonicalUrl: `${origin}/robots/${r.slug}`,
    jsonLd: buildRobotJsonLd({
      name: r.name,
      nameEn: r.nameEn,
      description: r.description,
      coverImageUrl: r.coverImageUrl,
      manufacturer: r.manufacturer,
      releaseYear: r.releaseYear,
      priceUsdFrom: r.priceUsdFrom,
      slug: r.slug
    })
  }
})

useSeo(seoMeta)

function specLabel(key: string): string {
  // 使用国际化文案，并根据当前语言附加单位
  switch (key) {
    case 'payloadKg':
      return `${t('robot.payload')} (kg)`
    case 'reachMm':
      return `${t('robot.reach')} (mm)`
    case 'dof':
      return t('robot.dof')
    case 'repeatabilityMm':
      return `${t('robot.repeatability')} (mm)`
    case 'maxSpeedDegS':
      return currentLocale.value === 'en' ? 'Max speed (°/s)' : '最大速度 (°/s)'
    case 'weightKg':
      return `${t('robot.weight')} (kg)`
    case 'ipRating':
      return t('robot.ipRating')
    case 'mounting':
      return t('robot.mounting')
    case 'maxSpeedMs':
      return currentLocale.value === 'en' ? 'Max speed (m/s)' : '最大速度 (m/s)'
    case 'maxLoadKg':
      return currentLocale.value === 'en' ? 'Max load (kg)' : '最大载重 (kg)'
    case 'batteryLifeH':
      return currentLocale.value === 'en' ? 'Battery life (h)' : '续航时间 (h)'
    case 'navigationType':
      return currentLocale.value === 'en' ? 'Navigation type' : '导航方式'
    case 'heightMm':
      return currentLocale.value === 'en' ? 'Height (mm)' : '高度 (mm)'
    case 'walkingSpeedMs':
      return currentLocale.value === 'en' ? 'Walking speed (m/s)' : '行走速度 (m/s)'
    default:
      return key
  }
}

const visibleSpecs = computed(() => {
  if (!robot.value?.specs) return {}
  return Object.fromEntries(
    Object.entries(robot.value.specs).filter(([, v]) => v !== undefined && v !== null)
  )
})

const specPriority: Record<string, number> = {
  payloadKg: 100,
  reachMm: 90,
  dof: 85,
  maxLoadKg: 80,
  weightKg: 75,
  maxSpeedMs: 70,
  walkingSpeedMs: 68,
  maxSpeedDegS: 65,
  batteryLifeH: 60,
  heightMm: 55,
  repeatabilityMm: 50,
  ipRating: 40,
  navigationType: 35,
  mounting: 30,
}

const orderedSpecs = computed<[string, unknown][]>(() => {
  const v = visibleSpecs.value as Record<string, unknown> | undefined
  if (!v) return []
  return Object.entries(v).sort(
    (a, b) => (specPriority[b[0]] ?? 0) - (specPriority[a[0]] ?? 0)
  )
})

const highlightSpecs = computed(() => orderedSpecs.value.slice(0, 4))
const secondarySpecs = computed(() => orderedSpecs.value.slice(4))
const hasVisibleSpecs = computed(() => orderedSpecs.value.length > 0)

const isInCompare = computed(() => robot.value ? robotStore.compareList.some(r => r.id === robot.value!.id) : false)
const isFavorited = computed(() => robot.value ? favoritesStore.isFavorited(robot.value.id) : false)

function addToCompare() {
  if (robot.value) robotStore.addToCompare(robot.value as any)
}

function toggleFavorite() {
  if (robot.value) favoritesStore.toggle(robot.value.id)
}

function strategyLabel(s: string) {
  if (currentLocale.value === 'en') {
    const mapEn: Record<string, string> = {
      hybrid: 'AI hybrid',
      text: 'Text similarity',
      collab: 'Collaborative filtering',
      popular: 'Popular',
    }
    return mapEn[s] ?? s
  }
  const mapZh: Record<string, string> = {
    hybrid: 'AI混合推荐',
    text: '文本相似',
    collab: '协同过滤',
    popular: '热门推荐',
  }
  return mapZh[s] ?? s
}

async function submitInquiry() {
  if (!robot.value) return
  inquiryError.value = ''
  inquirySubmitting.value = true
  try {
    await inquiryService.create({
      robotId: robot.value.id,
      message: inquiryForm.value.message,
      contactName: inquiryForm.value.contactName || undefined,
      contactEmail: inquiryForm.value.contactEmail,
      contactPhone: inquiryForm.value.contactPhone || undefined,
      contactCompany: inquiryForm.value.contactCompany || undefined
    })
    inquirySuccess.value = true
    inquiryForm.value = { message: '', contactName: '', contactEmail: '', contactPhone: '', contactCompany: '' }
  } catch (e: any) {
    inquiryError.value = e?.response?.data?.message || '提交失败，请稍后重试'
  } finally {
    inquirySubmitting.value = false
  }
}

async function loadRecommendations(id: number, slug: string) {
  loadingRecommend.value = true
  try {
    const res: any = await robotService.getRecommend(id, 4)
    const data = res.data || res
    recommendations.value = Array.isArray(data) ? data : (data.items || [])
    recommendStrategy.value = data.strategy || 'popular'
    analytics.viewRobot(id, slug)
  } catch {
    recommendations.value = []
  } finally {
    loadingRecommend.value = false
  }
}

async function loadRobot(slug: string) {
  loading.value = true
  robot.value = null
  currentImage.value = ''
  recommendations.value = []
  try {
    const res = await robotService.getBySlug(slug)
    robot.value = res.data
    if (robot.value.images.length > 0) {
      currentImage.value = robot.value.images[0].url
    } else if (robot.value.coverImageUrl) {
      currentImage.value = robot.value.coverImageUrl
    }
    await loadRecommendations(robot.value.id, slug)
  } finally {
    loading.value = false
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => loadRobot(route.params.slug as string))

watch(() => route.params.slug, (newSlug) => {
  if (newSlug) loadRobot(newSlug as string)
})
</script>

<style scoped>
/* 网格背景 */
.grid-bg {
  background-image:
    linear-gradient(rgba(0, 255, 209, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 209, 0.03) 1px, transparent 1px);
  background-size: 30px 30px;
}

/* 扫描线动画 */
.scan-line::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00FFD1, transparent);
  box-shadow: 0 0 20px 4px rgba(0, 255, 209, 0.3);
  animation: scan 3s ease-in-out infinite;
}
@keyframes scan {
  0%, 100% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}
</style>
