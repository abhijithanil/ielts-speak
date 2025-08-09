import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Mic, LogOut, User, Settings, CheckCircle, Menu, X } from 'lucide-react';
import { useVoice } from '../contexts/VoiceContext';

const Header = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const username = localStorage.getItem('username');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  
  const { 
    selectedVoice, 
    selectedGender,
    showVoiceSettings, 
    setShowVoiceSettings, 
    voiceSettingsRef, 
    handleVoiceSelection,
    handleGenderSelection,
    voicesLoaded
  } = useVoice();

  const isActive = (path) => {
    return location.pathname === path;
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 relative">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to="/dashboard" className="flex items-center space-x-2" onClick={closeMobileMenu}>
              <Mic className="h-6 w-6 sm:h-8 sm:w-8 text-primary-600" />
              <span className="text-lg sm:text-xl font-bold text-gray-900">IELTS Speaking</span>
            </Link>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex space-x-8">
            <Link
              to="/dashboard"
              className={`px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${
                isActive('/dashboard') || isActive('/')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Dashboard
            </Link>
            <Link
              to="/practice"
              className={`px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${
                isActive('/practice')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Practice
            </Link>
            <Link
              to="/feedback"
              className={`px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${
                isActive('/feedback')
                  ? 'text-primary-600 bg-primary-100'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Feedback
            </Link>
          </nav>

          {/* Desktop Voice Settings & User Menu */}
          <div className="hidden lg:flex items-center space-x-4">
            {/* Voice Settings */}
            <div className="relative" ref={voiceSettingsRef}>
              <button
                onClick={() => setShowVoiceSettings(!showVoiceSettings)}
                className="flex items-center space-x-2 bg-white px-4 py-2 rounded-xl shadow-md hover:shadow-lg transition-all duration-200 border border-gray-200"
                title="Voice Settings"
              >
                <Settings className="h-4 w-4 text-gray-600" />
                <span className="text-sm font-medium text-gray-600">
                  {selectedVoice} ({selectedGender})
                </span>
              </button>
              
              {/* Voice Selection Dropdown */}
              {showVoiceSettings && (
                <div className="absolute top-full right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-lg z-50 min-w-64 max-w-xs">
                  <div className="p-4">
                    {!voicesLoaded && (
                      <div className="text-center py-4">
                        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto mb-2"></div>
                        <p className="text-sm text-gray-500">Loading voices...</p>
                      </div>
                    )}
                    
                    {voicesLoaded && (
                      <>
                        {/* Accent Selection */}
                        <div className="mb-4">
                          <div className="text-xs font-semibold text-gray-500 mb-2 px-2">Select Accent</div>
                          {['US', 'British', 'Australian', 'Indian'].map((accent) => (
                            <button
                              key={accent}
                              onClick={() => handleVoiceSelection(accent)}
                              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all duration-200 ${
                                selectedVoice === accent
                                  ? 'bg-blue-100 text-blue-700 border border-blue-200'
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
                        <div className="border-t border-gray-200 pt-4">
                          <div className="text-xs font-semibold text-gray-500 mb-2 px-2">Select Gender</div>
                          {['Male', 'Female'].map((gender) => (
                            <button
                              key={gender}
                              onClick={() => handleGenderSelection(gender)}
                              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all duration-200 ${
                                selectedGender === gender
                                  ? 'bg-green-100 text-green-700 border border-green-200'
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
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* User Info */}
            <div className="flex items-center space-x-2 text-sm text-gray-600">
              <User className="h-4 w-4" />
              <span className="max-w-24 truncate">{username}</span>
            </div>
            
            {/* Logout Button */}
            <button
              onClick={handleLogout}
              className="flex items-center space-x-2 px-3 py-2 text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors duration-200"
            >
              <LogOut className="h-4 w-4" />
              <span>Logout</span>
            </button>
          </div>

          {/* Mobile Menu Button */}
          <div className="lg:hidden">
            <button
              onClick={toggleMobileMenu}
              className="p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100 transition-colors duration-200"
            >
              {isMobileMenuOpen ? (
                <X className="h-6 w-6" />
              ) : (
                <Menu className="h-6 w-6" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="lg:hidden absolute top-full left-0 right-0 bg-white border-b border-gray-200 shadow-lg z-50">
          <div className="px-4 py-4 space-y-4">
            {/* Mobile Navigation Links */}
            <div className="space-y-2">
              <Link
                to="/dashboard"
                onClick={closeMobileMenu}
                className={`block px-3 py-2 rounded-md text-base font-medium transition-colors duration-200 ${
                  isActive('/dashboard') || isActive('/')
                    ? 'text-primary-600 bg-primary-100'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                }`}
              >
                Dashboard
              </Link>
              <Link
                to="/practice"
                onClick={closeMobileMenu}
                className={`block px-3 py-2 rounded-md text-base font-medium transition-colors duration-200 ${
                  isActive('/practice')
                    ? 'text-primary-600 bg-primary-100'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                }`}
              >
                Practice
              </Link>
              <Link
                to="/feedback"
                onClick={closeMobileMenu}
                className={`block px-3 py-2 rounded-md text-base font-medium transition-colors duration-200 ${
                  isActive('/feedback')
                    ? 'text-primary-600 bg-primary-100'
                    : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
                }`}
              >
                Feedback
              </Link>
            </div>

            {/* Mobile Voice Settings */}
            <div className="border-t border-gray-200 pt-4">
              <div className="mb-3">
                <div className="flex items-center space-x-2 mb-2">
                  <Settings className="h-4 w-4 text-gray-600" />
                  <span className="text-sm font-semibold text-gray-700">Voice Settings</span>
                </div>
                <div className="text-xs text-gray-500 mb-3">
                  Current: {selectedVoice} ({selectedGender})
                </div>
              </div>

              {!voicesLoaded && (
                <div className="text-center py-4">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto mb-2"></div>
                  <p className="text-sm text-gray-500">Loading voices...</p>
                </div>
              )}

              {voicesLoaded && (
                <>
                  {/* Mobile Accent Selection */}
                  <div className="mb-4">
                    <div className="text-xs font-semibold text-gray-500 mb-2">Select Accent</div>
                    <div className="grid grid-cols-2 gap-2">
                      {['US', 'British', 'Australian', 'Indian'].map((accent) => (
                        <button
                          key={accent}
                          onClick={() => handleVoiceSelection(accent)}
                          className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                            selectedVoice === accent
                              ? 'bg-blue-100 text-blue-700 border border-blue-200'
                              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                          }`}
                        >
                          <div className="flex items-center justify-center space-x-1">
                            <span>{accent}</span>
                            {selectedVoice === accent && (
                              <CheckCircle className="h-3 w-3 text-blue-600" />
                            )}
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Mobile Gender Selection */}
                  <div className="mb-4">
                    <div className="text-xs font-semibold text-gray-500 mb-2">Select Gender</div>
                    <div className="grid grid-cols-2 gap-2">
                      {['Male', 'Female'].map((gender) => (
                        <button
                          key={gender}
                          onClick={() => handleGenderSelection(gender)}
                          className={`px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                            selectedGender === gender
                              ? 'bg-green-100 text-green-700 border border-green-200'
                              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                          }`}
                        >
                          <div className="flex items-center justify-center space-x-1">
                            <span>{gender}</span>
                            {selectedGender === gender && (
                              <CheckCircle className="h-3 w-3 text-green-600" />
                            )}
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>

            {/* Mobile User Info & Logout */}
            <div className="border-t border-gray-200 pt-4 space-y-3">
              <div className="flex items-center space-x-2 px-3 py-2 bg-gray-50 rounded-lg">
                <User className="h-4 w-4 text-gray-600" />
                <span className="text-sm text-gray-700 font-medium">{username}</span>
              </div>
              <button
                onClick={() => {
                  handleLogout();
                  closeMobileMenu();
                }}
                className="flex items-center space-x-2 w-full px-3 py-2 text-sm font-medium text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors duration-200"
              >
                <LogOut className="h-4 w-4" />
                <span>Logout</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;