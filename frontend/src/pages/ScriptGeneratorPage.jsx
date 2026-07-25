import { useState, useEffect } from 'react';
import { generateScript, getScriptHistory } from '../api/scriptApi';
import { speak, stopSpeaking } from '../utils/speech';

const SCENARIOS = [
  { value: 'craving', label: '🌊 Managing a Craving', desc: 'Script for when urges hit hard' },
  { value: 'relapse_risk', label: '⚠️ High Relapse Risk', desc: 'Script to get through a dangerous moment' },
  { value: 'refusal', label: '🚫 Refusing Substances', label2: 'What to say when offered' },
  { value: 'grounding', label: '🧘 Grounding & Calming', desc: 'A script to read during panic' },
  { value: 'reaching_out', label: '📞 Asking for Help', desc: 'What to say to your support person' },
];

const AUDIENCES = [
  { value: 'self', label: '👤 For Myself', desc: 'Script I read to myself' },
  { value: 'caregiver', label: '🤝 For My Caregiver', desc: 'What my caregiver should say' },
  { value: 'support_person', label: '👥 For My Support Person', desc: 'Script for a friend or sponsor' },
];

export default function ScriptGeneratorPage() {
  const [scenario, setScenario] = useState('');
  const [audience, setAudience] = useState('self');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [history, setHistory] = useState([]);
  const [speaking, setSpeaking] = useState(false);

  useEffect(() => {
    getScriptHistory().then(res => setHistory(res.data || [])).catch(() => {});
  }, []);

  const handleGenerate = async () => {
    if (!scenario) return alert('Please select a scenario.');
    setLoading(true);
    setError('');
    setResult(null);
    stopSpeaking();
    try {
      const res = await generateScript({ scenario, audience });
      setResult(res.data);
      const hist = await getScriptHistory();
      setHistory(hist.data || []);
    } catch (err) {
      setError('Failed to generate script. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSpeak = () => {
    if (!result) return;
    if (speaking) {
      stopSpeaking(); setSpeaking(false);
    } else {
      setSpeaking(true);
      speak(result.script, 0.85);
      setTimeout(() => setSpeaking(false), 15000);
    }
  };

  const handleCopy = () => {
    if (result?.script) navigator.clipboard?.writeText(result.script);
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-24 md:pb-6">
      <div className="max-w-lg mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">📝 Emergency Scripts</h1>
          <p className="text-gray-500 text-sm mt-1">AI generates personalized scripts for your specific situation.</p>
        </div>

        <div className="card mb-4">
          <p className="font-semibold text-gray-700 mb-3">What's the situation?</p>
          <div className="space-y-2">
            {SCENARIOS.map(s => (
              <button key={s.value} type="button"
                onClick={() => setScenario(s.value)}
                aria-pressed={scenario === s.value}
                className={`w-full border-2 rounded-xl p-3 text-left transition-all flex items-start gap-3
                  ${scenario === s.value
                    ? 'border-primary-500 bg-primary-50'
                    : 'border-gray-200 bg-white hover:border-gray-300'}`}>
                <div>
                  <p className="font-medium text-sm text-gray-800">{s.label}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{s.desc || s.label2}</p>
                </div>
              </button>
            ))}
          </div>
        </div>

        <div className="card mb-4">
          <p className="font-semibold text-gray-700 mb-3">Who is this script for?</p>
          <div className="space-y-2">
            {AUDIENCES.map(a => (
              <button key={a.value} type="button"
                onClick={() => setAudience(a.value)}
                aria-pressed={audience === a.value}
                className={`w-full border-2 rounded-xl p-3 text-left transition-all flex items-start gap-3
                  ${audience === a.value
                    ? 'border-calm-500 bg-calm-50'
                    : 'border-gray-200 bg-white hover:border-gray-300'}`}>
                <div>
                  <p className="font-medium text-sm text-gray-800">{a.label}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{a.desc}</p>
                </div>
              </button>
            ))}
          </div>
        </div>

        {error && <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 text-sm mb-4">{error}</div>}

        <button onClick={handleGenerate} disabled={loading} className="btn-primary w-full mb-6">
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              Generating AI Script...
            </span>
          ) : '✨ Generate AI Script'}
        </button>

        {/* Result */}
        {result && (
          <div className="card bg-gradient-to-br from-primary-50 to-calm-50 border-2 border-primary-200 mb-6">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-bold text-gray-800">{result.title}</h2>
              <span className="text-xs bg-primary-100 text-primary-700 px-2 py-1 rounded-full font-medium">AI Generated</span>
            </div>
            <div className="bg-white rounded-xl p-4 mb-4 border border-gray-200">
              <p className="text-gray-700 leading-relaxed text-sm italic">"{result.script}"</p>
            </div>
            <div className="flex gap-2">
              <button onClick={handleSpeak}
                className="flex-1 flex items-center justify-center gap-2 bg-primary-600 hover:bg-primary-700 text-white text-sm font-medium py-2 px-4 rounded-xl transition-colors"
                aria-label={speaking ? 'Stop reading' : 'Read aloud'}>
                {speaking ? '🔇 Stop' : '🔊 Read Aloud'}
              </button>
              <button onClick={handleCopy}
                className="flex-1 flex items-center justify-center gap-2 bg-white border border-gray-200 hover:bg-gray-50 text-gray-700 text-sm font-medium py-2 px-4 rounded-xl transition-colors">
                📋 Copy
              </button>
            </div>
          </div>
        )}

        {/* History */}
        {history.length > 0 && (
          <div>
            <h2 className="text-sm font-semibold text-gray-500 uppercase mb-3">Previous Scripts</h2>
            <div className="space-y-2">
              {history.slice(0, 5).map(s => (
                <div key={s.id} className="card py-3 px-4">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-bold bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full capitalize">
                      {s.scenario?.replace('_', ' ')}
                    </span>
                    <span className="text-xs text-gray-400">• {s.audience} •</span>
                    <span className="text-xs text-gray-400">{new Date(s.createdAt).toLocaleDateString()}</span>
                  </div>
                  <p className="text-sm text-gray-600 italic line-clamp-2">"{s.scriptText}"</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
