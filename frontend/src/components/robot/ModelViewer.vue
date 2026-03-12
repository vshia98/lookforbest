<template>
  <div class="model-viewer-wrapper">
    <!-- 加载 model-viewer CDN -->
    <component :is="'script'" type="module"
      src="https://ajax.googleapis.com/ajax/libs/model-viewer/3.5.0/model-viewer.min.js"
    />

    <div v-if="!modelData || !modelData.modelUrl" class="no-model-placeholder">
      <div class="no-model-inner">
        <div class="text-5xl mb-3">🤖</div>
        <p class="text-gray-500 text-sm">暂无 3D 模型</p>
      </div>
    </div>

    <div v-else class="relative rounded-2xl overflow-hidden bg-gray-50 border border-gray-100" style="height: 420px;">
      <!-- @ts-ignore: model-viewer 自定义元素 -->
      <model-viewer
        :src="modelData.modelUrl"
        :poster="modelData.posterUrl || undefined"
        :ios-src="modelData.arUrl || undefined"
        :alt="modelData.title || robotName || '3D 模型'"
        camera-controls
        auto-rotate
        ar
        ar-modes="webxr scene-viewer quick-look"
        shadow-intensity="1"
        style="width: 100%; height: 100%; background: transparent;"
        loading="eager"
        reveal="auto"
      >
        <!-- 标注热点 -->
        <template v-if="parsedAnnotations.length > 0">
          <button
            v-for="(ann, idx) in parsedAnnotations"
            :key="idx"
            :slot="`hotspot-${idx}`"
            :data-position="ann.position"
            :data-normal="ann.normal"
            class="annotation-hotspot"
            @click="activeAnnotation = activeAnnotation === idx ? null : idx"
          >
            <span class="hotspot-dot">{{ idx + 1 }}</span>
            <div v-if="activeAnnotation === idx" class="hotspot-label">
              <p class="font-semibold text-xs text-gray-800">{{ ann.label }}</p>
              <p v-if="ann.description" class="text-xs text-gray-500 mt-0.5">{{ ann.description }}</p>
            </div>
          </button>
        </template>

        <!-- AR 按钮 -->
        <button slot="ar-button" class="ar-button">
          📱 AR 查看
        </button>

        <!-- 加载进度 -->
        <div slot="progress-bar" class="progress-bar-wrapper">
          <div class="progress-bar-inner" />
        </div>
      </model-viewer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 最小类型声明，避免 TypeScript 报错
declare global {
  namespace JSX {
    interface IntrinsicElements {
      'model-viewer': any
    }
  }
}

interface Annotation {
  position: string
  normal: string
  label: string
  description?: string
}

interface RobotModelData {
  id?: number
  robotId?: number
  modelUrl?: string | null
  posterUrl?: string | null
  arUrl?: string | null
  title?: string | null
  annotations?: string | null
}

const props = defineProps<{
  robotId: number
  robotName?: string
}>()

const modelData = ref<RobotModelData | null>(null)
const loading = ref(true)
const activeAnnotation = ref<number | null>(null)

const parsedAnnotations = computed<Annotation[]>(() => {
  if (!modelData.value?.annotations) return []
  try {
    const parsed = JSON.parse(modelData.value.annotations)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

onMounted(async () => {
  try {
    const res = await axios.get(`/api/v1/robots/${props.robotId}/model`)
    modelData.value = res.data?.data ?? null
  } catch {
    modelData.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.model-viewer-wrapper {
  width: 100%;
}

.no-model-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 1rem;
}

.no-model-inner {
  text-align: center;
}

.annotation-hotspot {
  position: relative;
  border: none;
  background: transparent;
  cursor: pointer;
}

.hotspot-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(59,130,246,0.4);
  transition: transform 0.15s;
}

.hotspot-dot:hover {
  transform: scale(1.15);
}

.hotspot-label {
  position: absolute;
  left: 32px;
  top: 50%;
  transform: translateY(-50%);
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 6px 10px;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 10;
}

.ar-button {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.75rem;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: background 0.15s;
}

.ar-button:hover {
  background: #f3f4f6;
}

.progress-bar-wrapper {
  width: 100%;
  height: 3px;
  background: #e5e7eb;
  position: absolute;
  bottom: 0;
  left: 0;
}

.progress-bar-inner {
  height: 100%;
  background: #3b82f6;
  transition: width 0.3s;
}
</style>
