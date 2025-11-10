package dev.jackelyj.spring_agent.controller;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "spring-agent");
        response.put("version", "1.0.0");

        try {
            // 测试 Ollama 连接
            ollamaChatModel.call(
                new org.springframework.ai.chat.prompt.Prompt("Hello")
            );

            response.put("ollama", "CONNECTED");
            response.put("model", "gpt-oss");
        } catch (Exception e) {
            response.put("ollama", "DISCONNECTED");
            response.put("error", e.getMessage());
            response.put("status", "DOWN");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Spring Agent with Ollama");
        info.put("version", "1.0.0");
        info.put("description", "Spring Boot application with Spring AI 1.0.3 and Ollama integration");
        info.put("endpoints", Map.of(
            "chat", "POST /api/v1/chat - Regular chat",
            "stream", "POST /api/v1/chat/stream - Streaming chat",
            "health", "GET /health - Health check",
            "clear", "DELETE /api/v1/chat/clear/{conversationId} - Clear conversation"
        ));
        info.put("timestamp", LocalDateTime.now());
        return info;
    }
}
