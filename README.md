# Spring Agent - AI Chat Application with Tool Calling

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot application that demonstrates Spring AI Tool Calling integration with Ollama, providing an intelligent chat assistant that can automatically invoke various tools to answer user questions.

## 🚀 Features

### 🤖 AI-Powered Chat
- **Intelligent Conversation**: Natural language chat powered by Ollama models
- **Tool Calling**: AI automatically uses appropriate tools based on user queries
- **Memory Management**: Maintains conversation context across multiple interactions
- **Streaming Support**: Real-time streaming responses for better user experience

### 🛠️ Available Tools

#### 📅 DateTime Tools
- Get current date and time in any timezone
- Calculate days between dates
- Date arithmetic (add/subtract days)
- Format dates and times
- Leap year calculations
- Timestamp conversions

#### 🔢 Calculator Tools
- Basic arithmetic operations (+, -, ×, ÷)
- Advanced mathematical functions (power, square root, logarithms)
- Trigonometric functions (sin, cos, tan)
- Statistical calculations (average, sum, min, max)
- Unit conversions (length, temperature)
- Percentage and factorial calculations

#### 💻 System Information Tools
- System health monitoring
- JVM memory usage statistics
- Operating system information
- Java runtime details
- Environment variables access
- System properties inspection
- Disk usage information
- CPU and thread information

### 🌐 REST API
- RESTful chat endpoints
- JSON request/response format
- Cross-origin support (CORS)
- Health check endpoints
- Conversation memory management

### 🔌 Model Context Protocol (MCP) Support

Spring Agent now supports MCP, enabling seamless integration with AI assistants and applications:

#### MCP Server Features
- **34 Tools Exposed**: All DateTime, Calculator, and System Info tools available via MCP
- **Dual Transport**: Stdio (for AI IDEs) and SSE/HTTP (for remote access)
- **Auto-Discovery**: Tools automatically registered and exposed
- **Standards Compliant**: Full MCP specification implementation via Spring AI

#### MCP Client Features
- **External Tool Access**: Connect to and use external MCP servers
- **Multi-Server Support**: Manage multiple MCP connections simultaneously
- **Resource Reading**: Access files and resources from external servers
- **Health Monitoring**: Built-in connection health checks

#### Quick MCP Setup

**As MCP Server (for AI IDEs like Claude/Cursor):**
```bash
# Start in Stdio mode
./scripts/mcp-server-stdio.sh
```

**Configure in Claude Desktop:**
```json
{
  "mcpServers": {
    "spring-agent": {
      "command": "/path/to/spring-agent/scripts/mcp-server-stdio.sh"
    }
  }
}
```

**As MCP Server (for remote access):**
```bash
# Start in SSE mode
./scripts/mcp-server-sse.sh
# Access at: http://localhost:8080/mcp/message
```

**📖 Full MCP Documentation**: See [MCP Integration Guide](docs/MCP_INTEGRATION_GUIDE.md)

## 📋 Prerequisites

- **Java 21** or higher
- **Ollama** service running locally
- **Gradle** 8.0+ (for building)

## 🛠️ Installation

### 1. Install and Start Ollama

```bash
# Install Ollama (macOS)
brew install ollama

# Pull required models
ollama pull gpt-oss           # Chat model
ollama pull nomic-embed-text  # Embedding model (for vector store)

# Start Ollama service
ollama serve
```

### 2. Clone and Build the Application

```bash
# Clone the repository
git clone <repository-url>
cd spring-agent

# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### 3. Using Docker (Optional)

```bash
# Start PostgreSQL + pgvector
docker-compose up -d

# Run with PostgreSQL profile
./gradlew bootRun --args='--spring.profiles.active=postgres'
```


## 🎯 Quick Start

### Using the Demo Script

Run the included demo script to test all tool functionalities:

```bash
./demo.sh
```

This script will automatically:
- Verify the application is running
- Test all tool categories with sample queries
- Display formatted results

### Manual API Testing

#### Basic Chat
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What time is it now?",
    "conversationId": "user123"
  }'
```

#### Streaming Chat
```bash
curl -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Calculate 25 × 17",
    "conversationId": "user123"
  }'
```

#### With Custom System Prompt
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Help me with a math problem",
    "conversationId": "user123",
    "systemPrompt": "You are a mathematics tutor. Use calculator tools to show your work.",
    "enableTools": true
  }'
