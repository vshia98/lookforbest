<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">爬虫管理</h1>
        <p class="text-gray-500 text-sm mt-1">配置化数据源爬取与监控</p>
      </div>
      <div class="flex items-center gap-3">
        <button
          @click="runAll"
          :disabled="runningAll"
          class="px-4 py-2 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700 disabled:opacity-50"
        >
          {{ runningAll ? '运行中...' : '运行所有' }}
        </button>
        <button
          @click="showCreateModal = true"
          class="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm hover:bg-primary-700"
        >
          + 新增数据源
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-3 gap-4 mb-6">
      <div class="bg-white rounded-xl border border-gray-200 p-4">
        <div class="text-2xl font-bold text-gray-900">{{ sources.length }}</div>
        <div class="text-sm text-gray-500 mt-1">数据源总数</div>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-4">
        <div class="text-2xl font-bold text-green-600">{{ activeSources }}</div>
        <div class="text-sm text-gray-500 mt-1">启用数量</div>
      </div>
      <div class="bg-white rounded-xl border border-gray-200 p-4">
        <div class="text-2xl font-bold text-blue-600">{{ runningCount }}</div>
        <div class="text-sm text-gray-500 mt-1">正在运行</div>
      </div>
    </div>

    <!-- 数据源列表 -->
    <div class="bg-white rounded-xl border border-gray-200 mb-6">
      <div class="p-4 border-b border-gray-100 font-semibold text-gray-700">数据源列表</div>
      <div v-if="loading" class="p-8 text-center text-gray-400">加载中...</div>
      <div v-else-if="sources.length === 0" class="p-8 text-center text-gray-400">暂无数据源</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">名称</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">URL</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">状态</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">上次运行</th>
            <th class="text-left px-4 py-3 text-gray-600 font-medium">累计爬取</th>
            <th class="text-right px-4 py-3 text-gray-600 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="s in sources" :key="s.id" class="hover:bg-gray-50">
            <td class="px-4 py-3">
              <div class="font-medium text-gray-900">{{ s.name }}</div>
              <div v-if="s.description" class="text-xs text-gray-400 mt-0.5 line-clamp-1">{{ s.description }}</div>
            </td>
            <td class="px-4 py-3 text-gray-600 max-w-xs truncate">{{ s.url_pattern }}</td>
            <td class="px-4 py-3">
              <span v-if="s.is_running" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-blue-100 text-blue-700">
                <span class="w-1.5 h-1.5 bg-blue-500 rounded-full animate-pulse" />运行中
              </span>
              <span v-else-if="s.last_crawl_status === 'success'" class="px-2 py-0.5 rounded-full text-xs bg-green-100 text-green-700">成功</span>
              <span v-else-if="s.last_crawl_status === 'failed'" class="px-2 py-0.5 rounded-full text-xs bg-red-100 text-red-700">失败</span>
              <span v-else-if="!s.is_active" class="px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-500">已禁用</span>
              <span v-else class="px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-600">待运行</span>
            </td>
            <td class="px-4 py-3 text-gray-500 text-xs">{{ s.last_crawl_at ? formatDate(s.last_crawl_at) : '-' }}</td>
            <td class="px-4 py-3 text-gray-700">{{ s.total_crawled || 0 }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex justify-end gap-2">
                <button
                  @click="triggerRun(s)"
                  :disabled="s.is_running"
                  class="px-2 py-1 text-xs bg-green-50 text-green-700 border border-green-200 rounded hover:bg-green-100 disabled:opacity-40"
                >运行</button>
                <button
                  @click="editSource(s)"
                  class="px-2 py-1 text-xs bg-blue-50 text-blue-700 border border-blue-200 rounded hover:bg-blue-100"
                >编辑</button>
                <button
                  @click="toggleActive(s)"
                  class="px-2 py-1 text-xs rounded border"
                  :class="s.is_active ? 'bg-yellow-50 text-yellow-700 border-yellow-200 hover:bg-yellow-100' : 'bg-gray-50 text-gray-600 border-gray-200 hover:bg-gray-100'"
                >{{ s.is_active ? '禁用' : '启用' }}</button>
                <button
                  @click="confirmDelete(s)"
                  class="px-2 py-1 text-xs bg-red-50 text-red-700 border border-red-200 rounded hover:bg-red-100"
                >删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 运行日志 -->
    <div class="bg-white rounded-xl border border-gray-200">
      <div class="p-4 border-b border-gray-100 flex items-center justify-between">
        <span class="font-semibold text-gray-700">运行日志</span>
        <button @click="loadLogs" class="text-xs text-primary-500 hover:underline">刷新</button>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">数据源</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">开始时间</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">结束时间</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">状态</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">页数</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">采集/写入/跳过</th>
              <th class="text-left px-4 py-3 text-gray-600 font-medium">触发方式</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50">
              <td class="px-4 py-3 font-medium text-gray-800">{{ log.source_name }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">{{ formatDate(log.started_at) }}</td>
              <td class="px-4 py-3 text-gray-500 text-xs">{{ log.finished_at ? formatDate(log.finished_at) : '-' }}</td>
              <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded-full text-xs"
                  :class="{
                    'bg-green-100 text-green-700': log.status === 'success',
                    'bg-red-100 text-red-700': log.status === 'failed',
                    'bg-blue-100 text-blue-700': log.status === 'running',
                    'bg-gray-100 text-gray-600': log.status === 'cancelled'
                  }"
                >{{ log.status }}</span>
              </td>
              <td class="px-4 py-3 text-gray-600">{{ log.pages_crawled }}</td>
              <td class="px-4 py-3 text-gray-600">{{ log.items_crawled }} / {{ log.items_upserted }} / {{ log.items_skipped }}</td>
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
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div class="p-6 border-b border-gray-100">
          <h2 class="text-lg font-bold text-gray-900">{{ editingSource ? '编辑数据源' : '新增数据源' }}</h2>
        </div>
        <form @submit.prevent="submitSource" class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">名称 *</label>
            <input v-model="form.name" required class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">描述</label>
            <input v-model="form.description" class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">起始URL（多个用逗号分隔）*</label>
            <textarea v-model="form.url_pattern" required rows="2" class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Cron 表达式</label>
              <input v-model="form.cron_expr" class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" placeholder="0 0 3 * * ?" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">最大页数</label>
              <input v-model.number="form.max_pages" type="number" min="1" max="200" class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              爬取规则 JSON *
              <span class="text-xs font-normal text-gray-400 ml-1">（manufacturer, list_page, detail_fields）</span>
            </label>
            <textarea
              v-model="form.configText"
              required rows="12"
              class="w-full border border-gray-200 rounded-lg px-3 py-2 text-xs font-mono focus:outline-none focus:ring-2 focus:ring-primary-500"
              placeholder='{"manufacturer":{"name":"厂商名","name_en":"Vendor","country":"中国","country_code":"CN","website_url":""},"detail_fields":{"name":{"selector":"h1"},"name_en":{"selector":"h1"},"description":{"selector":"div.desc"},"cover_image_url":{"selector":"img.hero","attr":"src"},"category":{"value":"工业机器人"}}}'
            />
            <p v-if="configError" class="text-red-500 text-xs mt-1">{{ configError }}</p>
          </div>
          <div class="flex items-center gap-2">
            <input v-model="form.is_active" type="checkbox" id="is_active" class="rounded" />
            <label for="is_active" class="text-sm text-gray-700">启用</label>
          </div>
          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeModal" class="px-4 py-2 border border-gray-200 rounded-lg text-sm text-gray-600 hover:bg-gray-50">取消</button>
            <button type="submit" :disabled="saving" class="px-4 py-2 bg-primary-600 text-white rounded-lg text-sm hover:bg-primary-700 disabled:opacity-50">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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

const activeSources = computed(() => sources.value.filter(s => s.is_active).length)

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

async function loadSources() {
  loading.value = true
  try {
    const res = await http.get('/admin/crawler/sources')
    sources.value = res.data.data || []
    const statusRes = await http.get('/admin/crawler/status')
    runningCount.value = statusRes.data.running_count || 0
    sources.value.forEach(s => {
      s.is_running = (statusRes.data.running_jobs || []).includes(s.id)
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
    logs.value = res.data.data || []
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
  } catch (e) {
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
})
</script>
