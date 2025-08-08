# IELTS Speaking Practice Application

A full-stack web application that helps users practice for the IELTS speaking test. The app records user responses, transcribes them using Google's Speech-to-Text API, analyzes them with Google's Gemini AI, and provides detailed feedback with audio voiceovers.

## Features

- **Voice Recording**: Record audio responses to IELTS speaking questions
- **Speech-to-Text**: Automatic transcription using Google Cloud Speech-to-Text API
- **AI Analysis**: Detailed feedback using Google Gemini AI
- **Audio Feedback**: Text-to-speech conversion of feedback using Google Cloud Text-to-Speech API
- **Performance Scoring**: IELTS band scoring (0-9) across four criteria:
  - Fluency & Coherence
  - Lexical Resource
  - Grammatical Range & Accuracy
  - Pronunciation
- **Interactive UI**: Modern, responsive interface built with React and Tailwind CSS
- **Question Bank**: Pre-built IELTS speaking questions
- **Session History**: Track practice sessions and progress

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.2.0**
- **Spring Data JPA** for database operations
- **H2 Database** (development) / **PostgreSQL** (production)
- **Google Cloud APIs**:
  - Speech-to-Text API
  - Text-to-Speech API
  - Gemini AI API
- **Maven** for dependency management

### Frontend
- **React 18** with **Vite**
- **Tailwind CSS** for styling
- **React Router** for navigation
- **Axios** for API communication
- **Lucide React** for icons

## Prerequisites

1. **Java 17** or higher
2. **Node.js 16** or higher
3. **Maven 3.6** or higher
4. **Google Cloud Project** with the following APIs enabled:
   - Speech-to-Text API
   - Text-to-Speech API
   - Gemini AI API

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ielts-speaking-app
```

### 2. Backend Setup

#### 2.1. Configure Google Cloud Credentials

1. Create a Google Cloud project and enable the required APIs
2. Create a service account and download the JSON key file
3. Set the environment variable:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="path/to/your/service-account-key.json"
```

#### 2.2. Configure API Keys

Create a `.env` file in the `backend` directory:

```bash
# Google Cloud Configuration
GOOGLE_CLOUD_PROJECT_ID=your-project-id

# Gemini API Configuration
GEMINI_API_KEY=your-gemini-api-key
```

#### 2.3. Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Frontend Setup

#### 3.1. Install Dependencies

```bash
cd frontend
npm install
```

#### 3.2. Run the Frontend

```bash
npm run dev
```

The frontend will start on `http://localhost:3000`

## API Endpoints

### Speech Analysis
- `POST /api/v1/speech/analyze` - Analyze speech with audio file
- `POST /api/v1/speech/analyze-base64` - Analyze speech with base64 audio

### Questions
- `GET /api/v1/questions/random` - Get a random question
- `GET /api/v1/questions/all` - Get all questions
- `GET /api/v1/questions/count` - Get question count

## Usage

1. **Start Practice**: Click "Start Practice" on the dashboard
2. **Record Response**: Click the microphone button to start recording your answer
3. **Submit**: Click "Stop & Submit" when finished
4. **View Feedback**: Review your transcript, scores, and detailed feedback
5. **Listen to Feedback**: Play the AI-generated audio feedback

## Project Structure

```
ielts-speaking-app/
├── backend/
│   ├── src/main/java/com/ielts/speakingapp/
│   │   ├── controllers/          # REST controllers
│   │   ├── models/              # JPA entities
│   │   ├── models/dto/          # Data transfer objects
│   │   ├── repositories/        # JPA repositories
│   │   ├── services/            # Business logic services
│   │   └── config/              # Configuration classes
│   ├── src/main/resources/
│   │   └── application.yml      # Application configuration
│   └── pom.xml                  # Maven dependencies
├── frontend/
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── App.jsx             # Main app component
│   │   └── main.jsx            # Entry point
│   ├── package.json            # Node.js dependencies
│   └── vite.config.js          # Vite configuration
└── README.md
```

## Deployment

### Backend (Google Cloud Run)

1. Build the Docker image:
```bash
cd backend
mvn clean package
docker build -t ielts-speaking-backend .
```

2. Deploy to Cloud Run:
```bash
gcloud run deploy ielts-speaking-backend \
  --image gcr.io/your-project/ielts-speaking-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Frontend (Firebase Hosting)

1. Build the production version:
```bash
cd frontend
npm run build
```

2. Deploy to Firebase:
```bash
firebase deploy
```

## Environment Variables

### Backend
- `GOOGLE_CLOUD_PROJECT_ID`: Your Google Cloud project ID
- `GEMINI_API_KEY`: Your Gemini AI API key
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to service account key file

### Frontend
- `VITE_API_BASE_URL`: Backend API base URL (default: http://localhost:8080)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please open an issue in the GitHub repository or contact the development team.
