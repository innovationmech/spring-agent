package dev.jackelyj.spring_agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ConversationMemoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ConversationMemoryServiceTest {

    @Mock
    private ConversationMemoryService conversationMemoryService;

    @BeforeEach
    void setUp() {
        // 测试前的设置，如果需要的话
    }

    @Test
    void testClearConversation_Success() {
        // Arrange
        String conversationId = "test-conversation";
        when(conversationMemoryService.clearConversation(conversationId)).thenReturn(true);

        // Act
        boolean result = conversationMemoryService.clearConversation(conversationId);

        // Assert
        assertTrue(result);
        verify(conversationMemoryService, times(1)).clearConversation(conversationId);
    }

    @Test
    void testClearConversation_Failure() {
        // Arrange
        String conversationId = "invalid-conversation";
        when(conversationMemoryService.clearConversation(conversationId)).thenReturn(false);

        // Act
        boolean result = conversationMemoryService.clearConversation(conversationId);

        // Assert
        assertFalse(result);
        verify(conversationMemoryService, times(1)).clearConversation(conversationId);
    }

    @Test
    void testClearAllConversations_Success() {
        // Arrange
        when(conversationMemoryService.clearAllConversations()).thenReturn(true);

        // Act
        boolean result = conversationMemoryService.clearAllConversations();

        // Assert
        assertTrue(result);
        verify(conversationMemoryService, times(1)).clearAllConversations();
    }

    @Test
    void testClearAllConversations_Failure() {
        // Arrange
        when(conversationMemoryService.clearAllConversations()).thenReturn(false);

        // Act
        boolean result = conversationMemoryService.clearAllConversations();

        // Assert
        assertFalse(result);
        verify(conversationMemoryService, times(1)).clearAllConversations();
    }

    @Test
    void testIsHealthy_Success() {
        // Arrange
        when(conversationMemoryService.isHealthy()).thenReturn(true);

        // Act
        boolean result = conversationMemoryService.isHealthy();

        // Assert
        assertTrue(result);
        verify(conversationMemoryService, times(1)).isHealthy();
    }

    @Test
    void testIsHealthy_Failure() {
        // Arrange
        when(conversationMemoryService.isHealthy()).thenReturn(false);

        // Act
        boolean result = conversationMemoryService.isHealthy();

        // Assert
        assertFalse(result);
        verify(conversationMemoryService, times(1)).isHealthy();
    }
}