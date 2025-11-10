package dev.jackelyj.spring_agent.config;

import dev.jackelyj.spring_agent.tools.CalculatorTools;
import dev.jackelyj.spring_agent.tools.DateTimeTools;
import dev.jackelyj.spring_agent.tools.SystemInfoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.options.model:gpt-oss}")
    private String modelName;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private Double temperature;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

    @Bean
    public Object[] toolObjects(DateTimeTools dateTimeTools,
                               CalculatorTools calculatorTools,
                               SystemInfoTools systemInfoTools) {
        return new Object[]{dateTimeTools, calculatorTools, systemInfoTools};
    }

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