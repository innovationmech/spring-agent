package dev.jackelyj.spring_agent.demo;

import dev.jackelyj.spring_agent.dto.ChatRequest;
import dev.jackelyj.spring_agent.dto.ChatResponse;
import dev.jackelyj.spring_agent.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ToolCallingDemo implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallingDemo.class);

    @Autowired
    private ChatService chatService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Spring AI Tool Calling Demo ===");

        // 演示时间工具
        demonstrateDateTimeTools();

        // 演示计算器工具
        demonstrateCalculatorTools();

        // 演示系统信息工具
        demonstrateSystemInfoTools();

        // 演示混合工具使用
        demonstrateMixedToolUsage();

        logger.info("=== Demo completed ===");
    }

    private void demonstrateDateTimeTools() {
        logger.info("\n--- DateTime Tools Demo ---");

        ChatRequest timeRequest = new ChatRequest("现在几点了？", "demo-datetime");
        ChatResponse timeResponse = chatService.chat(timeRequest);
        logger.info("问: 现在几点了？");
        logger.info("答: {}", timeResponse.getResponse());

        ChatRequest dateRequest = new ChatRequest("2024年春节是哪一天？", "demo-datetime");
        ChatResponse dateResponse = chatService.chat(dateRequest);
        logger.info("问: 2024年春节是哪一天？");
        logger.info("答: {}", dateResponse.getResponse());
    }

    private void demonstrateCalculatorTools() {
        logger.info("\n--- Calculator Tools Demo ---");

        ChatRequest calcRequest = new ChatRequest("计算 123 + 456 等于多少？", "demo-calculator");
        ChatResponse calcResponse = chatService.chat(calcRequest);
        logger.info("问: 计算 123 + 456 等于多少？");
        logger.info("答: {}", calcResponse.getResponse());

        ChatRequest sqrtRequest = new ChatRequest("计算 144 的平方根", "demo-calculator");
        ChatResponse sqrtResponse = chatService.chat(sqrtRequest);
        logger.info("问: 计算 144 的平方根");
        logger.info("答: {}", sqrtResponse.getResponse());

        ChatRequest avgRequest = new ChatRequest("计算数字 [10, 20, 30, 40, 50] 的平均值和总和", "demo-calculator");
        ChatResponse avgResponse = chatService.chat(avgRequest);
        logger.info("问: 计算数字 [10, 20, 30, 40, 50] 的平均值和总和");
        logger.info("答: {}", avgResponse.getResponse());
    }

    private void demonstrateSystemInfoTools() {
        logger.info("\n--- System Info Tools Demo ---");

        ChatRequest healthRequest = new ChatRequest("检查系统健康状态", "demo-system");
        ChatResponse healthResponse = chatService.chat(healthRequest);
        logger.info("问: 检查系统健康状态");
        logger.info("答: {}", healthResponse.getResponse());

        ChatRequest memoryRequest = new ChatRequest("显示JVM内存使用情况", "demo-system");
        ChatResponse memoryResponse = chatService.chat(memoryRequest);
        logger.info("问: 显示JVM内存使用情况");
        logger.info("答: {}", memoryResponse.getResponse());

        ChatRequest systemRequest = new ChatRequest("获取操作系统和Java运行时信息", "demo-system");
        ChatResponse systemResponse = chatService.chat(systemRequest);
        logger.info("问: 获取操作系统和Java运行时信息");
        logger.info("答: {}", systemResponse.getResponse());
    }

    private void demonstrateMixedToolUsage() {
        logger.info("\n--- Mixed Tool Usage Demo ---");

        ChatRequest mixedRequest = new ChatRequest(
                "请帮我完成以下任务：\n" +
                "1. 告诉我当前的时间\n" +
                "2. 计算 3.14159 * 100\n" +
                "3. 检查系统内存使用率\n" +
                "4. 如果内存使用率超过80%，执行垃圾回收",
                "demo-mixed"
        );
        ChatResponse mixedResponse = chatService.chat(mixedRequest);
        logger.info("问: 多任务复合请求");
        logger.info("答: {}", mixedResponse.getResponse());

        // 测试单位转换
        ChatRequest conversionRequest = new ChatRequest(
                "帮我转换单位：\n" +
                "1. 100公里等于多少米？\n" +
                "2. 25摄氏度等于多少华氏度？",
                "demo-conversion"
        );
        ChatResponse conversionResponse = chatService.chat(conversionRequest);
        logger.info("问: 单位转换请求");
        logger.info("答: {}", conversionResponse.getResponse());
    }
}