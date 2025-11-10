# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that demonstrates Spring AI Tool Calling integration with Ollama. The application provides a chat API that can automatically invoke various tools to answer user questions about date/time, mathematical calculations, and system information.

## Key Architecture

### Core Components
- **ChatClient Configuration** (`src/main/java/dev/jackelyj/spring_agent/config/ChatClientConfig.java`): Configures Spring AI ChatClient with Ollama model and tool integration
- **Tool System**: Three tool categories provide automated functionality:
  - `DateTimeTools`: Date/time calculations and formatting
  - `CalculatorTools`: Mathematical operations and unit conversions
  - `SystemInfoTools`: JVM and system monitoring
- **Conversation Memory**: Maintains chat history using Spring AI's `MessageWindowChatMemory`
- **REST API**: `/api/v1/chat` endpoints for both regular and streaming conversations

### Package Structure
```
dev.jackelyj.spring_agent/
├── config/          # ChatClient and AI configuration
├── controller/      # REST endpoints
├── service/         # Business logic interfaces and implementations
├── dto/            # Data transfer objects
├── tools/          # AI tool implementations with @Tool annotations
└── demo/           # Demonstration classes
```

## Common Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests ToolCallingIntegrationTest
```

### Testing Tools
Use the provided demo script to test tool functionality:
```bash
./demo.sh
```

Or use the HTTP request file `request.http` for manual API testing.

### Prerequisites
- **Ollama service** must be running on `http://localhost:11434`
- Default model configured: `gpt-oss` (can be changed in `application.yml`)

## Development Guidelines

### Adding New Tools
1. Create a new component class in `src/main/java/dev/jackelyj/spring_agent/tools/`
2. Use `@Tool` annotations for methods that should be available to the AI
3. Add descriptive parameters with `@ToolParam` annotations
4. Register the tool in `ChatClientConfig.toolObjects()` method
5. Update tests in `ToolCallingIntegrationTest`

### Tool Best Practices
- Provide clear, descriptive tool descriptions in `@Tool` annotations
- Include detailed parameter descriptions in `@ToolParam` annotations
- Implement proper error handling and return user-friendly error messages
- Use consistent return value formats across tools

### Configuration Changes
- Model settings: `src/main/resources/application.yml`
- Tool registration: `src/main/java/dev/jackelyj/spring_agent/config/ChatClientConfig.java`
- Conversation memory: Configured with 10-message window by default

## API Endpoints

### Chat Operations
- `POST /api/v1/chat` - Standard chat with tool calling
- `POST /api/v1/chat/stream` - Streaming chat responses
- `GET /api/v1/chat` - Legacy GET endpoint (compatibility)
- `GET /api/v1/chat/stream` - Legacy streaming endpoint

### Memory Management
- `DELETE /api/v1/chat/clear/{conversationId}` - Clear specific conversation
- `POST /api/v1/chat/clear-all` - Clear all conversation memories

### Health Check
- `GET /api/v1/health/service` - Service health status

## Key Technical Details

### Spring AI Integration
- Uses Spring AI 1.0.3 with Ollama integration
- Implements `MessageChatMemoryAdvisor` for conversation persistence
- Tools automatically discovered via `@Tool` method annotations
- Supports both synchronous and streaming chat responses

### Error Handling
- Global exception handling in controllers returns proper HTTP status codes
- Tool implementations should handle errors gracefully and return descriptive messages
- Service health checks monitor both chat and memory services

### Testing Strategy
- Unit tests for individual services and controllers
- Integration tests (`ToolCallingIntegrationTest`) verify end-to-end tool functionality
- Tests exclude integration tag by default in Gradle configuration

## Documentation
- `TOOL_CALLING_GUIDE.md` - Comprehensive tool calling integration guide
- `SPRING_AI_CHATCLIENT_GUIDE.md` - Detailed Spring AI ChatClient configuration guide
- `SPRING_AI_QUICK_REFERENCE.md` - Quick reference for Spring AI features

## PostgreSQL + pgvector Integration

### Overview
The application now supports persistent chat memory and vector document storage using PostgreSQL with pgvector extension. This enables:
- **Persistent Chat Memory**: JDBC-based conversation history storage
- **Vector Store**: Semantic document search using pgvector
- **Dual Mode**: Switchable between in-memory and PostgreSQL storage

### Architecture (SOLID Principles)

The integration follows SOLID principles for maintainability and extensibility:

#### Single Responsibility Principle (SRP)
- `ChatMemoryConfig`: Only creates ChatMemory beans
- `VectorStoreConfig`: Only creates VectorStore and EmbeddingModel beans
- `ChatClientConfig`: Only creates ChatClient beans
- Each service class has one clear responsibility

#### Open/Closed Principle (OCP)
- System is open for extension (new storage implementations) without modifying existing code
- Interface-based design allows adding new implementations

#### Liskov Substitution Principle (LSP)
- In-memory and JDBC ChatMemory implementations are interchangeable
- All implementations fully satisfy their interface contracts

#### Interface Segregation Principle (ISP)
- `DocumentService`: Fine-grained interface for document operations
- `ChatMemoryRepository`: Focused interface for memory operations
- Clients only depend on methods they actually use

#### Dependency Inversion Principle (DIP)
- High-level modules (Controllers, Services) depend on abstractions
- `ChatController` depends on `DocumentService` interface, not implementation
- `ConversationMemoryServiceImpl` depends on `ChatMemoryRepository` interface

### Component Structure

