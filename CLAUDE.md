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