package dev.jackelyj.spring_agent.controller;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天控制器
 * 遵循依赖倒置原则，依赖于服务接口而非具体实现
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final ConversationMemoryService conversationMemoryService;

    @Autowired
    public ChatController(ChatService chatService, ConversationMemoryService conversationMemoryService) {
        this.chatService = chatService;
        this.conversationMemoryService = conversationMemoryService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.chat(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatResponse("Error processing chat request: " + e.getMessage(), null));
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request);
    }

    @DeleteMapping("/chat/clear/{conversationId}")
    public ResponseEntity<Map<String, String>> clearConversation(@PathVariable String conversationId) {
        try {
            boolean success = chatService.clearConversation(conversationId);

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("message", "Conversation memory cleared successfully");
                response.put("conversationId", conversationId);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to clear conversation memory");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to clear conversation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/chat/clear-all")
    public ResponseEntity<Map<String, String>> clearAllConversations() {
        try {
            boolean success = conversationMemoryService.clearAllConversations();

            Map<String, String> response = new HashMap<>();
            if (success) {
                response.put("message", "All conversation memories cleared successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to clear all conversations");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to clear all conversations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 兼容旧的GET端点
    @GetMapping("/chat")
    public ResponseEntity<ChatResponse> chatLegacy(@RequestParam String message,
                                                   @RequestParam(required = false) String conversationId) {
        ChatRequest request = new ChatRequest(message, conversationId);
        return chat(request);
    }

    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStreamLegacy(@RequestParam String message,
                                               @RequestParam(required = false) String conversationId) {
        ChatRequest request = new ChatRequest(message, conversationId);
        return chatStream(request);
    }

    /**
     * 获取服务健康状态
     */
    @GetMapping("/health/service")
    public ResponseEntity<Map<String, Object>> getServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        boolean chatServiceHealth = chatService.isHealthy();
        boolean memoryServiceHealth = conversationMemoryService.isHealthy();

        health.put("chatService", chatServiceHealth);
        health.put("conversationMemoryService", memoryServiceHealth);
        health.put("overall", chatServiceHealth && memoryServiceHealth);
        return ResponseEntity.ok(health);
    }
}
