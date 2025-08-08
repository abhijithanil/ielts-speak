# Environment Configuration

This document explains how to configure the environment settings for the IELTS Speaking App.

## Environment Modes

The application supports two environment modes:

### Development Mode (`dev`)
- **Skip Questions**: ✅ Enabled - Users can skip questions without answering
- **Timer**: ⏱️ Active - Questions have time limits
- **Auto-submit**: ✅ Enabled - Questions auto-submit when timer runs out
- **Environment Badge**: 🟡 Yellow badge showing "DEV"

### Production Mode (`prod`)
- **Skip Questions**: ❌ Disabled - Users must answer questions to proceed
- **Timer**: ⏱️ Active - Questions have time limits
- **Auto-submit**: ✅ Enabled - Questions auto-submit when timer runs out
- **Environment Badge**: 🟢 Green badge showing "PROD"

## Configuration

### Method 1: Direct Configuration (Recommended)

Edit `frontend/src/utils/config.js`:

```javascript
const config = {
  // Change this line to switch environments
  environment: 'dev', // Change to 'prod' for production
  
  // Question timer settings (in seconds)
  questionTimers: {
    part1: 30, // 30 seconds for Part 1 questions
    part2: 120, // 2 minutes for Part 2 cue card
    part3: 45, // 45 seconds for Part 3 questions
  },
  
  // Skip functionality settings
  allowSkip: true, // Set to false for production
  
  // Auto-submit when timer runs out
  autoSubmitOnTimeUp: true,
};
```

### Method 2: Environment Variables

Create environment files in the frontend directory:

**For Development:**
```bash
# frontend/.env.development
VITE_APP_ENV=dev
```

**For Production:**
```bash
# frontend/.env.production
VITE_APP_ENV=prod
```

## Question Timers

Each section has different timer durations:

- **Part 1**: 30 seconds per question
- **Part 2**: 2 minutes for cue card
- **Part 3**: 45 seconds per question

## Features

### Timer Display
- Timer is shown in the header with countdown
- Timer turns red when ≤ 10 seconds remaining
- Auto-submits when timer reaches 0

### Skip Functionality
- Only available in development mode
- Skip button appears when question hasn't been answered
- Shows yellow "Skip Question" button

### Navigation
- Previous button: Always enabled (except for first question)
- Next button: 
  - Development: Always enabled
  - Production: Only enabled after answering the question

### Environment Badge
- Shows current environment in the header
- Yellow for development, green for production

## Switching Environments

1. **For Development Testing:**
   ```javascript
   environment: 'dev'
   allowSkip: true
   ```

2. **For Production:**
   ```javascript
   environment: 'prod'
   allowSkip: false
   ```

## Testing

To test the environment configuration:

1. Set environment to `dev` and verify:
   - Skip button appears
   - Can navigate without answering
   - Yellow "DEV" badge shows

2. Set environment to `prod` and verify:
   - Skip button is hidden
   - Must answer questions to proceed
   - Green "PROD" badge shows
