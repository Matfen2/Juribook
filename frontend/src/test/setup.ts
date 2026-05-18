import '@testing-library/jest-dom';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// Nettoie le DOM entre chaque test (sinon les composants s'accumulent)
afterEach(() => {
  cleanup();
});