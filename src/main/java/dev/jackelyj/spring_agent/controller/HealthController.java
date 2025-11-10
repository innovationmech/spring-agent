package dev.jackelyj.spring_agent.controller;

import dev.jackelyj.spring_agent.mcp.service.McpClientService;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private OllamaChatModel ollamaChatModel;
    
    @Autowired(required = false)
    private McpClientService mcpClientService;
    
    @Value("${spring.ai.mcp.server.enabled:false}")
    private boolean mcpServerEnabled;
    
    @Value("${spring.ai.mcp.client.enabled:false}")
    private boolean mcpClientEnabled;
    
    @Value("${spring.ai.mcp.server.name:spring-agent-tools}")
    private String mcpServerName;
    
    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String mcpServerVersion;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "spring-agent");
        response.put("version", "1.0.0");

        try {
            // 测试 Ollama 连接
            ollamaChatModel.call(
                new org.springframework.ai.chat.prompt.Prompt("Hello")
            );

            response.put("ollama", "CONNECTED");
            response.put("model", "gpt-oss");
        } catch (Exception e) {
            response.put("ollama", "DISCONNECTED");
            response.put("error", e.getMessage());
            response.put("status", "DOWN");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Spring Agent with Ollama and MCP");
        info.put("version", "1.0.0");
        info.put("description", "Spring Boot application with Spring AI 1.0.3, Ollama, and Model Context Protocol integration");
        info.put("endpoints", Map.of(
            "chat", "POST /api/v1/chat - Regular chat",
            "stream", "POST /api/v1/chat/stream - Streaming chat",
            "health", "GET /health - Health check",
            "mcp-health", "GET /health/mcp - MCP Server/Client health check",
            "clear", "DELETE /api/v1/chat/clear/{conversationId} - Clear conversation",
            "mcp-tools", "GET /api/v1/mcp/tools - List external MCP tools (if client enabled)"
        ));
        info.put("timestamp", LocalDateTime.now());
        return info;
    }
    
    /**
     * MCP Server and Client health check endpoint.
     * Returns information about MCP server status, available tools, and client connections.
     */
    @GetMapping("/health/mcp")
    public ResponseEntity<Map<String, Object>> mcpHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        
        // MCP Server Status
        Map<String, Object> serverStatus = new HashMap<>();
        serverStatus.put("enabled", mcpServerEnabled);
        serverStatus.put("name", mcpServerName);
        serverStatus.put("version", mcpServerVersion);
        
        if (mcpServerEnabled) {
            // Count available tools
            int dateTimeToolsCount = 11;  // DateTimeTools has 11 @Tool methods
            int calculatorToolsCount = 13; // CalculatorTools has 13 @Tool methods
            int systemInfoToolsCount = 10; // SystemInfoTools has 10 @Tool methods
            int totalTools = dateTimeToolsCount + calculatorToolsCount + systemInfoToolsCount;
            
            serverStatus.put("status", "ACTIVE");
            serverStatus.put("toolsCount", totalTools);
            serverStatus.put("toolCategories", Map.of(
                "dateTime", dateTimeToolsCount,
                "calculator", calculatorToolsCount,
                "systemInfo", systemInfoToolsCount
            ));
            serverStatus.put("transports", Map.of(
                "sse", Map.of(
                    "enabled", true,
                    "endpoint", "/mcp/message"
                ),
                "stdio", Map.of(
                    "enabled", true,
                    "profile", "mcp-stdio"
                )
            ));
        } else {
            serverStatus.put("status", "DISABLED");
        }
        
        response.put("mcpServer", serverStatus);
        
        // MCP Client Status
        Map<String, Object> clientStatus = new HashMap<>();
        clientStatus.put("enabled", mcpClientEnabled);
        
        if (mcpClientEnabled && mcpClientService != null) {
            try {
                // Check if client is healthy
                Boolean isHealthy = mcpClientService.isHealthy().block();
                clientStatus.put("status", isHealthy ? "CONNECTED" : "DISCONNECTED");
                
                // Try to get connected servers count (if available)
                clientStatus.put("healthCheck", isHealthy ? "PASSED" : "FAILED");
            } catch (Exception e) {
                clientStatus.put("status", "ERROR");
                clientStatus.put("error", e.getMessage());
            }
        } else if (mcpClientEnabled) {
            clientStatus.put("status", "INITIALIZING");
        } else {
            clientStatus.put("status", "DISABLED");
        }
        
        response.put("mcpClient", clientStatus);
        
        // Overall status
        if (mcpServerEnabled || mcpClientEnabled) {
            response.put("status", "UP");
        } else {
            response.put("status", "INACTIVE");
            response.put("message", "Both MCP Server and Client are disabled");
        }
        
        return ResponseEntity.ok(response);
    }
}
