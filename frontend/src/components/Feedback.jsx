import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Play, Pause, RotateCcw, ArrowLeft } from 'lucide-react';

const Feedback = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isPlayingFeedback, setIsPlayingFeedback] = useState(false);
  const [isPlayingRecording, setIsPlayingRecording] = useState(false);
  const [activeTab, setActiveTab] = useState('corrections');

  const { analysisData, question, audioUrl, testSection } = location.state || {};

  useEffect(() => {
    if (!analysisData) {
      navigate('/practice');
    }
  }, [analysisData, navigate]);

  if (!analysisData) {
    return <div>Loading...</div>;
  }

  const renderScoreBar = (score, label) => (
    <div className="mb-4">
      <div className="flex justify-between items-center mb-2">
        <span className="text-gray-700">{label}</span>
        <span className="font-semibold text-primary-600">{score}/9</span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div 
          className="bg-primary-600 h-2 rounded-full transition-all duration-300"
          style={{ width: `${(score / 9) * 100}%` }}
        ></div>
      </div>
    </div>
  );

  const renderSentenceCorrection = (correction, index) => (
    <div key={index} className="border-b border-gray-200 pb-4 mb-4 last:border-b-0">
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Original:</span>
        <p className="text-gray-700 mt-1">{correction.original}</p>
      </div>
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Corrected:</span>
        <p className="text-green-700 font-medium mt-1">{correction.corrected}</p>
      </div>
      {correction.explanation && (
        <div>
          <span className="text-sm font-medium text-gray-500">Explanation:</span>
          <p className="text-gray-600 text-sm mt-1">{correction.explanation}</p>
        </div>
      )}
    </div>
  );

  const renderVocabularySuggestion = (suggestion, index) => (
    <div key={index} className="border-b border-gray-200 pb-4 mb-4 last:border-b-0">
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Word:</span>
        <p className="text-gray-700 mt-1 font-medium">{suggestion.word}</p>
      </div>
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Suggestion:</span>
        <p className="text-green-700 font-medium mt-1">{suggestion.suggestion}</p>
      </div>
      {suggestion.context && (
        <div>
          <span className="text-sm font-medium text-gray-500">Context:</span>
          <p className="text-gray-600 text-sm mt-1">{suggestion.context}</p>
        </div>
      )}
    </div>
  );

  const renderPronunciationTip = (tip, index) => (
    <div key={index} className="border-b border-gray-200 pb-4 mb-4 last:border-b-0">
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Word:</span>
        <p className="text-gray-700 mt-1 font-medium">{tip.word}</p>
      </div>
      <div className="mb-2">
        <span className="text-sm font-medium text-gray-500">Tip:</span>
        <p className="text-blue-700 font-medium mt-1">{tip.tip}</p>
      </div>
    </div>
  );

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate('/practice')}
          className="flex items-center space-x-2 text-gray-600 hover:text-gray-900"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Practice</span>
        </button>
        <button className="btn-primary">
          Practice Again
        </button>
      </div>

      {/* Performance Summary */}
      <div className="card">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Your Speaking Performance Summary</h2>
        
        {testSection && (
          <div className="mb-4">
            <span className="inline-block bg-primary-100 text-primary-600 px-3 py-1 rounded-full text-sm font-medium">
              {testSection.toUpperCase()}
            </span>
          </div>
        )}
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Overall Score */}
          <div className="text-center">
            <div className="text-6xl font-bold text-primary-600 mb-2">
              {analysisData.overallScore}
            </div>
            <div className="text-gray-600">Overall Band Score</div>
          </div>

          {/* Detailed Scores */}
          <div>
            {renderScoreBar(analysisData.fluencyScore, 'Fluency & Coherence')}
            {renderScoreBar(analysisData.lexicalScore, 'Lexical Resource')}
            {renderScoreBar(analysisData.grammaticalScore, 'Grammatical Range & Accuracy')}
            {renderScoreBar(analysisData.pronunciationScore, 'Pronunciation')}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Recorded Answer */}
        <div className="card">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">Recorded Answer</h3>
          
          {audioUrl && (
            <div className="mb-4">
              <audio
                controls
                className="w-full"
                onPlay={() => setIsPlayingRecording(true)}
                onPause={() => setIsPlayingRecording(false)}
                onEnded={() => setIsPlayingRecording(false)}
              >
                <source src={audioUrl} type="audio/wav" />
                Your browser does not support the audio element.
              </audio>
            </div>
          )}

          <div>
            <h4 className="font-medium text-gray-900 mb-2">Transcript</h4>
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-gray-700 leading-relaxed">
                {analysisData.transcript || 'No transcript available.'}
              </p>
            </div>
          </div>
        </div>

        {/* Feedback */}
        <div className="card">
          <h3 className="text-xl font-semibold text-gray-900 mb-4">Suggested Improvements</h3>
          
          {/* Feedback Tabs */}
          <div className="flex border-b border-gray-200 mb-4">
            <button
              onClick={() => setActiveTab('corrections')}
              className={`px-4 py-2 font-medium text-sm border-b-2 transition-colors ${
                activeTab === 'corrections'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Sentence Corrections
            </button>
            <button
              onClick={() => setActiveTab('vocabulary')}
              className={`px-4 py-2 font-medium text-sm border-b-2 transition-colors ${
                activeTab === 'vocabulary'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Vocabulary
            </button>
            <button
              onClick={() => setActiveTab('pronunciation')}
              className={`px-4 py-2 font-medium text-sm border-b-2 transition-colors ${
                activeTab === 'pronunciation'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Pronunciation
            </button>
          </div>

          {/* Tab Content */}
          <div className="min-h-[300px]">
            {activeTab === 'corrections' && (
              <div>
                {analysisData.sentenceCorrections && analysisData.sentenceCorrections.length > 0 ? (
                  analysisData.sentenceCorrections.map((correction, index) => 
                    renderSentenceCorrection(correction, index)
                  )
                ) : (
                  <p className="text-gray-500">No sentence corrections available.</p>
                )}
              </div>
            )}

            {activeTab === 'vocabulary' && (
              <div>
                {analysisData.vocabularySuggestions && analysisData.vocabularySuggestions.length > 0 ? (
                  analysisData.vocabularySuggestions.map((suggestion, index) => 
                    renderVocabularySuggestion(suggestion, index)
                  )
                ) : (
                  <p className="text-gray-500">No vocabulary suggestions available.</p>
                )}
              </div>
            )}

            {activeTab === 'pronunciation' && (
              <div>
                {analysisData.pronunciationTips && analysisData.pronunciationTips.length > 0 ? (
                  analysisData.pronunciationTips.map((tip, index) => 
                    renderPronunciationTip(tip, index)
                  )
                ) : (
                  <p className="text-gray-500">No pronunciation tips available.</p>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Overall Feedback */}
      <div className="card">
        <h3 className="text-xl font-semibold text-gray-900 mb-4">Overall Feedback</h3>
        <div className="bg-gray-50 p-4 rounded-lg">
          <p className="text-gray-700 leading-relaxed">
            {analysisData.feedback || 'No feedback available.'}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Feedback;
