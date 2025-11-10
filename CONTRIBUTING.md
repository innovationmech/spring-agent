# 贡献指南

感谢您对 Spring Agent 项目的关注！我们欢迎所有形式的贡献，包括但不限于：

- 🐛 Bug 报告
- 🚀 新功能建议
- 📝 文档改进
- 💻 代码贡献
- 🧪 测试用例
- 💡 使用案例分享

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发流程](#开发流程)
- [编码规范](#编码规范)
- [提交规范](#提交规范)
- [Pull Request 流程](#pull-request-流程)
- [测试要求](#测试要求)
- [文档要求](#文档要求)

## 行为准则

### 我们的承诺

为了营造一个开放和友好的环境，我们作为贡献者和维护者承诺：无论年龄、体型、残疾、民族、性别认同和表达、经验水平、国籍、个人形象、种族、宗教或性取向，参与我们项目和社区的每个人都能获得无骚扰的体验。

### 我们的标准

积极行为的例子包括：

- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同情

不可接受的行为包括：

- 使用性别化的语言或图像，以及不受欢迎的性关注或挑逗
- 恶意评论、侮辱/贬损性评论以及人身或政治攻击
- 公开或私下骚扰
- 未经明确许可发布他人的私人信息
- 在专业环境中可被合理认为不适当的其他行为

## 如何贡献

### 报告 Bug

在提交 Bug 报告之前，请：

1. **检查已有 Issues**: 搜索现有的 Issues 以确认问题尚未被报告
2. **使用最新版本**: 确保您使用的是最新版本的代码
3. **提供详细信息**: 使用 Bug 报告模板提供完整的信息

创建 Bug 报告时，请包括：

- 清晰的标题和描述
- 重现步骤
- 预期行为和实际行为
- 环境信息（Java 版本、OS、Ollama 版本等）
- 相关日志和错误信息
- 如果可能，提供最小可重现示例

### 建议新功能

在提交功能请求之前，请：

1. **检查现有功能**: 确认功能尚未存在
2. **检查路线图**: 查看项目路线图是否已计划该功能
3. **考虑范围**: 确保功能符合项目目标

创建功能请求时，请包括：

- 清晰的功能描述
- 使用场景和动机
- 可能的实现方案
- 是否愿意实现该功能

### 改进文档

文档改进总是受欢迎的！您可以：

- 修复拼写或语法错误
- 改进现有文档的清晰度
- 添加缺失的文档
- 提供更多示例
- 翻译文档

## 开发流程

### 1. Fork 和 Clone 项目

```bash
# Fork 项目到您的 GitHub 账号
# 然后 Clone 到本地
git clone https://github.com/YOUR_USERNAME/spring-agent.git
cd spring-agent

# 添加上游仓库
git remote add upstream https://github.com/jackelyj/spring-agent.git
```

### 2. 创建分支

```bash
# 更新本地 master
git checkout master
git pull upstream master

# 创建功能分支
git checkout -b feature/your-feature-name

# 或创建修复分支
git checkout -b fix/issue-number-description
```

分支命名规范：
- `feature/xxx` - 新功能
- `fix/xxx` - Bug 修复
- `docs/xxx` - 文档更新
- `refactor/xxx` - 代码重构
- `test/xxx` - 测试相关
- `chore/xxx` - 构建或辅助工具

### 3. 开发环境设置

```bash
# 安装依赖
./gradlew build

# 运行测试
./gradlew test

# 启动应用
./gradlew bootRun
```

### 4. 进行更改

在开发过程中：

- 保持提交小而集中
- 编写清晰的提交信息
- 遵循编码规范
- 添加必要的测试
- 更新相关文档

### 5. 测试更改

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests YourTestClass

# 检查代码风格
./gradlew checkstyleMain checkstyleTest

# 生成测试覆盖率报告
./gradlew jacocoTestReport
```

### 6. 提交更改

```bash
# 添加更改
git add .

# 提交（遵循提交规范）
git commit -m "feat: add new feature"

# 推送到您的 Fork
git push origin feature/your-feature-name
```

### 7. 创建 Pull Request

1. 访问您的 Fork 页面
2. 点击 "Pull Request"
3. 填写 PR 模板
4. 等待审查

## 编码规范

### Java 代码规范

遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)，主要规则：

#### 1. 命名约定

```java
// 类名：大驼峰
public class ChatService {}

// 方法和变量：小驼峰
private String conversationId;
public void processMessage() {}

// 常量：全大写下划线分隔
private static final int MAX_RETRIES = 3;

// 包名：全小写
package dev.jackelyj.spring_agent.service;
```

#### 2. 格式化

```java
// 缩进：4个空格（不使用 Tab）
public class Example {
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}

// 行长度：不超过 120 字符

// 空行：逻辑块之间使用空行分隔
public void method1() {
    // logic 1
}

public void method2() {
    // logic 2
}
```

#### 3. 注释

```java
/**
 * Javadoc 注释用于公共 API
 * 
 * @param message 用户消息
 * @return 响应结果
 */
public ChatResponse chat(String message) {
    // 行内注释解释复杂逻辑
    // TODO: 待办事项注释
}
```

#### 4. 最佳实践

```java
// 使用 Optional 避免 null
public Optional<User> findUser(String id) {
    return Optional.ofNullable(userRepository.findById(id));
}

// 使用 try-with-resources
try (InputStream is = new FileInputStream(file)) {
    // use stream
}

// 使用有意义的变量名
// Bad
String s = "John";

// Good
String userName = "John";

// 保持方法简短（一般不超过 30 行）
// 一个方法只做一件事
```

### Spring 最佳实践

```java
// 使用构造器注入（推荐）
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final ConversationMemoryService memoryService;
    
    public ChatServiceImpl(ChatClient chatClient, 
                          ConversationMemoryService memoryService) {
        this.chatClient = chatClient;
        this.memoryService = memoryService;
    }
}

// 避免字段注入
// @Autowired  // ❌ 不推荐
// private ChatClient chatClient;

// 使用接口抽象
public interface ChatService {
    ChatResponse chat(ChatRequest request);
}

// 使用 @Tool 注解创建工具
@Component
public class MyTools {
    @Tool(description = "Clear description")
    public String myTool(
        @ToolParam(description = "Parameter description") String param) {
        return "result";
    }
}
```

## 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

### 提交消息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整（不影响代码逻辑）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建或辅助工具
- `ci`: CI/CD 相关
- `revert`: 回滚提交

### Scope 范围（可选）

- `tools`: 工具相关
- `service`: 服务层
- `controller`: 控制器层
- `config`: 配置相关
- `test`: 测试相关
- `docs`: 文档相关
- `deps`: 依赖更新

### 示例

```bash
# 新功能
git commit -m "feat(tools): add weather query tool"

# Bug 修复
git commit -m "fix(service): resolve memory leak in chat service"

# 文档更新
git commit -m "docs: update README with Docker instructions"

# 代码重构
git commit -m "refactor(controller): simplify error handling"

# 完整提交消息
git commit -m "feat(tools): add weather query tool

Add a new tool that can query weather information using OpenWeatherMap API.

- Add WeatherTools component
- Implement getCurrentWeather method
- Add integration tests
- Update documentation

Closes #123"
```

## Pull Request 流程

### 1. PR 检查清单

在提交 PR 之前，确保：

- [ ] 代码遵循项目编码规范
- [ ] 所有测试通过
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 提交消息符合规范
- [ ] PR 描述清晰完整
- [ ] 没有合并冲突
- [ ] 代码已经过自我审查

### 2. PR 模板

提交 PR 时，请完整填写 PR 模板，包括：

- PR 描述和目的
- 相关 Issue
- 变更类型
- 变更内容列表
- 测试说明
- 截图（如适用）
- Checklist

### 3. 审查过程

PR 审查流程：

1. **自动检查**: GitHub Actions 自动运行测试和检查
2. **代码审查**: 维护者会审查您的代码
3. **反馈**: 可能会收到修改建议
4. **更新**: 根据反馈更新代码
5. **合并**: 审查通过后合并到主分支

### 4. 响应反馈

当收到审查反馈时：

- 及时响应审查意见
- 接受建设性批评
- 解释您的设计决策
- 进行必要的修改
- 标记已解决的评论

### 5. 保持同步

在 PR 期间保持与上游同步：

```bash
# 获取上游更新
git fetch upstream

# 合并到您的分支
git checkout feature/your-feature
git merge upstream/master

# 或使用 rebase（保持提交历史清晰）
git rebase upstream/master

# 解决冲突后推送
git push origin feature/your-feature --force-with-lease
```

## 测试要求

### 测试覆盖率

- 新代码必须有测试覆盖
- 整体覆盖率目标：≥ 70%
- 变更代码覆盖率：≥ 80%

### 测试类型

#### 1. 单元测试

```java
@Test
void shouldCalculateCorrectly() {
    // Given
    CalculatorTools calculator = new CalculatorTools();
    
    // When
    String result = calculator.add(5.0, 3.0);
    
    // Then
    assertThat(result).contains("8.0");
}
```

#### 2. 集成测试

```java
@SpringBootTest
@Tag("integration")
class ChatServiceIntegrationTest {
    @Autowired
    private ChatService chatService;
    
    @Test
    void shouldHandleToolCalling() {
        // Test with real components
    }
}
```

#### 3. Controller 测试

```java
@WebMvcTest(ChatController.class)
class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ChatService chatService;
    
    @Test
    void shouldReturnChatResponse() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
            .andExpect(status().isOk());
    }
}
```

### 运行测试

```bash
# 运行所有测试
./gradlew test

# 运行单元测试（排除集成测试）
./gradlew test --exclude-tag integration

# 运行集成测试
./gradlew test --tests "*IntegrationTest"

# 生成覆盖率报告
./gradlew jacocoTestReport

# 查看报告
open build/reports/jacoco/test/html/index.html
```

## 文档要求

### 代码文档

- 所有公共 API 必须有 Javadoc
- 复杂逻辑需要注释说明
- 使用有意义的变量和方法名

### 用户文档

当添加新功能时，更新以下文档：

- `README.md` - 如果影响使用方式
- `docs/` 目录中的相关指南
- API 文档示例
- 配置说明

### 文档风格

- 使用清晰简洁的语言
- 提供代码示例
- 使用适当的格式化
- 包含必要的图表或截图

## 版本发布

项目使用语义化版本 (SemVer)：

- `MAJOR.MINOR.PATCH` (例如: 1.2.3)
- `MAJOR`: 不兼容的 API 变更
- `MINOR`: 向后兼容的新功能
- `PATCH`: 向后兼容的 Bug 修复

## 获取帮助

如果您需要帮助：

1. **查看文档**: 首先查看 `docs/` 目录
2. **搜索 Issues**: 查看是否有人遇到类似问题
3. **创建 Discussion**: 在 GitHub Discussions 提问
4. **联系维护者**: 在 Issue 中 @维护者

## 许可证

通过贡献代码，您同意您的贡献将在 MIT 许可证下授权。

## 致谢

感谢所有贡献者对项目的付出！您的贡献让 Spring Agent 变得更好。

---

再次感谢您的贡献！🎉

