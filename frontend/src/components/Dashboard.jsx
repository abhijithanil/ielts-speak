import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Mic, BarChart3, Clock, Target, BookOpen, Play, TrendingUp, Award, Calendar, Bookmark } from 'lucide-react';
import api from '../utils/axios';

const Dashboard = () => {
  const [username] = useState(localStorage.getItem('username') || 'User');
  const [stats, setStats] = useState({
    totalSessions: 0,
    averageScore: 0,
    totalTimeSpent: 0,
    recentSessions: 0,
    improvementPercentage: 0,
    sectionStats: {},
    vocabularyStats: {},
    recentSessionsList: []
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const sections = [
    {
      id: 'part1',
      name: 'Part 1 - Introduction & Interview',
      description: 'Questions about familiar topics like hometown, family, work, studies, hobbies.',
      duration: '4-5 minutes',
      icon: <Mic className="h-6 w-6" />
    },
    {
      id: 'part2',
      name: 'Part 2 - Individual Long Turn',
      description: 'Speak for 1-2 minutes on a specific topic with bullet points.',
      duration: '3-4 minutes',
      icon: <BookOpen className="h-6 w-6" />
    },
    {
      id: 'part3',
      name: 'Part 3 - Two-Way Discussion',
      description: 'Deeper discussion on abstract topics and ideas.',
      duration: '4-5 minutes',
      icon: <Target className="h-6 w-6" />
    }
  ];

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    try {
      setIsLoading(true);
      const response = await api.get('/v1/dashboard/stats');
      setStats(response.data);
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      setError('Failed to load dashboard data');
    } finally {
      setIsLoading(false);
    }
  };

  const formatTime = (seconds) => {
    if (!seconds) return '0 min';
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getScoreColor = (score) => {
    if (score >= 8) return 'text-green-600';
    if (score >= 7) return 'text-blue-600';
    if (score >= 6) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getImprovementColor = (percentage) => {
    if (percentage > 0) return 'text-green-600';
    if (percentage < 0) return 'text-red-600';
    return 'text-gray-600';
  };

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Welcome back, {username}! 👋
        </h1>
        <p className="text-gray-600">
          Ready to improve your IELTS speaking skills? Here's your progress overview.
        </p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-100">
          <div className="flex items-center">
            <div className="p-3 bg-blue-100 rounded-lg">
              <BarChart3 className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total Sessions</p>
              <p className="text-2xl font-bold text-gray-900">{stats.totalSessions}</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-100">
          <div className="flex items-center">
            <div className="p-3 bg-green-100 rounded-lg">
              <Target className="h-6 w-6 text-green-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Average Score</p>
              <p className={`text-2xl font-bold ${getScoreColor(stats.averageScore)}`}>
                {stats.averageScore.toFixed(1)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-100">
          <div className="flex items-center">
            <div className="p-3 bg-purple-100 rounded-lg">
              <Clock className="h-6 w-6 text-purple-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Time Spent</p>
              <p className="text-2xl font-bold text-gray-900">{formatTime(stats.totalTimeSpent)}</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-100">
          <div className="flex items-center">
            <div className="p-3 bg-yellow-100 rounded-lg">
              <TrendingUp className="h-6 w-6 text-yellow-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Improvement</p>
              <p className={`text-2xl font-bold ${getImprovementColor(stats.improvementPercentage)}`}>
                {stats.improvementPercentage > 0 ? '+' : ''}{stats.improvementPercentage.toFixed(1)}%
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Section-wise Stats */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        {/* Practice Sections */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Practice Sections</h2>
          <div className="space-y-4">
            {sections.map((section) => {
              const sectionData = stats.sectionStats[section.id] || { count: 0, averageScore: 0 };
              return (
                <div key={section.id} className="bg-white rounded-xl shadow-lg p-6 border border-gray-100 hover:shadow-xl transition-shadow">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center">
                      <div className="p-2 bg-gradient-to-r from-blue-500 to-purple-500 rounded-lg">
                        {section.icon}
                      </div>
                      <div className="ml-3">
                        <h3 className="text-lg font-semibold text-gray-900">{section.name}</h3>
                        <p className="text-sm text-gray-500">{section.duration}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-gray-600">Sessions: {sectionData.count}</p>
                      <p className={`text-lg font-bold ${getScoreColor(sectionData.averageScore)}`}>
                        {sectionData.averageScore.toFixed(1)}
                      </p>
                    </div>
                  </div>
                  <p className="text-gray-600 mb-4">{section.description}</p>
                  <Link
                    to={`/practice?section=${section.id}`}
                    className="inline-flex items-center px-4 py-2 bg-gradient-to-r from-blue-600 to-purple-600 text-white text-sm font-medium rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-lg hover:shadow-xl"
                  >
                    <Play className="h-4 w-4 mr-2" />
                    Start Practice
                  </Link>
                </div>
              );
            })}
          </div>
        </div>

        {/* Recent Activity */}
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Recent Activity</h2>
          <div className="bg-white rounded-xl shadow-lg border border-gray-100">
            {stats.recentSessionsList.length > 0 ? (
              <div className="divide-y divide-gray-200">
                {stats.recentSessionsList.slice(0, 5).map((session) => (
                  <div key={session.id} className="p-6">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {session.question.length > 50 ? session.question.substring(0, 50) + '...' : session.question}
                        </p>
                        <div className="flex items-center mt-1">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            {session.testSection.toUpperCase()}
                          </span>
                          <span className="ml-2 text-sm text-gray-500">
                            {formatDate(session.createdAt)}
                          </span>
                        </div>
                      </div>
                      <div className="ml-4">
                        <p className={`text-lg font-bold ${getScoreColor(session.overallScore)}`}>
                          {session.overallScore?.toFixed(1) || 'N/A'}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="p-6 text-center text-gray-500">
                <Bookmark className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                <p>No recent sessions. Start practicing to see your activity here!</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-100">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
        <div className="flex flex-wrap gap-4">
          <Link
            to="/ielts-test"
            className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-green-600 to-emerald-600 text-white text-sm font-medium rounded-lg hover:from-green-700 hover:to-emerald-700 transition-all duration-200 shadow-lg hover:shadow-xl"
          >
            <Target className="h-4 w-4 mr-2" />
            Full IELTS Test
          </Link>
          <Link
            to="/practice"
            className="inline-flex items-center px-6 py-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white text-sm font-medium rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-200 shadow-lg hover:shadow-xl"
          >
            <Mic className="h-4 w-4 mr-2" />
            Random Practice
          </Link>
          <Link
            to="/feedback"
            className="inline-flex items-center px-6 py-3 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition-all duration-200"
          >
            <BarChart3 className="h-4 w-4 mr-2" />
            View Feedback
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
