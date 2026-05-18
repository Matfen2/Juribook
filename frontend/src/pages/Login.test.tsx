import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { Login } from './Login';
import { AuthProvider } from '../context/AuthContext';
import * as authApi from '../services/auth';

// Mock React Router pour intercepter la navigation
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

// Mock le module services/auth pour ne pas faire de vrais appels HTTP
vi.mock('../services/auth');

function renderLogin() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Login />
      </AuthProvider>
    </MemoryRouter>
  );
}

describe('Login — composant', () => {

  beforeEach(() => {
    vi.clearAllMocks();
    // Par défaut, fetchCurrentUser retourne null (utilisateur non connecté)
    vi.mocked(authApi.fetchCurrentUser).mockRejectedValue(new Error('Not authenticated'));
  });

  // ─── Test 1: rendu des champs ─────────────────────────────────────
  it('affiche les champs email et password', async () => {
    renderLogin();

    expect(await screen.findByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Mot de passe/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Se connecter/i })).toBeInTheDocument();
  });

  // ─── Test 2: soumission avec succès ──────────────────────────────
  it('soumet le formulaire et redirige vers /dashboard sur succès', async () => {
    const user = userEvent.setup();

    vi.mocked(authApi.login).mockResolvedValue({
      id: 'uuid-alice',
      email: 'alice@test.com',
      firstName: 'Alice',
      lastName: 'Martin',
      roles: ['CLIENT'],
    });

    renderLogin();

    await user.type(await screen.findByLabelText(/Email/i), 'alice@test.com');
    await user.type(screen.getByLabelText(/Mot de passe/i), 'Password1');
    await user.click(screen.getByRole('button', { name: /Se connecter/i }));

    await waitFor(() => {
      expect(authApi.login).toHaveBeenCalledWith({
        email: 'alice@test.com',
        password: 'Password1',
      });
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  // ─── Test 3: erreur backend affichée ─────────────────────────────
  it('affiche le message d\'erreur du backend si login échoue (401)', async () => {
    const user = userEvent.setup();

    // Simule une erreur axios 401 — structure conforme à AxiosError<ApiError>
    vi.mocked(authApi.login).mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 401,
        data: {
          status: 401,
          error: 'Unauthorized',
          message: 'Email ou mot de passe incorrect',
          timestamp: '2026-05-17T19:00:00Z',
        },
      },
    });

    renderLogin();

    await user.type(await screen.findByLabelText(/Email/i), 'alice@test.com');
    await user.type(screen.getByLabelText(/Mot de passe/i), 'WrongPassword');
    await user.click(screen.getByRole('button', { name: /Se connecter/i }));

    expect(await screen.findByText(/Email ou mot de passe incorrect/i)).toBeInTheDocument();
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});