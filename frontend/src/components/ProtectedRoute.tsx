import { type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types/auth';

interface Props {
  children: ReactNode;
  requireRole?: Role;
}

export function ProtectedRoute({ children, requireRole }: Props) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center bg-slate-950 text-slate-400">Chargement…</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (requireRole && !user.roles.includes(requireRole)) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 text-rose-400">
        Accès refusé — rôle {requireRole} requis
      </div>
    );
  }

  return <>{children}</>;
}