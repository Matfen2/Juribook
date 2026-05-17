import { useAuth } from '../context/AuthContext';

export function Dashboard() {
  const { user } = useAuth();

  if (!user) return null; // ProtectedRoute gère déjà la redirection

  const primaryRole = user.roles[0];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 px-6 py-12">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-2">
          Bienvenue {user.firstName} 👋
        </h1>
        <p className="text-slate-400 mb-8">
          Vous êtes connecté en tant que <strong>{primaryRole}</strong>
        </p>

        <div className="grid gap-4 md:grid-cols-2">
          {primaryRole === 'CLIENT' && (
            <>
              <DashboardCard title="Rechercher un avocat" description="Filtrer par spécialité et ville (Sprint 2)" />
              <DashboardCard title="Mes rendez-vous" description="Historique et rendez-vous à venir (Sprint 4)" />
            </>
          )}
          {primaryRole === 'LAWYER' && (
            <>
              <DashboardCard title="Mes disponibilités" description="Gérer mon calendrier (Sprint 3)" />
              <DashboardCard title="Demandes en attente" description="Confirmer ou refuser des RDV (Sprint 4)" />
            </>
          )}
          {primaryRole === 'ADMIN' && (
            <>
              <DashboardCard title="Validation avocats" description="Approuver les inscriptions (Sprint 7)" />
              <DashboardCard title="Journal d'audit" description="Historique complet (Sprint 5)" />
            </>
          )}
        </div>
      </div>
    </div>
  );
}

function DashboardCard({ title, description }: { title: string; description: string }) {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-lg p-6 hover:border-slate-700 cursor-pointer">
      <h2 className="text-lg font-semibold mb-1">{title}</h2>
      <p className="text-sm text-slate-400">{description}</p>
    </div>
  );
}