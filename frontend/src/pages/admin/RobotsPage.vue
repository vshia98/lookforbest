<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-gray-800">机器人管理</h2>
      <router-link
        to="/admin/robots/create"
        class="bg-primary-500 text-white text-sm px-4 py-2 rounded-lg hover:bg-primary-600 transition-colors"
      >
        + 添加机器人
      </router-link>
    </div>

    <!-- 搜索栏 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm p-4 mb-5">
      <div class="flex gap-3 flex-wrap">
        <input
          v-model="query"
          @keyup.enter="search"
          type="text"
          placeholder="搜索机器人名称..."
          class="flex-1 min-w-48 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
        />
        <select
          v-model="statusFilter"
          @change="search"
          class="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
        >
          <option value="">全部状态</option>
          <option value="active">已上线</option>
          <option value="discontinued">已停产</option>
          <option value="upcoming">即将上市</option>
        </select>
        <button
          @click="search"
          class="bg-primary-500 text-white px-4 py-2 rounded-lg text-sm hover:bg-primary-600 transition-colors"
        >
          搜索
        </button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedIds.size > 0" class="bg-blue-50 border border-blue-200 rounded-xl p-3 mb-4 flex items-center gap-3 flex-wrap">
      <span class="text-sm text-blue-700 font-medium">已选 {{ selectedIds.size }} 项</span>
      <select
        v-model="batchStatus"
        class="border border-blue-200 rounded-lg px-2 py-1 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-400"
      >
        <option value="">批量修改状态...</option>
        <option value="active">设为上线</option>
        <option value="discontinued">设为停产</option>
        <option value="upcoming">设为即将上市</option>
      </select>
      <button
        v-if="batchStatus"
        @click="batchUpdateStatus"
        :disabled="batchLoading"
        class="bg-blue-500 text-white text-sm px-3 py-1 rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50"
      >
        {{ batchLoading ? '处理中...' : '确认修改' }}
      </button>
      <button
        @click="showBatchDelete = true"
        :disabled="batchLoading"
        class="bg-red-500 text-white text-sm px-3 py-1 rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50"
      >
        批量删除
      </button>
      <button
        @click="clearSelection"
        class="text-blue-500 text-sm px-2 py-1 hover:bg-blue-100 rounded transition-colors"
      >
        取消选择
      </button>
    </div>

    <!-- 表格 -->
    <div class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center py-16">
        <div class="animate-spin w-6 h-6 border-2 border-primary-500 border-t-transparent rounded-full"></div>
        <span class="ml-2 text-gray-500 text-sm">加载中...</span>
      </div>

      <table v-else class="w-full text-sm">
        <thead>
          <tr class="border-b border-gray-100 bg-gray-50">
            <th class="py-3 px-4 w-8">
              <input
                type="checkbox"
                :checked="allSelected"
                :indeterminate="someSelected"
                @change="toggleSelectAll"
                class="rounded border-gray-300 text-primary-500 focus:ring-primary-500"
              />
            </th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">ID</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">名称</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">厂商</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">分类</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">状态</th>
            <th class="text-left py-3 px-4 text-gray-500 font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="robot in robots"
            :key="robot.id"
            class="border-b border-gray-50 hover:bg-gray-50 transition-colors"
            :class="{ 'bg-blue-50/50': selectedIds.has(robot.id) }"
          >
            <td class="py-3 px-4">
              <input
                type="checkbox"
                :checked="selectedIds.has(robot.id)"
                @change="toggleSelect(robot.id)"
                class="rounded border-gray-300 text-primary-500 focus:ring-primary-500"
              />
            </td>
            <td class="py-3 px-4 text-gray-400">#{{ robot.id }}</td>
            <td class="py-3 px-4">
              <div class="flex items-center gap-3">
                <img
                  v-if="robot.coverImageUrl"
                  :src="robot.coverImageUrl"
                  :alt="robot.name"
                  class="w-10 h-10 object-cover rounded-lg flex-shrink-0"
                />
                <div v-else class="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400 flex-shrink-0">🤖</div>
                <div>
                  <p class="font-medium text-gray-800">{{ robot.name }}</p>
                  <p class="text-xs text-gray-400">{{ robot.modelNumber }}</p>
                </div>
              </div>
            </td>
            <td class="py-3 px-4 text-gray-600">{{ robot.manufacturer?.name }}</td>
            <td class="py-3 px-4 text-gray-600">{{ robot.category?.name }}</td>
            <td class="py-3 px-4">
              <span class="px-2 py-1 text-xs rounded-full" :class="statusClass(robot.status)">
                {{ statusLabel(robot.status) }}
              </span>
            </td>
            <td class="py-3 px-4">
              <div class="flex items-center gap-2">
                <router-link
                  :to="`/admin/robots/${robot.id}/edit`"
                  class="text-primary-500 hover:text-primary-700 text-xs px-2 py-1 rounded hover:bg-primary-50 transition-colors"
                >
                  编辑
                </router-link>
                <button
                  @click="deleteRobot(robot)"
                  class="text-red-500 hover:text-red-700 text-xs px-2 py-1 rounded hover:bg-red-50 transition-colors"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="robots.length === 0">
            <td colspan="7" class="py-16 text-center text-gray-400">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="flex items-center justify-between px-4 py-3 border-t border-gray-100">
        <p class="text-sm text-gray-500">共 {{ total }} 条</p>
        <div v-if="totalPages > 1" class="flex items-center gap-2">
          <button
            :disabled="page <= 1"
            @click="page--; loadRobots()"
            class="px-3 py-1 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            上一页
          </button>
          <span class="text-sm text-gray-500">{{ page }} / {{ totalPages }}</span>
          <button
            :disabled="page >= totalPages"
            @click="page++; loadRobots()"
            class="px-3 py-1 text-sm border border-gray-200 rounded-lg disabled:opacity-40 hover:bg-gray-50 transition-colors"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 单条删除确认 -->
    <div
      v-if="deleteTarget"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      @click.self="deleteTarget = null"
    >
      <div class="bg-white rounded-xl shadow-xl p-6 max-w-sm w-full mx-4">
        <h3 class="font-bold text-gray-800 mb-2">确认删除</h3>
        <p class="text-gray-500 text-sm mb-6">确定要删除「{{ deleteTarget.name }}」吗？此操作不可撤销。</p>
        <div class="flex gap-3 justify-end">
          <button @click="deleteTarget = null" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">取消</button>
          <button @click="confirmDelete" :disabled="deleting" class="px-4 py-2 text-sm bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 批量删除确认 -->
    <div
      v-if="showBatchDelete"
      class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
      @click.self="showBatchDelete = false"
    >
      <div class="bg-white rounded-xl shadow-xl p-6 max-w-sm w-full mx-4">
        <h3 class="font-bold text-gray-800 mb-2">批量删除</h3>
        <p class="text-gray-500 text-sm mb-6">确定要删除选中的 <strong>{{ selectedIds.size }}</strong> 条机器人数据吗？此操作不可撤销。</p>
        <div class="flex gap-3 justify-end">
          <button @click="showBatchDelete = false" class="px-4 py-2 text-sm border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">取消</button>
          <button @click="confirmBatchDelete" :disabled="batchLoading" class="px-4 py-2 text-sm bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50">
            {{ batchLoading ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { robotService, adminService } from '@/services/robots'
import http from '@/services/api'

const loading = ref(false)
const robots = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 20
const query = ref('')
const statusFilter = ref('')

// 单条删除
const deleteTarget = ref<any>(null)
const deleting = ref(false)

// 批量操作
const selectedIds = ref<Set<number>>(new Set())
const batchStatus = ref('')
const batchLoading = ref(false)
const showBatchDelete = ref(false)

const totalPages = computed(() => Math.ceil(total.value / pageSize))
const allSelected = computed(() => robots.value.length > 0 && robots.value.every(r => selectedIds.value.has(r.id)))
const someSelected = computed(() => robots.value.some(r => selectedIds.value.has(r.id)) && !allSelected.value)

function statusLabel(s: string) {
  const map: Record<string, string> = { active: '上线', discontinued: '停产', upcoming: '即将上市' }
  return map[s] ?? s
}
function statusClass(s: string) {
  const map: Record<string, string> = {
    active: 'bg-green-100 text-green-700',
    discontinued: 'bg-gray-100 text-gray-500',
    upcoming: 'bg-yellow-100 text-yellow-700'
  }
  return map[s] ?? 'bg-gray-100 text-gray-500'
}

function toggleSelect(id: number) {
  const s = new Set(selectedIds.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selectedIds.value = s
}

function toggleSelectAll() {
  if (allSelected.value) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(robots.value.map(r => r.id))
  }
}

function clearSelection() {
  selectedIds.value = new Set()
  batchStatus.value = ''
}

async function loadRobots() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize }
    if (query.value) params.q = query.value
    if (statusFilter.value) params.status = statusFilter.value
    const res = await robotService.getList(params)
    robots.value = (res as any).data || (res as any).items || []
    total.value = (res as any).total || robots.value.length
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadRobots()
}

function deleteRobot(robot: any) {
  deleteTarget.value = robot
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await adminService.deleteRobot(deleteTarget.value.id)
    deleteTarget.value = null
    await loadRobots()
  } catch (e) {
    console.error(e)
  } finally {
    deleting.value = false
  }
}

async function batchUpdateStatus() {
  if (!batchStatus.value || selectedIds.value.size === 0) return
  batchLoading.value = true
  try {
    await adminService.batchUpdateStatus(Array.from(selectedIds.value), batchStatus.value)
    clearSelection()
    await loadRobots()
  } catch (e) {
    console.error(e)
  } finally {
    batchLoading.value = false
  }
}

async function confirmBatchDelete() {
  if (selectedIds.value.size === 0) return
  batchLoading.value = true
  try {
    await adminService.batchDelete(Array.from(selectedIds.value))
    showBatchDelete.value = false
    clearSelection()
    await loadRobots()
  } catch (e) {
    console.error(e)
  } finally {
    batchLoading.value = false
  }
}

onMounted(loadRobots)
</script>
