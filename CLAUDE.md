# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Kotlin-based Spring Boot web application using Gradle as the build system. The project provides two main service modules: QQ information query service and DeepInfra API proxy service. The application is designed as a microservice architecture with database access, HTTP proxy, and Tor network support.

## Common Development Commands

### Building and Running
- `./gradlew build` - Build the project
- `./gradlew bootRun` - Run the application locally (runs on port 7001)
- `./gradlew bootJar` - Package the application as an executable JAR
- `./gradlew clean` - Clean the build directory

### Testing
- `./gradlew test` - Run all tests
- `./gradlew check` - Run all checks including tests

### Development Tasks
- `./gradlew tasks` - List all available Gradle tasks
- `./gradlew dependencies` - Show project dependencies

## Code Architecture and Structure

### Core Architecture
The application consists of two main service modules:

1. **QQ Information Query Service** (`SEDBController`) - Provides QQ to phone number bidirectional lookup
2. **DeepInfra API Proxy Service** (`deepInfra/` package) - HTTP proxy with Tor network integration

### Project Structure
```
src/
├── main/kotlin/com/tongle/tongleapi/
│   ├── TongleApiApplication.kt          # Main application entry point
│   ├── SEDBController.kt               # QQ information query controller
│   ├── deepInfra/                      # DeepInfra proxy service module
│   │   ├── ProxyService.kt             # Core HTTP proxy service
│   │   ├── InfraController.kt          # DeepInfra API controller
│   │   ├── TorService.kt               # Tor network service
│   │   └── HttpClientService.kt        # HTTP client configuration
│   └── exception/
│       └── GlobalExceptionHandler.kt   # Global exception handling
└── resources/
    └── application.properties          # Application configuration
```

### Key Components

#### 1. QQ Information Query Service (SEDBController)
- **API Endpoint**: `GET /sedb/qq?type={qqid|phone}&value={queryValue}`
- **Database**: MySQL table `qq8e` with fields `qq` and `phone`
- **Features**:
  - Bidirectional lookup (QQ ↔ phone number)
  - Input validation (QQ: 5-15 digits, Phone: 11 digits)
  - SQL injection protection with table name validation
  - Standardized response format using `ResponseData` class

#### 2. DeepInfra API Proxy Service
- **Core Service**: `ProxyService` - HTTP proxy with auto-retry and IP rotation
- **Tor Integration**: `TorService` - Manages Tor network connections and IP switching
- **HTTP Client**: `HttpClientService` - OkHttp3 client with Tor proxy support
- **API Controller**: `InfraController` - Routes all requests to `deepinfra/v1/**`

**Key Features**:
- Tor network integration with automatic IP switching
- Auto-retry mechanism (max 5 retries) on 403 errors
- Support for all HTTP methods (GET/POST/PUT/DELETE/PATCH)
- Request/response header forwarding
- Configurable timeouts: connect(10s), read(5m), write(15s)

#### 3. Global Exception Handler
- Centralized error handling with structured responses
- Comprehensive error logging with timestamps
- Consistent error message format across all endpoints

### Technology Stack
- **Kotlin 2.2.0** - Primary programming language
- **Spring Boot 3.5.4** - Application framework
- **Java 17** - Runtime environment
- **Gradle Kotlin DSL** - Build system
- **Spring Web MVC** - REST API framework
- **Spring JDBC** - Database access layer
- **MySQL Connector** - Database driver
- **OkHttp3** - HTTP client library
- **SLF4J** - Logging framework
- **JUnit 5** - Testing framework

## Configuration Requirements

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/socialdb
spring.datasource.username=socialdb_admin
spring.datasource.password=fuckrubbishapps
```

### Tor Network Configuration
- **Control Port**: 9051 (for Tor authentication)
- **SOCKS Proxy Port**: 9050 (for HTTP requests)
- **Authentication Password**: "142857@Tor"
- **IP Switch Delay**: 10 seconds after NEWNYM signal

### Application Configuration
- **Server Port**: 7001
- **Application Name**: tongle-api
- **Debug Logging**: Enabled for `com.tongle.tongleapi.deepInfra` and `okhttp3`

## Development Workflow

### Adding New Features
1. **Controllers**: Add new REST controllers in the main package or appropriate sub-package
2. **Services**: Create service classes for business logic (follow existing patterns in `deepInfra/`)
3. **Configuration**: Update `application.properties` for any new configurations
4. **Exception Handling**: Leverage existing `GlobalExceptionHandler` or add specific exceptions
5. **Testing**: Write integration tests in the test package following existing patterns

### Working with HTTP Proxies
When working with external APIs:
1. Use `HttpClientService` for creating OkHttp clients with Tor support
2. Implement retry logic using `TorService` for IP switching
3. Leverage `ProxyService` as a template for proxy implementations
4. Ensure proper error handling and timeout configuration

### Database Operations
1. Use `JdbcTemplate` for database operations
2. Follow the parameterized query pattern in `SEDBController`
3. Implement table name validation for dynamic queries
4. Use `ResponseData` class for consistent API responses

### Testing Guidelines
The project includes integration tests covering:
- DeepInfra API proxy functionality
- Tor network IP switching
- Chat endpoint testing with DeepSeek-V3.2-Exp model

Run tests with:
```bash
./gradlew test                    # Run all tests
./gradlew test --tests "*TestName"  # Run specific test
```

## Important Notes

- The application requires Tor service running locally for DeepInfra proxy functionality
- Database credentials are currently hardcoded and should be externalized in production
- Tor authentication uses a hardcoded password that should be configurable
- The application serves as both a QQ lookup service and an AI API proxy