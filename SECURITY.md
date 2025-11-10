# 安全策略

## 支持的版本

我们为以下版本提供安全更新：

| 版本 | 支持状态 |
| ------- | ------------------ |
| 0.0.x   | :white_check_mark: |

## 报告漏洞

我们非常重视安全问题。如果您发现了潜在的安全漏洞，请遵循以下负责任的披露流程：

### 1. 不要公开披露

**请勿**在 GitHub Issues、Pull Requests 或其他公共渠道披露安全漏洞。

### 2. 私密报告

通过以下方式之一私密报告：

#### GitHub Security Advisory（推荐）
1. 访问项目的 [Security](https://github.com/jackelyj/spring-agent/security) 页面
2. 点击 "Report a vulnerability"
3. 填写详细信息

#### 电子邮件
发送邮件至：security@example.com

### 3. 报告内容

请在报告中包含以下信息：

- **漏洞描述**: 清晰描述安全问题
- **影响范围**: 说明哪些版本受影响
- **重现步骤**: 详细的复现步骤
- **概念验证**: 如果可能，提供 PoC 代码
- **影响评估**: 描述潜在的安全影响
- **建议修复**: 如果有修复建议，请提供
- **联系方式**: 您的联系方式以便后续沟通

### 4. 响应时间

我们承诺：

- **24小时内**: 确认收到报告
- **72小时内**: 初步评估和响应
- **30天内**: 提供修复或缓解措施（根据严重程度）

### 5. 公开流程

1. 我们将与您协作验证和修复问题
2. 准备安全补丁
3. 协调公开时间（通常在修复发布后）
4. 发布安全公告
5. 在 CHANGELOG 中记录（如果适用）

## 安全最佳实践

### 用户指南

#### 1. 网络安全

```yaml
# 不要暴露内部服务到公网
server:
  address: 127.0.0.1  # 仅本地访问
```

#### 2. 环境变量

```bash
# 使用环境变量存储敏感配置
export OLLAMA_API_KEY=your-secret-key
export DATABASE_PASSWORD=your-db-password
```

#### 3. 依赖更新

```bash
# 定期更新依赖
./gradlew dependencyUpdates

# 检查安全漏洞
./gradlew dependencyCheckAnalyze
```

#### 4. Docker 安全

```dockerfile
# 使用非 root 用户
USER spring:spring

# 最小化镜像
FROM eclipse-temurin:21-jre-alpine
```

#### 5. 数据验证

```java
// 验证用户输入
@Valid @RequestBody ChatRequest request
```

### 开发者指南

#### 1. 安全编码

```java
// 避免 SQL 注入
@Query("SELECT u FROM User u WHERE u.id = :id")
Optional<User> findById(@Param("id") String id);

// 防止 XSS
String sanitized = StringEscapeUtils.escapeHtml4(userInput);

// 安全的随机数
SecureRandom random = new SecureRandom();
```

#### 2. 敏感信息

```java
// 不要记录敏感信息
log.info("User logged in: {}", username);  // ✅
log.info("Password: {}", password);        // ❌

// 不要在代码中硬编码密钥
private static final String API_KEY = "hardcoded-key";  // ❌
```

#### 3. 依赖管理

- 使用官方仓库
- 验证依赖完整性
- 避免使用已知漏洞的版本
- 启用 Dependabot

#### 4. 配置安全

```yaml
# application.yml
spring:
  # 禁用不必要的端点
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
  
  # 启用安全头
  security:
    headers:
      content-security-policy: "default-src 'self'"
```

## 已知问题

当前版本没有已知的安全问题。

历史安全问题将在此列出（如有）。

## 安全相关配置

### 1. CORS 配置

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://trusted-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        // 配置其他 CORS 设置
        return source;
    }
}
```

### 2. 速率限制

考虑实现速率限制以防止滥用：

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    // 实现速率限制逻辑
}
```

### 3. 审计日志

启用审计日志记录重要操作：

```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning("execution(* dev.jackelyj.spring_agent.service.*.*(..))")
    public void logServiceCall(JoinPoint joinPoint) {
        // 记录服务调用
    }
}
```

## 安全检查清单

在部署前检查：

- [ ] 更新所有依赖到最新稳定版本
- [ ] 运行安全扫描工具
- [ ] 审查配置文件中的敏感信息
- [ ] 启用 HTTPS（生产环境）
- [ ] 配置防火墙规则
- [ ] 实施访问控制
- [ ] 启用日志记录和监控
- [ ] 定期备份数据
- [ ] 制定事件响应计划

## 安全工具

项目使用以下安全工具：

### 静态分析
- **SpotBugs**: 查找潜在 bug
- **Checkstyle**: 代码风格检查
- **OWASP Dependency Check**: 依赖漏洞扫描

### 运行时保护
- **Trivy**: 容器镜像扫描
- **CodeQL**: 代码安全分析

### CI/CD
- **GitHub Actions**: 自动化安全检查
- **Dependabot**: 自动依赖更新

## 合规性

### OWASP Top 10

项目遵循 [OWASP Top 10](https://owasp.org/www-project-top-ten/) 安全准则：

1. **注入攻击**: 使用参数化查询
2. **身份验证失效**: 实施强认证机制
3. **敏感数据暴露**: 加密敏感数据
4. **XML 外部实体**: 禁用 XXE
5. **访问控制失效**: 实施最小权限原则
6. **安全配置错误**: 遵循安全配置最佳实践
7. **跨站脚本**: 输入验证和输出编码
8. **不安全的反序列化**: 避免反序列化不可信数据
9. **使用含有已知漏洞的组件**: 定期更新依赖
10. **日志记录和监控不足**: 实施完善的日志系统

## 漏洞奖励计划

目前我们没有正式的漏洞奖励计划，但我们会：

- 在 Security Advisories 中致谢报告者
- 在项目文档中感谢贡献
- 考虑在未来实施奖励计划

## 安全更新通知

获取安全更新：

1. **Watch 项目**: 在 GitHub 上 watch 项目
2. **订阅 Releases**: 订阅 GitHub Releases
3. **RSS 订阅**: 订阅项目的 Security Advisories RSS
4. **社交媒体**: 关注项目社交媒体账号

## 联系方式

- **安全问题**: security@example.com
- **一般问题**: 通过 GitHub Issues
- **紧急情况**: 请通过邮件联系并标注 [URGENT]

## 致谢

感谢所有负责任地报告安全问题的研究人员和用户！

### 安全贡献者

（当有人报告安全问题后，将在此列出）

---

最后更新: 2025-11-10

