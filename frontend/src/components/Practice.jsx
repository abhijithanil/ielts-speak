import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Mic, Square, Play, Pause, RotateCcw, ArrowRight, CheckCircle, Clock, BookOpen, Target, AlertTriangle, Loader2, SkipForward, Volume2 } from 'lucide-react';
import { useVoice } from '../contexts/VoiceContext';
import api from '../utils/axios';
import config from '../utils/config';

const Practice = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [currentQuestion, setCurrentQuestion] = useState('');
  const [testSection, setTestSection] = useState('');
  const [isRecording, setIsRecording] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const [recordingTimeRemaining, setRecordingTimeRemaining] = useState(0);
  const [audioBlob, setAudioBlob] = useState(null);
  const [audioUrl, setAudioUrl] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [testType, setTestType] = useState(''); // 'complete', 'section', 'cue_card'
  const [bulletPoints, setBulletPoints] = useState([]);
  const [topic, setTopic] = useState('');
  const [analyses, setAnalyses] = useState([]);
  const [showSubmissionConfirm, setShowSubmissionConfirm] = useState(false);
  
  // New state for environment and timers
  const [questionTimer, setQuestionTimer] = useState(0);
  const [isTimerRunning, setIsTimerRunning] = useState(false);
  const [hasAnswered, setHasAnswered] = useState(false);
  const [environment] = useState(config.environment);

  // Text-to-speech state
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [autoSpeakEnabled, setAutoSpeakEnabled] = useState(true); // Default to enabled
  const [userHasInteracted, setUserHasInteracted] = useState(false);
  
  // Voice selection from context
  const { selectedVoice, getVoiceByAccent } = useVoice();

  // New state for automatic features
  const [preparationTimer, setPreparationTimer] = useState(null);
  const [preparationTimeRemaining, setPreparationTimeRemaining] = useState(0);
  const [isInPreparation, setIsInPreparation] = useState(false);
  const [autoRecordingTimer, setAutoRecordingTimer] = useState(null);

  const mediaRecorderRef = useRef(null);
  const audioRef = useRef(null);
  const timerRef = useRef(null);
  const questionTimerRef = useRef(null);
  const autoSpeakTimerRef = useRef(null);


  useEffect(() => {
    loadQuestions();
  }, [searchParams]);

  // Prevent navigation during practice test
  useEffect(() => {
    if (!isLoading && questions.length > 0) {
      // Disable browser back button
      const handlePopState = (event) => {
        event.preventDefault();
        event.stopPropagation();
        window.history.pushState(null, '', window.location.href);
        return false;
      };

      // Prevent page refresh/close
      const handleBeforeUnload = (event) => {
        event.preventDefault();
        event.returnValue = 'Are you sure you want to leave? Your progress will be lost.';
        return 'Are you sure you want to leave? Your progress will be lost.';
      };

      // Push current state to prevent back navigation
      window.history.pushState(null, '', window.location.href);

      // Add event listeners
      window.addEventListener('popstate', handlePopState);
      window.addEventListener('beforeunload', handleBeforeUnload);

      // Cleanup function
      return () => {
        window.removeEventListener('popstate', handlePopState);
        window.removeEventListener('beforeunload', handleBeforeUnload);
      };
    }
  }, [isLoading, questions.length]);

  // Initialize speech synthesis
  useEffect(() => {
    if ('speechSynthesis' in window) {
      // Load voices if needed
      if (window.speechSynthesis.getVoices().length === 0) {
        window.speechSynthesis.onvoiceschanged = () => {
          console.log('Voices loaded:', window.speechSynthesis.getVoices().length);
        };
      }
    }
  }, []);

  // Cleanup effect for all timers
  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      if (questionTimerRef.current) {
        clearInterval(questionTimerRef.current);
        questionTimerRef.current = null;
      }
      if (autoSpeakTimerRef.current) {
        clearTimeout(autoSpeakTimerRef.current);
        autoSpeakTimerRef.current = null;
      }
      if (preparationTimer) {
        clearInterval(preparationTimer);
      }
      if (autoRecordingTimer) {
        clearTimeout(autoRecordingTimer);
      }
    };
  }, [preparationTimer, autoRecordingTimer]);

  // Timer effect for question timing
  useEffect(() => {
    // Clear any existing timer first
    if (questionTimerRef.current) {
      clearInterval(questionTimerRef.current);
      questionTimerRef.current = null;
    }

    if (isTimerRunning && questionTimer > 0) {
      questionTimerRef.current = setInterval(() => {
        setQuestionTimer((prev) => {
          if (prev <= 1) {
            setIsTimerRunning(false);
            // Auto-submit when timer runs out
            if (!hasAnswered) {
              handleTimeUp();
            }
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (questionTimerRef.current) {
        clearInterval(questionTimerRef.current);
        questionTimerRef.current = null;
      }
    };
  }, [isTimerRunning, questionTimer, hasAnswered]);

  // Start timer when question changes
  useEffect(() => {
    if (questions.length > 0 && currentQuestionIndex < questions.length) {
      const currentQuestionData = questions[currentQuestionIndex];
      const section = currentQuestionData.section || testSection;
      const questionTimerDuration = config.questionTimers[section] || 30;
      const autoRecordingDelay = config.autoRecording[section] || 0;
      
      // Total timer duration = question timer + auto-recording delay (in seconds)
      const totalTimerDuration = questionTimerDuration + (autoRecordingDelay / 1000);
      
      setQuestionTimer(totalTimerDuration);
      setIsTimerRunning(true);
      setHasAnswered(false);
    }
  }, [currentQuestionIndex, questions, testSection]);

  const handleTimeUp = () => {
    setIsTimerRunning(false);
    if (!hasAnswered) {
      // Auto-submit empty response when time is up
      setError('Time is up! Moving to next question.');
      setTimeout(() => {
        setError(null);
        nextQuestion();
      }, 2000);
    } else {
      // If already answered, just move to next question
      nextQuestion();
    }
  };

  const skipQuestion = () => {
    if (config.allowSkip && !hasAnswered) {
      setError('Question skipped.');
      setTimeout(() => {
        setError(null);
        nextQuestion();
      }, 1000);
    }
  };

  const loadQuestions = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const sectionParam = searchParams.get('section');
      
      if (sectionParam) {
        // Load questions for specific section
        await loadSectionQuestions(sectionParam);
      } else {
        // Load complete test
        await loadCompleteTest();
      }
    } catch (error) {
      console.error('Error loading questions:', error);
      setError('Failed to load questions. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadSectionQuestions = async (section) => {
    const response = await api.get(`/v1/questions/section/${section}`);
    const data = response.data;
    
    setTestSection(section);
    setTestType(data.type);
    
    if (data.type === 'cue_card') {
      // Part 2 - Cue card format (limit to 1)
      setTopic(data.topic);
      setBulletPoints(data.bulletPoints || []);
      setCurrentQuestion(data.topic);
      setQuestions([{
        question: data.topic,
        section: section,
        bulletPoints: data.bulletPoints || [],
        type: 'cue_card'
      }]);
    } else {
      // Part 1 or Part 3 - Questions array with limits
      const questionList = data.questions || [];
      
      // Apply limits based on section
      let limitedQuestions = questionList;
      if (section === 'part1') {
        // Limit Part 1 to 4-6 questions
        const maxQuestions = Math.min(questionList.length, Math.floor(Math.random() * 3) + 4); // 4-6 questions
        limitedQuestions = questionList.slice(0, maxQuestions);
      } else if (section === 'part3') {
        // Limit Part 3 to 5-7 questions
        const maxQuestions = Math.min(questionList.length, Math.floor(Math.random() * 3) + 5); // 5-7 questions
        limitedQuestions = questionList.slice(0, maxQuestions);
      }
      
      const formattedQuestions = limitedQuestions.map((q, index) => ({
        question: q,
        section: section,
        order: index + 1
      }));
      setQuestions(formattedQuestions);
      setCurrentQuestion(formattedQuestions[0]?.question || '');
    }
    
    setCurrentQuestionIndex(0);
  };

  const loadCompleteTest = async () => {
    const response = await api.get('/v1/questions/complete-test');
    const data = response.data;
    
    console.log('Complete test data:', data);
    
    // Combine all questions from all sections with limits
    const allQuestions = [];
    
    // Add Part 1 questions (limit to 4-6 questions)
    if (data.part1 && Array.isArray(data.part1)) {
      const part1Questions = [];
      data.part1.forEach((questionObj) => {
        if (questionObj.questions && Array.isArray(questionObj.questions)) {
          questionObj.questions.forEach((q) => {
            part1Questions.push(q);
          });
        }
      });
      
      // Limit Part 1 to 4-6 questions
      const maxPart1Questions = Math.min(part1Questions.length, Math.floor(Math.random() * 3) + 4); // 4-6 questions
      const selectedPart1Questions = part1Questions.slice(0, maxPart1Questions);
      
      selectedPart1Questions.forEach((q, index) => {
        allQuestions.push({
          question: q,
          section: 'part1',
          order: allQuestions.length + 1
        });
      });
    }
    
    // Add Part 2 questions (limit to 1 cue card)
    if (data.part2 && Array.isArray(data.part2)) {
      // Only take the first cue card
      const firstCueCard = data.part2.find(questionObj => questionObj.topic);
      if (firstCueCard) {
        allQuestions.push({
          question: firstCueCard.topic,
          section: 'part2',
          order: allQuestions.length + 1,
          bulletPoints: firstCueCard.bulletPoints || [],
          type: 'cue_card'
        });
      }
    }
    
    // Add Part 3 questions (limit to 5-7 questions)
    if (data.part3 && Array.isArray(data.part3)) {
      const part3Questions = [];
      data.part3.forEach((questionObj) => {
        if (questionObj.questions && Array.isArray(questionObj.questions)) {
          questionObj.questions.forEach((q) => {
            part3Questions.push(q);
          });
        }
      });
      
      // Limit Part 3 to 5-7 questions
      const maxPart3Questions = Math.min(part3Questions.length, Math.floor(Math.random() * 3) + 5); // 5-7 questions
      const selectedPart3Questions = part3Questions.slice(0, maxPart3Questions);
      
      selectedPart3Questions.forEach((q, index) => {
        allQuestions.push({
          question: q,
          section: 'part3',
          order: allQuestions.length + 1
        });
      });
    }
    
    console.log('Processed questions:', allQuestions);
    
    if (allQuestions.length === 0) {
      setError('No questions found. Please try again.');
      return;
    }
    
    setQuestions(allQuestions);
    setCurrentQuestion(allQuestions[0]?.question || '');
    setTestSection(allQuestions[0]?.section || '');
    setTestType('complete');
    setCurrentQuestionIndex(0);
  };

  const nextQuestion = () => {
    if (currentQuestionIndex < questions.length - 1) {
      const nextIndex = currentQuestionIndex + 1;
      setCurrentQuestionIndex(nextIndex);
      
      const nextQuestionData = questions[nextIndex];
      setCurrentQuestion(nextQuestionData.question || nextQuestionData);
      setTestSection(nextQuestionData.section || testSection);
      
      // Set bullet points for Part 2
      if (nextQuestionData.type === 'cue_card' || nextQuestionData.section === 'part2') {
        setBulletPoints(nextQuestionData.bulletPoints || []);
        setTopic(nextQuestionData.question || nextQuestionData.topic || '');
      } else {
        setBulletPoints([]);
        setTopic('');
      }
      
      // Reset recording state and ensure timer is cleared
      resetRecording();
      
      // Reset recording timer to correct value for new question
      const nextSection = nextQuestionData.section || testSection;
      let maxRecordingTime;
      if (nextSection === 'part2') {
        maxRecordingTime = config.recordingTimeLimits.part2;
      } else if (nextSection === 'part1') {
        maxRecordingTime = config.questionTimers.part1;
      } else if (nextSection === 'part3') {
        maxRecordingTime = config.questionTimers.part3;
      } else {
        maxRecordingTime = config.recordingTimeLimits.complete;
      }
      setRecordingTimeRemaining(maxRecordingTime);
      
      // Reset question state for new question
      setHasAnswered(false);
      setIsTimerRunning(false);
      setQuestionTimer(0); // Will be set by useEffect when question changes
    } else {
      // Test complete
      setShowSubmissionConfirm(true);
    }
  };

  const startRecording = async () => {
    try {
      // If we're in preparation mode for Part 2, stop the preparation timer
      if (isInPreparation && testSection === 'part2') {
        console.log('Stopping preparation timer due to manual recording start');
        stopPreparationTimer();
      }
      
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorderRef.current = new MediaRecorder(stream);
      
      const chunks = [];
      mediaRecorderRef.current.ondataavailable = (event) => {
        chunks.push(event.data);
      };

             mediaRecorderRef.current.onstop = () => {
         const blob = new Blob(chunks, { type: 'audio/wav' });
         setAudioBlob(blob);
         setAudioUrl(URL.createObjectURL(blob));
         stream.getTracks().forEach(track => track.stop());
         
         // Store the recording time for analysis
         const finalRecordingTime = recordingTime;
         console.log('Recording stopped, duration:', finalRecordingTime);
         
         // Analyze the recording instead of just moving to next question
         setTimeout(() => {
           analyzeSpeech();
         }, 1000); // Short delay to show recording completed
       };

      mediaRecorderRef.current.start();
      setIsRecording(true);
      
      // Reset recording time only (recordingTimeRemaining will be set below)
      setRecordingTime(0);
      
             // Set countdown timer based on section and test type from config
       let maxRecordingTime;
       if (testSection === 'part2') {
         maxRecordingTime = config.recordingTimeLimits.part2;
       } else if (testSection === 'part1') {
         maxRecordingTime = config.questionTimers.part1;
       } else if (testSection === 'part3') {
         maxRecordingTime = config.questionTimers.part3;
       } else {
         // For complete test, use longer time since we have 5 min total requirement
         maxRecordingTime = config.recordingTimeLimits.complete;
       }
       setRecordingTimeRemaining(maxRecordingTime);
      
      // Clear any existing timer first
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      
             // Start recording timer with proper cleanup
       const startTime = Date.now();
       console.log('Starting recording timer for section:', testSection, 'max time:', maxRecordingTime);
       
       timerRef.current = setInterval(() => {
         const elapsedSeconds = Math.floor((Date.now() - startTime) / 1000);
         const remainingSeconds = Math.max(0, maxRecordingTime - elapsedSeconds);
         
         setRecordingTime(elapsedSeconds);
         setRecordingTimeRemaining(remainingSeconds);
         
         console.log('Recording timer update:', { elapsedSeconds, remainingSeconds, testSection });
         
         if (remainingSeconds <= 0) {
           // Auto-stop recording when time runs out
           if (timerRef.current) {
             clearInterval(timerRef.current);
             timerRef.current = null;
           }
           stopRecording();
         }
       }, 1000);
      
    } catch (error) {
      console.error('Error starting recording:', error);
      setError('Failed to start recording. Please check microphone permissions.');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      
      // Clear the recording timer immediately
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      
      // Stop any ongoing speech synthesis
      if (isSpeaking) {
        stopSpeaking();
      }
      
      // Mark as answered to prevent further interactions
      setHasAnswered(true);
    }
  };

  const playRecording = () => {
    if (audioRef.current) {
      audioRef.current.play();
      setIsPlaying(true);
    }
  };

  const pauseRecording = () => {
    if (audioRef.current) {
      audioRef.current.pause();
      setIsPlaying(false);
    }
  };

  const resetRecording = () => {
    setAudioBlob(null);
    setAudioUrl(null);
    setRecordingTime(0);
    setRecordingTimeRemaining(0);
    setIsPlaying(false);
    
    // Clear recording timer
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
    
    // Don't clear question timer here - it should continue running
    // Only clear it when moving to next question
  };

  // Separate function to reset recording time only
  const resetRecordingTime = () => {
    setRecordingTime(0);
    setRecordingTimeRemaining(0);
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  };

     const analyzeSpeech = async () => {
     console.log('analyzeSpeech called, audioBlob:', !!audioBlob, 'recordingTime:', recordingTime);
     
     if (!audioBlob) {
       console.log('No audio blob available, creating empty analysis');
       // Create an empty analysis entry to track the attempt
       const emptyAnalysis = {
         question: currentQuestion,
         section: testSection,
         order: currentQuestionIndex + 1,
         durationSeconds: recordingTime || 0,
         error: 'No recording available'
       };
       
       setAnalyses(prev => [...prev, emptyAnalysis]);
       
       // Move to next question even if no recording
       if (currentQuestionIndex < questions.length - 1) {
         nextQuestion();
       } else {
         setShowSubmissionConfirm(true);
       }
       return;
     }

     setIsAnalyzing(true);
     setError(null);

     try {
       const reader = new FileReader();
       reader.onload = async () => {
         const base64Audio = reader.result.split(',')[1];
         
         const requestData = {
           question: currentQuestion,
           audioData: base64Audio,
           audioFormat: 'audio/wav',
           testSection: testSection
         };

         console.log('Sending analysis request with duration:', recordingTime);
         const response = await api.post('/v1/speech/analyze-base64', requestData);
         
         const newAnalysis = {
           ...response.data,
           question: currentQuestion,
           section: testSection,
           order: currentQuestionIndex + 1,
           durationSeconds: recordingTime || 0
         };
         
         console.log('Analysis created with duration:', recordingTime);

         setAnalyses(prev => [...prev, newAnalysis]);
         
         // Mark question as answered
         setHasAnswered(true);
         setIsTimerRunning(false);
         
         // Record question usage
         try {
           await api.post('/v1/questions/record-usage', {
             questionText: currentQuestion,
             section: testSection,
             practiceSessionId: `session_${Date.now()}`,
             testSection: testSection
           });
         } catch (error) {
           console.error('Error recording question usage:', error);
           // Don't fail the analysis if recording usage fails
         }
         
         // Move to next question or show submission confirm
         if (currentQuestionIndex < questions.length - 1) {
           nextQuestion();
         } else {
           setShowSubmissionConfirm(true);
         }
       };
       reader.readAsDataURL(audioBlob);
     } catch (error) {
       console.error('Error analyzing speech:', error);
       setError('Failed to analyze speech. Please try again.');
       
       // Even if analysis fails, create an entry with the recording time
       const failedAnalysis = {
         question: currentQuestion,
         section: testSection,
         order: currentQuestionIndex + 1,
         durationSeconds: recordingTime || 0,
         error: 'Analysis failed'
       };
       
       setAnalyses(prev => [...prev, failedAnalysis]);
       
       // Move to next question
       if (currentQuestionIndex < questions.length - 1) {
         nextQuestion();
       } else {
         setShowSubmissionConfirm(true);
       }
     } finally {
       setIsAnalyzing(false);
     }
   };

  const submitTest = () => {
    // Navigate to feedback page with analyses data
    navigate('/feedback', { state: { analyses } });
  };

  const exitToDashboard = () => {
    navigate('/dashboard');
  };

  const retakeTest = async () => {
    // Reset all state and load new questions
    setQuestions([]);
    setCurrentQuestionIndex(0);
    setCurrentQuestion('');
    setTestSection('');
    setIsRecording(false);
    setIsPlaying(false);
    setRecordingTime(0);
    setRecordingTimeRemaining(0);
    setAudioBlob(null);
    setAudioUrl(null);
    setIsAnalyzing(false);
    setError(null);
    setAnalyses([]);
    setShowSubmissionConfirm(false);
    setBulletPoints([]);
    setTopic('');
    setQuestionTimer(0);
    setIsTimerRunning(false);
    setHasAnswered(false);
    
    // Load new questions
    await loadQuestions();
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const getSectionIcon = (section) => {
    switch (section?.toLowerCase()) {
      case 'part1':
        return <BookOpen className="h-5 w-5" />;
      case 'part2':
        return <Target className="h-5 w-5" />;
      case 'part3':
        return <AlertTriangle className="h-5 w-5" />;
      default:
        return <BookOpen className="h-5 w-5" />;
    }
  };

  const getSectionColor = (section) => {
    switch (section?.toLowerCase()) {
      case 'part1':
        return 'bg-gradient-to-r from-blue-500 to-blue-600';
      case 'part2':
        return 'bg-gradient-to-r from-green-500 to-green-600';
      case 'part3':
        return 'bg-gradient-to-r from-purple-500 to-purple-600';
      default:
        return 'bg-gradient-to-r from-gray-500 to-gray-600';
    }
  };



  // Simple and reliable text-to-speech function
  const speakQuestion = (text) => {
    console.log('speakQuestion called with:', text);
    
    if (!text || !window.speechSynthesis) {
      console.log('Speech synthesis not available or no text');
      return;
    }
    
    // Stop any existing speech
    window.speechSynthesis.cancel();
    setIsSpeaking(false);
    
    // Create new utterance
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 0.8;
    utterance.volume = 1.0;
    
    // Get selected voice based on accent
    const selectedVoiceObj = getVoiceByAccent(selectedVoice);
    console.log('Selected voice for accent:', selectedVoice, selectedVoiceObj?.name);
    
    if (selectedVoiceObj) {
      utterance.voice = selectedVoiceObj;
      console.log('Using voice:', selectedVoiceObj.name);
    } else {
      // Fallback to any English voice
      const voices = window.speechSynthesis.getVoices();
      const englishVoice = voices.find(voice => 
        voice.lang.startsWith('en') && voice.name.includes('Google')
      ) || voices.find(voice => 
        voice.lang.startsWith('en')
      ) || voices[0];
      
      if (englishVoice) {
        utterance.voice = englishVoice;
        console.log('Fallback voice:', englishVoice.name);
      }
    }
    
    // Set up event handlers
    utterance.onstart = () => {
      setIsSpeaking(true);
      console.log('Speech started:', text);
    };
    
    utterance.onend = () => {
      setIsSpeaking(false);
      console.log('Speech ended');
    };
    
    utterance.onerror = (event) => {
      console.error('Speech error:', event);
      setIsSpeaking(false);
    };
    
    // Speak the text
    window.speechSynthesis.speak(utterance);
    console.log('Speech synthesis speak() called');
  };

  const stopSpeaking = () => {
    window.speechSynthesis.cancel();
    setIsSpeaking(false);
  };

  // Handle user interaction for speech synthesis
  const handleUserInteraction = () => {
    setUserHasInteracted(true);
    console.log('User interaction detected');
  };

  // Auto-speak function - simple and reliable
  const autoSpeakQuestion = (text) => {
    console.log('Auto-speak called:', { text, autoSpeakEnabled, userHasInteracted });
    
    if (!autoSpeakEnabled || !text) {
      console.log('Auto-speak skipped:', { autoSpeakEnabled, hasText: !!text });
      return;
    }
    
    // Clear any existing auto-speak timer
    if (autoSpeakTimerRef.current) {
      clearTimeout(autoSpeakTimerRef.current);
    }
    
    // Set new auto-speak timer
    autoSpeakTimerRef.current = setTimeout(() => {
      console.log('Auto-speak executing after delay');
      
      // Only auto-speak if user has interacted
      if (userHasInteracted) {
        speakQuestion(text);
      } else {
        console.log('Auto-speak skipped - no user interaction yet');
      }
    }, 1000); // 1 second delay
  };

  // Preparation timer functions for Part 2
  const startPreparationTimer = () => {
    setIsInPreparation(true);
    setPreparationTimeRemaining(config.preparationTimer.part2);
    
    const timer = setInterval(() => {
      setPreparationTimeRemaining(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          setIsInPreparation(false);
          // Only start recording automatically if not already recording
          if (!isRecording) {
            startRecording();
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    
    setPreparationTimer(timer);
  };

  const stopPreparationTimer = () => {
    if (preparationTimer) {
      clearInterval(preparationTimer);
      setPreparationTimer(null);
    }
    setIsInPreparation(false);
    setPreparationTimeRemaining(0);
  };

  // Automatic recording functions
  const startAutoRecordingTimer = (section) => {
    const delay = config.autoRecording[section];
    
    if (delay > 0) {
      const timer = setTimeout(() => {
        // Only start recording if not already recording
        if (!isRecording) {
          startRecording();
        }
      }, delay);
      
      setAutoRecordingTimer(timer);
    } else if (section === 'part2') {
      // For Part 2, start preparation timer instead
      startPreparationTimer();
    }
  };

  const stopAutoRecordingTimer = () => {
    if (autoRecordingTimer) {
      clearTimeout(autoRecordingTimer);
      setAutoRecordingTimer(null);
    }
  };

  // Question timer functions
  const startQuestionTimer = () => {
    if (questions.length > 0 && currentQuestionIndex < questions.length) {
      const currentQuestionData = questions[currentQuestionIndex];
      const section = currentQuestionData.section || testSection;
      const questionTimerDuration = config.questionTimers[section] || 30;
      const autoRecordingDelay = config.autoRecording[section] || 0;
      
      // Total timer duration = question timer + auto-recording delay (in seconds)
      const totalTimerDuration = questionTimerDuration + (autoRecordingDelay / 1000);
      
      setQuestionTimer(totalTimerDuration);
      setIsTimerRunning(true);
      setHasAnswered(false);
    }
  };

  const stopQuestionTimer = () => {
    if (questionTimerRef.current) {
      clearInterval(questionTimerRef.current);
      questionTimerRef.current = null;
    }
    setIsTimerRunning(false);
    setQuestionTimer(0);
  };

  // Effect to handle question changes and automatic features
  useEffect(() => {
    console.log('Question/TestSection changed:', { currentQuestion, testSection, autoSpeakEnabled });
    
    if (currentQuestion && testSection) {
      // Start question timer
      startQuestionTimer();
      
      // Automatic question speaking - different logic for Part 2
      if (autoSpeakEnabled) {
        if (testSection === 'part2') {
          // For Part 2, only speak the instruction, not the question
          console.log('Part 2 - speaking instruction only');
          setTimeout(() => {
            speakQuestion("Here is your q card, and you have 1 min to prepare");
          }, 1000); // 1 second delay
        } else {
          // For other parts, speak the question
          console.log('Auto-speak enabled, calling autoSpeakQuestion');
          autoSpeakQuestion(currentQuestion);
          
          // Also try immediate speak for testing
          setTimeout(() => {
            console.log('Trying immediate speak for testing');
            speakQuestion(currentQuestion);
          }, 500);
        }
      } else {
        console.log('Auto-speak disabled');
      }
      
      // Start automatic recording timer
      startAutoRecordingTimer(testSection);
    }
    
    return () => {
      // Clean up timers when question changes
      stopQuestionTimer();
      stopAutoRecordingTimer();
      stopPreparationTimer();
    };
  }, [currentQuestion, testSection, autoSpeakEnabled]);

  // Loading state
  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="bg-gradient-to-br from-white to-blue-50 rounded-2xl shadow-xl p-8 border border-blue-100">
          <div className="flex flex-col items-center justify-center h-64">
            <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mb-6"></div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Loading Questions</h2>
            <p className="text-gray-600 text-center max-w-md">
              We're preparing your practice session. This may take a few moments...
            </p>
            <div className="mt-4 flex space-x-2">
              <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce"></div>
              <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
              <div className="w-2 h-2 bg-blue-600 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

     // Submission confirmation
   if (showSubmissionConfirm) {
     console.log('Analyses for submission:', analyses);
     const totalSpeakingTime = analyses.reduce((total, analysis) => {
       const duration = analysis.durationSeconds || 0;
       console.log('Analysis duration:', duration, 'for question:', analysis.question);
       return total + duration;
     }, 0);

     // Determine minimum speaking time based on test type
     let minimumSpeakingTime;
     let minimumTimeMessage;

     if (testType === 'complete') {
       minimumSpeakingTime = 300; // 5 minutes for complete test
       minimumTimeMessage = 'Minimum 5 minutes required';
     } else if (testSection === 'part1') {
       minimumSpeakingTime = 60; // 1 minute for Part 1
       minimumTimeMessage = 'Minimum 1 minute required';
     } else if (testSection === 'part2') {
       minimumSpeakingTime = 120; // 2 minutes for Part 2
       minimumTimeMessage = 'Minimum 2 minutes required';
     } else if (testSection === 'part3') {
       minimumSpeakingTime = 120; // 2 minutes for Part 3
       minimumTimeMessage = 'Minimum 2 minutes required';
     } else {
       minimumSpeakingTime = 300; // Default to 5 minutes
       minimumTimeMessage = 'Minimum 5 minutes required';
     }

     const isSpeakingTimeSufficient = totalSpeakingTime >= minimumSpeakingTime;
     const totalSpeakingTimeFormatted = formatTime(totalSpeakingTime);
     
     console.log('Total speaking time calculated:', totalSpeakingTime, 'formatted:', totalSpeakingTimeFormatted, 'minimum required:', minimumSpeakingTime);

         return (
       <div className="max-w-4xl mx-auto px-2 sm:px-4 py-4 sm:py-8">
         <div className="bg-gradient-to-br from-white to-blue-50 rounded-2xl shadow-2xl p-4 sm:p-8 border border-blue-100">
           {/* Submission Confirmation */}
           <div className="text-center space-y-4 sm:space-y-6">
             <div className="flex items-center justify-center space-x-2 sm:space-x-3">
               <h2 className="text-2xl sm:text-3xl font-bold text-white">Practice Complete!</h2>
               <button
                 onClick={() => {
                   const titleText = "Practice Complete!";
                   isSpeaking ? stopSpeaking() : speakQuestion(titleText);
                 }}
                 className={`flex-shrink-0 p-2 rounded-full transition-all duration-300 ${
                   isSpeaking 
                     ? 'bg-red-500 hover:bg-red-600 animate-pulse' 
                     : 'bg-white bg-opacity-20 hover:bg-opacity-30'
                 }`}
                 title={isSpeaking ? "Stop speaking" : "Hear title aloud"}
               >
                 <Volume2 className={`h-4 w-4 text-white ${isSpeaking ? 'animate-bounce' : ''}`} />
               </button>
             </div>
             <p className="text-gray-600 text-sm sm:text-lg">You have completed all questions. Would you like to submit for analysis?</p>
           </div>

           <div className="bg-gradient-to-r from-blue-500 to-purple-500 rounded-2xl p-4 sm:p-8 mb-6 sm:mb-8 text-white shadow-xl">
             <h3 className="text-xl sm:text-2xl font-bold mb-4 sm:mb-6 text-center">Practice Summary</h3>
             <div className="grid grid-cols-1 md:grid-cols-3 gap-4 sm:gap-6">
               <div className="text-center bg-white bg-opacity-20 rounded-xl p-3 sm:p-4 backdrop-blur-sm">
                 <p className="text-2xl sm:text-3xl font-bold">{questions.length}</p>
                 <p className="text-xs sm:text-sm opacity-90">Questions Completed</p>
               </div>
               <div className="text-center bg-white bg-opacity-20 rounded-xl p-3 sm:p-4 backdrop-blur-sm">
                 <p className="text-2xl sm:text-3xl font-bold">{analyses.length}</p>
                 <p className="text-xs sm:text-sm opacity-90">Responses Recorded</p>
               </div>
               <div className="text-center bg-white bg-opacity-20 rounded-xl p-3 sm:p-4 backdrop-blur-sm">
                 <p className={`text-2xl sm:text-3xl font-bold ${isSpeakingTimeSufficient ? 'text-green-200' : 'text-red-200'}`}>
                   {totalSpeakingTimeFormatted}
                 </p>
                 <p className="text-xs sm:text-sm opacity-90">Total Speaking Time</p>
                 {!isSpeakingTimeSufficient && (
                   <p className="text-xs text-red-200 mt-1">{minimumTimeMessage}</p>
                 )}
               </div>
             </div>
           </div>

           <div className="flex flex-col sm:flex-row justify-center space-y-3 sm:space-y-0 sm:space-x-4 lg:space-x-6">
             <button
               onClick={exitToDashboard}
               className="px-6 sm:px-8 py-3 sm:py-4 bg-gradient-to-r from-gray-500 to-gray-600 text-white rounded-2xl hover:from-gray-600 hover:to-gray-700 transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105 font-semibold text-sm sm:text-base"
             >
               Exit
             </button>
             <button
               onClick={retakeTest}
               className="px-6 sm:px-8 py-3 sm:py-4 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-2xl hover:from-blue-600 hover:to-blue-700 transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105 font-semibold text-sm sm:text-base"
             >
               Retake Test
             </button>
             <div className="relative group">
               <button
                 onClick={submitTest}
                 disabled={!isSpeakingTimeSufficient}
                 className={`px-6 sm:px-8 py-3 sm:py-4 rounded-2xl text-white transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105 font-semibold text-sm sm:text-base ${
                   isSpeakingTimeSufficient
                     ? 'bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700'
                     : 'bg-gradient-to-r from-gray-400 to-gray-500 cursor-not-allowed opacity-50'
                 }`}
               >
                 Submit for Analysis
               </button>
               
               {/* Hover warning for insufficient speaking time */}
               {!isSpeakingTimeSufficient && (
                 <div className="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 sm:px-4 py-2 bg-red-600 text-white text-xs sm:text-sm rounded-lg shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none whitespace-nowrap z-10">
                   <div className="relative">
                     <div className="absolute top-full left-1/2 transform -translate-x-1/2 w-0 h-0 border-l-4 border-r-4 border-t-4 border-transparent border-t-red-600"></div>
                     Insufficient speaking time. Please retake the test to get at least {minimumTimeMessage.toLowerCase()}.
                   </div>
                 </div>
               )}
             </div>
           </div>
         </div>
       </div>
     );
  }

  return (
    <div className="max-w-4xl mx-auto px-2 sm:px-4 py-4 sm:py-8">
      {/* Header */}
      <div className="bg-gradient-to-br from-white to-gray-50 rounded-2xl shadow-xl p-4 sm:p-8 mb-4 sm:mb-8 border border-gray-200">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-4 sm:mb-6 space-y-4 sm:space-y-0">
          <div className="flex flex-col sm:flex-row sm:items-center space-y-2 sm:space-y-0 sm:space-x-4">
            <div className="flex items-center space-x-2">
              <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                Practice Session
              </h1>
            </div>
            {/* Environment Badge */}
            <div className={`px-3 py-1 rounded-full text-xs font-semibold ${
              environment === 'dev' 
                ? 'bg-yellow-100 text-yellow-800 border border-yellow-300' 
                : 'bg-green-100 text-green-800 border border-green-300'
            }`}>
              <span>{environment.toUpperCase()}</span>
            </div>
          </div>
          <div className="flex flex-col sm:flex-row items-center space-y-2 sm:space-y-0 sm:space-x-3">
            {/* Question Timer */}
            <div className="bg-white px-3 sm:px-4 py-2 rounded-xl shadow-md">
              <div className="flex items-center space-x-2">
                <Clock className="h-4 w-4 sm:h-5 sm:w-5 text-blue-500" />
                <span className={`text-sm sm:text-base font-semibold ${
                  questionTimer <= 10 ? 'text-red-600' : 'text-gray-700'
                }`}>
                  {formatTime(questionTimer)}
                </span>
              </div>
            </div>
            <div className="bg-white px-3 sm:px-4 py-2 rounded-xl shadow-md">
              <div className="flex items-center space-x-2">
                <span className="text-sm sm:text-base text-gray-700 font-semibold">
                  Q{currentQuestionIndex + 1}/{questions.length}
                </span>
              </div>
            </div>
            {/* Auto-Speak Toggle */}
            <div className="bg-white px-3 sm:px-4 py-2 rounded-xl shadow-md">
              <div className="flex items-center space-x-2">
                <Volume2 className={`h-4 w-4 ${autoSpeakEnabled ? 'text-blue-600' : 'text-gray-400'}`} />
                <button
                  onClick={() => {
                    handleUserInteraction();
                    setAutoSpeakEnabled(!autoSpeakEnabled);
                  }}
                  className={`px-2 sm:px-3 py-1 rounded-full text-xs sm:text-sm font-medium transition-all duration-200 ${
                    autoSpeakEnabled 
                      ? 'bg-blue-100 text-blue-700 hover:bg-blue-200' 
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                  title={autoSpeakEnabled ? "Disable auto-speak" : "Enable auto-speak"}
                >
                  {autoSpeakEnabled ? 'ON' : 'OFF'}
                </button>
              </div>
            </div>
          </div>
        </div>

                 {/* Progress Bar */}
         <div className="w-full bg-gray-200 rounded-full h-3 mb-6 overflow-hidden">
           <div 
             className="bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 h-3 rounded-full transition-all duration-500 ease-out shadow-lg"
             style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
           ></div>
         </div>

         {/* Navigation Warning */}
         <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-3 sm:p-4 mb-4">
           <div className="flex items-start sm:items-center space-x-2">
             <AlertTriangle className="h-4 w-4 sm:h-5 sm:w-5 text-yellow-600 flex-shrink-0 mt-0.5 sm:mt-0" />
             <span className="text-yellow-800 text-xs sm:text-sm font-medium">
               Please complete the test. Navigation is disabled to prevent loss of progress.
             </span>
           </div>
         </div>

        {/* Section Badge */}
        {testSection && (
          <div className="flex flex-col sm:flex-row sm:items-center mb-4 space-y-2 sm:space-y-0">
            <div className={`p-2 sm:p-3 rounded-xl ${getSectionColor(testSection)} text-white sm:mr-4 shadow-lg flex items-center justify-center sm:justify-start`}>
              {getSectionIcon(testSection)}
            </div>
            <div className="flex-1 text-center sm:text-left">
              <h3 className="font-bold text-base sm:text-lg text-gray-900">{testSection.toUpperCase()}</h3>
              <p className="text-xs sm:text-sm text-gray-500">
                {testSection === 'part1' ? 'Introduction & Interview' : 
                 testSection === 'part2' ? 'Individual Long Turn' : 
                 'Two-Way Discussion'}
              </p>
            </div>
          </div>
        )}

        {/* Preparation Timer for Part 2 */}
        {isInPreparation && testSection === 'part2' && (
          <div className="mb-4 sm:mb-6 p-4 sm:p-6 bg-gradient-to-r from-yellow-400 to-orange-500 rounded-2xl text-white shadow-xl">
            <div className="text-center">
              <div className="flex items-center justify-center space-x-2 sm:space-x-3 mb-3 sm:mb-4">
                <Clock className="h-6 w-6 sm:h-8 sm:w-8" />
                <h3 className="text-lg sm:text-2xl font-bold">Preparation Time</h3>
              </div>
              <div className="text-3xl sm:text-4xl font-bold mb-2">
                {Math.floor(preparationTimeRemaining / 60)}:{(preparationTimeRemaining % 60).toString().padStart(2, '0')}
              </div>
              <p className="text-sm sm:text-lg opacity-90">Recording will start automatically when time is up</p>
            </div>
          </div>
        )}
      </div>

             {/* Question Card */}
       <div className="bg-gradient-to-br from-white to-blue-50 rounded-2xl shadow-xl p-4 sm:p-8 border border-blue-100">
         {/* Question Content */}
         <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-2xl p-4 sm:p-8 text-white shadow-2xl mb-4 sm:mb-8 relative overflow-hidden">
           <div className="absolute inset-0 opacity-10">
             <div className="absolute top-0 right-0 w-16 h-16 sm:w-32 sm:h-32 bg-white rounded-full -mr-8 -mt-8 sm:-mr-16 sm:-mt-16"></div>
             <div className="absolute bottom-0 left-0 w-12 h-12 sm:w-24 sm:h-24 bg-white rounded-full -ml-6 -mb-6 sm:-ml-12 sm:-mb-12"></div>
           </div>
           
           <div className="relative z-10">
             <div className="flex items-center mb-3 sm:mb-4">
               <div className="bg-white bg-opacity-20 rounded-full p-2 sm:p-3 mr-3 sm:mr-4">
                 <BookOpen className="h-5 w-5 sm:h-6 sm:w-6 text-white" />
               </div>
               <div className="flex items-center justify-between flex-1">
                 <h3 className="text-lg sm:text-xl font-bold">Question {currentQuestionIndex + 1}</h3>
               </div>
             </div>
             
             <div className="bg-white bg-opacity-10 rounded-xl p-4 sm:p-6 backdrop-blur-sm">
               <div className="flex items-start justify-between mb-3 sm:mb-4">
                 <p className="text-sm sm:text-lg leading-relaxed font-medium flex-1 mr-3 sm:mr-4">{currentQuestion}</p>
                 <button
                   onClick={() => {
                     handleUserInteraction();
                     isSpeaking ? stopSpeaking() : speakQuestion(currentQuestion);
                   }}
                   className={`flex-shrink-0 p-2 sm:p-3 rounded-full transition-all duration-300 ${
                     isSpeaking 
                       ? 'bg-red-500 hover:bg-red-600 animate-pulse' 
                       : 'bg-white bg-opacity-20 hover:bg-opacity-30'
                   }`}
                   title={isSpeaking ? "Stop speaking" : "Hear question aloud"}
                 >
                   <Volume2 className={`h-4 w-4 sm:h-5 sm:w-5 text-white ${isSpeaking ? 'animate-bounce' : ''}`} />
                 </button>
               </div>
               
               {/* Bullet points for Part 2 */}
               {testSection === 'part2' && bulletPoints && bulletPoints.length > 0 && (
                 <div className="mt-3 sm:mt-4">
                   <div className="mb-2 sm:mb-3">
                     <h4 className="text-white font-semibold text-sm sm:text-base">You should say:</h4>
                   </div>
                   <ul className="space-y-1 sm:space-y-2">
                     {bulletPoints.map((point, index) => (
                       <li key={index} className="flex items-start">
                         <span className="text-white font-bold mr-2 text-sm sm:text-base">•</span>
                         <span className="text-white opacity-90 text-sm sm:text-base">{point}</span>
                       </li>
                     ))}
                   </ul>
                 </div>
               )}
             </div>
           </div>
         </div>

        {/* Recording Controls */}
        <div className="space-y-4 sm:space-y-6">
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-3 sm:p-4">
              <p className="text-red-800 text-sm sm:text-base">{error}</p>
            </div>
          )}

          <div className="flex flex-col sm:flex-row items-center justify-center space-y-3 sm:space-y-0 sm:space-x-4">
            {!isRecording && !audioBlob && (
              <button
                onClick={() => {
                  handleUserInteraction();
                  startRecording();
                }}
                className="flex items-center px-6 sm:px-8 py-3 sm:py-4 bg-gradient-to-r from-red-500 to-red-600 text-white rounded-2xl hover:from-red-600 hover:to-red-700 transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105 w-full sm:w-auto"
              >
                <Mic className="h-5 w-5 sm:h-6 sm:w-6 mr-2 sm:mr-3" />
                <span className="font-semibold text-sm sm:text-base">Start Recording</span>
              </button>
            )}

            {isRecording && (
              <button
                onClick={stopRecording}
                className="flex items-center px-6 sm:px-8 py-3 sm:py-4 bg-gradient-to-r from-gray-500 to-gray-600 text-white rounded-2xl hover:from-gray-600 hover:to-gray-700 transition-all duration-300 shadow-lg hover:shadow-xl transform hover:scale-105 w-full sm:w-auto"
              >
                <Square className="h-5 w-5 sm:h-6 sm:w-6 mr-2 sm:mr-3" />
                <span className="font-semibold text-sm sm:text-base">Stop Recording ({formatTime(recordingTimeRemaining)})</span>
              </button>
            )}

            {/* Show processing message when recording stops and before moving to next question */}
            {audioBlob && !isRecording && (
              <div className="flex items-center space-x-3 px-4 sm:px-6 py-3 bg-blue-100 text-blue-700 rounded-xl w-full sm:w-auto">
                <div className="animate-spin rounded-full h-4 w-4 sm:h-5 sm:w-5 border-b-2 border-blue-600"></div>
                <span className="font-medium text-sm sm:text-base">Recording completed ({formatTime(recordingTime)}) - Analyzing...</span>
              </div>
            )}
            
            {/* Manual analyze button if needed */}
            {audioBlob && !isRecording && !isAnalyzing && (
              <button
                onClick={analyzeSpeech}
                className="flex items-center px-4 sm:px-6 py-3 bg-gradient-to-r from-green-500 to-green-600 text-white rounded-xl hover:from-green-600 hover:to-green-700 transition-all duration-300 shadow-lg hover:shadow-xl w-full sm:w-auto"
              >
                <CheckCircle className="h-5 w-5 sm:h-6 sm:w-6 mr-2 sm:mr-3" />
                <span className="font-medium text-sm sm:text-base">Analyze Recording</span>
              </button>
            )}

            {/* Skip button - only show in dev environment and when question hasn't been answered */}
            {config.allowSkip && !hasAnswered && !isRecording && (
              <button
                onClick={skipQuestion}
                className="flex items-center px-4 sm:px-6 py-3 bg-gradient-to-r from-yellow-500 to-yellow-600 text-white rounded-xl hover:from-yellow-600 hover:to-yellow-700 transition-all duration-300 shadow-lg hover:shadow-xl w-full sm:w-auto"
              >
                <SkipForward className="h-5 w-5 sm:h-6 sm:w-6 mr-2 sm:mr-3" />
                <span className="font-medium text-sm sm:text-base">Skip Question</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Practice;
