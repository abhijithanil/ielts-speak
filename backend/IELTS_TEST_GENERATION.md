# IELTS Speaking Test Generation

## Overview

This feature generates complete IELTS Speaking tests with structured JSON format, including all three parts (Part 1, Part 2, Part 3) with proper questions and bullet points.

## Features

### ✅ Structured Test Generation
- **Part 1**: 4-5 personal questions (hometown, work, hobbies, family, studies)
- **Part 2**: 1 topic with 3-4 bullet points for 1-2 minute speaking
- **Part 3**: 4-5 deeper discussion questions related to Part 2

### ✅ JSON Format
The generated test follows this exact structure:
```json
{
  "part1": {
    "questions": ["Question 1", "Question 2", "Question 3", "Question 4"]
  },
  "part2": {
    "topic": "Describe a memorable event",
    "bullet_points": ["What happened?", "Who was involved?", "Why was it memorable?"]
  },
  "part3": {
    "discussion_questions": ["Question 1", "Question 2", "Question 3"]
  }
}
```

### ✅ Comprehensive Logging
- Detailed logs for API requests and responses
- Error handling with fallback to mock data
- File output for verification and debugging

### ✅ File Output
The system automatically saves:
- `deepseek_request_*.json` - API request sent to DeepSeek
- `deepseek_response_*.json` - Raw response from DeepSeek
- `ielts_test_generation_*.json` - Generated test content

## API Endpoints

### Generate Complete Test
```bash
POST /api/v1/ielts-test/generate
Authorization: Bearer <jwt-token>
```

### Generate Structured Test (New)
```bash
POST /api/v1/ielts-test/generate-structured
Authorization: Bearer <jwt-token>
```

## Usage

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Generate a Test
```bash
# Using curl
curl -X POST http://localhost:8080/api/v1/ielts-test/generate-structured \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>"

# Using the test script
./test-ielts-generation.bat
```

### 3. Check Generated Files
Look for these files in the project directory:
- `deepseek_request_20240809_120000.json`
- `deepseek_response_20240809_120000.json`
- `ielts_test_generation_20240809_120000.json`

## Implementation Details

### DeepSeekService
- `generateFullIeltsTest()` - Generates complete test with structured JSON
- `saveToFile()` - Saves content to timestamped files
- Enhanced logging with `@Slf4j`

### IeltsTestResponse DTO
- Structured DTO classes for Part1, Part2, Part3
- JSON annotations for proper serialization
- Nested classes for clean organization

### IeltsSpeakingTestService
- `generateCompleteTest()` - Parses JSON and creates test structure
- Handles all three parts with proper timing
- Fallback to mock data on errors

## Error Handling

### Fallback Strategy
1. **API Failure**: Falls back to mock test data
2. **JSON Parsing Error**: Uses mock response
3. **Network Issues**: Continues with cached/mock data

### Logging Levels
- `INFO`: Successful operations and important events
- `DEBUG`: Detailed API requests and responses
- `ERROR`: Errors with full stack traces
- `WARN`: Non-critical issues

## Testing

### Manual Testing
1. Start the application
2. Run `test-ielts-generation.bat`
3. Check generated files in project directory
4. Verify JSON structure and content

### Verification
- Check that all three parts are generated
- Verify question count (4-5 for Part 1, 1 for Part 2, 4-5 for Part 3)
- Ensure bullet points are provided for Part 2
- Confirm JSON is valid and well-formed

## Example Output

```json
{
  "part1": {
    "questions": [
      "Tell me about your hometown. Where is it located?",
      "What do you do for work or study?",
      "Do you enjoy reading books? What kind of books do you prefer?",
      "How do you usually spend your weekends?"
    ]
  },
  "part2": {
    "topic": "Describe a memorable trip you have taken",
    "bullet_points": [
      "Where did you go?",
      "Who did you travel with?",
      "What did you do there?",
      "Why was it memorable?"
    ]
  },
  "part3": {
    "discussion_questions": [
      "Why do you think people enjoy traveling to different countries?",
      "What are the advantages and disadvantages of traveling alone?",
      "How has tourism changed in recent years?",
      "Do you think it's important to learn about other cultures?"
    ]
  }
}
```

## Troubleshooting

### Common Issues
1. **Authentication Error**: Ensure valid JWT token
2. **API Timeout**: Check DeepSeek API key and network
3. **JSON Parsing Error**: Verify DeepSeek response format
4. **File Write Error**: Check directory permissions

### Debug Steps
1. Check application logs for detailed error messages
2. Verify generated files in project directory
3. Test API endpoints with Postman or curl
4. Check DeepSeek API key configuration

## Future Enhancements

- [ ] Add test difficulty levels
- [ ] Support for different question types
- [ ] Caching for generated tests
- [ ] Test validation and quality checks
- [ ] Integration with user preferences
