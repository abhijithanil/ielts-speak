import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Mic, LogOut, User, Settings, CheckCircle } from 'lucide-react';
import { useVoice } from '../contexts/VoiceContext';

const Header = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const username = localStorage.getItem('username');
  const { 
    selectedVoice, 
    selectedGender,
    showVoiceSettings, 
    setShowVoiceSettings, 
    voiceSettingsRef, 
    handleVoiceSelection,
    handleGenderSelection
  } = useVoice();

  const isActive = (path) => {
    return location.pathname === path;
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/dashboard" className="flex items-center space-x-2">
              <Mic className="h-8 w-8 text-primary-600" />
              <span className="text-xl font-bold text-gray-900">IELTS Speaking</span>
            </Link>
          </div>

          <nav className="hidden md:flex space-x-8">
            <Link
              to="/dashboard"
              className={`px-3 py-2 rounded-md text-sm font-medium ${
                isActive('/dashboard') || isActive('/')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Dashboard
            </Link>
            <Link
              to="/practice"
              className={`px-3 py-2 rounded-md text-sm font-medium ${
                isActive('/practice')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Practice
            </Link>
            <Link
              to="/feedback"
              className={`px-3 py-2 rounded-md text-sm font-medium ${
                isActive('/feedback')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Feedback
            </Link>
          </nav>

          <div className="flex items-center space-x-4">
            {/* Voice Settings */}
            <div className="bg-white px-4 py-2 rounded-xl shadow-md relative" ref={voiceSettingsRef}>
              <div className="flex items-center space-x-2">
                <Settings className="h-4 w-4 text-gray-600" />
                <button
                  onClick={() => setShowVoiceSettings(!showVoiceSettings)}
                  className="px-3 py-1 rounded-full text-sm font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition-all duration-200"
                  title="Voice Settings"
                >
                  Voice: {selectedVoice} ({selectedGender})
                </button>
              </div>
              
              {/* Voice Selection Dropdown */}
              {showVoiceSettings && (
                <div className="absolute top-full right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-lg z-50 min-w-56">
                  <div className="p-3">
                    {/* Accent Selection */}
                    <div className="mb-3">
                      <div className="text-xs font-semibold text-gray-500 mb-2 px-2">Select Accent</div>
                      {['US', 'British', 'Australian', 'Indian'].map((accent) => (
                        <button
                          key={accent}
                          onClick={() => handleVoiceSelection(accent)}
                          className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all duration-200 ${
                            selectedVoice === accent
                              ? 'bg-blue-100 text-blue-700'
                              : 'hover:bg-gray-100 text-gray-700'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span>{accent}</span>
                            {selectedVoice === accent && (
                              <CheckCircle className="h-4 w-4 text-blue-600" />
                            )}
                          </div>
                        </button>
                      ))}
                    </div>

                    {/* Gender Selection */}
                    <div className="border-t border-gray-200 pt-3">
                      <div className="text-xs font-semibold text-gray-500 mb-2 px-2">Select Gender</div>
                      {['Male', 'Female'].map((gender) => (
                        <button
                          key={gender}
                          onClick={() => handleGenderSelection(gender)}
                          className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all duration-200 ${
                            selectedGender === gender
                              ? 'bg-green-100 text-green-700'
                              : 'hover:bg-gray-100 text-gray-700'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span>{gender}</span>
                            {selectedGender === gender && (
                              <CheckCircle className="h-4 w-4 text-green-600" />
                            )}
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center space-x-2 text-sm text-gray-600">
              <User className="h-4 w-4" />
              <span>{username}</span>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center space-x-2 px-3 py-2 text-sm font-medium text-gray-500 hover:text-gray-700"
            >
              <LogOut className="h-4 w-4" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
