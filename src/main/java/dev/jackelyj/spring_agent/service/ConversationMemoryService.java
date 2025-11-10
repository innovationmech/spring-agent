package dev.jackelyj.spring_agent.service;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 对话记忆管理服务接口
 * 
 * SOLID Principles:
 * - SRP: 专门管理对话记忆相关操作
 * - ISP: 提供客户端需要的记忆管理方法
 * - DIP: 高层服务依赖此抽象接口
 */
public interface ConversationMemoryService {

    /**
     * 清除指定对话的记忆
     *
     * @param conversationId 对话ID
     * @return 操作是否成功
     */
    boolean clearConversation(String conversationId);

    /**
     * 清除所有对话记忆
     *
     * @return 操作是否成功
     */
    boolean clearAllConversations();
    
    /**
     * 获取会话消息数量
     *
     * @param conversationId 对话ID
     * @return 消息数量
     */
    int getConversationMessageCount(String conversationId);
    
    /**
     * 获取所有会话 ID 列表
     *
     * @return 会话 ID 列表
     */
    List<String> getAllConversationIds();
    
    /**
     * 导出会话历史（用于数据迁移或备份）
     *
     * @param conversationId 对话ID
     * @return 消息列表
     */
    List<Message> exportConversation(String conversationId);
    
    /**
     * 检查会话是否存在
     *
     * @param conversationId 对话ID
     * @return 是否存在
     */
    boolean conversationExists(String conversationId);

    /**
     * 检查对话记忆服务是否可用
     *
     * @return 服务状态
     */
    boolean isHealthy();
}