```

## 📚 Usage Examples

### Time and Date Queries
```
User: What time is it in Tokyo?
AI: [Automatically calls timezone tool] The current time in Tokyo is 14:30:25.

User: How many days until Christmas?
AI: [Automatically calculates date difference] There are 45 days until Christmas.

User: What day of the week was January 1, 2000?
AI: [Automatically calls date formatting tool] January 1, 2000 was a Saturday.
```

### Mathematical Calculations
```
User: What is the square root of 144?
AI: [Automatically calls calculator tool] √144 = 12.

User: Calculate the area of a circle with radius 5
AI: [Automatically calls calculator tool] Area = π × 5² = 78.54 square units.

User: Convert 100 kilometers to miles
AI: [Automatically calls unit conversion tool] 100 km = 62.14 miles.
```

### System Information
```
User: Check system health
AI: [Automatically calls system monitoring tools] System Health Status:
- Memory usage: 45.2%
- CPU load: 2.1
- Available processors: 8
- Overall status: Healthy

User: How much JVM memory is being used?
AI: [Automatically calls memory monitoring tool] JVM Memory Usage:
- Heap memory: 512.00 MB used / 1024.00 MB max (50.00%)
- Non-heap memory: 128.50 MB used
```

## 🔧 Configuration

### Application Configuration

Edit `src/main/resources/application.yml` to customize:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434  # Ollama service URL
      chat:
        model: gpt-oss                  # Model name
        options:
          temperature: 0.7              # Response randomness (0-1)
          max-tokens: 1000             # Maximum response length
          top-p: 0.9                   # Nucleus sampling

server:
  port: 8080                          # Application port

logging:
  level:
    "[org.springframework.ai]": DEBUG    # Spring AI logging
    "[dev.jackelyj.spring_agent]": INFO   # Application logging
```

### Tool Configuration

Tools are automatically registered in `ChatClientConfig.java`. To add new tools:

1. Create a new component class with `@Tool` annotated methods
2. Add the component to the `toolObjects()` bean method
3. Restart the application

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Integration Tests
```bash
./gradlew test --tests ToolCallingIntegrationTest
```

### Test Coverage
The test suite includes:
- Unit tests for individual services and controllers
- Integration tests for tool calling functionality
- Streaming response tests
- Error handling validation
- Conversation memory tests

## 📖 API Documentation

### Chat Endpoints

#### POST /api/v1/chat
Send a chat message and receive a response.

**Request Body:**
```json
{
  "message": "Your message here",
  "conversationId": "optional-conversation-id",
  "systemPrompt": "Optional custom system prompt",
  "enableTools": true,
  "allowedToolNames": ["tool1", "tool2"]
}
```

**Response:**
```json
{
  "response": "AI response text",
  "conversationId": "conversation-id",
  "timestamp": "2024-01-15T14:30:25.123Z",
  "streaming": false,
  "toolsUsed": true,
  "toolsInvoked": ["getCurrentDateTime"],
  "toolResults": {"getCurrentDateTime": "2024-01-15T14:30:25"}
}
```

#### POST /api/v1/chat/stream
Stream chat responses for real-time interaction.

Same request format as `/chat`, but responses are streamed as Server-Sent Events.

### Memory Management Endpoints

#### DELETE /api/v1/chat/clear/{conversationId}
Clear memory for a specific conversation.

#### POST /api/v1/chat/clear-all
Clear all conversation memories.

### Health Endpoints

#### GET /api/v1/health/service
Check the health status of all services.

**Response:**
```json
{
  "chatService": true,
  "conversationMemoryService": true,
  "overall": true
}
```

