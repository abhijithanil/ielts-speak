import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Practice from './components/Practice';
import Feedback from './components/Feedback';
import Login from './components/Login';
import Signup from './components/Signup';
import IeltsSpeakingTest from './components/IeltsSpeakingTest';
import { VoiceProvider } from './contexts/VoiceContext';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <VoiceProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            
            {/* Protected routes */}
            <Route path="/" element={
              <ProtectedRoute>
                <div>
                  <Header />
                  <Dashboard />
                </div>
              </ProtectedRoute>
            } />
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <div>
                  <Header />
                  <Dashboard />
                </div>
              </ProtectedRoute>
            } />
            <Route path="/practice" element={
              <ProtectedRoute>
                <div>
                  <Header />
                  <Practice />
                </div>
              </ProtectedRoute>
            } />
            <Route path="/ielts-test" element={
              <ProtectedRoute>
                <div>
                  <Header />
                  <IeltsSpeakingTest />
                </div>
              </ProtectedRoute>
            } />
            <Route path="/feedback" element={
              <ProtectedRoute>
                <div>
                  <Header />
                  <Feedback />
                </div>
              </ProtectedRoute>
            } />
          </Routes>
        </div>
      </Router>
    </VoiceProvider>
  );
}

export default App;
