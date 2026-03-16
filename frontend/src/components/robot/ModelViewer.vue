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

    <div v-else class="relative rounded-2xl overflow-hidden bg-dark-100 border border-white/[0.06]" style="height: 420px;">
      <component :is="ModelViewerComponent"
        v-if="ModelViewerComponent"
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
              <p class="font-semibold text-xs text-white">{{ ann.label }}</p>
              <p v-if="ann.description" class="text-xs text-gray-400 mt-0.5">{{ ann.description }}</p>
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
      </component>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import axios from 'axios'

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

const ModelViewerComponent = ref<any>(null)

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
  await nextTick()
  ModelViewerComponent.value = (window as any).modelViewer
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
  background: #202228;
  border: 1px solid rgba(255, 255, 255, 0.04);
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
  background: #00FFD1;
  color: #1F1F23;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 0 12px rgba(0, 255, 209, 0.4);
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
  background: #2C2D36;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0.5rem;
  padding: 6px 10px;
  white-space: nowrap;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  z-index: 10;
}

.ar-button {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: #2C2D36;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0.75rem;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #00FFD1;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.15s;
}

.ar-button:hover {
  background: #33343D;
  box-shadow: 0 0 20px rgba(0, 255, 209, 0.15);
}

.progress-bar-wrapper {
  width: 100%;
  height: 3px;
  background: #33343D;
  position: absolute;
  bottom: 0;
  left: 0;
}

.progress-bar-inner {
  height: 100%;
  background: #00FFD1;
  transition: width 0.3s;
}
</style>
