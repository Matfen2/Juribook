import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth':          { target: 'http://localhost:8081', changeOrigin: true },
      '/api/lawyers':       { target: 'http://localhost:8082', changeOrigin: true },
      '/api/bookings':      { target: 'http://localhost:8083', changeOrigin: true },
      '/api/notifications': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/audit':         { target: 'http://localhost:8085', changeOrigin: true },
    },
  },
});