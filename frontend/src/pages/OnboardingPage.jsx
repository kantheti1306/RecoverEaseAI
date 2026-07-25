import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { completeOnboarding } from '../api/userApi';
import { useAuth } from '../context/AuthContext';

const steps = [
  { id: 1, title: 'Your Triggers', desc: 'What situations or feelings trigger your cravings?' },
  { id: 2, title: 'Calming Strategies', desc: 'What helps you calm down when you\'re overwhelmed?' },
  { id: 3, title: 'Emergency Contact', desc: 'Who should we help you reach in a crisis?' },
  { id: 4, title: 'Personal Reminder', desc: 'A message you want to hear during tough moments.' },
];

export default function OnboardingPage() {
  const [step, setStep] = useState(0);
  const [form, setForm] = useState({
    triggers: '',
    calmingStrategies: '',
    warningSignsPersonal: '',
    personalReminder: '',
    primaryContactName: '',
    primaryContactPhone: '',
    primaryContactRelation: '',
    consentToAlert: false,
    preferredLanguage: 'en',
  });
  const [loading, setLoading] = useState(false);
  const { updateOnboarding } = useAuth();
  const navigate = useNavigate();

  const triggerSuggestions = ['Loneliness', 'Work stress', 'Social events', 'Arguments', 'Boredom', 'Anxiety', 'Financial worry'];
  const calmingSuggestions = ['Deep breathing', 'Calling a friend', 'Walking outside', 'Journaling', 'Meditation', 'Music'];

  const addSuggestion = (field, val) => {
    setForm(p => ({
      ...p,
      [field]: p[field] ? `${p[field]}, ${val}` : val
    }));
  };

  const handleNext = () => setStep(s => s + 1);
  const handleBack = () => setStep(s => s - 1);

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await completeOnboarding(form);
      updateOnboarding();
      navigate('/dashboard');
    } catch (err) {
      alert('Failed to save. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const progress = ((step + 1) / steps.length) * 100;

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-calm-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">
        <div className="text-center mb-6">
          <span className="text-4xl">🌱</span>
          <h1 className="text-2xl font-bold text-gray-800 mt-2">Let's set up your safety profile</h1>
          <p className="text-gray-500 text-sm mt-1">This helps AI personalize support just for you.</p>
        </div>

        {/* Progress */}
        <div className="mb-6">
          <div className="flex justify-between text-xs text-gray-400 mb-1">
            <span>Step {step + 1} of {steps.length}</span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div className="bg-primary-500 h-2 rounded-full transition-all duration-500" style={{ width: `${progress}%` }}></div>
          </div>
        </div>

        <div className="card">
          <h2 className="text-lg font-bold text-gray-800 mb-1">{steps[step].title}</h2>
          <p className="text-sm text-gray-500 mb-4">{steps[step].desc}</p>

          {step === 0 && (
            <div>
              <textarea className="input-field mb-3" rows={3} placeholder="e.g. loneliness, work stress, social events..."
                value={form.triggers} onChange={e => setForm(p => ({ ...p, triggers: e.target.value }))} />
              <div className="flex flex-wrap gap-2">
                {triggerSuggestions.map(s => (
                  <button key={s} type="button" onClick={() => addSuggestion('triggers', s)}
                    className="text-xs bg-gray-100 hover:bg-primary-100 text-gray-600 hover:text-primary-700 px-3 py-1 rounded-full transition-colors">
                    + {s}
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 1 && (
            <div>
              <textarea className="input-field mb-3" rows={3} placeholder="e.g. deep breathing, calling a friend, going for a walk..."
                value={form.calmingStrategies} onChange={e => setForm(p => ({ ...p, calmingStrategies: e.target.value }))} />
              <div className="flex flex-wrap gap-2">
                {calmingSuggestions.map(s => (
                  <button key={s} type="button" onClick={() => addSuggestion('calmingStrategies', s)}
                    className="text-xs bg-gray-100 hover:bg-primary-100 text-gray-600 hover:text-primary-700 px-3 py-1 rounded-full transition-colors">
                    + {s}
                  </button>
                ))}
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-3">
              <input className="input-field" placeholder="Contact name (e.g. Mom, John, Sponsor)"
                value={form.primaryContactName} onChange={e => setForm(p => ({ ...p, primaryContactName: e.target.value }))} />
              <input className="input-field" placeholder="Phone number" type="tel"
                value={form.primaryContactPhone} onChange={e => setForm(p => ({ ...p, primaryContactPhone: e.target.value }))} />
              <input className="input-field" placeholder="Relation (e.g. Mom, Friend, Sponsor)"
                value={form.primaryContactRelation} onChange={e => setForm(p => ({ ...p, primaryContactRelation: e.target.value }))} />
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" className="w-4 h-4 accent-primary-600"
                  checked={form.consentToAlert} onChange={e => setForm(p => ({ ...p, consentToAlert: e.target.checked }))} />
                <span className="text-sm text-gray-600">Allow app to show this contact during crisis moments</span>
              </label>
            </div>
          )}

          {step === 3 && (
            <div>
              <textarea className="input-field mb-2" rows={4}
                placeholder="e.g. You have been sober for 3 months. You are stronger than this moment. Your family needs you."
                value={form.personalReminder} onChange={e => setForm(p => ({ ...p, personalReminder: e.target.value }))} />
              <p className="text-xs text-gray-400">💡 The AI will use this to personalize crisis responses for you.</p>
            </div>
          )}

          <div className="flex gap-3 mt-6">
            {step > 0 && (
              <button onClick={handleBack} className="btn-secondary flex-1">← Back</button>
            )}
            {step < steps.length - 1 ? (
              <button onClick={handleNext} className="btn-primary flex-1">Next →</button>
            ) : (
              <button onClick={handleSubmit} className="btn-primary flex-1" disabled={loading}>
                {loading ? 'Saving...' : '✅ Complete Setup'}
              </button>
            )}
          </div>

          <button onClick={() => { updateOnboarding(); navigate('/dashboard'); }}
            className="w-full text-center text-xs text-gray-400 hover:text-gray-600 mt-3 transition-colors">
            Skip for now (you can complete this later in Safety Plan)
          </button>
        </div>
      </div>
    </div>
  );
}
