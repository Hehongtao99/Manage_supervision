import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    host: '0.0.0.0',  // 支持所有IP地址访问，包括localhost和127.0.0.1
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true  // 启用WebSocket代理
      }
    }
  },
  define: {
    global: 'window'
  },
  // 添加优化依赖预构建配置
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia'],
    exclude: []
  },
  // 改进构建配置，处理动态导入
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus']
        }
      }
    },
    chunkSizeWarningLimit: 1000,
    // 帮助调试
    sourcemap: true
  }
})
