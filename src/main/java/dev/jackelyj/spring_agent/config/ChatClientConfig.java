package dev.jackelyj.spring_agent.config;

import dev.jackelyj.spring_agent.tools.CalculatorTools;
import dev.jackelyj.spring_agent.tools.DateTimeTools;
import dev.jackelyj.spring_agent.tools.SystemInfoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ChatClient beans.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for creating ChatClient beans (ChatMemory creation moved to ChatMemoryConfig)
 * - DIP: Depends on ChatMemory abstraction, not concrete implementation
 * - OCP: Open for extension with new chat client configurations
 * 
 * Note: ChatMemory bean is now provided by ChatMemoryConfig,
 * which supports both in-memory and JDBC implementations based on configuration.
 */
@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.options.model:gpt-oss}")
    private String modelName;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private Double temperature;

    /**
     * Configure tool objects for ChatClient.
     * Tools are automatically discovered and made available to the AI.
     */
    @Bean
    public Object[] toolObjects(DateTimeTools dateTimeTools,
                               CalculatorTools calculatorTools,
                               SystemInfoTools systemInfoTools) {
        return new Object[]{dateTimeTools, calculatorTools, systemInfoTools};
    }

    /**
     * Configure main ChatClient for synchronous conversations.
     * 
     * Dependencies are injected, following DIP:
     * - ChatMemory: Abstraction provided by ChatMemoryConfig
     * - OllamaChatModel: Auto-configured by Spring AI
     * - toolObjects: Configured in this class
     */
    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel,
                               ChatMemory chatMemory,
                               Object[] toolObjects) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .build())
                .defaultSystem("You are a helpful AI assistant with access to various tools for date/time calculations, mathematical operations, and system information. " +
                              "When users ask about time, dates, calculations, or system status, automatically use the appropriate tools to provide accurate information.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(toolObjects)
                .build();
    }

    /**
     * Configure streaming ChatClient for asynchronous conversations.
     */
    @Bean("streamingChatClient")
    public ChatClient streamingChatClient(OllamaChatModel ollamaChatModel,
                                        ChatMemory chatMemory,
                                        Object[] toolObjects) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .build())
                .defaultSystem("You are a helpful AI assistant for streaming conversations with access to various tools for date/time calculations, mathematical operations, and system information. " +
                              "When users ask about time, dates, calculations, or system status, automatically use the appropriate tools to provide accurate information.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(toolObjects)
                .build();
    }
}