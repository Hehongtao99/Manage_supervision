import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',  // 支持所有IP地址访问，包括localhost和127.0.0.1
    port: 4889,  // 设置前端端口为4889
    proxy: {
      '/api': {
        target: 'http://localhost:8999',
        changeOrigin: true,
        secure: false
      },
      '/uploads': {
        target: 'http://localhost:8999',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'http://localhost:8999',
        changeOrigin: true,
        secure: false,
        ws: true  // 启用WebSocket代理
      }
    }
  },
  define: {
    global: 'window'
  }
})
