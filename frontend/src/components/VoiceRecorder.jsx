import { useState, useEffect, useRef } from 'react';
import { createSpeechRecognizer } from '../utils/speech';

export default function VoiceRecorder({ onTranscript, isListening, setIsListening }) {
  const [supported, setSupported] = useState(true);
  const recognizerRef = useRef(null);

  useEffect(() => {
    const rec = createSpeechRecognizer(
      (transcript) => {
        onTranscript(transcript);
        setIsListening(false);
      },
      () => setIsListening(false),
      () => setIsListening(false)
    );
    recognizerRef.current = rec;
    setSupported(rec.isSupported);
    return () => rec.stop();
  }, []);

  const toggleListening = () => {
    if (isListening) {
      recognizerRef.current?.stop();
      setIsListening(false);
    } else {
      recognizerRef.current?.start();
      setIsListening(true);
    }
  };

  if (!supported) {
    return (
      <p className="text-xs text-gray-400 text-center mt-2">
        Voice not supported in this browser. Please type below.
      </p>
    );
  }

  return (
    <div className="flex flex-col items-center gap-3">
      <button
        onClick={toggleListening}
        aria-label={isListening ? 'Stop listening' : 'Start voice input'}
        className={`w-24 h-24 rounded-full flex items-center justify-center shadow-lg transition-all duration-300 text-4xl
          ${isListening
            ? 'bg-red-500 animate-pulse-slow scale-110 ring-4 ring-red-200'
            : 'bg-primary-600 hover:bg-primary-700 hover:scale-105'
          }`}
      >
        {isListening ? '🔴' : '🎤'}
      </button>
      <p className="text-sm text-gray-500 font-medium">
        {isListening ? 'Listening... tap to stop' : 'Tap to speak'}
      </p>
    </div>
  );
}
