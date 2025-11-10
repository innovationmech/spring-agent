package dev.jackelyj.spring_agent;

import dev.jackelyj.spring_agent.mcp.service.McpClientService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for MCP Client functionality.
 * 
 * This test is disabled by default because it requires an external MCP server to be running.
 * Enable it only when testing with a real MCP server.
 * 
 * Prerequisites:
 * 1. Start an external MCP server (e.g., filesystem MCP server)
 * 2. Update the configuration to point to your test server
 * 3. Remove the @Disabled annotation
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.mcp.server.enabled=false",
    "spring.ai.mcp.client.enabled=false",  // Change to true when testing with real server
    "spring.profiles.active=memory"
})
@Tag("integration")
@Disabled("Requires external MCP server - enable manually for testing")
public class McpClientIntegrationTest {

    @Autowired(required = false)
    private McpClientService mcpClientService;

    @Test
    public void testMcpClientServiceNotLoadedWhenDisabled() {
        // When MCP client is disabled, the service should not be loaded
        assertThat(mcpClientService).isNull();
    }

    // The following tests require MCP client to be enabled and configured
    // Uncomment and configure when testing with a real MCP server

    /*
    @Test
    public void testConnectToExternalServer() {
        assertThat(mcpClientService).isNotNull();
        
        Boolean isHealthy = mcpClientService.isHealthy().block();
        assertThat(isHealthy).isNotNull();
    }

    @Test
    public void testListExternalTools() {
        assertThat(mcpClientService).isNotNull();
        
        List<McpSchema.Tool> tools = mcpClientService.listAvailableTools().block();
        assertThat(tools).isNotNull();
        assertThat(tools).isNotEmpty();
        
        System.out.println("Found " + tools.size() + " tools from external server:");
        tools.forEach(tool -> 
            System.out.println("  - " + tool.name() + ": " + tool.description())
        );
    }

    @Test
    public void testCallExternalTool() {
        assertThat(mcpClientService).isNotNull();
        
        // Example: Call a tool (adjust tool name and args for your server)
        McpSchema.CallToolResult result = mcpClientService.callTool(
            "example_tool",
            Map.of("param1", "value1")
        ).block();
        
        assertThat(result).isNotNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.content()).isNotEmpty();
    }

    @Test
    public void testListExternalResources() {
        assertThat(mcpClientService).isNotNull();
        
        List<McpSchema.Resource> resources = mcpClientService.listAvailableResources().block();
        assertThat(resources).isNotNull();
        
        System.out.println("Found " + resources.size() + " resources from external server:");
        resources.forEach(resource -> 
            System.out.println("  - " + resource.uri() + ": " + resource.name())
        );
    }

    @Test
    public void testReadExternalResource() {
        assertThat(mcpClientService).isNotNull();
        
        // Example: Read a resource (adjust URI for your server)
        List<McpSchema.ResourceContents> contents = mcpClientService.readResource(
            "file:///example.txt"
        ).block();
        
        assertThat(contents).isNotNull();
        assertThat(contents).isNotEmpty();
    }

    @Test
    public void testGetToolInfo() {
        assertThat(mcpClientService).isNotNull();
        
        // Example: Get info about a specific tool
        McpSchema.Tool tool = mcpClientService.getToolInfo("example_tool").block();
        
        assertThat(tool).isNotNull();
        assertThat(tool.name()).isEqualTo("example_tool");
        assertThat(tool.description()).isNotEmpty();
    }
    */
}

