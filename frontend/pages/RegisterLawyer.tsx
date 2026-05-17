import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerLawyer } from '../services/auth';
import { FormField } from '../components/FormField';
import type { ApiError } from '../types/auth';
import { AxiosError } from 'axios';

const SPECIALTIES = [
  { code: 'DROIT_TRAVAIL',     name: 'Droit du travail' },
  { code: 'DROIT_FAMILLE',     name: 'Droit de la famille' },
  { code: 'DROIT_PENAL',       name: 'Droit pénal' },
  { code: 'DROIT_AFFAIRES',    name: 'Droit des affaires' },
  { code: 'DROIT_IMMOBILIER',  name: 'Droit immobilier' },
  { code: 'DROIT_FISCAL',      name: 'Droit fiscal' },
  { code: 'DROIT_SOCIAL',      name: 'Droit social' },
  { code: 'DROIT_COMMERCIAL',  name: 'Droit commercial' },
  { code: 'DROIT_CIVIL',       name: 'Droit civil' },
  { code: 'DROIT_ADMINISTRATIF', name: 'Droit administratif' },
];

export function RegisterLawyer() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: '', password: '', firstName: '', lastName: '', phone: '',
    barNumber: '', specialtyCode: 'DROIT_TRAVAIL', city: '',
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
      await registerLawyer(form);
      navigate('/login', {
        state: {
          message: 'Inscription enregistrée. Votre compte sera activé après validation par un administrateur.',
        },
      });
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
        <h1 className="text-2xl font-bold text-slate-100 mb-2">Inscription avocat</h1>
        <p className="text-sm text-slate-400 mb-6">
          Votre compte sera validé manuellement par un administrateur avant activation.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <FormField
            label="Email professionnel" name="email" type="email"
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
            label="Téléphone" name="phone" type="tel"
            value={form.phone} onChange={(v) => update('phone', v)}
            error={fieldErrors.phone} placeholder="0612345678" autoComplete="tel"
          />
          <FormField
            label="Numéro de barreau" name="barNumber"
            value={form.barNumber} onChange={(v) => update('barNumber', v)}
            required error={fieldErrors.barNumber} placeholder="PAR-2024-12345"
          />

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">
              Spécialité <span className="text-rose-400">*</span>
            </label>
            <select
              value={form.specialtyCode}
              onChange={(e) => update('specialtyCode', e.target.value)}
              className="w-full px-3 py-2 rounded bg-slate-800 border border-slate-700 text-slate-100"
            >
              {SPECIALTIES.map((s) => (
                <option key={s.code} value={s.code}>{s.name}</option>
              ))}
            </select>
          </div>

          <FormField
            label="Ville" name="city"
            value={form.city} onChange={(v) => update('city', v)}
            required error={fieldErrors.city}
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
            {submitting ? 'Création…' : 'Demander l\'inscription'}
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