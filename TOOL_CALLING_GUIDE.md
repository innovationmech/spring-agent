# Spring AI Tool Calling 集成指南

本项目已成功集成Spring AI Tool Calling功能，让AI助手能够自动调用各种工具来回答用户的问题。

## 🛠️ 可用工具

### 1. DateTimeTools (时间日期工具)
提供以下功能：
- 获取当前日期和时间
- 获取指定时区的当前时间
- 计算两个日期之间的天数差
- 在指定日期上添加天数
- 格式化日期时间
- 时间戳转换
- 判断闰年
- 获取月份天数

**使用示例：**
```
用户: 现在几点了？
AI: [自动调用getCurrentDateTime工具] 现在是 2024-01-15T14:30:25

用户: 2024年1月1日到今天有多少天？
AI: [自动调用calculateDaysBetween工具] 从 2024-01-01 到 2024-01-15 相差 14 天
```

### 2. CalculatorTools (计算器工具)
提供以下功能：
- 基本数学运算（加减乘除）
- 幂运算
- 平方根计算
- 对数计算
- 三角函数（sin, cos, tan）
- 平均值、总和、最大值、最小值计算
- 阶乘计算
- 百分比计算
- 单位转换（长度、温度）

**使用示例：**
```
用户: 计算 25 + 17 等于多少？
AI: [自动调用calculate工具] 25 + 17 = 42

用户: 16的平方根是多少？
AI: [自动调用squareRoot工具] √16 = 4

用户: 100公里等于多少米？
AI: [自动调用convertLength工具] 100 km = 100000 m
```

### 3. SystemInfoTools (系统信息工具)
提供以下功能：
- 系统健康状态检查
- JVM内存使用情况
- 操作系统信息
- Java运行时信息
- 系统环境变量
- 系统属性
- 垃圾回收
- 磁盘使用情况
- CPU信息
- 线程信息

**使用示例：**
```
用户: 检查系统健康状态
AI: [自动调用getSystemHealth工具] 系统健康状态检查:
  内存使用率: 45.2%
  系统负载: 2.1
  可用处理器: 8
  JVM运行时间: 1小时25分钟30秒
  总体状态: 健康

用户: 显示JVM内存使用情况
AI: [自动调用getMemoryUsage工具] JVM内存使用情况:
  堆内存:
    已使用: 512.00 MB
    最大值: 1024.00 MB
    使用率: 50.00%
```

## 🚀 使用方法

### 1. 启动应用
确保Ollama服务正在运行，然后启动Spring Boot应用：

```bash
./gradlew bootRun
```

### 2. 通过API调用

#### 普通聊天
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "现在几点了？",
    "conversationId": "user123"
  }'
```

#### 流式聊天
```bash
curl -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 123 * 456",
    "conversationId": "user123"
  }'
```

#### 带自定义系统提示
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我计算圆的面积",
    "conversationId": "user123",
    "systemPrompt": "你是一个数学助手，请使用计算器工具来回答问题",
    "enableTools": true
  }'
```

### 3. 请求参数说明

```json
{
  "message": "用户消息（必需）",
  "conversationId": "对话ID（可选）",
  "systemPrompt": "自定义系统提示（可选）",
  "enableTools": "是否启用工具调用（可选，默认true）",
  "allowedToolNames": ["允许的工具名称数组（可选）"]
}
```

### 4. 响应格式

```json
{
  "response": "AI回答内容",
  "conversationId": "对话ID",
  "timestamp": "响应时间戳",
  "streaming": "是否为流式响应",
  "toolsUsed": "是否使用了工具",
  "toolsInvoked": ["调用的工具名称列表"],
  "toolResults": {"工具执行结果"}
}
```

## 🧪 测试

运行集成测试来验证工具功能：

```bash
./gradlew test --tests ToolCallingIntegrationTest
```

测试包括：
- 时间工具测试
- 计算器工具测试
- 系统信息工具测试
- 混合工具使用测试
- 流式响应测试
- 错误处理测试
- 上下文记忆测试

## 🔧 自定义工具

要添加新的工具，请按照以下步骤：

### 1. 创建工具类
```java
@Component
public class MyCustomTools {

    @Tool(description = "工具描述")
    public String myCustomFunction(
            @ToolParam(description = "参数描述") String param) {
        // 工具实现逻辑
        return "结果";
    }
}
```

### 2. 注册工具
在 `ChatClientConfig.java` 中添加工具：

```java
@Bean
public Object[] toolObjects(DateTimeTools dateTimeTools,
                           CalculatorTools calculatorTools,
                           SystemInfoTools systemInfoTools,
                           MyCustomTools myCustomTools) {
    return new Object[]{
        dateTimeTools,
        calculatorTools,
        systemInfoTools,
        myCustomTools
    };
}
```

### 3. 重启应用
重启Spring Boot应用，新工具将自动可用。

## 🎯 最佳实践

1. **清晰的工具描述**：为每个工具提供清晰、准确的描述
2. **参数说明**：为每个参数提供详细的说明
3. **错误处理**：在工具中添加适当的错误处理
4. **返回格式**：保持返回结果格式的一致性
5. **测试覆盖**：为新工具编写充分的测试

## 📝 注意事项

1. 确保Ollama服务正在运行
2. 工具调用可能增加响应时间
3. 某些系统信息工具可能需要相应权限
4. 在生产环境中注意敏感信息的保护

## 🔍 故障排除

### 常见问题

1. **工具不工作**
   - 检查工具类是否正确注册为Spring Bean
   - 确认方法上有 `@Tool` 注解
   - 检查参数是否有 `@ToolParam` 注解

2. **响应慢**
   - 工具调用可能增加处理时间
   - 检查系统资源使用情况
   - 考虑优化工具实现

3. **错误信息**
   - 查看应用日志获取详细错误信息
   - 检查工具方法是否正确实现
   - 验证参数格式是否正确

## 📚 更多资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Ollama 文档](https://ollama.com/docs)