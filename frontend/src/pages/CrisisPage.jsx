import { useState, useRef } from 'react';
import { sendCrisisInput } from '../api/crisisApi';
import { speak, stopSpeaking } from '../utils/speech';
import VoiceRecorder from '../components/VoiceRecorder';

const RISK_BG = {
  LOW: 'bg-green-50 border-green-300',
  MEDIUM: 'bg-yellow-50 border-yellow-300',
  HIGH: 'bg-red-50 border-red-300',
};

const QUICK_PROMPTS = [
  { label: '😰 I have a craving', text: 'I am experiencing a strong craving right now and I need help.' },
  { label: '😔 I feel unsafe', text: 'I feel unsafe and overwhelmed right now.' },
  { label: '😤 I am really stressed', text: 'I am very stressed and struggling to cope.' },
  { label: '😢 I feel like giving up', text: 'I am struggling and feeling like giving up on recovery.' },
];

export default function CrisisPage() {
  const [input, setInput] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isListening, setIsListening] = useState(false);
  const [speaking, setSpeaking] = useState(false);

  const handleSubmit = async (textToSend) => {
    const text = textToSend || input;
    if (!text.trim()) return;
    setLoading(true);
    setError('');
    setResponse(null);
    stopSpeaking();
    try {
      const res = await sendCrisisInput({ inputText: text, mode: 'text' });
      setResponse(res.data);
      // Auto-speak the response
      setTimeout(() => {
        speak(res.data.ttsText || res.data.message);
      }, 300);
    } catch (err) {
      setError('Failed to get AI response. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleVoiceTranscript = (transcript) => {
    setInput(transcript);
    handleSubmit(transcript);
  };

  const handleSpeak = () => {
    if (!response) return;
    if (speaking) {
      stopSpeaking();
      setSpeaking(false);
    } else {
      setSpeaking(true);
      speak(`${response.message}. ${response.steps?.join('. ')}`);
      setTimeout(() => setSpeaking(false), 8000);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-gray-50 pb-24 md:pb-6">
      <div className="max-w-lg mx-auto px-4 py-6">

        {/* Header */}
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Crisis Support</h1>
          <p className="text-gray-500 text-sm mt-1">Speak or type what you're feeling. AI is here to help.</p>
        </div>

        {/* Voice recorder */}
        <div className="card mb-5 text-center">
          <p className="text-sm text-gray-500 mb-4 font-medium">🎤 Zero-typing: Just tap and speak</p>
          <VoiceRecorder
            onTranscript={handleVoiceTranscript}
            isListening={isListening}
            setIsListening={setIsListening}
          />
        </div>

        {/* Quick prompts */}
        <div className="mb-4">
          <p className="text-xs text-gray-400 font-medium mb-2">Or tap a quick option:</p>
          <div className="grid grid-cols-2 gap-2">
            {QUICK_PROMPTS.map(p => (
              <button key={p.label}
                onClick={() => { setInput(p.text); handleSubmit(p.text); }}
                className="text-sm bg-white border border-gray-200 hover:border-primary-300 hover:bg-primary-50 rounded-xl p-3 text-left transition-all duration-150 font-medium text-gray-700"
                aria-label={p.label}>
                {p.label}
              </button>
            ))}
          </div>
        </div>

        {/* Text input */}
        <div className="mb-4">
          <textarea
            className="input-field"
            rows={3}
            placeholder="Or type how you're feeling right now..."
            value={input}
            onChange={e => setInput(e.target.value)}
            aria-label="Describe how you're feeling"
          />
          <button
            onClick={() => handleSubmit()}
            disabled={loading || !input.trim()}
            className="btn-primary w-full mt-2"
            aria-label="Get AI crisis support"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                Getting AI support...
              </span>
            ) : '🧠 Get AI Support'}
          </button>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-4 mb-4 text-sm" role="alert">
            {error}
          </div>
        )}

        {/* AI Response */}
        {response && (
          <div className={`border-2 rounded-2xl p-5 mb-4 ${RISK_BG[response.riskLevel] || RISK_BG.MEDIUM}`}>
            {/* Risk level */}
            <div className="flex items-center justify-between mb-3">
              <span className={`text-xs font-bold px-3 py-1 rounded-full border risk-badge-${response.riskLevel}`}>
                {response.riskLevel} RISK
              </span>
              <button
                onClick={handleSpeak}
                className="text-sm bg-white border border-gray-200 rounded-lg px-3 py-1 hover:bg-gray-50 transition-colors"
                aria-label={speaking ? 'Stop reading aloud' : 'Read aloud'}
              >
                {speaking ? '🔇 Stop' : '🔊 Read Aloud'}
              </button>
            </div>

            {/* Emergency escalation */}
            {response.escalate && (
              <div className="bg-red-600 text-white rounded-xl p-3 mb-3 flex items-center gap-2">
                <span className="text-xl">🚨</span>
                <div>
                  <p className="font-bold text-sm">Please call emergency services immediately</p>
                  <p className="text-xs text-red-100">Dial 911 or your local emergency number</p>
                </div>
              </div>
            )}

            {/* AI Message */}
            <p className="text-gray-800 font-medium mb-4 leading-relaxed text-lg">{response.message}</p>

            {/* Steps */}
            {response.steps?.length > 0 && (
              <div className="mb-4">
                <p className="text-xs font-bold text-gray-500 uppercase mb-2">Next Steps:</p>
                <div className="space-y-2">
                  {response.steps.map((step, i) => (
                    <div key={i} className="flex items-start gap-2 bg-white bg-opacity-60 rounded-lg p-2">
                      <span className="text-primary-600 font-bold text-sm">{i + 1}.</span>
                      <p className="text-sm text-gray-700">{step}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Emergency Script */}
            {response.script && (
              <div className="bg-white bg-opacity-70 rounded-xl p-4 mb-4 border border-gray-200">
                <p className="text-xs font-bold text-gray-500 uppercase mb-2">📝 Emergency Script (say this):</p>
                <p className="text-sm text-gray-700 italic">"{response.script}"</p>
                <button
                  onClick={() => { navigator.clipboard?.writeText(response.script); }}
                  className="text-xs text-primary-600 hover:underline mt-2"
                >
                  📋 Copy script
                </button>
              </div>
            )}

            {/* Contact support */}
            {response.contactName && (
              <div className="bg-primary-600 text-white rounded-xl p-4 flex items-center justify-between">
                <div>
                  <p className="text-xs font-medium text-primary-100">Your Support Contact</p>
                  <p className="font-bold">{response.contactName}</p>
                  {response.contactPhone && <p className="text-sm text-primary-100">{response.contactPhone}</p>}
                </div>
                {response.contactPhone && (
                  <a href={`tel:${response.contactPhone}`}
                    className="bg-white text-primary-600 font-bold px-4 py-2 rounded-lg text-sm hover:bg-primary-50 transition-colors"
                    aria-label={`Call ${response.contactName}`}>
                    📞 Call
                  </a>
                )}
              </div>
            )}
          </div>
        )}

        {/* National helpline */}
        <div className="bg-gray-100 rounded-2xl p-4 text-center">
          <p className="text-sm font-semibold text-gray-700">🆘 National Substance Use Helpline</p>
          <a href="tel:18006624357" className="text-lg font-bold text-primary-600 hover:underline">
            1-800-662-4357
          </a>
          <p className="text-xs text-gray-400 mt-1">SAMHSA • Free, confidential, 24/7</p>
        </div>
      </div>
    </div>
  );
}
