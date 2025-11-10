package dev.jackelyj.spring_agent.repository.impl;

import dev.jackelyj.spring_agent.repository.ChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC-based adapter for ChatMemoryRepository.
 * 
 * This adapter wraps Spring AI's ChatMemory and provides
 * our application-specific interface implementation with JDBC backend.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for adapting JDBC chat memory to our interface
 * - LSP: Can substitute any ChatMemoryRepository implementation
 * - DIP: Depends on Spring AI's ChatMemory abstraction
 */
@Repository
@ConditionalOnProperty(name = "chat.memory.type", havingValue = "jdbc")
public class JdbcChatMemoryRepositoryAdapter implements ChatMemoryRepository {
    
    private final ChatMemory chatMemory;
    private final JdbcTemplate jdbcTemplate;
    
    public JdbcChatMemoryRepositoryAdapter(
            ChatMemory chatMemory,
            JdbcTemplate jdbcTemplate) {
        this.chatMemory = chatMemory;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public int getMessageCount(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        return messages.size();
    }
    
    @Override
    public List<String> getAllConversationIds() {
        // Query the chat_memory table for distinct conversation IDs
        String sql = "SELECT DISTINCT conversation_id FROM ai_chat_memory ORDER BY conversation_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("conversation_id"));
    }
    
    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }
    
    @Override
    public boolean conversationExists(String conversationId) {
        String sql = "SELECT COUNT(*) FROM ai_chat_memory WHERE conversation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId);
        return count != null && count > 0;
    }
    
    @Override
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
    }
    
    @Override
    public void clearAllConversations() {
        // Get all conversation IDs and clear them one by one
        List<String> conversationIds = getAllConversationIds();
        conversationIds.forEach(chatMemory::clear);
    }
}

