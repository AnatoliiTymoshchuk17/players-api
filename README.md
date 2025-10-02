# Player API Test Framework

Test automation framework for Player Controller API using Java 17, TestNG, and RestAssured.

## Technology Stack

- **Java 17** - Programming language
- **TestNG** - Test framework
- **RestAssured** - REST API testing library
- **Allure** - Test reporting
- **Owner** - Configuration management
- **DataFaker** - Test data generation
- **Log4j2** - Logging
- **Maven** - Build and dependency management

## Project Structure

```
players-api/
├── src/main/
│   ├── java/spribe/task/
│   │   ├── api/
│   │   │   ├── core/           # Core framework classes
│   │   │   ├── model/          # Data models
│   │   │   └── services/       # API service layer
│   │   ├── common/env/         # Configuration
│   │   ├── support/listeners/  # TestNG listeners
│   │   └── util/               # Utilities
│   └── resources/
│       ├── prod/               # Production config
│       ├── qa/                 # QA config
│       ├── stage/              # Stage config
│       └── dev/                # Development config
├── src/test/
│   ├── java/
│   │   ├── base/               # Base test class
│   │   └── spribe/task/tests/  # Test classes
│   └── resources/
│       ├── suites/             # TestNG suites
│       └── allure/             # Allure categories
└── pom.xml
```

## Configuration

The framework supports multiple environments through configuration files in `src/main/resources/{env}/config.properties`.

### Available Environments
- **prod** (default)
- **qa**
- **stage**
- **dev**

### Configuration Properties

```properties
# Application settings
app.baseUrl=http://3.68.165.45

# Authentication
editor.supervisor=supervisor
editor.admin=admin

# API Endpoints (configurable per environment)
endpoint.player.create=/player/create/{editor}
endpoint.player.get.by.id=/player/get
endpoint.player.get.all=/player/get/all
endpoint.player.update=/player/update/{editor}/{id}
endpoint.player.delete=/player/delete/{editor}

# Test execution
test.thread.count=3
test.retry.count=1

# API client
api.timeout=5000
api.retries=2

# Test data generation
test.user.min.age=16
test.user.max.age=60
test.password.min.length=7
test.password.max.length=15
```

## Running Tests

### Run all tests (default: prod environment)
```bash
mvn clean test
```

### Run tests in specific environment
```bash
mvn clean test -Denv=qa
mvn clean test -Denv=stage
mvn clean test -Denv=dev
```

### Run with custom thread count
```bash
mvn clean test -Dtest.thread.count=5
```

### Run with custom base URL
```bash
mvn clean test -Dapp.baseUrl=http://custom-url.com
```

### Run specific test class
```bash
mvn clean test -Dtest=CreatePlayerTests
```

### Run specific test method
```bash
mvn clean test -Dtest=CreatePlayerTests#supervisorCanCreateUserWithValidDataTest
```

## Generating Allure Report

### Generate and open report
```bash
mvn allure:serve
```

### Generate report only
```bash
mvn allure:report
```

Report will be available in `target/site/allure-maven-plugin/index.html`

### Allure Report Features

Our Allure reports include:

#### 📊 Overview
- Test execution summary
- Success rate
- Duration statistics
- Historical trends

#### 🔍 Categories
Automatic error classification:
- **Product defects** - Application bugs (AssertionError)
- **Test defects** - Test code issues (NPE, IllegalArgument)
- **Connection issues** - Network problems
- **Timeout issues** - Tests that timed out
- **Deserialization issues** - JSON parsing problems
- **HTTP Status Mismatch** - Unexpected status codes

#### 📝 Detailed Steps
Each test shows:
```
Test: Get player by id returns correct data
  ├─ Create player as supervisor
  │   ├─ Request: GET /player/create/{editor}
  │   ├─ Response: 200 (125ms)
  │   └─ Attachments: Request body, Response body
  ├─ Verify HTTP status code is 200
  │   └─ Attachments: Expected (200), Actual (200)
  ├─ Get player by id=123
  │   ├─ Request: POST /player/get
  │   ├─ Response: 200 (45ms)
  │   └─ Attachments: Request body, Response body (pretty JSON)
  └─ Verify player fields match
      ├─ Verify player ID is 123
      ├─ Verify login is 'testuser'
      └─ Verify role is 'user'
```

#### 📎 Attachments
- Request/Response bodies (pretty-printed JSON)
- Error messages and stack traces
- Response metrics (status, time)
- Expected vs Actual values
- Test data and configuration

#### 📈 Graphs & Charts
- Test duration distribution
- Success rate trend
- Retry statistics
- Category distribution

#### ⏱️ Timeline
- Parallel test execution visualization
- Test duration comparison
- Thread utilization

## Test Coverage

### Endpoints Tested
- ✅ `GET /player/create/{editor}` - Create player
- ✅ `POST /player/get` - Get player by ID
- ✅ `GET /player/get/all` - Get all players
- ✅ `PATCH /player/update/{editor}/{id}` - Update player
- ✅ `DELETE /player/delete/{editor}` - Delete player

## Key Features

### ✅ Configuration Management
- Environment-specific configurations
- System property overrides
- Owner library for type-safe config access

### ✅ Parallel Execution
- TestNG parallel execution at method level
- Configurable thread count
- Thread-safe test data generation

### ✅ Retry Mechanism
- Automatic retry for flaky tests
- Configurable retry count

### ✅ Comprehensive Logging
- **Request/Response Logging** - Via RestAssured with AllureRestAssured filter
- **Test Logging** - Every test action logged with context
- **Assertion Logging** - Each validation step logged separately
- **Response Details** - Status, content-type, response time
- **Error Logging** - Proper exception handling with WARN/ERROR levels
- **Log4j2** - Configurable logging framework
- **Different Levels** - INFO for main flow, DEBUG for details, WARN for issues

### ✅ Allure Reporting
- **Detailed Steps** - Every action and assertion as separate step
- **Automatic Attachments** - Request/response bodies, error details, metrics
- **Categories** - Automatic error classification (6 categories)
- **Environment Info** - Detailed test environment configuration
- **Response Metrics** - Status codes, response times, content types
- **Pretty JSON** - Formatted request/response bodies
- **Trends & Timeline** - Historical data and parallel execution visualization
- **Soft Assertions** - TestNG SoftAssert for comprehensive field validation

### ✅ Clean Code Practices
- Service layer pattern
- Response wrapper for type-safe deserialization
- Test data generators
- No Lombok, AssertJ, or BDD (as per requirements)

## Requirements

- **Java**: 17+
- **Maven**: 3.6+

## CI/CD Integration

### Example GitHub Actions
```yaml
- name: Run tests
  run: mvn clean test -Denv=qa

- name: Generate Allure report
  run: mvn allure:report
```

### Example Jenkins
```groovy
sh 'mvn clean test -Denv=${ENVIRONMENT}'
allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
```

## Troubleshooting

### Tests fail to connect
- Verify `app.baseUrl` is correct
- Check network connectivity
- Ensure API is running

### Configuration not loading
- Check environment name matches folder name
- Verify config.properties exists
- Check system property syntax: `-Denv=qa`

### Parallel execution issues
- Reduce thread count: `-Dtest.thread.count=1`
- Check for test data conflicts
- Review cleanup in @AfterMethod

## Contributing

1. Follow existing code style
2. Add tests for new features
3. Update documentation
4. Use meaningful commit messages

## Author

QA Automation Framework

## License

This is a test framework for assignment purposes.

