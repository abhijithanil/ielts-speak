import React, { createContext, useContext, useState, useRef, useEffect } from 'react';

const VoiceContext = createContext();

export const useVoice = () => {
  const context = useContext(VoiceContext);
  if (!context) {
    throw new Error('useVoice must be used within a VoiceProvider');
  }
  return context;
};

export const VoiceProvider = ({ children }) => {
  const [selectedVoice, setSelectedVoice] = useState('US');
  const [selectedGender, setSelectedGender] = useState('Female');
  const [showVoiceSettings, setShowVoiceSettings] = useState(false);
  const voiceSettingsRef = useRef(null);

  // Get voice by accent and gender
  const getVoiceByAccent = (accent, gender = selectedGender) => {
    const voices = window.speechSynthesis.getVoices();
    console.log('Available voices for accent selection:', voices.length);
    
    // Filter voices by accent first
    let filteredVoices = [];
    switch (accent) {
      case 'US':
        filteredVoices = voices.filter(voice => 
          voice.lang.startsWith('en-US') || 
          voice.name.includes('US') || 
          voice.name.includes('American')
        );
        break;
      case 'British':
        filteredVoices = voices.filter(voice => 
          voice.lang.startsWith('en-GB') || 
          voice.name.includes('British') || 
          voice.name.includes('UK')
        );
        break;
      case 'Australian':
        filteredVoices = voices.filter(voice => 
          voice.lang.startsWith('en-AU') || 
          voice.name.includes('Australian') || 
          voice.name.includes('Australia')
        );
        break;
      case 'Indian':
        filteredVoices = voices.filter(voice => 
          voice.lang.startsWith('en-IN') || 
          voice.name.includes('Indian') || 
          voice.name.includes('India')
        );
        break;
      default:
        filteredVoices = voices.filter(voice => voice.lang.startsWith('en'));
    }

    // If no voices found for the accent, fall back to any English voice
    if (filteredVoices.length === 0) {
      filteredVoices = voices.filter(voice => voice.lang.startsWith('en'));
    }

    // Try to find a voice matching the gender preference
    let preferredVoice = null;
    if (gender === 'Male') {
      preferredVoice = filteredVoices.find(voice => 
        voice.name.toLowerCase().includes('male') ||
        voice.name.toLowerCase().includes('david') ||
        voice.name.toLowerCase().includes('james') ||
        voice.name.toLowerCase().includes('peter') ||
        voice.name.toLowerCase().includes('mark') ||
        voice.name.toLowerCase().includes('alex')
      );
    } else if (gender === 'Female') {
      preferredVoice = filteredVoices.find(voice => 
        voice.name.toLowerCase().includes('female') ||
        voice.name.toLowerCase().includes('samantha') ||
        voice.name.toLowerCase().includes('victoria') ||
        voice.name.toLowerCase().includes('karen') ||
        voice.name.toLowerCase().includes('lisa') ||
        voice.name.toLowerCase().includes('sarah')
      );
    }

    // If no gender-specific voice found, return the first available voice for the accent
    return preferredVoice || filteredVoices[0] || voices[0];
  };

  // Demo voice function
  const demoVoice = (accent, gender = selectedGender) => {
    const demoText = "This is a demo of the selected voice.";
    const voice = getVoiceByAccent(accent, gender);
    
    if (voice) {
      const utterance = new SpeechSynthesisUtterance(demoText);
      utterance.voice = voice;
      utterance.rate = 0.8;
      utterance.volume = 1.0;
      
      utterance.onstart = () => console.log('Demo voice started');
      utterance.onend = () => console.log('Demo voice ended');
      utterance.onerror = (event) => console.error('Demo voice error:', event);
      
      window.speechSynthesis.cancel();
      window.speechSynthesis.speak(utterance);
    }
  };

  // Handle voice selection
  const handleVoiceSelection = (accent) => {
    setSelectedVoice(accent);
    demoVoice(accent, selectedGender);
  };

  // Handle gender selection
  const handleGenderSelection = (gender) => {
    setSelectedGender(gender);
    demoVoice(selectedVoice, gender);
  };

  // Click outside handler for voice settings
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (voiceSettingsRef.current && !voiceSettingsRef.current.contains(event.target)) {
        setShowVoiceSettings(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const value = {
    selectedVoice,
    setSelectedVoice,
    selectedGender,
    setSelectedGender,
    showVoiceSettings,
    setShowVoiceSettings,
    voiceSettingsRef,
    getVoiceByAccent,
    demoVoice,
    handleVoiceSelection,
    handleGenderSelection,
  };

  return (
    <VoiceContext.Provider value={value}>
      {children}
    </VoiceContext.Provider>
  );
};
