# Spring AI ChatClient 配置与 Tool Calling 集成完整指南

本文档详细介绍如何配置Spring AI的ChatClient以及如何集成和使用Tool Calling功能，基于最新的Spring AI文档和最佳实践。

## 📋 目录

1. [Spring AI ChatClient 概述](#spring-ai-chatclient-概述)
2. [基础配置](#基础配置)
3. [高级配置选项](#高级配置选项)
4. [Tool Calling 基础](#tool-calling-基础)
5. [工具定义方式](#工具定义方式)
6. [工具注册与集成](#工具注册与集成)
7. [运行时工具控制](#运行时工具控制)
8. [Advisor 集成](#advisor-集成)
9. [多模型配置](#多模型配置)
10. [最佳实践](#最佳实践)
11. [故障排除](#故障排除)

## 🎯 Spring AI ChatClient 概述

Spring AI ChatClient是Spring AI框架的核心组件，提供了与各种大语言模型交互的统一接口。它支持：

- **多种模型提供商**：OpenAI、Anthropic、Google Gemini、Ollama等
- **流式和非流式对话**
- **工具调用（Function Calling）**
- **对话记忆管理**
- **可扩展的Advisor链**

## 🏗️ 基础配置

### 1. 依赖配置

首先添加必要的Spring AI依赖（以Ollama为例）：

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
    <version>1.0.3</version>
</dependency>
```

```gradle
// build.gradle
implementation 'org.springframework.ai:spring-ai-starter-model-ollama:1.0.3'
```

### 2. 应用配置

```yaml
# application.yml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: gpt-oss
        options:
          temperature: 0.7
          max-tokens: 1000
          top-p: 0.9
```

### 3. 基础ChatClient配置

```java
@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.ollama.chat.options.model:gpt-oss}")
    private String modelName;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private Double temperature;

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model(modelName)
                        .temperature(temperature)
                        .build())
                .defaultSystem("You are a helpful AI assistant.")
                .build();
    }
}
```

## 🔧 高级配置选项

### 1. 带对话记忆的配置

```java
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model("gpt-oss")
                        .temperature(0.7)
                        .build())
                .defaultSystem("You are a helpful AI assistant with memory.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
```

### 2. 多ChatClient配置

```java
@Configuration
public class MultiChatClientConfig {

    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model("gpt-oss")
                        .temperature(0.5)
                        .build())
                .build();
    }
}
```

### 3. 使用不同ChatClient

```java
@Service
public class ChatService {

    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;

    public ChatService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                      @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {
        this.openAiChatClient = openAiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    public String chatWithOpenAI(String message) {
        return openAiChatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public String chatWithOllama(String message) {
        return ollamaChatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

### 4. 自定义模板分隔符

```java
@Bean
public ChatClient chatClientWithCustomDelimiters(OllamaChatModel chatModel) {
    return ChatClient.builder(chatModel)
            .templateRenderer(StTemplateRenderer.builder()
                    .startDelimiterToken('<')
                    .endDelimiterToken('>')
                    .build())
            .build();
}

// 使用自定义分隔符
String response = chatClientWithCustomDelimiters.prompt()
        .user("Tell me about <topic>")
        .param("topic", "Spring AI")
        .call()
        .content();
```

## 🛠️ Tool Calling 基础

Tool Calling（函数调用）允许AI模型调用外部工具来获取信息或执行操作，这是构建智能助手的关键功能。

### 1. 什么是Tool Calling？

Tool Calling允许AI模型：
- 调用预定义的Java方法
- 获取实时数据（天气、时间、系统信息等）
- 执行计算和数据处理
- 与外部系统交互
- 提供准确、最新的信息

### 2. 核心组件

- **Tool Callback**：工具回调接口
- **Tool Definition**：工具定义
- **Tool Metadata**：工具元数据
- **Tool Calling Manager**：工具调用管理器
- **Tool Calling Chat Options**：工具调用配置

## 🎯 工具定义方式

Spring AI支持多种工具定义方式：

### 方式1：@Tool 注解（推荐）

```java
@Component
public class DateTimeTools {

    @Tool(description = "获取当前日期和时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "计算两个日期之间的天数差")
    public String calculateDaysBetween(
            @ToolParam(description = "开始日期，格式：yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式：yyyy-MM-dd") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, Math.abs(days));
        } catch (Exception e) {
            return "日期格式错误，请使用 yyyy-MM-dd 格式";
        }
    }
}
```

### 方式2：Function Bean

```java
@Configuration
public class CalculatorTools {

    @Bean
    @Description("执行基本数学运算")
    public Function<MathRequest, MathResponse> calculateFunction() {
        return request -> {
            double result = switch (request.operator()) {
                case "+" -> request.a() + request.b();
                case "-" -> request.a() - request.b();
                case "*" -> request.a() * request.b();
                case "/" -> request.b() != 0 ? request.a() / request.b() : Double.NaN;
                default -> throw new IllegalArgumentException("不支持的操作符: " + request.operator());
            };
            return new MathResponse(result);
        };
    }

    public record MathRequest(double a, String operator, double b) {}
    public record MathResponse(double result) {}
}
```

### 方式3：FunctionToolCallback（程序化）

```java
@Configuration
public class WeatherTools {

    @Bean
    public ToolCallback weatherToolCallback() {
        return FunctionToolCallback
                .builder("getWeather", new WeatherService())
                .description("获取指定地点的天气信息")
                .inputType(WeatherRequest.class)
                .build();
    }

    public static class WeatherService implements Function<WeatherRequest, WeatherResponse> {
        @Override
        public WeatherResponse apply(WeatherRequest request) {
            // 模拟天气服务
            double temperature = request.location().contains("北京") ? 15.0 : 20.0;
            return new WeatherResponse(temperature, "晴朗", request.location());
        }
    }

    public record WeatherRequest(String location) {}
    public record WeatherResponse(double temperature, String condition, String location) {}
}
```

## 🔗 工具注册与集成

### 1. 在ChatClient中注册默认工具

```java
@Configuration
public class ToolCallingConfig {

    @Bean
    public Object[] toolObjects(DateTimeTools dateTimeTools,
                               CalculatorTools calculatorTools,
                               WeatherTools weatherTools) {
        return new Object[]{dateTimeTools, calculatorTools, weatherTools};
    }

    @Bean
    public ChatClient chatClientWithTools(OllamaChatModel chatModel,
                                        Object[] toolObjects) {
        return ChatClient.builder(chatModel)
                .defaultOptions(OllamaOptions.builder()
                        .model("gpt-oss")
                        .temperature(0.7)
                        .build())
                .defaultSystem("你是一个智能助手，可以使用各种工具来帮助用户。")
                .defaultTools(toolObjects)
                .build();
    }
}
```

### 2. 使用工具名称注册

```java
@Bean
public ChatClient chatClientWithToolNames(OllamaChatModel chatModel) {
    return ChatClient.builder(chatModel)
            .defaultToolNames("getCurrentDateTime", "calculateDaysBetween", "getWeather")
            .build();
}
```

### 3. 使用工具回调注册

```java
@Bean
public ChatClient chatClientWithToolCallbacks(OllamaChatModel chatModel,
                                            List<ToolCallback> toolCallbacks) {
    return ChatClient.builder(chatModel)
            .defaultToolCallbacks(toolCallbacks.toArray(new ToolCallback[0]))
            .build();
}
```

## ⚡ 运行时工具控制

### 1. 请求级别的工具控制

```java
@Service
public class DynamicToolService {

    private final ChatClient chatClient;

    public DynamicToolService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chatWithSpecificTools(String message, String... toolNames) {
        return chatClient.prompt()
                .user(message)
                .toolNames(toolNames)
                .call()
                .content();
    }

    public String chatWithToolObjects(String message, Object... tools) {
        return chatClient.prompt()
                .user(message)
                .tools(tools)
                .call()
                .content();
    }
}
```

### 2. 条件性工具使用

```java
@Service
public class ConditionalToolService {

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final CalculatorTools calculatorTools;

    public String processRequest(String message) {
        var promptBuilder = chatClient.prompt().user(message);

        // 根据消息内容动态选择工具
        if (message.contains("时间") || message.contains("日期")) {
            promptBuilder.tools(dateTimeTools);
        } else if (message.contains("计算") || message.contains("数学")) {
            promptBuilder.tools(calculatorTools);
        } else {
            // 使用所有默认工具
            // promptBuilder 不需要额外配置
        }

        return promptBuilder.call().content();
    }
}
```

### 3. 工具执行控制

```java
@Service
public class ControlledToolExecutionService {

    private final ChatModel chatModel;
    private final ToolCallingManager toolCallingManager;

    public String manualToolExecution(String message) {
        // 禁用内部工具执行，手动控制
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(new DateTimeTools())
                .internalToolExecutionEnabled(false)
                .build();

        Prompt prompt = new Prompt(message, chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);

        // 手动执行工具调用
        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);
            prompt = new Prompt(result.conversationHistory(), chatOptions);
            chatResponse = chatModel.call(prompt);
        }

        return chatResponse.getResult().getOutput().getText();
    }
}
```

## 🎨 Advisor 集成

### 1. ToolCallAdvisor 配置

```java
@Configuration
public class AdvisorConfig {

    @Bean
    public ToolCallAdvisor toolCallAdvisor(ToolCallingManager toolCallingManager) {
        return ToolCallAdvisor.builder()
                .toolCallingManager(toolCallingManager)
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                .build();
    }

    @Bean
    public ChatClient chatClientWithAdvisors(OllamaChatModel chatModel,
                                           ChatMemory chatMemory,
                                           ToolCallAdvisor toolCallAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        toolCallAdvisor
                )
                .defaultTools(new DateTimeTools(), new CalculatorTools())
                .build();
    }
}
```

### 2. 自定义Advisor

```java
public class SecurityToolAdvisor implements Advisor {

    private final SecurityService securityService;

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        // 检查用户权限
        String userId = (String) context.get("userId");
        if (!securityService.hasToolPermission(userId, request)) {
            throw new SecurityException("用户没有工具调用权限");
        }
        return request;
    }

    @Override
    public AdvisedResponse adviseResponse(AdvisedResponse response, Map<String, Object> context) {
        // 记录工具调用日志
        if (response.getResponse().getResult().getOutput().getText().contains("工具调用")) {
            String userId = (String) context.get("userId");
            logToolCall(userId, response);
        }
        return response;
    }
}
```

## 🌐 多模型配置

### 1. 使用mutate()方法创建多个模型

```java
@Service
public class MultiModelService {

    private final ChatClient gpt4Client;
    private final ChatClient claudeClient;
    private final ChatClient llamaClient;

    public MultiModelService(OpenAiChatModel baseOpenAiModel,
                           AnthropicChatModel baseAnthropicModel,
                           OllamaChatModel baseOllamaModel) {

        // GPT-4 配置
        this.gpt4Client = ChatClient.builder(baseOpenAiModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .build();

        // Claude 配置
        this.claudeClient = ChatClient.builder(baseAnthropicModel)
                .defaultOptions(AnthropicChatOptions.builder()
                        .model("claude-3-sonnet-20240229")
                        .temperature(0.5)
                        .build())
                .build();

        // Llama 配置
        this.llamaClient = ChatClient.builder(baseOllamaModel)
                .defaultOptions(OllamaOptions.builder()
                        .model("llama2")
                        .temperature(0.8)
                        .build())
                .build();
    }

    public String chatWithModel(String message, String modelType) {
        return switch (modelType.toLowerCase()) {
            case "gpt4" -> gpt4Client.prompt().user(message).call().content();
            case "claude" -> claudeClient.prompt().user(message).call().content();
            case "llama" -> llamaClient.prompt().user(message).call().content();
            default -> throw new IllegalArgumentException("不支持的模型类型: " + modelType);
        };
    }
}
```

### 2. 模型特定工具配置

```java
@Configuration
public class ModelSpecificToolConfig {

    @Bean("gpt4ChatClient")
    public ChatClient gpt4ChatClient(OpenAiChatModel openAiChatModel,
                                   DateTimeTools dateTimeTools,
                                   CalculatorTools calculatorTools) {
        return ChatClient.builder(openAiChatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .defaultTools(dateTimeTools, calculatorTools)
                .defaultSystem("你是GPT-4助手，擅长处理复杂任务。")
                .build();
    }

    @Bean("claudeChatClient")
    public ChatClient claudeChatClient(AnthropicChatModel anthropicChatModel,
                                     SystemInfoTools systemInfoTools) {
        return ChatClient.builder(anthropicChatModel)
                .defaultOptions(AnthropicChatOptions.builder()
                        .model("claude-3-sonnet-20240229")
                        .temperature(0.5)
                        .build())
                .defaultTools(systemInfoTools)
                .defaultSystem("你是Claude助手，专注于系统信息查询。")
                .build();
    }
}
```

## 📚 最佳实践

### 1. 工具设计原则

```java
@Component
public class BestPracticeTools {

    // ✅ 好的示例：清晰的描述和参数
    @Tool(description = "计算两个数字的乘法结果")
    public String multiplyNumbers(
            @ToolParam(description = "第一个乘数") double a,
            @ToolParam(description = "第二个乘数") double b) {
        return String.format("%.2f × %.2f = %.2f", a, b, a * b);
    }

    // ❌ 避免的示例：描述不清晰
    @Tool(description = "计算")
    public String calc(double x, double y) {
        return String.valueOf(x + y);
    }
}
```

### 2. 错误处理

```java
@Component
public class RobustTools {

    @Tool(description = "安全地执行除法运算")
    public String safeDivision(
            @ToolParam(description = "被除数") double dividend,
            @ToolParam(description = "除数") double divisor) {
        try {
            if (divisor == 0) {
                return "错误：除数不能为零";
            }
            double result = dividend / divisor;
            return String.format("%.2f ÷ %.2f = %.2f", dividend, divisor, result);
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "验证并解析日期")
    public String parseDate(
            @ToolParam(description = "日期字符串，支持多种格式") String dateString) {
        try {
            // 尝试多种日期格式
            List<DateTimeFormatter> formatters = List.of(
                    DateTimeFormatter.ISO_LOCAL_DATE,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")
            );

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDate date = LocalDate.parse(dateString, formatter);
                    return "成功解析日期：" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException ignored) {
                    // 继续尝试下一个格式
                }
            }

            return "无法解析日期：" + dateString + "，支持的格式：yyyy-MM-dd, MM/dd/yyyy, dd-MM-yyyy";
        } catch (Exception e) {
            return "日期解析错误：" + e.getMessage();
        }
    }
}
```

### 3. 工具组合使用

```java
@Service
public class ToolCompositionService {

    private final ChatClient chatClient;

    public String comprehensiveAnalysis(String location) {
        return chatClient.prompt()
                .user(String.format(
                        "请对地点 %s 进行全面分析：\n" +
                        "1. 获取当前时间\n" +
                        "2. 计算距离周末还有多少天\n" +
                        "3. 如果是工作日，计算工作时长\n" +
                        "4. 检查系统状态是否适合处理请求",
                        location
                ))
                .tools(new DateTimeTools(), new CalculatorTools(), new SystemInfoTools())
                .call()
                .content();
    }
}
```

### 4. 性能优化

```java
@Configuration
public class PerformanceConfig {

    // 缓存频繁调用的工具结果
    @Bean
    @Cacheable("weatherCache")
    public ToolCallback cachedWeatherTool() {
        return FunctionToolCallback
                .builder("getCachedWeather", new CachedWeatherService())
                .description("获取缓存的天气信息（5分钟有效期）")
                .inputType(WeatherRequest.class)
                .build();
    }

    // 异步工具执行
    @Bean
    public ToolCallback asyncSystemInfoTool() {
        return new AsyncToolCallback() {
            @Override
            public String call(String input, ToolContext context) {
                return CompletableFuture.supplyAsync(() -> {
                    // 模拟耗时的系统信息获取
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "系统信息获取完成";
                }).join();
            }
        };
    }
}
```

### 5. 监控和日志

```java
@Component
public class ToolMonitoringAdvisor implements Advisor {

    private static final Logger logger = LoggerFactory.getLogger(ToolMonitoringAdvisor.class);

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        logger.info("开始处理请求，用户：{}", context.get("userId"));
        return request;
    }

    @Override
    public AdvisedResponse adviseResponse(AdvisedResponse response, Map<String, Object> context) {
        ChatResponse chatResponse = response.getResponse();

        if (chatResponse.getResult().getOutput().getToolCalls() != null) {
            List<String> toolNames = chatResponse.getResult().getOutput().getToolCalls()
                    .stream()
                    .map(ToolCall::name)
                    .toList();

            logger.info("工具调用完成，使用的工具：{}", toolNames);
        }

        logger.info("请求处理完成，用户：{}", context.get("userId"));
        return response;
    }
}
```

## 🔧 故障排除

### 1. 常见问题

#### 工具不被调用
```java
// 检查工具定义
@Component
public class TroubleshootingTools {

    // ✅ 确保有正确的注解
    @Tool(description = "清晰、准确的工具描述")
    public String workingTool(
            @ToolParam(description = "详细的参数描述") String param) {
        return "工具执行成功";
    }

    // ❌ 常见错误：缺少描述
    @Tool // 缺少description
    public String brokenTool(String param) {
        return "工具不会被调用";
    }
}
```

#### 配置问题
```java
@Configuration
public class TroubleshootingConfig {

    // ✅ 正确的配置方式
    @Bean
    public ChatClient correctChatClient(OllamaChatModel chatModel,
                                      DateTimeTools dateTimeTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(dateTimeTools) // 使用defaultTools而不是defaultToolCallbacks
                .build();
    }

    // ❌ 错误的配置方式
    @Bean
    public ChatClient incorrectChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultTools(new Object()) // 传入错误的对象类型
                .build();
    }
}
```

### 2. 调试技巧

```java
@Service
public class DebuggingService {

    private final ChatClient chatClient;

    public String debugToolCalling(String message) {
        // 启用详细日志
        System.setProperty("spring.ai.chat.client.logging.enabled", "true");

        try {
            ChatResponse response = chatClient.prompt()
                    .user(message)
                    .call()
                    .chatResponse();

            // 检查工具调用情况
            if (response.getResult().getOutput().getToolCalls() != null) {
                logger.info("检测到工具调用：");
                response.getResult().getOutput().getToolCalls().forEach(toolCall -> {
                    logger.info("  工具名称：{}", toolCall.name());
                    logger.info("  工具参数：{}", toolCall.arguments());
                });
            }

            return response.getResult().getOutput().getText();
        } finally {
            // 恢复日志设置
            System.clearProperty("spring.ai.chat.client.logging.enabled");
        }
    }
}
```

### 3. 测试策略

```java
@SpringBootTest
public class ToolCallingIntegrationTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    public void testDateTimeTool() {
        String response = chatClient.prompt()
                .user("现在几点了？")
                .call()
                .content();

        assertThat(response).containsPattern("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    public void testCalculatorTool() {
        String response = chatClient.prompt()
                .user("计算 25 + 17 等于多少？")
                .call()
                .content();

        assertThat(response).contains("42");
    }

    @Test
    public void testToolErrorHandling() {
        String response = chatClient.prompt()
                .user("计算 10 除以 0")
                .call()
                .content();

        assertThat(response).contains("错误");
    }
}
```

## 📖 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [ChatClient API 参考](https://docs.spring.io/spring-ai/api/org/springframework/ai/chat/client/ChatClient.html)
- [Tool Calling 指南](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Spring Boot 最佳实践](https://spring.io/projects/spring-boot)

---

本指南涵盖了Spring AI ChatClient配置和Tool Calling的所有主要方面。通过遵循这些最佳实践和示例，你可以构建出功能强大、性能优秀的AI驱动应用程序。