# 多阶段构建 Dockerfile for Spring Agent
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制 Gradle 配置文件
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 下载依赖（利用 Docker 缓存）
RUN ./gradlew dependencies --no-daemon || return 0

# 复制源代码
COPY src ./src

# 构建应用
RUN ./gradlew build -x test --no-daemon

# 运行时镜像
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="jackelyj <your-email@example.com>"
LABEL description="Spring AI Tool Calling with Ollama"
LABEL version="1.0.0"

# 安装必要的运行时工具
RUN apk add --no-cache \
    curl \
    bash \
    tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/build/libs/*.jar app.jar

# 更改所有权
RUN chown -R spring:spring /app

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health/service || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -XX:+PrintGCDetails \
               -XX:+PrintGCDateStamps \
               -Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=10,filesize=10M \
               -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

