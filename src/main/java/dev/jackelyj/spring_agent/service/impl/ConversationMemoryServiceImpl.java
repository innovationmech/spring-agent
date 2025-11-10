package dev.jackelyj.spring_agent.service.impl;

import dev.jackelyj.spring_agent.repository.ChatMemoryRepository;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 对话记忆管理服务实现类
 * 
 * SOLID Principles:
 * - SRP: 只负责会话管理的业务逻辑
 * - DIP: 依赖 ChatMemoryRepository 抽象接口，而非具体实现
 * - LSP: 完全遵循 ConversationMemoryService 接口契约
 */
@Service
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ConversationMemoryServiceImpl.class);
    
    private final ChatMemoryRepository chatMemoryRepository;

    /**
     * 清理日志输入以防止日志注入攻击
     * 移除或转义换行符和回车符
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public ConversationMemoryServiceImpl(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Override
    public boolean clearConversation(String conversationId) {
        try {
            log.info("Clearing conversation: {}", sanitizeForLog(conversationId));
            chatMemoryRepository.clearConversation(conversationId);
            log.info("Successfully cleared conversation: {}", sanitizeForLog(conversationId));
            return true;
        } catch (Exception e) {
            log.error("Failed to clear conversation: {}", sanitizeForLog(conversationId), e);
            return false;
        }
    }

    @Override
    public boolean clearAllConversations() {
        try {
            log.info("Clearing all conversations");
            chatMemoryRepository.clearAllConversations();
            log.info("Successfully cleared all conversations");
            return true;
        } catch (Exception e) {
            log.error("Failed to clear all conversations", e);
            return false;
        }
    }
    
    @Override
    public int getConversationMessageCount(String conversationId) {
        try {
            int count = chatMemoryRepository.getMessageCount(conversationId);
            log.debug("Conversation {} has {} messages", sanitizeForLog(conversationId), count);
            return count;
        } catch (Exception e) {
            log.error("Failed to get message count for conversation: {}", sanitizeForLog(conversationId), e);
            return 0;
        }
    }
    
    @Override
    public List<String> getAllConversationIds() {
        try {
            List<String> ids = chatMemoryRepository.getAllConversationIds();
            log.debug("Found {} conversations", ids.size());
            return ids;
        } catch (Exception e) {
            log.error("Failed to get all conversation IDs", e);
            return List.of();
        }
    }
    
    @Override
    public List<Message> exportConversation(String conversationId) {
        try {
            log.info("Exporting conversation: {}", sanitizeForLog(conversationId));
            List<Message> messages = chatMemoryRepository.getConversationHistory(conversationId);
            log.info("Exported {} messages from conversation: {}", messages.size(), sanitizeForLog(conversationId));
            return messages;
        } catch (Exception e) {
            log.error("Failed to export conversation: {}", sanitizeForLog(conversationId), e);
            return List.of();
        }
    }
    
    @Override
    public boolean conversationExists(String conversationId) {
        try {
            return chatMemoryRepository.conversationExists(conversationId);
        } catch (Exception e) {
            log.error("Failed to check if conversation exists: {}", sanitizeForLog(conversationId), e);
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check if the repository is available
            return chatMemoryRepository != null;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }
}