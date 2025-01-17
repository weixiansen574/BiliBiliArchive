import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        host: '0.0.0.0', // 使用IP能访问
        proxy: {
            '/api': {
                target: 'http://192.168.1.6:80', // 后端 API 地址
                changeOrigin: true, // 支持跨域
            },
            '/files': {
              target: 'http://192.168.1.6:80',
              changeOrigin: true,
          }
        }
    }
})