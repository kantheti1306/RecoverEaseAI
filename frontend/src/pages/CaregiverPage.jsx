import { useState } from 'react';
import { getCaregiverGuidance } from '../api/caregiverApi';
import { speak, stopSpeaking } from '../utils/speech';

const SCENARIOS = [
  { value: 'anxious', label: '😰 Person is anxious or panicked', icon: '😰' },
  { value: 'angry', label: '😤 Person is angry or agitated', icon: '😤' },
  { value: 'possible_relapse', label: '⚠️ Possible relapse situation', icon: '⚠️' },
  { value: 'withdrawn', label: '😶 Person is withdrawn or isolating', icon: '😶' },
  { value: 'overdose_concern', label: '🚨 Suspected overdose (EMERGENCY)', icon: '🚨' },
];

export default function CaregiverPage() {
  const [scenario, setScenario] = useState('');
  const [context, setContext] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [speaking, setSpeaking] = useState(false);

  const handleSubmit = async () => {
    if (!scenario) return alert('Please select a scenario.');
    setLoading(true);
    setError('');
    setResult(null);
    stopSpeaking();
    try {
      const res = await getCaregiverGuidance({ scenario, context });
      setResult(res.data);
    } catch (err) {
      setError('Failed to get guidance. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSpeak = () => {
    if (!result) return;
    if (speaking) { stopSpeaking(); setSpeaking(false); return; }
    const text = `What to say: ${result.whatToSay}. Next steps: ${result.nextSteps?.join('. ')}`;
    setSpeaking(true);
    speak(text, 0.9);
    setTimeout(() => setSpeaking(false), 20000);
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-24 md:pb-6">
      <div className="max-w-lg mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">🤝 Caregiver Companion</h1>
          <p className="text-gray-500 text-sm mt-1">AI-powered guidance for supporting your loved one right now.</p>
        </div>

        {!result ? (
          <>
            <div className="card mb-4">
              <p className="font-semibold text-gray-700 mb-3">What's happening right now?</p>
              <div className="space-y-2">
                {SCENARIOS.map(s => (
                  <button key={s.value} type="button"
                    onClick={() => setScenario(s.value)}
                    aria-pressed={scenario === s.value}
                    className={`w-full border-2 rounded-xl p-4 text-left transition-all
                      ${scenario === s.value
                        ? s.value === 'overdose_concern'
                          ? 'border-red-500 bg-red-50'
                          : 'border-primary-500 bg-primary-50'
                        : 'border-gray-200 bg-white hover:border-gray-300'}`}>
                    <p className="font-medium text-gray-800 text-sm">{s.label}</p>
                  </button>
                ))}
              </div>
            </div>

            <div className="card mb-4">
              <label className="font-semibold text-gray-700 block mb-2" htmlFor="context">
                Any additional context? (optional)
              </label>
              <textarea id="context" className="input-field" rows={3}
                value={context} onChange={e => setContext(e.target.value)}
                placeholder="e.g. They seem more withdrawn than usual, won't answer calls..." />
            </div>

            {error && <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 text-sm mb-4">{error}</div>}

            <button onClick={handleSubmit} disabled={loading} className="btn-primary w-full">
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                  Getting AI guidance...
                </span>
              ) : '🧠 Get AI Guidance'}
            </button>
          </>
        ) : (
          <div>
            {/* Emergency alert for overdose */}
            {scenario === 'overdose_concern' && (
              <div className="bg-red-600 text-white rounded-2xl p-5 mb-4">
                <p className="font-bold text-lg">🚨 Suspected Overdose — Call 911 NOW</p>
                <p className="text-red-100 text-sm mt-1">Do not leave the person alone. Stay on the line with emergency services.</p>
                <a href="tel:911" className="mt-3 bg-white text-red-600 font-bold px-5 py-2 rounded-xl text-sm hover:bg-red-50 inline-block transition-colors">
                  📞 Call 911
                </a>
              </div>
            )}

            <div className="card mb-4 bg-blue-50 border-2 border-blue-200">
              <div className="flex items-center justify-between mb-3">
                <h2 className="font-bold text-gray-800">📢 What to Say</h2>
                <button onClick={handleSpeak}
                  className="text-sm bg-white border border-gray-200 rounded-lg px-3 py-1 hover:bg-gray-50 transition-colors"
                  aria-label={speaking ? 'Stop reading' : 'Read aloud'}>
                  {speaking ? '🔇 Stop' : '🔊 Read Aloud'}
                </button>
              </div>
              <div className="bg-white rounded-xl p-4 border border-gray-200">
                <p className="text-gray-700 leading-relaxed italic text-sm">"{result.whatToSay}"</p>
              </div>
            </div>

            {result.avoidSaying?.length > 0 && (
              <div className="card mb-4 bg-red-50 border-2 border-red-200">
                <h2 className="font-bold text-gray-800 mb-3">🚫 Do NOT Say</h2>
                <div className="space-y-2">
                  {result.avoidSaying.map((a, i) => (
                    <div key={i} className="flex items-start gap-2 bg-white rounded-lg p-2 border border-red-200">
                      <span className="text-red-400 font-bold">✗</span>
                      <p className="text-sm text-gray-700">{a}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {result.nextSteps?.length > 0 && (
              <div className="card mb-5 bg-green-50 border-2 border-green-200">
                <h2 className="font-bold text-gray-800 mb-3">✅ Immediate Next Steps</h2>
                <div className="space-y-2">
                  {result.nextSteps.map((step, i) => (
                    <div key={i} className="flex items-start gap-3 bg-white rounded-lg p-3 border border-green-200">
                      <span className="bg-green-500 text-white text-xs font-bold w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0">
                        {i + 1}
                      </span>
                      <p className="text-sm text-gray-700">{step}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <button onClick={() => setResult(null)} className="btn-secondary w-full">
              ← Back to Scenarios
            </button>

            <div className="mt-4 bg-gray-100 rounded-xl p-3 text-center">
              <p className="text-xs text-gray-400">For overdose: <strong>Call 911</strong> immediately.
                SAMHSA Helpline: <strong>1-800-662-4357</strong></p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
