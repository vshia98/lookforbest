<template>
  <!-- 浮动触发按钮 -->
  <div class="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3">
    <!-- 聊天窗口 -->
    <Transition name="chat-slide">
      <div
        v-if="isOpen"
        class="w-96 h-[520px] bg-dark-50 rounded-2xl shadow-2xl border border-white/[0.04] flex flex-col overflow-hidden"
      >
        <!-- 头部 -->
        <div class="bg-gradient-to-r from-blue-600 to-indigo-600 p-4 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-dark-50/20 rounded-full flex items-center justify-center text-white text-sm font-bold">AI</div>
            <div>
              <p class="text-white font-semibold text-sm">机器人选型助手</p>
              <p class="text-blue-100 text-xs">告诉我您的需求，我来推荐</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <button @click="clearSession" class="text-white/70 hover:text-white text-xs px-2 py-1 rounded hover:bg-dark-50/10 transition">清空</button>
            <button @click="isOpen = false" class="text-white/70 hover:text-white">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- 消息区域 -->
        <div ref="messageContainer" class="flex-1 overflow-y-auto p-4 space-y-3 bg-dark-100">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="text-center py-8">
            <div class="text-4xl mb-3">🤖</div>
            <p class="text-gray-500 text-sm">你好！我是 AI 选型助手</p>
            <p class="text-gray-400 text-xs mt-1">描述您的使用场景，我来推荐合适的机器人</p>
            <div class="mt-4 flex flex-wrap gap-2 justify-center">
              <button
                v-for="hint in quickHints"
                :key="hint"
                @click="sendQuick(hint)"
                class="px-3 py-1.5 bg-dark-50 border border-blue-200 rounded-full text-xs text-blue-400 hover:bg-blue-50 transition"
              >{{ hint }}</button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, idx) in messages" :key="idx" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
            <div
              class="max-w-[80%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed"
              :class="msg.role === 'user'
                ? 'bg-primary text-[#1a1a1a] rounded-br-sm'
                : 'bg-dark-50 text-gray-300  border border-white/[0.04] rounded-bl-sm'"
            >
              <div v-if="msg.role === 'assistant'" v-html="formatContent(msg.content)" />
              <span v-else>{{ msg.content }}</span>
            </div>
          </div>

          <!-- 流式输出中 -->
          <div v-if="streamingContent" class="flex justify-start">
            <div class="max-w-[80%] bg-dark-50 text-gray-300  border border-white/[0.04] rounded-2xl rounded-bl-sm px-4 py-2.5 text-sm leading-relaxed">
              <div v-html="formatContent(streamingContent)" />
              <span class="inline-block w-1.5 h-4 bg-blue-500 ml-1 animate-pulse" />
            </div>
          </div>

          <!-- Loading -->
          <div v-if="isLoading && !streamingContent" class="flex justify-start">
            <div class="bg-dark-50  border border-white/[0.04] rounded-2xl rounded-bl-sm px-4 py-3">
              <div class="flex gap-1">
                <div class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:0ms" />
                <div class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:150ms" />
                <div class="w-2 h-2 bg-blue-400 rounded-full animate-bounce" style="animation-delay:300ms" />
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="p-3 bg-dark-50 border-t border-white/[0.04]">
          <div class="flex gap-2">
            <input
              v-model="inputText"
              @keydown.enter.prevent="sendMessage"
              :disabled="isLoading"
              type="text"
              placeholder="描述您的需求..."
              class="flex-1 px-3 py-2 bg-dark-100 border border-white/[0.06] rounded-xl text-sm focus:outline-none focus:border-blue-400 focus:bg-dark-50 transition disabled:opacity-50"
            />
            <button
              @click="sendMessage"
              :disabled="isLoading || !inputText.trim()"
              class="px-4 py-2 bg-primary text-[#1a1a1a] rounded-xl text-sm font-medium hover:bg-primary-400 disabled:opacity-40 disabled:cursor-not-allowed transition"
            >
              发送
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 浮动按钮 -->
    <button
      @click="isOpen = !isOpen"
      class="w-14 h-14 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-full shadow-lg hover:shadow-xl hover:scale-105 transition-all flex items-center justify-center"
    >
      <svg v-if="!isOpen" class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
      </svg>
      <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const isOpen = ref(false)
const inputText = ref('')
const messages = ref<Message[]>([])
const isLoading = ref(false)
const streamingContent = ref('')
const messageContainer = ref<HTMLElement | null>(null)

const sessionKey = ref(localStorage.getItem('ai_chat_session') || crypto.randomUUID())

const quickHints = ['工业焊接机器人', '仓储物流机器人', '医疗手术机器人', '教育机器人']

onMounted(() => {
  localStorage.setItem('ai_chat_session', sessionKey.value)
  loadHistory()
})

async function loadHistory() {
  try {
    const res = await fetch(`/api/v1/chat/sessions/${sessionKey.value}/history`)
    if (res.ok) {
      const data: Message[] = await res.json()
      messages.value = data
      await scrollBottom()
    }
  } catch {}
}

async function sendQuick(hint: string) {
  inputText.value = hint
  await sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true
  streamingContent.value = ''
  await scrollBottom()

  try {
    const response = await fetch('/api/v1/chat/message', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sessionKey: sessionKey.value, message: text }),
    })

    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '[DONE]') {
            messages.value.push({ role: 'assistant', content: streamingContent.value })
            streamingContent.value = ''
          } else if (!data.startsWith('event:error')) {
            streamingContent.value += data
            await scrollBottom()
          }
        }
      }
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '对话失败，请重试。' })
  } finally {
    isLoading.value = false
    await scrollBottom()
  }
}

async function clearSession() {
  try {
    await fetch(`/api/v1/chat/sessions/${sessionKey.value}`, { method: 'DELETE' })
  } catch {}
  messages.value = []
  sessionKey.value = crypto.randomUUID()
  localStorage.setItem('ai_chat_session', sessionKey.value)
}

function formatContent(content: string): string {
  // 简单 Markdown 渲染：粗体、换行
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
    .replace(/```json([\s\S]*?)```/g, '') // 隐藏 JSON 推荐块（前端可进一步处理展示卡片）
}

async function scrollBottom() {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.25s ease;
}
.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.97);
}
</style>
