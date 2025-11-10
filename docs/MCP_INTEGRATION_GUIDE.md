# Spring Agent - MCP Integration Guide

## Overview

Spring Agent now supports the Model Context Protocol (MCP), enabling seamless integration with AI assistants and applications. This guide covers both MCP Server (exposing tools) and MCP Client (consuming external tools) functionalities.

## What is MCP?

The Model Context Protocol (MCP) is an open protocol that standardizes how applications provide context to Large Language Models (LLMs). It enables AI applications to:

- **Discover Tools**: Dynamically list available tools and their capabilities
- **Execute Tools**: Invoke tools with structured arguments and receive results
- **Access Resources**: Read and interact with various data sources
- **Provide Context**: Supply rich context to AI models

## Spring Agent MCP Capabilities

### MCP Server Features

Spring Agent exposes **34 tools** across 3 categories as an MCP server:

#### 1. DateTime Tools (11 tools)
- `getCurrentDateTime` - Get current date and time
- `getCurrentDate` - Get current date
- `getCurrentTime` - Get current time
- `getCurrentTimeInTimeZone` - Get time in specific timezone
- `calculateDaysBetween` - Calculate days between two dates
- `addDaysToDate` - Add/subtract days from a date
- `formatDateTime` - Format date/time with custom pattern
- `getCurrentTimestamp` - Get Unix timestamp
- `timestampToDateTime` - Convert timestamp to datetime
- `isLeapYear` - Check if year is leap year
- `getDaysInMonth` - Get number of days in month

#### 2. Calculator Tools (13 tools)
- `calculate` - Basic arithmetic (+, -, *, /)
- `power` - Calculate power/exponent
- `squareRoot` - Calculate square root
- `logarithm` - Calculate logarithm
- `trigonometric` - Calculate sin/cos/tan
- `average` - Calculate average of numbers
- `sum` - Calculate sum of numbers
- `minMax` - Find min and max values
- `factorial` - Calculate factorial
- `percentage` - Calculate percentage
- `convertLength` - Convert length units
- `convertTemperature` - Convert temperature units

#### 3. System Info Tools (10 tools)
- `getSystemHealth` - Get overall system health
- `getMemoryUsage` - Get JVM memory statistics
- `getSystemInfo` - Get OS information
- `getJavaRuntimeInfo` - Get Java/JVM details
- `getEnvironmentVariables` - Get environment variables
- `getSystemProperties` - Get system properties
- `runGarbageCollection` - Trigger GC
- `getDiskUsage` - Get disk usage statistics
- `getCpuInfo` - Get CPU information
- `getThreadInfo` - Get thread statistics

### MCP Client Features

Spring Agent can also act as an MCP client to consume tools from external MCP servers:

- Connect to multiple MCP servers simultaneously
- List and discover external tools
- Execute external tools with type-safe arguments
- Read resources from external servers
- Health monitoring and connection management

## Configuration

### MCP Server Configuration

Add to `application.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: spring-agent-tools
        version: 1.0.0
        type: ASYNC
        sse-message-endpoint: /mcp/message
```

### MCP Client Configuration

To connect to external MCP servers, add:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        type: ASYNC
        servers:
          - name: filesystem
            transport: stdio
            command: npx
            args:
              - -y
              - "@modelcontextprotocol/server-filesystem"
              - "/path/to/directory"
```

## Usage

### Running as MCP Server

#### Stdio Mode (For AI IDEs)

Stdio mode is ideal for integration with AI IDEs like Claude Desktop or Cursor:

```bash
# Using the provided script
./scripts/mcp-server-stdio.sh

# Or directly
java -jar build/libs/spring-agent-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=mcp-stdio
```

**Client Configuration (Claude Desktop example):**

```json
{
  "mcpServers": {
    "spring-agent-tools": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/spring-agent/build/libs/spring-agent-0.0.1-SNAPSHOT.jar",
        "--spring.profiles.active=mcp-stdio"
      ]
    }
  }
}
```

#### SSE/HTTP Mode (For Remote Access)

SSE mode provides HTTP-based access to MCP tools:

```bash
# Using the provided script
./scripts/mcp-server-sse.sh

# Or directly
java -jar build/libs/spring-agent-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=default \
  --spring.ai.mcp.server.enabled=true
```

**Client Configuration:**

```json
{
  "mcpServers": {
    "spring-agent-tools": {
      "url": "http://localhost:8080/mcp/message",
      "transport": "sse"
    }
  }
}
```

### Using MCP Client

When MCP client is enabled, you can interact with external MCP servers:

```java
@Autowired
private McpClientService mcpClientService;

// List available tools
Mono<List<Tool>> tools = mcpClientService.listAvailableTools();

// Call a tool
Mono<CallToolResult> result = mcpClientService.callTool(
    "tool_name",
    Map.of("arg1", "value1", "arg2", "value2")
);

// List resources
Mono<List<Resource>> resources = mcpClientService.listAvailableResources();

// Read resource
Mono<List<ResourceContents>> contents = mcpClientService.readResource(
    "file:///path/to/file"
);
```

### REST API for MCP Client

When MCP client is enabled, additional REST endpoints are available:

```bash
# List external tools
GET /api/v1/mcp/tools

# Get tool info
GET /api/v1/mcp/tools/{toolName}

# Call external tool
POST /api/v1/mcp/tools/call
{
  "toolName": "example_tool",
  "arguments": {
    "param1": "value1"
  }
}

