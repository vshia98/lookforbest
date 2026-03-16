<template>
  <div class="h-screen flex flex-col bg-dark-100">
    <!-- 顶部工具栏 -->
    <div class="bg-dark-50 border-b border-white/[0.06] px-4 py-2.5 flex items-center gap-3 shrink-0">
      <h1 class="text-sm font-bold text-white whitespace-nowrap">可视化选择器</h1>
      <input
        v-model="targetUrl"
        placeholder="输入目标页面 URL，按回车加载"
        @keydown.enter="loadPage"
        class="input-dark"
      />
      <button
        @click="loadPage"
        :disabled="pageLoading"
        class="px-4 py-1.5 bg-primary text-[#1a1a1a] rounded-lg text-sm hover:bg-primary-400 disabled:opacity-50 whitespace-nowrap"
      >{{ pageLoading ? '加载中...' : '加载页面' }}</button>
      <button
        @click="applyAndClose"
        :disabled="Object.keys(selectedFields).length === 0"
        class="px-4 py-1.5 bg-green-600 text-[#1a1a1a] rounded-lg text-sm hover:bg-green-700 disabled:opacity-50 whitespace-nowrap"
      >应用并关闭</button>
    </div>

    <div class="flex flex-1 overflow-hidden">
      <!-- 左侧：字段面板 -->
      <div class="w-72 bg-dark-50 border-r border-white/[0.06] flex flex-col shrink-0">
        <div class="p-3 border-b border-white/[0.04]">
          <div class="text-xs font-semibold text-gray-400 mb-2">选择模式</div>
          <div class="flex gap-1">
            <button
              @click="pickMode = 'detail'"
              class="flex-1 px-2 py-1.5 text-xs rounded-lg border transition-colors"
              :class="pickMode === 'detail' ? 'bg-primary/10 text-primary border-primary/30' : 'border-white/[0.06] text-gray-500 hover:bg-dark-100'"
            >字段选择</button>
            <button
              @click="pickMode = 'list'"
              class="flex-1 px-2 py-1.5 text-xs rounded-lg border transition-colors"
              :class="pickMode === 'list' ? 'bg-primary/10 text-primary border-primary/30' : 'border-white/[0.06] text-gray-500 hover:bg-dark-100'"
            >列表链接</button>
          </div>
        </div>

        <!-- 字段选择模式 -->
        <div v-if="pickMode === 'detail'" class="flex-1 overflow-y-auto p-3 space-y-1">
          <div class="text-xs text-gray-400 mb-2">点击字段名 → 在页面中点选元素</div>
          <div
            v-for="field in detailFieldList" :key="field.key"
            @click="startPicking(field.key)"
            class="flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer text-sm transition-colors"
            :class="activeField === field.key
              ? 'bg-amber-900/20 border border-amber-300 text-amber-300'
              : selectedFields[field.key]
                ? 'bg-green-900/20 border border-green-800 text-green-800'
                : 'bg-dark-100 border border-white/[0.04] text-gray-300 hover:bg-dark-100'"
          >
            <div>
              <div class="font-medium text-xs">{{ field.label }}</div>
              <div v-if="selectedFields[field.key]" class="text-[10px] font-mono text-gray-500 mt-0.5 truncate max-w-[180px]">
                {{ selectedFields[field.key].selector }}
              </div>
            </div>
            <div class="flex items-center gap-1">
              <span v-if="activeField === field.key" class="w-2 h-2 bg-amber-400 rounded-full animate-pulse" />
              <span v-else-if="selectedFields[field.key]" class="text-green-500 text-xs">OK</span>
              <button
                v-if="selectedFields[field.key]"
                @click.stop="removeField(field.key)"
                class="text-gray-300 hover:text-red-400 ml-1 text-xs"
              >x</button>
            </div>
          </div>
        </div>

        <!-- 列表链接模式 -->
        <div v-else class="flex-1 overflow-y-auto p-3">
          <div class="text-xs text-gray-400 mb-2">点击页面中的链接元素来选择列表选择器</div>
          <div
            @click="startPicking('__list_selector__')"
            class="flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer text-sm transition-colors"
            :class="activeField === '__list_selector__'
              ? 'bg-amber-900/20 border border-amber-300 text-amber-300'
              : listSelector
                ? 'bg-green-900/20 border border-green-800 text-green-800'
                : 'bg-dark-100 border border-white/[0.04] text-gray-300 hover:bg-dark-100'"
          >
            <div>
              <div class="font-medium text-xs">列表项链接选择器</div>
              <div v-if="listSelector" class="text-[10px] font-mono text-gray-500 mt-0.5">{{ listSelector }}</div>
            </div>
            <span v-if="activeField === '__list_selector__'" class="w-2 h-2 bg-amber-400 rounded-full animate-pulse" />
            <span v-else-if="listSelector" class="text-green-500 text-xs">OK</span>
          </div>
          <div v-if="listSelector" class="mt-3 text-xs text-gray-500">
            匹配到 {{ listMatchCount }} 个链接
          </div>
        </div>

        <!-- 提取属性设置 -->
        <div v-if="activeField && activeField !== '__list_selector__'" class="p-3 border-t border-white/[0.04] space-y-2">
          <div class="text-xs font-semibold text-gray-400">提取方式</div>
          <select v-model="extractMode" class="input-dark text-xs">
            <option value="text">文本内容</option>
            <option value="src">图片 src</option>
            <option value="href">链接 href</option>
            <option value="content">meta content</option>
            <option value="custom">自定义属性</option>
          </select>
          <input
            v-if="extractMode === 'custom'"
            v-model="customAttr"
            placeholder="属性名，如 data-value"
            class="input-dark text-xs"
          />
          <div>
            <label class="text-xs text-gray-500">正则提取 (可选)</label>
            <input
              v-model="regexPattern"
              placeholder="如 ([\d.]+)"
              class="input-dark"
            />
          </div>
        </div>

        <!-- 底部已选摘要 -->
        <div class="p-3 border-t border-white/[0.04] bg-dark-100">
          <div class="text-xs text-gray-500">已选择 {{ Object.keys(selectedFields).length }} 个字段{{ listSelector ? ' + 列表选择器' : '' }}</div>
        </div>
      </div>

      <!-- 右侧：页面预览 iframe -->
      <div class="flex-1 relative bg-dark-50">
        <div v-if="!pageHtml && !pageLoading" class="absolute inset-0 flex items-center justify-center text-gray-400 text-sm">
          输入 URL 并点击「加载页面」开始
        </div>
        <div v-if="pageLoading" class="absolute inset-0 flex items-center justify-center bg-dark-50/80 z-10">
          <div class="text-gray-500 text-sm">正在加载页面...</div>
        </div>
        <iframe
          v-if="pageHtml"
          ref="iframeRef"
          :srcdoc="injectedHtml"
          sandbox="allow-same-origin allow-scripts"
          class="w-full h-full border-0"
        />
        <!-- 选中元素预览值 -->
        <div v-if="hoveredValue" class="absolute bottom-4 left-4 right-4 bg-gray-900/90 text-white px-4 py-2 rounded-lg text-xs font-mono truncate z-20">
          {{ hoveredSelector }} → {{ hoveredValue }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import http from '@/services/api'

const targetUrl = ref('')
const pageHtml = ref('')
const pageLoading = ref(false)
const iframeRef = ref<HTMLIFrameElement | null>(null)
const pickMode = ref<'detail' | 'list'>('detail')
const activeField = ref<string | null>(null)
const extractMode = ref('text')
const customAttr = ref('')
const regexPattern = ref('')
const hoveredValue = ref('')
const hoveredSelector = ref('')
const listSelector = ref('')
const listMatchCount = ref(0)

interface FieldSelection {
  selector: string
  attr?: string
  regex?: string
  previewValue?: string
}
const selectedFields = ref<Record<string, FieldSelection>>({})

const detailFieldList = [
  { key: 'name', label: '产品名称 (name)' },
  { key: 'name_en', label: '英文名称 (name_en)' },
  { key: 'description', label: '产品描述 (description)' },
  { key: 'cover_image_url', label: '封面图片 (cover_image_url)' },
  { key: 'category', label: '分类 (category)' },
  { key: 'payload_kg', label: '负载 kg (payload_kg)' },
  { key: 'reach_mm', label: '臂展 mm (reach_mm)' },
  { key: 'dof', label: '自由度 (dof)' },
  { key: 'weight_kg', label: '重量 kg (weight_kg)' },
  { key: 'repeatability_mm', label: '重复精度 mm (repeatability_mm)' },
  { key: 'max_speed', label: '最大速度 (max_speed)' },
  { key: 'price_range', label: '价格区间 (price_range)' },
]

// 从 URL 参数读取初始 URL
onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const url = params.get('url')
  if (url) {
    targetUrl.value = url
    loadPage()
  }
  window.addEventListener('message', handleIframeMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', handleIframeMessage)
})