```
config/
├── ChatClientConfig.java       # ChatClient beans (SRP)
├── ChatMemoryConfig.java       # ChatMemory beans with conditional creation (SRP + LSP)
└── VectorStoreConfig.java      # VectorStore and EmbeddingModel beans (SRP)

service/
├── DocumentService.java        # Document management interface (ISP + DIP)
├── impl/
│   ├── DocumentServiceImpl.java    # Document service implementation (SRP)
│   └── ConversationMemoryServiceImpl.java  # Updated to use ChatMemoryRepository (DIP)

repository/
├── ChatMemoryRepository.java   # Chat memory abstraction (ISP + DIP)
└── impl/
    ├── JdbcChatMemoryRepositoryAdapter.java     # JDBC implementation (LSP)
    └── InMemoryChatMemoryRepository.java         # In-memory implementation (LSP)

dto/
├── DocumentRequest.java        # Document upload request
├── DocumentResponse.java       # Document operation response
├── DocumentSearchRequest.java  # Search parameters
└── DocumentSearchResult.java   # Search result with similarity score
```

### Docker Setup

#### Start PostgreSQL + pgvector
```bash
# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f postgres

# Stop services
docker-compose down
```

#### Connect to PostgreSQL
```bash
# Using psql
docker exec -it spring-agent-postgres psql -U postgres -d spring_ai_db

# Check tables
\dt

# Check pgvector extension
\dx
```

### Configuration Profiles

The application supports two modes via Spring profiles:

#### Memory Mode (Default)
```bash
# Run with in-memory storage
./gradlew bootRun

# Or explicitly
./gradlew bootRun --args='--spring.profiles.active=memory'
```

Configuration in `application.yml`:
```yaml
chat:
  memory:
    type: in-memory
```

#### PostgreSQL Mode
```bash
# Run with PostgreSQL + pgvector
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

Configuration in `application.yml`:
```yaml
spring:
  profiles:
    active: postgres
  datasource:
    url: jdbc:postgresql://localhost:5432/spring_ai_db
    username: postgres
    password: postgres

chat:
  memory:
    type: jdbc
```

### API Endpoints

#### Document Management
- `POST /api/v1/documents` - Upload documents to vector store
- `POST /api/v1/documents/search` - Semantic search with optional filters
- `DELETE /api/v1/documents` - Delete documents by IDs

#### Conversation Management
- `GET /api/v1/conversations` - List all conversation IDs
- `GET /api/v1/conversations/{id}/count` - Get message count for conversation
- `DELETE /api/v1/chat/clear/{conversationId}` - Clear specific conversation
- `POST /api/v1/chat/clear-all` - Clear all conversations

### Usage Examples

#### Upload Documents
```bash
curl -X POST http://localhost:8080/api/v1/documents \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "Spring AI is a framework for building AI applications.",
      "PostgreSQL is a powerful open-source database."
    ],
    "metadata": {
      "source": "manual",
      "category": "tech"
    }
  }'
```

#### Search Documents
```bash
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "database",
    "topK": 5,
    "threshold": 0.0
  }'
```

#### Search with Filter
```bash
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Spring",
    "topK": 10,
    "filterExpression": "category == '\''tech'\''",
    "threshold": 0.5
  }'
```

### Embedding Model

The application uses Ollama's `nomic-embed-text` model for generating embeddings:
- **Dimensions**: 1024
- **Model**: nomic-embed-text
- **Distance**: Cosine similarity

Make sure to pull the embedding model:
```bash
ollama pull nomic-embed-text
```

### Database Schema

#### Vector Store Table
Created automatically by Spring AI:
```sql
CREATE TABLE vector_store (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    content text,
    metadata json,
    embedding vector(1024)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
```

#### Chat Memory Table
Created automatically by Spring AI JDBC repository:
```sql
CREATE TABLE ai_chat_memory (
    conversation_id varchar(255),
    message_type varchar(50),
    content text,
    metadata json,
    created_at timestamp
);
```

### Migration Guide

#### From In-Memory to PostgreSQL

1. **Start PostgreSQL**:
   ```bash
   docker-compose up -d
   ```

2. **Update Configuration**:
   Change `application.yml` or use profile:
   ```yaml
   spring:
     profiles:
       active: postgres
   ```

3. **Restart Application**:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=postgres'
   ```

4. **Verify**:
   ```bash
   curl http://localhost:8080/api/v1/health/service
   ```

### Troubleshooting

#### PostgreSQL Connection Issues
```bash
# Check if PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Verify connection
docker exec -it spring-agent-postgres pg_isready -U postgres
```

#### pgvector Extension Not Found
```bash
# Connect to database
docker exec -it spring-agent-postgres psql -U postgres -d spring_ai_db

# Create extension
CREATE EXTENSION IF NOT EXISTS vector;
\dx  # List extensions
```

#### Embedding Model Issues
```bash
# Pull the model
ollama pull nomic-embed-text

# List available models
ollama list

# Test the model
ollama run nomic-embed-text "test"
```

### Performance Considerations

#### Vector Index
The HNSW index provides fast approximate nearest neighbor search:
- **Build Time**: Increases with dataset size
- **Query Time**: Fast, typically sub-second for millions of vectors
- **Memory**: Requires RAM proportional to dataset size

#### Connection Pooling
HikariCP is configured by default:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

#### Batch Processing
Documents are processed in batches:
```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        max-document-batch-size: 10000
```

### Testing

#### Run Integration Tests
```bash
# Run all tests including integration
./gradlew test

# Run specific integration test
./gradlew test --tests VectorStoreIntegrationTest
./gradlew test --tests PostgresChatMemoryIntegrationTest
```

#### Manual Testing
Use the provided demo script:
```bash
./demo.sh
```

Or the HTTP request file: `request.http`