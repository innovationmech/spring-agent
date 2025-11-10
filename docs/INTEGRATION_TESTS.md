# Integration Tests Guide

This document explains how to run integration tests for PostgreSQL + pgvector functionality.

## Prerequisites

### 1. Start PostgreSQL + pgvector
```bash
docker-compose up -d
```

Verify it's running:
```bash
docker-compose ps
```

### 2. Pull Ollama Embedding Model
```bash
ollama pull nomic-embed-text
```

### 3. Verify Ollama is Running
```bash
curl http://localhost:11434/api/tags
```

## Running Integration Tests

### Enable Integration Tests
Set the environment variable:

```bash
export ENABLE_INTEGRATION_TESTS=true
```

### Run All Integration Tests
```bash
./gradlew test --tests *IntegrationTest
```

### Run Specific Test Classes

#### Vector Store Tests
```bash
./gradlew test --tests VectorStoreIntegrationTest
```

#### Chat Memory Tests
```bash
./gradlew test --tests PostgresChatMemoryIntegrationTest
```

### Run All Tests (including integration)
```bash
./gradlew test
```

## Test Coverage

### VectorStoreIntegrationTest
Tests the following functionality:
- ✅ Adding documents to vector store
- ✅ Semantic similarity search
- ✅ Search with metadata filters
- ✅ Deleting documents
- ✅ Similarity score calculation

### PostgresChatMemoryIntegrationTest
Tests the following functionality:
- ✅ Adding and retrieving messages
- ✅ Message persistence across sessions
- ✅ Getting message count
- ✅ Listing all conversation IDs
- ✅ Exporting conversation history
- ✅ Clearing specific conversations
- ✅ Clearing all conversations
- ✅ Conversation isolation

## Troubleshooting

### PostgreSQL Connection Failed
```bash
# Check if PostgreSQL is running
docker-compose ps

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### pgvector Extension Not Found
```bash
# Connect to database
docker exec -it spring-agent-postgres psql -U postgres -d spring_ai_db

# Create extension
CREATE EXTENSION IF NOT EXISTS vector;
\dx  # List extensions
\q   # Quit
```

### Ollama Model Not Found
```bash
# List available models
ollama list

# Pull the embedding model
ollama pull nomic-embed-text

# Verify the model works
ollama run nomic-embed-text "test embedding"
```

### Test Failures
1. **Check application.yml profile**: Ensure `postgres` profile is active for tests
2. **Database connectivity**: Verify PostgreSQL is accessible on `localhost:5432`
3. **Clean state**: Run `docker-compose down -v && docker-compose up -d` to reset

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: pgvector/pgvector:pg16
        env:
          POSTGRES_PASSWORD: postgres
          POSTGRES_DB: spring_ai_db
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Install Ollama
        run: |
          curl -fsSL https://ollama.com/install.sh | sh
          ollama pull nomic-embed-text
      
      - name: Run Integration Tests
        env:
          ENABLE_INTEGRATION_TESTS: true
        run: ./gradlew test --tests *IntegrationTest
```

## Manual Testing

After successful integration test runs, you can also manually test the APIs:

```bash
# Start the application with postgres profile
./gradlew bootRun --args='--spring.profiles.active=postgres'

# Run the demo script
./demo.sh

# Or use the HTTP request file
# Open request.http in your IDE and execute requests
```

## Cleaning Up

### Stop PostgreSQL
```bash
docker-compose down
```

### Remove volumes (clean slate)
```bash
docker-compose down -v
```

### Unset environment variable
```bash
unset ENABLE_INTEGRATION_TESTS
```

