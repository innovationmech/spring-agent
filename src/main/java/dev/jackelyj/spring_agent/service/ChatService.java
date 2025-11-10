package dev.jackelyj.spring_agent.service;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天服务接口
 * 遵循依赖倒置原则，定义聊天业务的核心操作
 */
public interface ChatService {

    /**
     * 执行普通聊天对话
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 执行流式聊天对话
     *
     * @param request 聊天请求
     * @return 流式聊天响应
     */
    Flux<ChatResponse> chatStream(ChatRequest request);

    /**
     * 清除指定对话的记忆
     *
     * @param conversationId 对话ID
     * @return 操作结果
     */
    boolean clearConversation(String conversationId);

    /**
     * 检查服务是否可用
     *
     * @return 服务状态
     */
    boolean isHealthy();
}