// ─────────────────── 加载页面 ───────────────────
async function loadPage() {
  if (!targetUrl.value) return
  pageLoading.value = true
  try {
    const res: any = await http.post('/admin/crawler/fetch-page', { url: targetUrl.value })
    // api.ts 拦截器已解包 res.data，所以 res = { data: { html, url } }
    pageHtml.value = res?.data?.html || ''
    if (!pageHtml.value) {
      alert('未获取到页面内容，请检查 URL 是否正确')
    }
  } catch (e: any) {
    const detail = e.response?.data?.detail || e.detail || e.message || JSON.stringify(e)
    alert('加载失败: ' + detail)
  } finally {
    pageLoading.value = false
  }
}

// 注入选择器拾取脚本的 HTML
const injectedHtml = computed(() => {
  if (!pageHtml.value) return ''

  const scriptClose = '</' + 'script>'
  const pickerScript = `
<style id="__picker_style__">
  .__picker_hover__ {
    outline: 2px dashed #f59e0b !important;
    outline-offset: 2px !important;
    cursor: crosshair !important;
  }
  .__picker_selected__ {
    outline: 2px solid #22c55e !important;
    outline-offset: 2px !important;
  }
  .__picker_list_match__ {
    outline: 2px solid #3b82f6 !important;
    outline-offset: 1px !important;
  }
</style>
<script>
(function() {
  var picking = false;
  var lastHovered = null;

  function generateSelector(el) {
    if (el.id) return '#' + CSS.escape(el.id);
    var tag = el.tagName.toLowerCase();
    if (tag === 'body' || tag === 'html') return tag;
    var classes = Array.from(el.classList).filter(function(c) { return !c.startsWith('__picker_'); });
    if (classes.length > 0) {
      var sel = tag + '.' + classes.map(function(c) { return CSS.escape(c); }).join('.');
      try {
        var matches = document.querySelectorAll(sel);
        if (matches.length === 1) return sel;
        if (matches.length <= 5 && el.parentElement) {
          var parentSel = generateSelector(el.parentElement);
          var fullSel = parentSel + ' > ' + sel;
          try { if (document.querySelectorAll(fullSel).length <= 3) return fullSel; } catch(e) {}
        }
      } catch(e) {}
    }
    var par = el.parentElement;
    if (!par) return tag;
    var siblings = Array.from(par.children);
    var index = siblings.indexOf(el) + 1;
    var parSel = generateSelector(par);
    return parSel + ' > ' + tag + ':nth-child(' + index + ')';
  }

  window.addEventListener('message', function(e) {
    var data = e.data;
    if (!data || data.source !== 'selector-tool') return;
    if (data.action === 'startPicking') {
      picking = true;
      document.body.style.cursor = 'crosshair';
    }
    if (data.action === 'stopPicking') {
      picking = false;
      document.body.style.cursor = '';
      if (lastHovered) { lastHovered.classList.remove('__picker_hover__'); lastHovered = null; }
    }
    if (data.action === 'highlightSelector') {
      document.querySelectorAll('.__picker_selected__').forEach(function(el) { el.classList.remove('__picker_selected__'); });
      document.querySelectorAll('.__picker_list_match__').forEach(function(el) { el.classList.remove('__picker_list_match__'); });
      if (data.selector) {
        try {
          var matches = document.querySelectorAll(data.selector);
          matches.forEach(function(el) { el.classList.add(data.isListMode ? '__picker_list_match__' : '__picker_selected__'); });
          parent.postMessage({ source: 'selector-tool-iframe', action: 'matchCount', count: matches.length }, '*');
        } catch(err) {}
      }
    }
  });

  document.addEventListener('mousemove', function(e) {
    if (!picking) return;
    var el = document.elementFromPoint(e.clientX, e.clientY);
    if (!el || el === lastHovered) return;
    if (lastHovered) lastHovered.classList.remove('__picker_hover__');
    el.classList.add('__picker_hover__');
    lastHovered = el;
    var selector = generateSelector(el);
    parent.postMessage({
      source: 'selector-tool-iframe', action: 'hover',
      selector: selector,
      value: (el.textContent || '').trim().substring(0, 150),
      tag: el.tagName.toLowerCase()
    }, '*');
  }, true);

  document.addEventListener('click', function(e) {
    if (!picking) return;
    e.preventDefault();
    e.stopPropagation();
    var el = document.elementFromPoint(e.clientX, e.clientY);
    if (!el) return;
    var selector = generateSelector(el);
    parent.postMessage({
      source: 'selector-tool-iframe', action: 'picked',
      selector: selector,
      value: (el.textContent || '').trim().substring(0, 300),
      tag: el.tagName.toLowerCase(),
      hasHref: !!el.getAttribute('href'),
      hasSrc: !!el.getAttribute('src') || !!el.getAttribute('data-src')
    }, '*');
  }, true);
})();
` + scriptClose

  // 在 </body> 前注入脚本
  const html = pageHtml.value
  const bodyCloseIndex = html.lastIndexOf('</body>')
  if (bodyCloseIndex !== -1) {
    return html.slice(0, bodyCloseIndex) + pickerScript + html.slice(bodyCloseIndex)
  }
  return html + pickerScript
})

