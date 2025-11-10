package dev.jackelyj.spring_agent.service;

/**
 * 对话记忆管理服务接口
 * 遵循单一职责原则，专门管理对话记忆相关操作
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
     * 检查对话记忆服务是否可用
     *
     * @return 服务状态
     */
    boolean isHealthy();
}