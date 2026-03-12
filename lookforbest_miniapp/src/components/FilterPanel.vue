<template>
  <!-- 遮罩层 -->
  <view v-if="visible" class="mask" @tap="onClose" />

  <!-- 筛选面板（底部弹出） -->
  <view class="panel" :class="{ show: visible }">
    <view class="panel-header">
      <text class="panel-title">筛选</text>
      <text class="panel-reset" @tap="onReset">重置</text>
    </view>

    <!-- 载荷范围 -->
    <view class="section">
      <text class="section-label">额定载荷 (kg)</text>
      <view class="range-row">
        <input
          class="range-input"
          type="digit"
          placeholder="最小"
          placeholder-class="input-ph"
          :value="localFilter.payloadMin != null ? String(localFilter.payloadMin) : ''"
          @input="(e: any) => setPayloadMin(e.detail.value)"
        />
        <text class="range-sep">—</text>
        <input
          class="range-input"
          type="digit"
          placeholder="最大"
          placeholder-class="input-ph"
          :value="localFilter.payloadMax != null ? String(localFilter.payloadMax) : ''"
          @input="(e: any) => setPayloadMax(e.detail.value)"
        />
      </view>
    </view>

    <!-- 自由度 -->
    <view class="section">
      <text class="section-label">自由度</text>
      <view class="chips-row">
        <view
          v-for="dof in dofOptions"
          :key="dof"
          class="chip"
          :class="{ active: localFilter.dof === dof }"
          @tap="toggleDof(dof)"
        >
          <text class="chip-text">{{ dof }}轴</text>
        </view>
      </view>
    </view>

    <!-- 分类 -->
    <view class="section">
      <text class="section-label">机器人类型</text>
      <view class="chips-row">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="chip"
          :class="{ active: localFilter.categoryId === cat.id }"
          @tap="toggleCategory(cat.id)"
        >
          <text class="chip-text">{{ cat.name }}</text>
        </view>
      </view>
    </view>

    <view class="panel-footer">
      <view class="cancel-btn" @tap="onClose">
        <text class="cancel-text">取消</text>
      </view>
      <view class="confirm-btn" @tap="onConfirm">
        <text class="confirm-text">确定</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Category {
  id: number
  name: string
}

interface FilterOptions {
  payloadMin?: number
  payloadMax?: number
  dof?: number
  categoryId?: number
}

const props = defineProps<{
  visible: boolean
  categories: Category[]
  modelValue?: FilterOptions
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'confirm', filter: FilterOptions): void
}>()

const dofOptions = [4, 6, 7]
const localFilter = ref<FilterOptions>({ ...props.modelValue })

watch(() => props.modelValue, (val) => {
  localFilter.value = { ...val }
})

function setPayloadMin(val: string) {
  localFilter.value.payloadMin = val ? Number(val) : undefined
}

function setPayloadMax(val: string) {
  localFilter.value.payloadMax = val ? Number(val) : undefined
}

function toggleDof(dof: number) {
  localFilter.value.dof = localFilter.value.dof === dof ? undefined : dof
}

function toggleCategory(id: number) {
  localFilter.value.categoryId = localFilter.value.categoryId === id ? undefined : id
}

function onReset() {
  localFilter.value = {}
}

function onClose() {
  emit('update:visible', false)
}

function onConfirm() {
  emit('confirm', { ...localFilter.value })
  emit('update:visible', false)
}
</script>

<style scoped>
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
}

.panel {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  z-index: 101;
  padding: 0 0 env(safe-area-inset-bottom);
  transform: translateY(100%);
  transition: transform 0.3s ease;
}
.panel.show {
  transform: translateY(0);
}

.panel-header {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 40rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.panel-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a1a;
}
.panel-reset {
  font-size: 28rpx;
  color: #6366F1;
}

.section {
  padding: 28rpx 40rpx;
  border-bottom: 1rpx solid #f8f8f8;
}
.section-label {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.range-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}
.range-input {
  flex: 1;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  color: #333;
}
.input-ph {
  color: #ccc;
  font-size: 26rpx;
}
.range-sep {
  font-size: 28rpx;
  color: #999;
}

.chips-row {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16rpx;
}
.chip {
  background: #f0f0f0;
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
  border: 2rpx solid transparent;
}
.chip.active {
  background: #ede9ff;
  border-color: #6366F1;
}
.chip-text {
  font-size: 26rpx;
  color: #555;
}
.chip.active .chip-text {
  color: #6366F1;
  font-weight: bold;
}

.panel-footer {
  display: flex;
  flex-direction: row;
  gap: 24rpx;
  padding: 24rpx 40rpx 32rpx;
}
.cancel-btn {
  flex: 1;
  border: 2rpx solid #ddd;
  border-radius: 48rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cancel-text {
  font-size: 30rpx;
  color: #666;
}
.confirm-btn {
  flex: 2;
  background: #6366F1;
  border-radius: 48rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.confirm-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: bold;
}
</style>
