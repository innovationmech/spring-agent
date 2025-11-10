package dev.jackelyj.spring_agent.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Chat Memory.
 * 
 * Provides conditional beans for in-memory or JDBC-based chat memory
 * based on application configuration.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for creating ChatMemory beans
 * - OCP: Open for extension with new memory implementations
 * - LSP: Both implementations can substitute ChatMemory interface
 * - DIP: Returns ChatMemory abstraction, not concrete types
 */
@Configuration
public class ChatMemoryConfig {
    
    @Value("${chat.memory.max-messages:10}")
    private int maxMessages;
    
    /**
     * In-Memory Chat Memory Bean (default).
     * 
     * Active when chat.memory.type=in-memory or not specified.
     */
    @Bean
    @ConditionalOnProperty(name = "chat.memory.type", havingValue = "in-memory", matchIfMissing = true)
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .build();
    }
    
    /**
     * JDBC Chat Memory Bean.
     * 
     * Active when chat.memory.type=jdbc.
     * Requires ChatMemoryRepository to be auto-configured by Spring AI.
     */
    @Bean
    @ConditionalOnProperty(name = "chat.memory.type", havingValue = "jdbc")
    public ChatMemory jdbcChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }
}

