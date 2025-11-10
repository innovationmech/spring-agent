package dev.jackelyj.spring_agent.controller;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatController 单元测试
 */
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ConversationMemoryService conversationMemoryService;

    private ChatResponse testResponse;

    @BeforeEach
    void setUp() {
        new ChatRequest("Hello", "test-conversation", "You are a test assistant");
        testResponse = new ChatResponse("Hello! How can I help you?", "test-conversation", false);
    }

    @Test
    void testChat_Success() throws Exception {
        // Arrange
        when(chatService.chat(any(ChatRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\",\"conversationId\":\"test-conversation\",\"systemPrompt\":\"You are a test assistant\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hello! How can I help you?"))
                .andExpect(jsonPath("$.conversationId").value("test-conversation"))
                .andExpect(jsonPath("$.streaming").value(false));

        verify(chatService, times(1)).chat(any(ChatRequest.class));
    }

    @Test
    void testChatStream_Success() throws Exception {
        // Arrange
        ChatResponse response1 = new ChatResponse("Hello", "test-conversation", true);
        ChatResponse response2 = new ChatResponse(" there!", "test-conversation", true);
        when(chatService.chatStream(any(ChatRequest.class))).thenReturn(Flux.just(response1, response2));

        // Act & Assert
        mockMvc.perform(post("/api/v1/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\",\"conversationId\":\"test-conversation\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/event-stream"));

        verify(chatService, times(1)).chatStream(any(ChatRequest.class));
    }

    @Test
    void testClearConversation_Success() throws Exception {
        // Arrange
        when(chatService.clearConversation("test-conversation")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/chat/clear/test-conversation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conversation memory cleared successfully"))
                .andExpect(jsonPath("$.conversationId").value("test-conversation"));

        verify(chatService, times(1)).clearConversation("test-conversation");
    }

    @Test
    void testClearConversation_Failure() throws Exception {
        // Arrange
        when(chatService.clearConversation("invalid-conversation")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/chat/clear/invalid-conversation"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to clear conversation memory"));

        verify(chatService, times(1)).clearConversation("invalid-conversation");
    }

    @Test
    void testClearAllConversations_Success() throws Exception {
        // Arrange
        when(conversationMemoryService.clearAllConversations()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/v1/chat/clear-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All conversation memories cleared successfully"));

        verify(conversationMemoryService, times(1)).clearAllConversations();
    }

    @Test
    void testClearAllConversations_Failure() throws Exception {
        // Arrange
        when(conversationMemoryService.clearAllConversations()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/v1/chat/clear-all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to clear all conversations"));

        verify(conversationMemoryService, times(1)).clearAllConversations();
    }

    @Test
    void testGetServiceHealth_Success() throws Exception {
        // Arrange
        when(chatService.isHealthy()).thenReturn(true);
        when(conversationMemoryService.isHealthy()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/health/service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatService").value(true))
                .andExpect(jsonPath("$.conversationMemoryService").value(true))
                .andExpect(jsonPath("$.overall").value(true));

        verify(chatService, times(1)).isHealthy();
        verify(conversationMemoryService, times(1)).isHealthy();
    }

    @Test
    void testChatLegacy_Success() throws Exception {
        // Arrange
        when(chatService.chat(any(ChatRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/chat")
                .param("message", "Hello")
                .param("conversationId", "test-conversation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hello! How can I help you?"))
                .andExpect(jsonPath("$.conversationId").value("test-conversation"));

        verify(chatService, times(1)).chat(any(ChatRequest.class));
    }

    @Test
    void testChatErrorHandling() throws Exception {
        // Arrange
        when(chatService.chat(any(ChatRequest.class))).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"Hello\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.response").value("Error processing chat request: Service unavailable"));

        verify(chatService, times(1)).chat(any(ChatRequest.class));
    }
}