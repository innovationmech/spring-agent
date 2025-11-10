package dev.jackelyj.spring_agent.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String conversationId;

    private String systemPrompt;

    // 新增：工具调用相关字段
    private Boolean enableTools;

    private String[] allowedToolNames;

    public ChatRequest() {}

    public ChatRequest(String message, String conversationId) {
        this.message = message;
        this.conversationId = conversationId;
        this.enableTools = true; // 默认启用工具
    }

    public ChatRequest(String message, String conversationId, String systemPrompt) {
        this.message = message;
        this.conversationId = conversationId;
        this.systemPrompt = systemPrompt;
        this.enableTools = true; // 默认启用工具
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Boolean getEnableTools() {
        return enableTools;
    }

    public void setEnableTools(Boolean enableTools) {
        this.enableTools = enableTools;
    }

    public String[] getAllowedToolNames() {
        return allowedToolNames;
    }

    public void setAllowedToolNames(String[] allowedToolNames) {
        this.allowedToolNames = allowedToolNames;
    }
}