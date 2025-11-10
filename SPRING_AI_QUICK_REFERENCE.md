# Spring AI ChatClient & Tool Calling 快速参考

## 🚀 快速开始

### 基础配置
```java
@Configuration
public class QuickConfig {

    @Bean
    public ChatClient chatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个有用的AI助手")
                .build();
    }
}
```

### 基础使用
```java
@Service
public class ChatService {
    private final ChatClient chatClient;

    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

## 🛠️ Tool Calling 快速配置

### 1. 定义工具
```java
@Component
public class QuickTools {

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "计算数学表达式")
    public String calculate(
            @ToolParam(description = "第一个数字") double a,
            @ToolParam(description = "操作符 (+,-,*,/)") String op,
            @ToolParam(description = "第二个数字") double b) {
        return switch (op) {
            case "+" -> String.valueOf(a + b);
            case "-" -> String.valueOf(a - b);
            case "*" -> String.valueOf(a * b);
            case "/" -> b != 0 ? String.valueOf(a / b) : "除数不能为零";
            default -> "不支持的操作符";
        };
    }
}
```

### 2. 注册工具
```java
@Configuration
public class ToolConfig {

    @Bean
    public Object[] toolObjects(QuickTools quickTools) {
        return new Object[]{quickTools};
    }

    @Bean
    public ChatClient chatClientWithTools(OllamaChatModel chatModel, Object[] toolObjects) {
        return ChatClient.builder(chatModel)
                .defaultTools(toolObjects)
                .defaultSystem("你可以使用工具来帮助用户")
                .build();
    }
}
```

## 📋 常用配置模式

### 带对话记忆
```java
@Bean
public ChatClient chatClientWithMemory(OllamaChatModel chatModel, ChatMemory chatMemory) {
    return ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
}
```

### 多工具配置
```java
@Bean
public ChatClient multiToolChatClient(OllamaChatModel chatModel,
                                     DateTimeTools dateTools,
                                     CalculatorTools calcTools,
                                     SystemInfoTools systemTools) {
    return ChatClient.builder(chatModel)
            .defaultTools(dateTools, calcTools, systemTools)
            .build();
}
```

### 流式响应
```java
public Flux<String> chatStream(String message) {
    return chatClient.prompt()
            .user(message)
            .stream()
            .content();
}
```

## 🎯 工具定义模板

### 基础工具模板
```java
@Tool(description = "简洁明确的工具功能描述")
public String toolMethodName(
        @ToolParam(description = "参数1描述") String param1,
        @ToolParam(description = "参数2描述") int param2) {
    try {
        // 工具逻辑实现
        return "执行结果";
    } catch (Exception e) {
        return "错误：" + e.getMessage();
    }
}
```

### 复杂数据处理工具
```java
@Tool(description = "处理复杂计算任务")
public String complexCalculation(
        @ToolParam(description = "输入数据数组") String[] dataArray,
        @ToolParam(description = "计算类型：sum, avg, max, min") String calculationType) {

    double[] numbers = Arrays.stream(dataArray)
            .mapToDouble(Double::parseDouble)
            .toArray();

    return switch (calculationType.toLowerCase()) {
        case "sum" -> "总和：" + Arrays.stream(numbers).sum();
        case "avg" -> "平均值：" + Arrays.stream(numbers).average().orElse(0);
        case "max" -> "最大值：" + Arrays.stream(numbers).max().orElse(0);
        case "min" -> "最小值：" + Arrays.stream(numbers).min().orElse(0);
        default -> "不支持的计算类型";
    };
}
```

## 🔧 运行时工具控制

### 动态工具选择
```java
public String dynamicToolChat(String message) {
    var prompt = chatClient.prompt().user(message);

    // 根据消息内容选择工具
    if (message.contains("时间") || message.contains("日期")) {
        prompt.tools(new DateTimeTools());
    } else if (message.contains("计算")) {
        prompt.tools(new CalculatorTools());
    }

    return prompt.call().content();
}
```

### 指定工具名称
```java
public String specificToolChat(String message, String... toolNames) {
    return chatClient.prompt()
            .user(message)
            .toolNames(toolNames)
            .call()
            .content();
}
```

## 📊 常用工具类型

### 时间日期工具
```java
@Tool(description = "获取当前日期")
public String getCurrentDate() {
    return LocalDate.now().toString();
}

