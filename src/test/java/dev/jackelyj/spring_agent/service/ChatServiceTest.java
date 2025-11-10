package dev.jackelyj.spring_agent.service;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ChatService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatService chatService;

    private ChatRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new ChatRequest("Hello", "test-conversation", "You are a test assistant");
    }

    @Test
    void testChat_Success() {
        // Arrange
        ChatResponse expectedResponse = new ChatResponse("Hello! How can I help you?", "test-conversation", false);
        when(chatService.chat(testRequest)).thenReturn(expectedResponse);

        // Act
        ChatResponse actualResponse = chatService.chat(testRequest);

        // Assert
        assertNotNull(actualResponse);
        assertEquals("Hello! How can I help you?", actualResponse.getResponse());
        assertEquals("test-conversation", actualResponse.getConversationId());
        assertFalse(actualResponse.isStreaming());
        verify(chatService, times(1)).chat(testRequest);
    }

    @Test
    void testChatStream_Success() {
        // Arrange
        ChatResponse response1 = new ChatResponse("Hello", "test-conversation", true);
        ChatResponse response2 = new ChatResponse(" there!", "test-conversation", true);
        Flux<ChatResponse> expectedFlux = Flux.just(response1, response2);
        when(chatService.chatStream(testRequest)).thenReturn(expectedFlux);

        // Act & Assert
        StepVerifier.create(chatService.chatStream(testRequest))
                .expectNext(response1)
                .expectNext(response2)
                .verifyComplete();

        verify(chatService, times(1)).chatStream(testRequest);
    }

    @Test
    void testClearConversation_Success() {
        // Arrange
        when(chatService.clearConversation("test-conversation")).thenReturn(true);

        // Act
        boolean result = chatService.clearConversation("test-conversation");

        // Assert
        assertTrue(result);
        verify(chatService, times(1)).clearConversation("test-conversation");
    }

    @Test
    void testClearConversation_Failure() {
        // Arrange
        when(chatService.clearConversation("invalid-conversation")).thenReturn(false);

        // Act
        boolean result = chatService.clearConversation("invalid-conversation");

        // Assert
        assertFalse(result);
        verify(chatService, times(1)).clearConversation("invalid-conversation");
    }

    @Test
    void testIsHealthy_Success() {
        // Arrange
        when(chatService.isHealthy()).thenReturn(true);

        // Act
        boolean result = chatService.isHealthy();

        // Assert
        assertTrue(result);
        verify(chatService, times(1)).isHealthy();
    }

    @Test
    void testIsHealthy_Failure() {
        // Arrange
        when(chatService.isHealthy()).thenReturn(false);

        // Act
        boolean result = chatService.isHealthy();

        // Assert
        assertFalse(result);
        verify(chatService, times(1)).isHealthy();
    }
}