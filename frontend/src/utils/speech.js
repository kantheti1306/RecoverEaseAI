/**
 * Speech utilities using Web Speech API
 */

export const speak = (text, rate = 0.9, pitch = 1) => {
  if (!('speechSynthesis' in window)) return;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.rate = rate;
  utterance.pitch = pitch;
  utterance.volume = 1;
  // Prefer a calm English voice
  const voices = window.speechSynthesis.getVoices();
  const preferred = voices.find(v =>
    v.lang.startsWith('en') && v.name.toLowerCase().includes('samantha')
  ) || voices.find(v => v.lang.startsWith('en')) || voices[0];
  if (preferred) utterance.voice = preferred;
  window.speechSynthesis.speak(utterance);
};

export const stopSpeaking = () => {
  if ('speechSynthesis' in window) window.speechSynthesis.cancel();
};

export const isSpeechSupported = () => 'speechSynthesis' in window;

/**
 * Start speech recognition. Returns { start, stop, isSupported }
 */
export const createSpeechRecognizer = (onResult, onEnd, onError) => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    return { start: () => {}, stop: () => {}, isSupported: false };
  }
  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = false;
  recognition.lang = 'en-US';

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    onResult(transcript);
  };
  recognition.onend = () => onEnd && onEnd();
  recognition.onerror = (e) => onError && onError(e.error);

  return {
    start: () => {
      try { recognition.start(); } catch (e) { console.warn('Recognition start error', e); }
    },
    stop: () => {
      try { recognition.stop(); } catch (e) {}
    },
    isSupported: true,
  };
};
