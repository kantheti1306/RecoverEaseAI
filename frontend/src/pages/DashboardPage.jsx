import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getCheckInHistory } from '../api/checkinApi';

const RISK_COLORS = {
  LOW: 'bg-green-100 text-green-700 border-green-200',
  MEDIUM: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  HIGH: 'bg-red-100 text-red-700 border-red-200',
};

export default function DashboardPage() {
  const { user } = useAuth();
  const [latestCheckin, setLatestCheckin] = useState(null);

  useEffect(() => {
    getCheckInHistory().then(res => {
      if (res.data?.length > 0) setLatestCheckin(res.data[0]);
    }).catch(() => {});
  }, []);

  const isCaregiver = user?.role === 'CAREGIVER';

  const individualActions = [
    { to: '/crisis', icon: '🆘', label: 'Help Me Now', desc: 'Immediate AI crisis support', color: 'bg-red-50 border-red-200 hover:bg-red-100' },
    { to: '/checkin', icon: '📋', label: 'Daily Check-in', desc: 'Track mood & cravings', color: 'bg-blue-50 border-blue-200 hover:bg-blue-100' },
    { to: '/scripts', icon: '📝', label: 'Emergency Scripts', desc: 'AI-generated support scripts', color: 'bg-purple-50 border-purple-200 hover:bg-purple-100' },
    { to: '/resources', icon: '📚', label: 'Resources & Education', desc: 'Trusted recovery info', color: 'bg-green-50 border-green-200 hover:bg-green-100' },
    { to: '/safety-plan', icon: '🛡️', label: 'Safety Plan', desc: 'Your emergency contacts & tools', color: 'bg-yellow-50 border-yellow-200 hover:bg-yellow-100' },
  ];

  const caregiverActions = [
    { to: '/caregiver', icon: '🤝', label: 'Caregiver Guidance', desc: 'AI support for your situation', color: 'bg-blue-50 border-blue-200 hover:bg-blue-100' },
    { to: '/resources', icon: '📚', label: 'Resources & Education', desc: 'Trusted recovery info', color: 'bg-green-50 border-green-200 hover:bg-green-100' },
  ];

  const actions = isCaregiver ? caregiverActions : individualActions;

  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  return (
    <div className="min-h-screen bg-gray-50 pb-20 md:pb-6">
      <div className="max-w-2xl mx-auto px-4 py-6">

        {/* Greeting */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">
            {greeting}, {user?.name?.split(' ')[0]} 👋
          </h1>
          <p className="text-gray-500 mt-1 text-sm">
            {isCaregiver
              ? "You're making a difference by being here. How can we support you today?"
              : "You're not alone. Every day in recovery is a victory."}
          </p>
        </div>

        {/* Latest check-in risk banner */}
        {latestCheckin && !isCaregiver && (
          <div className={`border rounded-2xl px-5 py-4 mb-5 ${RISK_COLORS[latestCheckin.riskLevel] || RISK_COLORS.LOW}`}>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide opacity-70">Latest Check-in Risk</p>
                <p className="text-lg font-bold">{latestCheckin.riskLevel} Risk</p>
                <p className="text-sm opacity-80">{latestCheckin.aiSummary?.split('.')[0]}.</p>
              </div>
              <Link to="/checkin" className="text-xs font-medium underline opacity-70">Check in now →</Link>
            </div>
          </div>
        )}

        {/* Emergency banner if no check-in */}
        {!latestCheckin && !isCaregiver && (
          <div className="bg-primary-50 border border-primary-200 rounded-2xl px-5 py-4 mb-5">
            <p className="text-primary-700 font-medium text-sm">🌿 Welcome! Start with a daily check-in to track your progress.</p>
          </div>
        )}

        {/* Main CTA for individuals */}
        {!isCaregiver && (
          <Link to="/crisis"
            className="w-full bg-red-500 hover:bg-red-600 text-white rounded-2xl px-6 py-5 flex items-center gap-4 shadow-md hover:shadow-lg transition-all duration-200 mb-6 group">
            <span className="text-4xl group-hover:scale-110 transition-transform">🆘</span>
            <div>
              <p className="font-bold text-lg">Need Help Right Now?</p>
              <p className="text-red-100 text-sm">Tap for immediate AI crisis support with voice</p>
            </div>
            <span className="ml-auto text-2xl">→</span>
          </Link>
        )}

        {/* Action grid */}
        <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">
          {isCaregiver ? 'Caregiver Tools' : 'Your Tools'}
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {actions.filter(a => a.to !== '/crisis').map(action => (
            <Link key={action.to} to={action.to}
              className={`border rounded-2xl p-5 flex items-start gap-4 transition-all duration-200 hover:shadow-md ${action.color}`}
              aria-label={action.label}>
              <span className="text-3xl">{action.icon}</span>
              <div>
                <p className="font-semibold text-gray-800">{action.label}</p>
                <p className="text-xs text-gray-500 mt-0.5">{action.desc}</p>
              </div>
            </Link>
          ))}
        </div>

        {/* Safety reminder */}
        <div className="mt-8 bg-gray-100 rounded-2xl p-4 text-center">
          <p className="text-xs text-gray-400">
            🛡️ RecoverEase AI provides supportive guidance only. For medical emergencies, call <strong>911</strong>.
            <br />National Helpline: <strong>1-800-662-4357</strong> (SAMHSA)
          </p>
        </div>
      </div>
    </div>
  );
}
