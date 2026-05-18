import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerClient } from '../services/auth';
import { FormField } from '../components/FormField';
import type { ApiError } from '../types/auth';
import { AxiosError } from 'axios';

export function RegisterClient() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: '', password: '', firstName: '', lastName: '', phone: '',
  });
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  function update<K extends keyof typeof form>(key: K, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setFieldErrors({});
    setSubmitting(true);

    try {
      await registerClient(form);
      // L'inscription ne login pas automatiquement — on redirige vers /login
      navigate('/login', { state: { message: 'Compte créé, vous pouvez vous connecter' } });
    } catch (err) {
      const axiosErr = err as AxiosError<ApiError>;
      const data = axiosErr.response?.data;
      setError(data?.message ?? 'Erreur lors de l\'inscription');
      if (data?.fields) setFieldErrors(data.fields);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 px-4 py-12">
      <div className="w-full max-w-md bg-slate-900 rounded-lg p-8 border border-slate-800">
        <h1 className="text-2xl font-bold text-slate-100 mb-6">Créer un compte client</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label="Email" name="email" type="email"
            value={form.email} onChange={(v) => update('email', v)}
            required error={fieldErrors.email} autoComplete="email"
          />
          <FormField
            label="Mot de passe" name="password" type="password"
            value={form.password} onChange={(v) => update('password', v)}
            required error={fieldErrors.password}
            placeholder="8+ chars, 1 majuscule, 1 chiffre"
            autoComplete="new-password"
          />
          <FormField
            label="Prénom" name="firstName"
            value={form.firstName} onChange={(v) => update('firstName', v)}
            required error={fieldErrors.firstName}
          />
          <FormField
            label="Nom" name="lastName"
            value={form.lastName} onChange={(v) => update('lastName', v)}
            required error={fieldErrors.lastName}
          />
          <FormField
            label="Téléphone (optionnel)" name="phone" type="tel"
            value={form.phone} onChange={(v) => update('phone', v)}
            error={fieldErrors.phone} placeholder="0612345678"
            autoComplete="tel"
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
            {submitting ? 'Création…' : 'Créer mon compte'}
          </button>
        </form>

        <div className="mt-6 text-sm text-slate-400 text-center">
          Déjà inscrit ?{' '}
          <Link to="/login" className="text-blue-400 hover:text-blue-300">
            Se connecter
          </Link>
        </div>
      </div>
    </div>
  );
}