#### GET /health/mcp
Check MCP Server and Client health status.

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-01-15T10:30:00",
  "mcpServer": {
    "enabled": true,
    "name": "spring-agent-tools",
    "version": "1.0.0",
    "status": "ACTIVE",
    "toolsCount": 34,
    "toolCategories": {
      "dateTime": 11,
      "calculator": 13,
      "systemInfo": 10
    },
    "transports": {
      "sse": {
        "enabled": true,
        "endpoint": "/mcp/message"
      },
      "stdio": {
        "enabled": true,
        "profile": "mcp-stdio"
      }
    }
  },
  "mcpClient": {
    "enabled": false,
    "status": "DISABLED"
  }
}
```

## 🏗️ Project Structure

```
src/main/java/dev/jackelyj/spring_agent/
├── SpringAgentApplication.java     # Main application class
├── config/
│   ├── ChatClientConfig.java       # AI and ChatClient configuration
│   ├── ChatMemoryConfig.java       # Chat memory configuration
│   └── VectorStoreConfig.java      # Vector store configuration
├── controller/
│   ├── ChatController.java         # REST API endpoints
│   ├── HealthController.java       # Health check endpoints (with MCP)
│   └── DocumentController.java     # Document management endpoints
├── dto/
│   ├── ChatRequest.java           # Chat request DTO
│   └── ChatResponse.java          # Chat response DTO
├── mcp/                            # MCP Integration (NEW)
│   ├── service/
│   │   └── McpClientService.java  # MCP client service
│   └── controller/
│       └── McpDemoController.java # MCP demo endpoints
├── service/
│   ├── ChatService.java           # Chat service interface
│   ├── ConversationMemoryService.java  # Memory service interface
│   ├── DocumentService.java       # Document service interface
│   └── impl/                      # Service implementations
├── tools/
│   ├── CalculatorTools.java       # Mathematical operations (MCP-exposed)
│   ├── DateTimeTools.java         # Date/time operations (MCP-exposed)
│   └── SystemInfoTools.java       # System monitoring (MCP-exposed)
└── demo/
    └── ToolCallingDemo.java       # Demonstration class

scripts/                            # Startup scripts (NEW)
├── mcp-server-stdio.sh            # MCP Server Stdio mode
└── mcp-server-sse.sh              # MCP Server SSE/HTTP mode

docs/                               # Documentation
├── MCP_INTEGRATION_GUIDE.md       # MCP integration guide (NEW)
├── TOOL_CALLING_GUIDE.md          # Tool calling guide
└── POSTGRESQL_CHATMEMORY_INTEGRATION.md  # PostgreSQL guide
```

## 🔍 Development

### Adding New Tools

1. **Create Tool Class:**
```java
@Component
public class MyCustomTools {

    @Tool(description = "Clear description of what this tool does")
    public String myFunction(
            @ToolParam(description = "Description of parameter") String param) {
        // Implementation logic
        return "Result";
    }
}
```

2. **Register Tool:**
Add to `ChatClientConfig.toolObjects()` method:
```java
@Bean
public Object[] toolObjects(DateTimeTools dateTimeTools,
                           CalculatorTools calculatorTools,
                           SystemInfoTools systemInfoTools,
                           MyCustomTools myCustomTools) {
    return new Object[]{dateTimeTools, calculatorTools, systemInfoTools, myCustomTools};
}
```

3. **Test Integration:**
Add tests in `ToolCallingIntegrationTest.java`.

### Best Practices

- **Clear Descriptions**: Provide detailed, accurate descriptions for tools and parameters
- **Error Handling**: Implement robust error handling with user-friendly messages
- **Consistent Formatting**: Maintain consistent return value formats
- **Performance**: Consider caching for frequently called tools
- **Security**: Validate inputs and avoid exposing sensitive system information

## 📚 Documentation

- [TOOL_CALLING_GUIDE.md](docs/TOOL_CALLING_GUIDE.md) - Comprehensive tool calling integration guide
- [SPRING_AI_CHATCLIENT_GUIDE.md](docs/SPRING_AI_CHATCLIENT_GUIDE.md) - Detailed Spring AI ChatClient configuration
- [SPRING_AI_QUICK_REFERENCE.md](docs/SPRING_AI_QUICK_REFERENCE.md) - Quick reference for Spring AI features
- [POSTGRESQL_CHATMEMORY_INTEGRATION.md](docs/POSTGRESQL_CHATMEMORY_INTEGRATION.md) - PostgreSQL + pgvector integration guide
- [INTEGRATION_TESTS.md](docs/INTEGRATION_TESTS.md) - Integration testing guide
- [GITHUB_ACTIONS_GUIDE.md](docs/GITHUB_ACTIONS_GUIDE.md) - CI/CD configuration and usage guide
- [CLAUDE.md](CLAUDE.md) - Development guidelines for Claude Code

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Projects

- [Spring AI](https://spring.io/projects/spring-ai) - Spring's AI integration framework
- [Ollama](https://ollama.com/) - Get up and running with large language models locally
- [Spring Boot](https://spring.io/projects/spring-boot) - Java-based application framework

## 🙏 Acknowledgments

- Spring AI team for the excellent integration framework
- Ollama project for making local LLM deployment easy
- Spring Boot community for the robust application framework