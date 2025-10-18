# Cerebra Task - File Sharing Application

A Spring Boot-based file sharing application with user authentication, file upload/download capabilities, and secure sharing features.

## 🚀 Features

- **User Authentication**: JWT-based authentication with OTP verification
- **File Management**: Upload, download, and manage files with size limits
- **Secure Sharing**: Generate secure share links with expiration
- **Connection Pooling**: Optimized database connection pool with HikariCP
- **Caching**: Redis-based caching for improved performance
- **Database Migration**: Flyway for database schema management
- **API Documentation**: Swagger/OpenAPI documentation
- **Monitoring**: Health checks and metrics via Spring Boot Actuator

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.6, Java 21
- **Database**: MySQL 8.0
- **Cache**: Redis 6.2
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit 5, Testcontainers
- **Build Tool**: Maven
- **Containerization**: Docker Compose

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd cerebra-task
```

### 2. Start Required Services

**⚠️ Important: You must run Docker Compose first before starting the application!**

```bash
# Start MySQL and Redis services
docker compose up -d
```

This will start:
- MySQL database on port 3306
- Redis cache on port 6379

### 3. Verify Services are Running

```bash
# Check if services are up
docker compose ps
```

### 4. Run the Application

```bash
# Build and run the application
mvn spring-boot:run
```

The application will be available at: `http://localhost:8080`

### 5. Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🧪 Running Tests

### Run All Tests

```bash
# Run unit tests and integration tests
mvn verify
```

### Run Specific Test Types

```bash
# Run only unit tests
mvn test

# Run only integration tests
mvn verify -DskipUnitTests=true

# Run tests with coverage report
mvn clean verify
```

### Test Coverage

After Run `verify` command, you need to run this command to generate report of jacoco
```
mvn jacoco:report
```

After running it, you can view the coverage report at:
```
target/site/jacoco/index.html
```

## 🔧 Configuration

### Database Configuration

The application uses HikariCP connection pooling with the following optimized settings:

- **Maximum Pool Size**: 20 connections
- **Minimum Idle**: 5 connections
- **Connection Timeout**: 20 seconds
- **Idle Timeout**: 5 minutes
- **Max Lifetime**: 20 minutes
- **Leak Detection**: 60 seconds
