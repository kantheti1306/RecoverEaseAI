import { useState, useEffect } from 'react';
import { getProfile, completeOnboarding } from '../api/userApi';
import { useAuth } from '../context/AuthContext';

export default function SafetyPlanPage() {
  const { updateOnboarding } = useAuth();
  const [form, setForm] = useState({
    triggers: '',
    calmingStrategies: '',
    warningSignsPersonal: '',
    personalReminder: '',
    primaryContactName: '',
    primaryContactPhone: '',
    primaryContactRelation: '',
    consentToAlert: false,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    getProfile().then(res => {
      const d = res.data;
      setForm({
        triggers: d.triggers || '',
        calmingStrategies: d.calmingStrategies || '',
        warningSignsPersonal: d.warningSignsPersonal || '',
        personalReminder: d.personalReminder || '',
        primaryContactName: d.primaryContactName || '',
        primaryContactPhone: d.primaryContactPhone || '',
        primaryContactRelation: d.primaryContactRelation || '',
        consentToAlert: d.consentToAlert || false,
      });
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleSave = async () => {
    setSaving(true);
    try {
      await completeOnboarding(form);
      updateOnboarding();
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch {
      alert('Failed to save. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="w-8 h-8 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-24 md:pb-6">
      <div className="max-w-lg mx-auto px-4 py-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">🛡️ My Safety Plan</h1>
          <p className="text-gray-500 text-sm mt-1">Your personalized safety profile used by AI during crisis support.</p>
        </div>

        {saved && (
          <div className="bg-green-50 border border-green-200 text-green-700 rounded-xl p-3 mb-4 text-sm font-medium">
            ✅ Safety plan saved successfully!
          </div>
        )}

        <div className="space-y-4">
          <div className="card">
            <label className="font-semibold text-gray-700 block mb-2">🌊 My Triggers</label>
            <textarea className="input-field" rows={3}
              value={form.triggers}
              onChange={e => setForm(p => ({ ...p, triggers: e.target.value }))}
              placeholder="What situations or feelings trigger cravings?" />
          </div>

          <div className="card">
            <label className="font-semibold text-gray-700 block mb-2">🧘 Calming Strategies</label>
            <textarea className="input-field" rows={3}
              value={form.calmingStrategies}
              onChange={e => setForm(p => ({ ...p, calmingStrategies: e.target.value }))}
              placeholder="What helps you calm down when overwhelmed?" />
          </div>

          <div className="card">
            <label className="font-semibold text-gray-700 block mb-2">⚠️ My Warning Signs</label>
            <textarea className="input-field" rows={3}
              value={form.warningSignsPersonal}
              onChange={e => setForm(p => ({ ...p, warningSignsPersonal: e.target.value }))}
              placeholder="What are your personal warning signs of a bad day?" />
          </div>

          <div className="card">
            <label className="font-semibold text-gray-700 block mb-2">💬 Personal Reminder</label>
            <textarea className="input-field" rows={3}
              value={form.personalReminder}
              onChange={e => setForm(p => ({ ...p, personalReminder: e.target.value }))}
              placeholder="A message you want to hear during tough moments..." />
            <p className="text-xs text-gray-400 mt-1">💡 AI uses this during crisis mode to personalize support for you.</p>
          </div>

          <div className="card">
            <p className="font-semibold text-gray-700 mb-3">📞 Emergency Contact</p>
            <div className="space-y-3">
              <input className="input-field" placeholder="Contact name"
                value={form.primaryContactName}
                onChange={e => setForm(p => ({ ...p, primaryContactName: e.target.value }))} />
              <input className="input-field" placeholder="Phone number" type="tel"
                value={form.primaryContactPhone}
                onChange={e => setForm(p => ({ ...p, primaryContactPhone: e.target.value }))} />
              <input className="input-field" placeholder="Relation (e.g. Mom, Sponsor, Friend)"
                value={form.primaryContactRelation}
                onChange={e => setForm(p => ({ ...p, primaryContactRelation: e.target.value }))} />
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" className="w-4 h-4 accent-primary-600"
                  checked={form.consentToAlert}
                  onChange={e => setForm(p => ({ ...p, consentToAlert: e.target.checked }))} />
                <span className="text-sm text-gray-600">Show this contact during AI crisis sessions</span>
              </label>
            </div>
          </div>
        </div>

        <button onClick={handleSave} disabled={saving} className="btn-primary w-full mt-6">
          {saving ? 'Saving...' : '💾 Save Safety Plan'}
        </button>

        <div className="mt-4 bg-gray-100 rounded-xl p-3 text-center">
          <p className="text-xs text-gray-400">
            🔒 Your safety plan is private and securely stored.
            It is only used to personalize AI responses for you.
          </p>
        </div>
      </div>
    </div>
  );
}
