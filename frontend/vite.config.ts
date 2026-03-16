import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // 允许 model-viewer 等自定义元素
          isCustomElement: (tag) => tag === 'model-viewer'
        }
      }
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/lookforbest': {
        target: 'http://localhost:9000',
        changeOrigin: true
      }
    }
  }
})
