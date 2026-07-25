import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await login(form);
      loginUser(res.data);
      if (!res.data.onboardingComplete) {
        navigate('/onboarding');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fillDemo = (role) => {
    if (role === 'individual') {
      setForm({ email: 'testuser@recoverease.com', password: 'Demo@123' });
    } else {
      setForm({ email: 'caregiver@recoverease.com', password: 'Demo@123' });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-calm-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="text-6xl mb-4">🌿</div>
          <h1 className="text-3xl font-bold text-gray-800">RecoverEase AI</h1>
          <p className="text-gray-500 mt-2">Your recovery companion. You're not alone.</p>
        </div>

        <div className="card">
          <h2 className="text-xl font-bold text-gray-800 mb-6">Sign In</h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl px-4 py-3 mb-4 text-sm" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                className="input-field"
                value={form.email}
                onChange={e => setForm(p => ({ ...p, email: e.target.value }))}
                placeholder="you@example.com"
                required
                autoComplete="email"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                className="input-field"
                value={form.password}
                onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
                placeholder="••••••••"
                required
                autoComplete="current-password"
              />
            </div>
            <button type="submit" className="btn-primary w-full" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Demo credentials */}
          <div className="mt-4 p-3 bg-gray-50 rounded-xl border border-gray-200">
            <p className="text-xs text-gray-500 font-medium mb-2">🧪 Demo Access (evaluator test):</p>
            <div className="flex gap-2">
              <button
                onClick={() => fillDemo('individual')}
                className="text-xs bg-primary-100 text-primary-700 px-3 py-1.5 rounded-lg hover:bg-primary-200 transition-colors"
              >
                Individual User
              </button>
              <button
                onClick={() => fillDemo('caregiver')}
                className="text-xs bg-calm-100 text-calm-700 px-3 py-1.5 rounded-lg hover:bg-calm-200 transition-colors"
              >
                Caregiver
              </button>
            </div>
          </div>

          <p className="text-center text-sm text-gray-500 mt-4">
            New here?{' '}
            <Link to="/signup" className="text-primary-600 font-medium hover:underline">Create account</Link>
          </p>
        </div>

        <p className="text-center text-xs text-gray-400 mt-6 px-4">
          ⚠️ This platform provides supportive guidance only. In emergencies, call 911 or your local emergency services.
        </p>
      </div>
    </div>
  );
}
