package dev.jackelyj.spring_agent.controller;

import dev.jackelyj.spring_agent.dto.*;
import dev.jackelyj.spring_agent.service.ChatService;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import dev.jackelyj.spring_agent.service.DocumentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 聊天和文档管理控制器
 * 
 * SOLID Principles:
 * - SRP: 处理 HTTP 请求和响应，不包含业务逻辑
 * - DIP: 依赖于服务接口而非具体实现
 * - ISP: 将文档管理功能整合到一个控制器中（也可以拆分为独立的 DocumentController）
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    private final ConversationMemoryService conversationMemoryService;
    private final Optional<DocumentService> documentService;

    @Autowired
    public ChatController(
            ChatService chatService, 
            ConversationMemoryService conversationMemoryService,
            Optional<DocumentService> documentService) {
        this.chatService = chatService;
        this.conversationMemoryService = conversationMemoryService;
        this.documentService = documentService;
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
        health.put("documentService", documentService.isPresent());
        health.put("overall", chatServiceHealth && memoryServiceHealth);
        return ResponseEntity.ok(health);
    }
    
    // ==================== Document Management Endpoints ====================
    
    /**
     * 上传文档到向量存储
     */
    @PostMapping("/documents")
    public ResponseEntity<DocumentResponse> addDocuments(@Valid @RequestBody DocumentRequest request) {
        if (documentService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(DocumentResponse.error("Document service is not available. Please configure PostgreSQL."));
        }
        
        try {
            log.info("Receiving document upload request with {} texts", request.texts().size());
            List<String> ids = documentService.get().addDocuments(request.texts(), 
                    request.metadata() != null ? request.metadata() : Map.of());
            
            return ResponseEntity.ok(DocumentResponse.success(
                    "Documents uploaded successfully", ids));
        } catch (Exception e) {
            log.error("Failed to upload documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DocumentResponse.error("Failed to upload documents: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索相似文档
     */
    @PostMapping("/documents/search")
    public ResponseEntity<List<DocumentSearchResult>> searchDocuments(
            @Valid @RequestBody DocumentSearchRequest request) {
        if (documentService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        
        try {
            log.info("Searching documents with query: {}", request.query());
            
            List<DocumentSearchResult> results;
            if (request.filterExpression() != null && !request.filterExpression().isEmpty()) {
                results = documentService.get().searchWithFilter(
                        request.query(), 
                        request.filterExpression(), 
                        request.topK());
            } else {
                results = documentService.get().searchSimilar(
                        request.query(), 
                        request.topK(), 
                        request.threshold());
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Failed to search documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除指定文档
     */
    @DeleteMapping("/documents")
    public ResponseEntity<Map<String, String>> deleteDocuments(@RequestBody List<String> documentIds) {
        if (documentService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Document service is not available"));
        }
        
        try {
            log.info("Deleting {} documents", documentIds.size());
            documentService.get().deleteDocuments(documentIds);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Documents deleted successfully",
                    "count", String.valueOf(documentIds.size())));
        } catch (Exception e) {
            log.error("Failed to delete documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete documents: " + e.getMessage()));
        }
    }
    
    // ==================== Conversation Management Endpoints ====================
    
    /**
     * 获取所有会话 ID
     */
    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> getAllConversations() {
        try {
            List<String> conversationIds = conversationMemoryService.getAllConversationIds();
            
            Map<String, Object> response = new HashMap<>();
            response.put("conversations", conversationIds);
            response.put("count", conversationIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get all conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取会话消息数量
     */
    @GetMapping("/conversations/{conversationId}/count")
    public ResponseEntity<Map<String, Object>> getConversationMessageCount(@PathVariable String conversationId) {
        try {
            int count = conversationMemoryService.getConversationMessageCount(conversationId);
            boolean exists = conversationMemoryService.conversationExists(conversationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversationId);
            response.put("messageCount", count);
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get conversation message count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
