package dev.jackelyj.spring_agent;

import dev.jackelyj.spring_agent.tools.CalculatorTools;
import dev.jackelyj.spring_agent.tools.DateTimeTools;
import dev.jackelyj.spring_agent.tools.SystemInfoTools;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for MCP Server functionality.
 * 
 * <p><strong>Note: This test is currently disabled due to complex Spring Boot test configuration
 * requirements with database dependencies.</strong></p>
 * 
 * <h2>Manual Testing Instructions:</h2>
 * 
 * <h3>Test MCP Server via SSE/HTTP:</h3>
 * <pre>
 * # 1. Start the server
 * ./gradlew bootRun
 * 
 * # 2. Check health endpoint
 * curl http://localhost:8080/health/mcp
 * 
 * # 3. The MCP server will be available at:
 * http://localhost:8080/mcp/message
 * </pre>
 * 
 * <h3>Test MCP Server via Stdio:</h3>
 * <pre>
 * # 1. Build the application
 * ./gradlew bootJar
 * 
 * # 2. Run in Stdio mode
 * ./scripts/mcp-server-stdio.sh
 * 
 * # 3. Configure in Claude Desktop or Cursor IDE
 * See mcp-client-config.json for configuration
 * </pre>
 * 
 * <h3>Expected Behavior:</h3>
 * <ul>
 *   <li>MCP Server should be enabled and active</li>
 *   <li>34 tools should be registered (11 DateTime + 13 Calculator + 10 SystemInfo)</li>
 *   <li>Health endpoint should return status: UP</li>
 *   <li>Tools should be invokable via MCP protocol</li>
 * </ul>
 * 
 * @see <a href="../../../../../../../docs/MCP_INTEGRATION_GUIDE.md">MCP Integration Guide</a>
 */
@SpringBootTest(
    properties = {
        "spring.ai.mcp.server.enabled=true",
        "spring.ai.mcp.server.name=spring-agent-tools-test",
        "spring.ai.mcp.server.version=1.0.0-test",
        "spring.ai.mcp.server.type=ASYNC",
        "spring.ai.mcp.server.sse-message-endpoint=/mcp/message",
        "spring.ai.mcp.client.enabled=false",
        "chat.memory.type=in-memory"
    }
)
@ActiveProfiles("test")
@Tag("integration")
@Disabled("Requires database configuration or complex Spring Boot test setup. Use manual testing instead.")
public class McpServerIntegrationTest {

    @Autowired(required = false)
    private DateTimeTools dateTimeTools;

    @Autowired(required = false)
    private CalculatorTools calculatorTools;

    @Autowired(required = false)
    private SystemInfoTools systemInfoTools;

    @Value("${spring.ai.mcp.server.enabled}")
    private boolean mcpServerEnabled;

    @Value("${spring.ai.mcp.server.name}")
    private String mcpServerName;

    /**
     * Verify that MCP Server configuration is properly set.
     */
    @Test
    public void testMcpServerIsEnabled() {
        assertThat(mcpServerEnabled).isTrue();
        assertThat(mcpServerName).isEqualTo("spring-agent-tools-test");
    }

    /**
     * Verify that all tool components are registered.
     */
    @Test
    public void testToolsAreRegistered() {
        assertThat(dateTimeTools).isNotNull();
        assertThat(calculatorTools).isNotNull();
        assertThat(systemInfoTools).isNotNull();
    }

    /**
     * Test DateTime tools basic functionality.
     */
    @Test
    public void testDateTimeToolsFunctionality() {
        String currentDateTime = dateTimeTools.getCurrentDateTime();
        assertThat(currentDateTime).isNotNull().isNotEmpty();
        
        String currentDate = dateTimeTools.getCurrentDate();
        assertThat(currentDate).isNotNull().matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Test Calculator tools basic functionality.
     */
    @Test
    public void testCalculatorToolsFunctionality() {
        String result = calculatorTools.calculate(10.0, "+", 5.0);
        assertThat(result).contains("15");
        
        result = calculatorTools.power(2.0, 3.0);
        assertThat(result).contains("8");
    }

    /**
     * Test System Info tools basic functionality.
     */
    @Test
    public void testSystemInfoToolsFunctionality() {
        String systemHealth = systemInfoTools.getSystemHealth();
        assertThat(systemHealth).contains("系统健康状态检查");
        
        String memoryUsage = systemInfoTools.getMemoryUsage();
        assertThat(memoryUsage).contains("JVM内存使用情况");
    }
}
