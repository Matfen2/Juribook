import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { RegisterClient } from './RegisterClient';
import { AuthProvider } from '../context/AuthContext';
import * as authApi from '../services/auth';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('../services/auth');

function renderRegisterClient() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <RegisterClient />
      </AuthProvider>
    </MemoryRouter>
  );
}

describe('RegisterClient — composant', () => {

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(authApi.fetchCurrentUser).mockRejectedValue(new Error('Not authenticated'));
  });

  // ─── Test 4: rendu des champs obligatoires ───────────────────────
  it('affiche les champs obligatoires (email, password, firstName, lastName)', async () => {
    renderRegisterClient();

    expect(await screen.findByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Mot de passe/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Prénom/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Nom/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Créer mon compte/i })).toBeInTheDocument();
  });

  // ─── Test 5: erreurs de validation backend par champ ─────────────
  it('affiche les erreurs de validation des champs renvoyées par le backend (400)', async () => {
    const user = userEvent.setup();

    vi.mocked(authApi.registerClient).mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 400,
        data: {
          status: 400,
          error: 'Bad Request',
          message: 'Erreur de validation',
          timestamp: '2026-05-17T19:00:00Z',
          fields: {
            email: 'Format d\'email invalide',
            password: 'Le mot de passe doit contenir au moins une minuscule, une majuscule et un chiffre',
          },
        },
      },
    });

    renderRegisterClient();

    // Email VALIDE côté HTML5 (sinon le formulaire ne se soumet pas)
    // C'est le BACKEND qui rejettera (via le mock), pas le navigateur
    await user.type(await screen.findByLabelText(/Email/i), 'bob@test.com');
    await user.type(screen.getByLabelText(/Mot de passe/i), 'WeakPassword');
    await user.type(screen.getByLabelText(/Prénom/i), 'Bob');
    await user.type(screen.getByLabelText(/^Nom/i), 'Test');
    await user.click(screen.getByRole('button', { name: /Créer mon compte/i }));

    // L'erreur globale s'affiche
    expect(await screen.findByText(/Erreur de validation/i)).toBeInTheDocument();

    // Les erreurs spécifiques par champ s'affichent
    await waitFor(() => {
      expect(screen.getByText(/Format d'email invalide/i)).toBeInTheDocument();
      expect(screen.getByText(/au moins une minuscule, une majuscule et un chiffre/i)).toBeInTheDocument();
    });

    // Pas de redirection (le submit a échoué)
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});