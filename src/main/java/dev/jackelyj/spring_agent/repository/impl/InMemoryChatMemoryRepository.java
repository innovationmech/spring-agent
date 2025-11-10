package dev.jackelyj.spring_agent.repository.impl;

import dev.jackelyj.spring_agent.repository.ChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory adapter for ChatMemoryRepository.
 * 
 * This adapter wraps Spring AI's in-memory ChatMemory implementation
 * and provides our application-specific interface.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for adapting in-memory chat memory to our interface
 * - LSP: Can substitute any ChatMemoryRepository implementation
 * - DIP: Depends on Spring AI's ChatMemory abstraction
 */
@Repository
@ConditionalOnProperty(name = "chat.memory.type", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryChatMemoryRepository implements ChatMemoryRepository {
    
    private final ChatMemory chatMemory;
    
    // Track conversation IDs manually since in-memory implementation doesn't expose them
    private final Map<String, Integer> conversationTracker = new ConcurrentHashMap<>();
    
    public InMemoryChatMemoryRepository(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }
    
    @Override
    public int getMessageCount(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        int count = messages.size();
        
        // Track this conversation
        if (count > 0) {
            conversationTracker.put(conversationId, count);
        }
        
        return count;
    }
    
    @Override
    public List<String> getAllConversationIds() {
        return new ArrayList<>(conversationTracker.keySet());
    }
    
    @Override
    public List<Message> getConversationHistory(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        
        // Track this conversation
        if (!messages.isEmpty()) {
            conversationTracker.put(conversationId, messages.size());
        }
        
        return messages;
    }
    
    @Override
    public boolean conversationExists(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        boolean exists = !messages.isEmpty();
        
        if (exists) {
            conversationTracker.put(conversationId, messages.size());
        }
        
        return exists;
    }
    
    @Override
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
        conversationTracker.remove(conversationId);
    }
    
    @Override
    public void clearAllConversations() {
        // Clear all tracked conversations
        conversationTracker.keySet().forEach(chatMemory::clear);
        conversationTracker.clear();
    }
}