# List external resources
GET /api/v1/mcp/resources

# Read external resource
GET /api/v1/mcp/resources/read?resourceUri=file:///path
```

## Health Monitoring

### MCP Health Check Endpoint

```bash
GET /health/mcp
```

Response example:

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

## Transport Modes Comparison

| Feature | Stdio Mode | SSE/HTTP Mode |
|---------|-----------|---------------|
| **Use Case** | Local AI IDEs (Claude, Cursor) | Remote access, Web integration |
| **Performance** | Very fast (process communication) | Network-dependent |
| **Setup** | Simple (single process) | Requires web server |
| **Security** | Local only | Network-exposed |
| **Concurrency** | Single connection | Multiple concurrent clients |
| **Best For** | Development, desktop AI | Production, distributed systems |

## Integration Examples

### Claude Desktop

1. Locate Claude Desktop config:
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
   - Windows: `%APPDATA%/Claude/claude_desktop_config.json`

2. Add Spring Agent configuration:

```json
{
  "mcpServers": {
    "spring-agent": {
      "command": "/path/to/spring-agent/scripts/mcp-server-stdio.sh"
    }
  }
}
```

3. Restart Claude Desktop

4. Test by asking: "What's the current date and time?"

### Cursor IDE

1. Create/edit `~/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "spring-agent": {
      "command": "/path/to/spring-agent/scripts/mcp-server-stdio.sh",
      "type": "stdio"
    }
  }
}
```

2. Restart Cursor

3. The tools will be available in AI assistance

### Custom MCP Client

For custom integration, use the HTTP endpoint:

```javascript
// Example: List tools
fetch('http://localhost:8080/api/v1/mcp/tools')
  .then(res => res.json())
  .then(tools => console.log(tools));

// Example: Call tool
fetch('http://localhost:8080/api/v1/mcp/tools/call', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    toolName: 'getCurrentDateTime',
    arguments: {}
  })
})
  .then(res => res.json())
  .then(result => console.log(result));
```

## Troubleshooting

### MCP Server Issues

**Problem**: Server doesn't start in stdio mode

Solution:
- Check Java version (requires Java 21+)
- Verify JAR file exists: `./gradlew bootJar`
- Check Ollama is running: `curl http://localhost:11434`

**Problem**: Tools not discovered

Solution:
- Verify `@Tool` annotations on methods
- Check Spring component scanning
- Enable debug logging: `--logging.level.org.springframework.ai.mcp=DEBUG`

**Problem**: SSE endpoint not accessible

Solution:
- Verify server is running with correct profile
- Check firewall/network settings
- Confirm port 8080 is not in use

### MCP Client Issues

**Problem**: Cannot connect to external server

Solution:
- Verify external server is running
- Check command and arguments in configuration
- Test stdio command manually: `npx -y @modelcontextprotocol/server-filesystem /path`

**Problem**: Tools not listed from external server

Solution:
- Enable MCP client: `spring.ai.mcp.client.enabled=true`
- Check external server configuration
- Review client logs for connection errors

## Performance Considerations

### MCP Server

- **Stdio Mode**: Minimal overhead, direct process communication
- **SSE Mode**: Network latency, recommended for <1000 requests/minute
- **Tool Execution**: Tools are executed synchronously, consider timeout settings

### MCP Client

- **Connection Pooling**: Managed automatically by Spring AI
- **Timeout Settings**: Configure via `spring.ai.mcp.client.timeout`
- **Concurrent Requests**: Async client supports multiple simultaneous calls

## Security Considerations

### MCP Server

1. **Stdio Mode**: 
   - Runs as local process
   - Inherits permissions from parent
   - No network exposure

2. **SSE Mode**:
   - Exposed via HTTP
   - Consider authentication (not included by default)
   - Use HTTPS in production
   - Implement rate limiting

### MCP Client

1. **External Servers**:
   - Only connect to trusted MCP servers
   - Validate tool outputs before use
   - Sanitize file paths and resources

2. **Resource Access**:
   - Limit accessible directories
   - Validate URIs
   - Implement access controls

## Best Practices

1. **Development**: Use stdio mode for rapid testing
2. **Production**: Use SSE mode with proper authentication
3. **Monitoring**: Check `/health/mcp` endpoint regularly
4. **Logging**: Enable DEBUG for troubleshooting, INFO for production
5. **Resource Management**: Monitor memory usage with system tools
6. **Error Handling**: All tools return user-friendly error messages
7. **Documentation**: Tools are self-documenting via `@Tool` annotations

## Advanced Configuration

### Custom Tool Development

Add new tools by creating components with `@Tool` annotations:

```java
@Component
public class CustomTools {
    
    @Tool(description = "Custom tool description")
    public String myCustomTool(
        @ToolParam(description = "Parameter description") String param) {
        // Implementation
        return "Result";
    }
}
```

Tools are automatically discovered and exposed via MCP.

### Custom MCP Client Configuration

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        timeout: 30s
        servers:
          - name: server1
            transport: stdio
            command: command
            args: [arg1, arg2]
            env:
              ENV_VAR: value
          - name: server2
            transport: sse
            url: http://server2:8080/mcp
```

## References

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Spring Agent Project](../README.md)

## Support

For issues and questions:
1. Check this guide and [README.md](../README.md)
2. Review logs with DEBUG level
3. Test with `/health/mcp` endpoint
4. Check [integration tests](../src/test/java/dev/jackelyj/spring_agent/)

