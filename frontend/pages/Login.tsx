import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../services/auth';
import { useAuth } from '../context/AuthContext';
import { FormField } from '../components/FormField';
import type { ApiError } from '../types/auth';
import { AxiosError } from 'axios';

export function Login() {
  const navigate = useNavigate();
  const { setUser, refresh } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const user = await login({ email, password });
      // Refresh pour récupérer la version complète /me (avec rôles si LoginResponse n'en a pas)
      setUser(user);
      await refresh();
      navigate('/dashboard');
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      setError(axiosErr.response?.data?.message ?? 'Erreur de connexion');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <div className="w-full max-w-md bg-slate-900 rounded-lg p-8 border border-slate-800">
        <h1 className="text-2xl font-bold text-slate-100 mb-6">Connexion</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label="Email" name="email" type="email"
            value={email} onChange={setEmail}
            required autoComplete="email"
          />
          <FormField
            label="Mot de passe" name="password" type="password"
            value={password} onChange={setPassword}
            required autoComplete="current-password"
          />

          {error && (
            <div className="bg-rose-500/10 border border-rose-500/30 text-rose-400 text-sm rounded px-3 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-2 rounded bg-blue-600 hover:bg-blue-500 disabled:bg-slate-700 text-white font-medium"
          >
            {submitting ? 'Connexion…' : 'Se connecter'}
          </button>
        </form>

        <div className="mt-6 text-sm text-slate-400 text-center">
          Pas de compte ?{' '}
          <Link to="/register" className="text-blue-400 hover:text-blue-300">
            Créer un compte client
          </Link>
          {' · '}
          <Link to="/register/lawyer" className="text-blue-400 hover:text-blue-300">
            Avocat
          </Link>
        </div>
      </div>
    </div>
  );
}