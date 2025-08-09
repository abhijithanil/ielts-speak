# PostgreSQL Setup for IELTS Speaking App

## Prerequisites

1. **PostgreSQL Installation**
   - Download and install PostgreSQL from [https://www.postgresql.org/download/](https://www.postgresql.org/download/)
   - Make sure PostgreSQL service is running on port 5432 (default)

2. **Database Tools** (Optional)
   - pgAdmin (GUI tool for PostgreSQL)
   - psql (command-line tool)

## Database Setup

### Option 1: Using psql Command Line

1. **Open Command Prompt/Terminal**
2. **Connect to PostgreSQL as superuser:**
   ```bash
   psql -U postgres
   ```
3. **Create the database:**
   ```sql
   CREATE DATABASE ielts_speaking_app;
   ```
4. **Verify the database was created:**
   ```sql
   \l
   ```
5. **Exit psql:**
   ```sql
   \q
   ```

### Option 2: Using pgAdmin

1. **Open pgAdmin**
2. **Right-click on "Databases"**
3. **Select "Create" > "Database"**
4. **Enter database name:** `ielts_speaking_app`
5. **Click "Save"**

## Environment Variables

Set the following environment variables (optional - defaults are provided):

```bash
# Database Configuration
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password

# Other configurations
DEEPSEEK_API_KEY=your-deepseek-api-key
GOOGLE_CLOUD_PROJECT_ID=your-google-cloud-project-id
JWT_SECRET=your-jwt-secret
```

## Application Configuration

The application is configured to:
- **Host:** localhost
- **Port:** 5432
- **Database:** ielts_speaking_app
- **Username:** postgres (or value of DB_USERNAME)
- **Password:** password (or value of DB_PASSWORD)

## Running the Application

1. **Make sure PostgreSQL is running**
2. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```
3. **The application will automatically:**
   - Connect to the PostgreSQL database
   - Create tables if they don't exist (ddl-auto: update)
   - Start the Spring Boot application

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Make sure PostgreSQL service is running
   - Check if port 5432 is available
   - Verify PostgreSQL is installed correctly

2. **Authentication Failed**
   - Check username and password
   - Make sure the user has access to the database
   - Verify environment variables are set correctly

3. **Database Not Found**
   - Make sure the database `ielts_speaking_app` exists
   - Run the database creation script if needed

### Useful Commands

```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql  # Linux
brew services list | grep postgresql  # macOS
# On Windows, check Services app

# Connect to database
psql -U postgres -d ielts_speaking_app

# List all tables (after application has run)
\dt

# Describe table structure
\d+ users
\d+ practice_sessions
```

## Migration from H2

If you were previously using H2 database:

1. **Backup your data** (if any)
2. **Follow the PostgreSQL setup above**
3. **Start the application** - tables will be created automatically
4. **Data will need to be recreated** as H2 and PostgreSQL are different databases

## Production Considerations

For production deployment:

1. **Use environment variables** for sensitive data
2. **Set up proper database backups**
3. **Configure connection pooling**
4. **Use SSL connections** if required
5. **Set up proper database user permissions**
