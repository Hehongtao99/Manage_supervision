import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',  // 支持所有IP地址访问，包括localhost和127.0.0.1
    port: 8089,       // 设置前端端口为8089
    proxy: {
      '/api': {
        target: 'http://localhost:8992',
        changeOrigin: true,
        secure: false
      },
      '/uploads': {
        target: 'http://localhost:8992',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'http://localhost:8992',
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
