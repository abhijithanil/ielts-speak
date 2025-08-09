#!/bin/bash

echo "PostgreSQL Database Setup for IELTS Speaking App"
echo "================================================"
echo

echo "This script will help you set up the PostgreSQL database."
echo "Make sure PostgreSQL is installed and running on port 5432."
echo

read -p "Enter database name (default: ielts_speaking_app): " DB_NAME
DB_NAME=${DB_NAME:-ielts_speaking_app}

read -p "Enter database user (default: postgres): " DB_USER
DB_USER=${DB_USER:-postgres}

echo
echo "Creating database '$DB_NAME' with user '$DB_USER'..."
echo

psql -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"

if [ $? -eq 0 ]; then
    echo
    echo "Database '$DB_NAME' created successfully!"
    echo
    echo "Next steps:"
    echo "1. Start the application: mvn spring-boot:run"
    echo "2. The application will automatically create the required tables"
    echo "3. Access the application at: http://localhost:8080/api"
    echo
else
    echo
    echo "Error creating database. Please check:"
    echo "1. PostgreSQL is running on port 5432"
    echo "2. User '$DB_USER' has permission to create databases"
    echo "3. Database '$DB_NAME' doesn't already exist"
    echo
fi
