import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Home() {
  const { user } = useAuth();

  return (
    <div className="min-h-[calc(100vh-65px)] flex items-center justify-center bg-slate-950 text-slate-100 px-6">
      <div className="text-center max-w-2xl">
        <h1 className="text-5xl font-bold tracking-tight mb-4">JuriBook</h1>
        <p className="text-xl text-slate-400 mb-8">
          Trouvez un avocat, prenez rendez-vous en quelques clics
        </p>

        {user ? (
          <Link
            to="/dashboard"
            className="inline-block px-6 py-3 rounded bg-blue-600 hover:bg-blue-500 text-white font-medium"
          >
            Accéder à mon espace
          </Link>
        ) : (
          <div className="flex justify-center gap-3">
            <Link
              to="/register"
              className="px-6 py-3 rounded bg-blue-600 hover:bg-blue-500 text-white font-medium"
            >
              Créer un compte
            </Link>
            <Link
              to="/login"
              className="px-6 py-3 rounded border border-slate-700 hover:border-slate-600 text-slate-200 font-medium"
            >
              Se connecter
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}