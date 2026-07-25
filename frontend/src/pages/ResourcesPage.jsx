import { useState, useEffect } from 'react';
import { getResources, explainTopic } from '../api/resourceApi';

const CATEGORIES = [
  { value: '', label: '🌐 All' },
  { value: 'RELAPSE', label: '⚠️ Relapse' },
  { value: 'COPING', label: '🧘 Coping' },
  { value: 'OVERDOSE', label: '🚨 Overdose' },
  { value: 'FAMILY', label: '👨‍👩‍👧 Family' },
  { value: 'RECOVERY', label: '🌿 Recovery' },
];

export default function ResourcesPage() {
  const [resources, setResources] = useState([]);
  const [category, setCategory] = useState('');
  const [search, setSearch] = useState('');
  const [question, setQuestion] = useState('');
  const [aiAnswer, setAiAnswer] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadResources();
  }, [category]);

  const loadResources = async () => {
    setLoading(true);
    try {
      const res = await getResources(category ? { category } : {});
      setResources(res.data || []);
    } catch { }
    finally { setLoading(false); }
  };

  const handleSearch = async () => {
    if (!search.trim()) return loadResources();
    setLoading(true);
    try {
      const res = await getResources({ search });
      setResources(res.data || []);
    } catch { }
    finally { setLoading(false); }
  };

  const handleAskAI = async () => {
    if (!question.trim()) return;
    setAiLoading(true);
    setAiError('');
    setAiAnswer(null);
    try {
      const res = await explainTopic(question);
      setAiAnswer(res.data);
    } catch {
      setAiError('Failed to get AI explanation. Please try again.');
    } finally {
      setAiLoading(false);
    }
  };

  const CATEGORY_COLORS = {
    RELAPSE: 'bg-yellow-100 text-yellow-700',
    COPING: 'bg-blue-100 text-blue-700',
    OVERDOSE: 'bg-red-100 text-red-700',
    FAMILY: 'bg-purple-100 text-purple-700',
    RECOVERY: 'bg-green-100 text-green-700',
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-24 md:pb-6">
      <div className="max-w-2xl mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">📚 Resource Hub</h1>
          <p className="text-gray-500 text-sm mt-1">Trusted recovery knowledge, explained by AI in plain language.</p>
        </div>

        {/* AI Ask-anything box */}
        <div className="card mb-6 bg-gradient-to-br from-primary-50 to-calm-50 border-2 border-primary-200">
          <p className="font-semibold text-gray-800 mb-2">🧠 Ask AI anything about recovery</p>
          <p className="text-xs text-gray-500 mb-3">Powered by Google Gemini AI — references SAMHSA, NIDA, WHO, CDC</p>
          <div className="flex gap-2">
            <input className="input-field flex-1"
              value={question} onChange={e => setQuestion(e.target.value)}
              placeholder="e.g. What are signs of relapse? What is harm reduction?"
              onKeyDown={e => e.key === 'Enter' && handleAskAI()}
              aria-label="Ask a recovery question" />
            <button onClick={handleAskAI} disabled={aiLoading}
              className="btn-primary px-4 whitespace-nowrap" aria-label="Ask AI">
              {aiLoading ? '...' : 'Ask AI'}
            </button>
          </div>

          {aiError && <p className="text-red-500 text-sm mt-2">{aiError}</p>}

          {aiAnswer && (
            <div className="mt-4 bg-white rounded-xl p-4 border border-primary-200">
              <p className="text-gray-700 leading-relaxed text-sm">{aiAnswer.answer}</p>
              {aiAnswer.source && (
                <p className="text-xs text-gray-400 mt-2">📖 Source: {aiAnswer.source}</p>
              )}
            </div>
          )}
        </div>

        {/* Category filter */}
        <div className="flex gap-2 overflow-x-auto pb-2 mb-4">
          {CATEGORIES.map(c => (
            <button key={c.value} onClick={() => setCategory(c.value)}
              aria-pressed={category === c.value}
              className={`whitespace-nowrap text-sm px-4 py-2 rounded-xl border font-medium transition-all flex-shrink-0
                ${category === c.value
                  ? 'bg-primary-600 text-white border-primary-600'
                  : 'bg-white border-gray-200 text-gray-600 hover:border-gray-300'}`}>
              {c.label}
            </button>
          ))}
        </div>

        {/* Search */}
        <div className="flex gap-2 mb-5">
          <input className="input-field flex-1"
            value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search resources..."
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            aria-label="Search resources" />
          <button onClick={handleSearch} className="btn-secondary px-4" aria-label="Search">🔍</button>
        </div>

        {/* Resources list */}
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="w-8 h-8 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"></div>
          </div>
        ) : resources.length === 0 ? (
          <div className="text-center py-12 text-gray-400">
            <p className="text-4xl mb-3">📭</p>
            <p>No resources found. Try a different search.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {resources.map(r => (
              <div key={r.id} className="card hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-bold text-gray-800 text-base">{r.title}</h3>
                  <span className={`text-xs font-medium px-2 py-1 rounded-full ml-2 flex-shrink-0 ${CATEGORY_COLORS[r.category] || 'bg-gray-100 text-gray-600'}`}>
                    {r.category}
                  </span>
                </div>
                <p className="text-sm text-gray-600 leading-relaxed mb-3">{r.summary}</p>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-400">📖 {r.sourceName}</span>
                  {r.sourceUrl && (
                    <a href={r.sourceUrl} target="_blank" rel="noopener noreferrer"
                      className="text-xs text-primary-600 hover:underline font-medium"
                      aria-label={`Visit ${r.sourceName}`}>
                      Visit source →
                    </a>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Helpline */}
        <div className="mt-8 bg-primary-50 border border-primary-200 rounded-2xl p-4 text-center">
          <p className="text-sm font-semibold text-primary-700">Need immediate help?</p>
          <a href="tel:18006624357" className="text-xl font-bold text-primary-600 hover:underline">
            1-800-662-4357
          </a>
          <p className="text-xs text-gray-400 mt-1">SAMHSA National Helpline • Free, confidential, 24/7</p>
        </div>
      </div>
    </div>
  );
}
