package dev.jackelyj.spring_agent.repository;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * Chat memory repository interface for accessing conversation history.
 * 
 * This interface abstracts the underlying storage mechanism (in-memory, JDBC, etc.)
 * following the Dependency Inversion Principle (DIP).
 * 
 * Multiple implementations can be provided (in-memory, JDBC) that are
 * interchangeable according to the Liskov Substitution Principle (LSP).
 */
public interface ChatMemoryRepository {
    
    /**
     * Get the number of messages in a conversation.
     * 
     * @param conversationId The conversation ID
     * @return Number of messages in the conversation
     */
    int getMessageCount(String conversationId);
    
    /**
     * Get all conversation IDs.
     * 
     * @return List of all conversation IDs
     */
    List<String> getAllConversationIds();
    
    /**
     * Get the complete conversation history.
     * 
     * @param conversationId The conversation ID
     * @return List of messages in chronological order
     */
    List<Message> getConversationHistory(String conversationId);
    
    /**
     * Check if a conversation exists.
     * 
     * @param conversationId The conversation ID
     * @return true if the conversation exists, false otherwise
     */
    boolean conversationExists(String conversationId);
    
    /**
     * Clear all messages from a specific conversation.
     * 
     * @param conversationId The conversation ID to clear
     */
    void clearConversation(String conversationId);
    
    /**
     * Clear all conversations.
     */
    void clearAllConversations();
}

