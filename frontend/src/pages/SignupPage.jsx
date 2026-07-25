import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/authApi';
import { useAuth } from '../context/AuthContext';

export default function SignupPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'INDIVIDUAL' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await signup(form);
      loginUser(res.data);
      navigate('/onboarding');
    } catch (err) {
      setError(err.response?.data?.error || 'Sign up failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-calm-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="text-6xl mb-4">🌿</div>
          <h1 className="text-3xl font-bold text-gray-800">Join RecoverEase AI</h1>
          <p className="text-gray-500 mt-2">Start your supported recovery journey today.</p>
        </div>

        <div className="card">
          <h2 className="text-xl font-bold text-gray-800 mb-6">Create Account</h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl px-4 py-3 mb-4 text-sm" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="name">Full Name</label>
              <input id="name" type="text" className="input-field"
                value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))}
                placeholder="Your name" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="email">Email</label>
              <input id="email" type="email" className="input-field"
                value={form.email} onChange={e => setForm(p => ({ ...p, email: e.target.value }))}
                placeholder="you@example.com" required />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="password">Password</label>
              <input id="password" type="password" className="input-field"
                value={form.password} onChange={e => setForm(p => ({ ...p, password: e.target.value }))}
                placeholder="Min. 6 characters" required minLength={6} />
            </div>

            {/* Role selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">I am a...</label>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { value: 'INDIVIDUAL', label: '👤 Individual', desc: 'In recovery or seeking support' },
                  { value: 'CAREGIVER', label: '🤝 Caregiver', desc: 'Supporting a loved one' },
                ].map(opt => (
                  <button
                    key={opt.value}
                    type="button"
                    onClick={() => setForm(p => ({ ...p, role: opt.value }))}
                    className={`p-3 rounded-xl border-2 text-left transition-all
                      ${form.role === opt.value
                        ? 'border-primary-500 bg-primary-50'
                        : 'border-gray-200 bg-white hover:border-gray-300'}`}
                    aria-pressed={form.role === opt.value}
                  >
                    <div className="font-medium text-sm">{opt.label}</div>
                    <div className="text-xs text-gray-400 mt-0.5">{opt.desc}</div>
                  </button>
                ))}
              </div>
            </div>

            <button type="submit" className="btn-primary w-full" disabled={loading}>
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-4">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
