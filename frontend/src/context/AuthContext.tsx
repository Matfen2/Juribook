import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { CurrentUser } from '../types/auth';
import { fetchCurrentUser } from '../services/auth';

interface AuthContextType {
  user: CurrentUser | null;
  loading: boolean;
  setUser: (user: CurrentUser | null) => void;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  // Au montage, on tente de charger /me — si un cookie JWT valide existe, on récupère l'user
  useEffect(() => {
    refresh().finally(() => setLoading(false));
  }, []);

  async function refresh() {
    try {
      const me = await fetchCurrentUser();
      setUser(me);
    } catch {
      setUser(null);
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, setUser, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}