// ─────────────────── 选择器拾取 ───────────────────
function startPicking(fieldKey: string) {
  activeField.value = fieldKey
  // 根据字段自动设置提取方式
  if (fieldKey === 'cover_image_url') extractMode.value = 'src'
  else if (fieldKey === '__list_selector__') extractMode.value = 'href'
  else extractMode.value = 'text'
  regexPattern.value = ''
  customAttr.value = ''
  // 通知 iframe 开始拾取
  iframeRef.value?.contentWindow?.postMessage({ source: 'selector-tool', action: 'startPicking' }, '*')
}

function removeField(key: string) {
  delete selectedFields.value[key]
}

function handleIframeMessage(e: MessageEvent) {
  const data = e.data
  if (!data || data.source !== 'selector-tool-iframe') return

  if (data.action === 'hover') {
    hoveredSelector.value = data.selector
    hoveredValue.value = data.value?.substring(0, 100) || '(空)'
  }

  if (data.action === 'picked' && activeField.value) {
    const fieldKey = activeField.value

    // 自动推断提取方式
    if (data.hasSrc && (fieldKey === 'cover_image_url' || data.tag === 'img')) {
      extractMode.value = 'src'
    }
    if (data.hasHref && fieldKey === '__list_selector__') {
      extractMode.value = 'href'
    }

    if (fieldKey === '__list_selector__') {
      listSelector.value = data.selector
      // 高亮所有匹配
      iframeRef.value?.contentWindow?.postMessage({
        source: 'selector-tool',
        action: 'highlightSelector',
        selector: data.selector,
        isListMode: true
      }, '*')
    } else {
      const field: FieldSelection = {
        selector: data.selector,
        previewValue: data.value?.substring(0, 100)
      }
      if (extractMode.value !== 'text') {
        field.attr = extractMode.value === 'custom' ? customAttr.value : extractMode.value
      }
      if (regexPattern.value) {
        field.regex = regexPattern.value
      }
      selectedFields.value[fieldKey] = field

      // 高亮选中元素
      iframeRef.value?.contentWindow?.postMessage({
        source: 'selector-tool',
        action: 'highlightSelector',
        selector: data.selector,
        isListMode: false
      }, '*')
    }

    // 停止拾取
    activeField.value = null
    iframeRef.value?.contentWindow?.postMessage({ source: 'selector-tool', action: 'stopPicking' }, '*')
    hoveredValue.value = ''
    hoveredSelector.value = ''
  }

  if (data.action === 'matchCount') {
    listMatchCount.value = data.count
  }
}

// ─────────────────── 应用结果 ───────────────────
function applyAndClose() {
  const detailFields: Record<string, any> = {}
  for (const [key, val] of Object.entries(selectedFields.value)) {
    const rule: any = { selector: val.selector }
    if (val.attr) rule.attr = val.attr
    if (val.regex) rule.regex = val.regex
    detailFields[key] = rule
  }

  const result: any = {
    detail_fields: detailFields
  }
  if (listSelector.value) {
    result.list_page = {
      selector: listSelector.value,
      url_attr: 'href'
    }
  }

  // 通过 postMessage 发送给父窗口
  if (window.opener) {
    window.opener.postMessage({
      source: 'selector-tool-result',
      data: result
    }, '*')
    window.close()
  } else {
    // 没有 opener，复制到剪贴板
    navigator.clipboard.writeText(JSON.stringify(result, null, 2))
      .then(() => alert('已复制到剪贴板（JSON 格式）'))
      .catch(() => alert(JSON.stringify(result, null, 2)))
  }
}
</script>
