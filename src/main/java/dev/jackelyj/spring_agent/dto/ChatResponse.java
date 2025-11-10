package dev.jackelyj.spring_agent.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatResponse {

    private String response;
    private String conversationId;
    private LocalDateTime timestamp;
    private boolean streaming;

    // 新增：工具调用相关字段
    private boolean toolsUsed;
    private List<String> toolsInvoked;
    private Map<String, Object> toolResults;

    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String response, String conversationId) {
        this.response = response;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
        this.streaming = false;
    }

    public ChatResponse(String response, String conversationId, boolean streaming) {
        this.response = response;
        this.conversationId = conversationId;
        this.timestamp = LocalDateTime.now();
        this.streaming = streaming;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean isToolsUsed() {
        return toolsUsed;
    }

    public void setToolsUsed(boolean toolsUsed) {
        this.toolsUsed = toolsUsed;
    }

    public List<String> getToolsInvoked() {
        return toolsInvoked;
    }

    public void setToolsInvoked(List<String> toolsInvoked) {
        this.toolsInvoked = toolsInvoked;
    }

    public Map<String, Object> getToolResults() {
        return toolResults;
    }

    public void setToolResults(Map<String, Object> toolResults) {
        this.toolResults = toolResults;
    }
}