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
  const [voicesLoaded, setVoicesLoaded] = useState(false);
  const voiceSettingsRef = useRef(null);

  // Load voices when component mounts
  useEffect(() => {
    const loadVoices = () => {
      const voices = window.speechSynthesis.getVoices();
      if (voices.length > 0) {
        setVoicesLoaded(true);
        console.log('Voices loaded:', voices.length);
      }
    };

    // Load voices immediately if available
    loadVoices();

    // Also listen for the voiceschanged event
    window.speechSynthesis.addEventListener('voiceschanged', loadVoices);

    return () => {
      window.speechSynthesis.removeEventListener('voiceschanged', loadVoices);
    };
  }, []);

  // Get voice by accent and gender with improved logic
  const getVoiceByAccent = (accent, gender = selectedGender) => {
    if (!voicesLoaded) {
      console.log('Voices not loaded yet');
      return null;
    }

    const voices = window.speechSynthesis.getVoices();
    console.log(`Getting voice for accent: ${accent}, gender: ${gender}`);
    console.log('Available voices:', voices.map(v => ({ name: v.name, lang: v.lang })));
    
    // Define gender keywords for better matching
    const maleKeywords = ['male', 'man', 'david', 'james', 'peter', 'mark', 'alex', 'john', 'tom', 'daniel', 'arthur'];
    const femaleKeywords = ['female', 'woman', 'samantha', 'victoria', 'karen', 'lisa', 'sarah', 'emma', 'susan', 'anna', 'fiona'];
    
    // Filter voices by accent/locale
    let accentVoices = [];
    switch (accent) {
      case 'US':
        accentVoices = voices.filter(voice => 
          voice.lang.includes('en-US') || 
          voice.name.toLowerCase().includes('us') || 
          voice.name.toLowerCase().includes('american') ||
          voice.name.toLowerCase().includes('united states')
        );
        break;
      case 'British':
        accentVoices = voices.filter(voice => 
          voice.lang.includes('en-GB') || 
          voice.name.toLowerCase().includes('british') || 
          voice.name.toLowerCase().includes('uk') ||
          voice.name.toLowerCase().includes('england') ||
          voice.name.toLowerCase().includes('daniel') // Daniel is often British
        );
        break;
      case 'Australian':
        accentVoices = voices.filter(voice => 
          voice.lang.includes('en-AU') || 
          voice.name.toLowerCase().includes('australian') || 
          voice.name.toLowerCase().includes('australia')
        );
        break;
      case 'Indian':
        accentVoices = voices.filter(voice => 
          voice.lang.includes('en-IN') || 
          voice.name.toLowerCase().includes('indian') || 
          voice.name.toLowerCase().includes('india')
        );
        break;
      default:
        accentVoices = voices.filter(voice => voice.lang.startsWith('en'));
    }

    console.log(`Filtered voices for ${accent}:`, accentVoices.map(v => v.name));

    // If no specific accent voices found, fall back to any English voice
    if (accentVoices.length === 0) {
      accentVoices = voices.filter(voice => voice.lang.startsWith('en'));
      console.log('No accent-specific voices found, using all English voices:', accentVoices.length);
    }

    // Now filter by gender
    let genderFilteredVoices = [];
    const genderKeywords = gender === 'Male' ? maleKeywords : femaleKeywords;
    
    // First, try to find voices that explicitly match the gender
    genderFilteredVoices = accentVoices.filter(voice => {
      const nameLower = voice.name.toLowerCase();
      return genderKeywords.some(keyword => nameLower.includes(keyword));
    });

    console.log(`Gender filtered voices for ${gender}:`, genderFilteredVoices.map(v => v.name));

    // If we found gender-specific voices, use them
    if (genderFilteredVoices.length > 0) {
      const selectedVoice = genderFilteredVoices[0];
      console.log(`Selected voice: ${selectedVoice.name} for ${accent} ${gender}`);
      return selectedVoice;
    }

    // If no gender-specific voices found, try some heuristics
    // For many systems, the default voice or first voice might be female
    // and numbered voices (Voice 1, Voice 2) might alternate gender
    if (accentVoices.length > 0) {
      let fallbackVoice;
      
      if (gender === 'Female') {
        // Try to find a voice that doesn't contain obvious male keywords
        fallbackVoice = accentVoices.find(voice => {
          const nameLower = voice.name.toLowerCase();
          return !maleKeywords.some(keyword => nameLower.includes(keyword));
        });
      } else {
        // For male, try to find numbered voices (often Voice 2, Voice 4 are male)
        fallbackVoice = accentVoices.find(voice => {
          const nameLower = voice.name.toLowerCase();
          return nameLower.includes('voice 2') || nameLower.includes('voice 4') || 
                 nameLower.includes('voice 6') || nameLower.includes('voice 8');
        });
      }
      
      // If still no good match, just use the first or second voice based on gender preference
      if (!fallbackVoice) {
        if (gender === 'Male' && accentVoices.length > 1) {
          fallbackVoice = accentVoices[1]; // Often the second voice is male
        } else {
          fallbackVoice = accentVoices[0]; // Often the first voice is female
        }
      }
      
      console.log(`Using fallback voice: ${fallbackVoice.name} for ${accent} ${gender}`);
      return fallbackVoice;
    }

    // Final fallback - any available voice
    const finalFallback = voices[0];
    console.log(`Using final fallback voice: ${finalFallback?.name}`);
    return finalFallback;
  };

  // Demo voice function with better error handling
  const demoVoice = (accent, gender = selectedGender) => {
    if (!voicesLoaded) {
      console.log('Voices not loaded yet, skipping demo');
      return;
    }

    const demoText = `This is a demo of the ${accent} ${gender.toLowerCase()} voice.`;
    const voice = getVoiceByAccent(accent, gender);
    
    if (!voice) {
      console.log('No voice available for demo');
      return;
    }

    console.log(`Playing demo with voice: ${voice.name} (${voice.lang})`);
    
    // Cancel any ongoing speech
    window.speechSynthesis.cancel();
    
    // Small delay to ensure cancel completed
    setTimeout(() => {
      const utterance = new SpeechSynthesisUtterance(demoText);
      utterance.voice = voice;
      utterance.rate = 0.9;
      utterance.volume = 1.0;
      utterance.pitch = 1.0;
      
      utterance.onstart = () => console.log(`Demo voice started: ${voice.name}`);
      utterance.onend = () => console.log('Demo voice ended');
      utterance.onerror = (event) => {
        console.error('Demo voice error:', event);
        console.error('Error details:', { 
          error: event.error, 
          voice: voice.name, 
          lang: voice.lang 
        });
      };
      
      try {
        window.speechSynthesis.speak(utterance);
      } catch (error) {
        console.error('Error speaking utterance:', error);
      }
    }, 100);
  };

  // Handle voice selection with improved feedback
  const handleVoiceSelection = (accent) => {
    console.log(`Voice selection changed to: ${accent}`);
    setSelectedVoice(accent);
    
    // Demo the voice with current gender selection
    if (voicesLoaded) {
      // Small delay to ensure state update
      setTimeout(() => {
        demoVoice(accent, selectedGender);
      }, 200);
    }
  };

  // Handle gender selection with improved feedback
  const handleGenderSelection = (gender) => {
    console.log(`Gender selection changed to: ${gender}`);
    setSelectedGender(gender);
    
    // Demo the voice with new gender selection
    if (voicesLoaded) {
      // Small delay to ensure state update
      setTimeout(() => {
        demoVoice(selectedVoice, gender);
      }, 200);
    }
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

  // Debug function to list all available voices
  const debugVoices = () => {
    const voices = window.speechSynthesis.getVoices();
    console.log('=== All Available Voices ===');
    voices.forEach((voice, index) => {
      console.log(`${index + 1}. ${voice.name} (${voice.lang}) - Local: ${voice.localService}, Default: ${voice.default}`);
    });
    console.log('=== End Voice List ===');
  };

  // Log voice debugging info when voices are loaded
  useEffect(() => {
    if (voicesLoaded) {
      debugVoices();
    }
  }, [voicesLoaded]);

  const value = {
    selectedVoice,
    setSelectedVoice,
    selectedGender,
    setSelectedGender,
    showVoiceSettings,
    setShowVoiceSettings,
    voiceSettingsRef,
    voicesLoaded,
    getVoiceByAccent,
    demoVoice,
    handleVoiceSelection,
    handleGenderSelection,
    debugVoices,
  };

  return (
    <VoiceContext.Provider value={value}>
      {children}
    </VoiceContext.Provider>
  );
};