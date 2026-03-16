<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-white">爬虫管理</h1>
        <p class="text-gray-500 text-sm mt-1">配置化数据源爬取与监控</p>
      </div>
      <div class="flex items-center gap-3">
        <button
          @click="runAll"
          :disabled="runningAll"
          class="px-4 py-2 bg-green-600 text-[#1a1a1a] rounded-lg text-sm hover:bg-green-700 disabled:opacity-50"
        >
          {{ runningAll ? '运行中...' : '运行所有' }}
        </button>
        <button
          @click="openCreateModal"
          class="px-4 py-2 bg-primary text-[#1a1a1a] rounded-lg text-sm hover:bg-primary-400"
        >
          + 新增数据源
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="bg-dark-50 rounded-xl border border-white/[0.06] p-4">
        <div class="text-2xl font-bold text-white">{{ sources.length }}</div>
        <div class="text-sm text-gray-500 mt-1">数据源总数</div>
      </div>
      <div class="bg-dark-50 rounded-xl border border-white/[0.06] p-4">
        <div class="text-2xl font-bold text-green-600">{{ activeSources }}</div>
        <div class="text-sm text-gray-500 mt-1">启用数量</div>
      </div>
      <div class="bg-dark-50 rounded-xl border border-white/[0.06] p-4">
        <div class="text-2xl font-bold text-blue-400">{{ runningCount }}</div>
        <div class="text-sm text-gray-500 mt-1">正在运行</div>
      </div>
    </div>

    <!-- 数据源列表 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.06] mb-6">
      <div class="p-4 border-b border-white/[0.04] font-semibold text-gray-300">数据源列表</div>
      <div v-if="loading" class="p-8 text-center text-gray-400">加载中...</div>
      <div v-else-if="sources.length === 0" class="p-8 text-center text-gray-400">暂无数据源</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-dark-100">
          <tr>
            <th class="text-left px-4 py-3 text-gray-400 font-medium">名称</th>
            <th class="text-left px-4 py-3 text-gray-400 font-medium">URL</th>
            <th class="text-left px-4 py-3 text-gray-400 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-400 font-medium">上次运行</th>
            <th class="text-left px-4 py-3 text-gray-400 font-medium">累计爬取</th>
            <th class="text-right px-4 py-3 text-gray-400 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-white/[0.04]">
          <tr v-for="s in sources" :key="s.id" class="hover:bg-dark-100">
            <td class="px-4 py-3">
              <div class="font-medium text-white">{{ s.name }}</div>
              <div v-if="s.description" class="text-xs text-gray-400 mt-0.5 line-clamp-1">{{ s.description }}</div>
            </td>
            <td class="px-4 py-3 text-gray-400 max-w-xs truncate">{{ s.url_pattern }}</td>
            <td class="px-4 py-3">
              <span v-if="s.is_running" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-blue-900/30 text-blue-400">
                <span class="w-1.5 h-1.5 bg-blue-900/200 rounded-full animate-pulse" />运行中
              </span>
              <span v-else-if="s.last_crawl_status === 'success'" class="px-2 py-0.5 rounded-full text-xs bg-green-900/30 text-green-400">成功</span>
              <span v-else-if="s.last_crawl_status === 'failed'" class="px-2 py-0.5 rounded-full text-xs bg-red-900/30 text-red-400">失败</span>
              <span v-else-if="!s.is_active" class="px-2 py-0.5 rounded-full text-xs bg-dark-100 text-gray-500">已禁用</span>
              <span v-else class="px-2 py-0.5 rounded-full text-xs bg-dark-100 text-gray-400">待运行</span>
            </td>
            <td class="px-4 py-3 text-gray-500 text-xs">{{ s.last_crawl_at ? formatDate(s.last_crawl_at) : '-' }}</td>
            <td class="px-4 py-3 text-gray-300">{{ s.total_crawled || 0 }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex justify-end gap-2">
                <button
                  @click="triggerRun(s)"
                  :disabled="s.is_running"
                  class="px-2 py-1 text-xs bg-green-900/20 text-green-400 border border-green-800 rounded hover:bg-green-900/30 disabled:opacity-40"
                >运行</button>
                <button
                  @click="editSource(s)"
                  class="px-2 py-1 text-xs bg-blue-900/20 text-blue-400 border border-blue-800 rounded hover:bg-blue-900/30"
                >编辑</button>
                <button
                  @click="toggleActive(s)"
                  class="px-2 py-1 text-xs rounded border"
                  :class="s.is_active ? 'bg-yellow-900/20 text-yellow-400 border-yellow-800 hover:bg-yellow-900/30' : 'bg-dark-100 text-gray-400 border-white/[0.06] hover:bg-dark-100'"
                >{{ s.is_active ? '禁用' : '启用' }}</button>
                <button
                  @click="confirmDelete(s)"
                  class="px-2 py-1 text-xs bg-red-900/20 text-red-400 border border-red-800 rounded hover:bg-red-900/30"
                >删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 运行日志 -->
    <div class="bg-dark-50 rounded-xl border border-white/[0.06]">
      <div class="p-4 border-b border-white/[0.04] flex items-center justify-between">
        <span class="font-semibold text-gray-300">运行日志</span>
        <button @click="loadLogs" class="text-xs text-primary hover:underline">刷新</button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-dark-100">
            <tr>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">数据源</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">开始时间</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">结束时间</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">状态</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">页数</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">采集/写入/跳过</th>
              <th class="text-left px-4 py-3 text-gray-400 font-medium">触发方式</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/[0.04]">
            <tr v-for="log in logs" :key="log.id" class="hover:bg-dark-100">
              <td class="px-4 py-3 font-medium text-white">{{ log.source_name }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">{{ formatDate(log.started_at) }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">{{ log.finished_at ? formatDate(log.finished_at) : '-' }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs"
                  :class="{
                    'bg-green-900/30 text-green-400': log.status === 'success',
                    'bg-red-900/30 text-red-400': log.status === 'failed',
                    'bg-blue-900/30 text-blue-400': log.status === 'running',
                    'bg-dark-100 text-gray-400': log.status === 'cancelled'
                  }"
                >{{ log.status }}</span>
              </td>
              <td class="px-4 py-3 text-gray-400">{{ log.pages_crawled }}</td>
              <td class="px-4 py-3 text-gray-400">{{ log.items_crawled }} / {{ log.items_upserted }} / {{ log.items_skipped }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">{{ log.triggered_by }}</td>
            </tr>
            <tr v-if="logs.length === 0">
              <td colspan="7" class="px-4 py-8 text-center text-gray-400">暂无运行记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showCreateModal || editingSource" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-dark-50 rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-white/[0.04]">
          <h2 class="text-lg font-bold text-white">{{ editingSource ? '编辑数据源' : '新增数据源' }}</h2>
        </div>
        <form @submit.prevent="submitSource" class="p-6 space-y-4">
          <!-- 配置模板选择器 -->
          <div v-if="!editingSource">
            <label class="block text-sm font-medium text-gray-300 mb-1">快速模板</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="tpl in configTemplates" :key="tpl.id"
                type="button"
                @click="applyTemplate(tpl)"
                class="px-3 py-1.5 text-xs rounded-lg border transition-colors"
                :class="activeTemplate === tpl.id
                  ? 'bg-primary/10 text-primary border-primary/30'
                  : 'bg-dark-100 text-gray-400 border-white/[0.06] hover:bg-dark-100'"
              >{{ tpl.label }}</button>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">名称 *</label>
            <input v-model="form.name" required class="input-dark" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">描述</label>
            <input v-model="form.description" class="input-dark" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">起始URL（多个用逗号分隔）*</label>
            <textarea v-model="form.url_pattern" required rows="2" class="input-dark" />
          </div>

          <!-- Cron 表达式可视化选择器 -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">调度频率</label>
              <select
                v-model="selectedCronPreset"
                @change="applyCronPreset"
                class="input-dark"
              >
                <option v-for="p in cronPresets" :key="p.value" :value="p.value">{{ p.label }}</option>
              </select>
              <div class="flex items-center gap-2 mt-2">
                <input
                  v-model="form.cron_expr"
                  class="input-dark text-xs font-mono"
                  placeholder="0 0 3 * * ?"
                />
                class="text-xs text-gray-400 whitespace-nowrap"
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1">最大页数</label>
              <input v-model.number="form.max_pages" type="number" min="1" max="200" class="input-dark" />
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-1">
              <label class="block text-sm font-medium text-gray-300">
                爬取规则 JSON *
                <span class="text-xs font-normal text-gray-400 ml-1">(manufacturer, list_page, detail_fields)</span>
              </label>
              <div class="flex items-center gap-2">
                <!-- 可视化选择器按钮 -->
                <button
                  type="button"
                  @click="openSelectorTool"
                  :disabled="!form.url_pattern"
                  class="px-3 py-1 text-xs bg-purple-900/20 text-purple-400 border border-purple-200 rounded-lg hover:bg-purple-900/30 disabled:opacity-40 transition-colors"
                >可视化选择</button>
                <!-- 预览按钮 -->
                <button
                  type="button"
                  @click="runPreview"
                  :disabled="previewing || !form.url_pattern"
                  class="px-3 py-1 text-xs bg-amber-900/20 text-amber-400 border border-amber-200 rounded-lg hover:bg-amber-100 disabled:opacity-40 transition-colors"
                >{{ previewing ? '抓取中...' : '预览提取结果' }}</button>
              </div>
            </div>
            <textarea
              v-model="form.configText"
              required rows="12"
              class="input-dark text-xs font-mono"
            />
            class="text-red-500 text-xs mt-1"
          </div>

          <!-- 预览结果面板 -->
          <div v-if="previewResult" class="border border-amber-200 rounded-xl bg-amber-900/20/50 overflow-hidden">
            <div class="px-4 py-2.5 bg-amber-100/60 border-b border-amber-200 flex items-center justify-between">
              <span class="text-sm font-medium text-amber-300">预览结果</span>
              <button type="button" @click="previewResult = null" class="text-amber-500 hover:text-amber-400 text-xs">关闭</button>
            </div>
            <div class="p-4 space-y-3 text-sm">
              <div class="text-xs text-gray-500">
                页面: {{ previewResult.page_title || '(无标题)' }} | HTML {{ (previewResult.html_length / 1024).toFixed(1) }}KB
              </div>
              <!-- 字段提取结果 -->
              <div v-if="Object.keys(previewResult.fields).length > 0">
                <div class="text-xs font-medium text-gray-400 mb-1">字段提取:</div>
                <div class="grid grid-cols-1 gap-1">
                  <div v-for="(val, key) in previewResult.fields" :key="key" class="flex items-start gap-2 text-xs">
                    <span class="font-mono text-amber-400 min-w-[120px] shrink-0">{{ key }}:</span>
                    <span :class="val ? 'text-white' : 'text-red-400 italic'">{{ val || '(空 - 选择器未匹配)' }}</span>
                  </div>
                </div>
              </div>
              <!-- 列表URL提取结果 -->
              <div v-if="previewResult.list_urls.length > 0">
                <div class="text-xs font-medium text-gray-400 mb-1">列表页提取到 {{ previewResult.list_urls.length }} 个链接:</div>
                <div class="max-h-32 overflow-y-auto space-y-0.5">
                  <div v-for="(u, i) in previewResult.list_urls" :key="i" class="text-xs text-gray-400 font-mono truncate">{{ u }}</div>
                </div>
              </div>
              <div v-if="Object.keys(previewResult.fields).length === 0 && previewResult.list_urls.length === 0"
                class="text-xs text-red-500">未提取到任何数据，请检查选择器配置</div>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <input v-model="form.is_active" type="checkbox" id="is_active" class="rounded" />
            <label for="is_active" class="text-sm text-gray-300">启用</label>
          </div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeModal" class="px-4 py-2 border border-white/[0.06] rounded-lg text-sm text-gray-400 hover:bg-dark-100">取消</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 bg-primary text-[#1a1a1a] rounded-lg text-sm hover:bg-primary-400 disabled:opacity-50">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import http from '@/services/api'

const sources = ref<any[]>([])
const logs = ref<any[]>([])
const loading = ref(false)
const runningAll = ref(false)
const saving = ref(false)
const showCreateModal = ref(false)
const editingSource = ref<any>(null)
const configError = ref('')
const runningCount = ref(0)
const previewing = ref(false)
const previewResult = ref<any>(null)
const activeTemplate = ref('')
const selectedCronPreset = ref('custom')

const activeSources = computed(() => sources.value.filter(s => s.is_active).length)

// ─────────────────── 配置模板 ───────────────────
const configTemplates = [
  {
    id: 'product-list',
    label: '产品列表页 (列表→详情)',
    config: {
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      list_page: { selector: 'a.product-link', url_attr: 'href', next_page_selector: '', next_page_attr: 'href' },
      detail_fields: {
        name: { selector: 'h1' },
        name_en: { selector: 'h1' },
        description: { selector: 'div.description' },
        cover_image_url: { selector: 'img.main-image', attr: 'src' },
        category: { value: '工业机器人' },
        price_range: { value: 'inquiry' }
      }
    }
  },
  {
    id: 'single-product',
    label: '单产品页 (直接提取)',
    config: {
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      detail_fields: {
        name: { selector: 'h1' },
        name_en: { selector: 'h1' },
        description: { selector: 'div.product-description, div.description, meta[name="description"]', attr: 'content' },
        cover_image_url: { selector: 'img.product-image, img.hero, img.main', attr: 'src' },
        payload_kg: { selector: 'td.payload, span.payload', regex: '([\\d.]+)' },
        reach_mm: { selector: 'td.reach, span.reach', regex: '([\\d]+)' },
        dof: { selector: 'td.dof, span.dof', regex: '([\\d]+)' },
        weight_kg: { selector: 'td.weight, span.weight', regex: '([\\d.]+)' },
        category: { value: '工业机器人' },
        price_range: { value: 'inquiry' }
      }
    }
  },
  {
    id: 'humanoid',
    label: '人形机器人',
    config: {
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      detail_fields: {
        name: { selector: 'h1' },
        name_en: { selector: 'h1' },
        description: { selector: 'div.product-description, section.overview p' },
        cover_image_url: { selector: 'img.hero, img.product-image', attr: 'src' },
        dof: { selector: 'td:contains("DOF") + td, span.dof', regex: '([\\d]+)' },
        weight_kg: { selector: 'td:contains("Weight") + td, td:contains("重量") + td', regex: '([\\d.]+)' },
        category: { value: '人形机器人' },
        price_range: { value: 'inquiry' }
      }
    }
  },
  {
    id: 'quadruped',
    label: '四足机器人',
    config: {
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      detail_fields: {
        name: { selector: 'h1' },
        name_en: { selector: 'h1' },
        description: { selector: 'div.product-description, section.overview p' },
        cover_image_url: { selector: 'img.hero, img.product-image', attr: 'src' },
        payload_kg: { selector: 'td:contains("Payload") + td, td:contains("负载") + td', regex: '([\\d.]+)' },
        weight_kg: { selector: 'td:contains("Weight") + td, td:contains("重量") + td', regex: '([\\d.]+)' },
        category: { value: '四足机器人' },
        price_range: { value: 'inquiry' }
      }
    }
  },
  {
    id: 'collaborative',
    label: '协作机器人 (Cobot)',
    config: {
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      list_page: { selector: 'a.product-link, a.product-card', url_attr: 'href' },
      detail_fields: {
        name: { selector: 'h1' },
        name_en: { selector: 'h1' },
        description: { selector: 'div.product-description, div.overview' },
        cover_image_url: { selector: 'img.product-image', attr: 'src' },
        payload_kg: { selector: 'td:contains("Payload") + td', regex: '([\\d.]+)' },
        reach_mm: { selector: 'td:contains("Reach") + td', regex: '([\\d]+)' },
        dof: { selector: 'td:contains("Axes") + td, td:contains("DOF") + td', regex: '([\\d]+)' },
        repeatability_mm: { selector: 'td:contains("Repeatability") + td', regex: '([\\d.]+)' },
        category: { value: '协作机器人' },
        price_range: { value: 'inquiry' }
      }
    }
  }
]

// ─────────────────── Cron 预设 ───────────────────
const cronPresets = [
  { value: 'custom', label: '自定义', expr: '' },
  { value: 'daily-2am', label: '每天凌晨 2:00', expr: '0 0 2 * * ?' },
  { value: 'daily-3am', label: '每天凌晨 3:00', expr: '0 0 3 * * ?' },
  { value: 'daily-5am', label: '每天凌晨 5:00', expr: '0 0 5 * * ?' },
  { value: 'twice-daily', label: '每天 3:00 和 15:00', expr: '0 0 3,15 * * ?' },
  { value: 'every-6h', label: '每 6 小时', expr: '0 0 */6 * * ?' },
  { value: 'every-12h', label: '每 12 小时', expr: '0 0 */12 * * ?' },
  { value: 'weekly-mon', label: '每周一 3:00', expr: '0 0 3 * * 1' },
  { value: 'weekly-sun', label: '每周日 3:00', expr: '0 0 3 * * 0' },
  { value: 'monthly-1st', label: '每月1号 3:00', expr: '0 0 3 1 * ?' },
]

const cronDescription = computed(() => {
  const expr = form.cron_expr
  if (!expr) return ''
  const matched = cronPresets.find(p => p.expr === expr)
  if (matched && matched.value !== 'custom') return matched.label
  // 简单解析 6 段 cron
  const parts = expr.split(/\s+/)
  if (parts.length < 5) return '格式不完整'
  return `秒:${parts[0]} 分:${parts[1]} 时:${parts[2]} 日:${parts[3]} 月:${parts[4]}${parts[5] ? ' 周:' + parts[5] : ''}`
})

// ─────────────────── 表单 ───────────────────
const defaultForm = () => ({
  name: '',
  description: '',
  url_pattern: '',
  cron_expr: '0 0 3 * * ?',
  max_pages: 10,
  delay_ms: 1000,
  is_active: true,
  configText: JSON.stringify({
    manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
    list_page: { selector: 'a.product-link', url_attr: 'href', next_page_selector: '' },
    detail_fields: {
      name: { selector: 'h1' },
      name_en: { selector: 'h1' },
      description: { selector: 'div.description' },
      cover_image_url: { selector: 'img.main', attr: 'src' },
      category: { value: '工业机器人' },
      price_range: { value: 'inquiry' }
    }
  }, null, 2)
})

const form = reactive(defaultForm())

function applyTemplate(tpl: typeof configTemplates[0]) {
  activeTemplate.value = tpl.id
  form.configText = JSON.stringify(tpl.config, null, 2)
}

function applyCronPreset() {
  const preset = cronPresets.find(p => p.value === selectedCronPreset.value)
  if (preset && preset.expr) {
    form.cron_expr = preset.expr
  }
}

function openCreateModal() {
  showCreateModal.value = true
  activeTemplate.value = ''
  selectedCronPreset.value = 'daily-3am'
  previewResult.value = null
}

// ─────────────────── 可视化选择器 ───────────────────
function openSelectorTool() {
  const urls = form.url_pattern.split(',').map((u: string) => u.trim()).filter(Boolean)
  if (urls.length === 0) return
  const toolUrl = `/admin/selector-tool?url=${encodeURIComponent(urls[0])}`
  window.open(toolUrl, 'selector-tool', 'width=1400,height=900')
}

function handleSelectorToolResult(e: MessageEvent) {
  if (!e.data || e.data.source !== 'selector-tool-result') return
  const result = e.data.data
  if (!result) return

  // 合并选择器结果到当前 config
  try {
    let config = JSON.parse(form.configText)
    if (result.detail_fields) {
      config.detail_fields = { ...config.detail_fields, ...result.detail_fields }
    }
    if (result.list_page) {
      config.list_page = { ...config.list_page, ...result.list_page }
    }
    form.configText = JSON.stringify(config, null, 2)
  } catch {
    // 如果当前 configText 解析失败，用结果直接覆盖
    form.configText = JSON.stringify({
      manufacturer: { name: '', name_en: '', country: '', country_code: '', website_url: '' },
      ...result
    }, null, 2)
  }
}

// ─────────────────── 预览功能 ───────────────────
async function runPreview() {
  configError.value = ''
  let config: any
  try {
    config = JSON.parse(form.configText)
  } catch {
    configError.value = 'JSON 格式错误，请检查'
    return
  }

  const urls = form.url_pattern.split(',').map((u: string) => u.trim()).filter(Boolean)
  if (urls.length === 0) {
    configError.value = '请填写至少一个 URL'
    return
  }

  previewing.value = true
  previewResult.value = null
  try {
    const res = await http.post('/admin/crawler/preview', {
      url: urls[0],
      config
    })
    previewResult.value = res.data
  } catch (e: any) {
    const msg = e.response?.data?.detail || e.response?.data?.error || '预览请求失败'
    configError.value = `预览失败: ${msg}`
  } finally {
    previewing.value = false
  }
}

// ─────────────────── CRUD ───────────────────
async function loadSources() {
  loading.value = true
  try {
    const res = await http.get('/admin/crawler/sources')
    sources.value = res.data || []
    const statusRes = await http.get('/admin/crawler/status')
    const statusData = statusRes.data || {}
    runningCount.value = statusData.running_count || 0
    sources.value.forEach(s => {
      s.is_running = (statusData.running_jobs || []).includes(s.id)
    })
  } catch (e) {
    console.error('加载数据源失败', e)
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  try {
    const res = await http.get('/admin/crawler/logs?limit=30')
    logs.value = res.data || []
  } catch {}
}

async function runAll() {
  runningAll.value = true
  try {
    await http.post('/admin/crawler/run-all')
    setTimeout(loadSources, 2000)
  } finally {
    runningAll.value = false
  }
}

async function triggerRun(source: any) {
  source.is_running = true
  try {
    await http.post(`/admin/crawler/sources/${source.id}/run`)
    setTimeout(() => { loadSources(); loadLogs() }, 2000)
  } catch (e: any) {
    alert(e.response?.data?.detail || '触发失败')
    source.is_running = false
  }
}

async function toggleActive(source: any) {
  await http.put(`/admin/crawler/sources/${source.id}`, { is_active: !source.is_active })
  await loadSources()
}

function editSource(source: any) {
  editingSource.value = source
  previewResult.value = null
  activeTemplate.value = ''
  // 匹配当前 cron 到预设
  const matched = cronPresets.find(p => p.expr === source.cron_expr)
  selectedCronPreset.value = matched ? matched.value : 'custom'
  Object.assign(form, {
    name: source.name,
    description: source.description || '',
    url_pattern: source.url_pattern,
    cron_expr: source.cron_expr,
    max_pages: source.max_pages,
    delay_ms: source.delay_ms,
    is_active: !!source.is_active,
    configText: JSON.stringify(source.config, null, 2)
  })
}

async function confirmDelete(source: any) {
  if (!confirm(`确定删除数据源「${source.name}」？`)) return
  await http.delete(`/admin/crawler/sources/${source.id}`)
  await loadSources()
}

async function submitSource() {
  configError.value = ''
  let config: any
  try {
    config = JSON.parse(form.configText)
  } catch {
    configError.value = 'JSON 格式错误，请检查'
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name,
      description: form.description,
      url_pattern: form.url_pattern,
      cron_expr: form.cron_expr,
      max_pages: form.max_pages,
      delay_ms: form.delay_ms,
      is_active: form.is_active,
      config
    }
    if (editingSource.value) {
      await http.put(`/admin/crawler/sources/${editingSource.value.id}`, payload)
    } else {
      await http.post('/admin/crawler/sources', payload)
    }
    closeModal()
    await loadSources()
  } finally {
    saving.value = false
  }
}

function closeModal() {
  showCreateModal.value = false
  editingSource.value = null
  previewResult.value = null
  activeTemplate.value = ''
  selectedCronPreset.value = 'custom'
  Object.assign(form, defaultForm())
  configError.value = ''
}

function formatDate(dt: string) {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadSources()
  loadLogs()
  window.addEventListener('message', handleSelectorToolResult)
})

onUnmounted(() => {
  window.removeEventListener('message', handleSelectorToolResult)
})
</script>
