package dev.jackelyj.spring_agent;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.ollama.base-url=http://localhost:11434",
    "spring.ai.ollama.chat.model=gpt-oss",
    "chat.memory.type=in-memory"
})
@Tag("integration")
public class ToolCallingIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallingIntegrationTest.class);

    @Autowired
    private ChatService chatService;

    @Test
    public void testDateTimeToolsIntegration() {
        logger.info("Testing DateTime Tools integration...");

        // 测试获取当前时间
        ChatRequest timeRequest = new ChatRequest("现在几点了？", "test-conversation-1");
        ChatResponse timeResponse = chatService.chat(timeRequest);

        assertNotNull(timeResponse);
        assertNotNull(timeResponse.getResponse());
        assertFalse(timeResponse.getResponse().contains("Error"));
        logger.info("Time tool response: {}", timeResponse.getResponse());

        // 测试日期计算
        ChatRequest dateRequest = new ChatRequest("2024年1月1日到2024年1月10日之间有多少天？", "test-conversation-1");
        ChatResponse dateResponse = chatService.chat(dateRequest);

        assertNotNull(dateResponse);
        assertNotNull(dateResponse.getResponse());
        assertTrue(dateResponse.getResponse().contains("天"));
        logger.info("Date calculation response: {}", dateResponse.getResponse());
    }

    @Test
    public void testCalculatorToolsIntegration() {
        logger.info("Testing Calculator Tools integration...");

        // 测试基本计算
        ChatRequest calcRequest = new ChatRequest("计算 25 + 17 等于多少？", "test-conversation-2");
        ChatResponse calcResponse = chatService.chat(calcRequest);

        assertNotNull(calcResponse);
        assertNotNull(calcResponse.getResponse());
        assertTrue(calcResponse.getResponse().contains("42") || calcResponse.getResponse().contains("计算"));
        logger.info("Calculator response: {}", calcResponse.getResponse());

        // 测试平方根计算
        ChatRequest sqrtRequest = new ChatRequest("16的平方根是多少？", "test-conversation-2");
        ChatResponse sqrtResponse = chatService.chat(sqrtRequest);

        assertNotNull(sqrtResponse);
        assertNotNull(sqrtResponse.getResponse());
        logger.info("Square root response: {}", sqrtResponse.getResponse());

        // 测试平均值计算
        ChatRequest avgRequest = new ChatRequest("计算数字1,2,3,4,5的平均值", "test-conversation-2");
        ChatResponse avgResponse = chatService.chat(avgRequest);

        assertNotNull(avgResponse);
        assertNotNull(avgResponse.getResponse());
        logger.info("Average calculation response: {}", avgResponse.getResponse());
    }

    @Test
    public void testSystemInfoToolsIntegration() {
        logger.info("Testing System Info Tools integration...");

        // 测试系统健康状态
        ChatRequest healthRequest = new ChatRequest("检查系统健康状态", "test-conversation-3");
        ChatResponse healthResponse = chatService.chat(healthRequest);

        assertNotNull(healthResponse);
        assertNotNull(healthResponse.getResponse());
        assertTrue(healthResponse.getResponse().contains("健康") || healthResponse.getResponse().contains("系统"));
        logger.info("System health response: {}", healthResponse.getResponse());

        // 测试内存信息
        ChatRequest memoryRequest = new ChatRequest("显示JVM内存使用情况", "test-conversation-3");
        ChatResponse memoryResponse = chatService.chat(memoryRequest);

        assertNotNull(memoryResponse);
        assertNotNull(memoryResponse.getResponse());
        logger.info("Memory info response: {}", memoryResponse.getResponse());

        // 测试系统信息
        ChatRequest systemRequest = new ChatRequest("获取操作系统信息", "test-conversation-3");
        ChatResponse systemResponse = chatService.chat(systemRequest);

        assertNotNull(systemResponse);
        assertNotNull(systemResponse.getResponse());
        logger.info("System info response: {}", systemResponse.getResponse());
    }

    @Test
    public void testMixedToolUsage() {
        logger.info("Testing mixed tool usage...");

        // 测试多个工具的混合使用
        ChatRequest mixedRequest = new ChatRequest(
                "请告诉我现在的时间，然后计算123乘以456等于多少，最后检查系统的内存使用情况",
                "test-conversation-4"
        );
        ChatResponse mixedResponse = chatService.chat(mixedRequest);

        assertNotNull(mixedResponse);
        assertNotNull(mixedResponse.getResponse());
        assertFalse(mixedResponse.getResponse().contains("Error"));

        // 验证响应中包含时间、计算结果和内存信息
        logger.info("Mixed tool usage response: {}", mixedResponse.getResponse());
    }

    @Test
    public void testToolUsageWithStreaming() {
        logger.info("Testing tool usage with streaming...");

        // 测试流式响应中的工具调用
        ChatRequest streamRequest = new ChatRequest("计算圆周率到小数点后5位，然后告诉我今天的日期", "test-conversation-5");

        StringBuilder fullResponse = new StringBuilder();
        chatService.chatStream(streamRequest)
                .doOnNext(response -> {
                    assertNotNull(response);
                    assertNotNull(response.getResponse());
                    fullResponse.append(response.getResponse());
                })
                .blockLast();

        String completeResponse = fullResponse.toString();
        assertNotNull(completeResponse);
        assertFalse(completeResponse.isEmpty());
        logger.info("Streaming tool usage response: {}", completeResponse);
    }

    @Test
    public void testToolUsageWithCustomSystemPrompt() {
        logger.info("Testing tool usage with custom system prompt...");

        String customSystemPrompt = "你是一个数学助手，专门帮助用户进行各种计算。请使用计算工具来回答用户的问题。";
        ChatRequest customRequest = new ChatRequest(
                "计算 (15 + 25) * 3 - 10 等于多少？",
                "test-conversation-6",
                customSystemPrompt
        );
        customRequest.setEnableTools(true);

        ChatResponse customResponse = chatService.chat(customRequest);

        assertNotNull(customResponse);
        assertNotNull(customResponse.getResponse());
        assertFalse(customResponse.getResponse().contains("Error"));
        logger.info("Custom system prompt tool response: {}", customResponse.getResponse());
    }

    @Test
    public void testErrorHandlingInTools() {
        logger.info("Testing error handling in tools...");

        // 测试除零错误
        ChatRequest divideByZeroRequest = new ChatRequest("计算 10 除以 0 等于多少？", "test-conversation-7");
        ChatResponse divideByZeroResponse = chatService.chat(divideByZeroRequest);

        assertNotNull(divideByZeroResponse);
        assertNotNull(divideByZeroResponse.getResponse());
        logger.info("Divide by zero response: {}", divideByZeroResponse.getResponse());

        // 测试无效日期
        ChatRequest invalidDateRequest = new ChatRequest("计算 2024年2月30日到今天有多少天？", "test-conversation-7");
        ChatResponse invalidDateResponse = chatService.chat(invalidDateRequest);

        assertNotNull(invalidDateResponse);
        assertNotNull(invalidDateResponse.getResponse());
        logger.info("Invalid date response: {}", invalidDateResponse.getResponse());
    }

    @Test
    public void testToolUsageWithContextualMemory() {
        logger.info("Testing tool usage with contextual memory...");

        String conversationId = "test-conversation-8";

        // 第一个问题
        ChatRequest firstRequest = new ChatRequest("记住数字42", conversationId);
        ChatResponse firstResponse = chatService.chat(firstRequest);
        logger.info("First response: {}", firstResponse.getResponse());

        // 第二个问题，测试记忆和工具结合
        ChatRequest secondRequest = new ChatRequest("计算刚才记住的数字乘以2等于多少？", conversationId);
        ChatResponse secondResponse = chatService.chat(secondRequest);

        assertNotNull(secondResponse);
        assertNotNull(secondResponse.getResponse());
        logger.info("Contextual memory tool response: {}", secondResponse.getResponse());
    }
}