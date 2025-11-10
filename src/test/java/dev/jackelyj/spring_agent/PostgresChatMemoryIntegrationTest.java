package dev.jackelyj.spring_agent;

import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PostgreSQL-backed Chat Memory.
 * 
 * These tests require:
 * - PostgreSQL running on localhost:5432
 * - spring.profiles.active=postgres
 * 
 * Run with: ./gradlew test --tests PostgresChatMemoryIntegrationTest
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class PostgresChatMemoryIntegrationTest {
    
    @Autowired
    private ChatMemory chatMemory;
    
    @Autowired
    private ConversationMemoryService conversationMemoryService;
    
    private static final String TEST_CONVERSATION_ID = "test-postgres-conv";
    
    @BeforeEach
    void setUp() {
        // Clear test conversation before each test
        conversationMemoryService.clearConversation(TEST_CONVERSATION_ID);
    }
    
    @Test
    void contextLoads() {
        assertThat(chatMemory).isNotNull();
        assertThat(conversationMemoryService).isNotNull();
    }
    
    @Test
    void testAddAndRetrieveMessages() {
        // Given
        UserMessage userMsg = new UserMessage("Hello, AI!");
        AssistantMessage assistantMsg = new AssistantMessage("Hello! How can I help you?");
        
        // When
        chatMemory.add(TEST_CONVERSATION_ID, List.of(userMsg, assistantMsg));
        
        // Then
        List<Message> retrieved = chatMemory.get(TEST_CONVERSATION_ID);
        assertThat(retrieved).hasSize(2);
        assertThat(retrieved.get(0).getText()).isEqualTo("Hello, AI!");
        assertThat(retrieved.get(1).getText()).isEqualTo("Hello! How can I help you?");
    }
    
    @Test
    void testMessagePersistence() {
        // Given: Add messages
        chatMemory.add(TEST_CONVERSATION_ID, List.of(
            new UserMessage("What is Spring AI?"),
            new AssistantMessage("Spring AI is a framework for building AI applications.")
        ));
        
        // When: Retrieve messages (simulating app restart)
        List<Message> messages = chatMemory.get(TEST_CONVERSATION_ID);
        
        // Then: Messages should be persisted
        assertThat(messages).hasSize(2);
        assertThat(conversationMemoryService.conversationExists(TEST_CONVERSATION_ID))
            .isTrue();
    }
    
    @Test
    void testGetMessageCount() {
        // Given
        chatMemory.add(TEST_CONVERSATION_ID, List.of(
            new UserMessage("Message 1"),
            new AssistantMessage("Response 1"),
            new UserMessage("Message 2"),
            new AssistantMessage("Response 2")
        ));
        
        // When
        int count = conversationMemoryService.getConversationMessageCount(TEST_CONVERSATION_ID);
        
        // Then
        assertThat(count).isEqualTo(4);
    }
    
    @Test
    void testGetAllConversationIds() {
        // Given: Create multiple conversations
        String conv1 = "conv-1";
        String conv2 = "conv-2";
        
        chatMemory.add(conv1, List.of(new UserMessage("Conv 1 message")));
        chatMemory.add(conv2, List.of(new UserMessage("Conv 2 message")));
        
        // When
        List<String> conversationIds = conversationMemoryService.getAllConversationIds();
        
        // Then
        assertThat(conversationIds)
            .contains(conv1, conv2);
        
        // Cleanup
        conversationMemoryService.clearConversation(conv1);
        conversationMemoryService.clearConversation(conv2);
    }
    
    @Test
    void testExportConversation() {
        // Given
        chatMemory.add(TEST_CONVERSATION_ID, List.of(
            new UserMessage("Export test message"),
            new AssistantMessage("Export test response")
        ));
        
        // When
        List<Message> exported = conversationMemoryService.exportConversation(TEST_CONVERSATION_ID);
        
        // Then
        assertThat(exported).hasSize(2);
        assertThat(exported.get(0).getText()).isEqualTo("Export test message");
        assertThat(exported.get(1).getText()).isEqualTo("Export test response");
    }
    
    @Test
    void testClearConversation() {
        // Given: Add messages
        chatMemory.add(TEST_CONVERSATION_ID, List.of(
            new UserMessage("To be cleared"),
            new AssistantMessage("Will be removed")
        ));
        
        assertThat(conversationMemoryService.conversationExists(TEST_CONVERSATION_ID))
            .isTrue();
        
        // When
        boolean cleared = conversationMemoryService.clearConversation(TEST_CONVERSATION_ID);
        
        // Then
        assertThat(cleared).isTrue();
        assertThat(conversationMemoryService.getConversationMessageCount(TEST_CONVERSATION_ID))
            .isZero();
    }
    
    @Test
    void testClearAllConversations() {
        // Given: Create multiple conversations
        chatMemory.add("clear-all-1", List.of(new UserMessage("Msg 1")));
        chatMemory.add("clear-all-2", List.of(new UserMessage("Msg 2")));
        chatMemory.add("clear-all-3", List.of(new UserMessage("Msg 3")));
        
        // When
        boolean cleared = conversationMemoryService.clearAllConversations();
        
        // Then
        assertThat(cleared).isTrue();
        
        // All conversations should be empty
        assertThat(conversationMemoryService.getConversationMessageCount("clear-all-1"))
            .isZero();
        assertThat(conversationMemoryService.getConversationMessageCount("clear-all-2"))
            .isZero();
        assertThat(conversationMemoryService.getConversationMessageCount("clear-all-3"))
            .isZero();
    }
    
    @Test
    void testConversationExists() {
        // Given
        String existingConv = "existing-conv";
        String nonExistingConv = "non-existing-conv";
        
        chatMemory.add(existingConv, List.of(new UserMessage("Exists")));
        
        // When & Then
        assertThat(conversationMemoryService.conversationExists(existingConv))
            .isTrue();
        assertThat(conversationMemoryService.conversationExists(nonExistingConv))
            .isFalse();
        
        // Cleanup
        conversationMemoryService.clearConversation(existingConv);
    }
    
    @Test
    void testMultipleConversationIsolation() {
        // Given: Create two separate conversations
        String conv1 = "isolation-conv-1";
        String conv2 = "isolation-conv-2";
        
        chatMemory.add(conv1, List.of(
            new UserMessage("Conv 1 message")
        ));
        
        chatMemory.add(conv2, List.of(
            new UserMessage("Conv 2 message")
        ));
        
        // When
        List<Message> conv1Messages = chatMemory.get(conv1);
        List<Message> conv2Messages = chatMemory.get(conv2);
        
        // Then: Each conversation should only contain its own messages
        assertThat(conv1Messages).hasSize(1);
        assertThat(conv1Messages.get(0).getText()).contains("Conv 1");
        
        assertThat(conv2Messages).hasSize(1);
        assertThat(conv2Messages.get(0).getText()).contains("Conv 2");
        
        // Cleanup
        conversationMemoryService.clearConversation(conv1);
        conversationMemoryService.clearConversation(conv2);
    }
}