@Tool(description = "计算日期差")
public String dateDifference(
        @ToolParam(description = "开始日期 yyyy-MM-dd") String start,
        @ToolParam(description = "结束日期 yyyy-MM-dd") String end) {
    long days = ChronoUnit.DAYS.between(
            LocalDate.parse(start),
            LocalDate.parse(end)
    );
    return "相差天数：" + days;
}
```

### 数学计算工具
```java
@Tool(description = "平方根计算")
public String squareRoot(@ToolParam(description = "数字") double number) {
    if (number < 0) return "不能计算负数的平方根";
    return "√" + number + " = " + Math.sqrt(number);
}

@Tool(description = "幂运算")
public String power(
        @ToolParam(description = "基数") double base,
        @ToolParam(description = "指数") double exponent) {
    return base + "^" + exponent + " = " + Math.pow(base, exponent);
}
```

### 系统信息工具
```java
@Tool(description = "获取系统内存使用情况")
public String getMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long total = runtime.totalMemory();
    long free = runtime.freeMemory();
    long used = total - free;
    double usagePercent = (double) used / total * 100;

    return String.format("内存使用：%,.2f MB / %,.2f MB (%.1f%%)",
            used / 1024 / 1024, total / 1024 / 1024, usagePercent);
}
```

## 🎨 高级配置示例

### 多模型配置
```java
@Configuration
public class MultiModelConfig {

    @Bean("creativeChatClient")
    public ChatClient creativeChatClient(OpenAiChatModel gpt4) {
        return ChatClient.builder(gpt4)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.9)
                        .build())
                .defaultSystem("你是创意助手，思维活跃")
                .build();
    }

    @Bean("preciseChatClient")
    public ChatClient preciseChatClient(OpenAiChatModel gpt4) {
        return ChatClient.builder(gpt4)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.1)
                        .build())
                .defaultSystem("你是精确助手，追求准确性")
                .build();
    }
}
```

### 自定义Advisor
```java
public class LoggingAdvisor implements Advisor {

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        logger.info("处理请求：{}", request.getUserText());
        return request;
    }

    @Override
    public AdvisedResponse adviseResponse(AdvisedResponse response, Map<String, Object> context) {
        logger.info("生成响应：{}", response.getResponse().getResult().getOutput().getText());
        return response;
    }
}
```

## 🐛 常见问题解决

### 工具不响应
```java
// 检查清单：
// 1. 工具类是否有 @Component 注解
// 2. 方法是否有 @Tool 注解和描述
// 3. 参数是否有 @ToolParam 注解和描述
// 4. ChatClient 是否正确注册了工具
```

### 配置错误
```java
// 正确的工具注册方式
@Bean
public ChatClient chatClient(OllamaChatModel model, MyTools tools) {
    return ChatClient.builder(model)
            .defaultTools(tools) // ✅ 正确
            .build();
}

// 错误的方式
@Bean
public ChatClient chatClient(OllamaChatModel model) {
    return ChatClient.builder(model)
            .defaultToolCallbacks(myToolCallback) // ❌ 过时的方法
            .build();
}
```

## 📱 API 使用示例

### REST Controller
```java
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public String chat(@RequestBody ChatRequest request) {
        return chatClient.prompt()
                .user(request.getMessage())
                .call()
                .content();
    }

    @PostMapping("/stream")
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return chatClient.prompt()
                .user(request.getMessage())
                .stream()
                .content();
    }
}
```

### 请求DTO
```java
public class ChatRequest {
    private String message;
    private String conversationId;
    private String systemPrompt;
    private String[] allowedTools;

    // getters and setters
}
```

## 🎯 最佳实践要点

1. **工具描述要清晰**：让AI知道何时使用工具
2. **参数说明要详细**：包含格式和约束条件
3. **错误处理要完善**：提供有用的错误信息
4. **性能要考虑**：缓存频繁调用的工具结果
5. **日志要记录**：便于调试和监控

---

这个快速参考手册涵盖了Spring AI ChatClient和Tool Calling的核心用法。保存它作为日常开发的参考指南！