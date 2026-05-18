import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,           // permet d'utiliser describe/it/expect sans import
    environment: 'jsdom',    // DOM simulé pour les composants React
    setupFiles: './src/test/setup.ts',
    css: false,              // on n'a pas besoin de parser le CSS Tailwind dans les tests
  },
});