import { useState } from 'react';
import { submitCheckIn, getCheckInHistory } from '../api/checkinApi';
import { useEffect } from 'react';

const MOODS = [
  { value: 'great', label: '😁 Great', color: 'bg-green-100 border-green-300' },
  { value: 'good', label: '🙂 Good', color: 'bg-blue-100 border-blue-300' },
  { value: 'neutral', label: '😐 Neutral', color: 'bg-gray-100 border-gray-300' },
  { value: 'anxious', label: '😰 Anxious', color: 'bg-yellow-100 border-yellow-300' },
  { value: 'depressed', label: '😢 Low', color: 'bg-purple-100 border-purple-300' },
  { value: 'overwhelmed', label: '😩 Overwhelmed', color: 'bg-orange-100 border-orange-300' },
  { value: 'very_distressed', label: '😣 Distressed', color: 'bg-red-100 border-red-300' },
];

const RISK_COLORS = {
  LOW: { bg: 'bg-green-50', border: 'border-green-300', text: 'text-green-700', icon: '✅' },
  MEDIUM: { bg: 'bg-yellow-50', border: 'border-yellow-300', text: 'text-yellow-700', icon: '⚠️' },
  HIGH: { bg: 'bg-red-50', border: 'border-red-300', text: 'text-red-700', icon: '🚨' },
};

function Slider({ label, value, onChange, min = 1, max = 10 }) {
  const pct = ((value - min) / (max - min)) * 100;
  const color = pct > 66 ? 'text-red-500' : pct > 33 ? 'text-yellow-500' : 'text-green-500';
  return (
    <div>
      <div className="flex justify-between text-sm mb-1">
        <label className="font-medium text-gray-700">{label}</label>
        <span className={`font-bold ${color}`}>{value}/10</span>
      </div>
      <input type="range" min={min} max={max} value={value}
        onChange={e => onChange(Number(e.target.value))}
        className="w-full accent-primary-600 h-2"
        aria-label={label} />
      <div className="flex justify-between text-xs text-gray-400 mt-0.5">
        <span>Low</span><span>High</span>
      </div>
    </div>
  );
}

export default function CheckInPage() {
  const [mood, setMood] = useState('');
  const [cravingLevel, setCraving] = useState(3);
  const [stressLevel, setStress] = useState(3);
  const [sleepQuality, setSleep] = useState(7);
  const [voiceNote, setVoiceNote] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    getCheckInHistory().then(res => setHistory(res.data || [])).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!mood) return alert('Please select your mood.');
    setLoading(true);
    setError('');
    try {
      const res = await submitCheckIn({ mood, cravingLevel, stressLevel, sleepQuality, voiceNote });
      setResult(res.data);
      // Refresh history
      const hist = await getCheckInHistory();
      setHistory(hist.data || []);
    } catch (err) {
      setError('Failed to submit. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const risk = result ? RISK_COLORS[result.riskLevel] || RISK_COLORS.LOW : null;

  return (
    <div className="min-h-screen bg-gray-50 pb-24 md:pb-6">
      <div className="max-w-lg mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Daily Check-in</h1>
          <p className="text-gray-500 text-sm mt-1">How are you doing today? AI will analyze your response.</p>
        </div>

        {!result ? (
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Mood */}
            <div className="card">
              <p className="font-semibold text-gray-700 mb-3">How are you feeling right now?</p>
              <div className="grid grid-cols-2 gap-2">
                {MOODS.map(m => (
                  <button key={m.value} type="button"
                    onClick={() => setMood(m.value)}
                    aria-pressed={mood === m.value}
                    className={`border-2 rounded-xl p-3 text-sm font-medium transition-all
                      ${mood === m.value ? `${m.color} border-opacity-100 scale-95` : 'bg-white border-gray-200 hover:border-gray-300'}`}>
                    {m.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Sliders */}
            <div className="card space-y-5">
              <Slider label="🌊 Craving Level" value={cravingLevel} onChange={setCraving} />
              <Slider label="😤 Stress Level" value={stressLevel} onChange={setStress} />
              <Slider label="😴 Sleep Quality" value={sleepQuality} onChange={setSleep} />
            </div>

            {/* Optional note */}
            <div className="card">
              <label className="font-semibold text-gray-700 block mb-2" htmlFor="voiceNote">
                📝 Optional note (anything on your mind?)
              </label>
              <textarea id="voiceNote" className="input-field" rows={3}
                value={voiceNote} onChange={e => setVoiceNote(e.target.value)}
                placeholder="e.g. Had a rough morning, feeling tempted..." />
            </div>

            {error && <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 text-sm">{error}</div>}

            <button type="submit" className="btn-primary w-full" disabled={loading}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                  AI is analyzing...
                </span>
              ) : '🧠 Submit Check-in'}
            </button>
          </form>
        ) : (
          <div>
            {/* Result card */}
            <div className={`border-2 rounded-2xl p-6 mb-5 ${risk.bg} ${risk.border}`}>
              <div className="text-center mb-4">
                <span className="text-4xl">{risk.icon}</span>
                <h2 className={`text-2xl font-bold mt-2 ${risk.text}`}>{result.riskLevel} Risk</h2>
              </div>
              <p className="text-gray-700 mb-4 leading-relaxed">{result.summary}</p>

              <div>
                <p className="text-xs font-bold text-gray-500 uppercase mb-2">AI Suggestions:</p>
                <div className="space-y-2">
                  {result.suggestions?.map((s, i) => (
                    <div key={i} className="flex items-start gap-2 bg-white bg-opacity-70 rounded-lg p-2">
                      <span className="text-primary-600">•</span>
                      <p className="text-sm text-gray-700">{s}</p>
                    </div>
                  ))}
                </div>
              </div>

              {result.riskLevel === 'HIGH' && (
                <a href="/crisis" className="btn-danger w-full mt-4 text-center block">
                  🆘 Get Crisis Support Now
                </a>
              )}
            </div>

            <button onClick={() => setResult(null)} className="btn-secondary w-full">
              📋 New Check-in
            </button>
          </div>
        )}

        {/* History */}
        {history.length > 0 && (
          <div className="mt-8">
            <h2 className="text-sm font-semibold text-gray-500 uppercase mb-3">Recent Check-ins</h2>
            <div className="space-y-2">
              {history.slice(0, 5).map(h => (
                <div key={h.id} className="card py-3 px-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium capitalize text-gray-700">{h.mood?.replace('_', ' ')}</p>
                    <p className="text-xs text-gray-400">{new Date(h.createdAt).toLocaleDateString()}</p>
                  </div>
                  <span className={`text-xs font-bold px-2 py-1 rounded-full border risk-badge-${h.riskLevel}`}>
                    {h.riskLevel}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
