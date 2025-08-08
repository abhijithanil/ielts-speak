// Environment configuration
const config = {
  // To change environment, modify this line:
  environment: 'dev', // Change to 'prod' for production

  // Question timer settings (in seconds)
  questionTimers: {
    part1: 30, // 30 seconds for Part 1 questions
    part2: 200, // 2 minutes for Part 2 cue card + preparation time + additional time
    part3: 45, // 45 seconds for Part 3 questions
  },

  // Skip functionality settings
  // In development: allowSkip = true (can skip questions)
  // In production: allowSkip = false (must answer questions)
  allowSkip: true, // Set to false for production

  // Auto-submit when timer runs out
  autoSubmitOnTimeUp: true,

  // Automatic question speaking settings
  autoSpeak: {
    enabled: true, // User can toggle this - enabled by default
    delay: 1000, // 1 second delay before speaking
  },

  // Preparation timer for Part 2 (Q-card)
  preparationTimer: {
    part2: 60, // 1 minute preparation time
  },

  // Automatic recording settings
  autoRecording: {
    part1: 5000, // 5 seconds delay for Part 1
    part2: 0, // No delay for Part 2 (starts after preparation timer)
    part3: 5000, // 5 seconds delay for Part 3
  },

  // Recording time limits (in seconds)
  recordingTimeLimits: {
    part1: 60, // 1 minute for Part 1
    part2: 120, // 2 minutes for Part 2
    part3: 120, // 2 minutes for Part 3
    complete: 300, // 5 minutes for complete test
  },
};

// Override with environment variables if available
if (import.meta.env.VITE_APP_ENV) {
  config.environment = import.meta.env.VITE_APP_ENV;
  config.allowSkip = import.meta.env.VITE_APP_ENV === 'dev';
}

export default config;
