package dev.jackelyj.spring_agent.service.impl;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * 聊天服务实现类
 * 使用依赖注入，遵循依赖倒置原则
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    private final ChatClient streamingChatClient;
    private final ConversationMemoryService conversationMemoryService;

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

    @Autowired
    public ChatServiceImpl(ChatClient chatClient,
                           @Qualifier("streamingChatClient") ChatClient streamingChatClient,
                           ConversationMemoryService conversationMemoryService) {
        this.chatClient = chatClient;
        this.streamingChatClient = streamingChatClient;
        this.conversationMemoryService = conversationMemoryService;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            logger.info("Processing chat request for conversation: {}", sanitizeForLog(request.getConversationId()));

            String conversationId = getOrCreateConversationId(request);

            var promptSpec = chatClient.prompt()
                    .user(request.getMessage());

            // 添加自定义系统提示
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
                promptSpec.system(request.getSystemPrompt());
            }

            var promptBuilder = promptSpec.advisors(advisor -> advisor.param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, conversationId));

            String response = promptBuilder.call().content();

            logger.info("Successfully generated response for conversation: {}", sanitizeForLog(conversationId));
            return new ChatResponse(response, conversationId, false);

        } catch (Exception e) {
            logger.error("Error processing chat request: {}", e.getMessage(), e);
            return new ChatResponse("Error processing chat request: " + e.getMessage(),
                    request.getConversationId(), false);
        }
    }

    @Override
    public Flux<ChatResponse> chatStream(ChatRequest request) {
        try {
            logger.info("Processing streaming chat request for conversation: {}", sanitizeForLog(request.getConversationId()));

            String conversationId = getOrCreateConversationId(request);

            var promptSpec = streamingChatClient.prompt()
                    .user(request.getMessage());

            // 添加自定义系统提示
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
                promptSpec.system(request.getSystemPrompt());
            }

            var promptBuilder = promptSpec.advisors(advisor -> advisor.param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID, conversationId));

            return promptBuilder.stream()
                    .content()
                    .map(content -> new ChatResponse(content, conversationId, true))
                    .doOnComplete(() -> logger.info("Streaming completed for conversation: {}", sanitizeForLog(conversationId)))
                    .onErrorReturn(new ChatResponse("Error in streaming response", conversationId, true))
                    .doOnError(error -> logger.error("Streaming error for conversation {}: {}",
                            sanitizeForLog(conversationId), error.getMessage()));

        } catch (Exception e) {
            logger.error("Error processing streaming chat request: {}", e.getMessage(), e);
            return Flux.just(new ChatResponse("Error processing streaming request: " + e.getMessage(),
                    request.getConversationId(), true));
        }
    }

    @Override
    public boolean clearConversation(String conversationId) {
        logger.info("Clearing conversation memory for ID: {}", sanitizeForLog(conversationId));
        return conversationMemoryService.clearConversation(conversationId);
    }

    @Override
    public boolean isHealthy() {
        return chatClient != null &&
               streamingChatClient != null &&
               conversationMemoryService.isHealthy();
    }

    /**
     * 获取或创建对话ID
     */
    private String getOrCreateConversationId(ChatRequest request) {
        return request.getConversationId() != null
            ? request.getConversationId()
            : UUID.randomUUID().toString();
    }
}