import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Header() {
  const { user, setUser } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    // Sprint 1.7 ajoutera un vrai endpoint POST /api/auth/logout
    // qui clear le cookie côté serveur. Pour Sprint 1.6 on clear juste le state.
    setUser(null);
    navigate('/');
  }

  return (
    <header className="bg-slate-900 border-b border-slate-800">
      <nav className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
        <Link to="/" className="text-xl font-bold text-slate-100 hover:text-white">
          JuriBook
        </Link>

        <div className="flex items-center gap-4 text-sm">
          {user ? (
            <>
              <Link to="/dashboard" className="text-slate-300 hover:text-white">
                Dashboard
              </Link>
              <span className="text-slate-500">|</span>
              <span className="text-slate-400">
                {user.firstName} ({user.roles.join(', ')})
              </span>
              <button
                onClick={handleLogout}
                className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200"
              >
                Déconnexion
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-slate-300 hover:text-white">
                Connexion
              </Link>
              <Link
                to="/register"
                className="px-3 py-1 rounded bg-blue-600 hover:bg-blue-500 text-white"
              >
                Inscription
              </Link>
            </>
          )}
        </div>
      </nav>
    </header>
  );
}