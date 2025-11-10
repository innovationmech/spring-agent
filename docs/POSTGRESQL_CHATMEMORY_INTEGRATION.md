# Spring AI PostgreSQL + pgvector ChatMemory 集成指南

## 目录
- [概述](#概述)
- [依赖配置](#依赖配置)
- [PostgreSQL 数据库设置](#postgresql-数据库设置)
- [Spring Boot 配置](#spring-boot-配置)
- [代码实现](#代码实现)
- [使用示例](#使用示例)
- [测试](#测试)
- [最佳实践](#最佳实践)
- [故障排除](#故障排除)

## 概述

Spring AI 提供了强大的 ChatMemory 功能，支持多种存储后端。使用 PostgreSQL + pgvector 作为 ChatMemory 的持久化存储可以实现：

- **持久化对话历史**：对话数据保存在数据库中，应用重启后不丢失
- **分布式支持**：多个应用实例共享同一个对话存储
- **向量存储集成**：在同一数据库中同时使用 ChatMemory 和 VectorStore
- **企业级可靠性**：利用 PostgreSQL 的事务和备份能力

### 架构组件

```
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot Application               │
├─────────────────────────────────────────────────────────┤
│  ChatClient                                             │
│     ↓                                                    │
│  MessageChatMemoryAdvisor                               │
│     ↓                                                    │
│  MessageWindowChatMemory (maxMessages: 10)              │
│     ↓                                                    │
│  ChatMemoryRepository (Interface)                       │
│     ↓                                                    │
│  JdbcChatMemoryRepositoryAdapter                        │
│     ↓                                                    │
│  Spring AI Auto-configured ChatMemory (JDBC Backend)    │
├─────────────────────────────────────────────────────────┤
│                   JdbcTemplate                          │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              PostgreSQL + pgvector                      │
│  ┌───────────────────────────────────────────────────┐ │
│  │  ai_chat_memory (Chat Memory Table)               │ │
│  │  - conversation_id                                │ │
│  │  - message_type (USER, ASSISTANT, SYSTEM)        │ │
│  │  - content                                        │ │
│  │  - metadata (JSON)                                │ │
│  │  - created_at                                     │ │
│  └───────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────┐ │
│  │  vector_store (Vector Store Table)                │ │
│  │  - id                                             │ │
│  │  - content                                        │ │
│  │  - metadata (JSON)                                │ │
│  │  - embedding (vector(1024))                       │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## 依赖配置

### Gradle (build.gradle)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.7'
    id 'io.spring.dependency-management' version '1.1.7'
}

ext {
    set('springAiVersion', "1.0.3")
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    
    // Spring AI - Ollama (Chat Model)
    implementation 'org.springframework.ai:spring-ai-starter-model-ollama'
    
    // Spring AI - PostgreSQL Chat Memory (关键依赖)
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc'
    
    // Spring AI - PgVector Vector Store (可选，用于向量存储)
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
    
    // PostgreSQL Driver
    runtimeOnly 'org.postgresql:postgresql'
    
    // Test Dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}
```

### Maven (pom.xml)

```xml
<properties>
    <spring.ai.version>1.0.3</spring.ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring.ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    
    <!-- Spring AI - Ollama -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-ollama</artifactId>
    </dependency>
    
    <!-- Spring AI - PostgreSQL Chat Memory (关键依赖) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
    </dependency>
    
    <!-- Spring AI - PgVector Vector Store -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## PostgreSQL 数据库设置

### 使用 Docker Compose

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16
    container_name: spring-agent-postgres
    environment:
      POSTGRES_DB: spring_ai_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_INITDB_ARGS: "-E UTF8"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./src/main/resources/db/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
    driver: local
```

### 数据库初始化脚本

创建 `src/main/resources/db/init.sql`：

```sql
-- 启用 pgvector 扩展（用于向量存储）
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建 Schema（可选）
CREATE SCHEMA IF NOT EXISTS public;

-- Spring AI 会自动创建 ai_chat_memory 表
-- 如果需要手动创建，可以使用以下 SQL：

/*
CREATE TABLE IF NOT EXISTS ai_chat_memory (
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conversation_id 
    ON ai_chat_memory(conversation_id);

CREATE INDEX IF NOT EXISTS idx_created_at 
    ON ai_chat_memory(created_at);
*/

-- Spring AI 也会自动创建 vector_store 表
-- 手动创建示例：

/*
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT,
    metadata JSONB,
    embedding vector(1024)
);

CREATE INDEX ON vector_store 
    USING HNSW (embedding vector_cosine_ops);
*/
```

### 启动 PostgreSQL

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f postgres

# 检查状态
docker-compose ps

# 连接数据库
docker exec -it spring-agent-postgres psql -U postgres -d spring_ai_db
```

## Spring Boot 配置

### application.yml

```yaml
spring:
  application:
    name: spring-agent
  
  # PostgreSQL 数据源配置
  datasource:
    url: jdbc:postgresql://localhost:5432/spring_ai_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  # Spring AI 配置
  ai:
    # Ollama 配置
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: qwen2.5:3b
          temperature: 0.7
      embedding:
        options:
          model: nomic-embed-text

# 自定义 ChatMemory 配置
chat:
  memory:
    type: jdbc              # jdbc 或 in-memory
    max-messages: 10        # 保留的最大消息数
    initialize-schema: true # 自动初始化数据库表

# 日志配置
logging:
  level:
    org.springframework.ai: DEBUG
    org.springframework.jdbc: DEBUG
    dev.jackelyj.spring_agent: DEBUG
```

### application-memory.yml (内存模式)

```yaml
spring:
  config:
    activate:
      on-profile: memory

chat:
  memory:
    type: in-memory
    max-messages: 10
```

### application-postgres.yml (PostgreSQL 模式)

```yaml
spring:
  config:
    activate:
      on-profile: postgres

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/spring_ai_db
    username: postgres
    password: postgres

chat:
  memory:
    type: jdbc
    max-messages: 10
    initialize-schema: true
```

## 代码实现

### 1. ChatMemory 配置类

```java
package dev.jackelyj.spring_agent.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置类
 * 
 * 支持两种模式：
 * 1. in-memory: 内存存储（默认）
 * 2. jdbc: PostgreSQL 持久化存储
 */
@Configuration
public class ChatMemoryConfig {
    
    @Value("${chat.memory.max-messages:10}")
    private int maxMessages;
    
    /**
     * 内存模式 ChatMemory Bean
     * 
     * 当 chat.memory.type=in-memory 或未指定时激活
     */
    @Bean
    @ConditionalOnProperty(name = "chat.memory.type", havingValue = "in-memory", matchIfMissing = true)
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(maxMessages)
                .build();
    }
    
    /**
     * JDBC 模式 ChatMemory Bean
     * 
     * 当 chat.memory.type=jdbc 时激活
     * 依赖 Spring AI 自动配置的 ChatMemoryRepository
     */
    @Bean
    @ConditionalOnProperty(name = "chat.memory.type", havingValue = "jdbc")
    public ChatMemory jdbcChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }
}
```

### 2. ChatClient 配置类

```java
package dev.jackelyj.spring_agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 配置类
 * 
 * 配置带有 ChatMemory 支持的 ChatClient
 */
@Configuration
public class ChatClientConfig {
    
    /**
     * 配置 ChatClient，集成 ChatMemory Advisor
     */
    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder, 
            ChatMemory chatMemory,
            ApplicationContext applicationContext) {
        
        return builder
                // 添加 ChatMemory Advisor（自动管理对话历史）
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                // 配置工具调用上下文
                .defaultFunctions(new FunctionCallbackContext(applicationContext))
                .build();
    }
    
    /**
     * 提供 ChatClient.Builder Bean
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
```

### 3. Repository 适配器（可选）

如果需要自定义 ChatMemory Repository 接口：

```java
package dev.jackelyj.spring_agent.repository;

import org.springframework.ai.chat.messages.Message;
import java.util.List;

/**
 * ChatMemory Repository 接口
 * 
 * 提供应用级别的 ChatMemory 操作抽象
 */
public interface ChatMemoryRepository {
    
    /**
     * 获取对话的消息数量
     */
    int getMessageCount(String conversationId);
    
    /**
     * 获取所有对话 ID
     */
    List<String> getAllConversationIds();
    
    /**
     * 获取对话历史
     */
    List<Message> getConversationHistory(String conversationId);
    
    /**
     * 检查对话是否存在
     */
    boolean conversationExists(String conversationId);
    
    /**
     * 清除指定对话
     */
    void clearConversation(String conversationId);
    
    /**
     * 清除所有对话
     */
    void clearAllConversations();
}
```

### 4. JDBC 适配器实现

```java
package dev.jackelyj.spring_agent.repository.impl;

import dev.jackelyj.spring_agent.repository.ChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JDBC 后端的 ChatMemory Repository 适配器
 */
@Repository
@ConditionalOnProperty(name = "chat.memory.type", havingValue = "jdbc")
public class JdbcChatMemoryRepositoryAdapter implements ChatMemoryRepository {
    
    private final ChatMemory chatMemory;
    private final JdbcTemplate jdbcTemplate;
    
    public JdbcChatMemoryRepositoryAdapter(
            ChatMemory chatMemory,
            JdbcTemplate jdbcTemplate) {
        this.chatMemory = chatMemory;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public int getMessageCount(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        return messages.size();
    }
    
    @Override
    public List<String> getAllConversationIds() {
        String sql = "SELECT DISTINCT conversation_id FROM ai_chat_memory ORDER BY conversation_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("conversation_id"));
    }
    
    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return chatMemory.get(conversationId);
    }
    
    @Override
    public boolean conversationExists(String conversationId) {
        String sql = "SELECT COUNT(*) FROM ai_chat_memory WHERE conversation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId);
        return count != null && count > 0;
    }
    
    @Override
    public void clearConversation(String conversationId) {
        chatMemory.clear(conversationId);
    }
    
    @Override
    public void clearAllConversations() {
        List<String> conversationIds = getAllConversationIds();
        conversationIds.forEach(chatMemory::clear);
    }
}
```

### 5. Service 层实现

```java
package dev.jackelyj.spring_agent.service;

import org.springframework.ai.chat.messages.Message;
import java.util.List;

public interface ConversationMemoryService {
    
    /**
     * 清除指定对话
     */
    void clearConversation(String conversationId);
    
    /**
     * 清除所有对话
     */
    void clearAllConversations();
    
    /**
     * 获取所有对话 ID
     */
    List<String> getAllConversationIds();
    
    /**
     * 获取对话消息数量
     */
    int getMessageCount(String conversationId);
    
    /**
     * 检查对话是否存在
     */
    boolean conversationExists(String conversationId);
    
    /**
     * 获取对话历史
     */
    List<Message> getConversationHistory(String conversationId);
}
```

```java
package dev.jackelyj.spring_agent.service.impl;

import dev.jackelyj.spring_agent.repository.ChatMemoryRepository;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMemoryServiceImpl implements ConversationMemoryService {
    
    private final ChatMemoryRepository chatMemoryRepository;
    
    public ConversationMemoryServiceImpl(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }
    
    @Override
    public void clearConversation(String conversationId) {
        chatMemoryRepository.clearConversation(conversationId);
    }
    
    @Override
    public void clearAllConversations() {
        chatMemoryRepository.clearAllConversations();
    }
    
    @Override
    public List<String> getAllConversationIds() {
        return chatMemoryRepository.getAllConversationIds();
    }
    
    @Override
    public int getMessageCount(String conversationId) {
        return chatMemoryRepository.getMessageCount(conversationId);
    }
    
    @Override
    public boolean conversationExists(String conversationId) {
        return chatMemoryRepository.conversationExists(conversationId);
    }
    
    @Override
    public List<Message> getConversationHistory(String conversationId) {
        return chatMemoryRepository.getConversationHistory(conversationId);
    }
}
```

### 6. Controller 实现

```java
package dev.jackelyj.spring_agent.controller;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import dev.jackelyj.spring_agent.service.ConversationMemoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    
    private final ChatService chatService;
    private final ConversationMemoryService memoryService;
    
    public ChatController(ChatService chatService, ConversationMemoryService memoryService) {
        this.chatService = chatService;
        this.memoryService = memoryService;
    }
    
    /**
     * 标准聊天接口
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
    
    /**
     * 流式聊天接口
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatService.chatStream(request);
    }
    
    /**
     * 清除指定对话
     */
    @DeleteMapping("/clear/{conversationId}")
    public Map<String, String> clearConversation(@PathVariable String conversationId) {
        memoryService.clearConversation(conversationId);
        return Map.of("message", "Conversation cleared", "conversationId", conversationId);
    }
    
    /**
     * 清除所有对话
     */
    @PostMapping("/clear-all")
    public Map<String, String> clearAllConversations() {
        memoryService.clearAllConversations();
        return Map.of("message", "All conversations cleared");
    }
    
    /**
     * 获取所有对话 ID
     */
    @GetMapping("/conversations")
    public List<String> getAllConversations() {
        return memoryService.getAllConversationIds();
    }
    
    /**
     * 获取对话消息数量
     */
    @GetMapping("/conversations/{conversationId}/count")
    public Map<String, Object> getMessageCount(@PathVariable String conversationId) {
        int count = memoryService.getMessageCount(conversationId);
        return Map.of(
            "conversationId", conversationId,
            "messageCount", count
        );
    }
}
```

## 使用示例

### 1. 基本对话

```bash
# 发起对话（会话 ID: user-123）
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，我叫李明",
    "conversationId": "user-123"
  }'

# 继续对话（AI 会记住之前的内容）
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "我叫什么名字？",
    "conversationId": "user-123"
  }'

# 响应示例
{
  "response": "您的名字是李明。",
  "conversationId": "user-123"
}
```

### 2. 查看对话列表

```bash
# 获取所有对话 ID
curl http://localhost:8080/api/v1/chat/conversations

# 响应
["user-123", "user-456", "session-789"]
```

### 3. 查看对话消息数

```bash
# 获取指定对话的消息数量
curl http://localhost:8080/api/v1/chat/conversations/user-123/count

# 响应
{
  "conversationId": "user-123",
  "messageCount": 4
}
```

### 4. 清除对话历史

```bash
# 清除指定对话
curl -X DELETE http://localhost:8080/api/v1/chat/clear/user-123

# 清除所有对话
curl -X POST http://localhost:8080/api/v1/chat/clear-all
```

### 5. 在代码中直接使用 ChatClient

```java
@Service
public class MyService {
    
    private final ChatClient chatClient;
    
    public MyService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    public String chat(String conversationId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                // 设置对话 ID，ChatMemory 会自动管理历史
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
```

## 测试

### 1. 集成测试

```java
package dev.jackelyj.spring_agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("postgres")
@Tag("integration")
class PostgresChatMemoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/init.sql");
    
    @Autowired
    private ChatMemory chatMemory;
    
    private static final String TEST_CONVERSATION_ID = "test-conv-001";
    
    @BeforeEach
    void setUp() {
        // 清除测试对话
        chatMemory.clear(TEST_CONVERSATION_ID);
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
        // Given: 添加消息
        chatMemory.add(TEST_CONVERSATION_ID, List.of(
            new UserMessage("What is Spring AI?"),
            new AssistantMessage("Spring AI is a framework for building AI applications.")
        ));
        
        // When: 检索消息（模拟应用重启）
        List<Message> messages = chatMemory.get(TEST_CONVERSATION_ID);
        
        // Then: 消息应该被持久化
        assertThat(messages).hasSize(2);
    }
    
    @Test
    void testMessageWindow() {
        // Given: 添加超过窗口大小的消息
        for (int i = 1; i <= 15; i++) {
            chatMemory.add(TEST_CONVERSATION_ID, List.of(
                new UserMessage("Message " + i),
                new AssistantMessage("Response " + i)
            ));
        }
        
        // When: 获取消息
        List<Message> messages = chatMemory.get(TEST_CONVERSATION_ID);
        
        // Then: 只保留最近的 10 条消息（配置的 maxMessages）
        assertThat(messages.size()).isLessThanOrEqualTo(10);
    }
}
```

### 2. 运行测试

```bash
# 运行所有测试
./gradlew test

# 只运行集成测试
./gradlew test --tests *IntegrationTest

# 排除集成测试
./gradlew test -x integrationTest
```

## 最佳实践

### 1. 对话 ID 管理

```java
/**
 * 推荐的对话 ID 命名策略
 */
public class ConversationIdStrategy {
    
    // 用户会话 ID
    public static String userSession(String userId) {
        return "user:" + userId;
    }
    
    // 临时会话 ID（带时间戳）
    public static String temporarySession() {
        return "temp:" + System.currentTimeMillis();
    }
    
    // 业务场景 ID
    public static String businessScenario(String userId, String scenario) {
        return String.format("user:%s:scenario:%s", userId, scenario);
    }
}
```

### 2. 消息窗口大小配置

根据不同场景配置合适的窗口大小：

- **客服对话**: 10-20 条消息
- **技术支持**: 20-50 条消息
- **长期对话**: 50-100 条消息
- **简短交互**: 5-10 条消息

### 3. 性能优化

```yaml
# HikariCP 连接池优化
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # 根据并发量调整
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

### 4. 数据库索引优化

```sql
-- 优化查询性能的索引
CREATE INDEX CONCURRENTLY idx_chat_memory_conversation_created 
    ON ai_chat_memory(conversation_id, created_at DESC);

CREATE INDEX CONCURRENTLY idx_chat_memory_created_at 
    ON ai_chat_memory(created_at DESC);

-- 分析查询计划
EXPLAIN ANALYZE 
SELECT * FROM ai_chat_memory 
WHERE conversation_id = 'user-123' 
ORDER BY created_at DESC 
LIMIT 10;
```

### 5. 监控和日志

```java
@Aspect
@Component
@Slf4j
public class ChatMemoryMonitor {
    
    @Around("execution(* dev.jackelyj.spring_agent.service.ChatService.chat(..))")
    public Object monitorChat(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Chat completed in {}ms", duration);
            return result;
            
        } catch (Exception e) {
            log.error("Chat failed", e);
            throw e;
        }
    }
}
```

### 6. 数据清理策略

```java
@Component
@Slf4j
public class ChatMemoryCleanupTask {
    
    private final JdbcTemplate jdbcTemplate;
    
    public ChatMemoryCleanupTask(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 清理 30 天前的对话记录
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点执行
    public void cleanupOldConversations() {
        String sql = """
            DELETE FROM ai_chat_memory 
            WHERE created_at < NOW() - INTERVAL '30 days'
            """;
        
        int deleted = jdbcTemplate.update(sql);
        log.info("Cleaned up {} old chat messages", deleted);
    }
    
    /**
     * 归档重要对话
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void archiveImportantConversations() {
        String sql = """
            INSERT INTO ai_chat_memory_archive 
            SELECT * FROM ai_chat_memory 
            WHERE created_at < NOW() - INTERVAL '7 days'
            AND conversation_id LIKE 'important:%'
            """;
        
        jdbcTemplate.update(sql);
    }
}
```

## 故障排除

### 1. 数据库连接失败

**问题**: `Connection refused` 或 `Could not connect to database`

**解决方案**:
```bash
# 检查 PostgreSQL 是否运行
docker-compose ps

# 查看日志
docker-compose logs postgres

# 重启服务
docker-compose restart postgres

# 测试连接
psql -h localhost -U postgres -d spring_ai_db
```

### 2. 表未创建

**问题**: `relation "ai_chat_memory" does not exist`

**解决方案**:
```yaml
# 在 application.yml 中启用自动初始化
spring:
  ai:
    chat:
      memory:
        repository:
          jdbc:
            initialize-schema: always
```

或手动创建表：
```sql
CREATE TABLE ai_chat_memory (
    conversation_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. pgvector 扩展未安装

**问题**: `extension "vector" does not exist`

**解决方案**:
```bash
# 连接数据库
docker exec -it spring-agent-postgres psql -U postgres -d spring_ai_db

# 创建扩展
CREATE EXTENSION IF NOT EXISTS vector;

# 验证
\dx
```

### 4. 内存泄漏

**问题**: 长时间运行后内存占用过高

**解决方案**:
```yaml
# 限制消息窗口大小
chat:
  memory:
    max-messages: 10  # 减小窗口大小

# 定期清理旧对话
```

### 5. 查询性能问题

**问题**: 查询响应缓慢

**解决方案**:
```sql
-- 添加索引
CREATE INDEX idx_conversation_id ON ai_chat_memory(conversation_id);
CREATE INDEX idx_created_at ON ai_chat_memory(created_at);

-- 分析表
ANALYZE ai_chat_memory;

-- 查看查询计划
EXPLAIN ANALYZE 
SELECT * FROM ai_chat_memory 
WHERE conversation_id = 'user-123';
```

### 6. 字符编码问题

**问题**: 中文或特殊字符显示异常

**解决方案**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/spring_ai_db?characterEncoding=UTF-8
```

```sql
-- 检查数据库编码
SELECT datname, pg_encoding_to_char(encoding) 
FROM pg_database 
WHERE datname = 'spring_ai_db';

-- 创建数据库时指定编码
CREATE DATABASE spring_ai_db 
ENCODING 'UTF8' 
LC_COLLATE='en_US.UTF-8' 
LC_CTYPE='en_US.UTF-8';
```

## 总结

本指南详细介绍了如何在 Spring AI 中集成 PostgreSQL + pgvector 作为 ChatMemory 的持久化存储。主要优势包括：

✅ **持久化**: 对话历史永久保存，应用重启不丢失
✅ **分布式**: 多实例共享同一存储
✅ **可扩展**: 支持大规模并发对话
✅ **可观察**: 方便监控和调试
✅ **灵活性**: 支持自定义查询和数据清理策略

通过遵循本指南的最佳实践，你可以构建一个稳定、高效的对话系统！

## 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring AI Chat Memory API](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [pgvector GitHub](https://github.com/pgvector/pgvector)
- [项目 GitHub](https://github.com/yourusername/spring-agent)

