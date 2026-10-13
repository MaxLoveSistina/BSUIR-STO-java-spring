import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '')
  return {
    plugins: [react()],
    server: {
      // В dev-режиме запросы /api проксируются на Spring Boot
      proxy: {
        '/api': env.VITE_API_PROXY || 'http://localhost:8080',
      },
    },
  }
})
